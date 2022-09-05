package org.oppia.android.domain.oppialogger.analytics.testing

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.Data
import androidx.work.PeriodicWorkRequestBuilder
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
import org.oppia.android.domain.oppialogger.PerformanceMetricsLogStorageCacheSize
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulingWorker
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulingWorkerFactory
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorker
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.MetricLogScheduler
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsConfigurationsModule
import org.oppia.android.util.networking.NetworkConnectionUtilProdModule
import org.oppia.android.util.system.OppiaClockModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [FakeLogScheduler]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = FakeLogSchedulerTest.TestApplication::class)
class FakeLogSchedulerTest {

  @Inject
  lateinit var fakeLogScheduler: FakeLogScheduler

  @Inject
  lateinit var workerFactory: MetricLogSchedulingWorkerFactory

  @Inject
  lateinit var context: Context

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    val config = Configuration.Builder()
      .setExecutor(SynchronousExecutor())
      .setWorkerFactory(workerFactory)
      .build()
    WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
  }

  @Test
  fun testFakeScheduler_scheduleStorageLogging_verifyScheduling() {
    val workManager = WorkManager.getInstance(context)

    val inputData = Data.Builder().putString(
      MetricLogSchedulingWorker.WORKER_CASE_KEY,
      MetricLogSchedulingWorker.STORAGE_USAGE_WORKER
    ).build()

    val request = PeriodicWorkRequestBuilder<LogUploadWorker>(10, TimeUnit.SECONDS)
      .setInputData(inputData)
      .build()

    fakeLogScheduler.enqueueWorkRequestForStorageUsage(
      workManager,
      request
    )

    assertThat(fakeLogScheduler.getMostRecentStorageUsageMetricLoggingRequestId())
      .isEqualTo(request.id)
  }

  @Test
  fun testFakeScheduler_schedulePeriodicBackgroundMetricsLogging_verifyScheduling() {
    val workManager = WorkManager.getInstance(context)

    val inputData = Data.Builder().putString(
      MetricLogSchedulingWorker.WORKER_CASE_KEY,
      MetricLogSchedulingWorker.PERIODIC_BACKGROUND_METRIC_WORKER
    ).build()

    val request = PeriodicWorkRequestBuilder<LogUploadWorker>(10, TimeUnit.SECONDS)
      .setInputData(inputData)
      .build()

    fakeLogScheduler.enqueueWorkRequestForPeriodicBackgroundMetrics(
      workManager,
      request
    )

    assertThat(fakeLogScheduler.getMostRecentPeriodicBackgroundMetricLoggingRequestId())
      .isEqualTo(request.id)
  }

  @Test
  fun testFakeScheduler_schedulePeriodicUiMetricsLogging_verifyScheduling() {
    val workManager = WorkManager.getInstance(context)

    val inputData = Data.Builder().putString(
      MetricLogSchedulingWorker.WORKER_CASE_KEY,
      MetricLogSchedulingWorker.PERIODIC_UI_METRIC_WORKER
    ).build()

    val request = PeriodicWorkRequestBuilder<LogUploadWorker>(10, TimeUnit.SECONDS)
      .setInputData(inputData)
      .build()

    fakeLogScheduler.enqueueWorkRequestForPeriodicUiMetrics(
      workManager,
      request
    )

    assertThat(fakeLogScheduler.getMostRecentPeriodicUiMetricLoggingRequestId())
      .isEqualTo(request.id)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  interface TestModule {
    @Binds
    fun provideContext(application: Application): Context

    @Binds
    fun bindMetricLogScheduler(fakeLogScheduler: FakeLogScheduler): MetricLogScheduler
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

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, LoggerModule::class, TestDispatcherModule::class,
      TestLogReportingModule::class, RobolectricModule::class,
      PerformanceMetricsConfigurationsModule::class, LocaleProdModule::class,
      OppiaClockModule::class, NetworkConnectionUtilProdModule::class, TestLogStorageModule::class,
      PlatformParameterModule::class, PlatformParameterSingletonModule::class,
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(test: FakeLogSchedulerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerFakeLogSchedulerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: FakeLogSchedulerTest) {
      component.inject(test)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
