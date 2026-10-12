package org.oppia.android.domain.oppialogger.analytics

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.After
import org.junit.Assert.assertThrows
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
import org.oppia.android.app.model.FeatureFlagId
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase
import org.oppia.android.app.model.ScreenName
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
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
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
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
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tests for [ApplicationLifecycleLogger].
 *
 * These tests call the logger's public methods directly instead of going through
 * [ApplicationLifecycleObserver] and a real Activity, since the branching logic below
 * (session id updates, startup latency being logged once, CPU snapshotting toggles, etc.)
 * belongs to the logger itself.
 */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ApplicationLifecycleLoggerTest.TestApplication::class)
class ApplicationLifecycleLoggerTest {
  private companion object {
    private const val TEST_TIMESTAMP_IN_MILLIS_ONE = 1556094000000
    private const val TEST_TIMESTAMP_IN_MILLIS_TWO = 1556094100000
    private const val TEST_APP_START_TIME_MILLIS = 1556093900000
  }

  @Inject lateinit var context: Context
  @Inject lateinit var loggingIdentifierController: LoggingIdentifierController
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var applicationLifecycleLogger: ApplicationLifecycleLogger
  @Inject lateinit var fakeOppiaClock: FakeOppiaClock
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory
  @Inject lateinit var fakeAnalyticsEventLogger: FakeAnalyticsEventLogger
  @Inject lateinit var fakePerformanceMetricsEventLogger: FakePerformanceMetricsEventLogger
  @Inject lateinit var featureFlagsLogger: FeatureFlagsLogger

  @field:[JvmField Inject ForegroundCpuLoggingTimePeriodMillis]
  var foregroundCpuLoggingTimePeriodMillis: Long = Long.MIN_VALUE

  @field:[JvmField Inject BackgroundCpuLoggingTimePeriodMillis]
  var backgroundCpuLoggingTimePeriodMillis: Long = Long.MIN_VALUE

  @field:[Inject EnableDownloadsSupport]
  lateinit var testFeatureFlag: PlatformParameterValue<Boolean>

  @After
  fun tearDown() {
    TestPlatformParameterModule.reset()
  }

  // ---------------------------------------------------------------------------------------
  // recordAppOpened
  // ---------------------------------------------------------------------------------------

  @Test
  fun testRecordAppOpened_logsApkSizeAndStorageUsage() {
    setUpTestApplicationComponent()

    applicationLifecycleLogger.recordAppOpened(TEST_APP_START_TIME_MILLIS)
    testCoroutineDispatchers.runCurrent()

    val loggedMetrics = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvents(2)
    assertThat(loggedMetrics[0].loggableMetric.loggableMetricTypeCase)
      .isEqualTo(LoggableMetricTypeCase.APK_SIZE_METRIC)
    assertThat(loggedMetrics[1].loggableMetric.loggableMetricTypeCase)
      .isEqualTo(LoggableMetricTypeCase.STORAGE_USAGE_METRIC)
  }

  @Test
  fun testRecordAppOpened_logsAllFeatureFlags() {
    setUpTestApplicationComponent()
    featureFlagsLogger.setFeatureFlagItemMap(
      mapOf(FeatureFlagId.DOWNLOADS_SUPPORT to testFeatureFlag)
    )
    val sessionIdProvider = loggingIdentifierController.getAppSessionId()
    val sessionId = monitorFactory.waitForNextSuccessfulResult(sessionIdProvider)

    applicationLifecycleLogger.recordAppOpened(TEST_APP_START_TIME_MILLIS)
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasFeatureFlagContextThat {
      hasSessionIdThat().isEqualTo(sessionId)
      hasFeatureFlagItemContextThatAtIndex(0) {
        hasFeatureFlagIdThat().isEqualTo(FeatureFlagId.DOWNLOADS_SUPPORT)
        hasFeatureFlagSyncStateThat().isEqualTo(SyncStatus.NOT_SYNCED_FROM_SERVER)
      }
    }
  }

  @Test
  fun testRecordAppOpened_performanceMetricsDisabled_doesNotLogCpuUsage() {
    TestPlatformParameterModule.forceEnablePerformanceMetricsCollection(false)
    setUpTestApplicationComponent()

    applicationLifecycleLogger.recordAppOpened(TEST_APP_START_TIME_MILLIS)
    testCoroutineDispatchers.runCurrent()

    val cpuEvents = getLoggedCpuUsageEvents()
    assertThat(cpuEvents).isEmpty()
  }

