package org.oppia.android.testing.platformparameter

import android.os.Looper
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.oppia.android.app.model.FeatureFlagId.APP_AND_OS_DEPRECATION
import org.oppia.android.app.model.FeatureFlagId.DOWNLOADS_SUPPORT
import org.oppia.android.app.model.FeatureFlagId.EDIT_ACCOUNTS_OPTIONS_UI
import org.oppia.android.app.model.FeatureFlagId.FAST_LANGUAGE_SWITCHING_IN_LESSON
import org.oppia.android.app.model.FeatureFlagId.FLASHBACK_SUPPORT
import org.oppia.android.app.model.FeatureFlagId.LEARNER_STUDY_ANALYTICS
import org.oppia.android.app.model.FeatureFlagId.LOGGING_LEARNER_STUDY_IDS
import org.oppia.android.app.model.FeatureFlagId.MULTIPLE_CLASSROOMS
import org.oppia.android.app.model.FeatureFlagId.NPS_SURVEY
import org.oppia.android.app.model.FeatureFlagId.ONBOARDING_FLOW_V2
import org.oppia.android.app.model.FeatureFlagId.PERFORMANCE_METRICS_COLLECTION
import org.oppia.android.app.model.FeatureFlagId.SPOTLIGHT_UI
import org.oppia.android.app.model.FeatureFlagId.TOPIC_INFO_TAB
import org.oppia.android.app.model.FeatureFlagId.TOPIC_PRACTICE_TAB
import org.oppia.android.domain.platformparameter.FeatureFlagBindingModule
import org.oppia.android.domain.platformparameter.FeatureFlagsMapBindingModule
import org.oppia.android.domain.platformparameter.PlatformParameterBindingModule
import org.oppia.android.domain.platformparameter.PlatformParameterConfigRetriever
import org.oppia.android.domain.platformparameter.PlatformParameterController
import org.oppia.android.domain.platformparameter.PlatformParameterControllerProdImpl
import org.oppia.android.domain.platformparameter.PlatformParameterProcessState
import org.oppia.android.domain.platformparameter.testing.TestPlatformParameterConfigRetriever
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import javax.inject.Singleton
import kotlinx.coroutines.CompletableDeferred
import org.oppia.android.app.model.PlatformParameterId
import org.oppia.android.app.model.PlatformParameterId.PERFORMANCE_METRICS_COLLECTION_HIGH_FREQUENCY_TIME_INTERVAL_IN_MINUTES
import org.oppia.android.app.model.PlatformParameterId.PERFORMANCE_METRICS_COLLECTION_LOW_FREQUENCY_TIME_INTERVAL_IN_MINUTES
import org.oppia.android.app.model.PlatformParameterId.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS

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
  fun providePlatformParameterControllerProdImpl(
    platformParameterProcessState: PlatformParameterProcessState,
    factory: PlatformParameterControllerProdImpl.Factory
  ) = factory.create(platformParameterProcessState)

  @Provides
  @Singleton
  fun providePlatformParameterController(
    factory: PlatformParameterControllerProdImpl.Factory,
    testCoroutineDispatchers: TestCoroutineDispatchers
  ): PlatformParameterController {
    val prodController = factory.create(processState)
    return object : PlatformParameterController {
      private var isLoaded = false

      override fun loadParametersAsync(): Deferred<Unit> {
        // Calling code can be blocking which means the returned deferred must run immediately,
        // hence the use of the dispatcher. The Main dispatcher must be used due to the runCurrent()
        // call, however, since otherwise it can contend with tests trying to synchronize state and
        // then deadlock. Additional work must be done to ensure that the thread hop *doesn't*
        // happen if the main thread is already being used (since that could then cause a deadlock).
        if (isLoaded) {
          // This is a slight hack to allow the PlatformParameterProcessState injector to return
          // immediately if parameters are already loaded. This is needed in more complex
          // cross-thread initialization cases such as for background workers.
          return CompletableDeferred(Unit)
        }
        val dispatcher = if (Looper.getMainLooper().isCurrentThread) {
          Dispatchers.Unconfined // Run immediately on the current (main) thread.
        } else Dispatchers.Main // Defer to the main thread.
        return CoroutineScope(dispatcher).async { loadParamsAndRunTestDispatchersOnMainThread() }
      }

      override fun getParameterInitializationStatus() =
        prodController.getParameterInitializationStatus()

      override fun downloadRemoteParameters() = prodController.downloadRemoteParameters()

      private fun loadParamsAndRunTestDispatchersOnMainThread() {
        // TODO(#5835): Remove this blocking hack to ensure tests are properly inited for params.
        val loadResult = prodController.loadParametersAsync()
        testCoroutineDispatchers.runCurrent()
        check(loadResult.isCompleted) { "Expected parameter loading to have finished." }
        isLoaded = true
      }
    }
  }

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
    // TODO(#5835): Remove this blocking hack to ensure tests are properly inited for params. Make
    //  sure to double check parameter loading in BootstrapOppiaWorker. Commenting out that line
    //  should trigger test failures once this mechanism is cleaned up. If it doesn't then work will
    //  be needed in BootstrapOppiaWorkerTest to ensure that line is properly tested (since removing
    //  it can cause catastrophic problems in deployed apps--it MUST be tested).
    val loadDeferred = platformParameterController.loadParametersAsync()
    if (!loadDeferred.isCompleted) {
      testCoroutineDispatchers.runCurrent()
    }
    check(loadDeferred.isCompleted) { "Expected parameter loading to have finished." }
    return processState
  }

  companion object {
    fun forceEnableDownloadsSupport(value: Boolean) {
      TestPlatformParameterConfigRetriever.setFlagOverride(DOWNLOADS_SUPPORT, value)
    }

    fun forceEnableEditAccountsOptionsUi(value: Boolean) {
      TestPlatformParameterConfigRetriever.setFlagOverride(EDIT_ACCOUNTS_OPTIONS_UI, value)
    }

    fun forceEnableLearnerStudyAnalytics(value: Boolean) {
      TestPlatformParameterConfigRetriever.setFlagOverride(LEARNER_STUDY_ANALYTICS, value)
    }

    fun forceEnableFastLanguageSwitchingInLesson(value: Boolean) {
      TestPlatformParameterConfigRetriever.setFlagOverride(FAST_LANGUAGE_SWITCHING_IN_LESSON, value)
    }

    fun forceEnableLoggingLearnerStudyIds(value: Boolean) {
      TestPlatformParameterConfigRetriever.setFlagOverride(LOGGING_LEARNER_STUDY_IDS, value)
    }

    fun forceEnablePerformanceMetricsCollection(value: Boolean) {
      TestPlatformParameterConfigRetriever.setFlagOverride(PERFORMANCE_METRICS_COLLECTION, value)
    }

    fun forceEnableSpotlightUi(value: Boolean) {
      TestPlatformParameterConfigRetriever.setFlagOverride(SPOTLIGHT_UI, value)
    }

    fun forceEnableNpsSurvey(value: Boolean) {
      TestPlatformParameterConfigRetriever.setFlagOverride(NPS_SURVEY, value)
    }

    fun forceEnableOnboardingFlowV2(value: Boolean) {
      TestPlatformParameterConfigRetriever.setFlagOverride(ONBOARDING_FLOW_V2, value)
    }

    fun forceEnableMultipleClassrooms(value: Boolean) {
      TestPlatformParameterConfigRetriever.setFlagOverride(MULTIPLE_CLASSROOMS, value)
    }

    fun forceEnableAppAndOsDeprecation(value: Boolean) {
      TestPlatformParameterConfigRetriever.setFlagOverride(APP_AND_OS_DEPRECATION, value)
    }

    fun forceEnableFlashbackSupport(value: Boolean) {
      TestPlatformParameterConfigRetriever.setFlagOverride(FLASHBACK_SUPPORT, value)
    }

    fun forceEnableTopicInfoTab(value: Boolean) {
      TestPlatformParameterConfigRetriever.setFlagOverride(TOPIC_INFO_TAB, value)
    }

    fun forceEnableTopicPracticeTab(value: Boolean) {
      TestPlatformParameterConfigRetriever.setFlagOverride(TOPIC_PRACTICE_TAB, value)
    }

    fun forcePerformanceMetricsCollectionHighFrequencyTimeIntervalInMinutes(value: Int) {
      TestPlatformParameterConfigRetriever.setParameterOverride(
        PERFORMANCE_METRICS_COLLECTION_HIGH_FREQUENCY_TIME_INTERVAL_IN_MINUTES, value
      )
    }

    fun forcePerformanceMetricsCollectionLowFrequencyTimeIntervalInMinutes(value: Int) {
      TestPlatformParameterConfigRetriever.setParameterOverride(
        PERFORMANCE_METRICS_COLLECTION_LOW_FREQUENCY_TIME_INTERVAL_IN_MINUTES, value
      )
    }

    fun forceSyncUpWorkerTimePeriodInHours(value: Int) {
      TestPlatformParameterConfigRetriever.setParameterOverride(
        SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS, value
      )
    }

    fun reset() {
      TestPlatformParameterConfigRetriever.reset()
    }
  }
}
