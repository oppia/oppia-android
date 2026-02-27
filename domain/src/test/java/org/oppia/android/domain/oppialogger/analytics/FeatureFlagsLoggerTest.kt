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
import org.oppia.android.app.model.FeatureFlagId
import org.oppia.android.app.model.SyncStatus
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
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
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
import org.oppia.android.util.platformparameter.EnableDownloadsSupport
import org.oppia.android.util.platformparameter.EnableNpsSurvey
import org.oppia.android.util.platformparameter.PlatformParameterValue
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
  @Inject lateinit var fakeOppiaClock: FakeOppiaClock

  @field:[Inject EnableDownloadsSupport]
  lateinit var testFeatureFlag: PlatformParameterValue<Boolean>
  @field:[Inject EnableNpsSurvey]
  lateinit var testFeatureFlagWithEnabledDefault: PlatformParameterValue<Boolean>

  @Parameter var index: Int = Int.MIN_VALUE
  @Parameter lateinit var flagId: String

  private val flagIdEnum get() = FeatureFlagId.valueOf(flagId)

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testLogFeatureFlags_logFeatureFlags_hasEmptyUserUuid() {
    // TODO(#5341): The user UUID is not set in this test context and is expected to be empty.
    featureFlagsLogger.logAllFeatureFlags(TEST_SESSION_ID, fakeOppiaClock.getCurrentTimeMs())
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasFeatureFlagContextThat {
      hasUniqueUserUuidThat().isEmpty()
    }
  }

  @Test
  fun testLogFeatureFlags_logFeatureFlags_hasCorrectSessionId() {
    featureFlagsLogger.logAllFeatureFlags(TEST_SESSION_ID, fakeOppiaClock.getCurrentTimeMs())
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasFeatureFlagContextThat {
      hasSessionIdThat().isEqualTo(TEST_SESSION_ID)
    }
  }

  @Test
  fun testLogFeatureFlags_logsTestFeatureFlag_hasCorrectDefaultValues() {
    featureFlagsLogger.setFeatureFlagItemMap(
      mapOf(FeatureFlagId.DOWNLOADS_SUPPORT to testFeatureFlag)
    )
    featureFlagsLogger.logAllFeatureFlags(TEST_SESSION_ID, fakeOppiaClock.getCurrentTimeMs())

    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasFeatureFlagContextThat {
      hasFeatureFlagItemContextThatAtIndex(0) {
        hasFeatureFlagIdThat().isEqualTo(FeatureFlagId.DOWNLOADS_SUPPORT)
        hasFeatureFlagEnabledStateThat().isEqualTo(false)
        hasFeatureFlagSyncStateThat().isEqualTo(SyncStatus.NOT_SYNCED_FROM_SERVER)
      }
    }
  }

  @Test
  fun testLogFeatureFlags_logsTestFeatureFlagWithEnabledDefaults_hasCorrectDefaultValues() {
    featureFlagsLogger.setFeatureFlagItemMap(
      mapOf(FeatureFlagId.NPS_SURVEY to testFeatureFlagWithEnabledDefault)
    )
    featureFlagsLogger.logAllFeatureFlags(TEST_SESSION_ID, fakeOppiaClock.getCurrentTimeMs())

    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasFeatureFlagContextThat {
      hasFeatureFlagItemContextThatAtIndex(0) {
        hasFeatureFlagIdThat().isEqualTo(FeatureFlagId.NPS_SURVEY)
        hasFeatureFlagEnabledStateThat().isEqualTo(true)
        hasFeatureFlagSyncStateThat().isEqualTo(SyncStatus.NOT_SYNCED_FROM_SERVER)
      }
    }
  }

  @Test
  fun testLogFeatureFlags_correctNumberOfFeatureFlagsIsLogged() {
    val expectedFeatureFlagCount = 15

    featureFlagsLogger.logAllFeatureFlags(TEST_SESSION_ID, fakeOppiaClock.getCurrentTimeMs())
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasFeatureFlagContextThat {
      hasFeatureFlagItemCountThat().isEqualTo(expectedFeatureFlagCount)
    }
  }

  @Test
  @Iteration("learner_study_analytics", "index=0", "flagId=LEARNER_STUDY_ANALYTICS")
  @Iteration("performance_metrics_collection", "index=1", "flagId=PERFORMANCE_METRICS_COLLECTION")
  @Iteration("edit_accounts_options_ui", "index=2", "flagId=EDIT_ACCOUNTS_OPTIONS_UI")
  @Iteration("spotlight_ui", "index=3", "flagId=SPOTLIGHT_UI")
  @Iteration("downloads_support", "index=4", "flagId=DOWNLOADS_SUPPORT")
  @Iteration("config_change_state", "index=5", "flagId=INTERACTION_CONFIG_CHANGE_STATE_RETENTION")
  @Iteration("app_and_os_deprecation", "index=6", "flagId=APP_AND_OS_DEPRECATION")
  @Iteration("fast_lang_switching_in_lesson", "index=7", "flagId=FAST_LANGUAGE_SWITCHING_IN_LESSON")
  @Iteration("logging_learner_study_ids", "index=8", "flagId=LOGGING_LEARNER_STUDY_IDS")
  @Iteration("nps_survey", "index=9", "flagId=NPS_SURVEY")
  @Iteration("onboarding_flow_v2", "index=10", "flagId=ONBOARDING_FLOW_V2")
  @Iteration("multiple_classrooms", "index=11", "flagId=MULTIPLE_CLASSROOMS")
  @Iteration("flashback_support", "index=12", "flagId=FLASHBACK_SUPPORT")
  @Iteration("topic_info_tab", "index=13", "flagId=TOPIC_INFO_TAB")
  @Iteration("topic_practice_tab", "index=14", "flagId=TOPIC_PRACTICE_TAB")
  fun testLogFeatureFlags_allFeatureFlagIdsAreLogged() {
    featureFlagsLogger.logAllFeatureFlags(TEST_SESSION_ID, fakeOppiaClock.getCurrentTimeMs())

    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasFeatureFlagContextThat {
      hasFeatureFlagItemContextThatAtIndex(index) {
        hasFeatureFlagIdThat().isEqualTo(flagIdEnum)
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
      AssetModule::class,
      FakeOppiaClockModule::class,
      LocaleProdModule::class,
      LoggingIdentifierModule::class,
      NetworkConnectionUtilDebugModule::class,
      PlatformParameterSingletonModule::class,
      RobolectricModule::class,
      SyncStatusTestModule::class,
      TestDispatcherModule::class,
      TestLogReportingModule::class,
      TestLogStorageModule::class,
      TestModule::class,
      TestPlatformParameterModule::class
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
