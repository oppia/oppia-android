package org.oppia.android.domain.oppialogger.logscheduler

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
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
import org.mockito.Mockito
import org.oppia.android.app.model.OppiaMetricLog
import org.oppia.android.domain.oppialogger.EventLogStorageCacheSize
import org.oppia.android.domain.oppialogger.ExceptionLogStorageCacheSize
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.PerformanceMetricsLogStorageCacheSize
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.PerformanceMetricsController
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorker
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.testing.oppialogger.loguploader.FakeLogUploader
import org.oppia.android.testing.FakeEventLogger
import org.oppia.android.testing.FakeExceptionLogger
import org.oppia.android.testing.FakePerformanceMetricsEventLogger
import org.oppia.android.testing.logging.SyncStatusTestModule
import org.oppia.android.testing.mockito.anyOrNull
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EventLogger
import org.oppia.android.util.logging.ExceptionLogger
import org.oppia.android.util.logging.LogUploader
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsEventLogger
import org.oppia.android.util.networking.NetworkConnectionDebugUtil
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

/** Tests for [MetricLogSchedulingWorker]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = MetricLogSchedulingWorkerTest.TestApplication::class)
class MetricLogSchedulingWorkerTest {
  @Inject
  lateinit var networkConnectionUtil: NetworkConnectionDebugUtil

  @Inject
  lateinit var fakePerformanceMetricsEventLogger: FakePerformanceMetricsEventLogger

  @Inject
  lateinit var oppiaLogger: OppiaLogger

  @Inject
  lateinit var performanceMetricsController: PerformanceMetricsController

  @Inject
  lateinit var metricLogSchedulingWorkerFactory: MetricLogSchedulingWorkerFactory

  @Inject
  lateinit var dataProviders: DataProviders

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @field:[Inject MockPerformanceMetricsEventLogger]
  lateinit var mockPerformanceMetricsEventLogger: PerformanceMetricsEventLogger

  private lateinit var context: Context

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    context = InstrumentationRegistry.getInstrumentation().targetContext
    val config = Configuration.Builder()
      .setExecutor(SynchronousExecutor())
      .setWorkerFactory(metricLogSchedulingWorkerFactory)
      .build()
    WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
  }

  @Test
  fun testWorker_enqueueRequest_verifyStorageUsagePerformanceMetricLogging() {
    val workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext())

    val inputData = Data.Builder().putString(
      MetricLogSchedulingWorker.WORKER_CASE_KEY,
      MetricLogSchedulingWorker.STORAGE_USAGE_WORKER
    ).build()

    val request: OneTimeWorkRequest = OneTimeWorkRequestBuilder<LogUploadWorker>()
      .setInputData(inputData)
      .build()

    workManager.enqueue(request)
    testCoroutineDispatchers.runCurrent()
    val workInfo = workManager.getWorkInfoById(request.id)

    assertThat(workInfo.get().state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(
      fakePerformanceMetricsEventLogger
        .getMostRecentPerformanceMetricsEvent()
        .loggableMetric
        .loggableMetricTypeCase
    ).isEqualTo(OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.STORAGE_USAGE_METRIC)
  }

  @Test
  fun testWorker_enqueueRequest_verifyPeriodicPerformanceMetricsLogging() {
    val workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext())

    val inputData = Data.Builder().putString(
      MetricLogSchedulingWorker.WORKER_CASE_KEY,
      MetricLogSchedulingWorker.PERIODIC_METRIC_WORKER
    ).build()

    val request: OneTimeWorkRequest = OneTimeWorkRequestBuilder<LogUploadWorker>()
      .setInputData(inputData)
      .build()

    workManager.enqueue(request)
    testCoroutineDispatchers.runCurrent()
    val workInfo = workManager.getWorkInfoById(request.id)

    assertThat(workInfo.get().state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(
      fakePerformanceMetricsEventLogger
        .getMostRecentPerformanceMetricsEvent()
        .loggableMetric
        .loggableMetricTypeCase
    ).isEqualTo(OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.NETWORK_USAGE_METRIC)
  }

  @Test
  fun testWorker_enqueueRequest_verifyMemoryUsagePerformanceMetricsLogging() {
    val workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext())

    val inputData = Data.Builder().putString(
      MetricLogSchedulingWorker.WORKER_CASE_KEY,
      MetricLogSchedulingWorker.MEMORY_USAGE_WORKER
    ).build()

    val request: OneTimeWorkRequest = OneTimeWorkRequestBuilder<LogUploadWorker>()
      .setInputData(inputData)
      .build()

    workManager.enqueue(request)
    testCoroutineDispatchers.runCurrent()
    val workInfo = workManager.getWorkInfoById(request.id)

    assertThat(workInfo.get().state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(
      fakePerformanceMetricsEventLogger
        .getMostRecentPerformanceMetricsEvent()
        .loggableMetric
        .loggableMetricTypeCase
    ).isEqualTo(OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.MEMORY_USAGE_METRIC)
  }

  @Test
  fun testWorker_enqueueRequest_writeFails_verifyFailure() {
    val workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext())

    val inputData = Data.Builder().putString(
      MetricLogSchedulingWorker.WORKER_CASE_KEY,
      MetricLogSchedulingWorker.STORAGE_USAGE_WORKER
    ).build()

    val request: OneTimeWorkRequest = OneTimeWorkRequestBuilder<LogUploadWorker>()
      .setInputData(inputData)
      .build()

    setUpPerformanceMetricsEventLoggerToFail()
    workManager.enqueue(request)
    testCoroutineDispatchers.runCurrent()
    val workInfo = workManager.getWorkInfoById(request.id)

    assertThat(workInfo.get().state).isEqualTo(WorkInfo.State.FAILED)
    assertThat(fakePerformanceMetricsEventLogger.noPerformanceMetricsEventsPresent()).isTrue()
  }

  private fun setUpTestApplicationComponent() {
    DaggerMetricLogSchedulingWorkerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  private fun setUpPerformanceMetricsEventLoggerToFail() {
    // Simulate the log attempt itself failing during the job. Note that the reset is necessary here
    // to remove the default stubbing for the mock so that it can properly trigger a failure.
    Mockito.reset(mockPerformanceMetricsEventLogger)
    Mockito.`when`(mockPerformanceMetricsEventLogger.logPerformanceMetric(anyOrNull()))
      .thenThrow(IllegalStateException("Failure."))
  }

  @Qualifier
  annotation class MockPerformanceMetricsEventLogger

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    fun provideContext(application: Application): Context = application

    @Provides
    @Singleton
    @MockPerformanceMetricsEventLogger
    fun bindMockPerformanceMetricsEventLogger(
      fakePerformanceMetricsEventLogger: FakePerformanceMetricsEventLogger
    ): PerformanceMetricsEventLogger {
      return Mockito.mock(PerformanceMetricsEventLogger::class.java).also {
        Mockito.`when`(it.logPerformanceMetric(anyOrNull())).then { answer ->
          fakePerformanceMetricsEventLogger.logPerformanceMetric(
            answer.getArgument(
              /* index= */ 0, /* clazz= */
              OppiaMetricLog::class.java
            )
          )
          return@then null
        }
      }
    }

    @Provides
    fun bindFakeEventLogger(fakeEventLogger: FakeEventLogger): EventLogger = fakeEventLogger

    @Provides
    fun bindFakeExceptionLogger(fakeLogger: FakeExceptionLogger): ExceptionLogger = fakeLogger

    @Provides
    fun bindFakePerformanceMetricsEventLogger(
      @MockPerformanceMetricsEventLogger delegate: PerformanceMetricsEventLogger
    ): PerformanceMetricsEventLogger = delegate
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
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, RobolectricModule::class, TestLogStorageModule::class,
      TestDispatcherModule::class, LogReportWorkerModule::class,
      TestFirebaseLogUploaderModule::class, FakeOppiaClockModule::class,
      NetworkConnectionUtilDebugModule::class, LocaleProdModule::class, LoggerModule::class,
      AssetModule::class, PlatformParameterModule::class, PlatformParameterSingletonModule::class,
      LoggingIdentifierModule::class, SyncStatusTestModule::class, ApplicationLifecycleModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(metricLogSchedulingWorkerTest: MetricLogSchedulingWorkerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerMetricLogSchedulingWorkerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(metricLogSchedulingWorkerTest: MetricLogSchedulingWorkerTest) {
      component.inject(metricLogSchedulingWorkerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
