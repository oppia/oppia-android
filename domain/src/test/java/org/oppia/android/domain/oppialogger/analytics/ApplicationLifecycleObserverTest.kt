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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.OppiaMetricLog
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName
import org.oppia.android.domain.oppialogger.ApplicationIdSeed
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierController
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.testing.FakeEventLogger
import org.oppia.android.testing.FakePerformanceMetricsEventLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.TextInputActionTestActivity
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.logging.EventLogSubject.Companion.assertThat
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.platformparameter.ENABLE_LANGUAGE_SELECTION_UI_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.EnableLanguageSelectionUi
import org.oppia.android.util.platformparameter.EnablePerformanceMetricsCollection
import org.oppia.android.util.platformparameter.LearnerStudyAnalytics
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.platformparameter.SPLASH_SCREEN_WELCOME_MSG_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.SplashScreenWelcomeMsg
import org.oppia.android.util.platformparameter.SyncUpWorkerTimePeriodHours
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
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
  @Inject lateinit var loggingIdentifierController: LoggingIdentifierController
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var applicationLifecycleObserver: ApplicationLifecycleObserver
  @Inject lateinit var fakeOppiaClock: FakeOppiaClock
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory
  @Inject lateinit var fakeEventLogger: FakeEventLogger
  @Inject lateinit var profileManagementController: ProfileManagementController
  @Inject lateinit var performanceMetricsController: PerformanceMetricsController
  @Inject lateinit var fakePerformanceMetricsEventLogger: FakePerformanceMetricsEventLogger

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

    val eventLog = fakeEventLogger.getMostRecentEvent()
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

    val eventLog = fakeEventLogger.getMostRecentEvent()
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

    val eventLog = fakeEventLogger.getMostRecentEvent()
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

    val eventLog = fakeEventLogger.getMostRecentEvent()
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
  fun testObserver_onCreate_verifyPerformanceMetricsLoggingWithCorrectDetails() {
    setUpTestApplicationComponent()
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
  fun testObserver_onFirstActivityResume_verifyLogsStartupLatency() {
    setUpTestApplicationComponent()
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
  fun testObserver_onSecondActivityResume_verifyStartupLatencyIsLoggedOnce() {
    setUpTestApplicationComponent()
    applicationLifecycleObserver.onCreate()
    testCoroutineDispatchers.runCurrent()
    fakeOppiaClock.setCurrentTimeMs(TEST_TIMESTAMP_IN_MILLIS_TWO)
    activityRule.scenario.onActivity { activity ->
      applicationLifecycleObserver.onActivityResumed(activity)
      applicationLifecycleObserver.onActivityResumed(activity)

      val loggedEvents = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvents(
        fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
      )

      assertThat(loggedEvents.size).isEqualTo(5)
      assertThat(loggedEvents[0].loggableMetric.loggableMetricTypeCase)
        .isEqualTo(OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.APK_SIZE_METRIC)
      assertThat(loggedEvents[1].loggableMetric.loggableMetricTypeCase)
        .isEqualTo(OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.STORAGE_USAGE_METRIC)
      assertThat(loggedEvents[2].loggableMetric.loggableMetricTypeCase)
        .isEqualTo(OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.STARTUP_LATENCY_METRIC)
      assertThat(loggedEvents[3].loggableMetric.loggableMetricTypeCase)
        .isEqualTo(OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.MEMORY_USAGE_METRIC)
      assertThat(loggedEvents[4].loggableMetric.loggableMetricTypeCase)
        .isEqualTo(OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.MEMORY_USAGE_METRIC)
    }
  }

  @Test
  fun testObserver_activityResumed_verifyLogsMemoryUsage() {
    setUpTestApplicationComponent()

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
  fun testObserver_activityResumed_activityPaused_verifyCurrentScreenReturnsBackgroundValue() {
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
    setUpTestApplicationComponent()
    applicationLifecycleObserver.onAppInForeground()
    testCoroutineDispatchers.runCurrent()

    val event = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvents(
      fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    ).filter {
      it.loggableMetric.hasCpuUsageMetric()
    }

    assertThat(event).isNotEmpty()
    assertThat(event[0].currentScreen).isEqualTo(ScreenName.FOREGROUND_SCREEN)
  }

  @Test
  fun testObserver_onAppInBackground_logsCpuUsageWithCurrentScreenBackground() {
    setUpTestApplicationComponent()
    applicationLifecycleObserver.onAppInBackground()
    testCoroutineDispatchers.runCurrent()

    val event = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvents(
      fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    ).filter {
      it.loggableMetric.hasCpuUsageMetric()
    }

    assertThat(event).isNotEmpty()
    assertThat(event[0].currentScreen).isEqualTo(ScreenName.BACKGROUND_SCREEN)
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
    TestPlatformParameterModule.forceLearnerAnalyticsStudy = true
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    fakeOppiaClock.setCurrentTimeMs(TEST_TIMESTAMP_IN_MILLIS_ONE)
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

  @Module
  class TestPlatformParameterModule {
    companion object {
      var forceLearnerAnalyticsStudy: Boolean = false
    }

    @Provides
    @SplashScreenWelcomeMsg
    fun provideSplashScreenWelcomeMsgParam(): PlatformParameterValue<Boolean> {
      return PlatformParameterValue.createDefaultParameter(SPLASH_SCREEN_WELCOME_MSG_DEFAULT_VALUE)
    }

    @Provides
    @SyncUpWorkerTimePeriodHours
    fun provideSyncUpWorkerTimePeriod(): PlatformParameterValue<Int> {
      return PlatformParameterValue.createDefaultParameter(
        SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS_DEFAULT_VALUE
      )
    }

    @Provides
    @EnableLanguageSelectionUi
    fun provideEnableLanguageSelectionUi(): PlatformParameterValue<Boolean> {
      return PlatformParameterValue.createDefaultParameter(
        ENABLE_LANGUAGE_SELECTION_UI_DEFAULT_VALUE
      )
    }

    @Provides
    @LearnerStudyAnalytics
    fun provideLearnerStudyAnalytics(): PlatformParameterValue<Boolean> {
      return PlatformParameterValue.createDefaultParameter(forceLearnerAnalyticsStudy)
    }

    @Provides
    @EnablePerformanceMetricsCollection
    fun provideEnablePerformanceMetricsCollection(): PlatformParameterValue<Boolean> {
      return PlatformParameterValue.createDefaultParameter(true)
    }
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
      SyncStatusModule::class, CpuPerformanceSnapshotterModule::class,
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
