package org.oppia.android.domain.platformparameter

import android.content.Context
import dagger.Module
import dagger.Provides
import org.oppia.android.app.utility.getVersionCode
import org.oppia.android.util.platformparameter.CACHE_LATEX_RENDERING
import org.oppia.android.util.platformparameter.CACHE_LATEX_RENDERING_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.CacheLatexRendering
import org.oppia.android.util.platformparameter.ENABLE_APP_AND_OS_DEPRECATION_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.ENABLE_CONTINUE_BUTTON_ANIMATION_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.ENABLE_DOWNLOADS_SUPPORT_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.ENABLE_EDIT_ACCOUNTS_OPTIONS_UI_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.ENABLE_EXTRA_TOPIC_TABS_UI_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.ENABLE_INTERACTION_CONFIG_CHANGE_STATE_RETENTION_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.ENABLE_LANGUAGE_SELECTION_UI_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.ENABLE_PERFORMANCE_METRICS_COLLECTION
import org.oppia.android.util.platformparameter.ENABLE_PERFORMANCE_METRICS_COLLECTION_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.EnableAppAndOsDeprecation
import org.oppia.android.util.platformparameter.EnableContinueButtonAnimation
import org.oppia.android.util.platformparameter.EnableDownloadsSupport
import org.oppia.android.util.platformparameter.EnableEditAccountsOptionsUi
import org.oppia.android.util.platformparameter.EnableExtraTopicTabsUi
import org.oppia.android.util.platformparameter.EnableFastLanguageSwitchingInLesson
import org.oppia.android.util.platformparameter.EnableInteractionConfigChangeStateRetention
import org.oppia.android.util.platformparameter.EnableLanguageSelectionUi
import org.oppia.android.util.platformparameter.EnableLearnerStudyAnalytics
import org.oppia.android.util.platformparameter.EnableLoggingLearnerStudyIds
import org.oppia.android.util.platformparameter.EnablePerformanceMetricsCollection
import org.oppia.android.util.platformparameter.EnableSpotlightUi
import org.oppia.android.util.platformparameter.FAST_LANGUAGE_SWITCHING_IN_LESSON
import org.oppia.android.util.platformparameter.FAST_LANGUAGE_SWITCHING_IN_LESSON_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.FORCED_APP_UPDATE_VERSION_CODE
import org.oppia.android.util.platformparameter.ForcedAppUpdateVersionCode
import org.oppia.android.util.platformparameter.LEARNER_STUDY_ANALYTICS
import org.oppia.android.util.platformparameter.LOGGING_LEARNER_STUDY_IDS
import org.oppia.android.util.platformparameter.LOGGING_LEARNER_STUDY_IDS_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.LOWEST_SUPPORTED_API_LEVEL
import org.oppia.android.util.platformparameter.LOWEST_SUPPORTED_API_LEVEL_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.LowestSupportedApiLevel
import org.oppia.android.util.platformparameter.NPS_SURVEY_GRACE_PERIOD_IN_DAYS
import org.oppia.android.util.platformparameter.NPS_SURVEY_GRACE_PERIOD_IN_DAYS_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.NPS_SURVEY_MINIMUM_AGGREGATE_LEARNING_TIME_IN_A_TOPIC_IN_MINUTES
import org.oppia.android.util.platformparameter.NPS_SURVEY_MINIMUM_AGGREGATE_LEARNING_TIME_IN_A_TOPIC_IN_MINUTES_DEFAULT_VAL
import org.oppia.android.util.platformparameter.NpsSurveyGracePeriodInDays
import org.oppia.android.util.platformparameter.NpsSurveyMinimumAggregateLearningTimeInATopicInMinutes
import org.oppia.android.util.platformparameter.OPTIONAL_APP_UPDATE_VERSION_CODE
import org.oppia.android.util.platformparameter.OptionalAppUpdateVersionCode
import org.oppia.android.util.platformparameter.PERFORMANCE_METRICS_COLLECTION_HIGH_FREQUENCY_TIME_INTERVAL_IN_MINUTES
import org.oppia.android.util.platformparameter.PERFORMANCE_METRICS_COLLECTION_HIGH_FREQUENCY_TIME_INTERVAL_IN_MINUTES_DEFAULT_VAL
import org.oppia.android.util.platformparameter.PERFORMANCE_METRICS_COLLECTION_LOW_FREQUENCY_TIME_INTERVAL_IN_MINUTES
import org.oppia.android.util.platformparameter.PERFORMANCE_METRICS_COLLECTION_LOW_FREQUENCY_TIME_INTERVAL_IN_MINUTES_DEFAULT_VAL
import org.oppia.android.util.platformparameter.PERFORMANCE_METRICS_COLLECTION_UPLOAD_TIME_INTERVAL_IN_MINUTES
import org.oppia.android.util.platformparameter.PERFORMANCE_METRICS_COLLECTION_UPLOAD_TIME_INTERVAL_IN_MINUTES_DEFAULT_VAL
import org.oppia.android.util.platformparameter.PerformanceMetricsCollectionHighFrequencyTimeIntervalInMinutes
import org.oppia.android.util.platformparameter.PerformanceMetricsCollectionLowFrequencyTimeIntervalInMinutes
import org.oppia.android.util.platformparameter.PerformanceMetricsCollectionUploadTimeIntervalInMinutes
import org.oppia.android.util.platformparameter.PlatformParameterSingleton
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.platformparameter.SPLASH_SCREEN_WELCOME_MSG
import org.oppia.android.util.platformparameter.SPLASH_SCREEN_WELCOME_MSG_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS
import org.oppia.android.util.platformparameter.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.SplashScreenWelcomeMsg
import org.oppia.android.util.platformparameter.SyncUpWorkerTimePeriodHours

