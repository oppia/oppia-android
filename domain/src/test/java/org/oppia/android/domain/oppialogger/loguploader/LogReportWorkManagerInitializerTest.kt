package org.oppia.android.domain.oppialogger.loguploader

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.DelegatingWorkerFactory
import androidx.work.NetworkType
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.truth.Truth.assertThat
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.domain.oppialogger.EventLogStorageCacheSize
import org.oppia.android.domain.oppialogger.ExceptionLogStorageCacheSize
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.PerformanceMetricsLogStorageCacheSize
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.oppialogger.analytics.testing.FakeLogScheduler
import org.oppia.android.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulingWorker
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulingWorkerFactory
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.testing.oppialogger.loguploader.FakeLogUploader
import org.oppia.android.testing.FakeExceptionLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LogUploader
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.MetricLogScheduler
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtil
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class LogReportWorkManagerInitializerTest {

  @Inject
  lateinit var logUploadWorkerFactory: LogUploadWorkerFactory

  @Inject
  lateinit var metricLogSchedulingWorkerFactory: MetricLogSchedulingWorkerFactory

  @Inject
  lateinit var logReportWorkManagerInitializer: LogReportWorkManagerInitializer

  @Inject
  lateinit var exceptionsController: ExceptionsController

  @Inject
  lateinit var networkConnectionUtil: NetworkConnectionDebugUtil

  @Inject
  lateinit var fakeExceptionLogger: FakeExceptionLogger

  @Inject
  lateinit var dataProviders: DataProviders

  @Inject
  lateinit var oppiaLogger: OppiaLogger

  @Inject
  lateinit var fakeLogUploader: FakeLogUploader

  @Inject
  lateinit var fakeLogScheduler: FakeLogScheduler

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  private lateinit var context: Context

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    context = InstrumentationRegistry.getInstrumentation().targetContext

    val delegatingWorkerFactory = DelegatingWorkerFactory()
    delegatingWorkerFactory.addFactory(logUploadWorkerFactory)
    delegatingWorkerFactory.addFactory(metricLogSchedulingWorkerFactory)

    val config = Configuration.Builder()
      .setExecutor(SynchronousExecutor())
      .setWorkerFactory(delegatingWorkerFactory)
      .build()
    WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
  }

  @Test
  fun testWorkRequest_onCreate_enqueuesRequest_verifyRequestId() {
    logReportWorkManagerInitializer.onCreate(WorkManager.getInstance(context))
    testCoroutineDispatchers.runCurrent()

    val enqueuedEventWorkRequestId = logReportWorkManagerInitializer.getWorkRequestForEventsId()
    val enqueuedExceptionWorkRequestId =
      logReportWorkManagerInitializer.getWorkRequestForExceptionsId()
    val enqueuedPerformanceMetricsWorkRequestId =
      logReportWorkManagerInitializer.getWorkRequestForPerformanceMetricsId()
    val enqueuedSchedulingStorageUsageMetricWorkRequestId =
      logReportWorkManagerInitializer.getWorkRequestForSchedulingStorageUsageMetricLogsId()
    val enqueuedSchedulingPeriodicUiMetricWorkRequestId =
      logReportWorkManagerInitializer.getWorkRequestForSchedulingPeriodicUiMetricLogsId()
    val enqueuedSchedulingPeriodicBackgroundPerformanceMetricWorkRequestId =
      logReportWorkManagerInitializer
        .getWorkRequestForSchedulingPeriodicBackgroundPerformanceMetricLogsId()

    assertThat(fakeLogUploader.getMostRecentEventRequestId()).isEqualTo(enqueuedEventWorkRequestId)
    assertThat(fakeLogUploader.getMostRecentExceptionRequestId()).isEqualTo(
      enqueuedExceptionWorkRequestId
    )
    assertThat(fakeLogUploader.getMostRecentPerformanceMetricsRequestId()).isEqualTo(
      enqueuedPerformanceMetricsWorkRequestId
    )
    assertThat(fakeLogScheduler.getMostRecentStorageUsageMetricLoggingRequestId()).isEqualTo(
      enqueuedSchedulingStorageUsageMetricWorkRequestId
    )
    assertThat(fakeLogScheduler.getMostRecentPeriodicUiMetricLoggingRequestId()).isEqualTo(
      enqueuedSchedulingPeriodicUiMetricWorkRequestId
    )
    assertThat(fakeLogScheduler.getMostRecentPeriodicBackgroundMetricLoggingRequestId()).isEqualTo(
      enqueuedSchedulingPeriodicBackgroundPerformanceMetricWorkRequestId
    )
  }

  @Test
  fun testWorkRequest_verifyWorkerConstraints() {
    val workerConstraints = Constraints.Builder()
      .setRequiredNetworkType(NetworkType.CONNECTED)
      .setRequiresBatteryNotLow(true)
      .build()

    val logUploadingWorkRequestConstraints =
      logReportWorkManagerInitializer.getLogReportWorkerConstraints()

    assertThat(logUploadingWorkRequestConstraints).isEqualTo(workerConstraints)
  }

  @Test
  fun testWorkRequest_verifyWorkRequestDataForEvents() {
    val workerCaseForUploadingEvents: Data = Data.Builder()
      .putString(
        LogUploadWorker.WORKER_CASE_KEY,
        LogUploadWorker.EVENT_WORKER
      )
      .build()

    assertThat(logReportWorkManagerInitializer.getWorkRequestDataForEvents()).isEqualTo(
      workerCaseForUploadingEvents
    )
  }

  @Test
  fun testWorkRequest_verifyWorkRequestDataForExceptions() {
    val workerCaseForUploadingExceptions: Data = Data.Builder()
      .putString(
        LogUploadWorker.WORKER_CASE_KEY,
        LogUploadWorker.EXCEPTION_WORKER
      )
      .build()

    assertThat(
      logReportWorkManagerInitializer.getWorkRequestDataForExceptions()
    ).isEqualTo(workerCaseForUploadingExceptions)
  }

  @Test
  fun testWorkRequest_verifyWorkRequestDataForPerformanceMetrics() {
    val workerCaseForUploadingPerformanceMetrics: Data = Data.Builder()
      .putString(
        LogUploadWorker.WORKER_CASE_KEY,
        LogUploadWorker.PERFORMANCE_METRICS_WORKER
      )
      .build()

    assertThat(logReportWorkManagerInitializer.getWorkRequestDataForPerformanceMetrics()).isEqualTo(
      workerCaseForUploadingPerformanceMetrics
    )
  }

  @Test
  fun testWorkRequest_verifyWorkRequestData_forSchedulingStorageUsageMetricLogs() {
    val workerCaseForSchedulingStorageUsageMetricLogs: Data = Data.Builder()
      .putString(
        MetricLogSchedulingWorker.WORKER_CASE_KEY,
        MetricLogSchedulingWorker.STORAGE_USAGE_WORKER
      )
      .build()

    assertThat(
      logReportWorkManagerInitializer.getWorkRequestDataForSchedulingStorageUsageMetricLogs()
    ).isEqualTo(workerCaseForSchedulingStorageUsageMetricLogs)
  }

  @Test
  fun testWorkRequest_verifyWorkRequestData_forSchedulingPeriodicBackgroundPerformanceMetricLogs() {
    val workerCaseForSchedulingPeriodicPerformanceMetricLogs: Data = Data.Builder()
      .putString(
        MetricLogSchedulingWorker.WORKER_CASE_KEY,
        MetricLogSchedulingWorker.PERIODIC_BACKGROUND_METRIC_WORKER
      )
      .build()

    assertThat(
      logReportWorkManagerInitializer
        .getWorkRequestDataForSchedulingPeriodicBackgroundPerformanceMetricLogs()
    ).isEqualTo(workerCaseForSchedulingPeriodicPerformanceMetricLogs)
  }

  @Test
  fun testWorkRequest_verifyWorkRequestData_forSchedulingPeriodicUiMetricLogs() {
    val workerCaseForSchedulingMemoryUsageMetricLogs: Data = Data.Builder()
      .putString(
        MetricLogSchedulingWorker.WORKER_CASE_KEY,
        MetricLogSchedulingWorker.PERIODIC_UI_METRIC_WORKER
      )
      .build()

    assertThat(
      logReportWorkManagerInitializer.getWorkRequestDataForSchedulingPeriodicUiMetricLogs()
    ).isEqualTo(workerCaseForSchedulingMemoryUsageMetricLogs)
  }

  private fun setUpTestApplicationComponent() {
    DaggerLogReportWorkManagerInitializerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }
  }

  @Module
  class TestLogStorageModule {

    @Provides
    @EventLogStorageCacheSize
    fun provideEventLogStorageCacheSize(): Int = 2

    @Provides
    @ExceptionLogStorageCacheSize
    fun provideExceptionLogStorageSize(): Int = 2

    @Provides
    @PerformanceMetricsLogStorageCacheSize
    fun providePerformanceMetricsLogStorageCacheSize(): Int = 2
  }

  @Module
  interface TestFirebaseLogUploaderModule {

    @Binds
    fun bindsFakeLogUploader(fakeLogUploader: FakeLogUploader): LogUploader

    @Binds
    fun bindsFakeLogScheduler(fakeLogScheduler: FakeLogScheduler): MetricLogScheduler
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, RobolectricModule::class,
      TestLogStorageModule::class, TestDispatcherModule::class,
      LogReportWorkerModule::class, TestFirebaseLogUploaderModule::class,
      FakeOppiaClockModule::class, NetworkConnectionUtilDebugModule::class, LocaleProdModule::class,
      LoggerModule::class, AssetModule::class, LoggerModule::class, PlatformParameterModule::class,
      PlatformParameterSingletonModule::class, LoggingIdentifierModule::class,
      SyncStatusModule::class, ApplicationLifecycleModule::class,
      CpuPerformanceSnapshotterModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(logUploadWorkRequestTest: LogReportWorkManagerInitializerTest)
  }
}
