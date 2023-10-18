package org.oppia.android.util.platformparameter

import javax.inject.Qualifier

/**
 * This file contains all the constants that are associated with individual Feature Flags.
 * These constants are:
 *  - Qualifier Annotation
 *  - Feature Flag Name - The name begins with Enable_
 *  - Feature Flag Default Value
 *  - Feature Flag Status - A boolean that keeps track of whether the feature flag
 *  has been synced with Oppia Web or not.
 */

/**
 * Qualifier for the feature flag that controls whether the user has support for manually
 * downloading topics.
 */
@Qualifier annotation class EnableDownloadsSupport

/** Default value for feature flag corresponding to [EnableDownloadsSupport]. */
const val ENABLE_DOWNLOADS_SUPPORT_DEFAULT_VALUE = false

/**
 * Qualifier to keep track of the sync status of the [EnableDownloadsSupport] and help determine
 * if the value of the feature flag is server-provided or default.
 */
@Qualifier
annotation class FlagEnableDownloadsSupportIsServerProvided

/**
 * Default value for the feature flag sync status tracker corresponding to
 * [FlagEnableDownloadsSupportIsServerProvided].
 */
const val FLAG_ENABLE_DOWNLOADS_SUPPORT_IS_SERVER_PROVIDED_DEFAULT_VALUE = false

/** Qualifier for the feature flag corresponding to enabling the language selection UI. */
@Qualifier
annotation class EnableLanguageSelectionUi

/** Default value for the feature flag corresponding to [EnableLanguageSelectionUi]. */
const val ENABLE_LANGUAGE_SELECTION_UI_DEFAULT_VALUE = true

/**
 * Qualifier to keep track of the sync status of the [EnableLanguageSelectionUi] and help determine
 * if the value of the feature flag is server-provided or default.
 */
@Qualifier
annotation class FlagEnableLanguageSelectionUiIsServerProvided

/**
 * Default value for the feature flag sync status tracker corresponding to
 * [FlagEnableLanguageSelectionUiIsServerProvided].
 */
const val FLAG_ENABLE_LANGUAGE_SELECTION_UI_IS_SERVER_PROVIDED_DEFAULT_VALUE = false

/**
 * Qualifier for the feature flag corresponding to enabling the extra topic tabs: practice and info.
 */
@Qualifier
annotation class EnableExtraTopicTabsUi

/** Default value for the feature flag corresponding to [EnableExtraTopicTabsUi]. */
const val ENABLE_EXTRA_TOPIC_TABS_UI_DEFAULT_VALUE = false

/**
 * Qualifier to keep track of the sync status of the [EnableExtraTopicTabsUi] and help determine
 * if the value of the feature flag is server-provided or default.
 */
@Qualifier
annotation class FlagEnableExtraTopicTabsUiIsServerProvided

/**
 * Default value for the feature flag sync status tracker corresponding to
 * [FlagEnableExtraTopicTabsUiIsServerProvided].
 */
const val FLAG_ENABLE_EXTRA_TOPIC_TABS_UI_IS_SERVER_PROVIDED_DEFAULT_VALUE = false

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
const val LEARNER_STUDY_ANALYTICS = "learner_study_analytics"

/**
 * Default value of the feature flag that controls the visibility of [ProfileAndDeviceIdActivity]
 * and working of learner study related analytics logging.
 */
const val LEARNER_STUDY_ANALYTICS_DEFAULT_VALUE = false

/**
 * Qualifier to keep track of the sync status of the [EnableLearnerStudyAnalytics] and help determine
 * if the value of the feature flag is server-provided or default.
 */
@Qualifier
annotation class FlagEnableLearnerStudyAnalyticsIsServerProvided

/**
 * Default value for the feature flag sync status tracker corresponding to
 * [FlagEnableLearnerStudyAnalyticsIsServerProvided].
 */
const val FLAG_ENABLE_LEARNER_STUDY_ANALYTICS_IS_SERVER_PROVIDED_DEFAULT_VALUE = false