/** Dagger module that provides bindings for platform parameters for the alpha app builds. */
@Module
class PlatformParameterAlphaModule {
  @Provides
  @EnableDownloadsSupport
  fun provideEnableDownloadsSupport(): PlatformParameterValue<Boolean> =
    PlatformParameterValue.createDefaultParameter(ENABLE_DOWNLOADS_SUPPORT_DEFAULT_VALUE)

  @Provides
  @SplashScreenWelcomeMsg
  fun provideSplashScreenWelcomeMsgParam(
    platformParameterSingleton: PlatformParameterSingleton
  ): PlatformParameterValue<Boolean> {
    return platformParameterSingleton.getBooleanPlatformParameter(SPLASH_SCREEN_WELCOME_MSG)
      ?: PlatformParameterValue.createDefaultParameter(SPLASH_SCREEN_WELCOME_MSG_DEFAULT_VALUE)
  }

  @Provides
  @SyncUpWorkerTimePeriodHours
  fun provideSyncUpWorkerTimePeriod(
    platformParameterSingleton: PlatformParameterSingleton
  ): PlatformParameterValue<Int> {
    return platformParameterSingleton.getIntegerPlatformParameter(
      SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS
    ) ?: PlatformParameterValue.createDefaultParameter(
      SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS_DEFAULT_VALUE
    )
  }

  @Provides
  @EnableLanguageSelectionUi
  fun provideEnableLanguageSelectionUi(): PlatformParameterValue<Boolean> {
    return PlatformParameterValue.createDefaultParameter(
      ENABLE_LANGUAGE_SELECTION_UI_DEFAULT_VALUE
    )
  }

  @Provides
  @EnableEditAccountsOptionsUi
  fun provideEnableEditAccountsOptionsUi(): PlatformParameterValue<Boolean> {
    return PlatformParameterValue.createDefaultParameter(
      ENABLE_EDIT_ACCOUNTS_OPTIONS_UI_DEFAULT_VALUE
    )
  }

  @Provides
  @EnableLearnerStudyAnalytics
  fun provideLearnerStudyAnalytics(
    platformParameterSingleton: PlatformParameterSingleton
  ): PlatformParameterValue<Boolean> {
    return platformParameterSingleton.getBooleanPlatformParameter(LEARNER_STUDY_ANALYTICS)
      ?: PlatformParameterValue.createDefaultParameter(true)
  }

