package org.oppia.testing

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Replacement for Kotlin's test coroutine dispatcher that can be used to replace coroutine
 * dispatching functionality in Robolectric & Espresso tests in a way that can be coordinated across
 * multiple dispatchers for execution synchronization.
 *
 * Developers should never use this dispatcher directly. Integrating with it should be done via
 * [TestDispatcherModule] and ensuring thread synchronization should be done via
 * [TestCoroutineDispatchers]. Attempting to interact directly with this dispatcher may cause timing
 * inconsistencies between the UI thread and other application coroutine dispatchers.
 *
 * Further, no assumptions should be made about the coordination functionality of this utility on
 * Robolectric or Espresso. The implementation carefully manages the differences between these two
 * platforms, so tests should only rely on the API. See [TestCoroutineDispatchers] for more details
 * on how to properly integrate with the test coroutine dispatcher API.
 */
abstract class TestCoroutineDispatcher: CoroutineDispatcher() {
  /**
   * Returns whether there are any tasks known to the dispatcher that have not yet been started.
   *
   * Note that some of these tasks may be scheduled for the future. This is meant to be used in
   * conjunction with [FakeSystemClock.advanceTime] since that along with [runCurrent] will execute
   * all tasks up to the new time. If the time returned by [getNextFutureTaskCompletionTimeMillis]
   * plus the current time is passed to [FakeSystemClock.advanceTime], this dispatcher guarantees
   * that [hasPendingTasks] will return false after a call to [runCurrent] returns.
   *
   * This function makes no guarantees about idleness with respect to other dispatchers (e.g. even
   * if all tasks are executed, another dispatcher could schedule another task on this dispatcher in
   * response to a task from this dispatcher being executed). Cross-thread communication should be
   * managed using [TestCoroutineDispatchers], instead.
   */
  abstract fun hasPendingTasks(): Boolean

  /**
   * Returns the clock time at which the next future task will execute ('future' indicates that the
   * task cannot execute right now due to its execution time being in the future).
   */
  abstract fun getNextFutureTaskCompletionTimeMillis(timeMillis: Long): Long?

  /**
   * Returns whether there are any tasks that are immediately executable and pending.
   *
   * If [runCurrent] is used, this function is guaranteed to return false after that function
   * returns. Note that the same threading caveats mentioned for [hasPendingTasks] also pertains to
   * this function.
   */
  abstract fun hasPendingCompletableTasks(): Boolean

  /** Sets a [TaskIdleListener] to observe when the dispatcher becomes idle/non-idle. */
  abstract fun setTaskIdleListener(taskIdleListener: TaskIdleListener)

  /**
   * Runs all tasks currently scheduled to be run in the dispatcher, but none scheduled for the
   * future.
   */
  abstract fun runCurrent()

  /** A listener for whether the test coroutine dispatcher has become idle. */
  interface TaskIdleListener {
    /**
     * Called when the dispatcher has become non-idle. This may be called immediately after
     * registration, and may be called on different threads.
     */
    fun onDispatcherRunning()

    /**
     * Called when the dispatcher has become idle. This may be called immediately after
     * registration, and may be called on different threads.
     */
    fun onDispatcherIdle()
  }

  /** Injectable factory for creating the correct dispatcher for current test platform. */
  interface Factory {
    /**
     * Returns a new [TestCoroutineDispatcher] with the specified [CoroutineDispatcher] to back it
     * up for actual task execution.
     */
    fun createDispatcher(realDispatcher: CoroutineDispatcher): TestCoroutineDispatcher
  }
}
