package org.oppia.android.domain.oppialogger.logscheduler

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.WorkInfo
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.MEMORY_USAGE_METRIC
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.NETWORK_USAGE_METRIC
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.STORAGE_USAGE_METRIC
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.FakePerformanceMetricsEventLogger
import org.oppia.android.testing.logging.SyncStatusTestModule
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsConfigurationsModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import org.oppia.android.app.model.ScreenName
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleLogger
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulingWorker.Companion.WORKER_NAME
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulingWorker.Operation.SCHEDULE_LOG_PERIODIC_BACKGROUND_METRICS
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulingWorker.Operation.SCHEDULE_LOG_PERIODIC_UI_METRICS
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulingWorker.Operation.SCHEDULE_LOG_STORAGE_USAGE_METRICS
import org.oppia.android.domain.platformparameter.PlatformParameterControllerInjector
import org.oppia.android.domain.platformparameter.PlatformParameterControllerInjectorProvider
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.domain.workmanager.testing.OppiaWorkManagerTestDriver
import org.oppia.android.domain.workmanager.testing.OppiaWorkManagerTestInitializer
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.util.threading.BackgroundDispatcher
import org.oppia.android.util.threading.DispatcherInjector
import org.oppia.android.util.threading.DispatcherInjectorProvider
import org.robolectric.shadows.ShadowLog

