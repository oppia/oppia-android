package org.oppia.android.domain.oppialogger.logscheduler

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.Data
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.truth.Truth.assertThat
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.logging.SyncStatusTestModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.LogReportingModule
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsAssessorModule
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsConfigurationsModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [PerformanceMetricsLogScheduler]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = PerformanceMetricsLogSchedulerTest.TestApplication::class)
class PerformanceMetricsLogSchedulerTest {

  @Inject
  lateinit var performanceMetricsLogScheduler: PerformanceMetricsLogScheduler

  @Inject
  lateinit var metricLogSchedulingWorkerFactory: MetricLogSchedulingWorkerFactory

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var context: Context

  private lateinit var workManager: WorkManager

  private val workerCaseForSchedulingPeriodicBackgroundMetricLogs: Data = Data.Builder()
    .putString(
      MetricLogSchedulingWorker.WORKER_CASE_KEY,
      MetricLogSchedulingWorker.PERIODIC_BACKGROUND_METRIC_WORKER
    )
    .build()

  private val workerCaseForSchedulingStorageUsageMetricLogs: Data = Data.Builder()
    .putString(
      MetricLogSchedulingWorker.WORKER_CASE_KEY,
      MetricLogSchedulingWorker.STORAGE_USAGE_WORKER
    )
    .build()

  private val workerCaseForSchedulingPeriodicUiMetricLogs: Data = Data.Builder()
    .putString(
      MetricLogSchedulingWorker.WORKER_CASE_KEY,
      MetricLogSchedulingWorker.PERIODIC_UI_METRIC_WORKER
    )
    .build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    val config = Configuration.Builder()
      .setExecutor(SynchronousExecutor())
      .setWorkerFactory(metricLogSchedulingWorkerFactory)
      .build()
    WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
  }

  @Test
  fun testScheduler_enqueueRequestForPeriodicBackgroundMetrics_workRequestGetsEnqueued() {
    val workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext())
    val request = PeriodicWorkRequest
      .Builder(MetricLogSchedulingWorker::class.java, 15, TimeUnit.MINUTES)
      .setInputData(workerCaseForSchedulingPeriodicBackgroundMetricLogs)
      .build()

    performanceMetricsLogScheduler.enqueueWorkRequestForPeriodicBackgroundMetrics(
      workManager,
      request
    )
    testCoroutineDispatchers.runCurrent()
    val workInfo = workManager.getWorkInfoById(request.id)

    assertThat(workInfo.get().state).isEqualTo(WorkInfo.State.ENQUEUED)
  }

  @Test
  fun testScheduler_enqueueRequestForPeriodicUiMetric_workRequestGetsEnqueued() {
    val workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext())

    val request = PeriodicWorkRequest
      .Builder(MetricLogSchedulingWorker::class.java, 15, TimeUnit.MINUTES)
      .setInputData(workerCaseForSchedulingPeriodicUiMetricLogs)
      .build()

    performanceMetricsLogScheduler.enqueueWorkRequestForPeriodicUiMetrics(
      workManager,
      request
    )
    testCoroutineDispatchers.runCurrent()
    val workInfo = workManager.getWorkInfoById(request.id)

    assertThat(workInfo.get().state).isEqualTo(WorkInfo.State.ENQUEUED)
  }

  @Test
  fun testScheduler_enqueueRequestForStorageMetric_workRequestGetsEnqueued() {
    val workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext())

    val request = PeriodicWorkRequest
      .Builder(MetricLogSchedulingWorker::class.java, 15, TimeUnit.MINUTES)
      .setInputData(workerCaseForSchedulingStorageUsageMetricLogs)
      .build()

    performanceMetricsLogScheduler.enqueueWorkRequestForStorageUsage(
      workManager,
      request
    )
    testCoroutineDispatchers.runCurrent()
    val workInfo = workManager.getWorkInfoById(request.id)

    assertThat(workInfo.get().state).isEqualTo(WorkInfo.State.ENQUEUED)
  }

  private fun setUpTestApplicationComponent() {
    DaggerPerformanceMetricsLogSchedulerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  interface TestModule {
    @Binds
    fun provideContext(application: Application): Context
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, MetricLogSchedulerModule::class, LoggerModule::class,
      RobolectricModule::class, LocaleProdModule::class, FakeOppiaClockModule::class,
      TestDispatcherModule::class, LogReportWorkerModule::class, FakeOppiaClockModule::class,
      NetworkConnectionUtilDebugModule::class, LocaleProdModule::class, LoggerModule::class,
      AssetModule::class, PlatformParameterModule::class, PlatformParameterSingletonModule::class,
      LoggingIdentifierModule::class, SyncStatusTestModule::class,
      PerformanceMetricsAssessorModule::class, ApplicationLifecycleModule::class,
      LogReportingModule::class, PerformanceMetricsConfigurationsModule::class,
      LogStorageModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(performanceMetricsLogSchedulerTest: PerformanceMetricsLogSchedulerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerPerformanceMetricsLogSchedulerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(performanceMetricsLogSchedulerTest: PerformanceMetricsLogSchedulerTest) {
      component.inject(performanceMetricsLogSchedulerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
