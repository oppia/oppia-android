package org.oppia.android.domain.platformparameter

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.oppia.android.app.model.FeatureFlagId
import org.oppia.android.util.platformparameter.EnableAppAndOsDeprecation
import org.oppia.android.util.platformparameter.EnableDownloadsSupport
import org.oppia.android.util.platformparameter.EnableEditAccountsOptionsUi
import org.oppia.android.util.platformparameter.EnableFastLanguageSwitchingInLesson
import org.oppia.android.util.platformparameter.EnableFlashbackSupport
import org.oppia.android.util.platformparameter.EnableInteractionConfigChangeStateRetention
import org.oppia.android.util.platformparameter.EnableLearnerStudyAnalytics
import org.oppia.android.util.platformparameter.EnableLoggingLearnerStudyIds
import org.oppia.android.util.platformparameter.EnableMultipleClassrooms
import org.oppia.android.util.platformparameter.EnableNpsSurvey
import org.oppia.android.util.platformparameter.EnableOnboardingFlowV2
import org.oppia.android.util.platformparameter.EnablePerformanceMetricsCollection
import org.oppia.android.util.platformparameter.EnableSpotlightUi
import org.oppia.android.util.platformparameter.EnableTopicInfoTab
import org.oppia.android.util.platformparameter.EnableTopicPracticeTab
import org.oppia.android.util.platformparameter.PlatformParameterValue

// TODO(#5835): Remove this module.
/** Dagger module for providing a map of feature flags, per [FeatureFlags]. */
@Module
interface FeatureFlagsMapBindingModule {
  @Binds
  @IntoMap
  @FeatureFlags
  @FeatureFlagIdKey(FeatureFlagId.DOWNLOADS_SUPPORT)
  fun bindDownloadsSupport(
    @EnableDownloadsSupport param: PlatformParameterValue<Boolean>
  ): PlatformParameterValue<Boolean>

  @Binds
  @IntoMap
  @FeatureFlags
  @FeatureFlagIdKey(FeatureFlagId.EDIT_ACCOUNTS_OPTIONS_UI)
  fun bindEditAccountsOptionsUi(
    @EnableEditAccountsOptionsUi param: PlatformParameterValue<Boolean>
  ): PlatformParameterValue<Boolean>

  @Binds
  @IntoMap
  @FeatureFlags
  @FeatureFlagIdKey(FeatureFlagId.LEARNER_STUDY_ANALYTICS)
  fun bindLearnerStudyAnalytics(
    @EnableLearnerStudyAnalytics param: PlatformParameterValue<Boolean>
  ): PlatformParameterValue<Boolean>

  @Binds
  @IntoMap
  @FeatureFlags
  @FeatureFlagIdKey(FeatureFlagId.FAST_LANGUAGE_SWITCHING_IN_LESSON)
  fun bindFastLanguageSwitchingInLesson(
    @EnableFastLanguageSwitchingInLesson param: PlatformParameterValue<Boolean>
  ): PlatformParameterValue<Boolean>

  @Binds
  @IntoMap
  @FeatureFlags
  @FeatureFlagIdKey(FeatureFlagId.LOGGING_LEARNER_STUDY_IDS)
  fun bindLoggingLearnerStudyIds(
    @EnableLoggingLearnerStudyIds param: PlatformParameterValue<Boolean>
  ): PlatformParameterValue<Boolean>

  @Binds
  @IntoMap
  @FeatureFlags
  @FeatureFlagIdKey(FeatureFlagId.PERFORMANCE_METRICS_COLLECTION)
  fun bindPerformanceMetricsCollection(
    @EnablePerformanceMetricsCollection param: PlatformParameterValue<Boolean>
  ): PlatformParameterValue<Boolean>

  @Binds
  @IntoMap
  @FeatureFlags
  @FeatureFlagIdKey(FeatureFlagId.SPOTLIGHT_UI)
  fun bindSpotlightUi(
    @EnableSpotlightUi param: PlatformParameterValue<Boolean>
  ): PlatformParameterValue<Boolean>

  @Binds
  @IntoMap
  @FeatureFlags
  @FeatureFlagIdKey(FeatureFlagId.INTERACTION_CONFIG_CHANGE_STATE_RETENTION)
  fun bindInteractionConfigChangeStateRetention(
    @EnableInteractionConfigChangeStateRetention param: PlatformParameterValue<Boolean>
  ): PlatformParameterValue<Boolean>

  @Binds
  @IntoMap
  @FeatureFlags
  @FeatureFlagIdKey(FeatureFlagId.APP_AND_OS_DEPRECATION)
  fun bindAppAndOsDeprecation(
    @EnableAppAndOsDeprecation param: PlatformParameterValue<Boolean>
  ): PlatformParameterValue<Boolean>

  @Binds
  @IntoMap
  @FeatureFlags
  @FeatureFlagIdKey(FeatureFlagId.NPS_SURVEY)
  fun bindNpsSurvey(
    @EnableNpsSurvey param: PlatformParameterValue<Boolean>
  ): PlatformParameterValue<Boolean>

  @Binds
  @IntoMap
  @FeatureFlags
  @FeatureFlagIdKey(FeatureFlagId.ONBOARDING_FLOW_V2)
  fun bindOnboardingFlowV2(
    @EnableOnboardingFlowV2 param: PlatformParameterValue<Boolean>
  ): PlatformParameterValue<Boolean>

  @Binds
  @IntoMap
  @FeatureFlags
  @FeatureFlagIdKey(FeatureFlagId.MULTIPLE_CLASSROOMS)
  fun bindMultipleClassrooms(
    @EnableMultipleClassrooms param: PlatformParameterValue<Boolean>
  ): PlatformParameterValue<Boolean>

  @Binds
  @IntoMap
  @FeatureFlags
  @FeatureFlagIdKey(FeatureFlagId.FLASHBACK_SUPPORT)
  fun bindFlashbackSupport(
    @EnableFlashbackSupport param: PlatformParameterValue<Boolean>
  ): PlatformParameterValue<Boolean>

  @Binds
  @IntoMap
  @FeatureFlags
  @FeatureFlagIdKey(FeatureFlagId.TOPIC_INFO_TAB)
  fun bindTopicInfoTab(
    @EnableTopicInfoTab param: PlatformParameterValue<Boolean>
  ): PlatformParameterValue<Boolean>

  @Binds
  @IntoMap
  @FeatureFlags
  @FeatureFlagIdKey(FeatureFlagId.TOPIC_PRACTICE_TAB)
  fun bindTopicPracticeTab(
    @EnableTopicPracticeTab param: PlatformParameterValue<Boolean>
  ): PlatformParameterValue<Boolean>
}
