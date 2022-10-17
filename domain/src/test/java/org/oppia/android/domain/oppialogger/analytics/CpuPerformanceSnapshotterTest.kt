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
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val TEST_CPU_USAGE = 0.07192

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

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testSnapshotter_startsLoggingCpuUsageOnInitialisation() {
    fakePerformanceMetricAssessor.setRelativeCpuUsage(TEST_CPU_USAGE)
    testCoroutineDispatchers.runCurrent()

    val event = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()

    assertThat(event.loggableMetric.loggableMetricTypeCase).isEqualTo(CPU_USAGE_METRIC)
    assertThat(event.loggableMetric.cpuUsageMetric.cpuUsageMetric).isEqualTo(TEST_CPU_USAGE)
    assertThat(event.currentScreen).isEqualTo(ScreenName.FOREGROUND_SCREEN)
  }

  @Test
  fun testSnapshotter_updateIconificationToBackground_logsCpuUsageInBackgroundState() {
    testCoroutineDispatchers.runCurrent()
    fakePerformanceMetricAssessor.setRelativeCpuUsage(TEST_CPU_USAGE)
    cpuPerformanceSnapshotter.updateAppIconification(APP_IN_BACKGROUND)
    testCoroutineDispatchers.runCurrent()

    val event = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()

    assertThat(event.loggableMetric.loggableMetricTypeCase).isEqualTo(CPU_USAGE_METRIC)
    assertThat(event.loggableMetric.cpuUsageMetric.cpuUsageMetric).isEqualTo(TEST_CPU_USAGE)
    assertThat(event.currentScreen).isEqualTo(ScreenName.BACKGROUND_SCREEN)
  }

  @Test
  fun testSnapshotter_updateIconificationToForeground_logsCpuUsageInForegroundState() {
    testCoroutineDispatchers.runCurrent()
    fakePerformanceMetricAssessor.setRelativeCpuUsage(TEST_CPU_USAGE)
    cpuPerformanceSnapshotter.updateAppIconification(APP_IN_FOREGROUND)
    testCoroutineDispatchers.runCurrent()

    val event = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()

    assertThat(event.loggableMetric.loggableMetricTypeCase).isEqualTo(CPU_USAGE_METRIC)
    assertThat(event.loggableMetric.cpuUsageMetric.cpuUsageMetric).isEqualTo(TEST_CPU_USAGE)
    assertThat(event.currentScreen).isEqualTo(ScreenName.FOREGROUND_SCREEN)
  }

  @Test
  fun testSnapshotter_moveToForeground_moveToBackground_verifySequentialLogging() {
    testCoroutineDispatchers.runCurrent()
    cpuPerformanceSnapshotter.updateAppIconification(APP_IN_FOREGROUND)
    testCoroutineDispatchers.runCurrent()
    cpuPerformanceSnapshotter.updateAppIconification(APP_IN_BACKGROUND)
    testCoroutineDispatchers.runCurrent()

    val count = fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    val latestEvents =
      fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvents(count)
    val latestEvent = latestEvents[count - 1]
    val secondLatestEvent = latestEvents[count - 2]

    assertThat(latestEvent.currentScreen).isEqualTo(ScreenName.BACKGROUND_SCREEN)
    assertThat(secondLatestEvent.currentScreen).isEqualTo(ScreenName.FOREGROUND_SCREEN)
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

  @Module
  class TestCpuPerformanceSnapshotterModule {

    companion object {
      var foregroundCpuLoggingTimePeriod = TimeUnit.MINUTES.toMillis(5)
      var backgroundCpuLoggingTimePeriod = TimeUnit.MINUTES.toMillis(60)
    }

    @Provides
    fun providesCpuPerformanceSnapshotter(
      factory: CpuPerformanceSnapshotter.Factory
    ): CpuPerformanceSnapshotter = factory.createSnapshotter()

    @Provides
    @ForegroundCpuLoggingTimePeriodMillis
    fun provideForegroundCpuLoggingTimePeriod(): Long = foregroundCpuLoggingTimePeriod

    @Provides
    @BackgroundCpuLoggingTimePeriodMillis
    fun provideBackgroundCpuLoggingTimePeriod(): Long = backgroundCpuLoggingTimePeriod
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
      TestCpuPerformanceSnapshotterModule::class, ApplicationLifecycleModule::class
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
