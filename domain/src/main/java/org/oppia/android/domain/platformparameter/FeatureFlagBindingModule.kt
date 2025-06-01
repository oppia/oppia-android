package org.oppia.android.domain.platformparameter

import dagger.Module
import dagger.Provides
import org.oppia.android.app.model.FeatureFlagId
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
import org.oppia.android.util.platformparameter.PlatformParameterValue

// TODO(#5835): Remove this module.
@Module
class FeatureFlagBindingModule {
  @Provides
  @EnableDownloadsSupport
  fun provideEnableDownloadsSupport(processState: PlatformParameterProcessState) =
    processState.retrieveFeatureFlag(FeatureFlagId.DOWNLOADS_SUPPORT)

  @Provides
  @EnableEditAccountsOptionsUi
  fun provideEnableEditAccountsOptionsUi(processState: PlatformParameterProcessState) =
    processState.retrieveFeatureFlag(FeatureFlagId.EDIT_ACCOUNTS_OPTIONS_UI)

  @Provides
  @EnableLearnerStudyAnalytics
  fun provideLearnerStudyAnalytics(processState: PlatformParameterProcessState) =
    processState.retrieveFeatureFlag(FeatureFlagId.LEARNER_STUDY_ANALYTICS)

  @Provides
  @EnableFastLanguageSwitchingInLesson
  fun provideFastInLessonLanguageSwitching(processState: PlatformParameterProcessState) =
    processState.retrieveFeatureFlag(FeatureFlagId.FAST_LANGUAGE_SWITCHING_IN_LESSON)

  @Provides
  @EnableLoggingLearnerStudyIds
  fun provideLoggingLearnerStudyIds(processState: PlatformParameterProcessState) =
    processState.retrieveFeatureFlag(FeatureFlagId.LOGGING_LEARNER_STUDY_IDS)

  @Provides
  @EnablePerformanceMetricsCollection
  fun provideEnablePerformanceMetricCollection(processState: PlatformParameterProcessState) =
    processState.retrieveFeatureFlag(FeatureFlagId.PERFORMANCE_METRICS_COLLECTION)

  @Provides
  @EnableSpotlightUi
  fun provideEnableSpotlightUi(processState: PlatformParameterProcessState) =
    processState.retrieveFeatureFlag(FeatureFlagId.SPOTLIGHT_UI)

  @Provides
  @EnableExtraTopicTabsUi
  fun provideEnableExtraTopicTabsUi(processState: PlatformParameterProcessState) =
    processState.retrieveFeatureFlag(FeatureFlagId.EXTRA_TOPIC_TABS_UI)

  @Provides
  @EnableInteractionConfigChangeStateRetention
  fun provideEnableInteractionConfigChangeStateRetention(
    processState: PlatformParameterProcessState
  ) =
    processState.retrieveFeatureFlag(FeatureFlagId.INTERACTION_CONFIG_CHANGE_STATE_RETENTION)

  @Provides
  @EnableAppAndOsDeprecation
  fun provideEnableAppAndOsDeprecation(processState: PlatformParameterProcessState) =
    processState.retrieveFeatureFlag(FeatureFlagId.APP_AND_OS_DEPRECATION)

  @Provides
  @EnableNpsSurvey
  fun provideEnableNpsSurvey(processState: PlatformParameterProcessState) =
    processState.retrieveFeatureFlag(FeatureFlagId.NPS_SURVEY)

  @Provides
  @EnableOnboardingFlowV2
  fun provideEnableOnboardingFlowV2(processState: PlatformParameterProcessState) =
    processState.retrieveFeatureFlag(FeatureFlagId.ONBOARDING_FLOW_V2)

  @Provides
  @EnableMultipleClassrooms
  fun provideEnableMultipleClassrooms(processState: PlatformParameterProcessState) =
    processState.retrieveFeatureFlag(FeatureFlagId.MULTIPLE_CLASSROOMS)

  private companion object {
    private fun PlatformParameterProcessState.retrieveFeatureFlag(
      featureFlagId: FeatureFlagId
    ): PlatformParameterValue<Boolean> {
      return object : PlatformParameterValue<Boolean> {
        override val value = retrieveFeatureFlagState(featureFlagId)
        override val syncStatus = retrieveFeatureFlagSyncStatus(featureFlagId)
      }
    }
  }
}
