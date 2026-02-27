package org.oppia.android.domain.oppialogger.loguploader

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
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.OppiaMetricLog
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
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
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsConfigurationsModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPTIONAL_RESPONSE
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.APK_SIZE_METRIC
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.STARTUP_LATENCY_METRIC
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.STORAGE_USAGE_METRIC
import org.oppia.android.app.model.OppiaMetricLog.Priority
import org.oppia.android.app.model.OppiaMetricLog.Priority.HIGH_PRIORITY
import org.oppia.android.app.model.OppiaMetricLog.Priority.LOW_PRIORITY
import org.oppia.android.app.model.ScreenName
import org.oppia.android.app.model.ScreenName.BACKGROUND_SCREEN
import org.oppia.android.app.model.ScreenName.HOME_ACTIVITY
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.analytics.AnalyticsController
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
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.domain.workmanager.testing.OppiaWorkManagerTestDriver
import org.oppia.android.testing.FakeAnalyticsEventLogger
import org.oppia.android.testing.FakeExceptionLogger
import org.oppia.android.testing.FakeFirestoreEventLogger
import org.oppia.android.testing.FakePerformanceMetricsEventLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.util.threading.BackgroundDispatcher
import org.oppia.android.testing.logging.EventLogSubject.Companion.assertThat
import org.oppia.android.testing.logging.TestSyncStatusManager
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.DATA_UPLOADED
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.DATA_UPLOADING
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.INITIAL_UNKNOWN
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.NO_CONNECTIVITY
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.UPLOAD_ERROR
import org.oppia.android.util.networking.NetworkConnectionDebugUtil
import org.oppia.android.util.networking.NetworkConnectionUtil.ProdConnectionStatus
import org.oppia.android.util.system.OppiaClock
import org.oppia.android.util.threading.DispatcherInjector
import org.oppia.android.util.threading.DispatcherInjectorProvider
import org.robolectric.shadows.ShadowLog

private const val TEST_TOPIC_ID1 = "test_topic_id1"
private const val TEST_TOPIC_ID2 = "test_topic_id2"

/** Tests for [LogUploadWorker]. */
// FunctionName: test names are conventionally named with underscores.
// SameParameterValue: tests should have specific context included/excluded for readability.
@Suppress("FunctionName", "SameParameterValue")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = LogUploadWorkerTest.TestApplication::class)
class LogUploadWorkerTest {
  @Inject lateinit var context: Context
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var configuration: Configuration
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
  @Inject lateinit var testSyncStatusManager: TestSyncStatusManager
  @field:[Inject BackgroundDispatcher] lateinit var backgroundDispatcher: CoroutineDispatcher

  private val testException1 = Exception("Test 1")
  private val testException2 = Exception("Test 2")
  private val testException3 = Exception("Test 3")

  @After
  fun tearDown() {
    TestPlatformParameterModule.reset()
  }

  /* Tests for UPLOAD_EVENTS. */

  @Test
  fun testWorker_scheduleUploadEvents_sanityCheck_doNotRun_noLogsUploaded() {
    setUpTestApplicationComponent()
    initializeDependencies()
    forceNetworkConnectivityOff() // For offline caching.

    logImportantEvent(oppiaLogger.createOpenHomeContext())
    logLowPriorityEvent(oppiaLogger.createOpenPracticeTabContext(TEST_TOPIC_ID1))
    logImportantEvent(oppiaLogger.createOpenInfoTabContext(TEST_TOPIC_ID2))

    // Sanity check: ensure that logs do not automatically upload without the job running (otherwise
    // the other tests in this test suite are moot).
    assertThat(fakeAnalyticsEventLogger.getEventListCount()).isEqualTo(0)
    assertThat(getLatestSyncStatus()).isEqualTo(NO_CONNECTIVITY)
  }

