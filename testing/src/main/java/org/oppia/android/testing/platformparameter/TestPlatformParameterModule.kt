package org.oppia.android.testing.platformparameter

import androidx.annotation.VisibleForTesting
import dagger.Module
import dagger.Provides
import org.oppia.android.util.platformparameter.CACHE_LATEX_RENDERING
import org.oppia.android.util.platformparameter.CACHE_LATEX_RENDERING_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.CacheLatexRendering
import org.oppia.android.util.platformparameter.ENABLE_CONTINUE_BUTTON_ANIMATION_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.ENABLE_EDIT_ACCOUNTS_OPTIONS_UI_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.ENABLE_LANGUAGE_SELECTION_UI_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.ENABLE_PERFORMANCE_METRICS_COLLECTION
import org.oppia.android.util.platformparameter.ENABLE_PERFORMANCE_METRICS_COLLECTION_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.EnableContinueButtonAnimation
import org.oppia.android.util.platformparameter.EnableEditAccountsOptionsUi
import org.oppia.android.util.platformparameter.EnableLanguageSelectionUi
import org.oppia.android.util.platformparameter.EnablePerformanceMetricsCollection
import org.oppia.android.util.platformparameter.LEARNER_STUDY_ANALYTICS_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.LearnerStudyAnalytics
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
  @EnableLanguageSelectionUi
  fun provideEnableLanguageSelectionUi(): PlatformParameterValue<Boolean> {
    return PlatformParameterValue.createDefaultParameter(
      enableLanguageSelectionUi
    )
  }

  @Provides
  @EnableEditAccountsOptionsUi
  fun provideEnableEditAccountsOptionsUi(): PlatformParameterValue<Boolean> {
    return PlatformParameterValue.createDefaultParameter(
      enableEditAccountsOptionsUi
    )
  }

  @Provides
  @LearnerStudyAnalytics
  fun provideLearnerStudyAnalytics(): PlatformParameterValue<Boolean> {
    return PlatformParameterValue.createDefaultParameter(enableLearnerStudyAnalytics)
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
  @EnableContinueButtonAnimation
  fun provideEnableContinueButtonAnimation(): PlatformParameterValue<Boolean> {
    return PlatformParameterValue.createDefaultParameter(
      enableContinueButtonAnimation
    )
  }

  companion object {
    private var enableLanguageSelectionUi = ENABLE_LANGUAGE_SELECTION_UI_DEFAULT_VALUE
    private var enableEditAccountsOptionsUi = ENABLE_EDIT_ACCOUNTS_OPTIONS_UI_DEFAULT_VALUE
    private var enableLearnerStudyAnalytics = LEARNER_STUDY_ANALYTICS_DEFAULT_VALUE
    private var enableContinueButtonAnimation = ENABLE_CONTINUE_BUTTON_ANIMATION_DEFAULT_VALUE

    /** Enables forcing [EnableLanguageSelectionUi] platform parameter flag from tests. */
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun forceEnableLanguageSelectionUi(value: Boolean) {
      enableLanguageSelectionUi = value
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

    /** Enables forcing [EnableContinueButtonAnimation] platform parameter flag from tests. */
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun forceEnableContinueButtonAnimation(value: Boolean) {
      enableContinueButtonAnimation = value
    }
  }
}
