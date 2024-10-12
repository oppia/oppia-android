package org.oppia.android.domain.oppialogger.analytics

import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.EventLog.FeatureFlagItemContext
import org.oppia.android.app.model.EventLog.FeatureFlagListContext
import org.oppia.android.util.platformparameter.APP_AND_OS_DEPRECATION
import org.oppia.android.util.platformparameter.DOWNLOADS_SUPPORT
import org.oppia.android.util.platformparameter.EDIT_ACCOUNTS_OPTIONS_UI
import org.oppia.android.util.platformparameter.ENABLE_MULTIPLE_CLASSROOMS
import org.oppia.android.util.platformparameter.ENABLE_NPS_SURVEY
import org.oppia.android.util.platformparameter.ENABLE_ONBOARDING_FLOW_V2
import org.oppia.android.util.platformparameter.ENABLE_PERFORMANCE_METRICS_COLLECTION
import org.oppia.android.util.platformparameter.EXTRA_TOPIC_TABS_UI
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
import org.oppia.android.util.platformparameter.FAST_LANGUAGE_SWITCHING_IN_LESSON
import org.oppia.android.util.platformparameter.INTERACTION_CONFIG_CHANGE_STATE_RETENTION
import org.oppia.android.util.platformparameter.LEARNER_STUDY_ANALYTICS
import org.oppia.android.util.platformparameter.LOGGING_LEARNER_STUDY_IDS
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.platformparameter.SPOTLIGHT_UI
import javax.inject.Inject
import javax.inject.Singleton

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
  private val enableAppAndOsDeprecation: PlatformParameterValue<Boolean>,
  @EnableNpsSurvey
  private val enableNpsSurvey: PlatformParameterValue<Boolean>,
  @EnableOnboardingFlowV2
  private val enableOnboardingFlowV2: PlatformParameterValue<Boolean>,
  @EnableMultipleClassrooms
  private val enableMultipleClassrooms: PlatformParameterValue<Boolean>,
) {
  /**
   * A variable containing a list of all the feature flags in the app.
   *
   * @return a list of key-value pairs of [String] and [PlatformParameterValue]
   */
  private var featureFlagItemMap: Map<String, PlatformParameterValue<Boolean>> = mapOf(
    DOWNLOADS_SUPPORT to enableDownloadsSupport,
    EXTRA_TOPIC_TABS_UI to enableExtraTopicTabsUi,
    LEARNER_STUDY_ANALYTICS to enableLearnerStudyAnalytics,
    FAST_LANGUAGE_SWITCHING_IN_LESSON to enableFastLanguageSwitchingInLesson,
    LOGGING_LEARNER_STUDY_IDS to enableLoggingLearnerStudyIds,
    EDIT_ACCOUNTS_OPTIONS_UI to enableEditAccountsOptionsUi,
    ENABLE_PERFORMANCE_METRICS_COLLECTION to enablePerformanceMetricsCollection,
    SPOTLIGHT_UI to enableSpotlightUi,
    INTERACTION_CONFIG_CHANGE_STATE_RETENTION to enableInteractionConfigChangeStateRetention,
    APP_AND_OS_DEPRECATION to enableAppAndOsDeprecation,
    ENABLE_NPS_SURVEY to enableNpsSurvey,
    ENABLE_ONBOARDING_FLOW_V2 to enableOnboardingFlowV2,
    ENABLE_MULTIPLE_CLASSROOMS to enableMultipleClassrooms,
  )

  /**
   * This method can be used to override the featureFlagItemMap and sets its value to the given map.
   *
   * @param featureFlagItemMap denotes the map of feature flag names to their corresponding
   * [PlatformParameterValue]s
   */
  fun setFeatureFlagItemMap(featureFlagItemMap: Map<String, PlatformParameterValue<Boolean>>) {
    this.featureFlagItemMap = featureFlagItemMap
  }

  /**
   * This method logs the name, enabled status and sync status of all feature flags to Firebase.
   *
   * @param appSessionId denotes the id of the current appInForeground session
   */
  fun logAllFeatureFlags(appSessionId: String, currentProfileUuid: String?) {
    val featureFlagItemList = mutableListOf<FeatureFlagItemContext>()
    for (flag in featureFlagItemMap) {
      featureFlagItemList.add(
        createFeatureFlagItemContext(flag)
      )
    }

    // TODO(#5341): Set the UUID value for this context
    val featureFlagContext = FeatureFlagListContext.newBuilder()
      .setAppSessionId(appSessionId)
      .setUniqueUserUuid(currentProfileUuid)
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
   * @param flagDetails denotes the key-value pair of the feature flag name and its corresponding
   * [PlatformParameterValue]
   * @return an [EventLog.Context] for the feature flags to be logged
   */
  private fun createFeatureFlagItemContext(
    flagDetails: Map.Entry<String, PlatformParameterValue<Boolean>>,
  ): FeatureFlagItemContext {
    return FeatureFlagItemContext.newBuilder()
      .setFlagName(flagDetails.key)
      .setFlagEnabledState(flagDetails.value.value)
      .setFlagSyncStatus(flagDetails.value.syncStatus)
      .build()
  }
}
