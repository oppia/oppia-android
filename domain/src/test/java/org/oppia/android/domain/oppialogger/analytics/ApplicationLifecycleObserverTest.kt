package org.oppia.android.domain.oppialogger.analytics

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwnerInitializer
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.activity.route.ActivityRouterModule
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.testing.TestingBuildFlavorModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.APP_IN_FOREGROUND_TIME
import org.oppia.android.app.model.FeatureFlagId
import org.oppia.android.app.model.LegacyProfileId
import org.oppia.android.app.model.OppiaMetricLog
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase
import org.oppia.android.app.model.ScreenName
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.testing.activity.TestActivity
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.data.backends.gae.NetworkLoggingInterceptor
import org.oppia.android.data.backends.gae.RetrofitModule
import org.oppia.android.data.backends.gae.RetrofitServiceModule
import org.oppia.android.data.backends.gae.testing.NetworkConfigTestModule
import org.oppia.android.domain.classify.InteractionsModule
import org.oppia.android.domain.classify.rules.algebraicexpressioninput.AlgebraicExpressionInputModule
import org.oppia.android.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.android.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.android.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.android.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.android.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.android.domain.classify.rules.mathequationinput.MathEquationInputModule
import org.oppia.android.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.android.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.android.domain.classify.rules.numericexpressioninput.NumericExpressionInputModule
import org.oppia.android.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.android.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.android.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.android.domain.exploration.ExplorationProgressModule
import org.oppia.android.domain.exploration.ExplorationStorageModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.testing.ExpirationMetaDataRetrieverTestModule
import org.oppia.android.domain.oppialogger.ApplicationIdSeed
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierController
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulerModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.FakeAnalyticsEventLogger
import org.oppia.android.testing.FakePerformanceMetricsEventLogger
import org.oppia.android.testing.TestImageLoaderModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.logging.EventLogSubject.Companion.assertThat
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.oppia.android.util.platformparameter.EnableDownloadsSupport
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val TEST_TIMESTAMP_IN_MILLIS_ONE = 1556094000000
private const val TEST_TIMESTAMP_IN_MILLIS_TWO = 1556094100000

