package org.oppia.android.testing

import android.os.Build
import kotlinx.coroutines.CoroutineDispatcher
import java.util.concurrent.TimeUnit

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
abstract class TestCoroutineDispatcher : CoroutineDispatcher() {
  /** The default time value (in seconds) used for methods with timeouts. */
  @Suppress("PropertyName")
  val DEFAULT_TIMEOUT_SECONDS
    get() = computeTimeout()

  /** The default time unit used for methods that execute with timeouts. */
  @Suppress("PropertyName")
  val DEFAULT_TIMEOUT_UNIT = TimeUnit.SECONDS

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
   *
   * @param timeout the timeout value in the specified unit after which this run attempt should fail
   * @param timeoutUnit the unit for [timeout] corresponding to how long this method should wait
   *     when trying to execute tasks before giving up
   */
  abstract fun runCurrent(
    timeout: Long = DEFAULT_TIMEOUT_SECONDS,
    timeoutUnit: TimeUnit = DEFAULT_TIMEOUT_UNIT
  )

  /**
   * Runs all tasks currently scheduled, including future sheduled tasks. Normal use cases should
   * use [TestCoroutineDispatchers], not this method. This is reserved for special cases (like
   * isolated test dispatchers).
   */
  abstract fun runUntilIdle(
    timeout: Long = DEFAULT_TIMEOUT_SECONDS,
    timeoutUnit: TimeUnit = DEFAULT_TIMEOUT_UNIT
  )

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

  private companion object {
    private const val STANDARD_TIMEOUT_SECONDS = 10L
    private val TIMEOUT_WHEN_DEBUGGING_SECONDS = TimeUnit.HOURS.toSeconds(1)

    private fun computeTimeout(): Long {
      // When debugging tests, allow for significantly more time so that breakpoint debugging
      // doesn't trigger timeouts during investigation.
      return if (isDebuggerAttached()) {
        TIMEOUT_WHEN_DEBUGGING_SECONDS
      } else {
        STANDARD_TIMEOUT_SECONDS
      }
    }

    private fun isDebuggerAttached(): Boolean {
      return if (Build.FINGERPRINT.contains("robolectric", ignoreCase = true)) {
        isIntelliJDebuggerAttachedWithRobolectric()
      } else {
        isDebuggerAttachedWithEspresso()
      }
    }

    /**
     * Returns whether there's an IntelliJ debugger attached. This only needed for Robolectric tests
     * since [android.os.Debug.isDebuggerConnected] doesn't work in Robolectric. This approach only
     * works for Android Studio, unfortunately.
     */
    private fun isIntelliJDebuggerAttachedWithRobolectric(): Boolean {
      return System.getProperty("intellij.debug.agent")?.toBoolean() ?: false
    }

    private fun isDebuggerAttachedWithEspresso(): Boolean {
      return android.os.Debug.isDebuggerConnected()
    }
  }
}
