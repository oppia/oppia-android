package org.oppia.android.testing.platformparameter

import android.content.Context
import androidx.annotation.VisibleForTesting
import dagger.Module
import dagger.Provides
import org.oppia.android.app.model.PlatformParameter
import org.oppia.android.util.extensions.getVersionCode
import org.oppia.android.util.platformparameter.APP_AND_OS_DEPRECATION
import org.oppia.android.util.platformparameter.CACHE_LATEX_RENDERING
import org.oppia.android.util.platformparameter.CACHE_LATEX_RENDERING_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.CacheLatexRendering
import org.oppia.android.util.platformparameter.DOWNLOADS_SUPPORT
import org.oppia.android.util.platformparameter.ENABLE_APP_AND_OS_DEPRECATION_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.ENABLE_DOWNLOADS_SUPPORT_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.ENABLE_EDIT_ACCOUNTS_OPTIONS_UI_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.ENABLE_EXTRA_TOPIC_TABS_UI_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.ENABLE_INTERACTION_CONFIG_CHANGE_STATE_RETENTION_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.ENABLE_MULTIPLE_CLASSROOMS_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.ENABLE_NPS_SURVEY_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.ENABLE_ONBOARDING_FLOW_V2_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.ENABLE_PERFORMANCE_METRICS_COLLECTION_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.EnableAppAndOsDeprecation
import org.oppia.android.util.platformparameter.EnableDownloadsSupport
import org.oppia.android.util.platformparameter.EnableEditAccountsOptionsUi
import org.oppia.android.util.platformparameter.EnableExtraTopicTabsUi
import org.oppia.android.util.platformparameter.EnableFastLanguageSwitchingInLesson
import org.oppia.android.util.platformparameter.EnableInteractionConfigChangeStateRetention
import org.oppia.android.util.platformparameter.EnableLearnerStudyAnalytics
import org.oppia.android.util.platformparameter.EnableLoggingLearnerStudyIds
import org.oppia.android.util.platformparameter.EnableMultipleClassrooms
import org.oppia.android.util.platformparameter.EnableNpsSurvey
import org.oppia.android.util.platformparameter.EnableOnboardingFlowV2
import org.oppia.android.util.platformparameter.EnablePerformanceMetricsCollection
import org.oppia.android.util.platformparameter.EnableSpotlightUi
import org.oppia.android.util.platformparameter.FAST_LANGUAGE_SWITCHING_IN_LESSON_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.FORCED_APP_UPDATE_VERSION_CODE
import org.oppia.android.util.platformparameter.ForcedAppUpdateVersionCode
import org.oppia.android.util.platformparameter.LEARNER_STUDY_ANALYTICS_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.LOGGING_LEARNER_STUDY_IDS_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.LOWEST_SUPPORTED_API_LEVEL
import org.oppia.android.util.platformparameter.LOWEST_SUPPORTED_API_LEVEL_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.LowestSupportedApiLevel
import org.oppia.android.util.platformparameter.NPS_SURVEY_GRACE_PERIOD_IN_DAYS_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.NPS_SURVEY_MINIMUM_AGGREGATE_LEARNING_TIME_IN_A_TOPIC_IN_MINUTES_DEFAULT_VALUE
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
import javax.inject.Singleton

/* Fake Platform Parameter Module that provides individual Platform Parameters for testing. */
@Module
class TestPlatformParameterModule {
  @Provides
  @EnableTestFeatureFlag
  fun provideEnableTestFeatureFlag(
    platformParameterSingleton: PlatformParameterSingleton
  ): PlatformParameterValue<Boolean> {
    return platformParameterSingleton.getBooleanPlatformParameter(TEST_FEATURE_FLAG)
      ?: PlatformParameterValue.createDefaultParameter(TEST_FEATURE_FLAG_DEFAULT_VALUE)
  }