/** Tests for [ApplicationLifecycleObserver]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ApplicationLifecycleObserverTest.TestApplication::class)
class ApplicationLifecycleObserverTest {
  private companion object {
    private const val TEST_TIMESTAMP_APP_IN_FOREGROUND_MILLIS = 10000L
    private const val testUrl = "/"
    private const val testApiKey = "api_key"
    private const val testApiKeyValue = "api_key_value"
    private const val testResponseBody = "{\"test\": \"test\"}"
    private const val headerString = "$testApiKey: $testApiKeyValue"
  }

  @Inject lateinit var context: Context
  @Inject lateinit var loggingIdentifierController: LoggingIdentifierController
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var applicationLifecycleObserver: ApplicationLifecycleObserver
  @Inject lateinit var applicationLifecycleLogger: ApplicationLifecycleLogger
  @Inject lateinit var fakeOppiaClock: FakeOppiaClock
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory
  @Inject lateinit var fakeAnalyticsEventLogger: FakeAnalyticsEventLogger
  @Inject lateinit var profileManagementController: ProfileManagementController
  @Inject lateinit var performanceMetricsController: PerformanceMetricsController
  @Inject lateinit var fakePerformanceMetricsEventLogger: FakePerformanceMetricsEventLogger
  @Inject lateinit var fakeConsoleLogger: ConsoleLogger
  @Inject lateinit var networkLoggingInterceptor: NetworkLoggingInterceptor
  @Inject lateinit var featureFlagsLogger: FeatureFlagsLogger
  @Inject lateinit var mockWebServer: MockWebServer

  @field:[JvmField Inject ForegroundCpuLoggingTimePeriodMillis]
  var foregroundCpuLoggingTimePeriodMillis: Long = Long.MIN_VALUE

  @field:[JvmField Inject BackgroundCpuLoggingTimePeriodMillis]
  var backgroundCpuLoggingTimePeriodMillis: Long = Long.MIN_VALUE

  @field:[Inject EnableDownloadsSupport]
  lateinit var testFeatureFlag: PlatformParameterValue<Boolean>

  private lateinit var retrofit: Retrofit
  private lateinit var client: OkHttpClient
  private lateinit var mockWebServerUrl: HttpUrl
  private lateinit var request: Request

  @After
  fun tearDown() {
    TestPlatformParameterModule.reset()
    resetProcessLifecycleOwner()
  }

  @Test
  fun testObserver_withDisabledMetricsCollection_doesNotLogAnyEvent() {
    setUpTestApplicationComponent()

    runInActivity {}

    assertThat(fakePerformanceMetricsEventLogger.noPerformanceMetricsEventsPresent()).isTrue()
  }

  @Test
  fun testObserver_getSessionId_backgroundApp_thenForeground_limitExceeded_sessionIdUpdated() {
    setUpTestApplicationComponent()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    runInActivity {
      val sessionIdProvider = loggingIdentifierController.getSessionId()
      val firstSessionId = monitorFactory.waitForNextSuccessfulResult(sessionIdProvider)

      ensureAppIsInBackground()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.MINUTES.toMillis(45))
      ensureAppIsInForeground()

      val latestSessionId = monitorFactory.waitForNextSuccessfulResult(sessionIdProvider)
      assertThat(firstSessionId).isNotEqualTo(latestSessionId)
    }
  }

  @Test
  fun testObserver_getSessionId_backgroundApp_thenForeground_limitNotExceeded_sessionIdUnchanged() {
    setUpTestApplicationComponent()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    runInActivity {
      val sessionIdProvider = loggingIdentifierController.getSessionId()
      val firstSessionId = monitorFactory.waitForNextSuccessfulResult(sessionIdProvider)

      ensureAppIsInBackground()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.MINUTES.toMillis(15))
      ensureAppIsInForeground()

      val latestSessionId = monitorFactory.waitForNextSuccessfulResult(sessionIdProvider)
      assertThat(firstSessionId).isEqualTo(latestSessionId)
    }
  }

  @Test
  fun testObserver_onAppInForeground_loggedIntoProfile_studyOn_logsForegroundEventWithBothIds() {
    setUpTestApplicationWithLearnerStudy()
    logIntoAnalyticsReadyAdminProfile()

    runInActivity {
      val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
      assertThat(eventLog).isEssentialPriority()
      assertThat(eventLog).hasAppInForegroundContextThat {
        hasLearnerIdThat().isNotEmpty()
        hasInstallationIdThat().isNotEmpty()
      }
    }
  }

  @Test
  fun testObserver_onAppInForeground_notLoggedIn_studyOn_logsForegroundEventWithoutLearnerId() {
    setUpTestApplicationWithLearnerStudy()

    runInActivity {
      val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
      assertThat(eventLog).isEssentialPriority()
      assertThat(eventLog).hasAppInForegroundContextThat {
        hasLearnerIdThat().isEmpty()
        hasInstallationIdThat().isNotEmpty()
      }
    }
  }

  @Test
  fun testObserver_onAppInBackground_loggedIntoProfile_studyOn_logsBackgroundEventWithBothIds() {
    setUpTestApplicationWithLearnerStudy()
    logIntoAnalyticsReadyAdminProfile()

    runInActivity {
      ensureAppIsInBackground()

      val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
      assertThat(eventLog).isEssentialPriority()
      assertThat(eventLog).hasAppInBackgroundContextThat {
        hasLearnerIdThat().isNotEmpty()
        hasInstallationIdThat().isNotEmpty()
      }
    }
  }

  @Test
  fun testObserver_onAppInBackground_notLoggedIn_studyOn_logsBackgroundEventWithoutLearnerId() {
    setUpTestApplicationWithLearnerStudy()

    runInActivity {
      ensureAppIsInBackground()

      val eventLog = expectAnalyticsEvent { it.context.hasAppInBackgroundContext() }
      assertThat(eventLog).isEssentialPriority()
      assertThat(eventLog).hasAppInBackgroundContextThat {
        hasLearnerIdThat().isEmpty()
        hasInstallationIdThat().isNotEmpty()
      }
    }
  }

  @Test
  fun testObserver_onAppInForeground_setsAppInForeground() {
    setUpTestApplicationComponent()

    runInActivity {
      assertThat(performanceMetricsController.getIsAppInForeground()).isTrue()
    }
  }

  @Test
  fun testObserver_onAppInBackground_setsAppInBackground() {
    setUpTestApplicationComponent()

    runInActivity {
      ensureAppIsInBackground()

      assertThat(performanceMetricsController.getIsAppInForeground()).isFalse()
    }
  }

  @Test
  fun testObserver_getCurrentScreen_verifyInitialValueIsUnspecified() {
    setUpTestApplicationComponent()

    assertThat(applicationLifecycleLogger.getCurrentScreen())
      .isEqualTo(ScreenName.SCREEN_NAME_UNSPECIFIED)
  }

  @Test
  fun testObserver_onUnspecifiedActivityResume_verifyCurrentScreenReturnsUnspecifiedValue() {
    setUpTestApplicationComponent()

    runInActivity {
      val currentScreenValue = applicationLifecycleLogger.getCurrentScreen()
      assertThat(currentScreenValue).isEqualTo(ScreenName.SCREEN_NAME_UNSPECIFIED)
    }
  }

  @Test
  fun testObserver_onCreate_performanceMetricsLoggingWithCorrectDetailsOccurs() {
    setUpTestApplicationWithPerformanceMetricsCollection()
    testCoroutineDispatchers.runCurrent()

    val loggedMetrics = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvents(2)
    assertThat(loggedMetrics[0].loggableMetric.loggableMetricTypeCase)
      .isEqualTo(LoggableMetricTypeCase.APK_SIZE_METRIC)
    assertThat(loggedMetrics[1].loggableMetric.loggableMetricTypeCase).isEqualTo(
      LoggableMetricTypeCase.STORAGE_USAGE_METRIC
    )
    assertThat(loggedMetrics[0].timestampMillis).isEqualTo(TEST_TIMESTAMP_IN_MILLIS_ONE)
    assertThat(loggedMetrics[1].timestampMillis).isEqualTo(TEST_TIMESTAMP_IN_MILLIS_ONE)
  }

  @Test
  fun testObserver_onFirstActivityResume_verifyCurrentScreenReturnsCorrectValue() {
    setUpTestApplicationComponent()

    runInActivityWithScreenName(ScreenName.POLICIES_ACTIVITY) {
      val currentScreenValue = applicationLifecycleLogger.getCurrentScreen()
      assertThat(currentScreenValue).isEqualTo(ScreenName.POLICIES_ACTIVITY)
    }
  }

  @Test
  fun testObserver_onFirstActivityResume_logsStartupLatency() {
    setUpTestApplicationWithPerformanceMetricsCollection()
    fakeOppiaClock.setCurrentTimeMs(TEST_TIMESTAMP_IN_MILLIS_TWO)

    runInActivityWithScreenName(ScreenName.HOME_ACTIVITY) {
      val latencyEvent = expectPerformanceEvent { it.loggableMetric.hasStartupLatencyMetric() }
      assertThat(latencyEvent.loggableMetric.loggableMetricTypeCase)
        .isEqualTo(LoggableMetricTypeCase.STARTUP_LATENCY_METRIC)
      assertThat(latencyEvent.timestampMillis).isEqualTo(TEST_TIMESTAMP_IN_MILLIS_TWO)
      assertThat(latencyEvent.currentScreen).isEqualTo(ScreenName.HOME_ACTIVITY)
      assertThat(latencyEvent.loggableMetric.startupLatencyMetric.startupLatencyMillis)
        .isEqualTo(TEST_TIMESTAMP_IN_MILLIS_TWO - TEST_TIMESTAMP_IN_MILLIS_ONE)
    }
  }

  @Test
  fun testObserver_onSecondActivityResume_startupLatencyIsLoggedOnce() {
    setUpTestApplicationWithPerformanceMetricsCollection()
    fakeOppiaClock.setCurrentTimeMs(TEST_TIMESTAMP_IN_MILLIS_TWO)

    // Start up an activity twice so that it's resumed twice.
    runInActivity {}
    runInActivity {}

    // The startup latency metric should only be logged once.
    val startupEvents = collectAllPerformanceEvents { it.loggableMetric.hasStartupLatencyMetric() }
    assertThat(startupEvents).hasSize(1)
  }

  @Test
  fun testObserver_activityResumed_logsMemoryUsage() {
    setUpTestApplicationWithPerformanceMetricsCollection()

    runInActivityWithScreenName(ScreenName.HOME_ACTIVITY) {
      val memoryUsageEvent = expectPerformanceEvent { it.loggableMetric.hasMemoryUsageMetric() }
      assertThat(memoryUsageEvent.loggableMetric.loggableMetricTypeCase)
        .isEqualTo(LoggableMetricTypeCase.MEMORY_USAGE_METRIC)
      assertThat(memoryUsageEvent.timestampMillis).isEqualTo(TEST_TIMESTAMP_IN_MILLIS_ONE)
      assertThat(memoryUsageEvent.currentScreen).isEqualTo(ScreenName.HOME_ACTIVITY)
    }
  }

  @Test
  fun testObserver_activityResumed_activityPaused_currentScreenReturnsBackgroundValue() {
    setUpTestApplicationComponent()

    runInActivity {
      ensureActivityIsPaused()

      // The logger should currently be tracking that the background is the current screen.
      val currentScreen = applicationLifecycleLogger.getCurrentScreen()
      assertThat(currentScreen).isEqualTo(ScreenName.BACKGROUND_SCREEN)
    }
  }

  @Test
  fun testObserver_onAppInForeground_doesNotLogCpuUsage() {
    setUpTestApplicationWithPerformanceMetricsCollection()

    runInActivity {
      val cpuUsageEvents = collectAllPerformanceEvents { it.loggableMetric.hasCpuUsageMetric() }
      assertThat(cpuUsageEvents).isEmpty()
    }
  }

  @Test
  fun testObserver_onAppInForeground_waitLongEnough_logsCpuUsageWithCurrentScreenForeground() {
    setUpTestApplicationWithPerformanceMetricsCollection()

    runInActivity {
      testCoroutineDispatchers.advanceTimeBy(foregroundCpuLoggingTimePeriodMillis)

      val cpuUsageEvent = expectPerformanceEvent { it.loggableMetric.hasCpuUsageMetric() }
      assertThat(cpuUsageEvent.currentScreen).isEqualTo(ScreenName.FOREGROUND_SCREEN)
    }
  }

  @Test
  fun testObserver_onAppInBackground_logsCpuUsageWithCurrentScreenBackground() {
    setUpTestApplicationWithPerformanceMetricsCollection()
    runInActivity {
      ensureAppIsInBackground()

      testCoroutineDispatchers.advanceTimeBy(backgroundCpuLoggingTimePeriodMillis)

      val event = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()
      assertThat(event.currentScreen).isEqualTo(ScreenName.BACKGROUND_SCREEN)
    }
  }

  @Test
  fun testObserver_onAppInForeground_logsAllFeatureFlags() {
    setUpTestApplicationComponent()
    featureFlagsLogger.setFeatureFlagItemMap(
      mapOf(FeatureFlagId.DOWNLOADS_SUPPORT to testFeatureFlag)
    )
    // TODO(#5341): Replace appSessionId generation to the modified Twitter snowflake algorithm.
    val sessionIdProvider = loggingIdentifierController.getAppSessionId()
    val sessionId = monitorFactory.waitForNextSuccessfulResult(sessionIdProvider)

    runInActivity {
      val eventLog = expectAnalyticsEvent { it.context.hasFeatureFlagListContext() }
      assertThat(eventLog).hasFeatureFlagContextThat {
        hasSessionIdThat().isEqualTo(sessionId)
        hasFeatureFlagItemContextThatAtIndex(0) {
          hasFeatureFlagIdThat().isEqualTo(FeatureFlagId.DOWNLOADS_SUPPORT)
          hasFeatureFlagEnabledStateThat().isEqualTo(false)
          hasFeatureFlagSyncStateThat().isEqualTo(SyncStatus.NOT_SYNCED_FROM_SERVER)
        }
      }
    }
  }

  @Test
  fun testObserver_onAppInForeground_thenInBackground_logsAppInForegroundTime() {
    setUpTestApplicationComponent()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    runInActivity {
      val sessionIdProvider = loggingIdentifierController.getSessionId()
      val sessionId = monitorFactory.waitForNextSuccessfulResult(sessionIdProvider)
      val installationIdProvider = loggingIdentifierController.getInstallationId()
      val installationId = monitorFactory.waitForNextSuccessfulResult(installationIdProvider)

      testCoroutineDispatchers.advanceTimeBy(TEST_TIMESTAMP_APP_IN_FOREGROUND_MILLIS)
      ensureAppIsInBackground()

      val eventLog = expectAnalyticsEvent { it.context.hasAppInForegroundTime() }
      val eventLogContext = eventLog.context
      assertThat(eventLogContext.activityContextCase).isEqualTo(APP_IN_FOREGROUND_TIME)
      // Note that this will actually potentially be bigger because of additional waiting that
      // happens for background detection.
      assertThat(eventLogContext.appInForegroundTime.foregroundTime.toLong())
        .isGreaterThan(TEST_TIMESTAMP_APP_IN_FOREGROUND_MILLIS)
      assertThat(eventLogContext.appInForegroundTime.appSessionId).isEqualTo(sessionId)
      assertThat(eventLogContext.appInForegroundTime.installationId).isEqualTo(installationId)
    }
  }

  @Test
  fun testObserver_onAppInForeground_onConsoleError_logsConsoleErrors() {
    setUpTestApplicationComponent()

    runInActivity {
      val testTag = "TestObserver"
      val testMessage = "Test error message"
      fakeConsoleLogger.e(testTag, testMessage)
      testCoroutineDispatchers.runCurrent()

      val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
      val eventLogContext = eventLog.context
      assertThat(eventLogContext.activityContextCase).isEqualTo(ActivityContextCase.CONSOLE_LOG)
      assertThat(eventLogContext.consoleLog.fullErrorLog).isEqualTo(testMessage)
      assertThat(eventLogContext.consoleLog.logLevel).isEqualTo(LogLevel.ERROR.toString())
      assertThat(eventLogContext.consoleLog.logTag).isEqualTo(testTag)
    }
  }

  @Test
  fun testObserver_onAppInForeground_onNetworkCall_logsNetworkCalls() {
    setUpTestApplicationComponent()
    setUpRetrofitApiCall()
    runInActivity {
      mockWebServer.enqueue(MockResponse().setBody(testResponseBody))
      client.newCall(request).execute()
      testCoroutineDispatchers.runCurrent()

      val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
      val eventLogContext = eventLog.context
      val retrofitCallContext = eventLogContext.retrofitCallContext
      assertThat(eventLogContext.activityContextCase)
        .isEqualTo(ActivityContextCase.RETROFIT_CALL_CONTEXT)
      assertThat(retrofitCallContext.requestUrl).isEqualTo(mockWebServerUrl.toString())
      assertThat(retrofitCallContext.responseStatusCode).isEqualTo(HttpURLConnection.HTTP_OK)
    }
  }

  @Test
  fun testObserver_onAppInForeground_onNetworkCall_logsFailedNetworkCalls() {
    setUpTestApplicationComponent()
    setUpRetrofitApiCall()
    runInActivity {
      val pageNotFound = HttpURLConnection.HTTP_NOT_FOUND
      val mockResponse = MockResponse()
        .setResponseCode(pageNotFound)
        .setBody(testResponseBody)
      mockWebServer.enqueue(mockResponse)
      client.newCall(request).execute()
      testCoroutineDispatchers.runCurrent()

      val eventLog = expectAnalyticsEvent { it.context.hasRetrofitCallFailedContext() }
      val eventLogContext = eventLog.context
      val retrofitCallFailedContext = eventLogContext.retrofitCallFailedContext
      assertThat(eventLogContext.activityContextCase)
        .isEqualTo(ActivityContextCase.RETROFIT_CALL_FAILED_CONTEXT)
      assertThat(retrofitCallFailedContext.requestUrl).isEqualTo(mockWebServerUrl.toString())
      assertThat(retrofitCallFailedContext.responseStatusCode).isEqualTo(pageNotFound)
    }
  }

  private fun logIntoAnalyticsReadyAdminProfile() {
    val rootProfileId = LegacyProfileId.getDefaultInstance()
    val addProfileProvider = profileManagementController.addProfile(
      name = "Admin",
      pin = "",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = 0,
      isAdmin = true
    )
    monitorFactory.waitForNextSuccessfulResult(addProfileProvider)
    monitorFactory.waitForNextSuccessfulResult(
      profileManagementController.loginToProfile(rootProfileId)
    )
  }

  private fun setUpTestApplicationWithLearnerStudy() {
    TestPlatformParameterModule.forceEnableLoggingLearnerStudyIds(true)
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationWithPerformanceMetricsCollection() {
    TestPlatformParameterModule.forceEnablePerformanceMetricsCollection(true)
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    fakeOppiaClock.setCurrentTimeMs(TEST_TIMESTAMP_IN_MILLIS_ONE)

    // This will be called very early in the lifecycle and must be done for the observer to work.
    initializeProcessLifecycleOwner()
    applicationLifecycleObserver.onCreateStarted()
    applicationLifecycleObserver.onCompletedInitialization()
  }

  private fun initializeProcessLifecycleOwner() {
    // Hacky way to force ProcessLifecycleOwner to initialize itself and start listening for events.
    ProcessLifecycleOwnerInitializer().also { it.attachInfo(context, /* info= */ null) }.onCreate()
  }

  private fun resetProcessLifecycleOwner() {
    // TODO(#6187): Replace this with a safer hack that doesn't require forcing non-final.
    // A *VERY* hacky way to force ProcessLifecycleOwner to reset state by recreating its internal
    // (static) singleton that would otherwise share and leak state across test boundaries. Upgrades
    // to the Java version may make the 'final' override fail, but if that happens then the
    // lifecycle package can be updated to a version that has a non-final instance (or an alternate
    // utility could be used, instead).
    val lifecycleOwnerClass = ProcessLifecycleOwner::class.java
    val constructor = lifecycleOwnerClass.getDeclaredConstructor().also { it.isAccessible = true }
    val fieldModifiers =
      Field::class.java.getDeclaredField("modifiers").also { it.isAccessible = true }
    val instanceField = lifecycleOwnerClass.getDeclaredField("sInstance").also {
      it.isAccessible = true
      fieldModifiers.set(it, it.modifiers and Modifier.FINAL.inv())
    }
    instanceField.set(null, constructor.newInstance())
  }

  private fun runInActivityWithScreenName(
    screenName: ScreenName,
    testBlock: ActivityScenario<TestActivity>.() -> Unit
  ) {
    runInActivity(
      TestActivity.createIntent(context).apply { decorateWithScreenName(screenName) }, testBlock
    )
  }

  private fun runInActivity(
    intent: Intent = TestActivity.createIntent(context),
    testBlock: ActivityScenario<TestActivity>.() -> Unit
  ) {
    ActivityScenario.launch<TestActivity>(intent).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      scenario.testBlock()
    }
  }

  private fun ActivityScenario<TestActivity>.ensureAppIsInForeground() {
    updateStateAndWait(Lifecycle.State.RESUMED)
  }

  private fun ActivityScenario<TestActivity>.ensureAppIsInBackground() {
    updateStateAndWait(Lifecycle.State.CREATED)
  }

  private fun ActivityScenario<TestActivity>.ensureActivityIsPaused() {
    updateStateAndWait(Lifecycle.State.STARTED)
  }

  private fun ActivityScenario<TestActivity>.updateStateAndWait(state: Lifecycle.State) {
    moveToState(state)

    // ProcessLifecycleOwner uses a timer before firing the background event and that must be
    // fully processed before time can be advanced (otherwise time will be advanced before the app
    // is fully in the background). Wait for a little bit for that to process. This is a timing
    // quirk with how the coroutine dispatcher time synchronization behaves.
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(1))
  }

  private fun expectAnalyticsEvent(predicate: (EventLog) -> Boolean): EventLog {
    val eventCount = fakeAnalyticsEventLogger.getEventListCount()
    val events = fakeAnalyticsEventLogger.getMostRecentEvents(eventCount)
    return events.firstOrNull(predicate) ?: error("Expected to find event.")
  }

  private fun expectPerformanceEvent(predicate: (OppiaMetricLog) -> Boolean): OppiaMetricLog =
    collectAllPerformanceEvents(predicate).firstOrNull() ?: error("Expected to find event.")

  private fun collectAllPerformanceEvents(
    predicate: (OppiaMetricLog) -> Boolean
  ): List<OppiaMetricLog> {
    val eventCount = fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    val events = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvents(eventCount)
    return events.filter(predicate)
  }

  private fun setUpRetrofitApiCall() {
    mockWebServerUrl = mockWebServer.url(testUrl)
    client = OkHttpClient.Builder()
      .addInterceptor(networkLoggingInterceptor)
      .build()
    request = Request.Builder()
      .url(mockWebServerUrl)
      .addHeader(testApiKey, testApiKeyValue)
      .build()
    retrofit = Retrofit.Builder()
      .baseUrl(mockWebServerUrl)
      .addConverterFactory(MoshiConverterFactory.create())
      .client(client)
      .build()
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
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
  class TestLoggingIdentifierModule {
    companion object {
      const val applicationIdSeed = 1L
    }

    @Provides
    @ApplicationIdSeed
    fun provideApplicationIdSeed(): Long = applicationIdSeed
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      AccessibilityTestModule::class,
      ActivityRecreatorTestModule::class,
      ActivityRouterModule::class,
      AlgebraicExpressionInputModule::class,
      ApplicationLifecycleModule::class,
      ApplicationModule::class,
      AssetModule::class,
      CachingTestModule::class,
      ContinueModule::class,
      CpuPerformanceSnapshotterModule::class,
      DeveloperOptionsModule::class,
      DeveloperOptionsStarterModule::class,
      DragDropSortInputModule::class,
      ExpirationMetaDataRetrieverTestModule::class,
      ExplorationProgressModule::class,
      ExplorationStorageModule::class,
      FakeOppiaClockModule::class,
      FractionInputModule::class,
      GcsResourceModule::class,
      HintsAndSolutionConfigModule::class,
      HintsAndSolutionProdModule::class,
      HtmlParserEntityTypeModule::class,
      ImageClickInputModule::class,
      ImageParsingModule::class,
      InteractionsModule::class,
      ItemSelectionInputModule::class,
      LocaleProdModule::class,
      LogReportWorkerModule::class,
      LogStorageModule::class,
      MathEquationInputModule::class,
      MetricLogSchedulerModule::class,
      MultipleChoiceInputModule::class,
      NetworkConfigTestModule::class,
      NetworkConnectionDebugUtilModule::class,
      NetworkConnectionUtilDebugModule::class,
      NumberWithUnitsRuleModule::class,
      NumericExpressionInputModule::class,
      NumericInputRuleModule::class,
      PlatformParameterSingletonModule::class,
      QuestionModule::class,
      RatioInputModule::class,
      RetrofitModule::class,
      RetrofitServiceModule::class,
      RobolectricModule::class,
      SplitScreenInteractionModule::class,
      SyncStatusModule::class,
      TestAuthenticationModule::class,
      TestDispatcherModule::class,
      TestImageLoaderModule::class,
      TestLogReportingModule::class,
      TestLoggingIdentifierModule::class,
      TestModule::class,
      TestPlatformParameterModule::class,
      TestingBuildFlavorModule::class,
      TextInputRuleModule::class,
      WorkManagerConfigurationModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector, ApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(applicationLifecycleObserverImplTest: ApplicationLifecycleObserverTest)
  }

  class TestApplication :
    Application(),
    DataProvidersInjectorProvider,
    ActivityComponentFactory,
    ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerApplicationLifecycleObserverTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(applicationLifecycleObserverImplTest: ApplicationLifecycleObserverTest) {
      component.inject(applicationLifecycleObserverImplTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
