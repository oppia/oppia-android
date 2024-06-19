package org.oppia.android.domain.oppialogger.analytics

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.APP_IN_FOREGROUND_TIME
import org.oppia.android.app.model.OppiaMetricLog
import org.oppia.android.app.model.PlatformParameter
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName
import org.oppia.android.data.backends.gae.NetworkLoggingInterceptor
import org.oppia.android.domain.oppialogger.ApplicationIdSeed
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierController
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.testing.FakeAnalyticsEventLogger
import org.oppia.android.testing.FakePerformanceMetricsEventLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.TextInputActionTestActivity
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.logging.EventLogSubject.Companion.assertThat
import org.oppia.android.testing.platformparameter.EnableTestFeatureFlag
import org.oppia.android.testing.platformparameter.EnableTestFeatureFlagWithEnabledDefault
import org.oppia.android.testing.platformparameter.TEST_FEATURE_FLAG
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
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
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

  @Inject
  lateinit var loggingIdentifierController: LoggingIdentifierController

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var applicationLifecycleObserver: ApplicationLifecycleObserver

  @Inject
  lateinit var fakeOppiaClock: FakeOppiaClock

  @Inject
  lateinit var monitorFactory: DataProviderTestMonitor.Factory

  @Inject
  lateinit var fakeAnalyticsEventLogger: FakeAnalyticsEventLogger

  @Inject
  lateinit var profileManagementController: ProfileManagementController

  @Inject
  lateinit var performanceMetricsController: PerformanceMetricsController

  @Inject
  lateinit var fakePerformanceMetricsEventLogger: FakePerformanceMetricsEventLogger

  @Inject
  lateinit var fakeConsoleLogger: ConsoleLogger

  @Inject
  lateinit var networkLoggingInterceptor: NetworkLoggingInterceptor

  @Inject
  lateinit var featureFlagsLogger: FeatureFlagsLogger

  @field:[JvmField Inject ForegroundCpuLoggingTimePeriodMillis]
  var foregroundCpuLoggingTimePeriodMillis: Long = Long.MIN_VALUE

  @field:[JvmField Inject BackgroundCpuLoggingTimePeriodMillis]
  var backgroundCpuLoggingTimePeriodMillis: Long = Long.MIN_VALUE

  @field:[Inject EnableTestFeatureFlag]
  lateinit var testFeatureFlag: PlatformParameterValue<Boolean>

  @field:[Inject EnableTestFeatureFlagWithEnabledDefault]
  lateinit var testFeatureFlagWithEnabledDefault: PlatformParameterValue<Boolean>

  @get:Rule
  var activityRule =
    ActivityScenarioRule<TextInputActionTestActivity>(
      TextInputActionTestActivity.createIntent(ApplicationProvider.getApplicationContext()).apply {
        decorateWithScreenName(ScreenName.HOME_ACTIVITY)
      }
    )

  @get:Rule
  var activityRuleForUnspecifiedActivity =
    ActivityScenarioRule<TextInputActionTestActivity>(
      TextInputActionTestActivity.createIntent(ApplicationProvider.getApplicationContext())
    )

  private lateinit var retrofit: Retrofit
  private lateinit var mockWebServer: MockWebServer
  private lateinit var client: OkHttpClient
  private lateinit var mockWebServerUrl: HttpUrl
  private lateinit var request: Request

  @After
  fun tearDown() {
    TestPlatformParameterModule.reset()
  }

  @Test
  fun testObserver_withDisabledMetricsCollection_doesNotLogAnyEvent() {
    setUpTestApplicationComponent()
    applicationLifecycleObserver.onAppInForeground()
    testCoroutineDispatchers.runCurrent()
    assertThat(fakePerformanceMetricsEventLogger.noPerformanceMetricsEventsPresent()).isTrue()
  }

  @Test
  fun testObserver_getSessionId_backgroundApp_thenForeground_limitExceeded_sessionIdUpdated() {
    setUpTestApplicationComponent()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    val sessionIdProvider = loggingIdentifierController.getSessionId()
    val firstSessionId = monitorFactory.waitForNextSuccessfulResult(sessionIdProvider)

    waitInBackgroundFor(TimeUnit.MINUTES.toMillis(45))

    val latestSessionId = monitorFactory.waitForNextSuccessfulResult(sessionIdProvider)
    assertThat(firstSessionId).isNotEqualTo(latestSessionId)
  }

  @Test
  fun testObserver_getSessionId_backgroundApp_thenForeground_limitNotExceeded_sessionIdUnchanged() {
    setUpTestApplicationComponent()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    val sessionIdProvider = loggingIdentifierController.getSessionId()
    val firstSessionId = monitorFactory.waitForNextSuccessfulResult(sessionIdProvider)

    waitInBackgroundFor(TimeUnit.MINUTES.toMillis(15))

    val latestSessionId = monitorFactory.waitForNextSuccessfulResult(sessionIdProvider)
    assertThat(firstSessionId).isEqualTo(latestSessionId)
  }

  @Test
  fun testObserver_onAppInForeground_loggedIntoProfile_studyOn_logsForegroundEventWithBothIds() {
    setUpTestApplicationWithLearnerStudy()
    logIntoAnalyticsReadyAdminProfile()

    applicationLifecycleObserver.onAppInForeground()
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasAppInForegroundContextThat {
      hasLearnerIdThat().isNotEmpty()
      hasInstallationIdThat().isNotEmpty()
    }
  }

  @Test
  fun testObserver_onAppInForeground_notLoggedIn_studyOn_logsForegroundEventWithoutLearnerId() {
    setUpTestApplicationWithLearnerStudy()

    applicationLifecycleObserver.onAppInForeground()
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasAppInForegroundContextThat {
      hasLearnerIdThat().isEmpty()
      hasInstallationIdThat().isNotEmpty()
    }
  }

  @Test
  fun testObserver_onAppInBackground_loggedIntoProfile_studyOn_logsBackgroundEventWithBothIds() {
    setUpTestApplicationWithLearnerStudy()
    logIntoAnalyticsReadyAdminProfile()

    applicationLifecycleObserver.onAppInBackground()
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasAppInBackgroundContextThat {
      hasLearnerIdThat().isNotEmpty()
      hasInstallationIdThat().isNotEmpty()
    }
  }

  @Test
  fun testObserver_onAppInBackground_notLoggedIn_studyOn_logsBackgroundEventWithoutLearnerId() {
    setUpTestApplicationWithLearnerStudy()

    applicationLifecycleObserver.onAppInBackground()
    testCoroutineDispatchers.runCurrent()

    val eventLog = getOneOfLastTwoEventsLogged(ActivityContextCase.APP_IN_BACKGROUND_CONTEXT)

    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasAppInBackgroundContextThat {
      hasLearnerIdThat().isEmpty()
      hasInstallationIdThat().isNotEmpty()
    }
  }

  @Test
  fun testObserver_onAppInForeground_setsAppInForeground() {
    setUpTestApplicationComponent()
    applicationLifecycleObserver.onAppInForeground()

    assertThat(performanceMetricsController.getIsAppInForeground()).isTrue()
  }

  @Test
  fun testObserver_onAppInBackground_setsAppInBackground() {
    setUpTestApplicationComponent()
    applicationLifecycleObserver.onAppInBackground()

    assertThat(performanceMetricsController.getIsAppInForeground()).isFalse()
  }

  @Test
  fun testObserver_getCurrentScreen_verifyInitialValueIsUnspecified() {
    setUpTestApplicationComponent()
    assertThat(applicationLifecycleObserver.getCurrentScreen())
      .isEqualTo(ScreenName.SCREEN_NAME_UNSPECIFIED)
  }

  @Test
  fun testObserver_onUnspecifiedActivityResume_verifyCurrentScreenReturnsUnspecifiedValue() {
    setUpTestApplicationComponent()
    activityRuleForUnspecifiedActivity.scenario.onActivity { activity ->
      applicationLifecycleObserver.onActivityResumed(activity)
      val currentScreenValue = applicationLifecycleObserver.getCurrentScreen()
      assertThat(currentScreenValue).isEqualTo(ScreenName.SCREEN_NAME_UNSPECIFIED)
    }
  }

  @Test
  fun testObserver_onCreate_performanceMetricsLoggingWithCorrectDetailsOccurs() {
    setUpTestApplicationWithPerformanceMetricsCollection()
    applicationLifecycleObserver.onCreate()
    testCoroutineDispatchers.runCurrent()

    val loggedMetrics = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvents(2)
    assertThat(loggedMetrics[0].loggableMetric.loggableMetricTypeCase)
      .isEqualTo(OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.APK_SIZE_METRIC)
    assertThat(loggedMetrics[1].loggableMetric.loggableMetricTypeCase).isEqualTo(
      OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.STORAGE_USAGE_METRIC
    )
    assertThat(loggedMetrics[0].timestampMillis).isEqualTo(TEST_TIMESTAMP_IN_MILLIS_ONE)
    assertThat(loggedMetrics[1].timestampMillis).isEqualTo(TEST_TIMESTAMP_IN_MILLIS_ONE)
  }

  @Test
  fun testObserver_onFirstActivityResume_verifyCurrentScreenReturnsCorrectValue() {
    setUpTestApplicationComponent()
    activityRule.scenario.onActivity { activity ->
      applicationLifecycleObserver.onActivityResumed(activity)
      val currentScreenValue = applicationLifecycleObserver.getCurrentScreen()
      assertThat(currentScreenValue).isEqualTo(ScreenName.HOME_ACTIVITY)
    }
  }

  @Test
  fun testObserver_onFirstActivityResume_logsStartupLatency() {
    setUpTestApplicationWithPerformanceMetricsCollection()
    applicationLifecycleObserver.onCreate()
    testCoroutineDispatchers.runCurrent()
    fakeOppiaClock.setCurrentTimeMs(TEST_TIMESTAMP_IN_MILLIS_TWO)
    activityRule.scenario.onActivity { activity ->
      val expectedStartupLatency = TEST_TIMESTAMP_IN_MILLIS_TWO - TEST_TIMESTAMP_IN_MILLIS_ONE
      applicationLifecycleObserver.onActivityResumed(activity)
      val startupLatencyEvents =
        fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvents(3)
      val startupLatencyEvent = startupLatencyEvents[1]

      assertThat(startupLatencyEvent.loggableMetric.loggableMetricTypeCase).isEqualTo(
        OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.STARTUP_LATENCY_METRIC
      )
      assertThat(startupLatencyEvent.timestampMillis).isEqualTo(TEST_TIMESTAMP_IN_MILLIS_TWO)
      assertThat(startupLatencyEvent.currentScreen).isEqualTo(ScreenName.HOME_ACTIVITY)
      assertThat(startupLatencyEvent.loggableMetric.startupLatencyMetric.startupLatencyMillis)
        .isEqualTo(expectedStartupLatency)
    }
  }

  @Test
  fun testObserver_onSecondActivityResume_startupLatencyIsLoggedOnce() {
    setUpTestApplicationWithPerformanceMetricsCollection()

    applicationLifecycleObserver.onCreate()
    testCoroutineDispatchers.runCurrent()
    fakeOppiaClock.setCurrentTimeMs(TEST_TIMESTAMP_IN_MILLIS_TWO)
    activityRule.scenario.onActivity { activity ->
      applicationLifecycleObserver.onActivityResumed(activity)
      applicationLifecycleObserver.onActivityResumed(activity)

      val loggedStartupLatencyEvents =
        fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvents(
          fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
        ).filter {
          it.loggableMetric.hasStartupLatencyMetric()
        }

      assertThat(loggedStartupLatencyEvents.size).isEqualTo(1)
    }
  }

  @Test
  fun testObserver_activityResumed_logsMemoryUsage() {
    setUpTestApplicationWithPerformanceMetricsCollection()

    activityRule.scenario.onActivity { activity ->
      applicationLifecycleObserver.onActivityResumed(activity)

      val memoryUsageEvent =
        fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()

      assertThat(memoryUsageEvent.loggableMetric.loggableMetricTypeCase).isEqualTo(
        OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.MEMORY_USAGE_METRIC
      )
      assertThat(memoryUsageEvent.timestampMillis).isEqualTo(TEST_TIMESTAMP_IN_MILLIS_ONE)
      assertThat(memoryUsageEvent.currentScreen).isEqualTo(ScreenName.HOME_ACTIVITY)
    }
  }

  @Test
  fun testObserver_activityResumed_activityPaused_currentScreenReturnsBackgroundValue() {
    setUpTestApplicationComponent()
    activityRule.scenario.onActivity { activity ->
      applicationLifecycleObserver.onActivityResumed(activity)
      applicationLifecycleObserver.onActivityPaused(activity)
      val currentScreen = applicationLifecycleObserver.getCurrentScreen()

      assertThat(currentScreen).isEqualTo(ScreenName.BACKGROUND_SCREEN)
    }
  }

  @Test
  fun testObserver_onAppInForeground_logsCpuUsageWithCurrentScreenForeground() {
    setUpTestApplicationWithPerformanceMetricsCollection()
    applicationLifecycleObserver.onCreate()
    applicationLifecycleObserver.onAppInForeground()
    testCoroutineDispatchers.runCurrent()
    testCoroutineDispatchers.advanceTimeBy(foregroundCpuLoggingTimePeriodMillis)

    val event = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()

    assertThat(event.currentScreen).isEqualTo(ScreenName.FOREGROUND_SCREEN)
  }

  @Test
  fun testObserver_onAppInBackground_logsCpuUsageWithCurrentScreenBackground() {
    setUpTestApplicationWithPerformanceMetricsCollection()
    applicationLifecycleObserver.onCreate()
    applicationLifecycleObserver.onAppInBackground()
    testCoroutineDispatchers.runCurrent()
    testCoroutineDispatchers.advanceTimeBy(backgroundCpuLoggingTimePeriodMillis)

    val event = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()

    assertThat(event.currentScreen).isEqualTo(ScreenName.BACKGROUND_SCREEN)
  }

  @Test
  fun testObserver_onAppInForeground_logsAllFeatureFlags() {
    setUpTestApplicationComponent()

    featureFlagsLogger.setFeatureFlagItemMap(
      mapOf(TEST_FEATURE_FLAG to testFeatureFlag)
    )

    // TODO(#5341): Replace appSessionId generation to the modified Twitter snowflake algorithm.
    val sessionIdProvider = loggingIdentifierController.getAppSessionId()
    val sessionId = monitorFactory.waitForNextSuccessfulResult(sessionIdProvider)

    applicationLifecycleObserver.onCreate()
    testCoroutineDispatchers.runCurrent()
    testCoroutineDispatchers.advanceTimeBy(foregroundCpuLoggingTimePeriodMillis)

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()

    assertThat(eventLog).hasFeatureFlagContextThat {
      hasSessionIdThat().isEqualTo(sessionId)
      hasFeatureFlagItemContextThatAtIndex(0) {
        hasFeatureFlagNameThat().isEqualTo(TEST_FEATURE_FLAG)
        hasFeatureFlagEnabledStateThat().isEqualTo(false)
        hasFeatureFlagSyncStateThat().isEqualTo(PlatformParameter.SyncStatus.NOT_SYNCED_FROM_SERVER)
      }
    }
  }

  @Test
  fun testObserver_onAppInForeground_thenInBackground_logsAppInForegroundTime() {
    setUpTestApplicationComponent()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)

    applicationLifecycleObserver.onCreate()
    applicationLifecycleObserver.onAppInForeground()
    testCoroutineDispatchers.runCurrent()

    val sessionIdProvider = loggingIdentifierController.getSessionId()
    val sessionId = monitorFactory.waitForNextSuccessfulResult(sessionIdProvider)
    val installationIdProvider = loggingIdentifierController.getInstallationId()
    val installationId = monitorFactory.waitForNextSuccessfulResult(installationIdProvider)

    testCoroutineDispatchers.advanceTimeBy(TEST_TIMESTAMP_APP_IN_FOREGROUND_MILLIS)
    applicationLifecycleObserver.onAppInBackground()
    testCoroutineDispatchers.runCurrent()

    val eventLog = getOneOfLastTwoEventsLogged(APP_IN_FOREGROUND_TIME)
    val eventLogContext = eventLog.context

    assertThat(eventLogContext.activityContextCase)
      .isEqualTo(APP_IN_FOREGROUND_TIME)
    assertThat(eventLogContext.appInForegroundTime.foregroundTime.toLong())
      .isEqualTo(TEST_TIMESTAMP_APP_IN_FOREGROUND_MILLIS)
    assertThat(eventLogContext.appInForegroundTime.appSessionId).isEqualTo(sessionId)
    assertThat(eventLogContext.appInForegroundTime.installationId).isEqualTo(installationId)
  }

  @Test
  fun testObserver_onAppInForeground_onConsoleError_logsConsoleErrors() {
    setUpTestApplicationComponent()

    applicationLifecycleObserver.onCreate()
    applicationLifecycleObserver.onAppInForeground()
    testCoroutineDispatchers.runCurrent()

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

  @Test
  fun testObserver_onAppInForeground_onNetworkCall_logsNetworkCalls() {
    setUpTestApplicationComponent()
    setUpRetrofitApiCall()

    applicationLifecycleObserver.onCreate()
    applicationLifecycleObserver.onAppInForeground()
    testCoroutineDispatchers.runCurrent()

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
    assertThat(retrofitCallContext.headers).contains(headerString)
    assertThat(retrofitCallContext.body).isEqualTo(testResponseBody)
  }

  @Test
  fun testObserver_onAppInForeground_onNetworkCall_logsFailedNetworkCalls() {
    setUpTestApplicationComponent()
    setUpRetrofitApiCall()

    applicationLifecycleObserver.onCreate()
    applicationLifecycleObserver.onAppInForeground()
    testCoroutineDispatchers.runCurrent()

    val pageNotFound = HttpURLConnection.HTTP_NOT_FOUND
    val mockResponse = MockResponse()
      .setResponseCode(pageNotFound)
      .setBody(testResponseBody)

    mockWebServer.enqueue(mockResponse)
    client.newCall(request).execute()
    testCoroutineDispatchers.runCurrent()

    val eventLog = getOneOfLastTwoEventsLogged(ActivityContextCase.RETROFIT_CALL_FAILED_CONTEXT)
    val eventLogContext = eventLog.context
    val retrofitCallFailedContext = eventLogContext.retrofitCallFailedContext

    assertThat(eventLogContext.activityContextCase)
      .isEqualTo(ActivityContextCase.RETROFIT_CALL_FAILED_CONTEXT)
    assertThat(retrofitCallFailedContext.requestUrl).isEqualTo(mockWebServerUrl.toString())
    assertThat(retrofitCallFailedContext.responseStatusCode).isEqualTo(pageNotFound)
  }

  private fun waitInBackgroundFor(millis: Long) {
    applicationLifecycleObserver.onAppInBackground()
    testCoroutineDispatchers.runCurrent()
    fakeOppiaClock.setCurrentTimeMs(fakeOppiaClock.getCurrentTimeMs() + millis)
    testCoroutineDispatchers.advanceTimeBy(millis)

    applicationLifecycleObserver.onAppInForeground()
    testCoroutineDispatchers.runCurrent()
  }

  private fun logIntoAnalyticsReadyAdminProfile() {
    val rootProfileId = ProfileId.getDefaultInstance()
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
  }

  private fun setUpRetrofitApiCall() {
    mockWebServer = MockWebServer()
    client = OkHttpClient.Builder()
      .addInterceptor(networkLoggingInterceptor)
      .build()

    mockWebServerUrl = mockWebServer.url(testUrl)

    request = Request.Builder()
      .url(mockWebServerUrl)
      .addHeader(testApiKey, testApiKeyValue)
      .build()

    // Use retrofit with the MockWebServer here instead of MockRetrofit so that we can verify that
    // the full network request properly executes. MockRetrofit and MockWebServer perform the same
    // request mocking in different ways and we want to verify the full request is executed here.
    // See https://github.com/square/retrofit/issues/2340#issuecomment-302856504 for more context.
    retrofit = Retrofit.Builder()
      .baseUrl(mockWebServerUrl)
      .addConverterFactory(MoshiConverterFactory.create())
      .client(client)
      .build()
  }

  private fun getOneOfLastTwoEventsLogged(
    wantedContext: ActivityContextCase
  ): EventLog {
    val events = fakeAnalyticsEventLogger.getMostRecentEvents(2)
    return if (events[0].context.activityContextCase == wantedContext) events[0] else events[1]
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
      TestModule::class, TestLogReportingModule::class, LogStorageModule::class,
      TestDispatcherModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      NetworkConnectionUtilDebugModule::class, LocaleProdModule::class,
      TestPlatformParameterModule::class, PlatformParameterSingletonModule::class,
      TestLoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, CpuPerformanceSnapshotterModule::class, AssetModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(applicationLifecycleObserverImplTest: ApplicationLifecycleObserverTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerApplicationLifecycleObserverTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(applicationLifecycleObserverImplTest: ApplicationLifecycleObserverTest) {
      component.inject(applicationLifecycleObserverImplTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