/**
 * Qualifier for a feature flag that controls whether learners may be allowed (via an
 * admin-controlled setting) to use a special in-lesson button for quickly switching between content
 * languages.
 *
 * This is generally expected to only be used in tandem with [EnableLearnerStudyAnalytics].
 */
@Qualifier annotation class EnableFastLanguageSwitchingInLesson

/** The feature flag name corresponding to [EnableFastLanguageSwitchingInLesson]. */
const val FAST_LANGUAGE_SWITCHING_IN_LESSON = "fast_language_switching_in_lesson"

/**
 * The default enabled state for the feature corresponding to [EnableFastLanguageSwitchingInLesson].
 */
const val FAST_LANGUAGE_SWITCHING_IN_LESSON_DEFAULT_VALUE = false

/**
 * Qualifier to keep track of the sync status of the [EnableFastLanguageSwitchingInLesson] and help determine
 * if the value of the feature flag is server-provided or default.
 */
@Qualifier
annotation class FlagEnableFastLanguageSwitchingInLessonIsServerProvided

/**
 * Default value for the feature flag sync status tracker corresponding to
 * [FlagEnableFastLanguageSwitchingInLessonIsServerProvided].
 */
const val FLAG_ENABLE_FAST_LANGUAGE_SWITCHING_IN_LESSON_IS_SERVER_PROVIDED_DEFAULT_VALUE = false

/**
 * Qualifier for a feature flag that controls whether learner study IDs should be generated and
 * logged with outgoing events.
 *
 * This is generally expected to only be used in tandem with [EnableLearnerStudyAnalytics].
 */
@Qualifier annotation class EnableLoggingLearnerStudyIds

/** The feature flag name corresponding to [EnableLoggingLearnerStudyIds]. */
const val LOGGING_LEARNER_STUDY_IDS = "logging_learner_study_ids"

/** The default enabled state for the feature corresponding to [EnableLoggingLearnerStudyIds]. */
const val LOGGING_LEARNER_STUDY_IDS_DEFAULT_VALUE = false

/**
 * Qualifier to keep track of the sync status of the [EnableLoggingLearnerStudyIds] and help determine
 * if the value of the feature flag is server-provided or default.
 */
@Qualifier
annotation class FlagEnableLoggingLearnerStudyIdsIsServerProvided

/**
 * Default value for the feature flag sync status tracker corresponding to
 * [FlagEnableLoggingLearnerStudyIdsIsServerProvided].
 */
const val FLAG_ENABLE_LOGGING_LEARNER_STUDY_IDS_IS_SERVER_PROVIDED_DEFAULT_VALUE = false

/** Qualifier for the feature flag corresponding to enabling the edit accounts options. */
@Qualifier
annotation class EnableEditAccountsOptionsUi

/** Default value for the feature flag corresponding to [EnableEditAccountsOptionsUi]. */
const val ENABLE_EDIT_ACCOUNTS_OPTIONS_UI_DEFAULT_VALUE = false

/**
 * Qualifier to keep track of the sync status of the [EnableEditAccountsOptionsUi] and help determine
 * if the value of the feature flag is server-provided or default.
 */
@Qualifier
annotation class FlagEnableEditAccountsOptionsUiIsServerProvided

/**
 * Default value for the feature flag sync status tracker corresponding to
 * [FlagEnableEditAccountsOptionsUiIsServerProvided].
 */
const val FLAG_ENABLE_EDIT_ACCOUNTS_OPTIONS_UI_IS_SERVER_PROVIDED_DEFAULT_VALUE = false

/** Qualifier for the platform parameter that controls whether to record performance metrics. */
@Qualifier
annotation class EnablePerformanceMetricsCollection

/** Name of the platform parameter that controls whether to record performance metrics. */
const val ENABLE_PERFORMANCE_METRICS_COLLECTION = "enable_performance_metrics_collection"

/** Default value for whether to record performance metrics. */
const val ENABLE_PERFORMANCE_METRICS_COLLECTION_DEFAULT_VALUE = false

