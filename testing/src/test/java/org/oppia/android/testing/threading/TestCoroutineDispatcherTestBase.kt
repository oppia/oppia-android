package org.oppia.android.testing.threading

import com.google.common.truth.LongSubject
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.never
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import javax.inject.Inject
import com.google.common.collect.Range as GuavaRange

/**
 * Tests for [TestCoroutineDispatcher]. This provides the common tests needed to verify the
 * dispatcher API regardless of the implementation. This suite should not be used directly, and
 * instead should only be used by implementation test suites of [TestCoroutineDispatcher].
 *
 * NOTE TO DEVELOPERS: This is essentially the only time inheritance is good practice for tests
 * since JUnit doesn't provide a way to compose the tests themselves, only behaviors around the
 * tests.
 */
abstract class TestCoroutineDispatcherTestBase(
  /** Specifies how many millis should be used to separate future tasks. */
  protected val shortTaskDelayMillis: Long,
  /**
   * Specifies how many millis should be used to separate future tasks. These won't be run, and are
   * instead used when computing time thresholds between tasks.
   */
  protected val longTaskDelayMillis: Long,
  /**
   * The number of millis to check if two timestamps are the same when comparing long tasks
   * scheduled using [longTaskDelayMillis].
   */
  private val longTaskDelayDeltaCheckMillis: Long
) {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  @field:BackgroundTestDispatcher
  lateinit var backgroundTestDispatcher: TestCoroutineDispatcher

  @Mock
  lateinit var mockRunnable1: Runnable

  @Mock
  lateinit var mockRunnable2: Runnable

  @Mock
  lateinit var mockTaskIdleListener: TestCoroutineDispatcher.TaskIdleListener

  private val backgroundScope by lazy { CoroutineScope(backgroundTestDispatcher) }

  /**
   * Implementations should use this to set up the test application & verify that the dispatcher is
   * correct. The latter can be done with a call to [verifyDispatcherImplementation].
   */
  abstract fun setUp()

  /** Returns the current wall clock time, in milliseconds since the Unix epoch. */
  abstract fun getCurrentTimeMillis(): Long

  /** Moves the clock forward by the specified amount of time, based on the test environment. */
  abstract fun advanceTimeBy(timeMillis: Long)

  /**
   * Optionally attempts to stabilize the test thread after flushing all tasks in the test
   * dispatcher.
   */
  abstract fun stabilizeAfterDispatcherFlush()

  /**
   * Ensures that prior unstarted tasks scheduled via [scheduleFutureTask] are properly delayed for
   * the future. This will run any tasks scheduled via [scheduleImmediateTask], so those should be
   * scheduled after this function is called.
   */
  abstract fun ensureFutureTasksAreScheduled()

  @Test
  fun testDispatcher_scheduleFutureTask_doesNotRun() {
    scheduleFutureTask(shortTaskDelayMillis, mockRunnable1)

    verify(mockRunnable1, never()).run()
  }

  @Test
  fun testDispatcher_scheduleImmediateTask_runCurrent_runsTask() {
    scheduleImmediateTask(mockRunnable1)

    backgroundTestDispatcher.runCurrent()
    stabilizeAfterDispatcherFlush()

    verify(mockRunnable1).run()
  }

  @Test
  fun testDispatcher_scheduleImmediateTask_runCurrentTwice_runsTaskOnce() {
    scheduleImmediateTask(mockRunnable1)

    // Call runCurrent twice.
    backgroundTestDispatcher.runCurrent()
    backgroundTestDispatcher.runCurrent()
    stabilizeAfterDispatcherFlush()

    // Verify that the task was only run once.
    verify(mockRunnable1).run()
  }

  @Test
  fun testDispatcher_scheduleTwoImmediateTasks_runCurrent_runsBothTasks() {
    scheduleImmediateTask(mockRunnable1)
    scheduleImmediateTask(mockRunnable2)

    backgroundTestDispatcher.runCurrent()
    stabilizeAfterDispatcherFlush()

    verify(mockRunnable1).run()
    verify(mockRunnable2).run()
  }

  @Test
  fun testDispatcher_scheduleImmediateTasks_runCurrentBetween_runsEachTask() {
    scheduleImmediateTask(mockRunnable1)
    backgroundTestDispatcher.runCurrent()
    scheduleImmediateTask(mockRunnable2)
    backgroundTestDispatcher.runCurrent()
    stabilizeAfterDispatcherFlush()

    verify(mockRunnable1).run()
    verify(mockRunnable2).run()
  }

  @Test
  fun testDispatcher_scheduleFutureTask_runCurrent_doesNotRun() {
    // Use a long delay to avoid inadvertently running the task.
    scheduleFutureTask(longTaskDelayMillis, mockRunnable1)
    ensureFutureTasksAreScheduled()

    // Calling runCurrent after the future task is scheduled does not result in the task running.
    backgroundTestDispatcher.runCurrent()
    // Do not stabilize the dispatcher since this test is verifying a task does not run--doing so
    // could inadvertently run the task.

    verify(mockRunnable1, never()).run()
  }

  @Test
  fun testDispatcher_scheduleFutureTask_advanceTimePartway_runCurrent_doesNotRun() {
    // Use a long delay to avoid inadvertently running the task.
    scheduleFutureTask(longTaskDelayMillis, mockRunnable1)
    ensureFutureTasksAreScheduled()

    advanceTimeBy(shortTaskDelayMillis / 2)
    backgroundTestDispatcher.runCurrent()
    // Don't stabilize since this test is ensuring tasks weren't run.

    // Time wasn't advanced far enough to run the task.
    verify(mockRunnable1, never()).run()
  }

  @Test
  fun testDispatcher_scheduleFutureTask_advanceTimeToTaskTime_runCurrent_runsTask() {
    scheduleFutureTask(shortTaskDelayMillis, mockRunnable1)
    ensureFutureTasksAreScheduled()

    advanceTimeBy(shortTaskDelayMillis)
    backgroundTestDispatcher.runCurrent()
    stabilizeAfterDispatcherFlush()

    // Advancing time exactly to the threshold should run the task.
    verify(mockRunnable1).run()
  }

  @Test
  fun testDispatcher_scheduleFutureTask_advanceTimePastTaskTime_runCurrent_runsTask() {
    scheduleFutureTask(shortTaskDelayMillis, mockRunnable1)
    ensureFutureTasksAreScheduled()

    advanceTimeBy(shortTaskDelayMillis + shortTaskDelayMillis)
    backgroundTestDispatcher.runCurrent()
    stabilizeAfterDispatcherFlush()

    // Advancing past the task should result in it being run.
    verify(mockRunnable1).run()
  }

  @Test
  fun testDispatcher_scheduleFutureAndImmediateTasks_advancePast_runCurrent_runsBoth() {
    scheduleFutureTask(shortTaskDelayMillis, mockRunnable1)
    ensureFutureTasksAreScheduled()
    scheduleImmediateTask(mockRunnable2)

    advanceTimeBy(shortTaskDelayMillis + shortTaskDelayMillis)
    backgroundTestDispatcher.runCurrent()
    stabilizeAfterDispatcherFlush()

    // Both immediate and future tasks should run, but the order isn't well defined since they're
    // both executing in the same time quantum.
    verify(mockRunnable2).run()
    verify(mockRunnable1).run()
  }

  @Test
  fun testDispatcher_scheduleTwoFutureTasks_advanceTimeBetweenTasks_runCurrent_firstTaskRuns() {
    val taskDelayMs1 = shortTaskDelayMillis
    // Use a long task delay for the second task since it's specifically desired to not
    // inadvertently run the second task.
    val taskDelayMs2 = taskDelayMs1 + longTaskDelayMillis
    val midTaskDelay = (taskDelayMs1 + taskDelayMs2) / 2
    scheduleFutureTask(taskDelayMs1, mockRunnable1)
    scheduleFutureTask(taskDelayMs2, mockRunnable2)
    ensureFutureTasksAreScheduled()

    advanceTimeBy(midTaskDelay)
    backgroundTestDispatcher.runCurrent()
    // Don't stabilize after running since we don't want to unintentionally execute the second task.

    // Advancing between the two tasks should run the first but not the second.
    verify(mockRunnable1).run()
    verify(mockRunnable2, never()).run()
  }

  @Test
  fun testDispatcher_scheduleTwoFutureTasks_advancePastSecondTask_runCurrent_runsBoth() {
    val taskDelayMs1 = shortTaskDelayMillis
    val taskDelayMs2 = taskDelayMs1 + shortTaskDelayMillis
    scheduleFutureTask(taskDelayMs2, mockRunnable2)
    scheduleFutureTask(taskDelayMs1, mockRunnable1)
    ensureFutureTasksAreScheduled()

    advanceTimeBy(taskDelayMs2 + shortTaskDelayMillis)
    backgroundTestDispatcher.runCurrent()
    stabilizeAfterDispatcherFlush()

    // Both scheduled tasks should run, but the order isn't well defined since they're both
    // executing in the same time quantum.
    verify(mockRunnable2).run()
    verify(mockRunnable1).run()
  }

  @Test
  @Suppress("UnnecessaryVariable") // Extra variables are for readability.
  fun testDispatcher_scheduleTwoFutureTasks_advancePastSecondTask_threeSteps_runsBoth() {
    val taskStepMs = shortTaskDelayMillis
    val taskDelayMs1 = taskStepMs
    val taskDelayMs2 = taskDelayMs1 + taskStepMs
    scheduleFutureTask(taskDelayMs2, mockRunnable2)
    scheduleFutureTask(taskDelayMs1, mockRunnable1)
    ensureFutureTasksAreScheduled()

    // First step: run up to the first task.
    advanceTimeBy(taskStepMs - 1)
    backgroundTestDispatcher.runCurrent()
    // Second step: run the first task.
    advanceTimeBy(2)
    backgroundTestDispatcher.runCurrent()
    // Third step: run the second task.
    advanceTimeBy(taskStepMs)
    backgroundTestDispatcher.runCurrent()
    stabilizeAfterDispatcherFlush()

    // Both scheduled tasks should run, but the order isn't well defined since they're both
    // executing in the same time quantum.
    verify(mockRunnable2).run()
    verify(mockRunnable1).run()
  }

  @Test
  fun testDispatcher_hasPendingTasks_noTasks_returnsFalse() {
    val hasPendingTasks = backgroundTestDispatcher.hasPendingTasks()

    // There should be no tasks initially.
    assertThat(hasPendingTasks).isFalse()
  }

  @Test
  fun testDispatcher_hasPendingTasks_oneFutureTask_returnsTrue() {
    // Use a longer delay to provide time for the pending tasks check.
    scheduleFutureTask(longTaskDelayMillis, mockRunnable1)
    ensureFutureTasksAreScheduled()

    val hasPendingTasks = backgroundTestDispatcher.hasPendingTasks()

    assertThat(hasPendingTasks).isTrue()
  }

  @Test
  fun testDispatcher_getNextFutureTaskCompletionTimeMillis_timeNow_noTasks_returnsNull() {
    val currentTimeMillis = getCurrentTimeMillis()
    val nextTaskCompletionTime =
      backgroundTestDispatcher.getNextFutureTaskCompletionTimeMillis(currentTimeMillis)

    assertThat(nextTaskCompletionTime).isNull()
  }

  @Test
  fun testDispatcher_getNextFutureTaskCompletionTimeMillis_timeNow_oneImmediateTask_returnsNull() {
    scheduleImmediateTask(mockRunnable1)

    val currentTimeMillis = getCurrentTimeMillis()
    val nextTaskCompletionTime =
      backgroundTestDispatcher.getNextFutureTaskCompletionTimeMillis(currentTimeMillis)

    // The function returns null because the next task that can be run is now, so it's considered
    // past.
    assertThat(nextTaskCompletionTime).isNull()
  }

  @Test
  fun testDispatcher_getNextFutureTaskCompletionTimeMillis_timeNow_oneFutureTask_returnsTaskTime() {
    scheduleFutureTask(longTaskDelayMillis, mockRunnable1)
    ensureFutureTasksAreScheduled()

    val currentTimeMillis = getCurrentTimeMillis()
    val nextTaskCompletionTime =
      backgroundTestDispatcher.getNextFutureTaskCompletionTimeMillis(currentTimeMillis)

    // The next task is in the future.
    val estimatedTaskStartTimeMillis = currentTimeMillis + longTaskDelayMillis
    assertThat(nextTaskCompletionTime).isNotNull()
    assertThat(nextTaskCompletionTime).isWithinTimeDelta(estimatedTaskStartTimeMillis)
  }

  @Test
  fun testDispatcher_getTaskCompletionTimeMillis_timeAtNextTask_oneFutureTask_returnsNull() {
    scheduleFutureTask(longTaskDelayMillis, mockRunnable1)
    ensureFutureTasksAreScheduled()

    val currentTimeMillis = getCurrentTimeMillis()
    val expectedRunTime = currentTimeMillis + longTaskDelayMillis
    val nextTaskCompletionTime =
      backgroundTestDispatcher.getNextFutureTaskCompletionTimeMillis(expectedRunTime)

    // The function returns null because the queried time is exactly at the time the task is
    // being run.
    assertThat(nextTaskCompletionTime).isNull()
  }

  @Test
  fun testDispatcher_getTaskCompletionTimeMillis_timeInFarFuture_oneFutureTask_returnsNull() {
    scheduleFutureTask(longTaskDelayMillis, mockRunnable1)
    ensureFutureTasksAreScheduled()

    val currentTimeMillis = getCurrentTimeMillis()
    val nextTaskCompletionTime =
      backgroundTestDispatcher.getNextFutureTaskCompletionTimeMillis(currentTimeMillis + 100_000L)

    // The queried time is past scheduled tasks, so none need to be run after that time.
    assertThat(nextTaskCompletionTime).isNull()
  }

  @Test
  fun testDispatcher_getTaskCompletion_timeInFuture_futureAndImmediateTasks_returnsNull() {
    scheduleFutureTask(longTaskDelayMillis, mockRunnable1)
    ensureFutureTasksAreScheduled()
    scheduleImmediateTask(mockRunnable2)

    val currentTimeMillis = getCurrentTimeMillis()
    val nextTaskCompletionTime =
      backgroundTestDispatcher.getNextFutureTaskCompletionTimeMillis(currentTimeMillis + 100_000L)

    // Since the next task to be run is now, there are no "next future tasks".
    assertThat(nextTaskCompletionTime).isNull()
  }

  @Test
  fun testDispatcher_getTaskCompletion_timeNow_futureAndImmediateTasks_returnsNextTaskTime() {
    scheduleFutureTask(longTaskDelayMillis, mockRunnable1)
    ensureFutureTasksAreScheduled()
    scheduleImmediateTask(mockRunnable2)

    val currentTimeMillis = getCurrentTimeMillis()
    val nextTaskCompletionTime =
      backgroundTestDispatcher.getNextFutureTaskCompletionTimeMillis(currentTimeMillis)

    // The next future task to run relative to now is the scheduled task.
    val estimatedTaskStartTimeMillis = currentTimeMillis + longTaskDelayMillis
    assertThat(nextTaskCompletionTime).isNotNull()
    assertThat(nextTaskCompletionTime).isWithinTimeDelta(estimatedTaskStartTimeMillis)
  }

  @Test
  fun testDispatcher_getTaskCompletion_twoFutureTasks_timeBeforeFirst_returnsFirstTaskTime() {
    val taskDelayMs1 = longTaskDelayMillis
    val taskDelayMs2 = taskDelayMs1 + longTaskDelayMillis
    scheduleFutureTask(taskDelayMs1, mockRunnable1)
    scheduleFutureTask(taskDelayMs2, mockRunnable2)
    ensureFutureTasksAreScheduled()

    val currentTimeMillis = getCurrentTimeMillis()
    val nextTaskCompletionTime =
      backgroundTestDispatcher.getNextFutureTaskCompletionTimeMillis(currentTimeMillis)

    // The next task to run is the first.
    val estimatedTaskStartTimeMillis = currentTimeMillis + taskDelayMs1
    assertThat(nextTaskCompletionTime).isNotNull()
    assertThat(nextTaskCompletionTime).isWithinTimeDelta(estimatedTaskStartTimeMillis)
  }

  @Test
  fun testDispatcher_getTaskCompletion_twoFutureTasks_timeBetween_returnsSecondTaskTime() {
    val taskDelayMs1 = longTaskDelayMillis
    val taskDelayMs2 = taskDelayMs1 + longTaskDelayMillis
    scheduleFutureTask(taskDelayMs1, mockRunnable1)
    scheduleFutureTask(taskDelayMs2, mockRunnable2)
    ensureFutureTasksAreScheduled()

    val currentTimeMillis = getCurrentTimeMillis()
    val requestedTimeMills = currentTimeMillis + (taskDelayMs1 + taskDelayMs2) / 2
    val nextTaskCompletionTime =
      backgroundTestDispatcher.getNextFutureTaskCompletionTimeMillis(requestedTimeMills)

    // The second task to run is the next to run relative to the requested time.
    val estimatedTaskStartTimeMillis = currentTimeMillis + taskDelayMs2
    assertThat(nextTaskCompletionTime).isNotNull()
    assertThat(nextTaskCompletionTime).isWithinTimeDelta(estimatedTaskStartTimeMillis)
  }

  @Test
  fun testDispatcher_getTaskCompletion_twoFutureTasks_timeFarFuture_returnsNull() {
    val taskDelayMs1 = longTaskDelayMillis
    val taskDelayMs2 = taskDelayMs1 + longTaskDelayMillis
    scheduleFutureTask(taskDelayMs1, mockRunnable1)
    scheduleFutureTask(taskDelayMs2, mockRunnable2)
    ensureFutureTasksAreScheduled()

    val currentTimeMillis = getCurrentTimeMillis()
    val nextTaskCompletionTime =
      backgroundTestDispatcher.getNextFutureTaskCompletionTimeMillis(currentTimeMillis + 100_000L)

    // There are no tasks left after the requested time.
    assertThat(nextTaskCompletionTime).isNull()
  }

  @Test
  fun testDispatcher_getTaskCompletion_timeNow_futureImmediateTasks_runCurrent_returnsNextTime() {
    scheduleFutureTask(longTaskDelayMillis, mockRunnable1)
    ensureFutureTasksAreScheduled()
    scheduleImmediateTask(mockRunnable2)

    // Note that stabilize isn't called here since for the Espresso implementation it will lead to
    // the scheduled task being completed.
    backgroundTestDispatcher.runCurrent()
    val currentTimeMillis = getCurrentTimeMillis()
    val nextTaskCompletionTime =
      backgroundTestDispatcher.getNextFutureTaskCompletionTimeMillis(currentTimeMillis)

    // Since the immediate task was run, the next scheduled one is returned.
    val estimatedTaskStartTimeMillis = currentTimeMillis + longTaskDelayMillis
    assertThat(nextTaskCompletionTime).isNotNull()
    assertThat(nextTaskCompletionTime).isWithinTimeDelta(estimatedTaskStartTimeMillis)
  }

  @Test
  fun testDispatcher_getTaskCompletion_timeNow_futureImmediateTasks_advanceAndRun_returnsNull() {
    scheduleFutureTask(shortTaskDelayMillis, mockRunnable1)
    ensureFutureTasksAreScheduled()
    scheduleImmediateTask(mockRunnable2)

    advanceTimeBy(shortTaskDelayMillis)
    backgroundTestDispatcher.runCurrent()
    stabilizeAfterDispatcherFlush()
    val currentTimeMillis = getCurrentTimeMillis()
    val nextTaskCompletionTime =
      backgroundTestDispatcher.getNextFutureTaskCompletionTimeMillis(currentTimeMillis)

    // Even though the previous time millis is being used to query, there are no tasks left to
    // return since both were executed.
    assertThat(nextTaskCompletionTime).isNull()
  }

  // Note that the tests for verifying when hasPendingCompletableTasks returns true are
  // implementation-specific. See the Espresso/Robolectric subclass test suites for those specific
  // tests.

  @Test
  fun testDispatcher_hasPendingCompletableTasks_noTasks_returnsFalse() {
    val hasCompletableTasks = backgroundTestDispatcher.hasPendingCompletableTasks()

    // There should be no completable tasks initially.
    assertThat(hasCompletableTasks).isFalse()
  }

  @Test
  fun testDispatcher_hasPendingCompletableTasks_immediateAndFutureTask_runCurrent_returnsFalse() {
    scheduleFutureTask(shortTaskDelayMillis, mockRunnable1)
    ensureFutureTasksAreScheduled()
    scheduleImmediateTask(mockRunnable2)

    backgroundTestDispatcher.runCurrent()
    stabilizeAfterDispatcherFlush()
    val hasCompletableTasks = backgroundTestDispatcher.hasPendingCompletableTasks()

    // There is no longer a completable task (the immediate task was run, and it's not yet time to
    // run the scheduled one).
    assertThat(hasCompletableTasks).isFalse()
  }

  @Test
  fun testDispatcher_hasCompletableTasks_immediateFutureTask_runBoth_returnsFalse() {
    scheduleFutureTask(shortTaskDelayMillis, mockRunnable1)
    ensureFutureTasksAreScheduled()
    scheduleImmediateTask(mockRunnable2)

    advanceTimeBy(shortTaskDelayMillis)
    backgroundTestDispatcher.runCurrent()
    stabilizeAfterDispatcherFlush()
    val hasCompletableTasks = backgroundTestDispatcher.hasPendingCompletableTasks()

    // Running all tasks results in none left being completable.
    assertThat(hasCompletableTasks).isFalse()
  }

  @Test
  fun testDispatcher_hasCompletableTasks_twoFutureTasks_runOne_returnsFalse() {
    val taskDelayMs1 = shortTaskDelayMillis
    val taskDelayMs2 = taskDelayMs1 + shortTaskDelayMillis
    scheduleFutureTask(taskDelayMs1, mockRunnable1)
    scheduleFutureTask(taskDelayMs2, mockRunnable2)
    ensureFutureTasksAreScheduled()

    advanceTimeBy(taskDelayMs1)
    backgroundTestDispatcher.runCurrent()
    stabilizeAfterDispatcherFlush()
    val hasCompletableTasks = backgroundTestDispatcher.hasPendingCompletableTasks()

    // While there's a second task, it's not completable yet since time hasn't been advanced.
    assertThat(hasCompletableTasks).isFalse()
  }

  @Test
  fun testDispatcher_hasCompletableTasks_twoFutureTasks_runBoth_returnsFalse() {
    val taskDelayMs1 = shortTaskDelayMillis
    val taskDelayMs2 = taskDelayMs1 + shortTaskDelayMillis
    scheduleFutureTask(taskDelayMs1, mockRunnable1)
    scheduleFutureTask(taskDelayMs2, mockRunnable2)
    ensureFutureTasksAreScheduled()

    advanceTimeBy(taskDelayMs2)
    backgroundTestDispatcher.runCurrent()
    stabilizeAfterDispatcherFlush()
    val hasCompletableTasks = backgroundTestDispatcher.hasPendingCompletableTasks()

    // Running both tasks results in no completable tasks.
    assertThat(hasCompletableTasks).isFalse()
  }

  @Test
  fun testDispatcher_taskIdleListener_initialRegistration_isInitiallyIdle() {
    backgroundTestDispatcher.setTaskIdleListener(mockTaskIdleListener)

    verify(mockTaskIdleListener).onDispatcherIdle()
    verifyNoMoreInteractions(mockTaskIdleListener)
  }

  @Test
  fun testDispatcher_taskIdleListener_scheduleFutureTask_run_isRunningThenIdle() {
    backgroundTestDispatcher.setTaskIdleListener(mockTaskIdleListener)
    reset(mockTaskIdleListener)

    scheduleFutureTask(shortTaskDelayMillis, mockRunnable1)
    ensureFutureTasksAreScheduled()
    advanceTimeBy(shortTaskDelayMillis)
    backgroundTestDispatcher.runCurrent()
    stabilizeAfterDispatcherFlush()

    // The listener should have entered a running state, then become idle immediately after the task
    // was completed.
    val inOrder = inOrder(mockTaskIdleListener)
    inOrder.verify(mockTaskIdleListener, atLeastOnce()).onDispatcherRunning()
    inOrder.verify(mockTaskIdleListener, atLeastOnce()).onDispatcherIdle()
    inOrder.verifyNoMoreInteractions()
  }

  private fun LongSubject.isWithinTimeDelta(time: Long) =
    isWithin(time - longTaskDelayDeltaCheckMillis..time + longTaskDelayDeltaCheckMillis)

  private fun LongSubject.isWithin(range: LongRange) = isIn(range.toGuavaRange())

  private fun <T : Comparable<T>> ClosedRange<T>.toGuavaRange(): GuavaRange<T> {
    return GuavaRange.open(/* lower= */ start, /* upper= */ endInclusive)
  }

  /** Schedules a [Runnable] to run without a delay. */
  protected fun scheduleImmediateTask(runnable: Runnable) {
    backgroundScope.launch { runnable.run() }
  }

  /** Schedules a [Runnable] to run after [delayMs] milliseconds. */
  protected fun scheduleFutureTask(delayMs: Long, runnable: Runnable) {
    backgroundScope.launch {
      delay(delayMs)
      runnable.run()
    }
  }

  protected inline fun <reified T : TestCoroutineDispatcher> verifyDispatcherImplementation() {
    // Sanity check to ensure the correct implementation is being tested.
    assertThat(backgroundTestDispatcher).isInstanceOf(T::class.java)
  }
}