/** Tests for [MetricLogSchedulingWorker]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = MetricLogSchedulingWorkerTest.TestApplication::class)
class MetricLogSchedulingWorkerTest {
  @Inject lateinit var context: Context
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var configuration: Configuration
  @Inject lateinit var oppiaWorkManagerTestInitializer: OppiaWorkManagerTestInitializer
  @Inject lateinit var testDriver: OppiaWorkManagerTestDriver
  @Inject lateinit var fakePerformanceMetricsEventLogger: FakePerformanceMetricsEventLogger
  @Inject lateinit var applicationLifecycleLogger: ApplicationLifecycleLogger
  @field:[Inject BackgroundDispatcher] lateinit var backgroundDispatcher: CoroutineDispatcher

  @After
  fun tearDown() {
    TestPlatformParameterModule.reset()
  }

  @Test
  fun testWorker_scheduleLogPeriodicBackgroundMetrics_logsNetworkUsageMetricsInBackground() {
    TestPlatformParameterModule.forceEnablePerformanceMetricsCollection(true)
    setUpTestApplicationComponent()
    initializeDependencies()

    val workInfo = testDriver.runOneOffWork(WORKER_NAME, SCHEDULE_LOG_PERIODIC_BACKGROUND_METRICS)

    // Verify that the job succeeded, was the only job to log something, and logged correctly.
    val logCount = fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    val loggedEvent = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()
    assertThat(workInfo.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(logCount).isEqualTo(1)
    assertThat(loggedEvent.loggableMetric.loggableMetricTypeCase).isEqualTo(NETWORK_USAGE_METRIC)
    assertThat(loggedEvent.currentScreen).isEqualTo(ScreenName.BACKGROUND_SCREEN)
  }

  @Test
  fun testWorker_scheduleLogPeriodicBackgroundMetrics_perfMetricsOff_succeedsAndLogsNothing() {
    TestPlatformParameterModule.forceEnablePerformanceMetricsCollection(false)
    setUpTestApplicationComponent()
    initializeDependencies()

    val workInfo = testDriver.runOneOffWork(WORKER_NAME, SCHEDULE_LOG_PERIODIC_BACKGROUND_METRICS)

    // The job should succeed but nothing will be logged since performance metrics are disabled.
    val logCount = fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    assertThat(workInfo.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(logCount).isEqualTo(0)
  }

  @Test
  fun testWorker_scheduleLogPeriodicBackgroundMetrics_hasFailure_failsAndLogsError() {
    TestPlatformParameterModule.forceEnablePerformanceMetricsCollection(true)
    setUpTestApplicationComponent()
    initializeDependencies()
    fakePerformanceMetricsEventLogger.setFailure(Exception("Forced failure."))

    val workInfo = testDriver.runOneOffWork(WORKER_NAME, SCHEDULE_LOG_PERIODIC_BACKGROUND_METRICS)

    // Verify that the job failed and an error was logged due to an underlying event logger failure.
    val logCount = fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    val failureLine = fetchSingleWorkerErrorLog()
    assertThat(workInfo.state).isEqualTo(WorkInfo.State.FAILED)
    assertThat(logCount).isEqualTo(0)
    assertThat(failureLine).contains("Failed operation: schedule_log_periodic_background_metrics.")
    assertThat(failureLine).contains("java.lang.Exception: Forced failure.")
  }

  @Test
  fun testWorker_scheduleLogPeriodicUiMetrics_homeScreen_logsNetworkUsageMetricsInHomeScreen() {
    TestPlatformParameterModule.forceEnablePerformanceMetricsCollection(true)
    setUpTestApplicationComponent()
    initializeDependencies()
    simulateResumeHomeActivity()

    val workInfo = testDriver.runOneOffWork(WORKER_NAME, SCHEDULE_LOG_PERIODIC_UI_METRICS)

    // Verify that the job succeeded, was the only job to log something, and logged correctly.
    val logCount = fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    val loggedEvent = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()
    assertThat(workInfo.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(logCount).isEqualTo(1)
    assertThat(loggedEvent.loggableMetric.loggableMetricTypeCase).isEqualTo(MEMORY_USAGE_METRIC)
    assertThat(loggedEvent.currentScreen).isEqualTo(ScreenName.HOME_ACTIVITY)
  }

  @Test
  fun testWorker_scheduleLogPeriodicUiMetrics_homeScreen_perfMetricsOff_succeedsAndLogsNothing() {
    TestPlatformParameterModule.forceEnablePerformanceMetricsCollection(false)
    setUpTestApplicationComponent()
    initializeDependencies()
    simulateResumeHomeActivity()

    val workInfo = testDriver.runOneOffWork(WORKER_NAME, SCHEDULE_LOG_PERIODIC_UI_METRICS)

    // The job should succeed but nothing will be logged since performance metrics are disabled.
    val logCount = fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    assertThat(workInfo.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(logCount).isEqualTo(0)
  }

  @Test
  fun testWorker_scheduleLogPeriodicUiMetrics_homeScreen_hasFailure_failsAndLogsError() {
    TestPlatformParameterModule.forceEnablePerformanceMetricsCollection(true)
    setUpTestApplicationComponent()
    initializeDependencies()
    simulateResumeHomeActivity()
    fakePerformanceMetricsEventLogger.setFailure(Exception("Forced failure."))

    val workInfo = testDriver.runOneOffWork(WORKER_NAME, SCHEDULE_LOG_PERIODIC_UI_METRICS)

    // Verify that the job failed and an error was logged due to an underlying event logger failure.
    val logCount = fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    val failureLine = fetchSingleWorkerErrorLog()
    assertThat(workInfo.state).isEqualTo(WorkInfo.State.FAILED)
    assertThat(logCount).isEqualTo(0)
    assertThat(failureLine).contains("Failed operation: schedule_log_periodic_ui_metrics.")
    assertThat(failureLine).contains("java.lang.Exception: Forced failure.")
  }

  @Test
  fun testWorker_scheduleLogStorageUsageMetrics_logsStorageUsageMetricsInBackground() {
    TestPlatformParameterModule.forceEnablePerformanceMetricsCollection(true)
    setUpTestApplicationComponent()
    initializeDependencies()

    val workInfo = testDriver.runOneOffWork(WORKER_NAME, SCHEDULE_LOG_STORAGE_USAGE_METRICS)

    // Verify that the job succeeded, was the only job to log something, and logged correctly.
    val logCount = fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    val loggedEvent = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvent()
    assertThat(workInfo.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(logCount).isEqualTo(1)
    assertThat(loggedEvent.loggableMetric.loggableMetricTypeCase).isEqualTo(STORAGE_USAGE_METRIC)
    assertThat(loggedEvent.currentScreen).isEqualTo(ScreenName.BACKGROUND_SCREEN)
  }

  @Test
  fun testWorker_scheduleLogStorageUsageMetrics_perfMetricsOff_succeedsAndLogsNothing() {
    TestPlatformParameterModule.forceEnablePerformanceMetricsCollection(false)
    setUpTestApplicationComponent()
    initializeDependencies()

    val workInfo = testDriver.runOneOffWork(WORKER_NAME, SCHEDULE_LOG_STORAGE_USAGE_METRICS)

    // The job should succeed but nothing will be logged since performance metrics are disabled.
    val logCount = fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    assertThat(workInfo.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(logCount).isEqualTo(0)
  }

  @Test
  fun testWorker_scheduleLogStorageUsageMetrics_hasFailure_failsAndLogsError() {
    TestPlatformParameterModule.forceEnablePerformanceMetricsCollection(true)
    setUpTestApplicationComponent()
    initializeDependencies()
    fakePerformanceMetricsEventLogger.setFailure(Exception("Forced failure."))

    val workInfo = testDriver.runOneOffWork(WORKER_NAME, SCHEDULE_LOG_STORAGE_USAGE_METRICS)

    // Verify that the job failed and an error was logged due to an underlying event logger failure.
    val logCount = fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    val failureLine = fetchSingleWorkerErrorLog()
    assertThat(workInfo.state).isEqualTo(WorkInfo.State.FAILED)
    assertThat(logCount).isEqualTo(0)
    assertThat(failureLine).contains("Failed operation: schedule_log_storage_usage_metrics.")
    assertThat(failureLine).contains("java.lang.Exception: Forced failure.")
  }

  private fun simulateResumeHomeActivity() {
    // Simulate being on a specific screen (though clear any logs from the lifecycle logger to avoid
    // interfering with validating the worker).
    applicationLifecycleLogger.recordActivityResumed(ScreenName.HOME_ACTIVITY, 0L, 0L)
    fakePerformanceMetricsEventLogger.clearAllPerformanceMetricsEvents()
  }

  private fun initializeDependencies() {
    FirebaseApp.initializeApp(context)
    oppiaWorkManagerTestInitializer.initializeWorkManager(configuration)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun fetchSingleWorkerErrorLog(): String {
    val errorLogs = fetchWorkerErrorLogs()
    assertThat(errorLogs).hasSize(1)
    return errorLogs.single()
  }

  private fun fetchWorkerErrorLogs(): List<String> {
    // Extract all logs from the bootstrap worker and validate they are each errors before returning
    // the logged message lines.
    return ShadowLog.getLogs().filter { it.tag == WORKER_NAME }.onEach {
      assertThat(it.type).isEqualTo(Log.ERROR)
    }.map(ShadowLog.LogItem::msg)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    fun provideContext(application: Application): Context = application
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      ApplicationLifecycleModule::class,
      AssetModule::class,
      CpuPerformanceSnapshotterModule::class,
      FakeOppiaClockModule::class,
      LocaleProdModule::class,
      LoggerModule::class,
      LoggingIdentifierModule::class,
      NetworkConnectionUtilDebugModule::class,
      PerformanceMetricsConfigurationsModule::class,
      PlatformParameterSingletonModule::class,
      RobolectricModule::class,
      SyncStatusTestModule::class,
      TestDispatcherModule::class,
      TestModule::class,
      TestAuthenticationModule::class,
      LogStorageModule::class,
      TestLogReportingModule::class,
      WorkManagerConfigurationModule::class,
      TestPlatformParameterModule::class,
      MetricLogSchedulerModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector, DispatcherInjector, PlatformParameterControllerInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(metricLogSchedulingWorkerTest: MetricLogSchedulingWorkerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider, DispatcherInjectorProvider, PlatformParameterControllerInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerMetricLogSchedulingWorkerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(metricLogSchedulingWorkerTest: MetricLogSchedulingWorkerTest) {
      component.inject(metricLogSchedulingWorkerTest)
    }

    override fun getDataProvidersInjector() = component
    override fun getDispatcherInjector() = component
    override fun getPlatformParameterControllerInjector() = component
  }
}
