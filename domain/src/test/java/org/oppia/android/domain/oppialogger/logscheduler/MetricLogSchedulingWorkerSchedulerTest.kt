package org.oppia.android.domain.oppialogger.logscheduler

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.NetworkType.CONNECTED
import androidx.work.WorkInfo
import androidx.work.impl.model.WorkSpec
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import java.util.UUID
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.TestLogReportingModule
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
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.After
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.MEMORY_USAGE_METRIC
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.NETWORK_USAGE_METRIC
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.STORAGE_USAGE_METRIC
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulingWorker.Companion.WORKER_NAME
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulingWorker.Operation.SCHEDULE_LOG_PERIODIC_BACKGROUND_METRICS
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulingWorker.Operation.SCHEDULE_LOG_PERIODIC_UI_METRICS
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulingWorker.Operation.SCHEDULE_LOG_STORAGE_USAGE_METRICS
import org.oppia.android.domain.platformparameter.PlatformParameterControllerInjector
import org.oppia.android.domain.platformparameter.PlatformParameterControllerInjectorProvider
import org.oppia.android.domain.workmanager.OppiaWorker
import org.oppia.android.domain.workmanager.WorkManagerScheduler
import org.oppia.android.domain.workmanager.testing.OppiaWorkManagerTestDriver
import org.oppia.android.domain.workmanager.testing.OppiaWorkManagerTestInitializer
import org.oppia.android.testing.FakePerformanceMetricsEventLogger
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule.Companion.forcePerformanceMetricsCollectionHighFrequencyTimeIntervalInMinutes
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule.Companion.forcePerformanceMetricsCollectionLowFrequencyTimeIntervalInMinutes
import org.oppia.android.util.threading.BackgroundDispatcher
import org.oppia.android.util.threading.DispatcherInjector
import org.oppia.android.util.threading.DispatcherInjectorProvider

