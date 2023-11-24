package org.oppia.android.util.platformparameter

import javax.inject.Qualifier

/**
 * This file contains all the constants that are associated with individual Platform Parameters.
 * These constants are:
 *  - Qualifier Annotation
 *  - Platform Parameter Name
 *  - Platform Parameter Default Value
 */

/**
 * Qualifier for the platform parameter that controls whether the user has support for manually
 * downloading topics.
 */
@Qualifier annotation class EnableDownloadsSupport

/** Default value for feature flag corresponding to [EnableDownloadsSupport]. */
const val ENABLE_DOWNLOADS_SUPPORT_DEFAULT_VALUE = false

/**
 * Name of the platform parameter that automatically updates topics when a user toggles the
 * switch in the [AdministratorControlsFragmentPresenter].
 */
const val AUTOMATIC_UPDATE_TOPIC_SETTING = "automatically_update_topic"

/**
 * Default value of the platform parameter that automatically updates topics when a user toggles the
 * switch in the [AdministratorControlsFragmentPresenter].
 */
const val AUTOMATIC_UPDATE_TOPIC_SETTING_VALUE = false

/**
 * Qualifier for the platform parameter that controls the visibility of splash screen welcome
 * message toast in the [SplashTestActivity].
 */
@Qualifier
annotation class SplashScreenWelcomeMsg

/**
 * Name of the platform parameter that controls the visibility of splash screen welcome message
 * toast in the [SplashTestActivity].
 */
const val SPLASH_SCREEN_WELCOME_MSG = "splash_screen_welcome_msg"

/**
 * Default value of the platform parameter that controls the visibility of splash screen welcome
 * message toast in the [SplashTestActivity].
 */
const val SPLASH_SCREEN_WELCOME_MSG_DEFAULT_VALUE = false

/**
 * Server value of the platform parameter that controls the visibility of splash screen welcome
 * message toast in the [SplashTestActivity].
 */
const val SPLASH_SCREEN_WELCOME_MSG_SERVER_VALUE = true

/**
 * Qualifier for the platform parameter that defines the time period in hours, after which the
 * [PlatformParameterSyncUpWorker] will run again.
 */
@Qualifier
annotation class SyncUpWorkerTimePeriodHours

/**
 * Name of the platform parameter that defines the time period in hours, after which the
 * [PlatformParameterSyncUpWorker] will run again.
 */
const val SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS = "sync_up_worker_time_period"

/**
 * Default value of the platform parameter that defines the time period in hours, after which the
 * [PlatformParameterSyncUpWorker] will run again.
 */
const val SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS_DEFAULT_VALUE = 12

/** Qualifier for the feature flag corresponding to enabling the language selection UI. */
@Qualifier
annotation class EnableLanguageSelectionUi

/** Default value for the feature flag corresponding to [EnableLanguageSelectionUi]. */
const val ENABLE_LANGUAGE_SELECTION_UI_DEFAULT_VALUE = true

/**
 * Qualifier for the feature flag corresponding to enabling the extra topic tabs: practice and info.
 */
@Qualifier
annotation class EnableExtraTopicTabsUi

/** Default value for the feature flag corresponding to [EnableExtraTopicTabsUi]. */
const val ENABLE_EXTRA_TOPIC_TABS_UI_DEFAULT_VALUE = false

/**
 * Qualifier for the platform parameter that controls the visibility of [ProfileAndDeviceIdActivity]
 * and working of learner study related analytics logging.
 */
@Qualifier
annotation class EnableLearnerStudyAnalytics

/**
 * Name of the platform parameter that controls the visibility of [ProfileAndDeviceIdActivity]
 * and working of learner study related analytics logging.
 */
const val LEARNER_STUDY_ANALYTICS = "learner_study_analytics"

/**
 * Default value of the platform parameter that controls the visibility of [ProfileAndDeviceIdActivity]
 * and working of learner study related analytics logging.
 */
const val LEARNER_STUDY_ANALYTICS_DEFAULT_VALUE = false

/**
 * Qualifier for a feature flag that controls whether learners may be allowed (via an
 * admin-controlled setting) to use a special in-lesson button for quickly switching between content
 * languages.
 *
 * This is generally expected to only be used in tandem with [EnableLearnerStudyAnalytics].
 */
@Qualifier annotation class EnableFastLanguageSwitchingInLesson

/** The platform parameter name corresponding to [EnableFastLanguageSwitchingInLesson]. */
const val FAST_LANGUAGE_SWITCHING_IN_LESSON = "fast_language_switching_in_lesson"

