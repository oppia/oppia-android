package org.oppia.android.domain.platformparameter

import dagger.Module
import dagger.Provides
import org.oppia.android.app.model.PlatformParameterId
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.util.platformparameter.CacheLatexRendering
import org.oppia.android.util.platformparameter.ForcedAppUpdateVersionCode
import org.oppia.android.util.platformparameter.LowestSupportedApiLevel
import org.oppia.android.util.platformparameter.NpsSurveyGracePeriodInDays
import org.oppia.android.util.platformparameter.NpsSurveyMinimumAggregateLearningTimeInATopicInMinutes
import org.oppia.android.util.platformparameter.OptionalAppUpdateVersionCode
import org.oppia.android.util.platformparameter.PerformanceMetricsCollectionHighFrequencyTimeIntervalInMinutes
import org.oppia.android.util.platformparameter.PerformanceMetricsCollectionLowFrequencyTimeIntervalInMinutes
import org.oppia.android.util.platformparameter.PerformanceMetricsCollectionUploadTimeIntervalInMinutes
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.platformparameter.SplashScreenWelcomeMsg
import org.oppia.android.util.platformparameter.SyncUpWorkerTimePeriodHours

// TODO(#5835): Remove this module.
/** Dagger module for providing injectable bindings for platform parameters. */
@Module
class PlatformParameterBindingModule {
  @Provides
  @SplashScreenWelcomeMsg
  fun provideSplashScreenWelcomeMsgParam(processState: PlatformParameterProcessState) =
    processState.retrieveBooleanParameter(PlatformParameterId.SPLASH_SCREEN_WELCOME_MESSAGE)

  @Provides
  @SyncUpWorkerTimePeriodHours
  fun provideSyncUpWorkerTimePeriod(processState: PlatformParameterProcessState) =
    processState.retrieveIntParameter(PlatformParameterId.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS)

  @Provides
  @CacheLatexRendering
  fun provideCacheLatexRendering(processState: PlatformParameterProcessState) =
    processState.retrieveBooleanParameter(PlatformParameterId.CACHE_LATEX_RENDERING)

  @Provides
  @PerformanceMetricsCollectionUploadTimeIntervalInMinutes
  fun providePerformanceMetricsCollectionUploadTimeIntervalInMinutes(
    processState: PlatformParameterProcessState
  ) = processState.retrieveIntParameter(
    PlatformParameterId.PERFORMANCE_METRICS_COLLECTION_UPLOAD_TIME_INTERVAL_IN_MINUTES
  )

  @Provides
  @PerformanceMetricsCollectionHighFrequencyTimeIntervalInMinutes
  fun providePerformanceMetricsCollectionHighFrequencyTimeIntervalInMinutes(
    processState: PlatformParameterProcessState
  ) = processState.retrieveIntParameter(
    PlatformParameterId.PERFORMANCE_METRICS_COLLECTION_HIGH_FREQUENCY_TIME_INTERVAL_IN_MINUTES
  )

  @Provides
  @PerformanceMetricsCollectionLowFrequencyTimeIntervalInMinutes
  fun providePerformanceMetricsCollectionLowFrequencyTimeIntervalInMinutes(
    processState: PlatformParameterProcessState
  ) = processState.retrieveIntParameter(
    PlatformParameterId.PERFORMANCE_METRICS_COLLECTION_LOW_FREQUENCY_TIME_INTERVAL_IN_MINUTES
  )

  @Provides
  @OptionalAppUpdateVersionCode
  fun provideOptionalAppUpdateVersionCode(processState: PlatformParameterProcessState) =
    processState.retrieveIntParameter(PlatformParameterId.OPTIONAL_APP_UPDATE_VERSION_CODE)

  @Provides
  @ForcedAppUpdateVersionCode
  fun provideForcedAppUpdateVersionCode(processState: PlatformParameterProcessState) =
    processState.retrieveIntParameter(PlatformParameterId.FORCED_APP_UPDATE_VERSION_CODE)

  @Provides
  @LowestSupportedApiLevel
  fun provideLowestSupportedApiLevel(processState: PlatformParameterProcessState) =
    processState.retrieveIntParameter(PlatformParameterId.LOWEST_SUPPORTED_API_LEVEL)

  @Provides
  @NpsSurveyGracePeriodInDays
  fun provideNpsSurveyGracePeriodInDays(processState: PlatformParameterProcessState) =
    processState.retrieveIntParameter(PlatformParameterId.NPS_SURVEY_GRACE_PERIOD_IN_DAYS)

  @Provides
  @NpsSurveyMinimumAggregateLearningTimeInATopicInMinutes
  fun provideNpsSurveyMinimumAggregateLearningTimeInATopicInMinutes(
    processState: PlatformParameterProcessState
  ) = processState.retrieveIntParameter(
    PlatformParameterId.NPS_SURVEY_MINIMUM_AGGREGATE_LEARNING_TIME_IN_A_TOPIC_IN_MINUTES
  )

  private companion object {
    private fun PlatformParameterProcessState.retrieveIntParameter(
      platformParameterId: PlatformParameterId
    ): PlatformParameterValue<Int> {
      return object : PlatformParameterValue<Int> {
        override val value = retrievePlatformParameterIntegerState(platformParameterId)
        override val syncStatus = SyncStatus.SYNC_STATUS_UNSPECIFIED
      }
    }

    private fun PlatformParameterProcessState.retrieveBooleanParameter(
      platformParameterId: PlatformParameterId
    ): PlatformParameterValue<Boolean> {
      return object : PlatformParameterValue<Boolean> {
        override val value = retrievePlatformParameterBooleanState(platformParameterId)
        override val syncStatus = SyncStatus.SYNC_STATUS_UNSPECIFIED
      }
    }
  }
}
