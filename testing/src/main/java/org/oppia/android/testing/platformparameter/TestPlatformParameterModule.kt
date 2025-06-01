package org.oppia.android.testing.platformparameter

import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import org.oppia.android.app.model.FeatureFlagId
import org.oppia.android.domain.platformparameter.FeatureFlagBindingModule
import org.oppia.android.domain.platformparameter.FeatureFlagsMapBindingModule
import org.oppia.android.domain.platformparameter.PlatformParameterBindingModule
import org.oppia.android.domain.platformparameter.PlatformParameterConfigRetriever
import org.oppia.android.domain.platformparameter.PlatformParameterController
import org.oppia.android.domain.platformparameter.PlatformParameterControllerProdImpl
import org.oppia.android.domain.platformparameter.PlatformParameterProcessState
import org.oppia.android.domain.platformparameter.testing.TestPlatformParameterConfigRetriever
import org.oppia.android.testing.threading.TestCoroutineDispatchers

/* Fake Platform Parameter Module that provides individual Platform Parameters for testing. */
@Module(
  includes = [
    FeatureFlagsMapBindingModule::class,
    FeatureFlagBindingModule::class,
    PlatformParameterBindingModule::class
  ]
)
class TestPlatformParameterModule {
  private val processState by lazy { PlatformParameterProcessState() }

  @Provides
  @Singleton
  fun providePlatformParameterController(
    factory: PlatformParameterControllerProdImpl.Factory
  ): PlatformParameterController = factory.create(processState)

  @Provides
  fun providePlatformParameterConfigRetriever(
    impl: TestPlatformParameterConfigRetriever
  ): PlatformParameterConfigRetriever = impl

  @Provides
  @Singleton
  fun providePlatformParameterProcessState(
    platformParameterController: PlatformParameterController,
    testCoroutineDispatchers: TestCoroutineDispatchers
  ): PlatformParameterProcessState {
    // TODO(#5835): Remove this blocking hack to ensure tests are properly initialized for params.
    val loadDeferred = platformParameterController.loadParametersAsync()
    testCoroutineDispatchers.runCurrent()
    check(loadDeferred.isCompleted) { "Expected parameter loading to have finished." }
    return processState
  }

  companion object {
    fun forceEnableDownloadsSupport(value: Boolean) {
      TestPlatformParameterConfigRetriever.setFlagOverride(FeatureFlagId.DOWNLOADS_SUPPORT, value)
    }

    fun forceEnableEditAccountsOptionsUi(value: Boolean) {
      TestPlatformParameterConfigRetriever.setFlagOverride(FeatureFlagId.EDIT_ACCOUNTS_OPTIONS_UI, value)
    }

    fun forceEnableLearnerStudyAnalytics(value: Boolean) {
      TestPlatformParameterConfigRetriever.setFlagOverride(FeatureFlagId.LEARNER_STUDY_ANALYTICS, value)
    }

    fun forceEnableFastLanguageSwitchingInLesson(value: Boolean) {
      TestPlatformParameterConfigRetriever.setFlagOverride(FeatureFlagId.FAST_LANGUAGE_SWITCHING_IN_LESSON, value)
    }

    fun forceEnableLoggingLearnerStudyIds(value: Boolean) {
      TestPlatformParameterConfigRetriever.setFlagOverride(FeatureFlagId.LOGGING_LEARNER_STUDY_IDS, value)
    }

    fun forceEnableExtraTopicTabsUi(value: Boolean) {
      TestPlatformParameterConfigRetriever.setFlagOverride(FeatureFlagId.EXTRA_TOPIC_TABS_UI, value)
    }

    fun forceEnablePerformanceMetricsCollection(value: Boolean) {
      TestPlatformParameterConfigRetriever.setFlagOverride(FeatureFlagId.PERFORMANCE_METRICS_COLLECTION, value)
    }

    fun forceEnableSpotlightUi(value: Boolean) {
      TestPlatformParameterConfigRetriever.setFlagOverride(FeatureFlagId.SPOTLIGHT_UI, value)
    }

    fun forceEnableNpsSurvey(value: Boolean) {
      TestPlatformParameterConfigRetriever.setFlagOverride(FeatureFlagId.NPS_SURVEY, value)
    }

    fun forceEnableOnboardingFlowV2(value: Boolean) {
      TestPlatformParameterConfigRetriever.setFlagOverride(FeatureFlagId.ONBOARDING_FLOW_V2, value)
    }

    fun forceEnableMultipleClassrooms(value: Boolean) {
      TestPlatformParameterConfigRetriever.setFlagOverride(FeatureFlagId.MULTIPLE_CLASSROOMS, value)
    }

    fun reset() {
      TestPlatformParameterConfigRetriever.reset()
    }
  }
}
