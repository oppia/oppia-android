package org.oppia.android.domain.oppialogger.loguploader

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.NetworkType.CONNECTED
import androidx.work.WorkInfo
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.OppiaMetricLog
import org.oppia.android.app.model.OppiaMetricLog.Priority.HIGH_PRIORITY
import org.oppia.android.app.model.ScreenName.HOME_ACTIVITY
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.analytics.AnalyticsController
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.oppialogger.analytics.FirestoreDataController
import org.oppia.android.domain.oppialogger.analytics.PerformanceMetricsController
import org.oppia.android.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorker.Companion.WORKER_NAME
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorker.Operation.UPLOAD_EVENTS
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorker.Operation.UPLOAD_EXCEPTIONS
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorker.Operation.UPLOAD_FIRESTORE_DATA
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorker.Operation.UPLOAD_PERFORMANCE_METRICS
import org.oppia.android.domain.platformparameter.PlatformParameterControllerInjector
import org.oppia.android.domain.platformparameter.PlatformParameterControllerInjectorProvider
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.workmanager.WorkManagerScheduler
import org.oppia.android.domain.workmanager.testing.OppiaWorkManagerTestDriver
import org.oppia.android.testing.FakeAnalyticsEventLogger
import org.oppia.android.testing.FakeExceptionLogger
import org.oppia.android.testing.FakeFirestoreEventLogger
import org.oppia.android.testing.FakePerformanceMetricsEventLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
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
import org.oppia.android.util.networking.NetworkConnectionDebugUtil
import org.oppia.android.util.networking.NetworkConnectionUtil.ProdConnectionStatus.CELLULAR
import org.oppia.android.util.networking.NetworkConnectionUtil.ProdConnectionStatus.NONE
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.system.OppiaClock
import org.oppia.android.util.threading.BackgroundDispatcher
import org.oppia.android.util.threading.DispatcherInjector
import org.oppia.android.util.threading.DispatcherInjectorProvider
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowLog
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [LogReportWorkerScheduler]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = LogReportWorkerSchedulerTest.TestApplication::class)
// FunctionName: test names are conventionally named with underscores.
// SameParameterValue: tests should have specific context included/excluded for readability.
@Suppress("FunctionName", "SameParameterValue")
class LogReportWorkerSchedulerTest {
  @Inject lateinit var context: Context
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var workManagerScheduler: WorkManagerScheduler
  @Inject lateinit var logReportWorkerScheduler: LogReportWorkerScheduler
  @Inject lateinit var testDriver: OppiaWorkManagerTestDriver
  @Inject lateinit var oppiaLogger: OppiaLogger
  @Inject lateinit var oppiaClock: OppiaClock
  @Inject lateinit var analyticsController: AnalyticsController
  @Inject lateinit var exceptionsController: ExceptionsController
  @Inject lateinit var performanceMetricsController: PerformanceMetricsController
  @Inject lateinit var firestoreDataController: FirestoreDataController
  @Inject lateinit var networkConnectionUtil: NetworkConnectionDebugUtil
  @Inject lateinit var fakeAnalyticsEventLogger: FakeAnalyticsEventLogger
  @Inject lateinit var fakeExceptionLogger: FakeExceptionLogger
  @Inject lateinit var fakePerformanceMetricsEventLogger: FakePerformanceMetricsEventLogger
  @Inject lateinit var fakeFirestoreEventLogger: FakeFirestoreEventLogger
  @field:[Inject BackgroundDispatcher] lateinit var backgroundDispatcher: CoroutineDispatcher

  @After
  fun tearDown() {
    TestPlatformParameterModule.reset()
  }

  @Test
  fun testLogUploadWorker_hasFourOperationTypes() {
    setUpTestApplicationComponent()

    // A change detector test that, if failing, means that other tests in this suite need to be
    // updated to ensure that the new operation type is properly verified.
    assertThat(LogUploadWorker.Operation.values().toList()).containsExactly(
      UPLOAD_EVENTS, UPLOAD_EXCEPTIONS, UPLOAD_PERFORMANCE_METRICS, UPLOAD_FIRESTORE_DATA
    )
  }

