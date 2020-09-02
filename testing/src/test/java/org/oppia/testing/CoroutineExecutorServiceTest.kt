package org.oppia.testing

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.util.data.AsyncResult
import org.oppia.util.threading.BackgroundDispatcher
import org.robolectric.annotation.LooperMode
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.full.cast

/**
 * Tests for [CoroutineExecutorService]. NOTE: significant care should be taken when modifying these
 * tests since they combine several different coroutine dispatchers, including a real-time
 * dispatcher, the ones coordinated by [TestCoroutineDispatchers], and custom test dispatchers in
 * order to test complex scenarios like whether certain blocking operations block/timeout/complete
 * as expected.
 *
 * Many of these tests also depend on real time, which means different performing machines may
 * introduce flakes when running these tests. Please reach out to oppia-android-dev@googlegroups.com
 * if you find yourself in this situation.
 *
 * For developers changing this suite: note that an n-threaded real dispatcher is used to test
 * blocking operations since coordinating multiple co-dependent test dispatchers creates a circular
 * dependency that effectively always results in a deadlock (since there's no way to control the
 * order of tasks being executed in a coroutine dispatcher).
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class CoroutineExecutorServiceTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  @field:BackgroundDispatcher
  lateinit var backgroundCoroutineDispatcher: CoroutineDispatcher

  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject lateinit var testDispatcherFactory: TestCoroutineDispatcher.Factory

  @Mock lateinit var mockRunnable: Runnable
  @Mock lateinit var mockCallable: Callable<String>

  private val testDispatcher by lazy {
    testDispatcherFactory.createDispatcher(
      Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    )
  }
  private val testDispatcherScope by lazy { CoroutineScope(testDispatcher) }

  private val testDispatcher2 by lazy {
    testDispatcherFactory.createDispatcher(
      Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    )
  }
  private val testDispatcherScope2 by lazy { CoroutineScope(testDispatcher2) }

  // Dispatcher that can continually execute blocking tasks without deadlocking.
  private val realDispatcher = Executors.newCachedThreadPool().asCoroutineDispatcher()
  private val realDispatcherScope = CoroutineScope(realDispatcher)

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testExecute_withoutRunningPendingTasks_doesNotRunScheduledTask() {
    val executor = createExecutorServiceAsExecutor()

    executor.execute(mockRunnable)

    verify(mockRunnable, never()).run()
  }

  @Test
  fun testExecute_afterRunningPendingTasks_runsScheduledTask() {
    val executor = createExecutorServiceAsExecutor()

    executor.execute(mockRunnable)
    testCoroutineDispatchers.runCurrent()

    verify(mockRunnable).run()
  }

  @Test
  fun testExecute_nullParameter_throwsException() {
    val executorService = createExecutorService()

    assertThrows(NullPointerException::class) { executorService.execute(/* command= */ null) }
  }

  @Test
  fun testExecute_afterShutdown_throwsException() {
    val executorService = createExecutorService()
    executorService.shutdown()

    assertThrows(RejectedExecutionException::class) { executorService.execute(mockRunnable) }
  }

  @Test
  fun testExecute_afterShutdownNow_throwsException() {
    val executorService = createExecutorService()
    executorService.shutdownNow()

    assertThrows(RejectedExecutionException::class) { executorService.execute(mockRunnable) }
  }

  @Test
  fun testSubmitRunnable_withoutRunningPendingTasks_doesNotRunScheduledTask() {
    val executorService = createExecutorService()

    executorService.submit(mockRunnable)

    verify(mockRunnable, never()).run()
  }

  @Test
  fun testSubmitRunnable_afterRunningPendingTasks_runsScheduledTask() {
    val executorService = createExecutorService()

    executorService.submit(mockRunnable)
    testCoroutineDispatchers.runCurrent()

    verify(mockRunnable).run()
  }

  @Test
  fun testSubmitRunnable_nullParameter_throwsException() {
    val executorService = createExecutorService()

    val nullRunnable: Runnable? = null
    assertThrows(NullPointerException::class) { executorService.submit(nullRunnable) }
  }

  @Test
  fun testSubmitRunnable_afterShutdown_throwsException() {
    val executorService = createExecutorService()
    executorService.shutdown()

    assertThrows(RejectedExecutionException::class) { executorService.submit(mockRunnable) }
  }

  @Test
  fun testSubmitRunnable_afterShutdownNow_throwsException() {
    val executorService = createExecutorService()
    executorService.shutdownNow()

    assertThrows(RejectedExecutionException::class) { executorService.submit(mockRunnable) }
  }

  @Test
  fun testSubmitCallable_withoutRunningPendingTasks_doesNotRunScheduledTask() {
    val executorService = createExecutorService()

    executorService.submit(mockCallable)

    verify(mockCallable, never()).call()
  }

  @Test
  fun testSubmitCallable_afterRunningPendingTasks_runsScheduledTask() {
    val executorService = createExecutorService()

    executorService.submit(mockCallable)
    testCoroutineDispatchers.runCurrent()

    verify(mockCallable).call()
  }

  @Test
  fun testSubmitCallable_nullParameter_throwsException() {
    val executorService = createExecutorService()

    val nullCallable: Callable<String>? = null
    assertThrows(NullPointerException::class) { executorService.submit(nullCallable) }
  }

  @Test
  fun testSubmitCallable_afterShutdown_throwsException() {
    val executorService = createExecutorService()
    executorService.shutdown()

    assertThrows(RejectedExecutionException::class) { executorService.submit(mockCallable) }
  }

  @Test
  fun testSubmitCallable_afterShutdownNow_throwsException() {
    val executorService = createExecutorService()
    executorService.shutdownNow()

    assertThrows(RejectedExecutionException::class) { executorService.submit(mockCallable) }
  }

  @Test
  fun testSubmitCallable_returnedFuture_withoutRunningTasks_isNotCompleted() {
    val executorService = createExecutorService()
    val callable = Callable { "Task" }

    val callableFuture = executorService.submit(callable)

    assertThat(callableFuture.isDone).isFalse()
  }

  @Test
  fun testSubmitCallable_returnedFuture_afterRunningTasks_isCompleted() {
    val executorService = createExecutorService()
    val callable = Callable { "Task" }

    val callableFuture = executorService.submit(callable)
    testCoroutineDispatchers.runCurrent()

    assertThat(callableFuture.isDone).isTrue()
  }

  @Test
  fun testSubmitCallable_failed_returnedFuture_afterRunningTasks_hasFailure() {
    val executorService = createExecutorService()
    val callable = Callable { throw Exception("Task failed") }

    val callableFuture = executorService.submit(callable)
    testCoroutineDispatchers.runCurrent()

    assertThat(callableFuture.isDone).isTrue()
    val exception = assertThrows(ExecutionException::class) { callableFuture.get() }
    assertThat(exception).hasCauseThat().isInstanceOf(Exception::class.java)
    assertThat(exception).hasCauseThat().hasMessageThat().contains("Task failed")
  }

  @Test
  fun testSubmitCallable_successfulTask_afterFailure_returnedFutureSucceeds() {
    val executorService = createExecutorService()
    val failingCallable = Callable { throw Exception("Task failed") }
    val succeedingCallable = Callable { "Task succeeded" }

    // Note that order matters here: this test is verifying that a successful task completing after
    // a failing task can still succeed (rather than getting the same failure as the failing task).
    // Note that the two runCurrent calls and the isDone verification below are important to ensure
    // the task failure is recognized before evaluating whether the successful task succeeded.
    val failingFuture = executorService.submit(failingCallable)
    testCoroutineDispatchers.runCurrent()
    val succeedingFuture = executorService.submit(succeedingCallable)
    testCoroutineDispatchers.runCurrent()

    assertThat(failingFuture.isDone).isTrue()
    assertThat(succeedingFuture.isDone).isTrue()
    assertThat(succeedingFuture.get()).contains("Task succeeded")
  }

  @Test
  @Suppress("BlockingMethodInNonBlockingContext") // Intentional for testing purposes.
  fun testSubmitCallable_returnedFuture_pendingTask_tasksNotRun_getFunctionBlocks() {
    val executorService = createExecutorService()
    val callable = Callable { "Task" }
    val callableFuture = executorService.submit(callable)

    val getResult = testDispatcherScope.async {
      callableFuture.get()
    }

    // The getter should not return since the task isn't yet completed.
    assertThat(getResult.isCompleted).isFalse()
  }

  @Test
  @Suppress("BlockingMethodInNonBlockingContext") // Intentional for testing purposes.
  @ExperimentalCoroutinesApi
  fun testSubmitCallable_returnedFuture_pendingTask_runTasks_getFunctionReturnsComputedValue() {
    val executorService = createExecutorService()
    val callable = Callable { "Task" }
    val callableFuture = executorService.submit(callable)

    val getResult = testDispatcherScope.async {
      callableFuture.get()
    }
    testCoroutineDispatchers.runCurrent()
    testDispatcher.runUntilIdle()

    // The getter should return since the task has finished.
    assertThat(getResult.isCompleted).isTrue()
    assertThat(getResult.getCompleted()).isEqualTo("Task")
  }

  @Test
  @Suppress("BlockingMethodInNonBlockingContext") // Intentional for testing purposes.
  @ExperimentalCoroutinesApi
  fun testSubmitCallable_returnedFuture_pendingTask_tasksNotRun_timedGetFuncTimesOut() {
    val executorService = createExecutorService()
    val callable = Callable { "Task" }
    val callableFuture = executorService.submit(callable)

    val getResult = testDispatcherScope.async {
      try {
        AsyncResult.success(callableFuture.get(/* timeout= */ 1, TimeUnit.SECONDS))
      } catch (e: ExecutionException) {
        AsyncResult.failed<String>(e)
      }
    }
    testDispatcher.runUntilIdle()

    // The getter should return since the task has finished.
    assertThat(getResult.isCompleted).isTrue()
    assertThat(getResult.getCompleted().isFailure()).isTrue()
    assertThat(getResult.getCompleted().getErrorOrNull())
      .isInstanceOf(ExecutionException::class.java)
    assertThat(getResult.getCompleted().getErrorOrNull()?.cause)
      .isInstanceOf(TimeoutException::class.java)
  }

  @Test
  @Suppress("BlockingMethodInNonBlockingContext") // Intentional for testing purposes.
  @ExperimentalCoroutinesApi
  fun testSubmitCallable_returnedFuture_pendingTask_runTasks_timedGetFuncDoesNotTimeOut() {
    val executorService = createExecutorService()
    val callable = Callable { "Task" }
    val callableFuture = executorService.submit(callable)

    val getResult = realDispatcherScope.async {
      callableFuture.get(/* timeout= */ 1, TimeUnit.SECONDS)
    }
    testCoroutineDispatchers.runCurrent()
    waitForDeferredWithDispatcher1(getResult)

    // The getter should return since the task has finished.
    assertThat(getResult.getCompleted()).isEqualTo("Task")
  }

  @Test
  fun testSubmitCallable_returnedFuture_afterRunningTasks_getsComputedValue() {
    val executorService = createExecutorService()
    val callable = Callable { "Task" }

    val callableFuture = executorService.submit(callable)
    testCoroutineDispatchers.runCurrent()

    assertThat(callableFuture.get()).isEqualTo("Task")
  }

  @Test
  fun testSubmitCallable_returnedFuture_pendingTask_cancel_isCancelled() {
    val executorService = createExecutorService()
    val callable = Callable { "Task" }
    val callableFuture = executorService.submit(callable)

    callableFuture.cancel(/* mayInterruptIfRunning= */ false)
    testCoroutineDispatchers.runCurrent()

    assertThat(callableFuture.isCancelled).isTrue()
  }

  @Test
  fun testSubmitRunnable_withResult_withoutRunningPendingTasks_doesNotRunScheduledTask() {
    val executorService = createExecutorService()

    executorService.submit(mockRunnable, /* result= */ "Task")

    verify(mockRunnable, never()).run()
  }

  @Test
  fun testSubmitRunnable_withResult_afterRunningPendingTasks_runsScheduledTask() {
    val executorService = createExecutorService()

    executorService.submit(mockRunnable, /* result= */ "Task")
    testCoroutineDispatchers.runCurrent()

    verify(mockRunnable).run()
  }

  @Test
  fun testSubmitRunnable_withResult_nullParameter_throwsException() {
    val executorService = createExecutorService()

    val nullRunnable: Runnable? = null
    assertThrows(NullPointerException::class) {
      executorService.submit(nullRunnable, /* result= */ "Task")
    }
  }

  @Test
  fun testSubmitRunnable_withResult_afterShutdown_throwsException() {
    val executorService = createExecutorService()
    executorService.shutdown()

    assertThrows(RejectedExecutionException::class) {
      executorService.submit(mockRunnable, /* result= */ "Task")
    }
  }

  @Test
  fun testSubmitRunnable_withResult_afterShutdownNow_throwsException() {
    val executorService = createExecutorService()
    executorService.shutdownNow()

    assertThrows(RejectedExecutionException::class) {
      executorService.submit(mockRunnable, /* result= */ "Task")
    }
  }

  @Test
  fun testSubmitRunnable_withResult_afterRunningTasks_returnsFutureWithResult() {
    val executorService = createExecutorService()

    val resultFuture = executorService.submit(mockRunnable, /* result= */ "Task")
    testCoroutineDispatchers.runCurrent()

    // Verify that the result value is propagated to the finished future.
    assertThat(resultFuture.isDone).isTrue()
    assertThat(resultFuture.get()).isEqualTo("Task")
  }

  /* Note that the tests to verify shutdown-before-execution fails are elsewhere in the suite. */
  @Test
  fun testShutdown_afterExecute_doNotRunPendingTasks_doesNotRunTask() {
    val executorService = createExecutorService()
    executorService.submit(mockRunnable)

    executorService.shutdown()

    // Verify that shutdown() does not immediately force tasks to run.
    verify(mockRunnable, never()).run()
  }

  @Test
  fun testShutdown_afterExecute_thenRunPendingTasks_finishesTask() {
    val executorService = createExecutorService()
    executorService.submit(mockRunnable)

    executorService.shutdown()
    testCoroutineDispatchers.runCurrent()

    // The task is run because shutdown() doesn't stop existing tasks from being run.
    verify(mockRunnable).run()
  }

  @Test
  fun testShutdownNow_noTasks_returnsEmptyList() {
    val executorService = createExecutorService()

    val pendingTasks = executorService.shutdownNow()
    testCoroutineDispatchers.runCurrent()

    // No tasks were cancelled.
    assertThat(pendingTasks).isEmpty()
  }

  @Test
  fun testShutdownNow_finishedTask_returnsEmptyList() {
    val executorService = createExecutorService()
    executorService.submit(mockRunnable)
    testCoroutineDispatchers.runCurrent()

    val pendingTasks = executorService.shutdownNow()
    testCoroutineDispatchers.runCurrent()

    // No tasks were cancelled.
    assertThat(pendingTasks).isEmpty()
  }

  @Test
  fun testShutdownNow_afterExecute_thenRunPendingTasks_doesNotRunTask() {
    val executorService = createExecutorService()
    executorService.submit(mockRunnable)

    executorService.shutdownNow()
    testCoroutineDispatchers.runCurrent()

    // The task should not be run because shutdownNow() prevents non-started tasks from beginning.
    verify(mockRunnable, never()).run()
  }

  @Test
  fun testShutdownNow_afterExecute_thenRunPendingTasks_returnsRunnableForPendingTask() {
    val executorService = createExecutorService()
    executorService.submit(mockRunnable)

    val pendingTasks = executorService.shutdownNow()
    testCoroutineDispatchers.runCurrent()

    // Only verify that a single pending task was cancelled. No additional verification is done
    // because ExecutorService doesn't clearly define what the returned Runnable should represent.
    assertThat(pendingTasks).hasSize(1)
  }

  /*
   * Note that the equivalent of this test does not exist for shutdown() because:
   * 1) It would effectively be a more complicated version of the existing test that verifies
   *    shutdown does not stop late task execution.
   * 2) It's harder to arrange because there's a circular blocking dependency between the real
   *    dispatcher, the test thread, the custom test dispatcher, and the coordinated test
   *    dispatchers that can probably only be resolved by punching another hole in the executor
   *    service. Unlike other tests where this was done, it doesn't seem worth it for this situation
   *    since there is an existing test for the late-task execution behavior verification.
   */
  @Test
  fun testShutdownNow_afterStartingLongTask_taskAllowedToComplete_doesNotFinishTask() {
    val executorService = createExecutorService()
    // Create a long task that waits 1 second before calling the runnable.
    val longTask = wrapRunnableWithOneSecondDelayUsingDispatcher1(mockRunnable)
    executorService.submit(longTask)

    // Kick-off the task, but don't complete it. Note that this is done via a real dispatcher since
    // it will block until the test dispatcher is run.
    val syncDeferred = realDispatcherScope.async {
      // Run in a real thread to avoid races against task availability within the executor service.
      executorService.shutdownNow()
      testCoroutineDispatchers.runCurrent()
    }
    testDispatcher.runUntilIdle() // Allow the task to complete.
    waitForDeferredWithDispatcher1(syncDeferred)

    // The runnable should not have been run because shutdownNow() interrupts it.
    verify(mockRunnable, never()).run()
  }

  @Test
  fun testIsShutdown_withoutShutdown_returnsFalse() {
    val executorService = createExecutorService()

    assertThat(executorService.isShutdown).isFalse()
  }

  @Test
  fun testIsShutdown_afterShutdown_noTasks_returnsTrue() {
    val executorService = createExecutorService()

    executorService.shutdown()

    assertThat(executorService.isShutdown).isTrue()
  }

  @Test
  fun testIsShutdown_afterShutdown_withIncompleteTask_returnsTrue() {
    val executorService = createExecutorService()
    executorService.submit(mockRunnable)

    executorService.shutdown()

    assertThat(executorService.isShutdown).isTrue()
  }

  @Test
  fun testIsShutdown_afterShutdown_afterPendingTaSksFinish_returnsTrue() {
    val executorService = createExecutorService()
    executorService.submit(mockRunnable)

    executorService.shutdown()
    testCoroutineDispatchers.runCurrent()

    // Tasks finishing after shutdown is called should not affect whether it's shutdown.
    assertThat(executorService.isShutdown).isTrue()
  }

  @Test
  fun testIsTerminated_withoutShutdown_returnsFalse() {
    val executorService = createExecutorService()

    assertThat(executorService.isTerminated).isFalse()
  }

  @Test
  fun testIsTerminated_afterShutdown_noTasks_returnsTrue() {
    val executorService = createExecutorService()

    executorService.shutdown()

    assertThat(executorService.isTerminated).isTrue()
  }

  @Test
  fun testIsTerminated_afterShutdown_withIncompleteTasks_returnsFalse() {
    val executorService = createExecutorService()
    executorService.submit(mockRunnable)

    executorService.shutdown()

    // While the service is shutdown, it's not terminated since not all tasks have finished.
    assertThat(executorService.isTerminated).isFalse()
  }

  @Test
  fun testIsTerminated_afterShutdown_afterPendingTaSksFinish_returnsTrue() {
    val executorService = createExecutorService()
    executorService.submit(mockRunnable)

    executorService.shutdown()
    testCoroutineDispatchers.runCurrent()

    // The service is considered terminated only after tasks have completed.
    assertThat(executorService.isTerminated).isTrue()
  }

  @Test
  fun testAwaitTermination_beforeShutdown_throwsException() {
    val executorService = createExecutorService()

    // Note that this is not documented in the ExecutorService documentation, it seems necessary
    // since it doesn't make sense to return false (per the documentation) or block unless a
    // shutdown request was actually initiated.
    assertThrows(IllegalStateException::class) {
      executorService.awaitTermination(/* timeout= */ 1, TimeUnit.SECONDS)
    }
  }

  @Test
  fun testAwaitTermination_afterShutdown_noTasks_returnsTrue() {
    val executorService = createExecutorService()
    executorService.shutdown()

    val isTerminated = executorService.awaitTermination(/* timeout= */ 1, TimeUnit.SECONDS)

    assertThat(isTerminated).isTrue()
  }

  @Test
  @Suppress("BlockingMethodInNonBlockingContext") // Intentional to test blocking.
  @ExperimentalCoroutinesApi
  fun testAwaitTermination_afterShutdown_withLongTask_exceedTimeout_returnsFalse() {
    val executorService = createExecutorService()
    val delayMs = 10L
    executorService.submit(
      lateFinishingCallableWithDispatcher2(
        Callable { "Task 1" }, timeToWaitMillis = delayMs * 10
      )
    )
    executorService.shutdown()
    autoSettleServiceBeforeBlocking(executorService)

    val terminationDeferred = realDispatcherScope.async {
      executorService.awaitTermination(delayMs, TimeUnit.MILLISECONDS)
    }
    waitForDeferredWithDispatcher1(terminationDeferred)

    // The long task did not not complete in time, so the awaitTermination should fail.
    assertThat(terminationDeferred.getCompleted()).isFalse()
  }

  @Test
  @Suppress("BlockingMethodInNonBlockingContext") // Intentional to test blocking.
  @ExperimentalCoroutinesApi
  @Ignore("Flaky test") // TODO(#1763): Remove & stabilize test.
  fun testAwaitTermination_afterShutdown_withTasks_finishWithinTimeout_returnsTrue() {
    val executorService = createExecutorService()
    val delayMs = 10L
    executorService.submit(
      lateFinishingCallableWithDispatcher2(
        Callable { "Task 1" }, timeToWaitMillis = delayMs
      )
    )
    executorService.shutdown()
    autoSettleServiceBeforeBlocking(executorService)

    val terminationDeferred = realDispatcherScope.async {
      executorService.awaitTermination(delayMs * 10, TimeUnit.MILLISECONDS)
    }
    waitForDeferredWithDispatcher1(terminationDeferred)

    // The long task did not not complete in time, so the awaitTermination should fail.
    // The long task finished, so the awaitTermination should succeed.
    assertThat(terminationDeferred.getCompleted()).isTrue()
  }

  @Test
  @Suppress("BlockingMethodInNonBlockingContext") // Intentional to test blocking.
  fun testInvokeAll_doNotRunTasks_blocks() {
    val executorService = createExecutorService()
    val callable1 = Callable { "Task 1" }
    val callable2 = Callable { "Task 2" }

    val deferred = testDispatcherScope2.async {
      executorService.invokeAll(listOf(callable1, callable2))
    }

    assertThat(deferred.isCompleted).isFalse()
  }

  @Test
  @Suppress("BlockingMethodInNonBlockingContext") // Intentional to test blocking interruption.
  @ExperimentalCoroutinesApi
  fun testInvokeAll_oneTask_afterShutdown_throwsException() {
    val executorService = createExecutorService()
    val callable1 = Callable { "Task 1" }
    val callable2 = Callable { "Task 2" }
    executorService.shutdown()

    val deferred = realDispatcherScope.async {
      executorService.invokeAll(listOf(callable1, callable2))
    }
    waitForDeferredWithDispatcher1(deferred)

    assertThat(deferred.getCompletionExceptionOrNull())
      .isInstanceOf(RejectedExecutionException::class.java)
  }

  @Test
  fun testInvokeAll_nullTasks_throwsException() {
    val executorService = createExecutorService()

    assertThrows(NullPointerException::class) { executorService.invokeAll<Int>(/* tasks= */ null) }
  }

  @Test
  @Suppress("BlockingMethodInNonBlockingContext") // Intentional to test blocking interruption.
  @ExperimentalCoroutinesApi
  fun testInvokeAll_oneTask_afterShutdownNow_throwsException() {
    val executorService = createExecutorService()
    val callable1 = Callable { "Task 1" }
    val callable2 = Callable { "Task 2" }
    executorService.shutdownNow()

    val deferred = realDispatcherScope.async {
      executorService.invokeAll(listOf(callable1, callable2))
    }
    waitForDeferredWithDispatcher1(deferred)

    assertThat(deferred.getCompletionExceptionOrNull())
      .isInstanceOf(RejectedExecutionException::class.java)
  }

  @Test
  @Suppress("BlockingMethodInNonBlockingContext") // Intentional to test blocking.
  @ExperimentalCoroutinesApi
  fun testInvokeAll_runTasks_returnsListOfCompletedFuturesWithCorrectValuesInOrder() {
    val executorService = createExecutorService()
    val callable1 = Callable { "Task 1" }
    val callable2 = Callable { "Task 2" }
    autoSettleServiceBeforeBlocking(executorService)

    val deferred = realDispatcherScope.async {
      executorService.invokeAll(listOf(callable1, callable2))
    }
    waitForDeferredWithDispatcher1(deferred)

    // Since the executor finished execution, the invokeAll() call should return.
    val (future1, future2) = deferred.getCompleted()
    assertThat(future1.isDone).isTrue()
    assertThat(future2.isDone).isTrue()
    assertThat(future1.get()).isEqualTo("Task 1")
    assertThat(future2.get()).isEqualTo("Task 2")
  }

  @Test
  @Suppress("BlockingMethodInNonBlockingContext") // Intentional to test blocking.
  @ExperimentalCoroutinesApi
  fun testInvokeAll_oneTaskFails_runTasks_returnsListOfCompletedFuturesWithCorrectValuesInOrder() {
    val executorService = createExecutorService()
    val callable1 = Callable<String> { throw Exception("Task 1 failed") }
    val callable2 = Callable { "Task 2" }
    autoSettleServiceBeforeBlocking(executorService)

    val deferred = realDispatcherScope.async {
      executorService.invokeAll(listOf(callable1, callable2))
    }
    waitForDeferredWithDispatcher1(deferred)

    // Since the executor finished execution, the invokeAll() call should return.
    val (future1, future2) = deferred.getCompleted()
    assertThat(future1.isDone).isTrue()
    assertThat(future2.isDone).isTrue()
    assertThrows(ExecutionException::class) { future1.get() }
    assertThat(future2.get()).isEqualTo("Task 2")
  }

  @Test
  @Suppress("BlockingMethodInNonBlockingContext") // Intentional to test blocking.
  @ExperimentalCoroutinesApi
  fun testInvokeAll_withTimeout_doNotFinishFirstTaskOnTime_timesOut() {
    val executorService = createExecutorService()
    // Note that a longer delay is used here since testing for timeouts is inherently flaky: slower
    // machines are more likely to trigger a flake since this relies on a real dispatcher. To guard
    // against flakes, a long timeout is picked.
    val callable1 = lateFinishingCallableWithDispatcher2(
      Callable { "Task 1" }, timeToWaitMillis = 2500L
    )
    val callable2 = Callable { "Task 2" }
    autoSettleServiceBeforeBlocking(executorService)

    val deferred = realDispatcherScope.async {
      executorService.invokeAll(
        listOf(callable1, callable2), /* timeout= */ 500, TimeUnit.MILLISECONDS
      )
    }
    // Note that this must be different than the dispatcher used to block callable1 to prevent
    // deadlocking.
    waitForDeferredWithDispatcher1(deferred)

    // Verify that the first task doesn't complete since it took too long to run.
    val (future1, future2) = deferred.getCompleted()
    assertThat(future1.isCancelled).isTrue()
    assertThat(future2.isDone).isTrue()
    assertThat(future2.isCancelled).isFalse()
    assertThat(future2.get()).isEqualTo("Task 2")
  }

  @Test
  @Suppress("BlockingMethodInNonBlockingContext") // Intentional to test blocking.
  @ExperimentalCoroutinesApi
  fun testInvokeAll_withTimeout_doNotFinishSecondTaskOnTime_timesOut() {
    val executorService = createExecutorService()
    // Note that a longer delay is used here since testing for timeouts is inherently flaky: slower
    // machines are more likely to trigger a flake since this relies on a real dispatcher. To guard
    // against flakes, a long timeout is picked.
    val callable1 = Callable { "Task 1" }
    val callable2 = lateFinishingCallableWithDispatcher2(
      Callable { "Task 2" }, timeToWaitMillis = 2500L
    )
    autoSettleServiceBeforeBlocking(executorService)

    val deferred = realDispatcherScope.async {
      executorService.invokeAll(
        listOf(callable1, callable2), /* timeout= */ 500, TimeUnit.MILLISECONDS
      )
    }
    // Note that this must be different than the dispatcher used to block callable1 to prevent
    // deadlocking.
    waitForDeferredWithDispatcher1(deferred)

    // Verify that the first task doesn't complete since it took too long to run.
    val (future1, future2) = deferred.getCompleted()
    assertThat(future1.isDone).isTrue()
    assertThat(future1.isCancelled).isFalse()
    assertThat(future1.get()).isEqualTo("Task 1")
    assertThat(future2.isCancelled).isTrue()
  }

  @Test
  @Suppress("BlockingMethodInNonBlockingContext") // Intentional to test blocking.
  fun testInvokeAny_doNotRunTasks_blocks() {
    val executorService = createExecutorService()
    val callable = Callable { "Task 1" }

    val deferred = testDispatcherScope2.async {
      executorService.invokeAny(listOf(callable))
    }

    assertThat(deferred.isCompleted).isFalse()
  }

  @Test
  @Suppress("BlockingMethodInNonBlockingContext") // Intentional to test blocking.
  @ExperimentalCoroutinesApi
  fun testInvokeAny_oneTask_runTasks_returnsValueOfFirstTask() {
    val executorService = createExecutorService()
    val callable = Callable { "Task 1" }
    autoSettleServiceBeforeBlocking(executorService)

    val deferred = realDispatcherScope.async {
      executorService.invokeAny(listOf(callable))
    }
    waitForDeferredWithDispatcher1(deferred)

    assertThat(deferred.getCompleted()).isEqualTo("Task 1")
  }

  @Test
  fun testInvokeAny_nullTasks_throwsException() {
    val executorService = createExecutorService()

    assertThrows(NullPointerException::class) { executorService.invokeAny<Int>(/* tasks= */ null) }
  }

  @Test
  @Suppress("BlockingMethodInNonBlockingContext") // Intentional to test blocking interruption.
  @ExperimentalCoroutinesApi
  fun testInvokeAny_oneTask_afterShutdown_throwsException() {
    val executorService = createExecutorService()
    val callable = Callable { "Task 1" }
    executorService.shutdown()
    autoSettleServiceBeforeBlocking(executorService)

    val deferred = realDispatcherScope.async {
      executorService.invokeAny(listOf(callable))
    }
    waitForDeferredWithDispatcher1(deferred)

    assertThat(deferred.getCompletionExceptionOrNull())
      .isInstanceOf(RejectedExecutionException::class.java)
  }

  @Test
  @Suppress("BlockingMethodInNonBlockingContext") // Intentional to test blocking interruption.
  @ExperimentalCoroutinesApi
  fun testInvokeAny_oneTask_afterShutdownNow_throwsException() {
    val executorService = createExecutorService()
    val callable = Callable { "Task 1" }
    executorService.shutdownNow()
    autoSettleServiceBeforeBlocking(executorService)

    val deferred = realDispatcherScope.async {
      executorService.invokeAny(listOf(callable))
    }
    waitForDeferredWithDispatcher1(deferred)

    assertThat(deferred.getCompletionExceptionOrNull())
      .isInstanceOf(RejectedExecutionException::class.java)
  }

  @Test
  @Suppress("BlockingMethodInNonBlockingContext") // Intentional to test blocking.
  @ExperimentalCoroutinesApi
  fun testInvokeAny_oneShortTask_oneLongTask_runTasks_returnsValueOfShortTask() {
    val executorService = createExecutorService()
    val delayMs = 10L
    val callable1 = lateFinishingCallableWithDispatcher2(
      Callable { "Long task" }, timeToWaitMillis = delayMs * 10
    )
    val callable2 = Callable { "Short task" }
    // Wait for the invokeAny selection to be fully arranged before executing, otherwise the delayed
    // task is guaranteed to be finished before selection happens (invalidating the selection
    // behavior). This is highly dependent on an implementation detail, but there's no other way to
    // force execution order to verify the service is doing the right thing for invokeAny.
    autoSettleServiceAfterSelection(executorService)

    val deferred = realDispatcherScope.async {
      executorService.invokeAny(listOf(callable1, callable2))
    }
    // Note that this must be different than the dispatcher used to block callable1 to prevent
    // deadlocking.
    waitForDeferredWithDispatcher1(deferred)

    assertThat(deferred.getCompleted()).isEqualTo("Short task")
  }

  @Test
  @Suppress("BlockingMethodInNonBlockingContext") // Intentional to test blocking.
  @ExperimentalCoroutinesApi
  fun testInvokeAny_oneShortTask_oneLongTask_shortTaskFails_runTasks_returnsValueOfLongTask() {
    val executorService = createExecutorService()
    val delayMs = 10L
    val callable1 = lateFinishingCallableWithDispatcher2(
      Callable { "Long task" }, timeToWaitMillis = delayMs * 10
    )
    val callable2 = Callable<String> { throw Exception("Failed task") }
    autoSettleServiceBeforeBlocking(executorService)

    val deferred = realDispatcherScope.async {
      executorService.invokeAny(listOf(callable1, callable2))
    }
    // Note that this must be different than the dispatcher used to block callable1 to prevent
    // deadlocking.
    waitForDeferredWithDispatcher1(deferred)

    // The short task failing should trigger the long task's result being received.
    assertThat(deferred.getCompleted()).isEqualTo("Long task")
  }

  @Test
  @Suppress("BlockingMethodInNonBlockingContext") // Intentional to test blocking.
  @ExperimentalCoroutinesApi
  @Ignore("Flaky test") // TODO(#1763): Remove & stabilize test.
  fun testInvokeAny_noTaskCompletesOnTime_throwsTimeoutException() {
    val executorService = createExecutorService()
    // Note that a longer delay is used here since testing for timeouts is inherently flaky: slower
    // machines are more likely to trigger a flake since this relies on a real dispatcher. To guard
    // against flakes, a long timeout is picked.
    val callable = lateFinishingCallableWithDispatcher2(
      Callable { "Long task" }, timeToWaitMillis = 2500L
    )
    autoSettleServiceAfterSelection(executorService)

    val deferred = realDispatcherScope.async {
      executorService.invokeAny(listOf(callable), /* timeout= */ 500, TimeUnit.MILLISECONDS)
    }
    // Note that this must be different than the dispatcher used to block callable1 to prevent
    // deadlocking.
    waitForDeferredWithDispatcher1(deferred)

    // The invokeAny call itself should fail with a TimeoutException since nothing finished in time.
    assertThat(deferred.getCompletionExceptionOrNull()).isInstanceOf(TimeoutException::class.java)
  }

  private fun setUpTestApplicationComponent() {
    DaggerCoroutineExecutorServiceTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  private fun createExecutorService(): ExecutorService {
    return CoroutineExecutorService(backgroundCoroutineDispatcher)
  }

  private fun createExecutorServiceAsExecutor(): Executor {
    return CoroutineExecutorService(backgroundCoroutineDispatcher)
  }

  private fun autoSettleServiceBeforeBlocking(executorService: ExecutorService) {
    (executorService as CoroutineExecutorService).setPriorToBlockingCallback {
      testCoroutineDispatchers.runCurrent()
    }
  }

  private fun autoSettleServiceAfterSelection(executorService: ExecutorService) {
    (executorService as CoroutineExecutorService).setAfterSelectionSetupCallback {
      testCoroutineDispatchers.runCurrent()
    }
  }

  private fun wrapRunnableWithOneSecondDelayUsingDispatcher1(runnable: Runnable): Runnable {
    return Runnable {
      wrapCallableWithOneSecondDelayUsingDispatcher1(Callable { runnable.run() }).call()
    }
  }

  private fun <T> wrapCallableWithOneSecondDelayUsingDispatcher1(
    callable: Callable<T>
  ): Callable<T> {
    return wrapCallableWithOneSecondDelay(callable, testDispatcherScope)
  }

  private fun <T> wrapCallableWithOneSecondDelay(
    callable: Callable<T>,
    coroutineScope: CoroutineScope
  ): Callable<T> {
    return Callable {
      val deferred = coroutineScope.async { delay(TimeUnit.SECONDS.toMillis(1)) }
      runBlocking {
        deferred.await()
        callable.call()
      }
    }
  }

  /** Waits for the specified deferred, or times out according to the test coroutine dispatchers. */
  private fun <T> waitForDeferredWithDispatcher1(deferred: Deferred<T>) {
    @Suppress("BlockingMethodInNonBlockingContext") // Intentional to test blocking.
    val deferredWait = testDispatcherScope.async {
      runBlocking {
        deferred.await()
      }
    }
    testDispatcher.runUntilIdle()
    assertThat(deferredWait.isCompleted).isTrue() // Sanity check.
  }

  @Suppress("DeferredResultUnused") // Deferred is indirectly blocked on via withContext.
  private fun <T> lateFinishingCallableWithDispatcher2(
    callable: Callable<T>,
    timeToWaitMillis: Long
  ): Callable<T> {
    return Callable {
      runBlocking {
        realDispatcherScope.async {
          delay(timeToWaitMillis)
          testDispatcher2.runUntilIdle()
        }
        withContext(testDispatcher2) {
          callable.call()
        }
      }
    }
  }

  // TODO(#89): Move to a common test library.
  private fun <T : Throwable> assertThrows(type: KClass<T>, operation: () -> Unit): T {
    try {
      operation()
      fail("Expected to encounter exception of $type")
    } catch (t: Throwable) {
      if (type.isInstance(t)) {
        return type.cast(t)
      }
      // Unexpected exception; throw it.
      throw t
    }
    throw AssertionError(
      "Reached an impossible state when verifying that an exception was thrown."
    )
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
      TestDispatcherModule::class, TestModule::class, TestLogReportingModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(coroutineExecutorServiceTest: CoroutineExecutorServiceTest)
  }
}