/**
 * Qualifier to keep track of the sync status of the [EnablePerformanceMetricsCollection] and help
 * determine if the value of the feature flag is server-provided or default.
 */
@Qualifier
annotation class FlagEnablePerformanceMetricsCollectionIsServerProvided

/**
 * Default value for the feature flag sync status tracker corresponding to
 * [FlagEnablePerformanceMetricsCollectionIsServerProvided].
 */
const val FLAG_ENABLE_PERFORMANCE_METRICS_COLLECTION_IS_SERVER_PROVIDED_DEFAULT_VALUE = false

/**
 * Qualifier for the platform parameter that controls whether to animate the continue button
 * interaction and navigation items. This is used to disable the animation during testing because
 * Espresso has known problems while testing views that contain animations.
 */
@Qualifier
annotation class EnableContinueButtonAnimation

/** Default value for whether to enable continue button animation. */
const val ENABLE_CONTINUE_BUTTON_ANIMATION_DEFAULT_VALUE = true

/**
 * Qualifier to keep track of the sync status of the [EnableContinueButtonAnimation] and help
 * determine if the value of the feature flag is server-provided or default.
 */
@Qualifier
annotation class FlagEnableContinueButtonAnimationIsServerProvided

/**
 * Default value for the feature flag sync status tracker corresponding to
 * [FlagEnableContinueButtonAnimationIsServerProvided].
 */
const val FLAG_ENABLE_CONTINUE_BUTTON_ANIMATION_IS_SERVER_PROVIDED_DEFAULT_VALUE = false

/** Qualifier for the feature flag corresponding to enabling the spotlight UI. */
@Qualifier
annotation class EnableSpotlightUi

/** Default value for the feature flag corresponding to [EnableSpotlightUi]. */
const val ENABLE_SPOTLIGHT_UI_DEFAULT_VALUE = false

/**
 * Qualifier to keep track of the sync status of the [EnableSpotlightUi] and help determine
 * if the value of the feature flag is server-provided or default.
 */
@Qualifier
annotation class FlagEnableSpotlightUiIsServerProvided

/**
 * Default value for the feature flag sync status tracker corresponding to
 * [FlagEnableSpotlightUiIsServerProvided].
 */
const val FLAG_ENABLE_SPOTLIGHT_UI_IS_SERVER_PROVIDED_DEFAULT_VALUE = false

/**
 * Qualifier for the feature flag that controls whether input interaction state is correctly
 * retained across configuration changes.
 */
@Qualifier
annotation class EnableInteractionConfigChangeStateRetention

/**
 * Default value for feature flag corresponding to [EnableInteractionConfigChangeStateRetention].
 */
const val ENABLE_INTERACTION_CONFIG_CHANGE_STATE_RETENTION_DEFAULT_VALUE = false

/**
 * Qualifier to keep track of the sync status of the [EnableInteractionConfigChangeStateRetention]
 * and help determine if the value of the feature flag is server-provided or default.
 */
@Qualifier
annotation class FlagEnableInteractionConfigChangeStateRetentionIsServerProvided

/**
 * Default value for the feature flag sync status tracker corresponding to
 * [FlagEnableInteractionConfigChangeStateRetentionIsServerProvided].
 */
const val FLAG_ENABLE_INTERACTION_CONFIG_CHANGE_STATE_RETENTION_IS_SERVER_PROVIDED_DEFAULT_VALUE = false

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
 * Qualifier to keep track of the sync status of the [EnableAppAndOsDeprecation] and help determine
 * if the value of the feature flag is server-provided or default.
 */
@Qualifier
annotation class FlagEnableAppAndOsDeprecationIsServerProvided

/**
 * Default value for the feature flag sync status tracker corresponding to
 * [FlagEnableAppAndOsDeprecationIsServerProvided].
 */
const val FLAG_ENABLE_APP_AND_OS_DEPRECATION_IS_SERVER_PROVIDED_DEFAULT_VALUE = false