/**
 * The default enabled state for the feature corresponding to [EnableFastLanguageSwitchingInLesson].
 */
const val FAST_LANGUAGE_SWITCHING_IN_LESSON_DEFAULT_VALUE = false

/**
 * Qualifier for a feature flag that controls whether learner study IDs should be generated and
 * logged with outgoing events.
 *
 * This is generally expected to only be used in tandem with [EnableLearnerStudyAnalytics].
 */
@Qualifier annotation class EnableLoggingLearnerStudyIds

/** The platform parameter name corresponding to [EnableLoggingLearnerStudyIds]. */
const val LOGGING_LEARNER_STUDY_IDS = "logging_learner_study_ids"

/** The default enabled state for the feature corresponding to [EnableLoggingLearnerStudyIds]. */
const val LOGGING_LEARNER_STUDY_IDS_DEFAULT_VALUE = false

/**
 * Qualifier for the platform parameter that controls whether to cache LaTeX rendering using Glide.
 */
@Qualifier
annotation class CacheLatexRendering

/** Name of the platform that controls whether to cache LaTeX rendering using Glide. */
const val CACHE_LATEX_RENDERING = "cache_latex_rendering"

/** Default value for whether to cache LaTeX rendering using Glide. */
const val CACHE_LATEX_RENDERING_DEFAULT_VALUE = true

/** Qualifier for the feature flag corresponding to enabling the edit accounts options. */
@Qualifier
annotation class EnableEditAccountsOptionsUi

/** Default value for the feature flag corresponding to [EnableEditAccountsOptionsUi]. */
const val ENABLE_EDIT_ACCOUNTS_OPTIONS_UI_DEFAULT_VALUE = false

/** Qualifier for the platform parameter that controls whether to record performance metrics. */
@Qualifier
annotation class EnablePerformanceMetricsCollection

/** Name of the platform parameter that controls whether to record performance metrics. */
const val ENABLE_PERFORMANCE_METRICS_COLLECTION = "enable_performance_metrics_collection"

/** Default value for whether to record performance metrics. */
const val ENABLE_PERFORMANCE_METRICS_COLLECTION_DEFAULT_VALUE = false

/**
 * Qualifier for the platform parameter that controls the time interval in minutes of uploading
 * previously recorded performance metrics to the remote service.
 */
@Qualifier
annotation class PerformanceMetricsCollectionUploadTimeIntervalInMinutes

/**
 * Name of the platform parameter that controls the time interval in minutes of uploading previously
 * recorded performance metrics to the remote service.
 */
const val PERFORMANCE_METRICS_COLLECTION_UPLOAD_TIME_INTERVAL_IN_MINUTES =
  "performance_metrics_collection_upload_time_interval_in_minutes"

/**
 * Default value of the time interval in minutes of uploading previously recorded performance
 * metrics to the remote service.
 */
const val PERFORMANCE_METRICS_COLLECTION_UPLOAD_TIME_INTERVAL_IN_MINUTES_DEFAULT_VAL = 15

/**
 * Qualifier for the platform parameter that controls the time interval in minutes of recording
 * performance metrics that are to be recorded more frequently.
 */
@Qualifier
annotation class PerformanceMetricsCollectionHighFrequencyTimeIntervalInMinutes

/**
 * Name of the platform parameter that controls the time interval in minutes of recording
 * performance metrics that are to be recorded more frequently.
 */
const val PERFORMANCE_METRICS_COLLECTION_HIGH_FREQUENCY_TIME_INTERVAL_IN_MINUTES =
  "performance_metrics_collection_high_frequency_time_interval_in_minutes"

/**
 * Default value of the time interval in minutes of recording performance metrics that are to be
 * recorded more frequently.
 */
const val PERFORMANCE_METRICS_COLLECTION_HIGH_FREQUENCY_TIME_INTERVAL_IN_MINUTES_DEFAULT_VAL =
  15

/**
 * Qualifier for the platform parameter that controls the time interval in minutes of recording
 * performance metrics that are to be recorded less frequently.
 */
@Qualifier
annotation class PerformanceMetricsCollectionLowFrequencyTimeIntervalInMinutes

/**
 * Name of the platform parameter that controls the time interval in minutes of recording
 * performance metrics that are to be recorded less frequently.
 */
const val PERFORMANCE_METRICS_COLLECTION_LOW_FREQUENCY_TIME_INTERVAL_IN_MINUTES =
  "performance_metrics_collection_low_frequency_time_interval_in_minutes"

/**
 * Default value of the time interval in minutes of recording performance metrics that are to be
 * recorded less frequently.
 */