  // ---------------------------------------------------------------------------------------
  // recordAppInForeground
  // ---------------------------------------------------------------------------------------

  @Test
  fun testRecordAppInForeground_backgroundApp_limitExceeded_updatesSessionId() {
    setUpTestApplicationComponent()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)

    applicationLifecycleLogger.recordAppInForeground(TEST_TIMESTAMP_IN_MILLIS_ONE)
    val sessionIdProvider = loggingIdentifierController.getSessionId()
    val firstSessionId = monitorFactory.waitForNextSuccessfulResult(sessionIdProvider)

    applicationLifecycleLogger.recordAppInBackground(TEST_TIMESTAMP_IN_MILLIS_ONE)
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.MINUTES.toMillis(45))
    applicationLifecycleLogger.recordAppInForeground(
      TEST_TIMESTAMP_IN_MILLIS_ONE + TimeUnit.MINUTES.toMillis(45)
    )

    val latestSessionId = monitorFactory.waitForNextSuccessfulResult(sessionIdProvider)
    assertThat(firstSessionId).isNotEqualTo(latestSessionId)
  }

  @Test
  fun testRecordAppInForeground_backgroundApp_limitNotExceeded_sessionIdUnchanged() {
    setUpTestApplicationComponent()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)

    applicationLifecycleLogger.recordAppInForeground(TEST_TIMESTAMP_IN_MILLIS_ONE)
    val sessionIdProvider = loggingIdentifierController.getSessionId()
    val firstSessionId = monitorFactory.waitForNextSuccessfulResult(sessionIdProvider)

    applicationLifecycleLogger.recordAppInBackground(TEST_TIMESTAMP_IN_MILLIS_ONE)
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.MINUTES.toMillis(15))
    applicationLifecycleLogger.recordAppInForeground(
      TEST_TIMESTAMP_IN_MILLIS_ONE + TimeUnit.MINUTES.toMillis(15)
    )

    val latestSessionId = monitorFactory.waitForNextSuccessfulResult(sessionIdProvider)
    assertThat(firstSessionId).isEqualTo(latestSessionId)
  }

  @Test
  fun testRecordAppInForeground_logsForegroundEvent() {
    setUpTestApplicationComponent()

    applicationLifecycleLogger.recordAppInForeground(TEST_TIMESTAMP_IN_MILLIS_ONE)
    testCoroutineDispatchers.runCurrent()

    val eventLog = expectAnalyticsEvent { it.context.hasAppInForegroundContext() }
    assertThat(eventLog.context.hasAppInForegroundContext()).isTrue()
  }

  @Test
  fun testRecordAppInForeground_performanceMetricsEnabled_logsCpuUsage() {
    TestPlatformParameterModule.forceEnablePerformanceMetricsCollection(true)
    setUpTestApplicationComponent()
    // The CPU snapshotter is initialized when the app is opened.
    applicationLifecycleLogger.recordAppOpened(TEST_APP_START_TIME_MILLIS)
    testCoroutineDispatchers.runCurrent()

    applicationLifecycleLogger.recordAppInForeground(TEST_TIMESTAMP_IN_MILLIS_ONE)
    // The snapshotter handles iconification changes through a command queue, so let it process
    // the change before advancing time to the point where CPU usage is logged.
    testCoroutineDispatchers.runCurrent()
    testCoroutineDispatchers.advanceTimeBy(foregroundCpuLoggingTimePeriodMillis)

    val cpuEvents = getLoggedCpuUsageEvents()
    assertThat(cpuEvents).isNotEmpty()
    assertThat(cpuEvents.last().currentScreen).isEqualTo(ScreenName.FOREGROUND_SCREEN)
  }

  @Test
  fun testRecordAppInForeground_performanceMetricsDisabled_doesNotLogCpuUsage() {
    TestPlatformParameterModule.forceEnablePerformanceMetricsCollection(false)
    setUpTestApplicationComponent()

    applicationLifecycleLogger.recordAppInForeground(TEST_TIMESTAMP_IN_MILLIS_ONE)
    testCoroutineDispatchers.advanceTimeBy(foregroundCpuLoggingTimePeriodMillis)

    assertThat(getLoggedCpuUsageEvents()).isEmpty()
  }

  // ---------------------------------------------------------------------------------------
  // recordAppInBackground
  // ---------------------------------------------------------------------------------------

  @Test
  fun testRecordAppInBackground_logsBackgroundEvent() {
    setUpTestApplicationComponent()
    applicationLifecycleLogger.recordAppInForeground(TEST_TIMESTAMP_IN_MILLIS_ONE)

    applicationLifecycleLogger.recordAppInBackground(TEST_TIMESTAMP_IN_MILLIS_TWO)
    testCoroutineDispatchers.runCurrent()

    val eventLog = expectAnalyticsEvent { it.context.hasAppInBackgroundContext() }
    assertThat(eventLog.context.hasAppInBackgroundContext()).isTrue()
  }

  @Test
  fun testRecordAppInBackground_logsForegroundTimeSpent() {
    setUpTestApplicationComponent()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    applicationLifecycleLogger.recordAppInForeground(TEST_TIMESTAMP_IN_MILLIS_ONE)

    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
    applicationLifecycleLogger.recordAppInBackground(
      TEST_TIMESTAMP_IN_MILLIS_ONE + TimeUnit.SECONDS.toMillis(10)
    )
    testCoroutineDispatchers.runCurrent()

    val eventLog = expectAnalyticsEvent { it.context.hasAppInForegroundTime() }
    assertThat(eventLog.context.hasAppInForegroundTime()).isTrue()
    // The foreground time is in milliseconds (ApplicationLifecycleObserverTest compares it against
    // a millisecond value).
    assertThat(eventLog.context.appInForegroundTime.foregroundTime.toLong())
      .isEqualTo(TimeUnit.SECONDS.toMillis(10))
  }

  @Test
  fun testRecordAppInBackground_performanceMetricsEnabled_logsCpuUsage() {
    TestPlatformParameterModule.forceEnablePerformanceMetricsCollection(true)
    setUpTestApplicationComponent()
    // The CPU snapshotter is initialized when the app is opened.
    applicationLifecycleLogger.recordAppOpened(TEST_APP_START_TIME_MILLIS)
    applicationLifecycleLogger.recordAppInForeground(TEST_TIMESTAMP_IN_MILLIS_ONE)

    applicationLifecycleLogger.recordAppInBackground(TEST_TIMESTAMP_IN_MILLIS_TWO)
    testCoroutineDispatchers.advanceTimeBy(backgroundCpuLoggingTimePeriodMillis)

    assertThat(getLoggedCpuUsageEvents()).isNotEmpty()
  }

  // ---------------------------------------------------------------------------------------
  // Invalid lifecycle sequences
  // ---------------------------------------------------------------------------------------

  @Test
  fun testRecordAppInBackground_withoutForeground_throwsException() {
    setUpTestApplicationComponent()

    val exception = assertThrows(IllegalStateException::class.java) {
      applicationLifecycleLogger.recordAppInBackground(TEST_TIMESTAMP_IN_MILLIS_ONE)
    }

    assertThat(exception).hasMessageThat().contains("already thought to be in the background")
  }

  @Test
  fun testRecordAppInForeground_whenAlreadyInForeground_throwsException() {
    setUpTestApplicationComponent()
    applicationLifecycleLogger.recordAppInForeground(TEST_TIMESTAMP_IN_MILLIS_ONE)

    val exception = assertThrows(IllegalStateException::class.java) {
      applicationLifecycleLogger.recordAppInForeground(TEST_TIMESTAMP_IN_MILLIS_TWO)
    }

    assertThat(exception).hasMessageThat().contains("already thought to be in the foreground")
  }

  @Test
  fun testRecordAppInBackground_whenAlreadyInBackground_throwsException() {
    setUpTestApplicationComponent()
    applicationLifecycleLogger.recordAppInForeground(TEST_TIMESTAMP_IN_MILLIS_ONE)
    applicationLifecycleLogger.recordAppInBackground(TEST_TIMESTAMP_IN_MILLIS_TWO)

    val exception = assertThrows(IllegalStateException::class.java) {
      applicationLifecycleLogger.recordAppInBackground(TEST_TIMESTAMP_IN_MILLIS_TWO + 1)
    }

    assertThat(exception).hasMessageThat().contains("already thought to be in the background")
  }

  // ---------------------------------------------------------------------------------------
  // recordActivityResumed / recordActivityPaused / getCurrentScreen
  // ---------------------------------------------------------------------------------------

  @Test
  fun testGetCurrentScreen_initialValue_isUnspecified() {
    setUpTestApplicationComponent()

    assertThat(applicationLifecycleLogger.getCurrentScreen())
      .isEqualTo(ScreenName.SCREEN_NAME_UNSPECIFIED)
  }

  @Test
  fun testRecordActivityResumed_updatesCurrentScreen() {
    setUpTestApplicationComponent()

    applicationLifecycleLogger.recordActivityResumed(
      ScreenName.HOME_ACTIVITY, TEST_APP_START_TIME_MILLIS, TEST_TIMESTAMP_IN_MILLIS_ONE
    )

    assertThat(applicationLifecycleLogger.getCurrentScreen()).isEqualTo(ScreenName.HOME_ACTIVITY)
  }

  @Test
  fun testRecordActivityResumed_firstCall_logsStartupLatency() {
    setUpTestApplicationComponent()

    applicationLifecycleLogger.recordActivityResumed(
      ScreenName.HOME_ACTIVITY, TEST_APP_START_TIME_MILLIS, TEST_TIMESTAMP_IN_MILLIS_ONE
    )
    testCoroutineDispatchers.runCurrent()

    val latencyEvents = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvents(
      fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    ).filter { it.loggableMetric.hasStartupLatencyMetric() }
    assertThat(latencyEvents).hasSize(1)
    assertThat(latencyEvents[0].loggableMetric.startupLatencyMetric.startupLatencyMillis)
      .isEqualTo(TEST_TIMESTAMP_IN_MILLIS_ONE - TEST_APP_START_TIME_MILLIS)
  }

  @Test
  fun testRecordActivityResumed_secondCall_doesNotLogStartupLatencyAgain() {
    setUpTestApplicationComponent()

    applicationLifecycleLogger.recordActivityResumed(
      ScreenName.HOME_ACTIVITY, TEST_APP_START_TIME_MILLIS, TEST_TIMESTAMP_IN_MILLIS_ONE
    )
    applicationLifecycleLogger.recordActivityResumed(
      ScreenName.HOME_ACTIVITY, TEST_APP_START_TIME_MILLIS, TEST_TIMESTAMP_IN_MILLIS_TWO
    )
    testCoroutineDispatchers.runCurrent()

    val latencyEvents = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvents(
      fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    ).filter { it.loggableMetric.hasStartupLatencyMetric() }
    assertThat(latencyEvents).hasSize(1)
  }

  @Test
  fun testRecordActivityResumed_logsMemoryUsageEveryCall() {
    setUpTestApplicationComponent()

    applicationLifecycleLogger.recordActivityResumed(
      ScreenName.HOME_ACTIVITY, TEST_APP_START_TIME_MILLIS, TEST_TIMESTAMP_IN_MILLIS_ONE
    )
    applicationLifecycleLogger.recordActivityResumed(
      ScreenName.HOME_ACTIVITY, TEST_APP_START_TIME_MILLIS, TEST_TIMESTAMP_IN_MILLIS_TWO
    )
    testCoroutineDispatchers.runCurrent()

    val memoryEvents = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvents(
      fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    ).filter { it.loggableMetric.hasMemoryUsageMetric() }
    assertThat(memoryEvents).hasSize(2)
  }

  @Test
  fun testRecordActivityPaused_setsCurrentScreenToBackground() {
    setUpTestApplicationComponent()
    applicationLifecycleLogger.recordActivityResumed(
      ScreenName.HOME_ACTIVITY, TEST_APP_START_TIME_MILLIS, TEST_TIMESTAMP_IN_MILLIS_ONE
    )

    applicationLifecycleLogger.recordActivityPaused()

    assertThat(applicationLifecycleLogger.getCurrentScreen())
      .isEqualTo(ScreenName.BACKGROUND_SCREEN)
  }

  // ---------------------------------------------------------------------------------------
  // Test setup boilerplate
  // ---------------------------------------------------------------------------------------

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    fakeOppiaClock.setCurrentTimeMs(TEST_TIMESTAMP_IN_MILLIS_ONE)
  }

  private fun getLoggedCpuUsageEvents() =
    fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvents(
      fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    ).filter { it.loggableMetric.hasCpuUsageMetric() }

  private fun expectAnalyticsEvent(predicate: (EventLog) -> Boolean): EventLog {
    val eventCount = fakeAnalyticsEventLogger.getEventListCount()
    val events = fakeAnalyticsEventLogger.getMostRecentEvents(eventCount)
    return events.firstOrNull(predicate) ?: error("Expected to find event.")
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
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

    fun inject(applicationLifecycleLoggerTest: ApplicationLifecycleLoggerTest)
  }

  class TestApplication :
    Application(),
    DataProvidersInjectorProvider,
    ActivityComponentFactory,
    ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerApplicationLifecycleLoggerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(applicationLifecycleLoggerTest: ApplicationLifecycleLoggerTest) {
      component.inject(applicationLifecycleLoggerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
