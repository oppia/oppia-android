package org.oppia.android.domain.oppialogger.analytics

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.CPU_USAGE_METRIC
import org.oppia.android.app.model.ScreenName
import org.oppia.android.domain.oppialogger.EventLogStorageCacheSize
import org.oppia.android.domain.oppialogger.ExceptionLogStorageCacheSize
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.PerformanceMetricsLogStorageCacheSize
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.FakePerformanceMetricAssessor
import org.oppia.android.testing.FakePerformanceMetricsEventLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.logging.SyncStatusTestModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsAssessor.AppIconification.APP_IN_BACKGROUND
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsAssessor.AppIconification.APP_IN_FOREGROUND
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.platformparameter.ENABLE_LANGUAGE_SELECTION_UI_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.EnableLanguageSelectionUi
import org.oppia.android.util.platformparameter.EnablePerformanceMetricsCollection
import org.oppia.android.util.platformparameter.LEARNER_STUDY_ANALYTICS_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.LearnerStudyAnalytics
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.platformparameter.SPLASH_SCREEN_WELCOME_MSG_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.SplashScreenWelcomeMsg
import org.oppia.android.util.platformparameter.SyncUpWorkerTimePeriodHours
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

private const val TEST_CPU_USAGE_ONE = 0.07192
private const val TEST_CPU_USAGE_TWO = 0.32192

