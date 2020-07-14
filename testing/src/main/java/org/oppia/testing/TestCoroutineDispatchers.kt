package org.oppia.testing

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import org.robolectric.shadows.ShadowLooper
import java.util.TreeSet
import javax.inject.Inject

// TODO(#1274): Add thorough testing for this class.

/**
 * Helper class to coordinate execution between all threads currently running in a test environment,
 * using both Robolectric for the main thread and [TestCoroutineDispatcher] for application-specific
 * threads.
 *
 * This class should be used at any point in a test where the test should ensure that a clean thread
 * synchronization point is needed (such as after an async operation is kicked off). This class can
 * guarantee that all threads enter a truly idle state (e.g. even in cases where execution "ping
 * pongs" across multiple threads will still be resolved with a single call to [runCurrent],
 * [advanceTimeBy], or [advanceUntilIdle]).
 *
 * Note that it's recommended all Robolectric tests that utilize this class run in a PAUSED looper
 * mode so that clock coordination is consistent between Robolectric's scheduler and this utility
 * class, otherwise unexpected inconsistencies may arise.
 *
 * *NOTE TO DEVELOPERS*: This class is NOT yet ready for broad use until after #89 is resolved.
 * Please ask in oppia-android-dev if you have a use case that you think requires this class.
 * Specific cases will be allowed to integrate with if other options are infeasible. Other tests
 * should rely on existing mechanisms until this utility is ready for broad use.
 */
