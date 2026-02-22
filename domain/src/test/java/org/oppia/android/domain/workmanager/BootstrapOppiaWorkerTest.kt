package org.oppia.android.domain.workmanager

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.common.truth.Truth.assertThat
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.FirebaseApp
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.guava.asDeferred
import org.oppia.android.domain.platformparameter.PlatformParameterControllerInjector
import org.oppia.android.domain.platformparameter.PlatformParameterControllerInjectorProvider
import org.oppia.android.domain.workmanager.BootstrapOppiaWorker.Companion.DELEGATED_WORKER_NAME_INPUT_KEY
import org.oppia.android.domain.workmanager.BootstrapOppiaWorker.Companion.constructTaskTypeKey
import org.oppia.android.domain.workmanager.BootstrapOppiaWorkerTest.MockAlternatingFailingWorker.TaskType.ALTERNATING_TASK
import org.oppia.android.domain.workmanager.BootstrapOppiaWorkerTest.MockFailingOppiaWorker.TaskType.FAILING_WORKER1_TASK1
import org.oppia.android.domain.workmanager.BootstrapOppiaWorkerTest.MockOppiaWorker1.TaskType.WORKER1_TASK1
import org.oppia.android.domain.workmanager.BootstrapOppiaWorkerTest.MockOppiaWorker1.TaskType.WORKER1_TASK2
import org.oppia.android.domain.workmanager.BootstrapOppiaWorkerTest.MockOppiaWorker1.TaskType.WORKER1_TASK3
import org.oppia.android.domain.workmanager.BootstrapOppiaWorkerTest.MockOppiaWorker2.TaskType.WORKER2_TASK1
import org.oppia.android.domain.workmanager.BootstrapOppiaWorkerTest.MockPlatformParamOppiaWorker.TaskType.FETCH_PLATFORM_PARAMETER
import org.oppia.android.domain.workmanager.BootstrapOppiaWorkerTest.MockSuccessThenFailWorker.TaskType.MAYBE_SUCCEED_TASK
import org.oppia.android.domain.workmanager.BootstrapOppiaWorkerTest.MockThrowingOppiaWorker.TaskType.FAILING_WORKER2_TASK1
import org.oppia.android.domain.workmanager.testing.OppiaWorkManagerTestDriver
import org.oppia.android.domain.workmanager.testing.OppiaWorkManagerTestInitializer
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.DebugLogReportingModule
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.platformparameter.SplashScreenWelcomeMsg
import org.oppia.android.util.threading.BackgroundDispatcher
import org.oppia.android.util.threading.DispatcherInjector
import org.oppia.android.util.threading.DispatcherInjectorProvider
import org.robolectric.shadows.ShadowLog

