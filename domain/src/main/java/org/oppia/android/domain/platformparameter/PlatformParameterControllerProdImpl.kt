package org.oppia.android.domain.platformparameter

/**
 * Production implementation for the controller to manage and synchronize platform parameters and
 * feature flags.
 */
@Singleton
class PlatformParameterControllerProdImpl @Inject constructor(
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val dataProviders: DataProviders,
  private val configRetriever: PlatformParameterConfigRetriever,
  private val platformParameterService: PlatformParameterService,
  private val processState: PlatformParameterProcessState
): PlatformParameterController {
  private val databaseStore by lazy {
    cacheStoreFactory.create(
      DATABASE_NAME, RemotePlatformParameterAndFeatureFlagDatabase.getDefaultInstance()
    ).also {
      // Ensure that flags are fully loaded ahead of a call to retrieveData() since loadParameters()
      // guarantees exactly one non-pending result. Note that this will also guarantee that the file
      // is created on-disk (which is fine--it will just be empty).
      it.primeInMemoryAndDiskCacheAsync(
        updateMode = UpdateMode.UPDATE_ALWAYS,
        publishMode = PublishMode.PUBLISH_TO_IN_MEMORY_CACHE
      ).invokeOnCompletion { failure ->
        if (failure != null) {
          oppiaLogger.e(
            "PlatformParameterController", "Failed to load platform params/feature flags.", failure
          )
        }
      }
    }
  }

  override fun loadParameters(): DataProvider<Any?> {
    // Note that the returned provider will be guaranteed to happen after loading due to the
    // datastore being primed upon controller initialization. The provider may still block if the
    // attempt to load parameters happens very quickly after controller creation, but it should
    // never return pending. There's very likely to be at least some I/O blocking for loading the
    // asset definitions for the first time.
    return loadAllParameterStates().transform(LOAD_PARAMETERS_PROVIDER_ID) { params ->
      // Synchronize injectable parameter and flag state.
      val platformParams = params.filterIsInstance<ParameterState.PlatformParameter>()
      val featureFlags = params.filterIsInstance<ParameterState.FeatureFlag>()
      val platStatesById = platformParams.associate { it.definition.id to it.computeCurrentState() }
      val flagStatesById = featureFlags.associate { it.definition.id to it.computeCurrentState() }
      processState.initializePlatformParameters(platStatesById)
      processState.initializeFeatureFlags(flagStatesById)

      // Erase the data provider's value so that callers cannot inadvertently depend on the actual
      // list of parameters available.
      return@transform Unit
    }
  }

  override fun downloadRemoteParameters(): DataProvider<Any?> {
    return loadAllParameterStates().transformAsync(
      DOWNLOAD_REMOTE_PARAMETERS_PROVIDER_ID
    ) { params ->
      // Server names are guaranteed to be unique.
      val paramsByName = params.associateBy { it.remoteServerName }
      val remoteParameters = checkNotNull(fetchPlatformParametersFromRemote()) {
        "Failed to fetch platform parameters. Perhaps no network stack is available?"
      }
      remoteParameters.forEach { name, syncValue ->
        // Only save results corresponding to known flags.
        paramsByName[name]?.updateFromServer(parseRemoteParameterValue(name, syncValue))
      }

      // Recompute and save the parameters to the local database (but do not update the local memory
      // cache to avoid subscribers to loadParameters's DataProvider from being notified).
      val platformParams = params.filterIsInstance<ParameterState.PlatformParameter>()
      val featureFlags = params.filterIsInstance<ParameterState.FeatureFlag>()
      databaseStore.storeDataAsync(updateInMemoryCache = false) { oldDatabase ->
        val existingParameters = oldDatabase.remotePlatformParameterList
        val existingFlags = oldDatabase.remoteFeatureFlagList
        return@storeDataAsync oldDatabase.toBuilder().apply {
          clearRemotePlatformParameter()
          clearRemoteFeatureFlag()
          addAllRemotePlatformParameter(merge(existingParameters, platformParams.serialize()))
          addAllRemoteFeatureFlag(merge(existingFlags, featureFlags.serialize()))
        }.build()
      }.await() // Ensure the datastore updates as part of the operation's overall success.

      // Erase the data provider's value so that callers cannot inadvertently depend on the actual
      // list of parameters available.
      return@transform Unit
    }
  }

  fun loadRemotePlatformParameters(): DataProvider<List<RemotePlatformParameter>> {
    return databaseStore.transform(asdf) { database ->
      database.remotePlatformParameterList
    }
  }

  fun loadRemoteFeatureFlags(): DataProvider<List<RemoteFeatureFlag>> {
    return databaseStore.transform(asdf) { database ->
      database.remoteFeatureFlagList
    }
  }

  fun loadSupportedPlatformParameters(): DataProvider<List<PlatformParameterDefinition>> {
    return dataProviders.createInMemoryDataProviderAsync(SUPPORTED_PLATFORM_PARAMS_PROV_ID) {
      withContext(Dispatchers.IO) {
        configRetriever.loadSupportedPlatformParameters().platformParameterDefinitionList
      }
    }
  }

  fun loadSupportedFeatureFlags(): DataProvider<List<FeatureFlagDefinition>> {
    return dataProviders.createInMemoryDataProviderAsync(SUPPORTED_FEATURE_FLAGS_PROVIDER_ID) {
      withContext(Dispatchers.IO) {
        configRetriever.loadSupportedFeatureFlags().featureFlagDefinitionList
      }
    }
  }

  private fun loadAllParameterStates(): DataProvider<List<ParameterState>> {
    val platformParameterStates =
      loadSupportedPlatformParameters().combineWith(
        asdf, loadRemotePlatformParameters()
      ) { definitions, remote ->
        val remoteById = remote.associateBy { it.id }
        definitions.map { ParameterState.PlatformParameter(it, remoteById[it.id]) }
      }
    val featureFlagStates =
      loadRemoteFeatureFlags().combineWith(
        asdf, loadRemoteFeatureFlags()
      ) { definitions, remote ->
        val remoteById = remote.associateBy { it.id }
        definitions.map { ParameterState.FeatureFlag(it, remoteById[it.id]) }
      }
    return platformParameterStates.combineWith(
      asdf, featureFlags
    ) { platformParameters, featureFlags -> platformParameters + featureFlags }
  }

  private suspend fun fetchPlatformParametersFromRemote(): Response<Map<String, Any>>? {
    return withContext(Dispatchers.IO) {
      platformParameterService.getPlatformParametersByVersion(context.getVersionName()).execute()
    }
  }

  private sealed class ParameterState {
    abstract val remoteServerName: String

    abstract fun updateFromServer(value: PlatformParameterValue)

    data class PlatformParameter(val definition: PlatformParameterDefinition, val remote: RemotePlatformParameter?): ParameterState() {
      private var latestSyncFromServer: PlatformParameterValue? = null

      override val remoteServerName = definition.remoteServerName

      override fun updateFromServer(value: PlatformParameterValue) {
        latestSyncFromServer = value
      }

      // Always compute based on the remote value, if any, and fall back to the definition.
      fun computeCurrentState(): PlatformParameterValue =
        remote?.remoteValue ?: definition.defaultValue

      fun serialize(): RemotePlatformParameter? {
        // Ensure there's always a record of the parameter.
        val remoteValue = latestSyncFromServer
        val remote = this.remote ?: computeRemote()
        return if (remoteValue != null) {
          remote.toBuilder().apply {
            this.remoteValue = remoteValue
            this.syncStatus = SyncStatus.SYNCED_FROM_SERVER
          }.build()
        } else remote // Nothing changes.
      }

      private fun computeRemote() = RemotePlatformParameter.newBuilder().apply {
        this.id = definition.id
        this.syncStatus = SyncStatus.NOT_SYNCED_FROM_SERVER
      }
    }

    data class FeatureFlag(val definition: FeatureFlagDefinition, val remote: RemoteFeatureFlag?): ParameterState() {
      private var latestSyncFromServer: boolean? = null

      override val remoteServerName = definition.remoteServerName

      override fun updateFromServer(value: PlatformParameterValue) {
        check(value.valueTypeCase == PlatformParameterValue.ValueTypeCase.BOOLEAN) {
          "Expected feature flag corresponding to ${definition.id} (remote: $remoteServerName) to" +
            " be a boolean, but received from server: $value"
        }
        latestSyncFromServer = value.boolean
      }

      // Always compute based on the remote value, if any, and fall back to the definition.
      fun computeCurrentState(): Boolean = remote?.remoteIsEnabled ?: definition.defaultIsEnabled

      fun serialize(): RemoteFeatureFlag? {
        // Ensure there's always a record of the parameter.
        val remoteValue = latestSyncFromServer
        val remote = this.remote ?: computeRemote()
        return if (remoteValue != null) {
          remote.toBuilder().apply {
            this.remoteIsEnabled = remoteValue
            this.syncStatus = SyncStatus.SYNCED_FROM_SERVER
          }.build()
        } else remote // Nothing changes.
      }

      private fun computeRemote() = RemoteFeatureFlag.newBuilder().apply {
        this.id = definition.id
        this.syncStatus = SyncStatus.NOT_SYNCED_FROM_SERVER
      }
    }
  }

  private companion object {
    private const val LOAD_PARAMETERS_PROVIDER_ID = "load_parameters"
    private const val DOWNLOAD_REMOTE_PARAMETERS_PROVIDER_ID = "download_remote_parameters"
    private const val SUPPORTED_PLATFORM_PARAMS_PROV_ID = "supported_platform_params_prov_id"
    private const val SUPPORTED_FEATURE_FLAGS_PROVIDER_ID = "supported_feature_flags_provider_id"
    private const val DATABASE_NAME = "platform_parameter_and_feature_flag_database"

    private fun parseRemoteParameterValue(name: String, value: Any): PlatformParameterValue {
      return when (value) {
        is Boolean -> createBooleanParameterValue(value)
        is Int -> createIntegerParameterValue(value)
        is String -> createStringParameterValue(value)
        else -> error("Remote parameter '$name' has an unsupported value type: $value.")
      }
    }

    private fun createBooleanParameterValue(value: Boolean): PlatformParameterValue =
      PlatformParameterValue.newBuilder().apply { this.boolean = value }.build()

    private fun createIntegerParameterValue(value: Int): PlatformParameterValue =
      PlatformParameterValue.newBuilder().apply { this.integer = value }.build()

    private fun createStringParameterValue(value: String): PlatformParameterValue =
      PlatformParameterValue.newBuilder().apply { this.string = value }.build()

    private fun List<ParameterState.PlatformParameter>.serialize(): List<RemotePlatformParameter> =
      map { it.serialize() }

    private fun List<ParameterState.FeatureFlag>.serialize(): List<RemoteFeatureFlag> =
      map { it.serialize() }

    private fun merge(
      existing: List<RemotePlatformParameter>, updated: List<RemotePlatformParameter>
    ): List<RemotePlatformParameter> {
      // Take all of the updated remotes and re-add any existing remotes not included. This ensures
      // definitions (or sync results) being removed and then re-added do not require re-syncing, or
      // that state is not inconsistent due to Oppia web omitting a certain value.
      val existingById = existing.associateBy { it.id }
      val updatedById = updated.associateBy { it.id }
      val extraExistingIds = existingById.keys - updatedById.keys
      return updated + extraExistingIds.map { existingById.getValue(it) }
    }

    private fun merge(
      existing: List<RemoteFeatureFlag>, updated: List<RemoteFeatureFlag>
    ): List<RemoteFeatureFlag> {
      // Take all of the updated remotes and re-add any existing remotes not included. This ensures
      // definitions (or sync results) being removed and then re-added do not require re-syncing, or
      // that state is not inconsistent due to Oppia web omitting a certain value.
      val existingById = existing.associateBy { it.id }
      val updatedById = updated.associateBy { it.id }
      val extraExistingIds = existingById.keys - updatedById.keys
      return updated + extraExistingIds.map { existingById.getValue(it) }
    }
  }
}