  @Provides
  @EnableFastLanguageSwitchingInLesson
  fun provideFastInLessonLanguageSwitching(
    platformParameterSingleton: PlatformParameterSingleton
  ): PlatformParameterValue<Boolean> {
    return platformParameterSingleton.getBooleanPlatformParameter(FAST_LANGUAGE_SWITCHING_IN_LESSON)
      ?: PlatformParameterValue.createDefaultParameter(
        FAST_LANGUAGE_SWITCHING_IN_LESSON_DEFAULT_VALUE
      )
  }

  @Provides
  @EnableLoggingLearnerStudyIds
  fun provideLoggingLearnerStudyIds(
    platformParameterSingleton: PlatformParameterSingleton
  ): PlatformParameterValue<Boolean> {
    return platformParameterSingleton.getBooleanPlatformParameter(LOGGING_LEARNER_STUDY_IDS)
      ?: PlatformParameterValue.createDefaultParameter(LOGGING_LEARNER_STUDY_IDS_DEFAULT_VALUE)
  }

  @Provides
  @CacheLatexRendering
  fun provideCacheLatexRendering(
    platformParameterSingleton: PlatformParameterSingleton
  ): PlatformParameterValue<Boolean> {
    return platformParameterSingleton.getBooleanPlatformParameter(CACHE_LATEX_RENDERING)
      ?: PlatformParameterValue.createDefaultParameter(CACHE_LATEX_RENDERING_DEFAULT_VALUE)
  }

  @Provides
  @EnablePerformanceMetricsCollection
  fun provideEnablePerformanceMetricCollection(
    platformParameterSingleton: PlatformParameterSingleton
  ): PlatformParameterValue<Boolean> {
    return platformParameterSingleton.getBooleanPlatformParameter(
      ENABLE_PERFORMANCE_METRICS_COLLECTION
    ) ?: PlatformParameterValue.createDefaultParameter(
      ENABLE_PERFORMANCE_METRICS_COLLECTION_DEFAULT_VALUE
    )
  }

  @Provides
  @PerformanceMetricsCollectionUploadTimeIntervalInMinutes
  fun providePerformanceMetricsCollectionUploadTimeIntervalInMinutes(
    platformParameterSingleton: PlatformParameterSingleton
  ): PlatformParameterValue<Int> {
    return platformParameterSingleton.getIntegerPlatformParameter(
      PERFORMANCE_METRICS_COLLECTION_UPLOAD_TIME_INTERVAL_IN_MINUTES
    ) ?: PlatformParameterValue.createDefaultParameter(
      PERFORMANCE_METRICS_COLLECTION_UPLOAD_TIME_INTERVAL_IN_MINUTES_DEFAULT_VAL
    )
  }

  @Provides
  @PerformanceMetricsCollectionHighFrequencyTimeIntervalInMinutes
  fun providePerformanceMetricsCollectionHighFrequencyTimeIntervalInMinutes(
    platformParameterSingleton: PlatformParameterSingleton
  ): PlatformParameterValue<Int> {
    return platformParameterSingleton.getIntegerPlatformParameter(
      PERFORMANCE_METRICS_COLLECTION_HIGH_FREQUENCY_TIME_INTERVAL_IN_MINUTES
    ) ?: PlatformParameterValue.createDefaultParameter(
      PERFORMANCE_METRICS_COLLECTION_HIGH_FREQUENCY_TIME_INTERVAL_IN_MINUTES_DEFAULT_VAL
    )
  }

  @Provides
  @PerformanceMetricsCollectionLowFrequencyTimeIntervalInMinutes
  fun providePerformanceMetricsCollectionLowFrequencyTimeIntervalInMinutes(
    platformParameterSingleton: PlatformParameterSingleton
  ): PlatformParameterValue<Int> {
    return platformParameterSingleton.getIntegerPlatformParameter(
      PERFORMANCE_METRICS_COLLECTION_LOW_FREQUENCY_TIME_INTERVAL_IN_MINUTES
    ) ?: PlatformParameterValue.createDefaultParameter(
      PERFORMANCE_METRICS_COLLECTION_LOW_FREQUENCY_TIME_INTERVAL_IN_MINUTES_DEFAULT_VAL
    )
  }

  @Provides
  @EnableSpotlightUi
  fun provideEnableSpotlightUi(): PlatformParameterValue<Boolean> =
    PlatformParameterValue.createDefaultParameter(true) // Enable spotlights for alpha users.