  @Provides
  @EnableTestFeatureFlagWithEnabledDefault
  fun provideEnableTestFeatureFlagWithEnabledDefault(
    platformParameterSingleton: PlatformParameterSingleton
  ): PlatformParameterValue<Boolean> {
    return platformParameterSingleton.getBooleanPlatformParameter(
      TEST_FEATURE_FLAG_WITH_ENABLED_DEFAULTS
    )
      ?: PlatformParameterValue.createDefaultParameter(
        defaultValue = TEST_FEATURE_FLAG_WITH_ENABLED_DEFAULT_VALUE,
        defaultSyncStatus = PlatformParameter.SyncStatus.SYNCED_FROM_SERVER
      )
  }

  @Provides
  @EnableDownloadsSupport
  fun provideEnableDownloadsSupport(
    platformParameterSingleton: PlatformParameterSingleton
  ): PlatformParameterValue<Boolean> {
    return platformParameterSingleton.getBooleanPlatformParameter(DOWNLOADS_SUPPORT)
      ?: PlatformParameterValue.createDefaultParameter(enableDownloadsSupport)
  }

  @TestStringParam
  @Provides
  @Singleton
  fun provideTestStringParam(
    platformParameterSingleton: PlatformParameterSingleton
  ): PlatformParameterValue<String> {
    return platformParameterSingleton.getStringPlatformParameter(TEST_STRING_PARAM_NAME)
      ?: PlatformParameterValue.createDefaultParameter(TEST_STRING_PARAM_DEFAULT_VALUE)
  }

  @TestIntegerParam
  @Provides
  @Singleton
  fun provideTestIntegerParam(
    platformParameterSingleton: PlatformParameterSingleton
  ): PlatformParameterValue<Int> {
    return platformParameterSingleton.getIntegerPlatformParameter(TEST_INTEGER_PARAM_NAME)
      ?: PlatformParameterValue.createDefaultParameter(TEST_INTEGER_PARAM_DEFAULT_VALUE)
  }

  @TestBooleanParam
  @Provides
  @Singleton
  fun provideTestBooleanParam(
    platformParameterSingleton: PlatformParameterSingleton
  ): PlatformParameterValue<Boolean> {
    return platformParameterSingleton.getBooleanPlatformParameter(TEST_BOOLEAN_PARAM_NAME)
      ?: PlatformParameterValue.createDefaultParameter(TEST_BOOLEAN_PARAM_DEFAULT_VALUE)
  }

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
  @EnableEditAccountsOptionsUi
  fun provideEnableEditAccountsOptionsUi(): PlatformParameterValue<Boolean> =
    PlatformParameterValue.createDefaultParameter(enableEditAccountsOptionsUi)

  @Provides
  @EnableLearnerStudyAnalytics
  fun provideLearnerStudyAnalytics(): PlatformParameterValue<Boolean> =
    PlatformParameterValue.createDefaultParameter(enableLearnerStudyAnalytics)

  @Provides
  @EnableFastLanguageSwitchingInLesson
  fun provideFastInLessonLanguageSwitching(): PlatformParameterValue<Boolean> =
    PlatformParameterValue.createDefaultParameter(enableFastLanguageSwitchingInLesson)