  @Test
  fun testWorker_scheduleUploadEvents_noEvents_uploadsNothing() {
    setUpTestApplicationComponent()
    initializeDependencies()

    forceNetworkConnectivityToCellular() // For the job to run.
    val monitor = testDriver.runOneOffWork(WORKER_NAME, UPLOAD_EVENTS)

    // Verify that the job succeeded and there was nothing to upload. Sync status will also update
    // since the attempt to upload finished.
    val logCount = fakeAnalyticsEventLogger.getEventListCount()
    assertThat(monitor.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(logCount).isEqualTo(0)
    assertThat(getLatestSyncStatus()).isEqualTo(DATA_UPLOADED)
  }

  @Test
  fun testWorker_scheduleUploadEvents_severalEvents_uploadsAllEvents() {
    setUpTestApplicationComponent()
    initializeDependencies()
    forceNetworkConnectivityOff() // For offline caching.
    logImportantEvent(oppiaLogger.createOpenHomeContext())
    logLowPriorityEvent(oppiaLogger.createOpenPracticeTabContext(TEST_TOPIC_ID1))
    logImportantEvent(oppiaLogger.createOpenInfoTabContext(TEST_TOPIC_ID2))

    forceNetworkConnectivityToCellular() // For the job to run.
    val monitor = testDriver.runOneOffWork(WORKER_NAME, UPLOAD_EVENTS)

    // Verify that the job succeeded, was the only job to upload something, and uploaded correctly.
    val logCount = fakeAnalyticsEventLogger.getEventListCount()
    val events = fakeAnalyticsEventLogger.getMostRecentEvents(3)
    assertThat(monitor.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(logCount).isEqualTo(3)
    assertThat(events[0]).hasOpenHomeContext()
    assertThat(events[0]).isEssentialPriority()
    assertThat(events[1]).hasOpenPracticeTabContextThat().hasTopicIdThat().isEqualTo(TEST_TOPIC_ID1)
    assertThat(events[1]).isOptionalPriority()
    assertThat(events[2]).hasOpenInfoTabContextThat().hasTopicIdThat().isEqualTo(TEST_TOPIC_ID2)
    assertThat(events[2]).isEssentialPriority()
    assertThat(getLatestSyncStatus()).isEqualTo(DATA_UPLOADED)
    assertThat(testSyncStatusManager.getSyncStatuses())
      .containsExactly(INITIAL_UNKNOWN, NO_CONNECTIVITY, DATA_UPLOADING, DATA_UPLOADED)
      .inOrder()
  }

  @Test
  fun testWorker_scheduleUploadEvents_severalEvents_runAgain_uploadsNothing() {
    setUpTestApplicationComponent()
    initializeDependencies()
    forceNetworkConnectivityOff() // For offline caching.
    logImportantEvent(oppiaLogger.createOpenHomeContext())
    logLowPriorityEvent(oppiaLogger.createOpenPracticeTabContext(TEST_TOPIC_ID1))
    logImportantEvent(oppiaLogger.createOpenInfoTabContext(TEST_TOPIC_ID2))
    forceNetworkConnectivityToCellular() // For the job to run.
    testDriver.runOneOffWork(WORKER_NAME, UPLOAD_EVENTS)
    fakeAnalyticsEventLogger.clearAllEvents() // Reset for next job.

    // Run a second time.
    val monitor = testDriver.runOneOffWork(WORKER_NAME, UPLOAD_EVENTS)

    // Verify nothing uploaded (since everything should've uploaded during the first job).
    val logCount = fakeAnalyticsEventLogger.getEventListCount()
    assertThat(monitor.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(logCount).isEqualTo(0)
  }

  @Test
  fun testWorker_scheduleUploadEvents_hasFailure_failsAndLogsErrorAndReportsSyncStatus() {
    setUpTestApplicationComponent()
    initializeDependencies()
    forceNetworkConnectivityOff() // For offline caching.
    logImportantEvent(oppiaLogger.createOpenHomeContext())
    fakeAnalyticsEventLogger.setFailure(Exception("Forced failure."))

    forceNetworkConnectivityToCellular() // For the job to run.
    val monitor = testDriver.runOneOffWork(WORKER_NAME, UPLOAD_EVENTS)

    // Verify that the job failed and an error was logged due to an underlying failure.
    val logCount = fakeAnalyticsEventLogger.getEventListCount()
    val failureLine = fetchSingleWorkerErrorLog()
    assertThat(monitor.state).isEqualTo(WorkInfo.State.FAILED)
    assertThat(logCount).isEqualTo(0)
    assertThat(failureLine).contains("Failed operation: upload_events.")
    assertThat(failureLine).contains("java.lang.Exception: Forced failure.")
    assertThat(getLatestSyncStatus()).isEqualTo(UPLOAD_ERROR)
  }

  @Test
  fun testWorker_scheduleUploadEvents_losesConnectivity_failsAndLogsErrorAndReportsSyncStatus() {
    setUpTestApplicationComponent()
    initializeDependencies()
    forceNetworkConnectivityOff() // For offline caching.
    logImportantEvent(oppiaLogger.createOpenHomeContext())

    // Run the job with connectivity disabled. This is slightly contrived because technically the
    // job wouldn't start, but it has the rough effect of simulating connectivity being lost after
    // the job kicks off.
    val monitor = testDriver.runOneOffWork(WORKER_NAME, UPLOAD_EVENTS)

    // Verify that the job failed and an error was logged due to an underlying failure.
    val logCount = fakeAnalyticsEventLogger.getEventListCount()
    val failureLine = fetchSingleWorkerErrorLog()
    assertThat(monitor.state).isEqualTo(WorkInfo.State.FAILED)
    assertThat(logCount).isEqualTo(0)
    assertThat(failureLine).contains("Failed operation: upload_events.")
    assertThat(failureLine).contains("Cannot upload events without internet connectivity.")
    assertThat(getLatestSyncStatus()).isEqualTo(NO_CONNECTIVITY)
  }

  /* Tests for UPLOAD_EXCEPTIONS. */

  @Test
  fun testWorker_scheduleUploadExceptions_sanityCheck_doNotRun_noExceptionsUploaded() {
    setUpTestApplicationComponent()
    initializeDependencies()
    forceNetworkConnectivityOff() // For offline caching.

    logNonFatalException(testException1)
    logFatalException(testException2)
    logNonFatalException(testException3)

    // Sanity check: ensure that logs do not automatically upload without the job running (otherwise
    // the other tests in this test suite are moot).
    assertThat(fakeExceptionLogger.getExceptionCount()).isEqualTo(0)
    expectSyncStatusToBeUnchanged()
  }

  @Test
  fun testWorker_scheduleUploadExceptions_noExceptions_uploadsNothing() {
    setUpTestApplicationComponent()
    initializeDependencies()

    forceNetworkConnectivityToCellular() // For the job to run.
    val monitor = testDriver.runOneOffWork(WORKER_NAME, UPLOAD_EXCEPTIONS)

    // Verify that the job succeeded and there was nothing to upload. Sync status will also update
    // since the attempt to upload finished.
    val logCount = fakeExceptionLogger.getExceptionCount()
    assertThat(monitor.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(logCount).isEqualTo(0)
    expectSyncStatusToBeUnchanged()
  }

  @Test
  fun testWorker_scheduleUploadExceptions_severalExceptions_uploadsAllExceptions() {
    setUpTestApplicationComponent()
    initializeDependencies()
    forceNetworkConnectivityOff() // For offline caching.
    logNonFatalException(testException1)
    logFatalException(testException2)
    logNonFatalException(testException3)

    forceNetworkConnectivityToCellular() // For the job to run.
    val monitor = testDriver.runOneOffWork(WORKER_NAME, UPLOAD_EXCEPTIONS)

    // Verify that the job succeeded, was the only job to upload something, and uploaded correctly.
    val logCount = fakeExceptionLogger.getExceptionCount()
    val exceptions = fakeExceptionLogger.getMostRecentExceptions(3)
    assertThat(monitor.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(logCount).isEqualTo(3)
    assertThat(exceptions[0]).hasMessageThat().isEqualTo("Test 1")
    assertThat(exceptions[1]).hasMessageThat().isEqualTo("Test 2")
    assertThat(exceptions[2]).hasMessageThat().isEqualTo("Test 3")
    assertThat(exceptions[0].toComparableTrace()).isEqualTo(testException1.toComparableTrace())
    assertThat(exceptions[1].toComparableTrace()).isEqualTo(testException2.toComparableTrace())
    assertThat(exceptions[2].toComparableTrace()).isEqualTo(testException3.toComparableTrace())
    expectSyncStatusToBeUnchanged()
  }

  @Test
  fun testWorker_scheduleUploadExceptions_severalExceptions_runAgain_uploadsNothing() {
    setUpTestApplicationComponent()
    initializeDependencies()
    forceNetworkConnectivityOff() // For offline caching.
    logNonFatalException(testException1)
    logFatalException(testException2)
    logNonFatalException(testException3)
    forceNetworkConnectivityToCellular() // For the job to run.
    testDriver.runOneOffWork(WORKER_NAME, UPLOAD_EXCEPTIONS)
    fakeExceptionLogger.clearAllExceptions() // Reset for next job.

    // Run a second time.
    val monitor = testDriver.runOneOffWork(WORKER_NAME, UPLOAD_EXCEPTIONS)

    // Verify nothing uploaded (since everything should've uploaded during the first job).
    val logCount = fakeExceptionLogger.getExceptionCount()
    assertThat(monitor.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(logCount).isEqualTo(0)
    expectSyncStatusToBeUnchanged()
  }

  @Test
  fun testWorker_scheduleUploadExceptions_hasFailure_failsAndLogsError() {
    setUpTestApplicationComponent()
    initializeDependencies()
    forceNetworkConnectivityOff() // For offline caching.
    logNonFatalException(testException1)
    fakeExceptionLogger.setFailure(Exception("Forced failure."))

    forceNetworkConnectivityToCellular() // For the job to run.
    val monitor = testDriver.runOneOffWork(WORKER_NAME, UPLOAD_EXCEPTIONS)

    // Verify that the job failed and an error was logged due to an underlying failure.
    val logCount = fakeExceptionLogger.getExceptionCount()
    val failureLine = fetchSingleWorkerErrorLog()
    assertThat(monitor.state).isEqualTo(WorkInfo.State.FAILED)
    assertThat(logCount).isEqualTo(0)
    assertThat(failureLine).contains("Failed operation: upload_exceptions.")
    assertThat(failureLine).contains("java.lang.Exception: Forced failure.")
    expectSyncStatusToBeUnchanged()
  }

  @Test
  fun testWorker_scheduleUploadExceptions_losesConnectivity_failsAndLogsError() {
    setUpTestApplicationComponent()
    initializeDependencies()
    forceNetworkConnectivityOff() // For offline caching.
    logNonFatalException(testException1)

    // Run the job with connectivity disabled. This is slightly contrived because technically the
    // job wouldn't start, but it has the rough effect of simulating connectivity being lost after
    // the job kicks off.
    val monitor = testDriver.runOneOffWork(WORKER_NAME, UPLOAD_EXCEPTIONS)

    // Verify that the job failed and an error was logged due to an underlying failure.
    val logCount = fakeExceptionLogger.getExceptionCount()
    val failureLine = fetchSingleWorkerErrorLog()
    assertThat(monitor.state).isEqualTo(WorkInfo.State.FAILED)
    assertThat(logCount).isEqualTo(0)
    assertThat(failureLine).contains("Failed operation: upload_exceptions.")
    assertThat(failureLine).contains("Cannot upload exceptions without internet connectivity.")
    expectSyncStatusToBeUnchanged()
  }

  /* Tests for UPLOAD_PERFORMANCE_METRICS. */

  @Test
  fun testWorker_scheduleUploadPerfMetrics_sanityCheck_doNotRun_noMetricsUploaded() {
    setUpTestApplicationComponent()
    initializeDependencies()
    forceNetworkConnectivityOff() // For offline caching.

    logPerformanceMetric(createStartupLatencyMetric(startMs = 123), HOME_ACTIVITY, HIGH_PRIORITY)
    logPerformanceMetric(createApkSizeMetric(sizeBytes = 456), BACKGROUND_SCREEN, LOW_PRIORITY)
    logPerformanceMetric(createStorageSizeMetric(sizeBytes = 789), BACKGROUND_SCREEN, LOW_PRIORITY)

    // Sanity check: ensure that logs do not automatically upload without the job running (otherwise
    // the other tests in this test suite are moot).
    assertThat(fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()).isEqualTo(0)
    expectSyncStatusToBeUnchanged()
  }

  @Test
  fun testWorker_scheduleUploadPerfMetrics_noMetrics_uploadsNothing() {
    setUpTestApplicationComponent()
    initializeDependencies()

    forceNetworkConnectivityToCellular() // For the job to run.
    val monitor = testDriver.runOneOffWork(WORKER_NAME, UPLOAD_PERFORMANCE_METRICS)

    // Verify that the job succeeded and there was nothing to upload. Sync status will also update
    // since the attempt to upload finished.
    val logCount = fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    assertThat(monitor.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(logCount).isEqualTo(0)
    expectSyncStatusToBeUnchanged()
  }

  @Test
  fun testWorker_scheduleUploadPerfMetrics_severalMetrics_uploadsAllMetrics() {
    setUpTestApplicationComponent()
    initializeDependencies()
    forceNetworkConnectivityOff() // For offline caching.
    logPerformanceMetric(createStartupLatencyMetric(startMs = 123), HOME_ACTIVITY, HIGH_PRIORITY)
    logPerformanceMetric(createApkSizeMetric(sizeBytes = 456), BACKGROUND_SCREEN, LOW_PRIORITY)
    logPerformanceMetric(createStorageSizeMetric(sizeBytes = 789), BACKGROUND_SCREEN, LOW_PRIORITY)

    forceNetworkConnectivityToCellular() // For the job to run.
    val monitor = testDriver.runOneOffWork(WORKER_NAME, UPLOAD_PERFORMANCE_METRICS)

    // Verify that the job succeeded, was the only job to upload something, and uploaded correctly.
    val logCount = fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    assertThat(monitor.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(logCount).isEqualTo(3)
    val events = fakePerformanceMetricsEventLogger.getMostRecentPerformanceMetricsEvents(3)
    val (event1, event2, event3) = events
    assertThat(event1.loggableMetric.loggableMetricTypeCase).isEqualTo(STARTUP_LATENCY_METRIC)
    assertThat(event1.currentScreen).isEqualTo(HOME_ACTIVITY)
    assertThat(event1.priority).isEqualTo(HIGH_PRIORITY)
    assertThat(event1.loggableMetric.startupLatencyMetric.startupLatencyMillis).isEqualTo(123)
    assertThat(event2.loggableMetric.loggableMetricTypeCase).isEqualTo(APK_SIZE_METRIC)
    assertThat(event2.currentScreen).isEqualTo(BACKGROUND_SCREEN)
    assertThat(event2.priority).isEqualTo(LOW_PRIORITY)
    assertThat(event2.loggableMetric.apkSizeMetric.apkSizeBytes).isEqualTo(456)
    assertThat(event3.loggableMetric.loggableMetricTypeCase).isEqualTo(STORAGE_USAGE_METRIC)
    assertThat(event3.currentScreen).isEqualTo(BACKGROUND_SCREEN)
    assertThat(event3.priority).isEqualTo(LOW_PRIORITY)
    assertThat(event3.loggableMetric.storageUsageMetric.storageUsageBytes).isEqualTo(789)
    expectSyncStatusToBeUnchanged()
  }

  @Test
  fun testWorker_scheduleUploadPerfMetrics_severalMetrics_runAgain_uploadsNothing() {
    setUpTestApplicationComponent()
    initializeDependencies()
    forceNetworkConnectivityOff() // For offline caching.
    logPerformanceMetric(createStartupLatencyMetric(startMs = 123), HOME_ACTIVITY, HIGH_PRIORITY)
    logPerformanceMetric(createApkSizeMetric(sizeBytes = 456), BACKGROUND_SCREEN, LOW_PRIORITY)
    logPerformanceMetric(createStorageSizeMetric(sizeBytes = 789), BACKGROUND_SCREEN, LOW_PRIORITY)
    forceNetworkConnectivityToCellular() // For the job to run.
    testDriver.runOneOffWork(WORKER_NAME, UPLOAD_PERFORMANCE_METRICS)
    fakePerformanceMetricsEventLogger.clearAllPerformanceMetricsEvents() // Reset for next job.

    // Run a second time.
    val monitor = testDriver.runOneOffWork(WORKER_NAME, UPLOAD_PERFORMANCE_METRICS)

    // Verify nothing uploaded (since everything should've uploaded during the first job).
    val logCount = fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    assertThat(monitor.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(logCount).isEqualTo(0)
    expectSyncStatusToBeUnchanged()
  }

  @Test
  fun testWorker_scheduleUploadPerfMetrics_hasFailure_failsAndLogsError() {
    setUpTestApplicationComponent()
    initializeDependencies()
    forceNetworkConnectivityOff() // For offline caching.
    logPerformanceMetric(createStartupLatencyMetric(startMs = 123), HOME_ACTIVITY, HIGH_PRIORITY)
    fakePerformanceMetricsEventLogger.setFailure(Exception("Forced failure."))

    forceNetworkConnectivityToCellular() // For the job to run.
    val monitor = testDriver.runOneOffWork(WORKER_NAME, UPLOAD_PERFORMANCE_METRICS)

    // Verify that the job failed and an error was logged due to an underlying failure.
    val logCount = fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    val failureLine = fetchSingleWorkerErrorLog()
    assertThat(monitor.state).isEqualTo(WorkInfo.State.FAILED)
    assertThat(logCount).isEqualTo(0)
    assertThat(failureLine).contains("Failed operation: upload_performance_metrics.")
    assertThat(failureLine).contains("java.lang.Exception: Forced failure.")
    expectSyncStatusToBeUnchanged()
  }

  @Test
  fun testWorker_scheduleUploadPerfMetrics_losesConnectivity_failsAndLogsError() {
    setUpTestApplicationComponent()
    initializeDependencies()
    forceNetworkConnectivityOff() // For offline caching.
    logPerformanceMetric(createStartupLatencyMetric(startMs = 123), HOME_ACTIVITY, HIGH_PRIORITY)

    // Run the job with connectivity disabled. This is slightly contrived because technically the
    // job wouldn't start, but it has the rough effect of simulating connectivity being lost after
    // the job kicks off.
    val monitor = testDriver.runOneOffWork(WORKER_NAME, UPLOAD_PERFORMANCE_METRICS)

    // Verify that the job failed and an error was logged due to an underlying failure.
    val logCount = fakePerformanceMetricsEventLogger.getPerformanceMetricsEventListCount()
    val failure = fetchSingleWorkerErrorLog()
    assertThat(monitor.state).isEqualTo(WorkInfo.State.FAILED)
    assertThat(logCount).isEqualTo(0)
    assertThat(failure).contains("Failed operation: upload_performance_metrics.")
    assertThat(failure).contains("Cannot upload performance metrics without internet connectivity.")
    expectSyncStatusToBeUnchanged()
  }

  /* Tests for UPLOAD_FIRESTORE_DATA. */

  @Test
  fun testWorker_scheduleUploadFirestoreData_sanityCheck_doNotRun_noEventsUploaded() {
    setUpTestApplicationComponent()
    initializeDependencies()
    forceNetworkConnectivityOff() // For offline caching.

    logFirestoreEvent(createOptionalSurveyResponseContext("feedback 1", "test_survey_id1"))
    logFirestoreEvent(createOptionalSurveyResponseContext("feedback 2", "test_survey_id2"))
    logFirestoreEvent(createOptionalSurveyResponseContext("feedback 3", "test_survey_id3"))

    // Sanity check: ensure that logs do not automatically upload without the job running (otherwise
    // the other tests in this test suite are moot).
    assertThat(fakeFirestoreEventLogger.getEventListCount()).isEqualTo(0)
    expectSyncStatusToBeUnchanged()
  }

  @Test
  fun testWorker_scheduleUploadFirestoreData_noEvents_uploadsNothing() {
    setUpTestApplicationComponent()
    initializeDependencies()

    forceNetworkConnectivityToCellular() // For the job to run.
    val monitor = testDriver.runOneOffWork(WORKER_NAME, UPLOAD_FIRESTORE_DATA)

    // Verify that the job succeeded and there was nothing to upload. Sync status will also update
    // since the attempt to upload finished.
    val logCount = fakeFirestoreEventLogger.getEventListCount()
    assertThat(monitor.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(logCount).isEqualTo(0)
    expectSyncStatusToBeUnchanged()
  }

  @Test
  fun testWorker_scheduleUploadFirestoreData_severalEvents_uploadsAllEvents() {
    setUpTestApplicationComponent()
    initializeDependencies()
    forceNetworkConnectivityOff() // For offline caching.
    logFirestoreEvent(createOptionalSurveyResponseContext("feedback 1", "test_survey_id1"))
    logFirestoreEvent(createOptionalSurveyResponseContext("feedback 2", "test_survey_id2"))
    logFirestoreEvent(createOptionalSurveyResponseContext("feedback 3", "test_survey_id3"))

    forceNetworkConnectivityToCellular() // For the job to run.
    val monitor = testDriver.runOneOffWork(WORKER_NAME, UPLOAD_FIRESTORE_DATA)

    // Verify that the job succeeded, was the only job to upload something, and uploaded correctly.
    val logCount = fakeFirestoreEventLogger.getEventListCount()
    val events = fakeFirestoreEventLogger.getMostRecentEvents(3)
    assertThat(monitor.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(logCount).isEqualTo(3)
    val (event1, event2, event3) = events
    assertThat(event1.priority).isEqualTo(EventLog.Priority.ESSENTIAL)
    assertThat(event1.context.activityContextCase).isEqualTo(OPTIONAL_RESPONSE)
    assertThat(event1.context.optionalResponse.feedbackAnswer).isEqualTo("feedback 1")
    assertThat(event1.context.optionalResponse.surveyDetails.surveyId).isEqualTo("test_survey_id1")
    assertThat(event2.priority).isEqualTo(EventLog.Priority.ESSENTIAL)
    assertThat(event2.context.activityContextCase).isEqualTo(OPTIONAL_RESPONSE)
    assertThat(event2.context.optionalResponse.feedbackAnswer).isEqualTo("feedback 2")
    assertThat(event2.context.optionalResponse.surveyDetails.surveyId).isEqualTo("test_survey_id2")
    assertThat(event3.priority).isEqualTo(EventLog.Priority.ESSENTIAL)
    assertThat(event3.context.activityContextCase).isEqualTo(OPTIONAL_RESPONSE)
    assertThat(event3.context.optionalResponse.feedbackAnswer).isEqualTo("feedback 3")
    assertThat(event3.context.optionalResponse.surveyDetails.surveyId).isEqualTo("test_survey_id3")
    expectSyncStatusToBeUnchanged()
  }

  @Test
  fun testWorker_scheduleUploadFirestoreData_severalEvents_runAgain_uploadsNothing() {
    setUpTestApplicationComponent()
    initializeDependencies()
    forceNetworkConnectivityOff() // For offline caching.
    logFirestoreEvent(createOptionalSurveyResponseContext("feedback 1", "test_survey_id1"))
    logFirestoreEvent(createOptionalSurveyResponseContext("feedback 2", "test_survey_id2"))
    logFirestoreEvent(createOptionalSurveyResponseContext("feedback 3", "test_survey_id3"))
    forceNetworkConnectivityToCellular() // For the job to run.
    testDriver.runOneOffWork(WORKER_NAME, UPLOAD_FIRESTORE_DATA)
    fakeFirestoreEventLogger.clearAllEvents() // Reset for next job.

    // Run a second time.
    val monitor = testDriver.runOneOffWork(WORKER_NAME, UPLOAD_FIRESTORE_DATA)

    // Verify nothing uploaded (since everything should've uploaded during the first job).
    val logCount = fakeFirestoreEventLogger.getEventListCount()
    assertThat(monitor.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(logCount).isEqualTo(0)
    expectSyncStatusToBeUnchanged()
  }

  @Test
  fun testWorker_scheduleUploadFirestoreData_hasFailure_failsAndLogsError() {
    setUpTestApplicationComponent()
    initializeDependencies()
    forceNetworkConnectivityOff() // For offline caching.
    logFirestoreEvent(createOptionalSurveyResponseContext("feedback 1", "test_survey_id1"))
    fakeFirestoreEventLogger.setFailure(Exception("Forced failure."))

    forceNetworkConnectivityToCellular() // For the job to run.
    val monitor = testDriver.runOneOffWork(WORKER_NAME, UPLOAD_FIRESTORE_DATA)

    // Verify that the job failed and an error was logged due to an underlying failure.
    val logCount = fakeFirestoreEventLogger.getEventListCount()
    val failureLine = fetchSingleWorkerErrorLog()
    assertThat(monitor.state).isEqualTo(WorkInfo.State.FAILED)
    assertThat(logCount).isEqualTo(0)
    assertThat(failureLine).contains("Failed operation: upload_firestore_data.")
    assertThat(failureLine).contains("java.lang.Exception: Forced failure.")
    expectSyncStatusToBeUnchanged()
  }

  @Test
  fun testWorker_scheduleUploadFirestoreData_losesConnectivity_failsAndLogsError() {
    setUpTestApplicationComponent()
    initializeDependencies()
    forceNetworkConnectivityOff() // For offline caching.
    logFirestoreEvent(createOptionalSurveyResponseContext("feedback 1", "test_survey_id1"))

    // Run the job with connectivity disabled. This is slightly contrived because technically the
    // job wouldn't start, but it has the rough effect of simulating connectivity being lost after
    // the job kicks off.
    val monitor = testDriver.runOneOffWork(WORKER_NAME, UPLOAD_FIRESTORE_DATA)

    // Verify that the job failed and an error was logged due to an underlying failure.
    val logCount = fakeFirestoreEventLogger.getEventListCount()
    val failureLine = fetchSingleWorkerErrorLog()
    assertThat(monitor.state).isEqualTo(WorkInfo.State.FAILED)
    assertThat(logCount).isEqualTo(0)
    assertThat(failureLine).contains("Failed operation: upload_firestore_data.")
    assertThat(failureLine).contains("Cannot upload Firestore events without internet connectivity.")
    expectSyncStatusToBeUnchanged()
  }

  private fun forceNetworkConnectivityOff() {
    networkConnectionUtil.setCurrentConnectionStatus(ProdConnectionStatus.NONE)
  }

  private fun forceNetworkConnectivityToCellular() {
    networkConnectionUtil.setCurrentConnectionStatus(ProdConnectionStatus.CELLULAR)
  }

  private fun logImportantEvent(eventContext: EventLog.Context) {
    analyticsController.logImportantEvent(eventContext, profileId = null)
    testCoroutineDispatchers.runCurrent()
  }

  private fun logLowPriorityEvent(eventContext: EventLog.Context) {
    analyticsController.logLowPriorityEvent(eventContext, profileId = null)
    testCoroutineDispatchers.runCurrent()
  }

  private fun logFatalException(exception: Exception) {
    exceptionsController.logFatalException(exception)
    testCoroutineDispatchers.runCurrent()
  }

  private fun logNonFatalException(exception: Exception) {
    exceptionsController.logNonFatalException(exception)
    testCoroutineDispatchers.runCurrent()
  }

  private fun logPerformanceMetric(
    loggableMetric: LoggableMetric, screenName: ScreenName, priority: Priority
  ) {
    performanceMetricsController.logPerformanceMetricsEvent(
      oppiaClock.getCurrentTimeMs(), screenName, loggableMetric, priority
    )
    testCoroutineDispatchers.runCurrent()
  }

  private fun logFirestoreEvent(eventLogContext: EventLog.Context) {
    firestoreDataController.logEvent(eventLogContext, profileId = null)
    testCoroutineDispatchers.runCurrent()
  }

  private fun createStartupLatencyMetric(startMs: Long): LoggableMetric {
    return LoggableMetric.newBuilder().apply {
      startupLatencyMetric = OppiaMetricLog.StartupLatencyMetric.newBuilder().apply {
        startupLatencyMillis = startMs
      }.build()
    }.build()
  }

  private fun createApkSizeMetric(sizeBytes: Long): LoggableMetric {
    return LoggableMetric.newBuilder().apply {
      apkSizeMetric = OppiaMetricLog.ApkSizeMetric.newBuilder().apply {
        apkSizeBytes = sizeBytes
      }.build()
    }.build()
  }

  private fun createStorageSizeMetric(sizeBytes: Long): LoggableMetric {
    return LoggableMetric.newBuilder().apply {
      storageUsageMetric = OppiaMetricLog.StorageUsageMetric.newBuilder().apply {
        storageUsageBytes = sizeBytes
      }.build()
    }.build()
  }

  private fun createOptionalSurveyResponseContext(
    feedback: String, surveyId: String
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

  private fun expectSyncStatusToBeUnchanged() {
    // This is useful for tests that should never be changing sync status (basically everything
    // except upload events).
    assertThat(getLatestSyncStatus()).isEqualTo(INITIAL_UNKNOWN)
  }

  private fun getLatestSyncStatus(): SyncStatus = testSyncStatusManager.getSyncStatuses().last()

  /**
   * Returns a list of lists of each relevant element of a [StackTraceElement] to be used for
   * comparison in a way that's consistent across JDK versions.
   */
  private fun Exception.toComparableTrace(): List<StackTraceElement> {
    return stackTrace.map {
      StackTraceElement(
        it.fileName ?: "", // Match ExceptionsController behavior when a null file name happens.
        checkNotNull(it.methodName),
        checkNotNull(it.lineNumber),
        checkNotNull(it.className)
      )
    }
  }

  private fun initializeDependencies() {
    FirebaseApp.initializeApp(context)
    testDriver.initializeWorkManager(configuration)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private data class StackTraceElement(
    val fileName: String, val methodName: String, val lineNumber: Int, val className: String
  )

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
      FakeOppiaClockModule::class,
      LocaleProdModule::class,
      LogReportWorkerModule::class,
      LoggerModule::class,
      LoggingIdentifierModule::class,
      NetworkConnectionUtilDebugModule::class,
      PerformanceMetricsConfigurationsModule::class,
      PlatformParameterSingletonModule::class,
      RobolectricModule::class,
      SyncStatusTestModule::class,
      TestAuthenticationModule::class,
      TestDispatcherModule::class,
      TestModule::class,
      TestPlatformParameterModule::class,
      CpuPerformanceSnapshotterModule::class,
      LogStorageModule::class,
      TestLogReportingModule::class,
      WorkManagerConfigurationModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector, DispatcherInjector, PlatformParameterControllerInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(logUploadWorkerTest: LogUploadWorkerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider, DispatcherInjectorProvider, PlatformParameterControllerInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerLogUploadWorkerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(logUploadWorkerTest: LogUploadWorkerTest) {
      component.inject(logUploadWorkerTest)
    }

    override fun getDataProvidersInjector() = component
    override fun getDispatcherInjector() = component
    override fun getPlatformParameterControllerInjector() = component
  }
}