/** Tests for [MetricLogSchedulingWorkerScheduler]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = MetricLogSchedulingWorkerSchedulerTest.TestApplication::class)
class MetricLogSchedulingWorkerSchedulerTest {
  @Inject lateinit var context: Context
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var workManagerScheduler: WorkManagerScheduler
  @Inject lateinit var metricLogSchedulingWorkerScheduler: MetricLogSchedulingWorkerScheduler
  @Inject lateinit var oppiaWorkManagerTestInitializer: OppiaWorkManagerTestInitializer
  @Inject lateinit var fakePerformanceMetricsEventLogger: FakePerformanceMetricsEventLogger
  @Inject lateinit var testDriver: OppiaWorkManagerTestDriver
  @field:[Inject BackgroundDispatcher] lateinit var backgroundDispatcher: CoroutineDispatcher

  @After
  fun tearDown() {
    TestPlatformParameterModule.reset()
  }

  @Test
  fun testMetricLogSchedulingWorker_hasThreeOperationTypes() {
    setUpTestApplicationComponent()

    // A change detector test that, if failing, means that other tests in this suite need to be
    // updated to ensure that the new operation type is properly verified.
    assertThat(MetricLogSchedulingWorker.Operation.values().toList()).containsExactly(
      SCHEDULE_LOG_PERIODIC_BACKGROUND_METRICS,
      SCHEDULE_LOG_PERIODIC_UI_METRICS,
      SCHEDULE_LOG_STORAGE_USAGE_METRICS
    )
  }

  @Test
  fun testScheduleWork_schedulesMetricLogSchedulingWorkerForPeriodicBackgroundMetrics() {
    forcePerformanceMetricsCollectionHighFrequencyTimeIntervalInMinutes(100)
    forcePerformanceMetricsCollectionLowFrequencyTimeIntervalInMinutes(200)
    setUpTestApplicationComponent()
    initializeDependencies()

    metricLogSchedulingWorkerScheduler.scheduleWork(workManagerScheduler)

    // Verify that the job was scheduled correctly and uses the high-frequency time.
    val id = testDriver.findUniqueId(WORKER_NAME, SCHEDULE_LOG_PERIODIC_BACKGROUND_METRICS)
    val workInfo = testDriver.lookUpWorkInfo(id)
    assertThat(workInfo?.state).isEqualTo(WorkInfo.State.ENQUEUED)
    assertThat(lookUpWorkSpec(id)?.intervalDuration).isEqualTo(TimeUnit.MINUTES.toMillis(100))
    assertThat(lookUpWorkSpec(id)?.constraints?.requiredNetworkType).isEqualTo(CONNECTED)
  }

  @Test
  fun testScheduleWork_schedulesMetricLogSchedulingWorkerForPeriodicUiMetrics() {
    forcePerformanceMetricsCollectionHighFrequencyTimeIntervalInMinutes(300)
    forcePerformanceMetricsCollectionLowFrequencyTimeIntervalInMinutes(400)
    setUpTestApplicationComponent()
    initializeDependencies()

    metricLogSchedulingWorkerScheduler.scheduleWork(workManagerScheduler)

    // Verify that the job was scheduled correctly and uses the high-frequency time.
    val id = testDriver.findUniqueId(WORKER_NAME, SCHEDULE_LOG_PERIODIC_UI_METRICS)
    val workInfo = testDriver.lookUpWorkInfo(id)
    assertThat(workInfo?.state).isEqualTo(WorkInfo.State.ENQUEUED)
    assertThat(lookUpWorkSpec(id)?.intervalDuration).isEqualTo(TimeUnit.MINUTES.toMillis(300))
    assertThat(lookUpWorkSpec(id)?.constraints?.requiredNetworkType).isEqualTo(CONNECTED)
  }

  @Test
  fun testScheduleWork_schedulesMetricLogSchedulingWorkerForStorageUsageMetrics() {
    forcePerformanceMetricsCollectionHighFrequencyTimeIntervalInMinutes(500)
    forcePerformanceMetricsCollectionLowFrequencyTimeIntervalInMinutes(600)
    setUpTestApplicationComponent()
    initializeDependencies()

    metricLogSchedulingWorkerScheduler.scheduleWork(workManagerScheduler)

    // Verify that the job was scheduled correctly and uses the low-frequency time.s
    val id = testDriver.findUniqueId(WORKER_NAME, SCHEDULE_LOG_STORAGE_USAGE_METRICS)
    val workInfo = testDriver.lookUpWorkInfo(id)
    assertThat(workInfo?.state).isEqualTo(WorkInfo.State.ENQUEUED)
    assertThat(lookUpWorkSpec(id)?.intervalDuration).isEqualTo(TimeUnit.MINUTES.toMillis(600))
    assertThat(lookUpWorkSpec(id)?.constraints?.requiredNetworkType).isEqualTo(CONNECTED)
  }

  @Test
  fun testScheduleWork_constraintsMet_runsThreeJobs() {
    TestPlatformParameterModule.forceEnablePerformanceMetricsCollection(true)
    forcePerformanceMetricsCollectionHighFrequencyTimeIntervalInMinutes(100)
    forcePerformanceMetricsCollectionLowFrequencyTimeIntervalInMinutes(150)
    setUpTestApplicationComponent()
    initializeDependencies()
    metricLogSchedulingWorkerScheduler.scheduleWork(workManagerScheduler)

    testDriver.forceConstraintsMet(findUniqueId(SCHEDULE_LOG_PERIODIC_BACKGROUND_METRICS))
    testDriver.forceConstraintsMet(findUniqueId(SCHEDULE_LOG_PERIODIC_UI_METRICS))
    testDriver.forceConstraintsMet(findUniqueId(SCHEDULE_LOG_STORAGE_USAGE_METRICS))
    testCoroutineDispatchers.runCurrent()

    // Order cannot be easily checked here, so just verify that they ran.
    val logCount = fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    assertThat(logCount).isEqualTo(3)
    val loggedEvents = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvents(3)
    assertThat(loggedEvents.map { it.loggableMetric.loggableMetricTypeCase })
      .containsExactly(NETWORK_USAGE_METRIC, MEMORY_USAGE_METRIC, STORAGE_USAGE_METRIC)
  }

  @Test
  fun testScheduleWork_constraintsMet_fifteenAndSixtyMinIncrements_wait35_runsSevenJobs() {
    TestPlatformParameterModule.forceEnablePerformanceMetricsCollection(true)
    forcePerformanceMetricsCollectionHighFrequencyTimeIntervalInMinutes(15)
    forcePerformanceMetricsCollectionLowFrequencyTimeIntervalInMinutes(60)
    setUpTestApplicationComponent()
    initializeDependencies()
    metricLogSchedulingWorkerScheduler.scheduleWork(workManagerScheduler)

    testDriver.forceConstraintsMet(findUniqueId(SCHEDULE_LOG_PERIODIC_BACKGROUND_METRICS))
    testDriver.forceConstraintsMet(findUniqueId(SCHEDULE_LOG_PERIODIC_UI_METRICS))
    testDriver.forceConstraintsMet(findUniqueId(SCHEDULE_LOG_STORAGE_USAGE_METRICS))
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.MINUTES.toMillis(35))

    // Advancing 35 minutes means the high frequency jobs (2) each ran 3 times (they run immediately
    // upon being scheduled), and the low frequency job (1) only ran 1 time since it was scheduled.
    val logCount = fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    assertThat(logCount).isEqualTo(7)
    val loggedEvents = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvents(7)
    assertThat(loggedEvents.map { it.loggableMetric.loggableMetricTypeCase })
      .containsExactly(
        NETWORK_USAGE_METRIC,
        NETWORK_USAGE_METRIC,
        NETWORK_USAGE_METRIC,
        MEMORY_USAGE_METRIC,
        MEMORY_USAGE_METRIC,
        MEMORY_USAGE_METRIC,
        STORAGE_USAGE_METRIC
      )
  }

  private fun lookUpWorkSpec(id: UUID): WorkSpec? = testDriver.lookUpWorkSpec(id)

  private fun findUniqueId(taskType: OppiaWorker.TaskType): UUID =
    testDriver.findUniqueId(WORKER_NAME, taskType)

  private fun initializeDependencies() {
    FirebaseApp.initializeApp(context)
    oppiaWorkManagerTestInitializer.initializeWorkManager()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
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
      ApplicationLifecycleModule::class,
      AssetModule::class,
      CpuPerformanceSnapshotterModule::class,
      FakeOppiaClockModule::class,
      LocaleProdModule::class,
      LogStorageModule::class,
      LoggerModule::class,
      LoggingIdentifierModule::class,
      MetricLogSchedulerModule::class,
      NetworkConnectionUtilDebugModule::class,
      PerformanceMetricsConfigurationsModule::class,
      PlatformParameterSingletonModule::class,
      RobolectricModule::class,
      SyncStatusTestModule::class,
      TestDispatcherModule::class,
      TestLogReportingModule::class,
      TestModule::class,
      TestAuthenticationModule::class,
      TestPlatformParameterModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector, DispatcherInjector, PlatformParameterControllerInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(metricLogSchedulingWorkerSchedulerTest: MetricLogSchedulingWorkerSchedulerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider, DispatcherInjectorProvider, PlatformParameterControllerInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerMetricLogSchedulingWorkerSchedulerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(metricLogSchedulingWorkerSchedulerTest: MetricLogSchedulingWorkerSchedulerTest) {
      component.inject(metricLogSchedulingWorkerSchedulerTest)
    }

    override fun getDataProvidersInjector() = component
    override fun getPlatformParameterControllerInjector() = component
    override fun getDispatcherInjector(): DispatcherInjector = component
  }
}
