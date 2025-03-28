package org.oppia.android.domain.oppialogger.analytics

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.FeatureFlagId
import org.oppia.android.app.model.FeatureFlagId.DOWNLOADS_SUPPORT
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.data.backends.gae.NetworkConfigTestModule
import org.oppia.android.data.backends.gae.NetworkModule
import org.oppia.android.domain.oppialogger.EventLogStorageCacheSize
import org.oppia.android.domain.oppialogger.ExceptionLogStorageCacheSize
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.platformparameter.testing.PlatformParameterTestInitializer
import org.oppia.android.domain.platformparameter.testing.PlatformParameterTestModule
import org.oppia.android.domain.platformparameter.testing.TestPlatformParameterConfigRetriever
import org.oppia.android.testing.FakeAnalyticsEventLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Iteration
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Parameter
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.SelectRunnerPlatform
import org.oppia.android.testing.junit.ParameterizedRobolectricTestRunner
import org.oppia.android.testing.logging.EventLogSubject.Companion.assertThat
import org.oppia.android.testing.logging.SyncStatusTestModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [FeatureFlagsLogger]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(OppiaParameterizedTestRunner::class)
@SelectRunnerPlatform(ParameterizedRobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = FeatureFlagsLoggerTest.TestApplication::class,
  sdk = [Build.VERSION_CODES.O]
)
class FeatureFlagsLoggerTest {
  // This initializes platform parameters and feature flags at injection, so it's unused.
  @[Inject Suppress("unused")] lateinit var flagInitializer: PlatformParameterTestInitializer
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var featureFlagsLogger: FeatureFlagsLogger
  @Inject lateinit var fakeAnalyticsEventLogger: FakeAnalyticsEventLogger

  @Parameter var index: Int = Int.MIN_VALUE
  @Parameter lateinit var flagId: String

  private val flagIdParam get() = FeatureFlagId.valueOf(flagId)

  @After
  fun tearDown() {
    TestPlatformParameterConfigRetriever.reset()
  }

  @Test
  fun testLogFeatureFlags_logFeatureFlags_hasEmptyUserUuid() {
    setUpTestApplicationComponent()

    // TODO(#5341): The user UUID is not set in this test context and is expected to be empty.
    featureFlagsLogger.logAllFeatureFlags(TEST_SESSION_ID)
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasFeatureFlagContextThat {
      hasUniqueUserUuidThat().isEmpty()
    }
  }

  @Test
  fun testLogFeatureFlags_logFeatureFlags_hasCorrectSessionId() {
    setUpTestApplicationComponent()

    featureFlagsLogger.logAllFeatureFlags(TEST_SESSION_ID)
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasFeatureFlagContextThat {
      hasSessionIdThat().isEqualTo(TEST_SESSION_ID)
    }
  }

  @Test
  fun testLogFeatureFlags_withEnabled_logsCorrectValues() {
    TestPlatformParameterConfigRetriever.setFlagOverride(DOWNLOADS_SUPPORT, true)
    setUpTestApplicationComponent()

    featureFlagsLogger.logAllFeatureFlags(TEST_SESSION_ID)
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasFeatureFlagContextThat {
      hasFeatureFlagItemContextThatAtIndex(0) {
        hasIdThat().isEqualTo(DOWNLOADS_SUPPORT)
        hasEnabledStateThat().isTrue()
        hasSyncStatusThat().isEqualTo(SyncStatus.NOT_SYNCED_FROM_SERVER)
      }
    }
  }

  @Test
  fun testLogFeatureFlags_withDisabled_logsCorrectValues() {
    TestPlatformParameterConfigRetriever.setFlagOverride(DOWNLOADS_SUPPORT, false)
    setUpTestApplicationComponent()

    featureFlagsLogger.logAllFeatureFlags(TEST_SESSION_ID)
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasFeatureFlagContextThat {
      hasFeatureFlagItemContextThatAtIndex(0) {
        hasIdThat().isEqualTo(DOWNLOADS_SUPPORT)
        hasEnabledStateThat().isFalse()
        hasSyncStatusThat().isEqualTo(SyncStatus.NOT_SYNCED_FROM_SERVER)
      }
    }
  }