  @Provides
  @EnableLoggingLearnerStudyIds
  fun provideLoggingLearnerStudyIds(): PlatformParameterValue<Boolean> =
    PlatformParameterValue.createDefaultParameter(enableLoggingLearnerStudyIds)

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
  fun provideEnablePerformanceMetricCollection(): PlatformParameterValue<Boolean> {
    return PlatformParameterValue.createDefaultParameter(enablePerformanceMetricsCollection)
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
  @EnableExtraTopicTabsUi
  fun provideEnableExtraTopicTabsUi(): PlatformParameterValue<Boolean> =
    PlatformParameterValue.createDefaultParameter(enableExtraTopicTabsUi)

  @Provides
  @EnableInteractionConfigChangeStateRetention
  fun provideEnableInteractionConfigChangeStateRetention(): PlatformParameterValue<Boolean> =
    PlatformParameterValue.createDefaultParameter(enableInteractionConfigChangeStateRetention)

  @Provides
  @EnableSpotlightUi
  fun provideEnableSpotlightUi(): PlatformParameterValue<Boolean> {
    return PlatformParameterValue.createDefaultParameter(
      enableSpotlightUi
    )
  }

  @Provides
  @EnableAppAndOsDeprecation
  fun provideEnableAppAndOsDeprecation(
    platformParameterSingleton: PlatformParameterSingleton
  ): PlatformParameterValue<Boolean> {
    return platformParameterSingleton.getBooleanPlatformParameter(APP_AND_OS_DEPRECATION)
      ?: PlatformParameterValue.createDefaultParameter(ENABLE_APP_AND_OS_DEPRECATION_DEFAULT_VALUE)
  }

  @Provides
  @Singleton
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
  fun provideNpsSurveyGracePeriodInDays(): PlatformParameterValue<Int> {
    return PlatformParameterValue.createDefaultParameter(gracePeriodInDays)
  }

  @Provides
  @NpsSurveyMinimumAggregateLearningTimeInATopicInMinutes
  fun provideNpsSurveyMinimumAggregateLearningTimeInATopicInMinutes():
    PlatformParameterValue<Int> {
      return PlatformParameterValue.createDefaultParameter(minimumLearningTime)
    }

  @Provides
  @EnableNpsSurvey
  fun provideEnableNpsSurvey(): PlatformParameterValue<Boolean> {
    return PlatformParameterValue.createDefaultParameter(enableNpsSurvey)
  }

  @Provides
  @EnableOnboardingFlowV2
  fun provideEnableOnboardingFlowV2(): PlatformParameterValue<Boolean> {
    return PlatformParameterValue.createDefaultParameter(enableOnboardingFlowV2)
  }

  @Provides
  @EnableMultipleClassrooms
  fun provideEnableMultipleClassrooms(): PlatformParameterValue<Boolean> {
    return PlatformParameterValue.createDefaultParameter(enableMultipleClassrooms)
  }

  companion object {
    private var enableDownloadsSupport = ENABLE_DOWNLOADS_SUPPORT_DEFAULT_VALUE
    private var enableEditAccountsOptionsUi = ENABLE_EDIT_ACCOUNTS_OPTIONS_UI_DEFAULT_VALUE
    private var enableLearnerStudyAnalytics = LEARNER_STUDY_ANALYTICS_DEFAULT_VALUE
    private var enableFastLanguageSwitchingInLesson =
      FAST_LANGUAGE_SWITCHING_IN_LESSON_DEFAULT_VALUE
    private var enableLoggingLearnerStudyIds = LOGGING_LEARNER_STUDY_IDS_DEFAULT_VALUE
    private var enableExtraTopicTabsUi = ENABLE_EXTRA_TOPIC_TABS_UI_DEFAULT_VALUE
    private var enableInteractionConfigChangeStateRetention =
      ENABLE_INTERACTION_CONFIG_CHANGE_STATE_RETENTION_DEFAULT_VALUE
    private var enablePerformanceMetricsCollection =
      ENABLE_PERFORMANCE_METRICS_COLLECTION_DEFAULT_VALUE
    private var enableSpotlightUi = true
    private var enableAppAndOsDeprecation = ENABLE_APP_AND_OS_DEPRECATION_DEFAULT_VALUE
    private var minimumLearningTime =
      NPS_SURVEY_MINIMUM_AGGREGATE_LEARNING_TIME_IN_A_TOPIC_IN_MINUTES_DEFAULT_VALUE
    private var gracePeriodInDays = NPS_SURVEY_GRACE_PERIOD_IN_DAYS_DEFAULT_VALUE
    private var enableNpsSurvey = ENABLE_NPS_SURVEY_DEFAULT_VALUE
    private var enableOnboardingFlowV2 = ENABLE_ONBOARDING_FLOW_V2_DEFAULT_VALUE
    private var enableMultipleClassrooms = ENABLE_MULTIPLE_CLASSROOMS_DEFAULT_VALUE

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun forceEnableDownloadsSupport(value: Boolean) {
      enableDownloadsSupport = value
    }

    /** Enables forcing [EnableEditAccountsOptionsUI] platform parameter flag from tests. */
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun forceEnableEditAccountsOptionsUi(value: Boolean) {
      enableEditAccountsOptionsUi = value
    }

    /** Enables forcing [EnableLearnerStudyAnalytics] platform parameter flag from tests. */
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun forceEnableLearnerStudyAnalytics(value: Boolean) {
      enableLearnerStudyAnalytics = value
    }

    /** Enables forcing [EnableFastLanguageSwitchingInLesson] platform parameter flag from tests. */
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun forceEnableFastLanguageSwitchingInLesson(value: Boolean) {
      enableFastLanguageSwitchingInLesson = value
    }

    /** Enables forcing [EnableLoggingLearnerStudyIds] platform parameter flag from tests. */
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun forceEnableLoggingLearnerStudyIds(value: Boolean) {
      enableLoggingLearnerStudyIds = value
    }

    /** Enables forcing [EnableExtraTopicTabsUi] platform parameter flag from tests. */
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun forceEnableExtraTopicTabsUi(value: Boolean) {
      enableExtraTopicTabsUi = value
    }

    /** Enables forcing [EnableInteractionConfigChangeStateRetention] platform parameter flag from tests. */
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun forceEnableInteractionConfigChangeStateRetention(value: Boolean) {
      enableInteractionConfigChangeStateRetention = value
    }

    /** Enables forcing [EnablePerformanceMetricsCollection] platform parameter flag from tests. */
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun forceEnablePerformanceMetricsCollection(value: Boolean) {
      enablePerformanceMetricsCollection = value
    }

    /** Enables forcing [EnableSpotlightUi] platform parameter flag from tests. */
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun forceEnableSpotlightUi(value: Boolean) {
      enableSpotlightUi = value
    }

    /** Enables forcing [EnableNpsSurvey] feature flag from tests. */
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun forceEnableNpsSurvey(value: Boolean) {
      enableNpsSurvey = value
    }

    /** Enables forcing [EnableOnboardingFlowV2] platform parameter flag from tests. */
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun forceEnableOnboardingFlowV2(value: Boolean) {
      enableOnboardingFlowV2 = value
    }

    /** Enables forcing [EnableMultipleClassrooms] platform parameter flag from tests. */
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun forceEnableMultipleClassrooms(value: Boolean) {
      enableMultipleClassrooms = value
    }

    /** Enables forcing [EnableAppAndOsDeprecation] feature flag from tests. */
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun forceEnableAppAndOsDeprecation(value: Boolean) {
      enableAppAndOsDeprecation = value
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun reset() {
      enableDownloadsSupport = ENABLE_DOWNLOADS_SUPPORT_DEFAULT_VALUE
      enableEditAccountsOptionsUi = ENABLE_EDIT_ACCOUNTS_OPTIONS_UI_DEFAULT_VALUE
      enableLearnerStudyAnalytics = LEARNER_STUDY_ANALYTICS_DEFAULT_VALUE
      enableFastLanguageSwitchingInLesson = FAST_LANGUAGE_SWITCHING_IN_LESSON_DEFAULT_VALUE
      enableLoggingLearnerStudyIds = LOGGING_LEARNER_STUDY_IDS_DEFAULT_VALUE
      enableExtraTopicTabsUi = ENABLE_EXTRA_TOPIC_TABS_UI_DEFAULT_VALUE
      enableInteractionConfigChangeStateRetention =
        ENABLE_INTERACTION_CONFIG_CHANGE_STATE_RETENTION_DEFAULT_VALUE
      enablePerformanceMetricsCollection = ENABLE_PERFORMANCE_METRICS_COLLECTION_DEFAULT_VALUE
      enableAppAndOsDeprecation = ENABLE_APP_AND_OS_DEPRECATION_DEFAULT_VALUE
      enableOnboardingFlowV2 = ENABLE_ONBOARDING_FLOW_V2_DEFAULT_VALUE
      enableMultipleClassrooms = ENABLE_MULTIPLE_CLASSROOMS_DEFAULT_VALUE
    }
  }
}
