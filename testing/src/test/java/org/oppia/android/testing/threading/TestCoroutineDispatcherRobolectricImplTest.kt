package org.oppia.android.testing.threading

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Subject
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.atLeast
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.never
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.verifyZeroInteractions
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.IsOnRobolectric
import org.oppia.android.testing.time.FakeSystemClock
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

/** Tests for [TestCoroutineDispatcherRobolectricImpl]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = TestCoroutineDispatcherRobolectricImplTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class TestCoroutineDispatcherRobolectricImplTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  @field:BackgroundTestDispatcher
  lateinit var backgroundTestDispatcher: TestCoroutineDispatcher

  @Inject
  lateinit var fakeSystemClock: FakeSystemClock

  @Mock
  lateinit var mockRunnable1: Runnable

  @Mock
  lateinit var mockRunnable2: Runnable

  @Mock
  lateinit var mockTaskIdleListener: TestCoroutineDispatcher.TaskIdleListener

  private val backgroundScope by lazy { CoroutineScope(backgroundTestDispatcher) }

  @Before
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun setUp() {
    setUpTestApplicationComponent()
    verifyDispatcherImplementations()
  }

  @Test
  fun testDispatcher_scheduleImmediateTask_doesNotRun() {
    scheduleImmediateTask(mockRunnable1)

    verify(mockRunnable1, never()).run()
  }

  @Test
  fun testDispatcher_scheduleFutureTask_doesNotRun() {
    scheduleFutureTask(delayMs = 100L, mockRunnable1)

    verify(mockRunnable1, never()).run()
  }

  @Test
  fun testDispatcher_scheduleFutureTask_prepareFutureTask_doesNotRun() {
    scheduleFutureTask(delayMs = 100L, mockRunnable1)
    ensureFutureTasksAreScheduled()

    verify(mockRunnable1, never()).run()
  }

  @Test
  fun testDispatcher_scheduleImmediateTask_runCurrent_runsTask() {
    scheduleImmediateTask(mockRunnable1)

    backgroundTestDispatcher.runCurrent()

    verify(mockRunnable1).run()
  }

  @Test
  fun testDispatcher_scheduleImmediateTask_runCurrentTwice_runsTaskOnce() {
    scheduleImmediateTask(mockRunnable1)

    // Call runCurrent twice.
    backgroundTestDispatcher.runCurrent()
    backgroundTestDispatcher.runCurrent()

    // Verify that the task was only run once.
    verify(mockRunnable1).run()
  }

  @Test
  fun testDispatcher_scheduleTwoImmediateTasks_runCurrent_runsBothTasks() {
    scheduleImmediateTask(mockRunnable1)
    scheduleImmediateTask(mockRunnable2)

    backgroundTestDispatcher.runCurrent()

    verify(mockRunnable1).run()
    verify(mockRunnable2).run()
  }

  @Test
  fun testDispatcher_scheduleImmediateTasks_runCurrentBetween_runsEachTask() {

    scheduleImmediateTask(mockRunnable1)
    backgroundTestDispatcher.runCurrent()
    scheduleImmediateTask(mockRunnable2)
    backgroundTestDispatcher.runCurrent()

    verify(mockRunnable1).run()
    verify(mockRunnable2).run()
  }

  @Test
  fun testDispatcher_scheduleFutureTask_runCurrent_doesNotRun() {
    scheduleFutureTask(delayMs = 100L, mockRunnable1)
    ensureFutureTasksAreScheduled()

    // Calling runCurrent after the future task is scheduled does not result in the task running.
    backgroundTestDispatcher.runCurrent()

    verify(mockRunnable1, never()).run()
  }

  @Test
  fun testDispatcher_scheduleFutureTask_advanceTimePartway_runCurrent_doesNotRun() {
    scheduleFutureTask(delayMs = 100L, mockRunnable1)
    ensureFutureTasksAreScheduled()

    fakeSystemClock.advanceTime(millis = 50L)
    backgroundTestDispatcher.runCurrent()

    // Time wasn't advanced far enough to run the task.
    verify(mockRunnable1, never()).run()
  }

  @Test
  fun testDispatcher_scheduleFutureTask_advanceTimeToTaskTime_doesNotRun() {
    val taskDelayMs = 100L
    scheduleFutureTask(taskDelayMs, mockRunnable1)
    ensureFutureTasksAreScheduled()

    fakeSystemClock.advanceTime(taskDelayMs)

    // Advancing time without running the dispatcher will not result in the task being called.
    verify(mockRunnable1, never()).run()
  }

  @Test
  fun testDispatcher_scheduleFutureTask_advanceTimeToTaskTime_runCurrent_runsTask() {
    val taskDelayMs = 100L
    scheduleFutureTask(taskDelayMs, mockRunnable1)
    ensureFutureTasksAreScheduled()

    fakeSystemClock.advanceTime(taskDelayMs)
    backgroundTestDispatcher.runCurrent()

    // Advancing time exactly to the threshold should run the task.
    verify(mockRunnable1).run()
  }

  @Test
  fun testDispatcher_scheduleFutureTask_advanceTimePastTaskTime_runCurrent_runsTask() {
    val taskDelayMs = 100L
    scheduleFutureTask(taskDelayMs, mockRunnable1)
    ensureFutureTasksAreScheduled()

    fakeSystemClock.advanceTime(taskDelayMs + 100L)
    backgroundTestDispatcher.runCurrent()

    // Advancing past the task should result in it being run.
    verify(mockRunnable1).run()
  }

  @Test
  fun testDispatcher_scheduleFutureAndImmediateTasks_advancePast_runCurrent_runsBoth() {
    val taskDelayMs = 100L
    scheduleFutureTask(taskDelayMs, mockRunnable1)
    ensureFutureTasksAreScheduled()
    scheduleImmediateTask(mockRunnable2)

    fakeSystemClock.advanceTime(taskDelayMs + 100L)
    backgroundTestDispatcher.runCurrent()

    // Both immediate and future tasks should run, but the order isn't well defined since they're
    // both executing in the same time quantum.
    verify(mockRunnable2).run()
    verify(mockRunnable1).run()
  }

  @Test
  fun testDispatcher_scheduleTwoFutureTasks_advanceTimeBetweenTasks_runCurrent_firstTaskRuns() {
    val taskDelayMs1 = 100L
    val taskDelayMs2 = taskDelayMs1 + 100L
    val midTaskDelay = (taskDelayMs1 + taskDelayMs2) / 2
    scheduleFutureTask(taskDelayMs1, mockRunnable1)
    scheduleFutureTask(taskDelayMs2, mockRunnable2)
    ensureFutureTasksAreScheduled()

    fakeSystemClock.advanceTime(midTaskDelay)
    backgroundTestDispatcher.runCurrent()

    // Advancing between the two tasks should run the first but not the second.
    verify(mockRunnable1).run()
    verify(mockRunnable2, never()).run()
  }

  @Test
  fun testDispatcher_scheduleTwoFutureTasks_advancePastSecondTask_runCurrent_runsBoth() {
    val taskDelayMs1 = 100L
    val taskDelayMs2 = taskDelayMs1 + 100L
    scheduleFutureTask(taskDelayMs2, mockRunnable2)
    scheduleFutureTask(taskDelayMs1, mockRunnable1)
    ensureFutureTasksAreScheduled()

    fakeSystemClock.advanceTime(taskDelayMs2 + 100L)
    backgroundTestDispatcher.runCurrent()

    // Both scheduled tasks should run, but the order isn't well defined since they're both
    // executing in the same time quantum.
    verify(mockRunnable2).run()
    verify(mockRunnable1).run()
  }

  @Test
  @Suppress("UnnecessaryVariable") // Extra variables are for readability.
  fun testDispatcher_scheduleTwoFutureTasks_advancePastSecondTask_threeSteps_runsBoth() {
    val taskStepMs = 100L
    val taskDelayMs1 = taskStepMs
    val taskDelayMs2 = taskDelayMs1 + taskStepMs
    scheduleFutureTask(taskDelayMs2, mockRunnable2)
    scheduleFutureTask(taskDelayMs1, mockRunnable1)
    ensureFutureTasksAreScheduled()

    // First step: run up to the first task.
    fakeSystemClock.advanceTime(taskStepMs - 1)
    backgroundTestDispatcher.runCurrent()
    // Second step: run the first task.
    fakeSystemClock.advanceTime(2)
    backgroundTestDispatcher.runCurrent()
    // Third step: run the second task.
    fakeSystemClock.advanceTime(taskStepMs)
    backgroundTestDispatcher.runCurrent()

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
  fun testDispatcher_hasPendingTasks_oneImmediateTask_returnsTrue() {
    scheduleImmediateTask(mockRunnable1)

    val hasPendingTasks = backgroundTestDispatcher.hasPendingTasks()

    assertThat(hasPendingTasks).isTrue()
  }

  @Test
  fun testDispatcher_hasPendingTasks_oneFutureTask_returnsTrue() {
    scheduleFutureTask(delayMs = 100L, mockRunnable1)
    ensureFutureTasksAreScheduled()

    val hasPendingTasks = backgroundTestDispatcher.hasPendingTasks()

    assertThat(hasPendingTasks).isTrue()
  }

  @Test
  fun testDispatcher_getNextFutureTaskCompletionTimeMillis_timeNow_noTasks_returnsNull() {
    val currentTimeMillis = fakeSystemClock.getTimeMillis()
    val nextTaskCompletionTime =
      backgroundTestDispatcher.getNextFutureTaskCompletionTimeMillis(currentTimeMillis)

    assertThat(nextTaskCompletionTime).isNull()
  }

  @Test
  fun testDispatcher_getNextFutureTaskCompletionTimeMillis_timeNow_oneImmediateTask_returnsNull() {
    scheduleImmediateTask(mockRunnable1)

    val currentTimeMillis = fakeSystemClock.getTimeMillis()
    val nextTaskCompletionTime =
      backgroundTestDispatcher.getNextFutureTaskCompletionTimeMillis(currentTimeMillis)

    // The function returns null because the next task that can be run is now, so it's considered
    // past.
    assertThat(nextTaskCompletionTime).isNull()
  }

  @Test
  fun testDispatcher_getNextFutureTaskCompletionTimeMillis_timeNow_oneFutureTask_returnsTaskTime() {
    val taskDelayMs = 100L
    scheduleFutureTask(taskDelayMs, mockRunnable1)
    ensureFutureTasksAreScheduled()

    val currentTimeMillis = fakeSystemClock.getTimeMillis()
    val nextTaskCompletionTime =
      backgroundTestDispatcher.getNextFutureTaskCompletionTimeMillis(currentTimeMillis)

    // The next task is in the future.
    assertThat(nextTaskCompletionTime).isNotNull()
    assertThat(nextTaskCompletionTime).isEqualTo(currentTimeMillis + taskDelayMs)
  }

  @Test
  fun testDispatcher_getTaskCompletionTimeMillis_timeAtNextTask_oneFutureTask_returnsNull() {
    val taskDelayMs = 100L
    val currentTimeMillis = fakeSystemClock.getTimeMillis()
    val expectedRunTime = currentTimeMillis + taskDelayMs
    scheduleFutureTask(taskDelayMs, mockRunnable1)
    ensureFutureTasksAreScheduled()

    val nextTaskCompletionTime =
      backgroundTestDispatcher.getNextFutureTaskCompletionTimeMillis(expectedRunTime)

    // The function returns null because the queried time is exactly at the time the task is
    // being run.
    assertThat(nextTaskCompletionTime).isNull()
  }

  @Test
  fun testDispatcher_getTaskCompletionTimeMillis_timeInFarFuture_oneFutureTask_returnsNull() {
    val taskDelayMs = 100L
    val currentTimeMillis = fakeSystemClock.getTimeMillis()
    scheduleFutureTask(taskDelayMs, mockRunnable1)
    ensureFutureTasksAreScheduled()

    val nextTaskCompletionTime =
      backgroundTestDispatcher.getNextFutureTaskCompletionTimeMillis(currentTimeMillis + 10000L)

    // The queried time is past scheduled tasks, so none need to be run after that time.
    assertThat(nextTaskCompletionTime).isNull()
  }

  @Test
  fun testDispatcher_getTaskCompletion_timeInFuture_futureAndImmediateTasks_returnsNull() {
    val taskDelayMs = 100L
    val currentTimeMillis = fakeSystemClock.getTimeMillis()
    scheduleFutureTask(taskDelayMs, mockRunnable1)
    ensureFutureTasksAreScheduled()
    scheduleImmediateTask(mockRunnable2)

    val nextTaskCompletionTime =
      backgroundTestDispatcher.getNextFutureTaskCompletionTimeMillis(currentTimeMillis + 10_000L)

    // Since the next task to be run is now, there are no "next future tasks".
    assertThat(nextTaskCompletionTime).isNull()
  }

  @Test
  fun testDispatcher_getTaskCompletion_timeNow_futureAndImmediateTasks_returnsNextTaskTime() {
    val taskDelayMs = 100L
    val currentTimeMillis = fakeSystemClock.getTimeMillis()
    scheduleFutureTask(taskDelayMs, mockRunnable1)
    ensureFutureTasksAreScheduled()
    scheduleImmediateTask(mockRunnable2)

    val nextTaskCompletionTime =
      backgroundTestDispatcher.getNextFutureTaskCompletionTimeMillis(currentTimeMillis)

    // The next future task to run relative to now is the scheduled task.
    assertThat(nextTaskCompletionTime).isNotNull()
    assertThat(nextTaskCompletionTime).isEqualTo(currentTimeMillis + taskDelayMs)
  }

  @Test
  fun testDispatcher_getTaskCompletion_twoFutureTasks_timeBeforeFirst_returnsFirstTaskTime() {
    val taskDelayMs1 = 100L
    val taskDelayMs2 = 100L
    val currentTimeMillis = fakeSystemClock.getTimeMillis()
    scheduleFutureTask(taskDelayMs1, mockRunnable1)
    scheduleFutureTask(taskDelayMs2, mockRunnable2)
    ensureFutureTasksAreScheduled()

    val nextTaskCompletionTime =
      backgroundTestDispatcher.getNextFutureTaskCompletionTimeMillis(currentTimeMillis)

    // The next task to run is the first.
    assertThat(nextTaskCompletionTime).isNotNull()
    assertThat(nextTaskCompletionTime).isEqualTo(currentTimeMillis + taskDelayMs1)
  }

  @Test
  fun testDispatcher_getTaskCompletion_twoFutureTasks_timeBetween_returnsSecondTaskTime() {
    val taskDelayMs1 = 100L
    val taskDelayMs2 = taskDelayMs1 + 100L
    val currentTimeMillis = fakeSystemClock.getTimeMillis()
    val requestedTimeMills = currentTimeMillis + (taskDelayMs1 + taskDelayMs2) / 2
    scheduleFutureTask(taskDelayMs1, mockRunnable1)
    scheduleFutureTask(taskDelayMs2, mockRunnable2)
    ensureFutureTasksAreScheduled()

    val nextTaskCompletionTime =
      backgroundTestDispatcher.getNextFutureTaskCompletionTimeMillis(requestedTimeMills)

    // The second task to run is the next to run relative to the requested time.
    assertThat(nextTaskCompletionTime).isNotNull()
    assertThat(nextTaskCompletionTime).isEqualTo(currentTimeMillis + taskDelayMs2)
  }

  @Test
  fun testDispatcher_getTaskCompletion_twoFutureTasks_timeFarFuture_returnsNull() {
    val taskDelayMs1 = 100L
    val taskDelayMs2 = 100L
    val currentTimeMillis = fakeSystemClock.getTimeMillis()
    scheduleFutureTask(taskDelayMs1, mockRunnable1)
    scheduleFutureTask(taskDelayMs2, mockRunnable2)
    ensureFutureTasksAreScheduled()

    val nextTaskCompletionTime =
      backgroundTestDispatcher.getNextFutureTaskCompletionTimeMillis(currentTimeMillis + 10_000L)

    // There are no tasks left after the requested time.
    assertThat(nextTaskCompletionTime).isNull()
  }

  @Test
  fun testDispatcher_getTaskCompletion_timeNow_futureImmediateTasks_runCurrent_returnsNextTime() {
    val taskDelayMs = 100L
    val currentTimeMillis = fakeSystemClock.getTimeMillis()
    scheduleFutureTask(taskDelayMs, mockRunnable1)
    ensureFutureTasksAreScheduled()
    scheduleImmediateTask(mockRunnable2)

    backgroundTestDispatcher.runCurrent()
    val nextTaskCompletionTime =
      backgroundTestDispatcher.getNextFutureTaskCompletionTimeMillis(currentTimeMillis)

    // Since the immediate task was run, the next scheduled one is returned.
    assertThat(nextTaskCompletionTime).isNotNull()
    assertThat(nextTaskCompletionTime).isEqualTo(currentTimeMillis + taskDelayMs)
  }

  @Test
  fun testDispatcher_getTaskCompletion_timeNow_futureImmediateTasks_advanceAndRun_returnsNull() {
    val taskDelayMs = 100L
    val currentTimeMillis = fakeSystemClock.getTimeMillis()
    scheduleFutureTask(taskDelayMs, mockRunnable1)
    ensureFutureTasksAreScheduled()
    scheduleImmediateTask(mockRunnable2)

    fakeSystemClock.advanceTime(taskDelayMs)
    backgroundTestDispatcher.runCurrent()
    val nextTaskCompletionTime =
      backgroundTestDispatcher.getNextFutureTaskCompletionTimeMillis(currentTimeMillis)

    // Even though the previous time millis is being used to query, there are no tasks left to
    // return since both were executed.
    assertThat(nextTaskCompletionTime).isNull()
  }

  @Test
  fun testDispatcher_hasPendingCompletableTasks_noTasks_returnsFalse() {
    val hasCompletableTasks = backgroundTestDispatcher.hasPendingCompletableTasks()

    // There should be no completable tasks initially.
    assertThat(hasCompletableTasks).isFalse()
  }

  @Test
  fun testDispatcher_hasPendingCompletableTasks_oneImmediateTask_returnsTrue() {
    scheduleImmediateTask(mockRunnable1)

    val hasCompletableTasks = backgroundTestDispatcher.hasPendingCompletableTasks()

    // An immediate task is completable now.
    assertThat(hasCompletableTasks).isTrue()
  }

  @Test
  fun testDispatcher_hasPendingCompletableTasks_oneFutureTask_returnsFalse() {
    scheduleFutureTask(delayMs = 100L, mockRunnable1)
    ensureFutureTasksAreScheduled()

    val hasCompletableTasks = backgroundTestDispatcher.hasPendingCompletableTasks()

    // Future tasks aren't yet completable.
    assertThat(hasCompletableTasks).isFalse()
  }

  @Test
  fun testDispatcher_hasPendingCompletableTasks_advanceBeforeTask_returnsFalse() {
    val taskDelayMs = 100L
    scheduleFutureTask(taskDelayMs, mockRunnable1)
    ensureFutureTasksAreScheduled()

    fakeSystemClock.advanceTime(taskDelayMs - 1)
    val hasCompletableTasks = backgroundTestDispatcher.hasPendingCompletableTasks()

    // The clock hasn't yet advanced far enough for the task to be completable.
    assertThat(hasCompletableTasks).isFalse()
  }

  @Test
  fun testDispatcher_hasPendingCompletableTasks_advanceToTask_returnsTrue() {
    val taskDelayMs = 100L
    scheduleFutureTask(taskDelayMs, mockRunnable1)
    ensureFutureTasksAreScheduled()

    fakeSystemClock.advanceTime(taskDelayMs)
    val hasCompletableTasks = backgroundTestDispatcher.hasPendingCompletableTasks()

    // The scheduled task is now completable.
    assertThat(hasCompletableTasks).isTrue()
  }

  @Test
  fun testDispatcher_hasPendingCompletableTasks_advancePastTask_returnsTrue() {
    val taskDelayMs = 100L
    scheduleFutureTask(taskDelayMs, mockRunnable1)
    ensureFutureTasksAreScheduled()

    fakeSystemClock.advanceTime(taskDelayMs + 1)
    val hasCompletableTasks = backgroundTestDispatcher.hasPendingCompletableTasks()

    // The scheduled task has been completable.
    assertThat(hasCompletableTasks).isTrue()
  }

  @Test
  fun testDispatcher_hasPendingCompletableTasks_immediateAndFutureTask_returnsTrue() {
    val taskDelayMs = 100L
    scheduleFutureTask(taskDelayMs, mockRunnable1)
    ensureFutureTasksAreScheduled()
    scheduleImmediateTask(mockRunnable2)

    val hasCompletableTasks = backgroundTestDispatcher.hasPendingCompletableTasks()

    // The immediate task is completable now.
    assertThat(hasCompletableTasks).isTrue()
  }

  @Test
  fun testDispatcher_hasPendingCompletableTasks_immediateAndFutureTask_runCurrent_returnsFalse() {
    val taskDelayMs = 100L
    scheduleFutureTask(taskDelayMs, mockRunnable1)
    ensureFutureTasksAreScheduled()
    scheduleImmediateTask(mockRunnable2)

    backgroundTestDispatcher.runCurrent()
    val hasCompletableTasks = backgroundTestDispatcher.hasPendingCompletableTasks()

    // There is no longer a completable task (the immediate task was run, and it's not yet time to
    // run the scheduled one).
    assertThat(hasCompletableTasks).isFalse()
  }

  @Test
  fun testDispatcher_hasCompletableTasks_immediateFutureTask_runCurrentThenAdvance_returnsTrue() {
    val taskDelayMs = 100L
    scheduleFutureTask(taskDelayMs, mockRunnable1)
    ensureFutureTasksAreScheduled()
    scheduleImmediateTask(mockRunnable2)

    backgroundTestDispatcher.runCurrent()
    fakeSystemClock.advanceTime(taskDelayMs)
    val hasCompletableTasks = backgroundTestDispatcher.hasPendingCompletableTasks()

    // Advancing time after completing the immediate task should yield completable tasks.
    assertThat(hasCompletableTasks).isTrue()
  }

  @Test
  fun testDispatcher_hasCompletableTasks_immediateFutureTask_runBoth_returnsFalse() {
    val taskDelayMs = 100L
    scheduleFutureTask(taskDelayMs, mockRunnable1)
    ensureFutureTasksAreScheduled()
    scheduleImmediateTask(mockRunnable2)

    fakeSystemClock.advanceTime(taskDelayMs)
    backgroundTestDispatcher.runCurrent()
    val hasCompletableTasks = backgroundTestDispatcher.hasPendingCompletableTasks()

    // Running all tasks results in none left being completable.
    assertThat(hasCompletableTasks).isFalse()
  }

  @Test
  fun testDispatcher_hasCompletableTasks_twoFutureTasks_runOne_returnsFalse() {
    val taskDelayMs1 = 100L
    val taskDelayMs2 = taskDelayMs1 + 100L
    scheduleFutureTask(taskDelayMs1, mockRunnable1)
    scheduleFutureTask(taskDelayMs2, mockRunnable2)
    ensureFutureTasksAreScheduled()

    fakeSystemClock.advanceTime(taskDelayMs1)
    backgroundTestDispatcher.runCurrent()
    val hasCompletableTasks = backgroundTestDispatcher.hasPendingCompletableTasks()

    // While there's a second task, it's not completable yet since time hasn't been advanced.
    assertThat(hasCompletableTasks).isFalse()
  }

  @Test
  @Suppress("UnnecessaryVariable") // Extra variables are for readability.
  fun testDispatcher_hasCompletableTasks_twoFutureTasks_runOne_thenAdvance_returnsTrue() {
    val taskStepMs = 100L
    val taskDelayMs1 = taskStepMs
    val taskDelayMs2 = taskDelayMs1 + taskStepMs
    scheduleFutureTask(taskDelayMs1, mockRunnable1)
    scheduleFutureTask(taskDelayMs2, mockRunnable2)
    ensureFutureTasksAreScheduled()

    fakeSystemClock.advanceTime(taskStepMs)
    backgroundTestDispatcher.runCurrent()
    fakeSystemClock.advanceTime(taskStepMs)
    val hasCompletableTasks = backgroundTestDispatcher.hasPendingCompletableTasks()

    // Finishing the first task and stepping to the second results in the second now being
    // completable.
    assertThat(hasCompletableTasks).isTrue()
  }

  @Test
  fun testDispatcher_hasCompletableTasks_twoFutureTasks_runBoth_returnsFalse() {
    val taskDelayMs1 = 100L
    val taskDelayMs2 = taskDelayMs1 + 100L
    scheduleFutureTask(taskDelayMs1, mockRunnable1)
    scheduleFutureTask(taskDelayMs2, mockRunnable2)
    ensureFutureTasksAreScheduled()

    fakeSystemClock.advanceTime(taskDelayMs2)
    backgroundTestDispatcher.runCurrent()
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
  fun testDispatcher_taskIdleListener_scheduleImmediateTask_noCallbacks() {
    backgroundTestDispatcher.setTaskIdleListener(mockTaskIdleListener)
    reset(mockTaskIdleListener)

    scheduleImmediateTask(mockRunnable1)

    verifyZeroInteractions(mockTaskIdleListener)
  }

  @Test
  fun testDispatcher_taskIdleListener_scheduleFutureTask_noCallbacks() {
    backgroundTestDispatcher.setTaskIdleListener(mockTaskIdleListener)
    reset(mockTaskIdleListener)

    scheduleFutureTask(delayMs = 100L, mockRunnable1)

    verifyZeroInteractions(mockTaskIdleListener)
  }

  @Test
  fun testDispatcher_taskIdleListener_scheduleImmediateTask_runCurrent_isRunningThenIdle() {
    backgroundTestDispatcher.setTaskIdleListener(mockTaskIdleListener)
    reset(mockTaskIdleListener)

    scheduleImmediateTask(mockRunnable1)
    backgroundTestDispatcher.runCurrent()

    // The listener should have entered a running state, then become idle immediately after the task
    // was completed.
    val inOrder = inOrder(mockTaskIdleListener)
    inOrder.verify(mockTaskIdleListener).onDispatcherRunning()
    inOrder.verify(mockTaskIdleListener).onDispatcherIdle()
    inOrder.verifyNoMoreInteractions()
  }

  @Test
  fun testDispatcher_taskIdleListener_scheduleImmediateTask_runCurrentTwice_callbacksCalledOnce() {
    backgroundTestDispatcher.setTaskIdleListener(mockTaskIdleListener)
    reset(mockTaskIdleListener)

    scheduleImmediateTask(mockRunnable1)
    // Intentionally call runCurrent twice.
    backgroundTestDispatcher.runCurrent()
    backgroundTestDispatcher.runCurrent()

    // The callbacks shouldn't be called again.
    val inOrder = inOrder(mockTaskIdleListener)
    inOrder.verify(mockTaskIdleListener).onDispatcherRunning()
    inOrder.verify(mockTaskIdleListener).onDispatcherIdle()
    inOrder.verifyNoMoreInteractions()
  }

  @Test
  fun testDispatcher_taskIdleListener_scheduleImmediateFutureTask_runCurrent_runningIdleCalled() {
    backgroundTestDispatcher.setTaskIdleListener(mockTaskIdleListener)
    scheduleFutureTask(delayMs = 100L, mockRunnable1)
    ensureFutureTasksAreScheduled()
    scheduleImmediateTask(mockRunnable2)
    reset(mockTaskIdleListener)

    backgroundTestDispatcher.runCurrent()

    // The callbacks should be called for the first task. Note that order can't be reliably checked
    // here due to multiple tasks being executed in the same time quantum
    verify(mockTaskIdleListener, atLeastOnce()).onDispatcherRunning()
    verify(mockTaskIdleListener, atLeastOnce()).onDispatcherIdle()
    verifyNoMoreInteractions(mockTaskIdleListener)
  }

  @Test
  fun testDispatcher_taskIdleListener_scheduleImmediateFutureTask_runBothTogether_calledOnce() {
    backgroundTestDispatcher.setTaskIdleListener(mockTaskIdleListener)
    val taskDelayMs = 100L
    scheduleFutureTask(taskDelayMs, mockRunnable1)
    ensureFutureTasksAreScheduled()
    scheduleImmediateTask(mockRunnable2)
    reset(mockTaskIdleListener)

    fakeSystemClock.advanceTime(taskDelayMs)
    backgroundTestDispatcher.runCurrent()

    // The callbacks should be called once for both tasks. Note that order can't be reliably checked
    // here due to multiple tasks being executed in the same time quantum.
    verify(mockTaskIdleListener, atLeastOnce()).onDispatcherRunning()
    verify(mockTaskIdleListener, atLeastOnce()).onDispatcherIdle()
    verifyNoMoreInteractions(mockTaskIdleListener)
  }

  @Test
  fun testDispatcher_taskIdleListener_scheduleImmediateFutureTask_runBothSeparately_calledTwice() {
    backgroundTestDispatcher.setTaskIdleListener(mockTaskIdleListener)
    val taskDelayMs = 100L
    scheduleFutureTask(taskDelayMs, mockRunnable1)
    ensureFutureTasksAreScheduled()
    scheduleImmediateTask(mockRunnable2)
    reset(mockTaskIdleListener)

    // Run the tasks in 2 phases.
    backgroundTestDispatcher.runCurrent()
    fakeSystemClock.advanceTime(taskDelayMs)
    backgroundTestDispatcher.runCurrent()

    // The callbacks should be called multiple times. The number of times is implementation
    // dependent.
    verify(mockTaskIdleListener, atLeast(2)).onDispatcherIdle()
    verify(mockTaskIdleListener, atLeast(2)).onDispatcherRunning()
  }

  // Verify initial state running

  private fun scheduleImmediateTask(runnable: Runnable) {
    backgroundScope.launch { runnable.run() }
  }

  private fun scheduleFutureTask(delayMs: Long, runnable: Runnable) {
    backgroundScope.launch {
      delay(delayMs)
      runnable.run()
    }
  }

  /**
   * Ensures that prior unstarted tasks scheduled via [scheduleFutureTask] are properly delayed for
   * the future. This will run any tasks scheduled via [scheduleImmediateTask], so those should be
   * scheduled after this function is called.
   */
  private fun ensureFutureTasksAreScheduled() {
    backgroundTestDispatcher.runCurrent()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  private fun verifyDispatcherImplementations() {
    // Sanity checks to ensure the correct implementation is being tested.
    assertThat(backgroundTestDispatcher).isInstanceOf(TestCoroutineDispatcherRobolectricImpl::class)
  }

  private inline fun <S: Subject<S, T>, T, reified E: Any> Subject<S, T>.isInstanceOf(
    type: KClass<E>
  ) = isInstanceOf(type.java)

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    // TODO(#59): Either isolate these to their own shared test module, or use the real logging
    // module in tests to avoid needing to specify these settings for tests.
    @EnableConsoleLog
    @Provides
    fun provideEnableConsoleLog(): Boolean = true

    @EnableFileLog
    @Provides
    fun provideEnableFileLog(): Boolean = false

    @GlobalLogLevel
    @Provides
    fun provideGlobalLogLevel(): LogLevel = LogLevel.VERBOSE

    @Provides
    @IsOnRobolectric
    fun provideIsOnRobolectric(): Boolean = true
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class,
      TestModule::class,
      TestLogReportingModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(test: TestCoroutineDispatcherRobolectricImplTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerTestCoroutineDispatcherRobolectricImplTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: TestCoroutineDispatcherRobolectricImplTest) {
      component.inject(test)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