  @Provides
  @EnableExtraTopicTabsUi
  fun provideEnableExtraTopicTabsUi(): PlatformParameterValue<Boolean> {
    return PlatformParameterValue.createDefaultParameter(
      ENABLE_EXTRA_TOPIC_TABS_UI_DEFAULT_VALUE
    )
  }

  @Provides
  @EnableInteractionConfigChangeStateRetention
  fun provideEnableInteractionConfigChangeStateRetention(): PlatformParameterValue<Boolean> {
    return PlatformParameterValue.createDefaultParameter(
      ENABLE_INTERACTION_CONFIG_CHANGE_STATE_RETENTION_DEFAULT_VALUE
    )
  }

  @Provides
  @EnableContinueButtonAnimation
  fun provideEnableContinueButtonAnimation(): PlatformParameterValue<Boolean> {
    return PlatformParameterValue.createDefaultParameter(
      ENABLE_CONTINUE_BUTTON_ANIMATION_DEFAULT_VALUE
    )
  }

  @Provides
  @EnableAppAndOsDeprecation
  fun provideEnableAppAndOsDeprecation(): PlatformParameterValue<Boolean> {
    return PlatformParameterValue.createDefaultParameter(
      ENABLE_APP_AND_OS_DEPRECATION_DEFAULT_VALUE
    )
  }

  @Provides
  @OptionalAppUpdateVersionCode
  fun provideOptionalAppUpdateVersionCode(
    platformParameterSingleton: PlatformParameterSingleton,
    context: Context
  ): PlatformParameterValue<Int> {
    return platformParameterSingleton.getIntegerPlatformParameter(
      OPTIONAL_APP_UPDATE_VERSION_CODE
    ) ?: PlatformParameterValue.createDefaultParameter(
      context.getVersionCode()
    )
  }

  @Provides
  @ForcedAppUpdateVersionCode
  fun provideForcedAppUpdateVersionCode(
    platformParameterSingleton: PlatformParameterSingleton,
    context: Context
  ): PlatformParameterValue<Int> {
    return platformParameterSingleton.getIntegerPlatformParameter(
      FORCED_APP_UPDATE_VERSION_CODE
    ) ?: PlatformParameterValue.createDefaultParameter(
      context.getVersionCode()
    )
  }

  @Provides
  @LowestSupportedApiLevel
  fun provideLowestSupportedApiLevel(
    platformParameterSingleton: PlatformParameterSingleton
  ): PlatformParameterValue<Int> {
    return platformParameterSingleton.getIntegerPlatformParameter(
      LOWEST_SUPPORTED_API_LEVEL
    ) ?: PlatformParameterValue.createDefaultParameter(
      LOWEST_SUPPORTED_API_LEVEL_DEFAULT_VALUE
    )
  }

  @Provides
  @NpsSurveyGracePeriodInDays
  fun provideNpsSurveyGracePeriodInDays(
    platformParameterSingleton: PlatformParameterSingleton
  ): PlatformParameterValue<Int> {
    return platformParameterSingleton.getIntegerPlatformParameter(
      NPS_SURVEY_GRACE_PERIOD_IN_DAYS
    ) ?: PlatformParameterValue.createDefaultParameter(
      NPS_SURVEY_GRACE_PERIOD_IN_DAYS_DEFAULT_VALUE
    )
  }

  @Provides
  @NpsSurveyMinimumAggregateLearningTimeInATopicInMinutes
  fun provideNpsSurveyMinimumAggregateLearningTimeInATopicInMinutes(
    platformParameterSingleton: PlatformParameterSingleton
  ): PlatformParameterValue<Int> {
    return platformParameterSingleton.getIntegerPlatformParameter(
      NPS_SURVEY_MINIMUM_AGGREGATE_LEARNING_TIME_IN_A_TOPIC_IN_MINUTES
    ) ?: PlatformParameterValue.createDefaultParameter(
      NPS_SURVEY_MINIMUM_AGGREGATE_LEARNING_TIME_IN_A_TOPIC_IN_MINUTES_DEFAULT_VAL
    )
  }
}