const val PERFORMANCE_METRICS_COLLECTION_LOW_FREQUENCY_TIME_INTERVAL_IN_MINUTES_DEFAULT_VAL =
  1440

/** Qualifier for the feature flag corresponding to enabling the spotlight UI. */
@Qualifier
annotation class EnableSpotlightUi

/** Default value for the feature flag corresponding to [EnableSpotlightUi]. */
const val ENABLE_SPOTLIGHT_UI_DEFAULT_VALUE = false

/**
 * Qualifier for the platform parameter that controls whether input interaction state is correctly
 * retained across configuration changes.
 */
@Qualifier
annotation class EnableInteractionConfigChangeStateRetention

/**
 * Default value for feature flag corresponding to [EnableInteractionConfigChangeStateRetention].
 */
const val ENABLE_INTERACTION_CONFIG_CHANGE_STATE_RETENTION_DEFAULT_VALUE = false

/**
 * Qualifier for the [EnableAppAndOsDeprecation] feature flag that controls whether to enable
 * app and OS deprecation or not.
 */
@Qualifier
annotation class EnableAppAndOsDeprecation

/**
 * Default value for the feature flag corresponding to [EnableAppAndOsDeprecation].
 */
const val ENABLE_APP_AND_OS_DEPRECATION_DEFAULT_VALUE = false

/**
 * Qualifier for the platform parameter that contains the version code of the latest available
 * optional app update, which is used to notify the app that a soft update is available.
 */
@Qualifier
annotation class OptionalAppUpdateVersionCode

/**
 * Name of the platform parameter that contains the integer version code of the latest available
 * optional app update.
 */
const val OPTIONAL_APP_UPDATE_VERSION_CODE = "optional_app_update_version_code"

/**
 * Qualifier for the platform parameter that contains the version code of the latest available
 * forced app update, which is used to notify the app that a mandatory update is available.
 */
@Qualifier
annotation class ForcedAppUpdateVersionCode

/**
 * Name of the platform parameter that contains the integer version code of the latest available
 * forced app update.
 */
const val FORCED_APP_UPDATE_VERSION_CODE = "forced_app_update_version_code"

/**
 * Qualifier for the platform parameter that contains an integer indicating the lowest supported
 * Android API Level.
 */
@Qualifier
annotation class LowestSupportedApiLevel

/**
 * Name of the platform parameter that contains an integer indicating the lowest supported Android
 * API Level.
 */
const val LOWEST_SUPPORTED_API_LEVEL = "lowest_supported_api_level"

/**
 * Default value for the platform parameter that contains an integer indicating the lowest
 * supported Android API Level.
 *
 * The current minimum supported API level is 19 (KitKat).
 */
const val LOWEST_SUPPORTED_API_LEVEL_DEFAULT_VALUE = 19

/**
 * Qualifier for the platform parameter that controls the time interval in days between showing
 * subsequent NPS surveys.
 */
@Qualifier
annotation class NpsSurveyGracePeriodInDays

/**
 * Name of the platform parameter that controls the time interval in days between showing subsequent
 * NPS surveys.
 */
const val NPS_SURVEY_GRACE_PERIOD_IN_DAYS = "nps_survey_grace_period_in_days"

/** Default value of the time interval in days between showing subsequent NPS surveys. */
const val NPS_SURVEY_GRACE_PERIOD_IN_DAYS_DEFAULT_VALUE = 30

/**
 * Qualifier for the platform parameter that controls the minimum learning time in a topic, in
 * minutes, that indicates a learner has had sufficient interaction with the app to be able to
 * provide informed feedback about their experience with the app.
 */
@Qualifier
annotation class NpsSurveyMinimumAggregateLearningTimeInATopicInMinutes

/**
 * Name of the platform parameter that controls the minimum learning time in a topic, in
 * minutes, that indicates a learner has had sufficient interaction with the app to be able to
 * provide informed feedback about their experience with the app.
 */
const val NPS_SURVEY_MINIMUM_AGGREGATE_LEARNING_TIME_IN_A_TOPIC_IN_MINUTES =
  "nps_survey_minimum_aggregate_learning_time_in_a_topic_in_minutes"

/**
 * Default value of the minimum learning time in a topic, in minutes, that indicates a learner has
 * had sufficient interaction with the app to be able to provide informed feedback about their
 * experience with the app.
 */
const val NPS_SURVEY_MINIMUM_AGGREGATE_LEARNING_TIME_IN_A_TOPIC_IN_MINUTES_DEFAULT_VALUE = 5
