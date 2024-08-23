package org.oppia.android.util.platformparameter

import javax.inject.Qualifier

/**
 * This file contains all the constants that are associated with individual Feature Flags.
 * These constants are:
 *  - Qualifier Annotation
 *  - Feature Flag Name - The name begins with Enable_
 *  - Feature Flag Default Value
 *  - Feature Flag Status - A boolean that keeps track of whether the feature flag
 *  has been synced with Oppia Web.
 */

/**
 * Qualifier for the feature flag that controls whether the user has support for manually
 * downloading topics.
 */
@Qualifier annotation class EnableDownloadsSupport

/** Name of the feature flag that controls whether to enable downloads support. */
const val DOWNLOADS_SUPPORT = "android_enable_downloads_support"

/** Default value for feature flag corresponding to [EnableDownloadsSupport]. */
const val ENABLE_DOWNLOADS_SUPPORT_DEFAULT_VALUE = false

/**
 * Qualifier for the feature flag corresponding to enabling the extra topic tabs: practice and info.
 */
@Qualifier
annotation class EnableExtraTopicTabsUi

/** Name of the feature flag that controls whether to enable the extra topics tab UI. */
const val EXTRA_TOPIC_TABS_UI = "android_enable_extra_topic_tabs_ui"

/** Default value for the feature flag corresponding to [EnableExtraTopicTabsUi]. */
const val ENABLE_EXTRA_TOPIC_TABS_UI_DEFAULT_VALUE = false

/**
 * Qualifier for the feature flag that controls the visibility of [ProfileAndDeviceIdActivity]
 * and working of learner study related analytics logging.
 */
@Qualifier
annotation class EnableLearnerStudyAnalytics

/**
 * Name of the feature flag that controls the visibility of [ProfileAndDeviceIdActivity]
 * and working of learner study related analytics logging.
 */
const val LEARNER_STUDY_ANALYTICS = "android_enable_learner_study_analytics"

/**
 * Default value of the feature flag that controls the visibility of [ProfileAndDeviceIdActivity]
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

/** The feature flag name corresponding to [EnableFastLanguageSwitchingInLesson]. */
const val FAST_LANGUAGE_SWITCHING_IN_LESSON = "android_enable_fast_language_switching_in_lesson"

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

/** The feature flag name corresponding to [EnableLoggingLearnerStudyIds]. */
const val LOGGING_LEARNER_STUDY_IDS = "android_enable_logging_learner_study_ids"

/** The default enabled state for the feature corresponding to [EnableLoggingLearnerStudyIds]. */
const val LOGGING_LEARNER_STUDY_IDS_DEFAULT_VALUE = false

/** Qualifier for the feature flag corresponding to enabling the edit accounts options. */
@Qualifier
annotation class EnableEditAccountsOptionsUi

/** Name of the feature flag that controls whether to enable the edit account options UI. */
const val EDIT_ACCOUNTS_OPTIONS_UI = "android_enable_edit_accounts_options_ui"

/** Default value for the feature flag corresponding to [EnableEditAccountsOptionsUi]. */
const val ENABLE_EDIT_ACCOUNTS_OPTIONS_UI_DEFAULT_VALUE = false

/** Qualifier for the feature flag that controls whether to record performance metrics. */
@Qualifier
annotation class EnablePerformanceMetricsCollection

/** Name of the feature flag that controls whether to record performance metrics. */
const val ENABLE_PERFORMANCE_METRICS_COLLECTION = "android_enable_performance_metrics_collection"

/** Default value for whether to record performance metrics. */
const val ENABLE_PERFORMANCE_METRICS_COLLECTION_DEFAULT_VALUE = false

/** Qualifier for the feature flag corresponding to enabling the spotlight UI. */
@Qualifier
annotation class EnableSpotlightUi

/** Name of the feature flag that controls whether to enable the spotlight UI. */
const val SPOTLIGHT_UI = "android_enable_spotlight_ui"

/** Default value for the feature flag corresponding to [EnableSpotlightUi]. */
const val ENABLE_SPOTLIGHT_UI_DEFAULT_VALUE = false

/**
 * Qualifier for the feature flag that controls whether input interaction state is correctly
 * retained across configuration changes.
 */
@Qualifier
annotation class EnableInteractionConfigChangeStateRetention

/**
 * Name of the feature flag that controls whether input interaction state is correctly retained
 * across configuration changes.
 */
const val INTERACTION_CONFIG_CHANGE_STATE_RETENTION =
  "android_enable_interaction_config_change_state_retention"

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

/** Name of the feature flag that controls whether to enable app and os deprecation. */
const val APP_AND_OS_DEPRECATION = "android_enable_app_and_os_deprecation"

/**
 * Default value for the feature flag corresponding to [EnableAppAndOsDeprecation].
 */
const val ENABLE_APP_AND_OS_DEPRECATION_DEFAULT_VALUE = false

/** Qualifier for the feature flag that toggles the the NPS Survey. */
@Qualifier
annotation class EnableNpsSurvey

/** Name of the feature flag that toggles the NPS Survey. */
const val ENABLE_NPS_SURVEY = "android_enable_nps_survey"

/** Default value of the feature flag corresponding to [EnableNpsSurvey]. */
const val ENABLE_NPS_SURVEY_DEFAULT_VALUE = true

/** Qualifier for the feature flag that toggles the new onboarding flow. */
@Qualifier
annotation class EnableOnboardingFlowV2

/** Name of the feature flag that toggles the new onboarding flow. */
const val ENABLE_ONBOARDING_FLOW_V2 = "android_enable_onboarding_flow_v2"

/** Default value of the feature flag corresponding to [EnableOnboardingFlowV2]. */
const val ENABLE_ONBOARDING_FLOW_V2_DEFAULT_VALUE = false

/** Qualifier for the feature flag that toggles the new multiple classrooms. */
@Qualifier
annotation class EnableMultipleClassrooms

/** Name of the feature flag that toggles the new multiple classrooms. */
const val ENABLE_MULTIPLE_CLASSROOMS = "enable_multiple_classrooms"

/** Default value of the feature flag corresponding to [EnableMultipleClassrooms]. */
const val ENABLE_MULTIPLE_CLASSROOMS_DEFAULT_VALUE = false
