package org.oppia.android.domain.oppialogger.analytics

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.PlatformParameter.SyncStatus
import org.oppia.android.domain.oppialogger.EventLogStorageCacheSize
import org.oppia.android.domain.oppialogger.ExceptionLogStorageCacheSize
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.FakeAnalyticsEventLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Iteration
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Parameter
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.SelectRunnerPlatform
import org.oppia.android.testing.junit.ParameterizedRobolectricTestRunner
import org.oppia.android.testing.logging.EventLogSubject.Companion.assertThat
import org.oppia.android.testing.logging.SyncStatusTestModule
import org.oppia.android.testing.platformparameter.EnableTestFeatureFlag
import org.oppia.android.testing.platformparameter.EnableTestFeatureFlagWithEnabledDefault
import org.oppia.android.testing.platformparameter.TEST_FEATURE_FLAG
import org.oppia.android.testing.platformparameter.TEST_FEATURE_FLAG_WITH_ENABLED_DEFAULTS
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
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
import org.oppia.android.util.platformparameter.APP_AND_OS_DEPRECATION
import org.oppia.android.util.platformparameter.DOWNLOADS_SUPPORT
import org.oppia.android.util.platformparameter.EDIT_ACCOUNTS_OPTIONS_UI
import org.oppia.android.util.platformparameter.ENABLE_MULTIPLE_CLASSROOMS
import org.oppia.android.util.platformparameter.ENABLE_NPS_SURVEY
import org.oppia.android.util.platformparameter.ENABLE_ONBOARDING_FLOW_V2
import org.oppia.android.util.platformparameter.ENABLE_PERFORMANCE_METRICS_COLLECTION
import org.oppia.android.util.platformparameter.EXTRA_TOPIC_TABS_UI
import org.oppia.android.util.platformparameter.FAST_LANGUAGE_SWITCHING_IN_LESSON
import org.oppia.android.util.platformparameter.INTERACTION_CONFIG_CHANGE_STATE_RETENTION
import org.oppia.android.util.platformparameter.LEARNER_STUDY_ANALYTICS
import org.oppia.android.util.platformparameter.LOGGING_LEARNER_STUDY_IDS
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.platformparameter.SPOTLIGHT_UI
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
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var featureFlagsLogger: FeatureFlagsLogger
  @Inject lateinit var fakeAnalyticsEventLogger: FakeAnalyticsEventLogger

  @field:[Inject EnableTestFeatureFlag]
  lateinit var testFeatureFlag: PlatformParameterValue<Boolean>
  @field:[Inject EnableTestFeatureFlagWithEnabledDefault]
  lateinit var testFeatureFlagWithEnabledDefault: PlatformParameterValue<Boolean>

  @Parameter var index: Int = Int.MIN_VALUE
  @Parameter lateinit var flagName: String

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testLogFeatureFlags_logFeatureFlags_hasEmptyUserUuid() {
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
    featureFlagsLogger.logAllFeatureFlags(TEST_SESSION_ID)
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasFeatureFlagContextThat {
      hasSessionIdThat().isEqualTo(TEST_SESSION_ID)
    }
  }

  @Test
  fun testLogFeatureFlags_logsTestFeatureFlag_hasCorrectDefaultValues() {
    featureFlagsLogger.setFeatureFlagItemMap(
      mapOf(TEST_FEATURE_FLAG to testFeatureFlag)
    )
    featureFlagsLogger.logAllFeatureFlags(TEST_SESSION_ID)

    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasFeatureFlagContextThat {
      hasFeatureFlagItemContextThatAtIndex(0) {
        hasFeatureFlagNameThat().isEqualTo(TEST_FEATURE_FLAG)
        hasFeatureFlagEnabledStateThat().isEqualTo(false)
        hasFeatureFlagSyncStateThat().isEqualTo(SyncStatus.NOT_SYNCED_FROM_SERVER)
      }
    }
  }

  @Test
  fun testLogFeatureFlags_logsTestFeatureFlagWithEnabledDefaults_hasCorrectDefaultValues() {
    featureFlagsLogger.setFeatureFlagItemMap(
      mapOf(TEST_FEATURE_FLAG_WITH_ENABLED_DEFAULTS to testFeatureFlagWithEnabledDefault)
    )
    featureFlagsLogger.logAllFeatureFlags(TEST_SESSION_ID)

    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasFeatureFlagContextThat {
      hasFeatureFlagItemContextThatAtIndex(0) {
        hasFeatureFlagNameThat().isEqualTo(TEST_FEATURE_FLAG_WITH_ENABLED_DEFAULTS)
        hasFeatureFlagEnabledStateThat().isEqualTo(true)
        hasFeatureFlagSyncStateThat().isEqualTo(SyncStatus.SYNCED_FROM_SERVER)
      }
    }
  }

  @Test
  fun testLogFeatureFlags_correctNumberOfFeatureFlagsIsLogged() {
    val expectedFeatureFlagCount = 13

    featureFlagsLogger.logAllFeatureFlags(TEST_SESSION_ID)
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasFeatureFlagContextThat {
      hasFeatureFlagItemCountThat().isEqualTo(expectedFeatureFlagCount)
    }
  }

  @Test
  @Iteration("downloads_support", "index=0", "flagName=$DOWNLOADS_SUPPORT")
  @Iteration("extra_topic_tabs_ui", "index=1", "flagName=$EXTRA_TOPIC_TABS_UI")
  @Iteration("learner_study_analytics", "index=2", "flagName=$LEARNER_STUDY_ANALYTICS")
  @Iteration(
    "fast_language_switching_in_lesson", "index=3",
    "flagName=$FAST_LANGUAGE_SWITCHING_IN_LESSON"
  )
  @Iteration("logging_learner_study_ids", "index=4", "flagName=$LOGGING_LEARNER_STUDY_IDS")
  @Iteration("edit_accounts_options_ui", "index=5", "flagName=$EDIT_ACCOUNTS_OPTIONS_UI")
  @Iteration(
    "enable_performance_metrics_collection", "index=6",
    "flagName=$ENABLE_PERFORMANCE_METRICS_COLLECTION"
  )
  @Iteration("spotlight_ui", "index=7", "flagName=$SPOTLIGHT_UI")
  @Iteration(
    "interaction_config_change_state_retention", "index=8",
    "flagName=$INTERACTION_CONFIG_CHANGE_STATE_RETENTION"
  )
  @Iteration("app_and_os_deprecation", "index=9", "flagName=$APP_AND_OS_DEPRECATION")
  @Iteration("enable_nps_survey", "index=10", "flagName=$ENABLE_NPS_SURVEY")
  @Iteration("enable_onboarding_flow_v2", "index=11", "flagName=$ENABLE_ONBOARDING_FLOW_V2")
  @Iteration("enable_multiple_classrooms", "index=12", "flagName=$ENABLE_MULTIPLE_CLASSROOMS")
  fun testLogFeatureFlags_allFeatureFlagNamesAreLogged() {
    featureFlagsLogger.logAllFeatureFlags(TEST_SESSION_ID)

    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasFeatureFlagContextThat {
      hasFeatureFlagItemContextThatAtIndex(index) {
        hasFeatureFlagNameThat().isEqualTo(flagName)
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
      TestPlatformParameterModule::class, PlatformParameterSingletonModule::class,
      LoggingIdentifierModule::class, SyncStatusTestModule::class, AssetModule::class
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