  @Test
  fun testScheduleWork_schedulesLogUploadWorkerToUploadEvents() {
    setUpTestApplicationComponent()
    initializeDependencies()

    logReportWorkerScheduler.scheduleWork(workManagerScheduler)

    // Verify that the job was scheduled correctly.
    val monitor = testDriver.lookUpPeriodicMonitor(WORKER_NAME, UPLOAD_EVENTS)
    assertThat(monitor.state).isEqualTo(WorkInfo.State.ENQUEUED)
    assertThat(monitor.intervalDurationMs).isEqualTo(TimeUnit.HOURS.toMillis(6))
    assertThat(monitor.requiredNetworkType).isEqualTo(CONNECTED)
  }

  @Test
  fun testScheduleWork_schedulesLogUploadWorkerToUploadExceptions() {
    setUpTestApplicationComponent()
    initializeDependencies()

    logReportWorkerScheduler.scheduleWork(workManagerScheduler)

    // Verify that the job was scheduled correctly.
    val monitor = testDriver.lookUpPeriodicMonitor(WORKER_NAME, UPLOAD_EXCEPTIONS)
    assertThat(monitor.state).isEqualTo(WorkInfo.State.ENQUEUED)
    assertThat(monitor.intervalDurationMs).isEqualTo(TimeUnit.HOURS.toMillis(6))
    assertThat(monitor.requiredNetworkType).isEqualTo(CONNECTED)
  }

  @Test
  fun testScheduleWork_schedulesLogUploadWorkerToUploadPerformanceMetrics() {
    setUpTestApplicationComponent()
    initializeDependencies()

    logReportWorkerScheduler.scheduleWork(workManagerScheduler)

    // Verify that the job was scheduled correctly.
    val monitor = testDriver.lookUpPeriodicMonitor(WORKER_NAME, UPLOAD_PERFORMANCE_METRICS)
    assertThat(monitor.state).isEqualTo(WorkInfo.State.ENQUEUED)
    assertThat(monitor.intervalDurationMs).isEqualTo(TimeUnit.HOURS.toMillis(6))
    assertThat(monitor.requiredNetworkType).isEqualTo(CONNECTED)
  }

  @Test
  fun testScheduleWork_schedulesLogUploadWorkerToUploadFirestoreData() {
    setUpTestApplicationComponent()
    initializeDependencies()

    logReportWorkerScheduler.scheduleWork(workManagerScheduler)

    // Verify that the job was scheduled correctly.
    val monitor = testDriver.lookUpPeriodicMonitor(WORKER_NAME, UPLOAD_FIRESTORE_DATA)
    assertThat(monitor.state).isEqualTo(WorkInfo.State.ENQUEUED)
    assertThat(monitor.intervalDurationMs).isEqualTo(TimeUnit.HOURS.toMillis(6))
    assertThat(monitor.requiredNetworkType).isEqualTo(CONNECTED)
  }

  @Test
  fun testScheduleWork_constraintsMet_runsFourJobs() {
    setUpTestApplicationComponent()
    initializeDependencies()
    logOneEventPerType()
    logReportWorkerScheduler.scheduleWork(workManagerScheduler)

    forceConstraintsMet(UPLOAD_EVENTS)
    forceConstraintsMet(UPLOAD_EXCEPTIONS)
    forceConstraintsMet(UPLOAD_PERFORMANCE_METRICS)
    forceConstraintsMet(UPLOAD_FIRESTORE_DATA)
    testCoroutineDispatchers.runCurrent()

    // All of the jobs should've run and uploading the logged metrics.
    assertThat(fetchWorkerErrorLogs()).isEmpty() // No job failures should have occurred.
    assertThat(fakeAnalyticsEventLogger.getEventListCount()).isEqualTo(1)
    assertThat(fakeExceptionLogger.getExceptionCount()).isEqualTo(1)
    assertThat(fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()).isEqualTo(1)
    assertThat(fakeFirestoreEventLogger.getEventListCount()).isEqualTo(1)
  }

  @Test
  fun testScheduleWork_constraintsMet_tenHoursElapsed_runsEightJobs() {
    setUpTestApplicationComponent()
    initializeDependencies()
    logOneEventPerType() // Log events right away for the immediate job runs.
    logOneEventEveryHourIsh()
    logReportWorkerScheduler.scheduleWork(workManagerScheduler)

    forceConstraintsMet(UPLOAD_EVENTS)
    forceConstraintsMet(UPLOAD_EXCEPTIONS)
    forceConstraintsMet(UPLOAD_PERFORMANCE_METRICS)
    forceConstraintsMet(UPLOAD_FIRESTORE_DATA)
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.HOURS.toMillis(10))