/** Tests for [CpuPerformanceSnapshotter]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = CpuPerformanceSnapshotterTest.TestApplication::class)
class CpuPerformanceSnapshotterTest {

  @Inject lateinit var cpuPerformanceSnapshotter: CpuPerformanceSnapshotter
  @Inject lateinit var fakePerformanceMetricsEventLogger: FakePerformanceMetricsEventLogger
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var applicationLifecycleObserver: ApplicationLifecycleObserver
  @Inject lateinit var fakePerformanceMetricAssessor: FakePerformanceMetricAssessor
  @Inject lateinit var fakeOppiaClock: FakeOppiaClock

  @field:[JvmField Inject ForegroundCpuLoggingTimePeriodMillis]
  var foregroundCpuLoggingTimePeriodMillis: Long = Long.MIN_VALUE

  @field:[JvmField Inject BackgroundCpuLoggingTimePeriodMillis]
  var backgroundCpuLoggingTimePeriodMillis: Long = Long.MIN_VALUE

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testSnapshotter_updateIconificationToBackground_logsCpuUsageInBackgroundState() {
    fakePerformanceMetricAssessor.setRelativeCpuUsage(TEST_CPU_USAGE_ONE)
    cpuPerformanceSnapshotter.updateAppIconification(APP_IN_BACKGROUND)
    testCoroutineDispatchers.runCurrent()
    testCoroutineDispatchers.advanceTimeBy(backgroundCpuLoggingTimePeriodMillis)

    val event = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()

    assertThat(event.loggableMetric.loggableMetricTypeCase).isEqualTo(CPU_USAGE_METRIC)
    assertThat(event.loggableMetric.cpuUsageMetric.cpuUsageMetric).isEqualTo(TEST_CPU_USAGE_ONE)
    assertThat(event.currentScreen).isEqualTo(ScreenName.BACKGROUND_SCREEN)
  }

  @Test
  fun testSnapshotter_updateIconificationToForeground_logsCpuUsageInForegroundState() {
    fakePerformanceMetricAssessor.setRelativeCpuUsage(TEST_CPU_USAGE_ONE)
    cpuPerformanceSnapshotter.updateAppIconification(APP_IN_FOREGROUND)
    testCoroutineDispatchers.runCurrent()
    testCoroutineDispatchers.advanceTimeBy(foregroundCpuLoggingTimePeriodMillis)

    val event = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()

    assertThat(event.loggableMetric.loggableMetricTypeCase).isEqualTo(CPU_USAGE_METRIC)
    assertThat(event.loggableMetric.cpuUsageMetric.cpuUsageMetric).isEqualTo(TEST_CPU_USAGE_ONE)
    assertThat(event.currentScreen).isEqualTo(ScreenName.FOREGROUND_SCREEN)
  }

  @Test
  fun testSnapshotter_moveToForeground_moveToBackground_logsSequentially() {
    cpuPerformanceSnapshotter.updateAppIconification(APP_IN_FOREGROUND)
    testCoroutineDispatchers.runCurrent()
    cpuPerformanceSnapshotter.updateAppIconification(APP_IN_BACKGROUND)
    testCoroutineDispatchers.runCurrent()
    testCoroutineDispatchers.advanceTimeBy(backgroundCpuLoggingTimePeriodMillis)

    val count = fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    val latestEvents =
      fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvents(count)
    // Event that got logged after time advancement.
    val latestEvent = latestEvents[count - 1]
    // Event that got logged on second iconification update.
    val secondLatestEvent = latestEvents[count - 2]
    // Event that got logged on first iconification update.
    val thirdLatestEvent = latestEvents[count - 3]

    assertThat(latestEvent.currentScreen).isEqualTo(ScreenName.BACKGROUND_SCREEN)
    assertThat(secondLatestEvent.currentScreen).isEqualTo(ScreenName.FOREGROUND_SCREEN)
    assertThat(thirdLatestEvent.currentScreen).isEqualTo(ScreenName.FOREGROUND_SCREEN)
  }

  @Test
  fun testSnapshotter_moveToForegorund_logsCpuUsage_logsCpuUsageAfterCorrectDelay() {
    fakePerformanceMetricAssessor.setRelativeCpuUsage(TEST_CPU_USAGE_ONE)
    cpuPerformanceSnapshotter.updateAppIconification(APP_IN_FOREGROUND)
    testCoroutineDispatchers.runCurrent()
    val firstEvent = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()

    fakePerformanceMetricAssessor.setRelativeCpuUsage(TEST_CPU_USAGE_TWO)
    testCoroutineDispatchers.advanceTimeBy(foregroundCpuLoggingTimePeriodMillis)
    val latestEvent = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()

    assertThat(latestEvent.isInitialized).isTrue()
    assertThat(latestEvent.currentScreen).isEqualTo(ScreenName.FOREGROUND_SCREEN)
    // Verifying that a CPU usage metric is logged after delay.
    assertThat(latestEvent.loggableMetric.cpuUsageMetric.cpuUsageMetric)
      .isWithin(1e-5).of(TEST_CPU_USAGE_TWO)
    // Verifying that the logged CPU usage metric does not equal to the previously logged metric.
    assertThat(latestEvent.loggableMetric.cpuUsageMetric.cpuUsageMetric)
      .isNotWithin(1e-5).of(firstEvent.loggableMetric.cpuUsageMetric.cpuUsageMetric)
  }

  @Test
  fun testSnapshotter_moveToFg_logsCpuUsage_failsToLogSecondCpuLogBeforeCorrectDelay() {
    cpuPerformanceSnapshotter.updateAppIconification(APP_IN_FOREGROUND)
    testCoroutineDispatchers.runCurrent()
    fakePerformanceMetricsEventLogger.clearAllPerformanceMetricsEvents()

    val timePeriodLessThanFgCpuLoggingTimePeriod = foregroundCpuLoggingTimePeriodMillis - 1000
    testCoroutineDispatchers.advanceTimeBy(timePeriodLessThanFgCpuLoggingTimePeriod)

    assertThat(fakePerformanceMetricsEventLogger.noPerformanceMetricsEventsPresent()).isTrue()
  }

  @Test
  fun testSnapshotter_moveToBackgorund_logsCpuUsage_logsCpuUsageAfterCorrectDelay() {
    fakePerformanceMetricAssessor.setRelativeCpuUsage(TEST_CPU_USAGE_ONE)
    cpuPerformanceSnapshotter.updateAppIconification(APP_IN_BACKGROUND)
    testCoroutineDispatchers.runCurrent()
    val firstEvent = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()

    fakePerformanceMetricAssessor.setRelativeCpuUsage(TEST_CPU_USAGE_TWO)
    testCoroutineDispatchers.advanceTimeBy(backgroundCpuLoggingTimePeriodMillis)
    val latestEvent = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()

    assertThat(latestEvent.isInitialized).isTrue()
    assertThat(latestEvent.currentScreen).isEqualTo(ScreenName.BACKGROUND_SCREEN)
    // Verifying that a CPU usage metric is logged after delay.
    assertThat(latestEvent.loggableMetric.cpuUsageMetric.cpuUsageMetric)
      .isWithin(1e-5).of(TEST_CPU_USAGE_TWO)
    // Verifying that the logged CPU usage metric does not equal to the previously logged metric.
    assertThat(latestEvent.loggableMetric.cpuUsageMetric.cpuUsageMetric)
      .isNotWithin(1e-5).of(firstEvent.loggableMetric.cpuUsageMetric.cpuUsageMetric)
  }

  @Test
  fun testSnapshotter_moveToBg_logsCpuUsage_failsToLogSecondCpuLogBeforeCorrectDelay() {
    cpuPerformanceSnapshotter.updateAppIconification(APP_IN_BACKGROUND)
    testCoroutineDispatchers.runCurrent()
    fakePerformanceMetricsEventLogger.clearAllPerformanceMetricsEvents()

    val timePeriodLessThanBgCpuLoggingTimePeriod = backgroundCpuLoggingTimePeriodMillis - 1000
    testCoroutineDispatchers.advanceTimeBy(timePeriodLessThanBgCpuLoggingTimePeriod)

    assertThat(fakePerformanceMetricsEventLogger.noPerformanceMetricsEventsPresent()).isTrue()
  }

  @Test
  fun testSnapshotter_moveToBg_logsCpuUsage_moveToFg_logsTailEventBeforeNewEvent() {
    cpuPerformanceSnapshotter.updateAppIconification(APP_IN_BACKGROUND)
    testCoroutineDispatchers.runCurrent()
    fakePerformanceMetricsEventLogger.clearAllPerformanceMetricsEvents()

    val timePeriodLessThanBgCpuLoggingTimePeriod = backgroundCpuLoggingTimePeriodMillis - 1000
    testCoroutineDispatchers.advanceTimeBy(timePeriodLessThanBgCpuLoggingTimePeriod)
    cpuPerformanceSnapshotter.updateAppIconification(APP_IN_FOREGROUND)
    testCoroutineDispatchers.runCurrent()

    val latestEventCount = fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    val latestEvents =
      fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvents(latestEventCount)

    assertThat(latestEvents[latestEventCount - 1].currentScreen)
      .isEqualTo(ScreenName.BACKGROUND_SCREEN)
  }

  @Test
  fun testSnapshotter_logCpuUsage_correctCpuUsageValueIsLogged() {
    fakePerformanceMetricAssessor.setRelativeCpuUsage(TEST_CPU_USAGE_ONE)
    cpuPerformanceSnapshotter.updateAppIconification(APP_IN_FOREGROUND)
    testCoroutineDispatchers.runCurrent()

    val latestEvent = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()

    assertThat(latestEvent.loggableMetric.cpuUsageMetric.cpuUsageMetric)
      .isWithin(1e-5).of(TEST_CPU_USAGE_ONE)
  }

  @Test
  fun testSnapshotter_relativeCpuUsageEqualsNull_doesNotLogCpuUsage() {
    fakePerformanceMetricAssessor.setRelativeCpuUsage(null)
    cpuPerformanceSnapshotter.updateAppIconification(APP_IN_FOREGROUND)
    testCoroutineDispatchers.runCurrent()

    assertThat(fakePerformanceMetricsEventLogger.noPerformanceMetricsEventsPresent()).isTrue()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
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
    // Module in tests to avoid needing to specify these settings for tests.
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
    @PerformanceMetricsLogStorageCacheSize
    fun providePerformanceMetricsLogStorageCacheSize(): Int = 5

    @Provides
    @ExceptionLogStorageCacheSize
    fun provideExceptionLogStorageCacheSize(): Int = 2
  }

  @Module
  class TestPlatformParameterModule {
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
      return PlatformParameterValue.createDefaultParameter(
        LEARNER_STUDY_ANALYTICS_DEFAULT_VALUE
      )
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
      TestModule::class, TestLogReportingModule::class, RobolectricModule::class,
      TestDispatcherModule::class, TestLogStorageModule::class,
      NetworkConnectionUtilDebugModule::class, LocaleProdModule::class, FakeOppiaClockModule::class,
      TestPlatformParameterModule::class, PlatformParameterSingletonModule::class,
      LoggingIdentifierModule::class, SyncStatusTestModule::class,
      CpuPerformanceSnapshotterModule::class, ApplicationLifecycleModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(cpuPerformanceSnapshotterTest: CpuPerformanceSnapshotterTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerCpuPerformanceSnapshotterTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(cpuPerformanceSnapshotterTest: CpuPerformanceSnapshotterTest) {
      component.inject(cpuPerformanceSnapshotterTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