  @Test
  fun testLogFeatureFlags_correctNumberOfFeatureFlagsIsLogged() {
    setUpTestApplicationComponent()

    featureFlagsLogger.logAllFeatureFlags(TEST_SESSION_ID)
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasFeatureFlagContextThat {
      hasFeatureFlagItemCountThat().isEqualTo(13)
    }
  }

  @Test
  @Iteration("downloads_support", "index=0", "flagId=DOWNLOADS_SUPPORT")
  @Iteration("extra_topic_tabs_ui", "index=1", "flagId=EXTRA_TOPIC_TABS_UI")
  @Iteration("learner_study_analytics", "index=2", "flagId=LEARNER_STUDY_ANALYTICS")
  @Iteration(
    "fast_language_switching_in_lesson", "index=3", "flagId=FAST_LANGUAGE_SWITCHING_IN_LESSON"
  )
  @Iteration("logging_learner_study_ids", "index=4", "flagId=LOGGING_LEARNER_STUDY_IDS")
  @Iteration("edit_accounts_options_ui", "index=5", "flagId=EDIT_ACCOUNTS_OPTIONS_UI")
  @Iteration("performance_metrics_collection", "index=6", "flagId=PERFORMANCE_METRICS_COLLECTION")
  @Iteration("spotlight_ui", "index=7", "flagId=SPOTLIGHT_UI")
  @Iteration(
    "interaction_config_change_state_retention", "index=8",
    "flagId=INTERACTION_CONFIG_CHANGE_STATE_RETENTION"
  )
  @Iteration("app_and_os_deprecation", "index=9", "flagId=APP_AND_OS_DEPRECATION")
  @Iteration("nps_survey", "index=10", "flagId=NPS_SURVEY")
  @Iteration("onboarding_flow_v2", "index=11", "flagId=ONBOARDING_FLOW_V2")
  @Iteration("multiple_classrooms", "index=12", "flagId=MULTIPLE_CLASSROOMS")
  fun testLogFeatureFlags_allFeatureFlagNamesAreLogged() {
    setUpTestApplicationComponent()

    featureFlagsLogger.logAllFeatureFlags(TEST_SESSION_ID)
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasFeatureFlagContextThat {
      hasFeatureFlagItemContextThatAtIndex(index) {
        hasIdThat().isEqualTo(flagIdParam)
      }
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private companion object {
    private const val TEST_SESSION_ID = "test_session_id"
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    // TODO(#59): Either isolate these to their own shared test module, or use the real logging
    // module in tests to avoid needing to specify these settings for tests.
    @EnableConsoleLog
    @Provides
    fun provideEnableConsoleLog(): Boolean = true

    @EnableFileLog
    @Provides
    fun provideEnableFileLog(): Boolean = false

    @GlobalLogLevel
    @Provides
    fun provideGlobalLogLevel(): LogLevel = LogLevel.VERBOSE
  }

  @Module
  class TestLogStorageModule {
    @Provides
    @EventLogStorageCacheSize
    fun provideEventLogStorageCacheSize(): Int = 2

    @Provides
    @ExceptionLogStorageCacheSize
    fun provideExceptionLogStorageCacheSize(): Int = 2
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, RobolectricModule::class,
      TestDispatcherModule::class, TestLogStorageModule::class,
      NetworkConnectionUtilDebugModule::class, LocaleProdModule::class, FakeOppiaClockModule::class,
      PlatformParameterTestModule::class, PlatformParameterTestModule::class,
      LoggingIdentifierModule::class, SyncStatusTestModule::class, AssetModule::class,
      NetworkModule::class, NetworkConfigTestModule::class
    ]
  )

  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(featureFlagLoggerTest: FeatureFlagsLoggerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerFeatureFlagsLoggerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(featureFlagLoggerTest: FeatureFlagsLoggerTest) {
      component.inject(featureFlagLoggerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