/** Tests for [BootstrapOppiaWorker]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = BootstrapOppiaWorkerTest.TestApplication::class)
@OptIn(ExperimentalCoroutinesApi::class)
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
class BootstrapOppiaWorkerTest {
  @Inject lateinit var context: Context
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var fakeOppiaClock: FakeOppiaClock
  @Inject lateinit var oppiaWorkManagerTestInitializer: OppiaWorkManagerTestInitializer
  @Inject lateinit var testDriver: OppiaWorkManagerTestDriver
  @field:[Inject BackgroundDispatcher] lateinit var backgroundDispatcher: CoroutineDispatcher

  private val workManager: WorkManager get() = oppiaWorkManagerTestInitializer.workManager

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    FirebaseApp.initializeApp(context)
    // Ensure OppiaClock is synchronized with FakeSystemClock since the latter is used when
    // controlling time with dispatchers.
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)

    oppiaWorkManagerTestInitializer.initializeWorkManager()

    // WorkManager and workers output most their issues issues to logcat, so this ensures those get
    // printed to the test log. This leads to a noisier test run but it makes debugging failures
    // significantly easier.
    ShadowLog.stream = System.out

    // Reset static state between tests.
    workerResults.clear()

    // TODO: Notes to self:
    // - Use the tests for debug worker to update all the worker tests for log upload, platform params, and perf metrics.
    // - Use the tests for debug initializer to update all the initializer tests for log upload, platform params, and perf metrics.
    // - Finish up the rest of the documentation
    // - Make all tests build & pass
    // - Validate the work manager behavior on an alpha build
    // - REEVALUTE: It may be worthwhile just to drop the WM upgrades because, while it's nice to test the clock this way, it's basically completely fake outside this test due to the poor way WorkManager handles constraint satisfaction.
    // - Push and explain the following:
    //   - The workaround to ignore metadata is not sustainable and only serves to validate the solution here.
    //   - The solution here allows full control over scheduling (yay), though it requires a double Kotlin major upgrade (crap) and ideally a migration off of CoroutineExecutorService (double crap), but that could potentially wait since it seems to be working okay here.
    // - Move the dispatcher and coroutine service changes. The UI task coordinator is actually wrong today and this branch has the fix for it.
  }

  @Test
  fun testWorker_oneOff_differentWorkerClass_cancelsAndLogsError() {
    val workInfo = enqueueOneOffWork<RealListenableWorker>()

    val errorLogLine = fetchSingleBootstrapWorkerErrorLog()
    assertThat(workInfo.state).isEqualTo(WorkInfo.State.CANCELLED)
    assertThat(errorLogLine).contains("Attempting to bootstrap old or invalid worker class")
    assertThat(errorLogLine).contains("BootstrapOppiaWorkerTest\$RealListenableWorker")
  }

  @Test
  fun testWorker_oneOff_missingWorkerName_cancelsAndLogsError() {
    // Run BootstrapOppiaWorker with no input data (which means no worker name).
    val workInfo = enqueueOneOffWork<BootstrapOppiaWorker>()

    val errorLogLine = fetchSingleBootstrapWorkerErrorLog()
    assertThat(workInfo.state).isEqualTo(WorkInfo.State.CANCELLED)
    assertThat(errorLogLine).contains("Attempting to bootstrap for an invalid worker delegate")
    assertThat(errorLogLine).contains("null (no provider found)")
  }

  @Test
  fun testWorker_oneOff_missingTaskTypeKey_cancelsAndLogsError() {
    val workInfo = enqueueOneOffWork<BootstrapOppiaWorker>(newInputData(MockOppiaWorker1.WORKER_NAME))

    val errorLogLine = fetchSingleBootstrapWorkerErrorLog()
    assertThat(workInfo.state).isEqualTo(WorkInfo.State.CANCELLED)
    assertThat(errorLogLine).contains("Encountered invalid task type when trying to prepare worker")
    assertThat(errorLogLine).contains("MockOppiaWorker1: null")
  }

  @Test
  fun testWorker_oneOff_invalidWorkerName_cancelsAndLogsError() {
    val workInfo = enqueueOneOffWork<BootstrapOppiaWorker>(newInputData("invalid_worker_name"))

    val errorLogLine = fetchSingleBootstrapWorkerErrorLog()
    assertThat(workInfo.state).isEqualTo(WorkInfo.State.CANCELLED)
    assertThat(errorLogLine).contains("Attempting to bootstrap for an invalid worker delegate")
    assertThat(errorLogLine).contains("invalid_worker_name (no provider found)")
  }

  @Test
  fun testWorker_oneOff_invalidTaskType_cancelsAndLogsError() {
    val workInfo = enqueueOneOffWork<BootstrapOppiaWorker>(
      newInputData(MockOppiaWorker1.WORKER_NAME, taskTypeName = "invalid_task_type_name")
    )

    val errorLogLine = fetchSingleBootstrapWorkerErrorLog()
    assertThat(workInfo.state).isEqualTo(WorkInfo.State.CANCELLED)
    assertThat(errorLogLine).contains("Encountered invalid task type when trying to prepare worker")
    assertThat(errorLogLine).contains("MockOppiaWorker1: invalid_task_type_name")
  }

  @Test
  fun testWorker_oneOff_worker1_taskType1_succeedsWithNoLoggedErrors() {
    val workInfo = runOneOffWork(MockOppiaWorker1.WORKER_NAME, WORKER1_TASK1)

    assertThat(workInfo.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(fetchBootstrapWorkerErrorLogs()).isEmpty()
    assertThat(fetchSingleWorkerResult()).isEqualTo("Ran MockOppiaWorker1 for task: worker1_task1")
  }

  @Test
  fun testWorker_oneOff_worker1_taskType2_succeedsWithNoLoggedErrors() {
    val workInfo = runOneOffWork(MockOppiaWorker1.WORKER_NAME, WORKER1_TASK2)

    assertThat(workInfo.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(fetchBootstrapWorkerErrorLogs()).isEmpty()
    assertThat(fetchSingleWorkerResult()).isEqualTo("Ran MockOppiaWorker1 for task: worker1_task2")
  }

  @Test
  fun testWorker_oneOff_worker2_taskType1_succeedsWithNoLoggedErrors() {
    val workInfo = runOneOffWork(MockOppiaWorker2.WORKER_NAME, WORKER2_TASK1)

    assertThat(workInfo.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(fetchBootstrapWorkerErrorLogs()).isEmpty()
    assertThat(fetchSingleWorkerResult()).isEqualTo("Ran MockOppiaWorker2 for task: worker2_task1")
  }

  @Test
  fun testWorker_oneOff_failingWorker_failsWithNoLoggedErrors() {
    val workInfo = runOneOffWork(MockFailingOppiaWorker.WORKER_NAME, FAILING_WORKER1_TASK1)

    assertThat(workInfo.state).isEqualTo(WorkInfo.State.FAILED)
    // A standard job failure shouldn't log bootstrap worker errors.
    assertThat(fetchBootstrapWorkerErrorLogs()).isEmpty()
    assertThat(fetchSingleWorkerResult()).isEqualTo("Ran MockFailingOppiaWorker for task: failing_worker1_task1")
  }

  @Test
  fun testWorker_oneOff_throwingWorker_failsWithNoLoggedErrors() {
    val workInfo = runOneOffWork(MockThrowingOppiaWorker.WORKER_NAME, FAILING_WORKER2_TASK1)

    assertThat(workInfo.state).isEqualTo(WorkInfo.State.FAILED)
    // A standard job failure shouldn't log bootstrap worker errors.
    assertThat(fetchBootstrapWorkerErrorLogs()).isEmpty()
    assertThat(fetchSingleWorkerResult()).isEqualTo("Pre-throw in MockThrowingOppiaWorker")
  }

  @Test
  fun testWorker_oneOff_workerWithPlatformParameters_canInjectParametersSuccessfully() {
    val workInfo = runOneOffWork(MockPlatformParamOppiaWorker.WORKER_NAME, FETCH_PLATFORM_PARAMETER)

    assertThat(workInfo.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(fetchBootstrapWorkerErrorLogs()).isEmpty()
    assertThat(fetchSingleWorkerResult()).isEqualTo("Fetched platform parameter value: 'false'")
  }

  @Test
  fun testWorker_oneOff_sameWorkerTwice_sameParameters_bothSucceed() {
    val workInfo1 = runOneOffWork(MockOppiaWorker1.WORKER_NAME, WORKER1_TASK1)
    val workInfo2 = runOneOffWork(MockOppiaWorker1.WORKER_NAME, WORKER1_TASK1)

    assertThat(workInfo1.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(workInfo2.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(fetchBootstrapWorkerErrorLogs()).isEmpty()
    assertThat(workerResults).containsExactly(
      "Ran MockOppiaWorker1 for task: worker1_task1",
      "Ran MockOppiaWorker1 for task: worker1_task1"
    ).inOrder()
  }

  @Test
  fun testWorker_oneOff_sameWorkerTwice_differentParameters_bothSucceed() {
    val workInfo1 = runOneOffWork(MockOppiaWorker1.WORKER_NAME, WORKER1_TASK1)
    val workInfo2 = runOneOffWork(MockOppiaWorker1.WORKER_NAME, WORKER1_TASK2)

    assertThat(workInfo1.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(workInfo2.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(fetchBootstrapWorkerErrorLogs()).isEmpty()
    assertThat(workerResults).containsExactly(
      "Ran MockOppiaWorker1 for task: worker1_task1",
      "Ran MockOppiaWorker1 for task: worker1_task2"
    ).inOrder()
  }

  @Test
  fun testWorker_oneOff_differentWorkers_sameTime_bothSucceed() {
    val workInfo1 = runOneOffWork(MockOppiaWorker1.WORKER_NAME, WORKER1_TASK1)
    val workInfo2 = runOneOffWork(MockOppiaWorker2.WORKER_NAME, WORKER2_TASK1)

    assertThat(workInfo1.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(workInfo2.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(fetchBootstrapWorkerErrorLogs()).isEmpty()
    assertThat(workerResults).containsExactly(
      "Ran MockOppiaWorker1 for task: worker1_task1",
      "Ran MockOppiaWorker2 for task: worker2_task1"
    ).inOrder()
  }

  @Test
  fun testWorker_periodic_differentWorkerClass_cancelsAndLogsError() {
    val workInfo = enqueuePeriodicWork<RealListenableWorker>()

    val errorLogLine = fetchSingleBootstrapWorkerErrorLog()
    assertThat(workInfo?.state).isEqualTo(WorkInfo.State.CANCELLED)
    assertThat(errorLogLine).contains("Attempting to bootstrap old or invalid worker class")
    assertThat(errorLogLine).contains("BootstrapOppiaWorkerTest\$RealListenableWorker")
  }

  @Test
  fun testWorker_periodic_missingWorkerName_cancelsAndLogsError() {
    // Run BootstrapOppiaWorker with no input data (which means no worker name).
    val workInfo = enqueuePeriodicWork<BootstrapOppiaWorker>()

    val errorLogLine = fetchSingleBootstrapWorkerErrorLog()
    assertThat(workInfo?.state).isEqualTo(WorkInfo.State.CANCELLED)
    assertThat(errorLogLine).contains("Attempting to bootstrap for an invalid worker delegate")
    assertThat(errorLogLine).contains("null (no provider found)")
  }

  @Test
  fun testWorker_periodic_missingTaskTypeKey_cancelsAndLogsError() {
    val workInfo = enqueuePeriodicWork<BootstrapOppiaWorker>(newInputData(MockOppiaWorker1.WORKER_NAME))

    val errorLogLine = fetchSingleBootstrapWorkerErrorLog()
    assertThat(workInfo?.state).isEqualTo(WorkInfo.State.CANCELLED)
    assertThat(errorLogLine).contains("Encountered invalid task type when trying to prepare worker")
    assertThat(errorLogLine).contains("MockOppiaWorker1: null")
  }

  @Test
  fun testWorker_periodic_invalidWorkerName_cancelsAndLogsError() {
    val workInfo = enqueueOneOffWork<BootstrapOppiaWorker>(newInputData("invalid_worker_name"))

    val errorLogLine = fetchSingleBootstrapWorkerErrorLog()
    assertThat(workInfo.state).isEqualTo(WorkInfo.State.CANCELLED)
    assertThat(errorLogLine).contains("Attempting to bootstrap for an invalid worker delegate")
    assertThat(errorLogLine).contains("invalid_worker_name (no provider found)")
  }

  @Test
  fun testWorker_periodic_invalidTaskType_cancelsAndLogsError() {
    val workInfo = enqueueOneOffWork<BootstrapOppiaWorker>(
      newInputData(MockOppiaWorker1.WORKER_NAME, taskTypeName = "invalid_task_type_name")
    )

    val errorLogLine = fetchSingleBootstrapWorkerErrorLog()
    assertThat(workInfo.state).isEqualTo(WorkInfo.State.CANCELLED)
    assertThat(errorLogLine).contains("Encountered invalid task type when trying to prepare worker")
    assertThat(errorLogLine).contains("MockOppiaWorker1: invalid_task_type_name")
  }

  @Test
  fun testWorker_periodic_worker1_taskType1_succeedsWithNoLoggedErrorsAndReEnqueues() {
    val workInfo = runPeriodicWork(MockOppiaWorker1.WORKER_NAME, WORKER1_TASK1)

    assertThat(workInfo?.state).isEqualTo(WorkInfo.State.ENQUEUED)
    assertThat(fetchBootstrapWorkerErrorLogs()).isEmpty()
    assertThat(fetchSingleWorkerResult()).isEqualTo("Ran MockOppiaWorker1 for task: worker1_task1")
  }

  @Test
  fun testWorker_periodic_worker1_taskType2_succeedsWithNoLoggedErrorsAndReEnqueues() {
    val workInfo = runPeriodicWork(MockOppiaWorker1.WORKER_NAME, WORKER1_TASK2)

    assertThat(workInfo?.state).isEqualTo(WorkInfo.State.ENQUEUED)
    assertThat(fetchBootstrapWorkerErrorLogs()).isEmpty()
    assertThat(fetchSingleWorkerResult()).isEqualTo("Ran MockOppiaWorker1 for task: worker1_task2")
  }

  @Test
  fun testWorker_periodic_worker2_taskType1_succeedsWithNoLoggedErrorsAndReEnqueues() {
    val workInfo = runPeriodicWork(MockOppiaWorker2.WORKER_NAME, WORKER2_TASK1)

    assertThat(workInfo?.state).isEqualTo(WorkInfo.State.ENQUEUED)
    assertThat(fetchBootstrapWorkerErrorLogs()).isEmpty()
    assertThat(fetchSingleWorkerResult()).isEqualTo("Ran MockOppiaWorker2 for task: worker2_task1")
  }

  @Test
  fun testWorker_periodic_failingWorker_failsWithNoLoggedErrorsAndReEnqueues() {
    val workInfo = runPeriodicWork(MockFailingOppiaWorker.WORKER_NAME, FAILING_WORKER1_TASK1)

    // Standard job failures should re-enqueue and result in no bootstrap worker errors.
    assertThat(workInfo?.state).isEqualTo(WorkInfo.State.ENQUEUED)
    assertThat(fetchBootstrapWorkerErrorLogs()).isEmpty()
    assertThat(fetchSingleWorkerResult()).isEqualTo("Ran MockFailingOppiaWorker for task: failing_worker1_task1")
  }

  @Test
  fun testWorker_periodic_throwingWorker_failsWithNoLoggedErrorsAndReEnqueues() {
    val workInfo = runPeriodicWork(MockThrowingOppiaWorker.WORKER_NAME, FAILING_WORKER2_TASK1)

    // Standard job failures should re-enqueue and result in no bootstrap worker errors.
    assertThat(workInfo?.state).isEqualTo(WorkInfo.State.ENQUEUED)
    assertThat(fetchBootstrapWorkerErrorLogs()).isEmpty()
    assertThat(fetchSingleWorkerResult()).isEqualTo("Pre-throw in MockThrowingOppiaWorker")
  }

  @Test
  fun testWorker_periodic_workerWithPlatformParameters_canInjectParametersSuccessfully() {
    val workInfo = runPeriodicWork(MockPlatformParamOppiaWorker.WORKER_NAME, FETCH_PLATFORM_PARAMETER)

    assertThat(workInfo?.state).isEqualTo(WorkInfo.State.ENQUEUED)
    assertThat(fetchBootstrapWorkerErrorLogs()).isEmpty()
    assertThat(fetchSingleWorkerResult()).isEqualTo("Fetched platform parameter value: 'false'")
  }

  @Test
  fun testWorker_periodic_sameWorkerTwice_sameParameters_onlyFirstRunsAndIsReEnqueued() {
    val workInfo1 = runPeriodicWork(MockOppiaWorker1.WORKER_NAME, WORKER1_TASK1)
    val workInfo2 = runPeriodicWork(MockOppiaWorker1.WORKER_NAME, WORKER1_TASK1)

    // Since the work name is based on the worker and task type, attempting to schedule the same job
    // twice will only result in the first one being scheduled.
    assertThat(workInfo1?.state).isEqualTo(WorkInfo.State.ENQUEUED)
    assertThat(workInfo2).isNull() // Not scheduled.
    assertThat(fetchBootstrapWorkerErrorLogs()).isEmpty()
    assertThat(fetchSingleWorkerResult()).isEqualTo("Ran MockOppiaWorker1 for task: worker1_task1")
  }

  @Test
  fun testWorker_periodic_sameWorkerTwice_differentParameters_bothSucceedAndReEnqueue() {
    val workInfo1 = runPeriodicWork(MockOppiaWorker1.WORKER_NAME, WORKER1_TASK1)
    val workInfo2 = runPeriodicWork(MockOppiaWorker1.WORKER_NAME, WORKER1_TASK2)

    assertThat(workInfo1?.state).isEqualTo(WorkInfo.State.ENQUEUED)
    assertThat(workInfo2?.state).isEqualTo(WorkInfo.State.ENQUEUED)
    assertThat(fetchBootstrapWorkerErrorLogs()).isEmpty()
    assertThat(workerResults).containsExactly(
      "Ran MockOppiaWorker1 for task: worker1_task1",
      "Ran MockOppiaWorker1 for task: worker1_task2"
    ).inOrder()
  }

  @Test
  fun testWorker_periodic_differentWorkers_sameTime_bothSucceedAndReEnqueue() {
    val workInfo1 = runPeriodicWork(MockOppiaWorker1.WORKER_NAME, WORKER1_TASK1)
    val workInfo2 = runPeriodicWork(MockOppiaWorker2.WORKER_NAME, WORKER2_TASK1)

    assertThat(workInfo1?.state).isEqualTo(WorkInfo.State.ENQUEUED)
    assertThat(workInfo2?.state).isEqualTo(WorkInfo.State.ENQUEUED)
    assertThat(fetchBootstrapWorkerErrorLogs()).isEmpty()
    assertThat(workerResults).containsExactly(
      "Ran MockOppiaWorker1 for task: worker1_task1",
      "Ran MockOppiaWorker2 for task: worker2_task1"
    ).inOrder()
  }

  @Test
  fun testWorker_periodic_waitTwoPeriods_workerRunsThreeTimesAndReEnqueues() {
    val workInfo = runPeriodicWork(MockOppiaWorker1.WORKER_NAME, WORKER1_TASK1)

    testCoroutineDispatchers.advanceTimeBy(TimeUnit.MINUTES.toMillis(35))

    // The worker should have run 3 times since it's scheduled to run every 15 minutes and it will
    // run immediately when scheduled since constraints are met.
    assertThat(workInfo?.state).isEqualTo(WorkInfo.State.ENQUEUED)
    assertThat(fetchBootstrapWorkerErrorLogs()).isEmpty()
    assertThat(workerResults).containsExactly(
      "Ran MockOppiaWorker1 for task: worker1_task1",
      "Ran MockOppiaWorker1 for task: worker1_task1",
      "Ran MockOppiaWorker1 for task: worker1_task1"
    ).inOrder()
  }

  @Test
  fun testWorker_periodic_intermittentSuccessesAndThrownErrors_keepsWorkerEnqueued() {
    val workInfo = runPeriodicWork(MockAlternatingFailingWorker.WORKER_NAME, ALTERNATING_TASK)

    testCoroutineDispatchers.advanceTimeBy(TimeUnit.MINUTES.toMillis(35))

    // The worker should have run 3 times and alternated between successes and failures, but
    // continue to run and be enqueued.
    assertThat(workInfo?.state).isEqualTo(WorkInfo.State.ENQUEUED)
    assertThat(fetchBootstrapWorkerErrorLogs()).isEmpty()
    assertThat(workerResults).containsExactly(
      "Ran MockAlternatingFailingWorker. Fail this time: false",
      "Ran MockAlternatingFailingWorker. Fail this time: true",
      "Ran MockAlternatingFailingWorker. Fail this time: false"
    ).inOrder()
  }

  @Test
  fun testWorker_periodic_successfulThenIncompatibleWorker_logsErrorAndCancelsJob() {
    val initialWorkInfo = runPeriodicWork(MockSuccessThenFailWorker.WORKER_NAME, MAYBE_SUCCEED_TASK)

    // Advance the clock enough for the job to run 4 times.
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.MINUTES.toMillis(55))

    // The worker should have run twice. The first is a success, but the second simulates a code
    // change where the job may no longer accept the same task type that was previously periodically
    // scheduled. This should result in a hard stop and cancellation of the job, plus logged errors.
    val workInfo = testDriver.lookUpWorkInfo(initialWorkInfo?.id!!) // Look up the latest status.
    val errorLogLine = fetchSingleBootstrapWorkerErrorLog()
    assertThat(workInfo?.state).isEqualTo(WorkInfo.State.CANCELLED)
    assertThat(errorLogLine).contains("Encountered invalid task type when trying to prepare worker")
    assertThat(errorLogLine).contains("MockSuccessThenFailWorker: maybe_succeed_task")
    assertThat(fetchSingleWorkerResult()).isEqualTo("Ran MockSuccessThenFailWorker")
  }

  @Test
  fun testWorker_multiplePeriodicAndOneOffWorkers_differentRates_allRunInOrderWithNoErrors() {
    val initialInfo1 = runPeriodicWork(MockOppiaWorker1.WORKER_NAME, WORKER1_TASK1, repeatMins = 15)
    val info2 = runOneOffWork(MockOppiaWorker1.WORKER_NAME, WORKER1_TASK2)
    val initialInfo3 = runPeriodicWork(MockOppiaWorker2.WORKER_NAME, WORKER2_TASK1, repeatMins = 31)
    // Advance the clock enough to run job 1 three times and job 2 twice.
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.MINUTES.toMillis(35))
    val info4 = runOneOffWork(MockOppiaWorker1.WORKER_NAME, WORKER1_TASK3)

    // Advance enough for job 1 to run once more.
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.MINUTES.toMillis(12))

    val info1 = testDriver.lookUpWorkInfo(initialInfo1?.id!!) // Look up the latest status.
    val info3 = testDriver.lookUpWorkInfo(initialInfo3?.id!!) // Look up the latest status.
    assertThat(info1?.state).isEqualTo(WorkInfo.State.ENQUEUED)
    assertThat(info2.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(info3?.state).isEqualTo(WorkInfo.State.ENQUEUED)
    assertThat(info4.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(fetchBootstrapWorkerErrorLogs()).isEmpty()
    assertThat(workerResults).containsExactly(
      "Ran MockOppiaWorker1 for task: worker1_task1", // Repeat worker 1 run 1.
      "Ran MockOppiaWorker1 for task: worker1_task2", // One-off worker 1.
      "Ran MockOppiaWorker2 for task: worker2_task1", // Repeat worker 2 run 1.
      "Ran MockOppiaWorker1 for task: worker1_task1", // Repeat worker 1 run 2.
      "Ran MockOppiaWorker1 for task: worker1_task1", // Repeat worker 1 run 3.
      "Ran MockOppiaWorker2 for task: worker2_task1", // Repeat worker 2 run 2.
      "Ran MockOppiaWorker1 for task: worker1_task3", // One-off worker 2.
      "Ran MockOppiaWorker1 for task: worker1_task1", // Repeat worker 1 run 4.
    ).inOrder()
  }

  private fun <T: OppiaWorker.TaskType> runOneOffWork(workerName: String, taskType: T): WorkInfo {
    return enqueueOneOffWork<BootstrapOppiaWorker>(
      newInputData(workerName, taskTypeName = taskType.persistentName)
    )
  }

  private fun <T: OppiaWorker.TaskType> runPeriodicWork(
    workerName: String, taskType: T, repeatMins: Long = 15
  ): WorkInfo? {
    return enqueuePeriodicWork<BootstrapOppiaWorker>(
      newInputData(workerName, taskType.persistentName), repeatMins
    )
  }

  private inline fun <reified T: ListenableWorker> enqueueOneOffWork(
    inputData: Data = Data.Builder().build()
  ): WorkInfo {
    val id = runInBackground {
      OneTimeWorkRequest.Builder(T::class.java)
        .setInputData(inputData)
        .build()
        .also(workManager::enqueue)
        .id
    }
    return checkNotNull(testDriver.lookUpWorkInfo(id)) { "Expected one-off job to run." }
  }

  private inline fun <reified T: ListenableWorker> enqueuePeriodicWork(
    inputData: Data = Data.Builder().build(),
    repeatIntervalMins: Long = 15
  ): WorkInfo? {
    val id = runInBackground {
      PeriodicWorkRequest.Builder(T::class.java, repeatIntervalMins, TimeUnit.MINUTES)
        .setInputData(inputData)
        .build().also {
          val workName = computeWorkNameFromInputData(inputData)
          workManager.enqueueUniquePeriodicWork(workName, ExistingPeriodicWorkPolicy.KEEP, it)
        }.id
    }
    return testDriver.lookUpWorkInfo(id)
  }

  private fun newInputData(workerName: String): Data {
    return Data.Builder().putString(DELEGATED_WORKER_NAME_INPUT_KEY, workerName).build()
  }

  private fun newInputData(workerName: String, taskTypeName: String): Data {
    return Data.Builder()
      .putString(DELEGATED_WORKER_NAME_INPUT_KEY, workerName)
      .putString(constructTaskTypeKey(workerName), taskTypeName)
      .build()
  }

  private fun computeWorkNameFromInputData(inputData: Data): String {
    val workerName = inputData.getString(DELEGATED_WORKER_NAME_INPUT_KEY)
    val taskTypeName = workerName?.let { inputData.getString(constructTaskTypeKey(workerName)) }
    return "$workerName.$taskTypeName"
  }

  private fun <T> runInBackground(func: suspend () -> T): T {
    val resultDeferred = CoroutineScope(backgroundDispatcher).async { func() }
    testCoroutineDispatchers.runCurrent()
    assertThat(resultDeferred.isCompleted).isTrue()
    return resultDeferred.getCompleted()
  }

  private fun fetchBootstrapWorkerErrorLogs(): List<String> {
    // Extract all logs from the bootstrap worker and validate they are each errors before returning
    // the logged message lines.
    return ShadowLog.getLogs().filter { it.tag == "BootstrapOppiaWorker" }.map {
      assertThat(it.type).isEqualTo(Log.ERROR)
      return@map it.msg
    }
  }

  private fun fetchSingleBootstrapWorkerErrorLog(): String {
    val errorLogs = fetchBootstrapWorkerErrorLogs()
    assertThat(errorLogs).hasSize(1)
    return errorLogs.single()
  }

  private fun fetchSingleWorkerResult(): String {
    assertThat(workerResults).hasSize(1)
    return workerResults.single()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  companion object {
    val workerResults = CopyOnWriteArrayList<String>()
  }

  @Module
  interface TestModule {
    @Binds fun bindApplicationContext(application: Application): Context
  }

  @Module
  interface TestWorkManagerModule {
    @Binds
    @IntoMap
    @StringKey(MockOppiaWorker1.WORKER_NAME)
    fun provideMockOppiaWorker1Factory(
      factory: MockOppiaWorker1.Factory
    ): OppiaWorker.Factory<*>

    @Binds
    @IntoMap
    @StringKey(MockOppiaWorker2.WORKER_NAME)
    fun provideMockOppiaWorker2Factory(
      factory: MockOppiaWorker2.Factory
    ): OppiaWorker.Factory<*>

    @Binds
    @IntoMap
    @StringKey(MockPlatformParamOppiaWorker.WORKER_NAME)
    fun provideMockPlatformParamOppiaWorkerFactory(
      factory: MockPlatformParamOppiaWorker.Factory
    ): OppiaWorker.Factory<*>

    @Binds
    @IntoMap
    @StringKey(MockFailingOppiaWorker.WORKER_NAME)
    fun provideMockFailingOppiaWorkerFactory(
      factory: MockFailingOppiaWorker.Factory
    ): OppiaWorker.Factory<*>

    @Binds
    @IntoMap
    @StringKey(MockThrowingOppiaWorker.WORKER_NAME)
    fun provideMockThrowingOppiaWorkerFactory(
      factory: MockThrowingOppiaWorker.Factory
    ): OppiaWorker.Factory<*>

    @Binds
    @IntoMap
    @StringKey(MockAlternatingFailingWorker.WORKER_NAME)
    fun provideMockAlternatingFailingWorkerFactory(
      factory: MockAlternatingFailingWorker.Factory
    ): OppiaWorker.Factory<*>

    @Binds
    @IntoMap
    @StringKey(MockSuccessThenFailWorker.WORKER_NAME)
    fun provideMockSuccessThenFailWorkerFactory(
      factory: MockSuccessThenFailWorker.Factory
    ): OppiaWorker.Factory<*>
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, RobolectricModule::class, TestDispatcherModule::class,
      FakeOppiaClockModule::class, TestWorkManagerModule::class,
      LocaleProdModule::class,
      LoggerModule::class,
      TestPlatformParameterModule::class,
      AssetModule::class,
      DebugLogReportingModule::class
    ]
  )
  interface TestApplicationComponent : DispatcherInjector, PlatformParameterControllerInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(test: BootstrapOppiaWorkerTest)
  }

  class TestApplication : Application(), DispatcherInjectorProvider, PlatformParameterControllerInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerBootstrapOppiaWorkerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: BootstrapOppiaWorkerTest) {
      component.inject(test)
    }

    override fun getDispatcherInjector() = component
    override fun getPlatformParameterControllerInjector() = component
  }

  class MockOppiaWorker1 private constructor() : OppiaWorker<MockOppiaWorker1.TaskType> {
    enum class TaskType(override val persistentName: String) : OppiaWorker.TaskType {
      WORKER1_TASK1("worker1_task1"),
      WORKER1_TASK2("worker1_task2"),
      WORKER1_TASK3("worker1_task3")
    }

    override suspend fun doWork(taskType: TaskType): OppiaWorker.Result {
      workerResults += "Ran MockOppiaWorker1 for task: ${taskType.persistentName}"
      return OppiaWorker.Result.SUCCESS
    }

    class Factory @Inject constructor() : OppiaWorker.Factory<TaskType> {
      override val supportedTaskTypes: List<TaskType> = TaskType.values().toList()

      override fun createWorker(): OppiaWorker<TaskType> {
        return MockOppiaWorker1()
      }
    }

    companion object {
      const val WORKER_NAME = "MockOppiaWorker1"
    }
  }

  class MockOppiaWorker2 private constructor() : OppiaWorker<MockOppiaWorker2.TaskType> {
    enum class TaskType(override val persistentName: String) : OppiaWorker.TaskType {
      WORKER2_TASK1("worker2_task1")
    }

    override suspend fun doWork(taskType: TaskType): OppiaWorker.Result {
      workerResults += "Ran MockOppiaWorker2 for task: ${taskType.persistentName}"
      return OppiaWorker.Result.SUCCESS
    }

    class Factory @Inject constructor() : OppiaWorker.Factory<TaskType> {
      override val supportedTaskTypes: List<TaskType> = TaskType.values().toList()

      override fun createWorker(): OppiaWorker<TaskType> {
        return MockOppiaWorker2()
      }
    }

    companion object {
      const val WORKER_NAME = "MockOppiaWorker2"
    }
  }

  class MockPlatformParamOppiaWorker private constructor(
    private val splashScreenWelcomeMsgParam: Provider<PlatformParameterValue<Boolean>>
  ) : OppiaWorker<MockPlatformParamOppiaWorker.TaskType> {
    enum class TaskType(override val persistentName: String) : OppiaWorker.TaskType {
      FETCH_PLATFORM_PARAMETER("fetch_platform_parameter")
    }

    override suspend fun doWork(taskType: TaskType): OppiaWorker.Result {
      val splashScreenWelcomeMsgValue = splashScreenWelcomeMsgParam.get().value
      workerResults += "Fetched platform parameter value: '$splashScreenWelcomeMsgValue'"
      return OppiaWorker.Result.SUCCESS
    }

    class Factory @Inject constructor(
      @SplashScreenWelcomeMsg
      private val splashScreenWelcomeMsgParam: Provider<PlatformParameterValue<Boolean>>
    ) : OppiaWorker.Factory<TaskType> {
      override val supportedTaskTypes: List<TaskType> = TaskType.values().toList()

      override fun createWorker(): OppiaWorker<TaskType> {
        return MockPlatformParamOppiaWorker(splashScreenWelcomeMsgParam)
      }
    }

    companion object {
      const val WORKER_NAME = "MockPlatformParamOppiaWorker"
    }
  }

  class MockFailingOppiaWorker private constructor() : OppiaWorker<MockFailingOppiaWorker.TaskType> {
    enum class TaskType(override val persistentName: String) : OppiaWorker.TaskType {
      FAILING_WORKER1_TASK1("failing_worker1_task1")
    }

    override suspend fun doWork(taskType: TaskType): OppiaWorker.Result {
      workerResults += "Ran MockFailingOppiaWorker for task: ${taskType.persistentName}"
      return OppiaWorker.Result.FAILURE
    }

    class Factory @Inject constructor() : OppiaWorker.Factory<TaskType> {
      override val supportedTaskTypes: List<TaskType> = TaskType.values().toList()

      override fun createWorker(): OppiaWorker<TaskType> {
        return MockFailingOppiaWorker()
      }
    }

    companion object {
      const val WORKER_NAME = "MockFailingOppiaWorker"
    }
  }

  class MockThrowingOppiaWorker private constructor() : OppiaWorker<MockThrowingOppiaWorker.TaskType> {
    enum class TaskType(override val persistentName: String) : OppiaWorker.TaskType {
      FAILING_WORKER2_TASK1("failing_worker2_task1")
    }

    override suspend fun doWork(taskType: TaskType): OppiaWorker.Result {
      workerResults += "Pre-throw in MockThrowingOppiaWorker"
      error("Catastrophic failure.")
    }

    class Factory @Inject constructor() : OppiaWorker.Factory<TaskType> {
      override val supportedTaskTypes: List<TaskType> = TaskType.values().toList()

      override fun createWorker(): OppiaWorker<TaskType> {
        return MockThrowingOppiaWorker()
      }
    }

    companion object {
      const val WORKER_NAME = "MockThrowingOppiaWorker"
    }
  }

  class MockAlternatingFailingWorker private constructor() : OppiaWorker<MockAlternatingFailingWorker.TaskType> {
    enum class TaskType(override val persistentName: String) : OppiaWorker.TaskType {
      ALTERNATING_TASK("alternating_tasks")
    }

    override suspend fun doWork(taskType: TaskType): OppiaWorker.Result {
      workerResults += "Ran MockAlternatingFailingWorker. Fail this time: $failThisTime"
      val shouldThrow = failThisTime
      failThisTime = !failThisTime
      if (shouldThrow) error("Catastrophic failure.")
      return OppiaWorker.Result.SUCCESS
    }

    class Factory @Inject constructor() : OppiaWorker.Factory<TaskType> {
      override val supportedTaskTypes: List<TaskType> = TaskType.values().toList()

      override fun createWorker(): OppiaWorker<TaskType> {
        return MockAlternatingFailingWorker()
      }
    }

    companion object {
      const val WORKER_NAME = "MockAlternatingFailingWorker"

      private var failThisTime = false
    }
  }

  class MockSuccessThenFailWorker private constructor() : OppiaWorker<MockSuccessThenFailWorker.TaskType> {
    enum class TaskType(override val persistentName: String) : OppiaWorker.TaskType {
      MAYBE_SUCCEED_TASK("maybe_succeed_task")
    }

    override suspend fun doWork(taskType: TaskType): OppiaWorker.Result {
      workerResults += "Ran MockSuccessThenFailWorker"
      hasIncompatibility = true
      return OppiaWorker.Result.SUCCESS
    }

    class Factory @Inject constructor() : OppiaWorker.Factory<TaskType> {
      override val supportedTaskTypes: List<TaskType> = TaskType.values().toList()

      override fun createWorker(): OppiaWorker<TaskType> {
        return MockSuccessThenFailWorker()
      }

      override suspend fun doWorkForTaskName(taskName: String): OppiaWorker.Result? {
        // Simulate a "later" run of the job (e.g. in the next process run) not having the same
        // tasks supported.
        if (hasIncompatibility) return null
        return super.doWorkForTaskName(taskName)
      }
    }

    companion object {
      const val WORKER_NAME = "MockSuccessThenFailWorker"

      private var hasIncompatibility = false
    }
  }

  private class RealListenableWorker(
    appContext: Context, workerParams: WorkerParameters
  ) : ListenableWorker(appContext, workerParams) {
    override fun startWork(): ListenableFuture<Result> {
      error("Not implemented for test.")
    }
  }
}
