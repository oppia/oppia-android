package testing.src.main.java.org.oppia.android.testing.platformparameter

import org.oppia.android.app.model.EphemeralFeatureFlag
import org.oppia.android.app.model.EphemeralPlatformParameter
import org.oppia.android.app.model.FeatureFlagId
import org.oppia.android.app.model.PlatformParameterId
import org.oppia.android.app.model.PlatformParameterValue
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.domain.platformparameter.PlatformParameterDebugController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import javax.inject.Inject

class FakePlatformParameterDebugController @Inject constructor(
  private val dataProviders: DataProviders
) : PlatformParameterDebugController {
  override fun loadEphemeralPlatformParameters(): DataProvider<List<EphemeralPlatformParameter>> {
    return dataProviders.createInMemoryDataProviderAsync(
      LOAD_EPHEMERAL_PLATFORM_PARAMETERS_PROVIDER_ID
    ) {
      val ephemeralParameters =
        listOf(
          createEphemeralPlatformParameter(
            PlatformParameterId.SPLASH_SCREEN_WELCOME_MESSAGE,
            PlatformParameterValue.newBuilder().setBoolean(true).build(),
            SyncStatus.NOT_SYNCED_FROM_SERVER
          ),
          createEphemeralPlatformParameter(
            PlatformParameterId.OPTIONAL_APP_UPDATE_VERSION_CODE,
            PlatformParameterValue.newBuilder().setInteger(0).build(),
            SyncStatus.SYNCED_FROM_SERVER
          ),
          createEphemeralPlatformParameter(
            PlatformParameterId.PERFORMANCE_METRICS_COLLECTION_UPLOAD_TIME_INTERVAL_IN_MINUTES,
            PlatformParameterValue.newBuilder().setInteger(20).build(),
            SyncStatus.SYNCED_FROM_SERVER
          ),

        )
      return@createInMemoryDataProviderAsync AsyncResult.Success(ephemeralParameters)
    }
  }

  override fun loadEphemeralFeatureFlags(): DataProvider<List<EphemeralFeatureFlag>> {
    return dataProviders.createInMemoryDataProviderAsync(LOAD_EPHEMERAL_FEATURE_FLAGS_PROVIDER_ID) {
      val ephemeralFeatureFlags =
        listOf(
          createEphemeralFeatureFlag(
            FeatureFlagId.DOWNLOADS_SUPPORT,
            true,
            SyncStatus.SYNCED_FROM_SERVER
          ),
          createEphemeralFeatureFlag(
            FeatureFlagId.FLASHBACK_SUPPORT,
            true,
            SyncStatus.SYNCED_FROM_SERVER
          ),
          createEphemeralFeatureFlag(
            FeatureFlagId.MULTIPLE_CLASSROOMS,
            false,
            SyncStatus.NOT_SYNCED_FROM_SERVER
          ),
          createEphemeralFeatureFlag(
            FeatureFlagId.SPOTLIGHT_UI,
            false,
            SyncStatus.NOT_SYNCED_FROM_SERVER
          )
        )
      return@createInMemoryDataProviderAsync AsyncResult.Success(ephemeralFeatureFlags)
    }
  }

  private fun createEphemeralPlatformParameter(
    id: PlatformParameterId,
    currentValue: PlatformParameterValue,
    syncStatus: SyncStatus
  ): EphemeralPlatformParameter {
    return EphemeralPlatformParameter.newBuilder()
      .setId(id)
      .setCurrentValue(currentValue)
      .setSyncStatus(syncStatus)
      .build()
  }

  private fun createEphemeralFeatureFlag(
    id: FeatureFlagId,
    currentValue: Boolean,
    syncStatus: SyncStatus
  ): EphemeralFeatureFlag {
    return EphemeralFeatureFlag.newBuilder()
      .setId(id)
      .setCurrentValue(currentValue)
      .setSyncStatus(syncStatus)
      .build()
  }

  companion object {
    const val LOAD_EPHEMERAL_PLATFORM_PARAMETERS_PROVIDER_ID =
      "load_ephemeral_platform_parameters_provider_id"
    const val LOAD_EPHEMERAL_FEATURE_FLAGS_PROVIDER_ID =
      "load_ephemeral_feature_flags_provider_id"
  }
}
