package org.oppia.android.domain.platformparameter

import kotlinx.coroutines.Deferred
import org.oppia.android.util.data.DataProvider
import javax.inject.Inject
import org.oppia.android.app.model.EphemeralFeatureFlag
import org.oppia.android.app.model.EphemeralPlatformParameter
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.AsyncResult

/**
 * Debug implementation for the controller to manage and synchronize platform parameters and
 * feature flags.
 */
class PlatformParameterControllerDebugImpl @Inject constructor(
  private val platformParameterControllerProdImpl: PlatformParameterControllerProdImpl,
  private val dataProviders: DataProviders,
) : PlatformParameterController, PlatformParameterDebugController {

  override fun loadParametersAsync(): Deferred<Any?> {
    return platformParameterControllerProdImpl.loadParametersAsync()
  }

  override fun getParameterInitializationStatus(): DataProvider<Boolean> {
    return platformParameterControllerProdImpl.getParameterInitializationStatus()
  }

  override fun downloadRemoteParameters(): DataProvider<Any?> {
   return platformParameterControllerProdImpl.downloadRemoteParameters()
  }

  override fun loadEphemeralPlatformParameters(): DataProvider<List<EphemeralPlatformParameter>> {
    return dataProviders.createInMemoryDataProviderAsync(LOAD_EPHEMERAL_PLATFORM_PARAMETERS_PROVIDER_ID) {
      val defaultParameters = platformParameterControllerProdImpl.loadSupportedPlatformParameters()
      val remoteParameters = platformParameterControllerProdImpl.loadRemotePlatformParameters()
      val remoteParamById = remoteParameters.associateBy { it.id }

      val ephemeralParameters = defaultParameters.map { paramDefinition ->
        val remoteParam = remoteParamById[paramDefinition.id]

        val currentValue = remoteParam?.remoteValue ?: paramDefinition.defaultValue
        val syncStatus = remoteParam?.syncStatus ?:
        SyncStatus.NOT_SYNCED_FROM_SERVER

        EphemeralPlatformParameter.newBuilder().apply {
          this.id = paramDefinition.id
          this.currentValue = currentValue
          this.syncStatus = syncStatus
        }.build()
      }

      return@createInMemoryDataProviderAsync AsyncResult.Success(ephemeralParameters)
    }
  }

  override fun loadEphemeralFeatureFlags(): DataProvider<List<EphemeralFeatureFlag>> {
    return dataProviders.createInMemoryDataProviderAsync(LOAD_EPHEMERAL_FEATURE_FLAGS_PROVIDER_ID) {
      val defaultFlags = platformParameterControllerProdImpl.loadSupportedFeatureFlags()
      val remoteFlags = platformParameterControllerProdImpl.loadRemoteFeatureFlags()
      val remoteFlagById = remoteFlags.associateBy { it.id }

      val ephemeralFlags = defaultFlags.map { flagDefinition ->
        val remoteFlag = remoteFlagById[flagDefinition.id]

        val currentValue = remoteFlag?.remoteIsEnabled ?: flagDefinition.defaultIsEnabled
        val syncStatus = remoteFlag?.syncStatus ?:
        SyncStatus.NOT_SYNCED_FROM_SERVER

        EphemeralFeatureFlag.newBuilder().apply {
          this.id = flagDefinition.id
          this.currentValue = currentValue
          this.syncStatus = syncStatus
        }.build()
      }

      return@createInMemoryDataProviderAsync AsyncResult.Success(ephemeralFlags)
    }
  }

  private companion object {
    private const val LOAD_EPHEMERAL_PLATFORM_PARAMETERS_PROVIDER_ID = "load_ephemeral_platform_parameters"
    private const val LOAD_EPHEMERAL_FEATURE_FLAGS_PROVIDER_ID = "load_ephemeral_feature_flags"
  }
}