    // There are initially 1 set of events logged per job type, then one every hour. However, the
    // jobs are scheduled to run every 6 hours (with 1 running immediately). This means although 11
    // total events will be cached for logging, only 7 will actually be uploaded across the 2 runs.
    assertThat(fetchWorkerErrorLogs()).isEmpty() // No job failures should have occurred.
    assertThat(fakeAnalyticsEventLogger.getEventListCount()).isEqualTo(7)
    assertThat(fakeExceptionLogger.getExceptionCount()).isEqualTo(7)
    assertThat(fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()).isEqualTo(7)
    assertThat(fakeFirestoreEventLogger.getEventListCount()).isEqualTo(7)
  }

  private fun logOneEventPerType() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE) // For offline caching.
    analyticsController.logImportantEvent(oppiaLogger.createOpenHomeContext(), profileId = null)
    exceptionsController.logNonFatalException(Exception("test"))
    performanceMetricsController.logPerformanceMetricsEvent(
      oppiaClock.getCurrentTimeMs(),
      HOME_ACTIVITY,
      createStartupLatencyMetric(startMs = 123),
      HIGH_PRIORITY
    )
    firestoreDataController.logEvent(
      createOptionalSurveyResponseContext("feedback 1", "test_survey_id1"), profileId = null
    )

    // Note that the order is important here. Logs must be synchronized before restoring
    // connectivity to ensure that they're cached and not immediately uploaded. This also means that
    // this helper method cannot be called except on the main thread.
    testCoroutineDispatchers.runCurrent()
    networkConnectionUtil.setCurrentConnectionStatus(CELLULAR)
  }

  private fun logOneEventEveryHourIsh() {
    CoroutineScope(backgroundDispatcher).launch {
      // Note that the '- 1' here is very important. The logs need to be logged just shy of the hour
      // mark to make sure that logOneEventPerType() can restore network connectivity BEFORE the
      // job has time to run otherwise it will run in non-connectivity mode and fail.
      delay(TimeUnit.HOURS.toMillis(1) - 1)
      // Hop to main thread so that coroutines can be synchronized.
      withContext(Dispatchers.Main) {
        logOneEventPerType()
        logOneEventEveryHourIsh()
      }
    }
  }

  private fun forceConstraintsMet(taskType: LogUploadWorker.Operation) {
    testDriver.lookUpPeriodicMonitor(WORKER_NAME, taskType).forceConstraintsMet()
  }

  private fun createStartupLatencyMetric(startMs: Long): OppiaMetricLog.LoggableMetric {
    return OppiaMetricLog.LoggableMetric.newBuilder().apply {
      startupLatencyMetric = OppiaMetricLog.StartupLatencyMetric.newBuilder().apply {
        startupLatencyMillis = startMs
      }.build()
    }.build()
  }

  private fun createOptionalSurveyResponseContext(
    feedback: String,
    surveyId: String
  ): EventLog.Context {
    return EventLog.Context.newBuilder().apply {
      optionalResponse = EventLog.OptionalSurveyResponseContext.newBuilder().apply {
        feedbackAnswer = feedback
        surveyDetails = EventLog.SurveyResponseContext.newBuilder().apply {
          this.surveyId = surveyId
        }.build()
      }.build()
    }.build()
  }

  private fun fetchWorkerErrorLogs(): List<String> {
    // Extract all logs from the bootstrap worker and validate they are each errors before returning
    // the logged message lines.
    return ShadowLog.getLogs().filter { it.tag == WORKER_NAME }.onEach {
      assertThat(it.type).isEqualTo(Log.ERROR)
    }.map(ShadowLog.LogItem::msg)
  }

  private fun initializeDependencies() {
    FirebaseApp.initializeApp(context)
    testDriver.initializeWorkManager()
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
      LogReportWorkerModule::class,
      NetworkConnectionUtilDebugModule::class,
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
  interface TestApplicationComponent :
    DataProvidersInjector, DispatcherInjector, PlatformParameterControllerInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(logUploadWorkRequestTest: LogReportWorkerSchedulerTest)
  }

  class TestApplication :
    Application(),
    DataProvidersInjectorProvider,
    DispatcherInjectorProvider,
    PlatformParameterControllerInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerLogReportWorkerSchedulerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: LogReportWorkerSchedulerTest) {
      component.inject(test)
    }

    override fun getDataProvidersInjector() = component
    override fun getDispatcherInjector() = component
    override fun getPlatformParameterControllerInjector() = component
  }
}
