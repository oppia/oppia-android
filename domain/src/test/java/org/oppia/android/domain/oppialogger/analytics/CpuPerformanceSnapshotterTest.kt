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
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
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
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.concurrent.TimeUnit
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

  @field:[JvmField Inject InitialIconificationCutOffTimePeriodMillis]
  var initialIconificationCutOffTimePeriodMillis: Long = Long.MIN_VALUE

  @Before
  fun setUp() {
    TestPlatformParameterModule.forceEnablePerformanceMetricsCollection(true)
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
    cpuPerformanceSnapshotter.updateAppIconification(APP_IN_FOREGROUND)
    testCoroutineDispatchers.runCurrent()

    val count = fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    val latestEvents =
      fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvents(count)
    // Event that got logged on third update call.
    val latestEvent = latestEvents[count - 1]
    // Event that got logged after time advancement.
    val secondLatestEvent = latestEvents[count - 2]
    // Event that got logged on second update call.
    val thirdLatestEvent = latestEvents[count - 3]

    assertThat(latestEvent.currentScreen).isEqualTo(ScreenName.BACKGROUND_SCREEN)
    assertThat(secondLatestEvent.currentScreen).isEqualTo(ScreenName.BACKGROUND_SCREEN)
    assertThat(thirdLatestEvent.currentScreen).isEqualTo(ScreenName.FOREGROUND_SCREEN)
  }

  @Test
  fun testSnapshotter_moveToForegorund_logsCpuUsage_logsCpuUsageAfterCorrectDelay() {
    fakePerformanceMetricAssessor.setRelativeCpuUsage(TEST_CPU_USAGE_ONE)
    cpuPerformanceSnapshotter.updateAppIconification(APP_IN_FOREGROUND)
    testCoroutineDispatchers.runCurrent()
    testCoroutineDispatchers.advanceTimeBy(foregroundCpuLoggingTimePeriodMillis)
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
    testCoroutineDispatchers.advanceTimeBy(backgroundCpuLoggingTimePeriodMillis)
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
    testCoroutineDispatchers.advanceTimeBy(foregroundCpuLoggingTimePeriodMillis)

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

  @Test
  fun testSnapshotter_onCreate_moveToForegroundBeforeCutOff_logsCpuUsageInForegroundAfterDelay() {
    applicationLifecycleObserver.onCreate()
    testCoroutineDispatchers.runCurrent()
    // clearing up all app startup performance metrics: apk size and storage usage.
    fakePerformanceMetricsEventLogger.clearAllPerformanceMetricsEvents()
    testCoroutineDispatchers.advanceTimeBy(1000)
    applicationLifecycleObserver.onAppInForeground()
    testCoroutineDispatchers.runCurrent()
    testCoroutineDispatchers.advanceTimeBy(foregroundCpuLoggingTimePeriodMillis)

    val count = fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    val event = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()

    assertThat(count).isEqualTo(1)
    assertThat(event).isNotNull()
    assertThat(event.currentScreen).isEqualTo(ScreenName.FOREGROUND_SCREEN)
  }

  @Test
  fun testSnapshotter_moveToFgBeforeCutOff_moveToBgBeforeDelayEnds_logsInFg_logsInBgAfterDelay() {
    applicationLifecycleObserver.onCreate()
    testCoroutineDispatchers.runCurrent()
    // clearing up all app startup performance metrics: apk size and storage usage.
    fakePerformanceMetricsEventLogger.clearAllPerformanceMetricsEvents()
    testCoroutineDispatchers.advanceTimeBy(1000)
    applicationLifecycleObserver.onAppInForeground()
    testCoroutineDispatchers.runCurrent()
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.MINUTES.toMillis(2))
    applicationLifecycleObserver.onAppInBackground()
    testCoroutineDispatchers.runCurrent()
    testCoroutineDispatchers.advanceTimeBy(backgroundCpuLoggingTimePeriodMillis)

    val count = fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    val events = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvents(count)

    assertThat(count).isEqualTo(2)
    assertThat(events[0].currentScreen).isEqualTo(ScreenName.FOREGROUND_SCREEN)
    assertThat(events[1].currentScreen).isEqualTo(ScreenName.BACKGROUND_SCREEN)
  }

  @Test
  fun testSnapshotter_moveToFgBeforeCutOff_moveToBgAfterFirstDelay_logsCpuWithCorrectIcon() {
    applicationLifecycleObserver.onCreate()
    testCoroutineDispatchers.runCurrent()
    // clearing up all app startup performance metrics: apk size and storage usage.
    fakePerformanceMetricsEventLogger.clearAllPerformanceMetricsEvents()
    testCoroutineDispatchers.advanceTimeBy(1000)
    applicationLifecycleObserver.onAppInForeground()
    testCoroutineDispatchers.runCurrent()
    testCoroutineDispatchers.advanceTimeBy(foregroundCpuLoggingTimePeriodMillis)
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.MINUTES.toMillis(1))
    applicationLifecycleObserver.onAppInBackground()
    testCoroutineDispatchers.runCurrent()
    testCoroutineDispatchers.advanceTimeBy(backgroundCpuLoggingTimePeriodMillis)

    val count = fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    val events = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvents(count)

    assertThat(count).isEqualTo(3)
    assertThat(events[0].currentScreen).isEqualTo(ScreenName.FOREGROUND_SCREEN)
    assertThat(events[1].currentScreen).isEqualTo(ScreenName.FOREGROUND_SCREEN)
    assertThat(events[2].currentScreen).isEqualTo(ScreenName.BACKGROUND_SCREEN)
  }

  @Test
  fun testSnapshotter_onCreate_setsIconificationToBgAfterCutOff_logsCpuInBgAfterCorrectDelay() {
    applicationLifecycleObserver.onCreate()
    testCoroutineDispatchers.runCurrent()
    // clearing up all app startup performance metrics: apk size and storage usage.
    fakePerformanceMetricsEventLogger.clearAllPerformanceMetricsEvents()
    testCoroutineDispatchers.advanceTimeBy(initialIconificationCutOffTimePeriodMillis)
    testCoroutineDispatchers.advanceTimeBy(backgroundCpuLoggingTimePeriodMillis)

    val count = fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    val events = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvents(count)

    assertThat(count).isEqualTo(1)
    assertThat(events[0].currentScreen).isEqualTo(ScreenName.BACKGROUND_SCREEN)
  }

  @Test
  fun testSnapshotter_onCreate_setsIconToBgAfterCutOff_moveToFgAndLogsCpuInBg_logsCpuInFg() {
    applicationLifecycleObserver.onCreate()
    testCoroutineDispatchers.runCurrent()
    // clearing up all app startup performance metrics: apk size and storage usage.
    fakePerformanceMetricsEventLogger.clearAllPerformanceMetricsEvents()
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.MINUTES.toMillis(5))
    applicationLifecycleObserver.onAppInForeground()
    testCoroutineDispatchers.runCurrent()
    testCoroutineDispatchers.advanceTimeBy(foregroundCpuLoggingTimePeriodMillis)

    val count = fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    val events = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvents(count)

    assertThat(count).isEqualTo(2)
    assertThat(events[0].currentScreen).isEqualTo(ScreenName.BACKGROUND_SCREEN)
    assertThat(events[1].currentScreen).isEqualTo(ScreenName.FOREGROUND_SCREEN)
  }

  @Test
  fun testSnapshotter_setsIconToBgAfterCutOff_logsCpuInBg_moveToFgAndLogsCpuInBg_logsCpuInFg() {
    applicationLifecycleObserver.onCreate()
    testCoroutineDispatchers.runCurrent()
    // clearing up all app startup performance metrics: apk size and storage usage.
    fakePerformanceMetricsEventLogger.clearAllPerformanceMetricsEvents()
    testCoroutineDispatchers.advanceTimeBy(backgroundCpuLoggingTimePeriodMillis)
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.MINUTES.toMillis(3))
    applicationLifecycleObserver.onAppInForeground()
    testCoroutineDispatchers.runCurrent()
    testCoroutineDispatchers.advanceTimeBy(foregroundCpuLoggingTimePeriodMillis)

    val count = fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    val events = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvents(count)

    assertThat(count).isEqualTo(3)
    assertThat(events[0].currentScreen).isEqualTo(ScreenName.BACKGROUND_SCREEN)
    assertThat(events[1].currentScreen).isEqualTo(ScreenName.BACKGROUND_SCREEN)
    assertThat(events[2].currentScreen).isEqualTo(ScreenName.FOREGROUND_SCREEN)
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
    @PerformanceMetricsLogStorageCacheSize
    fun providePerformanceMetricsLogStorageCacheSize(): Int = 5

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
