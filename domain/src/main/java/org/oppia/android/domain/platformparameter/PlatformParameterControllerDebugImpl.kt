package org.oppia.android.domain.platformparameter

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import org.oppia.android.app.model.EphemeralFeatureFlag
import org.oppia.android.app.model.EphemeralPlatformParameter
import org.oppia.android.app.model.LocalOverridePlatformParameterDatabase
import org.oppia.android.app.model.OverriddenFeatureFlag
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.extensions.safeForEach
import org.oppia.android.util.threading.BackgroundDispatcher
import javax.inject.Inject

/**
 * Debug implementation for the controller to manage and synchronize platform parameters and
 * feature flags.
 */
class PlatformParameterControllerDebugImpl @Inject constructor(
  private val platformParameterControllerProdImpl: PlatformParameterControllerProdImpl,
  private val dataProviders: DataProviders,
  private val oppiaLogger: OppiaLogger,
  private val processState: PlatformParameterProcessState,
  private val cacheStoreFactory: PersistentCacheStore.Factory,
  @BackgroundDispatcher private val backgroundCoroutineDispatcher: CoroutineDispatcher
) : PlatformParameterController {

  private val databaseStore by lazy {
    cacheStoreFactory.create(
      DATABASE_NAME, LocalOverridePlatformParameterDatabase.getDefaultInstance()
    )
  }

  // Note that the 'by lazy' here guarantees thread-safe and singleton initialization.
  private val initializationDeferred by lazy { loadParametersInternalAsync() }
  private val parametersAreLoadedFlow by lazy { MutableStateFlow(false) }

  init {
    // Ensure that parameters and flags are fully loaded ahead of a call to retrieveData() since
    // loadRemotePlatformParameters() needs to guarantee that the existing parameters are loaded
    // despite any later calls to downloadRemoteParameters(). Note that this will also guarantee
    // that the file is created on-disk (which is fine--it will just initially be empty).
    databaseStore.primeInMemoryAndDiskCacheAsync(
      updateMode = PersistentCacheStore.UpdateMode.UPDATE_IF_NEW_CACHE,
      publishMode = PersistentCacheStore.PublishMode.PUBLISH_TO_IN_MEMORY_CACHE
    ).invokeOnCompletion { failure ->
      failure?.let {
        oppiaLogger.e(
          "PlatformParameterController", "Failed to load platform params/feature flags.", failure
        )
      }
    }
  }

  override fun loadParametersAsync() = initializationDeferred

  override fun getParameterInitializationStatus(): DataProvider<Boolean> {
    return dataProviders.run {
      parametersAreLoadedFlow.convertToAutomaticDataProvider(
        GET_PARAMETER_INITIALIZATION_STATUS_PROVIDER_ID
      )
    }
  }

  override fun downloadRemoteParameters(): DataProvider<Unit> {
    return dataProviders.createInMemoryDataProviderAsync(DOWNLOAD_REMOTE_PARAMETERS_PROVIDER_ID) {
      // TODO(#5345): Finish implementing forcing remote parameter downloads.
      return@createInMemoryDataProviderAsync AsyncResult.Success(Unit)
    }
  }

  /** Loads the locally overridden feature flags from the database. */
  suspend fun loadLocalOverriddenFeatureFlags(): List<OverriddenFeatureFlag> {
    return databaseStore.readDataAsync().await().overriddenFeatureFlagList
  }

  /**
   * Returns a [DataProvider] that loads the current values of all supported
   * platform parameters as a list of [EphemeralPlatformParameter].
   *
   * For each parameter, uses a remote override if available; otherwise falls
   * back to its default value, with the appropriate [SyncStatus].
   */
  fun loadEphemeralPlatformParameters(): DataProvider<List<EphemeralPlatformParameter>> {
    return dataProviders.createInMemoryDataProviderAsync(
      LOAD_EPHEMERAL_PLATFORM_PARAMETERS_PROVIDER_ID
    ) {
      val defaultParameters = platformParameterControllerProdImpl.loadSupportedPlatformParameters()
      val remoteParameters = platformParameterControllerProdImpl.loadRemotePlatformParameters()
      val remoteParamById = remoteParameters.associateBy { it.id }

      val ephemeralParameters = defaultParameters.map { paramDefinition ->
        val remoteParam = remoteParamById[paramDefinition.id]

        val currentValue = remoteParam?.remoteValue ?: paramDefinition.defaultValue
        val syncStatus = remoteParam?.syncStatus
          ?: SyncStatus.NOT_SYNCED_FROM_SERVER

        EphemeralPlatformParameter.newBuilder().apply {
          this.id = paramDefinition.id
          this.currentValue = currentValue
          this.syncStatus = syncStatus
        }.build()
      }

      return@createInMemoryDataProviderAsync AsyncResult.Success(ephemeralParameters)
    }
  }

  /**
   * Returns a [DataProvider] that loads the current values of all supported
   * feature flags as a list of [EphemeralFeatureFlag].
   *
   * For each flag, uses a remote override if available; otherwise falls
   * back to its default value, with the appropriate [SyncStatus].
   */
  fun loadEphemeralFeatureFlags(): DataProvider<List<EphemeralFeatureFlag>> {
    return dataProviders.createInMemoryDataProviderAsync(
      LOAD_EPHEMERAL_FEATURE_FLAGS_PROVIDER_ID
    ) {
      val defaultFlags = platformParameterControllerProdImpl.loadSupportedFeatureFlags()
      val remoteFlags = platformParameterControllerProdImpl.loadRemoteFeatureFlags()
      val remoteFlagById = remoteFlags.associateBy { it.id }

      val localOverrides = loadLocalOverriddenFeatureFlags()
      val localFlagById = localOverrides.associateBy { it.id }

      val ephemeralFlags = defaultFlags.map { flagDefinition ->
        val localFlag = localFlagById[flagDefinition.id]
        val remoteFlag = remoteFlagById[flagDefinition.id]

        val currentValue = localFlag?.overriddenValue
          ?: remoteFlag?.remoteIsEnabled
          ?: flagDefinition.defaultIsEnabled

        val syncStatus = localFlag?.let { SyncStatus.LOCAL_OVERRIDE }
          ?: remoteFlag?.syncStatus
          ?: SyncStatus.NOT_SYNCED_FROM_SERVER

        EphemeralFeatureFlag.newBuilder().apply {
          this.id = flagDefinition.id
          this.currentValue = currentValue
          this.syncStatus = syncStatus
        }.build()
      }

      return@createInMemoryDataProviderAsync AsyncResult.Success(ephemeralFlags)
    }
  }

  private fun loadParametersInternalAsync(): Deferred<Unit> {
    return CoroutineScope(backgroundCoroutineDispatcher).async {
      val ephemeralPlatformParametersResult =
        loadEphemeralPlatformParameters().retrieveData()
      val ephemeralFeatureFlagsResult = loadEphemeralFeatureFlags().retrieveData()
      val platStatesById = when (ephemeralPlatformParametersResult) {
        is AsyncResult.Failure -> {
          oppiaLogger.e(
            "PlatformParameterControllerDebugImpl",
            "Failed to load ephemeral platform parameters:",
            ephemeralPlatformParametersResult.error
          )
          emptyMap()
        }
        is AsyncResult.Success ->
          ephemeralPlatformParametersResult.value.associate { it.id to it.currentValue }
        is AsyncResult.Pending -> emptyMap()
      }

      val flagStatesById = when (ephemeralFeatureFlagsResult) {
        is AsyncResult.Failure -> {
          oppiaLogger.e(
            "PlatformParameterControllerDebugImpl",
            "Failed to load ephemeral feature flags:",
            ephemeralFeatureFlagsResult.error
          )
          emptyMap()
        }
        is AsyncResult.Success ->
          ephemeralFeatureFlagsResult.value.associate { it.id to it.currentValue }
        else -> emptyMap()
      }

      val statusesById = when (ephemeralFeatureFlagsResult) {
        is AsyncResult.Failure -> {
          oppiaLogger.e(
            "PlatformParameterControllerDebugImpl",
            "Failed to load ephemeral feature flag sync statuses:",
            ephemeralFeatureFlagsResult.error
          )
          emptyMap()
        }
        is AsyncResult.Success ->
          ephemeralFeatureFlagsResult.value.associate { it.id to it.syncStatus }
        else -> emptyMap()
      }

      processState.initializePlatformParameters(platStatesById)
      processState.initializeFeatureFlags(flagStatesById)
      processState.initializeFeatureFlagSyncStatuses(statusesById)

      // Let observers know that parameters have been initialized.
      parametersAreLoadedFlow.value = true
    }
  }

  /**
   * Updates the local override database with the provided list of overridden feature flags.
   *
   * @param overriddenFlags the list of [OverriddenFeatureFlag]s to store as local overrides.
   * @return a [DataProvider] representing the result of the update operation.
   */
  fun updateOverriddenFeatureFlags(
    overriddenFlags: List<OverriddenFeatureFlag>
  ): DataProvider<Any?> {
    return dataProviders.createInMemoryDataProviderAsync(
      UPDATE_OVERRIDDEN_FEATURE_FLAGS_PROVIDER_ID
    ) {
      databaseStore.storeDataAsync(updateInMemoryCache = true) { oldDatabase ->
        val existingOverrides = oldDatabase.overriddenFeatureFlagList.associateBy { it.id }
        val latestValues = existingOverrides.toMutableMap().apply {
          overriddenFlags.safeForEach { override ->
            this[override.id] = override
          }
        }
        oldDatabase.toBuilder()
          .clearOverriddenFeatureFlag()
          .addAllOverriddenFeatureFlag(latestValues.values)
          .build()
      }.await()

      return@createInMemoryDataProviderAsync AsyncResult.Success(Unit)
    }
  }

  private companion object {
    private const val LOAD_EPHEMERAL_PLATFORM_PARAMETERS_PROVIDER_ID =
      "load_ephemeral_platform_parameters"
    private const val DOWNLOAD_REMOTE_PARAMETERS_PROVIDER_ID = "download_remote_parameters"
    private const val LOAD_EPHEMERAL_FEATURE_FLAGS_PROVIDER_ID = "load_ephemeral_feature_flags"
    private const val UPDATE_OVERRIDDEN_FEATURE_FLAGS_PROVIDER_ID =
      "update_overridden_feature_flags"
    private const val GET_PARAMETER_INITIALIZATION_STATUS_PROVIDER_ID =
      "get_parameter_initialization_status"
    private const val DATABASE_NAME =
      "local_overridden_platform_parameter_and_feature_flag_database"
  }
}