@InternalCoroutinesApi
class TestCoroutineDispatchers @Inject constructor(
  @BackgroundTestDispatcher private val backgroundTestDispatcher: TestCoroutineDispatcher,
  @BlockingTestDispatcher private val blockingTestDispatcher: TestCoroutineDispatcher,
  private val fakeSystemClock: FakeSystemClock
) {
  private val shadowUiLooper = ShadowLooper.shadowMainLooper()

  /**
   * Runs all current tasks pending, but does not follow up with executing any tasks that are
   * scheduled after this method finishes.
   *
   * It's recommended to always use this method when trying to bring a test to a reasonable idle
   * state since it doesn't change the clock time. If a test needs to advance time to complete some
   * operation, it should use [advanceTimeBy].
   */
  @ExperimentalCoroutinesApi
  fun runCurrent() {
    do {
      flushNextTasks()
    } while (hasPendingCompletableTasks())
  }

  /**
   * Advances the system clock by the specified time in milliseconds and then ensures any new tasks
   * that were scheduled are fully executed before proceeding. This does not guarantee the
   * dispatchers enter an idle state, but it should guarantee that any tasks previously not executed
   * due to it not yet being the time for them to be executed may now run if the clock was
   * sufficiently forwarded. That is, running [runCurrent] after this method returns will do
   * nothing.
   *
   * It's recommended to always use this method when a test needs to wait for a specific future task
   * to complete. If a test doesn't require time to change to reach an idle state, [runCurrent]
   * should be used, instead. [advanceUntilIdle] should be reserved for cases when the test needs to
   * wait for a future operation, but doesn't know how long.
   */
  @ExperimentalCoroutinesApi
  fun advanceTimeBy(delayTimeMillis: Long) {
    var remainingDelayMillis = delayTimeMillis
    while (remainingDelayMillis > 0) {
      val currentTimeMillis = fakeSystemClock.getTimeMillis()
      val taskDelayMillis =
        advanceToNextFutureTask(currentTimeMillis, maxDelayMs = remainingDelayMillis)
      if (taskDelayMillis == null) {
        // If there are no delayed tasks, advance by the full time requested.
        fakeSystemClock.advanceTime(remainingDelayMillis)
        runCurrent()
      }
      remainingDelayMillis -= taskDelayMillis ?: remainingDelayMillis
    }
  }

  /**
   * Runs all tasks on all tracked threads & coroutine dispatchers until no other tasks are pending.
   * However, tasks that require the clock to be advanced will likely not be run (depending on
   * whether the test under question is using a paused execution model, which is recommended for
   * Robolectric tests).
   *
   * It's only recommended to use this method in cases when a test needs to wait for a future task
   * to complete, but is unaware how long it needs to wait. [advanceTimeBy] and [runCurrent] are
   * preferred methods for synchronizing execution with tests since this method may have the
   * unintentional side effect of executing future tasks before the test anticipates it.
   */
  @ExperimentalCoroutinesApi
  fun advanceUntilIdle() {
    // First, run through all tasks that are currently pending and can be run immediately.
    runCurrent()

    // Now, the dispatchers can't proceed until time moves forward. Execute the next most recent
    // task schedule, and everything subsequently scheduled until the dispatchers are in a waiting
    // state again. Repeat until all tasks have been executed (and thus the dispatchers enter an
    // idle state).
    while (hasPendingTasks()) {
      val currentTimeMillis = fakeSystemClock.getTimeMillis()
      val taskDelayMillis = checkNotNull(advanceToNextFutureTask(currentTimeMillis)) {
        "Expected to find task with delay for waiting dispatchers with non-empty task queues"
      }
      fakeSystemClock.advanceTime(taskDelayMillis)
      runCurrent()
    }
  }

  /**
   * Advances the clock to the next most recently scheduled task, then runs all tasks until the
   * dispatcher enters a waiting state (meaning they cannot execute anything until the clock is
   * advanced). If a task was executed, returns the delay added to the current system time in order
   * to execute it. Returns null if the time to the next task is beyond the specified maximum delay,
   * if any.
   */
  @ExperimentalCoroutinesApi
  private fun advanceToNextFutureTask(
    currentTimeMillis: Long, maxDelayMs: Long = Long.MAX_VALUE
  ): Long? {
    val nextFutureTimeMillis = getNextFutureTaskTimeMillis(currentTimeMillis)
    val timeToTaskMillis = nextFutureTimeMillis?.let { it - currentTimeMillis }
    val timeToAdvanceBy = timeToTaskMillis?.takeIf { it < maxDelayMs }
    return timeToAdvanceBy?.let {
      fakeSystemClock.advanceTime(it)
      runCurrent()
      return@let it
    }
  }

  @ExperimentalCoroutinesApi
  private fun flushNextTasks() {
    if (backgroundTestDispatcher.hasPendingCompletableTasks()) {
      backgroundTestDispatcher.runCurrent()
    }
    if (blockingTestDispatcher.hasPendingCompletableTasks()) {
      blockingTestDispatcher.runCurrent()
    }
    if (!shadowUiLooper.isIdle) {
      shadowUiLooper.idle()
    }
  }

  /** Returns whether any of the dispatchers have any tasks to run, including in the future. */
  private fun hasPendingTasks(): Boolean {
    return backgroundTestDispatcher.hasPendingTasks() ||
        blockingTestDispatcher.hasPendingTasks() ||
        getNextUiThreadFutureTaskTimeMillis(fakeSystemClock.getTimeMillis()) != null
  }

  /** Returns whether any of the dispatchers have tasks that can be run now. */
  private fun hasPendingCompletableTasks(): Boolean {
    return backgroundTestDispatcher.hasPendingCompletableTasks() ||
        blockingTestDispatcher.hasPendingCompletableTasks() ||
        !shadowUiLooper.isIdle
  }

  private fun getNextFutureTaskTimeMillis(timeMillis: Long): Long? {
    val nextBackgroundFutureTaskTimeMills =
      backgroundTestDispatcher.getNextFutureTaskCompletionTimeMillis(timeMillis)
    val nextBlockingFutureTaskTimeMills =
      backgroundTestDispatcher.getNextFutureTaskCompletionTimeMillis(timeMillis)
    val nextUiFutureTaskTimeMills = getNextUiThreadFutureTaskTimeMillis(timeMillis)
    val futureTimes: TreeSet<Long> = sortedSetOf()
    nextBackgroundFutureTaskTimeMills?.let { futureTimes.add(it) }
    nextBlockingFutureTaskTimeMills?.let { futureTimes.add(it) }
    nextUiFutureTaskTimeMills?.let { futureTimes.add(it) }
    return futureTimes.firstOrNull()
  }

  private fun getNextUiThreadFutureTaskTimeMillis(timeMillis: Long): Long? {
    val delayMs = shadowUiLooper.nextScheduledTaskTime.toMillis()
    if (delayMs == 0L && shadowUiLooper.isIdle) {
      // If there's no delay and the looper is idle, that means there are no scheduled tasks.
      return null
    }
    return timeMillis + delayMs
  }
}
