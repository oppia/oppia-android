package org.oppia.android.domain.oppialogger.analytics

import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.EventLog.FeatureFlagItemContext
import org.oppia.android.app.model.EventLog.FeatureFlagListContext
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.app.model.FeatureFlagId
import org.oppia.android.app.model.FeatureFlagId.APP_AND_OS_DEPRECATION
import org.oppia.android.app.model.FeatureFlagId.DOWNLOADS_SUPPORT
import org.oppia.android.app.model.FeatureFlagId.EDIT_ACCOUNTS_OPTIONS_UI
import org.oppia.android.app.model.FeatureFlagId.EXTRA_TOPIC_TABS_UI
import org.oppia.android.app.model.FeatureFlagId.FAST_LANGUAGE_SWITCHING_IN_LESSON
import org.oppia.android.app.model.FeatureFlagId.INTERACTION_CONFIG_CHANGE_STATE_RETENTION
import org.oppia.android.app.model.FeatureFlagId.LEARNER_STUDY_ANALYTICS
import org.oppia.android.app.model.FeatureFlagId.LOGGING_LEARNER_STUDY_IDS
import org.oppia.android.app.model.FeatureFlagId.MULTIPLE_CLASSROOMS
import org.oppia.android.app.model.FeatureFlagId.NPS_SURVEY
import org.oppia.android.app.model.FeatureFlagId.ONBOARDING_FLOW_V2
import org.oppia.android.app.model.FeatureFlagId.PERFORMANCE_METRICS_COLLECTION
import org.oppia.android.app.model.FeatureFlagId.SPOTLIGHT_UI
import org.oppia.android.domain.platformparameter.FeatureFlag

/**
 * Convenience logger for feature flags.
 *
 * This logger is meant to be used for feature flag-related logging on every app launch. It is
 * primarily used within the ApplicationLifecycleObserver to log the status of feature flags in a
 * given app session.
 */
@Singleton
class FeatureFlagsLogger @Inject constructor(
  private val analyticsController: AnalyticsController,
  @FeatureFlag(DOWNLOADS_SUPPORT) private val enableDownloadsSupport: Boolean,
  @FeatureFlag(EXTRA_TOPIC_TABS_UI) private val enableExtraTopicTabsUi: Boolean,
  @FeatureFlag(LEARNER_STUDY_ANALYTICS) private val enableLearnerStudyAnalytics: Boolean,
  @FeatureFlag(FAST_LANGUAGE_SWITCHING_IN_LESSON)
  private val enableFastLanguageSwitchingInLesson: Boolean,
  @FeatureFlag(LOGGING_LEARNER_STUDY_IDS) private val enableLoggingLearnerStudyIds: Boolean,
  @FeatureFlag(EDIT_ACCOUNTS_OPTIONS_UI) private val enableEditAccountsOptionsUi: Boolean,
  @FeatureFlag(PERFORMANCE_METRICS_COLLECTION)
  private val enablePerformanceMetricsCollection: Boolean,
  @FeatureFlag(SPOTLIGHT_UI) private val enableSpotlightUi: Boolean,
  @FeatureFlag(INTERACTION_CONFIG_CHANGE_STATE_RETENTION)
  private val enableInteractionConfigChangeStateRetention: Boolean,
  @FeatureFlag(APP_AND_OS_DEPRECATION) private val enableAppAndOsDeprecation: Boolean,
  @FeatureFlag(NPS_SURVEY) private val enableNpsSurvey: Boolean,
  @FeatureFlag(ONBOARDING_FLOW_V2) private val enableOnboardingFlowV2: Boolean,
  @FeatureFlag(MULTIPLE_CLASSROOMS) private val enableMultipleClassrooms: Boolean,
) {
  /**
   * A variable containing a list of all the feature flags in the app.
   *
   * @return a list of key-value pairs of [FeatureFlagId] and [Boolean]
   */
  private var featureFlagItemMap: Map<FeatureFlagId, Boolean> = mapOf(
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
    NPS_SURVEY to enableNpsSurvey,
    ONBOARDING_FLOW_V2 to enableOnboardingFlowV2,
    MULTIPLE_CLASSROOMS to enableMultipleClassrooms,
  )

  /**
   * This method can be used to override the featureFlagItemMap and sets its value to the given map.
   *
   * @param featureFlagItemMap denotes the map of [FeatureFlagId]s to their enabled states
   */
  fun setFeatureFlagItemMap(featureFlagItemMap: Map<FeatureFlagId, Boolean>) {
    this.featureFlagItemMap = featureFlagItemMap
  }

  /**
   * This method logs the name, enabled status and sync status of all feature flags to Firebase.
   *
   * @param appSessionId denotes the id of the current appInForeground session
   */
  fun logAllFeatureFlags(appSessionId: String) {
    val featureFlagItemList = mutableListOf<FeatureFlagItemContext>()
    for (flag in featureFlagItemMap) {
      featureFlagItemList.add(
        createFeatureFlagItemContext(flag)
      )
    }

    // TODO(#5341): Set the UUID value for this context
    val featureFlagContext = FeatureFlagListContext.newBuilder()
      .setAppSessionId(appSessionId)
      .addAllFeatureFlags(featureFlagItemList)
      .build()

    analyticsController.logLowPriorityEvent(
      EventLog.Context.newBuilder()
        .setFeatureFlagListContext(featureFlagContext)
        .build(),
      profileId = null
    )
  }

  /**
   * Creates an [EventLog] context for the feature flags to be logged.
   *
   * @param flagDetails denotes the key-value pair of the feature flag name and its enabled state
   * @return an [EventLog.Context] for the feature flags to be logged
   */
  private fun createFeatureFlagItemContext(
    flagDetails: Map.Entry<FeatureFlagId, Boolean>,
  ): FeatureFlagItemContext {
    // TODO: Fix this.
    return FeatureFlagItemContext.newBuilder()
//      .setFlagName(flagDetails.key)
      .setFlagEnabledState(flagDetails.value)
//      .setFlagSyncStatus(flagDetails.value.syncStatus)
      .build()
  }
}
