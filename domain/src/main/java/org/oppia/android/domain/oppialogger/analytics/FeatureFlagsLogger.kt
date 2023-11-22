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
import org.oppia.android.util.platformparameter.FAST_LANGUAGE_SWITCHING_IN_LESSON
import org.oppia.android.util.platformparameter.INTERACTION_CONFIG_CHANGE_STATE_RETENTION
import org.oppia.android.util.platformparameter.LEARNER_STUDY_ANALYTICS
import org.oppia.android.util.platformparameter.LOGGING_LEARNER_STUDY_IDS
import org.oppia.android.util.platformparameter.PERFORMANCE_METRICS_COLLECTION
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.platformparameter.SPOTLIGHT_UI
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Convenience logger for feature flags.
 *
 * This logger is meant to be used to log the current status of all feature flags once after the app
 * has been launched.
 */
@Singleton
class FeatureFlagsLogger @Inject constructor(
  private val analyticsController: AnalyticsController,
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
   * This method logs all the configured feature flags to firebase.
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
    val listOfFlags = arrayListOf<FeatureFlagItem>()

    val enableDownloadsSupportFlagItem = FeatureFlagItem.newBuilder()
      .setFlagName(DOWNLOADS_SUPPORT)
      .setFlagEnabledState(enableDownloadsSupport.value)
      .setFlagSyncStatus(enableDownloadsSupport.syncStatus)
      .build()

    val enableExtraTopicTabsUiFlagItem = FeatureFlagItem.newBuilder()
      .setFlagName(EXTRA_TOPIC_TABS_UI)
      .setFlagEnabledState(enableExtraTopicTabsUi.value)
      .setFlagSyncStatus(enableExtraTopicTabsUi.syncStatus)
      .build()

    val enableLearnerStudyAnalyticsFlagItem = FeatureFlagItem.newBuilder()
      .setFlagName(LEARNER_STUDY_ANALYTICS)
      .setFlagEnabledState(enableLearnerStudyAnalytics.value)
      .setFlagSyncStatus(enableLearnerStudyAnalytics.syncStatus)
      .build()

    val enableFastLanguageSwitchingInLessonFlagItem = FeatureFlagItem.newBuilder()
      .setFlagName(FAST_LANGUAGE_SWITCHING_IN_LESSON)
      .setFlagEnabledState(enableFastLanguageSwitchingInLesson.value)
      .setFlagSyncStatus(enableFastLanguageSwitchingInLesson.syncStatus)
      .build()

    val enableLoggingLearnerStudyIdsFlagItem = FeatureFlagItem.newBuilder()
      .setFlagName(LOGGING_LEARNER_STUDY_IDS)
      .setFlagEnabledState(enableLoggingLearnerStudyIds.value)
      .setFlagSyncStatus(enableLoggingLearnerStudyIds.syncStatus)
      .build()

    val enableEditAccountsOptionsUiFlagItem = FeatureFlagItem.newBuilder()
      .setFlagName(EDIT_ACCOUNTS_OPTIONS_UI)
      .setFlagEnabledState(enableEditAccountsOptionsUi.value)
      .setFlagSyncStatus(enableEditAccountsOptionsUi.syncStatus)
      .build()

    val enablePerformanceMetricsCollectionFlagItem = FeatureFlagItem.newBuilder()
      .setFlagName(PERFORMANCE_METRICS_COLLECTION)
      .setFlagEnabledState(enablePerformanceMetricsCollection.value)
      .setFlagSyncStatus(enablePerformanceMetricsCollection.syncStatus)
      .build()

    val enableSpotlightUiFlagItem = FeatureFlagItem.newBuilder()
      .setFlagName(SPOTLIGHT_UI)
      .setFlagEnabledState(enableSpotlightUi.value)
      .setFlagSyncStatus(enableSpotlightUi.syncStatus)
      .build()

    val enableInteractionConfigChangeStateRetentionFlagItem = FeatureFlagItem.newBuilder()
      .setFlagName(INTERACTION_CONFIG_CHANGE_STATE_RETENTION)
      .setFlagEnabledState(enableInteractionConfigChangeStateRetention.value)
      .setFlagSyncStatus(enableInteractionConfigChangeStateRetention.syncStatus)
      .build()

    val enableAppAndOsDeprecationFlagItem = FeatureFlagItem.newBuilder()
      .setFlagName(APP_AND_OS_DEPRECATION)
      .setFlagEnabledState(enableAppAndOsDeprecation.value)
      .setFlagSyncStatus(enableAppAndOsDeprecation.syncStatus)
      .build()

    listOfFlags.addAll(
      listOf(
        enableDownloadsSupportFlagItem, enableExtraTopicTabsUiFlagItem,
        enableLearnerStudyAnalyticsFlagItem, enableFastLanguageSwitchingInLessonFlagItem,
        enableLoggingLearnerStudyIdsFlagItem, enableEditAccountsOptionsUiFlagItem,
        enablePerformanceMetricsCollectionFlagItem, enableSpotlightUiFlagItem,
        enableInteractionConfigChangeStateRetentionFlagItem, enableAppAndOsDeprecationFlagItem
      )
    )

    return listOfFlags
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
          .setUuid("")
          .setSessionId(sessionId)
          .addAllFeatureFlag(
            compileFeatureFlagsForLogging()
          )
          .build()
      )
      .build()
  }
}
