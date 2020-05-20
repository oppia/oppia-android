package org.oppia.testing

import android.os.SystemClock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import org.robolectric.shadows.ShadowLooper
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Helper class to coordinate execution between all threads currently running in a test environment,
 * using both Robolectric for the main thread and [TestCoroutineDispatcher] for application-specific
 * threads.
 *
 * This class should be used at any point in a test where the test should ensure that a clean thread
 * synchronization point is needed (such as after an async operation is kicked off). This class can
 * guarantee that all threads enter a truly idle state (e.g. even in cases where execution "ping
 * pongs" across multiple threads will still be resolved with a single call to [advanceUntilIdle]).
 *
 * Note that it's recommended all Robolectric tests that utilize this class run in a PAUSED looper
 * mode so that clock coordination is consistent between Robolectric's scheduler and this utility
 * class, otherwise unexpected inconsistencies may arise.
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
   * Note that it's generally not recommended to use this method since it may result in
   * unanticipated dependencies on the order in which this class processes tasks for each handled
   * thread and coroutine dispatcher.
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
   * sufficiently forwarded.
   */
  @ExperimentalCoroutinesApi
  fun advanceTimeBy(delayTimeMillis: Long) {
    fakeSystemClock.advanceTime(delayTimeMillis)
    runCurrent()
  }

  /**
   * Runs all tasks on all tracked threads & coroutine dispatchers until no other tasks are pending.
   * However, tasks that require the clock to be advanced will likely not be run (depending on
   * whether the test under question is using a paused execution model, which is recommended for
   * Robolectric tests).
   */
  @ExperimentalCoroutinesApi
  fun advanceUntilIdle() {
    do {
      flushAllTasks()
    } while (hasPendingTasks())
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

  @ExperimentalCoroutinesApi
  private fun flushAllTasks() {
    val currentTimeMillis = fakeSystemClock.getTimeMillis()
    if (backgroundTestDispatcher.hasPendingCompletableTasks()) {
      backgroundTestDispatcher.advanceUntilIdle()
    }
    if (blockingTestDispatcher.hasPendingCompletableTasks()) {
      blockingTestDispatcher.advanceUntilIdle()
    }
    shadowUiLooper.idleFor(currentTimeMillis, TimeUnit.MILLISECONDS)
    SystemClock.setCurrentTimeMillis(currentTimeMillis)
  }

  private fun hasPendingTasks(): Boolean {
    // TODO(#89): Ensure the check for pending UI thread tasks is actually correct.
    return backgroundTestDispatcher.hasPendingTasks()
        || blockingTestDispatcher.hasPendingTasks()
        || !shadowUiLooper.isIdle
  }

  private fun hasPendingCompletableTasks(): Boolean {
    return backgroundTestDispatcher.hasPendingCompletableTasks()
        || blockingTestDispatcher.hasPendingCompletableTasks()
        || !shadowUiLooper.isIdle
  }
}
