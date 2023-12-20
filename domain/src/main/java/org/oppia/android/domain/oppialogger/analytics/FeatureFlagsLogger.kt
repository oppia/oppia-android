package org.oppia.android.domain.oppialogger.analytics

import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.EventLog.FeatureFlagContext
import org.oppia.android.app.model.EventLog.FeatureFlagContext.FeatureFlagItem
import org.oppia.android.util.platformparameter.APP_AND_OS_DEPRECATION
import org.oppia.android.util.platformparameter.DOWNLOADS_SUPPORT
import org.oppia.android.util.platformparameter.EDIT_ACCOUNTS_OPTIONS_UI
import org.oppia.android.util.platformparameter.EXTRA_TOPIC_TABS_UI
import org.oppia.android.util.platformparameter.EnableAppAndOsDeprecation
import org.oppia.android.util.platformparameter.EnableDownloadsSupport
import org.oppia.android.util.platformparameter.EnableEditAccountsOptionsUi
import org.oppia.android.util.platformparameter.EnableExtraTopicTabsUi
import org.oppia.android.util.platformparameter.EnableFastLanguageSwitchingInLesson
import org.oppia.android.util.platformparameter.EnableInteractionConfigChangeStateRetention
import org.oppia.android.util.platformparameter.EnableLearnerStudyAnalytics
import org.oppia.android.util.platformparameter.EnableLoggingLearnerStudyIds
import org.oppia.android.util.platformparameter.EnablePerformanceMetricsCollection
import org.oppia.android.util.platformparameter.EnableSpotlightUi
import org.oppia.android.util.platformparameter.EnableTestFeatureFlag
import org.oppia.android.util.platformparameter.EnableTestFeatureFlagWithEnabledDefault
import org.oppia.android.util.platformparameter.FAST_LANGUAGE_SWITCHING_IN_LESSON
import org.oppia.android.util.platformparameter.INTERACTION_CONFIG_CHANGE_STATE_RETENTION
import org.oppia.android.util.platformparameter.LEARNER_STUDY_ANALYTICS
import org.oppia.android.util.platformparameter.LOGGING_LEARNER_STUDY_IDS
import org.oppia.android.util.platformparameter.PERFORMANCE_METRICS_COLLECTION
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.platformparameter.SPOTLIGHT_UI
import org.oppia.android.util.platformparameter.TEST_FEATURE_FLAG
import org.oppia.android.util.platformparameter.TEST_FEATURE_FLAG_WITH_ENABLED_DEFAULTS
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Convenience logger for feature flags.
 *
 * This logger is meant to be used for feature flag-related logging on every app launch. It is
 * primarily used within the ApplicationLifeCycleObserver to log the status of feature flags in a
 * given app session.
 */
@Singleton
class FeatureFlagsLogger @Inject constructor(
  private val analyticsController: AnalyticsController,
  @EnableTestFeatureFlag
  private val testFeatureFlag: PlatformParameterValue<Boolean>,
  @EnableTestFeatureFlagWithEnabledDefault
  private val testFeatureFlagWithEnabledDefault: PlatformParameterValue<Boolean>,
  @EnableDownloadsSupport
  private val enableDownloadsSupport: PlatformParameterValue<Boolean>,
  @EnableExtraTopicTabsUi
  private val enableExtraTopicTabsUi: PlatformParameterValue<Boolean>,
  @EnableLearnerStudyAnalytics
  private val enableLearnerStudyAnalytics: PlatformParameterValue<Boolean>,
  @EnableFastLanguageSwitchingInLesson
  private val enableFastLanguageSwitchingInLesson: PlatformParameterValue<Boolean>,
  @EnableLoggingLearnerStudyIds
  private val enableLoggingLearnerStudyIds: PlatformParameterValue<Boolean>,
  @EnableEditAccountsOptionsUi
  private val enableEditAccountsOptionsUi: PlatformParameterValue<Boolean>,
  @EnablePerformanceMetricsCollection
  private val enablePerformanceMetricsCollection: PlatformParameterValue<Boolean>,
  @EnableSpotlightUi
  private val enableSpotlightUi: PlatformParameterValue<Boolean>,
  @EnableInteractionConfigChangeStateRetention
  private val enableInteractionConfigChangeStateRetention: PlatformParameterValue<Boolean>,
  @EnableAppAndOsDeprecation
  private val enableAppAndOsDeprecation: PlatformParameterValue<Boolean>
) {
  /**
   * This method logs the name, enabled status and sync status of all feature flags to Firebase.
   *
   * @param sessionId denotes the id of the current appInForeground session
   */
  fun logAllFeatureFlags(sessionId: String) {
    analyticsController.logImportantEvent(
      createFeatureFlagContext(sessionId = sessionId),
      null
    )
  }

  /**
   * Collects all the feature flags in the app, creates a FeatureFlagItem for them and adds them to
   * a list that is returned to the calling object.
   *
   * @return a list of [FeatureFlagItem]s
   */
  private fun compileFeatureFlagsForLogging(): List<FeatureFlagItem> {
    val featureFlagItemMap = mapOf(
      TEST_FEATURE_FLAG to testFeatureFlag,
      TEST_FEATURE_FLAG_WITH_ENABLED_DEFAULTS to testFeatureFlagWithEnabledDefault,
      DOWNLOADS_SUPPORT to enableDownloadsSupport,
      EXTRA_TOPIC_TABS_UI to enableExtraTopicTabsUi,
      LEARNER_STUDY_ANALYTICS to enableLearnerStudyAnalytics,
      FAST_LANGUAGE_SWITCHING_IN_LESSON to enableFastLanguageSwitchingInLesson,
      LOGGING_LEARNER_STUDY_IDS to enableLoggingLearnerStudyIds,
      EDIT_ACCOUNTS_OPTIONS_UI to enableEditAccountsOptionsUi,
      PERFORMANCE_METRICS_COLLECTION to enablePerformanceMetricsCollection,
      SPOTLIGHT_UI to enableSpotlightUi,
      INTERACTION_CONFIG_CHANGE_STATE_RETENTION to enableInteractionConfigChangeStateRetention,
      APP_AND_OS_DEPRECATION to enableAppAndOsDeprecation,
    )

    val featureFlagItemList = mutableListOf<FeatureFlagItem>()
    for (entry in featureFlagItemMap) {
      featureFlagItemList.add(
        FeatureFlagItem.newBuilder()
          .setFlagName(entry.key)
          .setFlagEnabledState(entry.value.value)
          .setFlagSyncStatus(entry.value.syncStatus)
          .build()
      )
    }

    return featureFlagItemList
  }

  /**
   * Creates an [EventLog] context for the feature flags to be logged.
   *
   * @param sessionId denotes the session id of the current appInForeground session
   * @return an [EventLog.Context] for the feature flags to be logged
   */
  private fun createFeatureFlagContext(sessionId: String): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setFeatureFlagContext(
        FeatureFlagContext.newBuilder()
          .setSessionId(sessionId)
          .addAllFeatureFlags(compileFeatureFlagsForLogging())
          .build()
      )
      .build()
  }
}
