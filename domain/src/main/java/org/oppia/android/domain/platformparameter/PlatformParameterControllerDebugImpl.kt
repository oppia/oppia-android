package org.oppia.android.domain.platformparameter

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import org.oppia.android.app.model.EphemeralFeatureFlag
import org.oppia.android.app.model.EphemeralPlatformParameter
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
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
  @BackgroundDispatcher private val backgroundCoroutineDispatcher: CoroutineDispatcher
) : PlatformParameterController {

  // Note that the 'by lazy' here guarantees thread-safe and singleton initialization.
  private val initializationDeferred by lazy { loadParametersInternalAsync() }
  private val parametersAreLoadedFlow by lazy { MutableStateFlow(false) }
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
      // TODO(#5835): Finish implementing forcing remote parameter downloads.

      return@createInMemoryDataProviderAsync AsyncResult.Success(Unit)
    }
  }

  /** Returns a merged list of platform parameters by resolving values. */
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

  /** Returns a merged list of feature flags by resolving values. */
  fun loadEphemeralFeatureFlags(): DataProvider<List<EphemeralFeatureFlag>> {
    return dataProviders.createInMemoryDataProviderAsync(LOAD_EPHEMERAL_FEATURE_FLAGS_PROVIDER_ID) {
      val defaultFlags = platformParameterControllerProdImpl.loadSupportedFeatureFlags()
      val remoteFlags = platformParameterControllerProdImpl.loadRemoteFeatureFlags()
      val remoteFlagById = remoteFlags.associateBy { it.id }

      val ephemeralFlags = defaultFlags.map { flagDefinition ->
        val remoteFlag = remoteFlagById[flagDefinition.id]

        val currentValue = remoteFlag?.remoteIsEnabled ?: flagDefinition.defaultIsEnabled
        val syncStatus = remoteFlag?.syncStatus
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

      // Erase the data provider's value so that callers cannot inadvertently depend on the actual
      // list of parameters available.
    }
  }

  private companion object {
    private const val LOAD_EPHEMERAL_PLATFORM_PARAMETERS_PROVIDER_ID =
      "load_ephemeral_platform_parameters"
    private const val DOWNLOAD_REMOTE_PARAMETERS_PROVIDER_ID = "download_remote_parameters"
    private const val LOAD_EPHEMERAL_FEATURE_FLAGS_PROVIDER_ID = "load_ephemeral_feature_flags"
    private const val GET_PARAMETER_INITIALIZATION_STATUS_PROVIDER_ID =
      "get_parameter_initialization_status"
  }
}
