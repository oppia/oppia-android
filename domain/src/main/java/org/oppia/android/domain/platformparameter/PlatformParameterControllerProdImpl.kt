package org.oppia.android.domain.platformparameter

import android.content.Context
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.oppia.android.app.model.FeatureFlagDefinition
import org.oppia.android.app.model.PlatformParameterDefinition
import org.oppia.android.app.model.PlatformParameterValue
import org.oppia.android.app.model.RemoteFeatureFlag
import org.oppia.android.app.model.RemotePlatformParameter
import org.oppia.android.app.model.RemotePlatformParameterAndFeatureFlagDatabase
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.data.backends.gae.api.PlatformParameterService
import org.oppia.android.data.backends.gae.model.GaePlatformParameterValue
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.transform
import org.oppia.android.util.data.DataProviders.Companion.transformAsync
import org.oppia.android.util.extensions.getVersionName
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import org.oppia.android.app.model.PlatformParameter
import org.oppia.android.util.threading.BackgroundDispatcher

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
  private val processState: PlatformParameterProcessState,
  private val oppiaLogger: OppiaLogger,
  private val context: Context,
  @BackgroundDispatcher private val backgroundCoroutineDispatcher: CoroutineDispatcher,
) : PlatformParameterController {
  private val databaseStore by lazy {
    cacheStoreFactory.create(
      DATABASE_NAME, RemotePlatformParameterAndFeatureFlagDatabase.getDefaultInstance()
    )
  }
  // Note that the 'by lazy' here guarantees thread-safe and singleton initialization.
  private val initializationDeferred by lazy { loadParametersInternalAsync() }
  private val areParametersLoadedFlow by lazy { MutableStateFlow(false) }

  init {
    // Ensure that parameters and flags are fully loaded ahead of a call to retrieveData() since
    // loadParameters() guarantees exactly one non-pending result. Note that this will also
    // guarantee that the file is created on-disk (which is fine--it will just initially be empty).
    databaseStore.primeInMemoryAndDiskCacheAsync(
      updateMode = PersistentCacheStore.UpdateMode.UPDATE_IF_NEW_CACHE,
      publishMode = PersistentCacheStore.PublishMode.PUBLISH_TO_IN_MEMORY_CACHE
    ).invokeOnCompletion { failure ->
      if (failure != null) {
        oppiaLogger.e(
          "PlatformParameterController", "Failed to load platform params/feature flags.", failure
        )
      }
    }
  }

  override fun loadParametersAsync() = initializationDeferred

  override fun getParameterInitializationStatus(): DataProvider<Boolean> {
    return dataProviders.run {
      areParametersLoadedFlow.convertToAutomaticDataProvider(
        GET_PARAMETER_INITIALIZATION_STATUS_PROVIDER_ID
      )
    }
  }

  override fun downloadRemoteParameters(): DataProvider<Any?> {
    check(areParametersLoadedFlow.value) {
      "Can only remotely download parameters after they have been loaded."
    }
    return dataProviders.createInMemoryDataProviderAsync(DOWNLOAD_REMOTE_PARAMETERS_PROVIDER_ID) {
      val params = loadAllParameterStates()

      // Server names are guaranteed to be unique.
      val paramsByName = params.associateBy { it.remoteServerName }
      val remoteParametersResponse = fetchParamsFromRemote()
      check(remoteParametersResponse.isSuccessful) {
        "Failed to fetch platform parameters. Error: ${remoteParametersResponse.errorBody()}."
      }
      val remoteParameters = checkNotNull(remoteParametersResponse.body()) {
        "Failed to fetch platform parameters. Expected response to be non-empty."
      }
      remoteParameters.forEach { (name, syncValue) ->
        // Only save results corresponding to known flags.
        paramsByName[name]?.updateFromServer(syncValue.toProto(name))
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
      return@createInMemoryDataProviderAsync AsyncResult.Success(Unit)
    }
  }

  // TODO: Remove this?
  override fun updatePlatformParameterDatabase(platformParameterList: List<PlatformParameter>): DataProvider<Any?> {
    TODO("Not yet implemented")
  }

  // TODO: Remove this?
  override fun getParameterDatabase(): DataProvider<Unit> = getParameterInitializationStatus().transform("temp") { }

  suspend fun loadRemotePlatformParameters(): List<RemotePlatformParameter> {
    return databaseStore.readDataAsync().await().remotePlatformParameterList
  }

  suspend fun loadRemoteFeatureFlags(): List<RemoteFeatureFlag> {
    return databaseStore.readDataAsync().await().remoteFeatureFlagList
  }

  fun loadSupportedPlatformParameters(): List<PlatformParameterDefinition> {
    return configRetriever.loadSupportedPlatformParameters().platformParameterDefinitionList
  }

  fun loadSupportedFeatureFlags(): List<FeatureFlagDefinition> {
    return configRetriever.loadSupportedFeatureFlags().featureFlagDefinitionList
  }

  private fun loadParametersInternalAsync(): Deferred<Any?> {
    return CoroutineScope(backgroundCoroutineDispatcher).async {
      val params = loadAllParameterStates()

      // Synchronize injectable parameter and flag state.
      val platformParams = params.filterIsInstance<ParameterState.PlatformParameter>()
      val featureFlags = params.filterIsInstance<ParameterState.FeatureFlag>()
      val platStatesById = platformParams.associate { it.definition.id to it.computeCurrentState() }
      val flagStatesById = featureFlags.associate { it.definition.id to it.computeCurrentState() }
      val statusesById = featureFlags.associate { it.definition.id to it.computeCurrentStatus() }
      processState.initializePlatformParameters(platStatesById)
      processState.initializeFeatureFlags(flagStatesById)
      processState.initializeFeatureFlagSyncStatuses(statusesById)

      // Let observers know that parameters have been initialized.
      areParametersLoadedFlow.value = true

      // Erase the data provider's value so that callers cannot inadvertently depend on the actual
      // list of parameters available.
    }
  }

  private suspend fun loadAllParameterStates(): List<ParameterState> {
    val remoteParamById = loadRemotePlatformParameters().associateBy { it.id }
    val remoteFlagById = loadRemoteFeatureFlags().associateBy { it.id }
    return loadSupportedPlatformParameters().map { paramDefinition ->
      ParameterState.PlatformParameter(paramDefinition, remoteParamById[paramDefinition.id])
    } + loadSupportedFeatureFlags().map { flagDefinition ->
      ParameterState.FeatureFlag(flagDefinition, remoteFlagById[flagDefinition.id])
    }
  }

  private suspend fun fetchParamsFromRemote(): Response<Map<String, GaePlatformParameterValue>> {
    return withContext(Dispatchers.IO) {
      platformParameterService.getPlatformParametersByVersion(context.getVersionName()).execute()
    }
  }

  private sealed class ParameterState {
    abstract val remoteServerName: String

    abstract fun updateFromServer(value: PlatformParameterValue)

    data class PlatformParameter(
      val definition: PlatformParameterDefinition,
      val remote: RemotePlatformParameter?
    ) : ParameterState() {
      private var latestSyncFromServer: PlatformParameterValue? = null

      override val remoteServerName = definition.remoteServerName

      override fun updateFromServer(value: PlatformParameterValue) {
        latestSyncFromServer = value
      }

      // Always compute based on the remote value, if any, and fall back to the definition.
      fun computeCurrentState(): PlatformParameterValue =
        remote?.remoteValue ?: definition.defaultValue

      fun serialize(): RemotePlatformParameter {
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
      }.build()
    }

    data class FeatureFlag(
      val definition: FeatureFlagDefinition,
      val remote: RemoteFeatureFlag?
    ) : ParameterState() {
      private var latestSyncFromServer: Boolean? = null

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

      fun computeCurrentStatus(): SyncStatus =
        remote?.syncStatus ?: SyncStatus.NOT_SYNCED_FROM_SERVER

      fun serialize(): RemoteFeatureFlag {
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
      }.build()
    }
  }

  private companion object {
    private const val GET_PARAMETER_INITIALIZATION_STATUS_PROVIDER_ID =
      "get_parameter_initialization_status"
    private const val LOAD_PARAMETERS_PROVIDER_ID = "load_parameters"
    private const val DOWNLOAD_REMOTE_PARAMETERS_PROVIDER_ID = "download_remote_parameters"
    private const val SUPPORTED_PLATFORM_PARAMS_PROV_ID = "supported_platform_params"
    private const val SUPPORTED_FEATURE_FLAGS_PROVIDER_ID = "supported_feature_flags"
    private const val LOAD_REMOTE_AND_LOCAL_PLATFORM_PARAMS_PROVIDER_ID =
      "load_remote_and_local_platform_params"
    private const val LOAD_REMOTE_AND_LOCAL_FEATURE_FLAGS_PROVIDER_ID =
      "load_remote_and_local_feature_flags"
    private const val LOAD_ALL_PARAMETER_STATES_PROVIDER_ID = "load_all_parameter_states"
    private const val LOAD_REMOTE_PLATFORM_PARAMETERS_PROVIDER_ID =
      "load_remote_platform_parameters"
    private const val LOAD_REMOTE_FEATURE_FLAGS_PROVIDER_ID = "load_remote_feature_flags"
    private const val DATABASE_NAME = "platform_parameter_and_feature_flag_database"

    private fun GaePlatformParameterValue.toProto(name: String): PlatformParameterValue {
      return PlatformParameterValue.newBuilder().apply {
        when (this@toProto) {
          is GaePlatformParameterValue.StringValue -> this.string = value
          is GaePlatformParameterValue.IntValue -> this.integer = value
          is GaePlatformParameterValue.BooleanValue -> this.boolean = value
          GaePlatformParameterValue.UnsupportedValue ->
            error("Remote parameter '$name' has an unsupported value type: $this.")
        }
      }.build()
    }

    @JvmName("serializeListOfRemotePlatformParameter")
    private fun List<ParameterState.PlatformParameter>.serialize(): List<RemotePlatformParameter> =
      map { it.serialize() }

    @JvmName("serializeListOfRemoteFeatureFlag")
    private fun List<ParameterState.FeatureFlag>.serialize(): List<RemoteFeatureFlag> =
      map { it.serialize() }

    @JvmName("mergeListsOfRemotePlatformParameter")
    private fun merge(
      existing: List<RemotePlatformParameter>,
      updated: List<RemotePlatformParameter>
    ): List<RemotePlatformParameter> {
      // Take all of the updated remotes and re-add any existing remotes not included. This ensures
      // definitions (or sync results) being removed and then re-added do not require re-syncing, or
      // that state is not inconsistent due to Oppia web omitting a certain value.
      val existingById = existing.associateBy { it.id }
      val updatedById = updated.associateBy { it.id }
      val extraExistingIds = existingById.keys - updatedById.keys
      return updated + extraExistingIds.map { existingById.getValue(it) }
    }

    @JvmName("mergeListsRemoteFeatureFlag")
    private fun merge(
      existing: List<RemoteFeatureFlag>,
      updated: List<RemoteFeatureFlag>
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
