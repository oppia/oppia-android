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
/**
 * Qualifier for the platform parameter that controls whether to cache LaTeX rendering using Glide.
 */
@Qualifier
annotation class CacheLatexRendering

/** Name of the platform that controls whether to cache LaTeX rendering using Glide. */
const val CACHE_LATEX_RENDERING = "cache_latex_rendering"

/** Default value for whether to cache LaTeX rendering using Glide. */
const val CACHE_LATEX_RENDERING_DEFAULT_VALUE = true

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
