package org.oppia.android.testing.threading

import dagger.Reusable
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.oppia.android.testing.time.FakeSystemClock
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.inject.Inject

// TODO(#1274): Add tests for this class.
/**
 * [MonitoredTaskCoordinator] that helps to coordinate execution with Robolectric's UI thread
 * scheduler.
 *
 * This coordinator must be considered with other [MonitoredTaskCoordinator]s in order to ensure
 * background/UI cross-thread communication synchronization.
 *
 * A reusable instance of this class is available for application-level injection, though a
 * different instance may be created across injections (at the discretion of Dagger).
 *
 * This class is never meant to be used outside of this package. Callers should instead use
 * [TestCoroutineDispatchers].
 */
@Reusable
class MonitoredRobolectricUiTaskCoordinator @Inject constructor(
  private val fakeSystemClock: FakeSystemClock
) : MonitoredTaskCoordinator {
  private val classLoader by lazy { MonitoredRobolectricUiTaskCoordinator::class.java.classLoader }
  private val shadowLooperClass by lazy {
    classLoader.loadClass("org.robolectric.shadows.ShadowLooper")
  }
  private val shadowUiLooper by lazy {
    shadowLooperClass.getDeclaredMethod("shadowMainLooper").invoke(/* obj= */ null)
  }
  private val isIdleMethod by lazy { shadowLooperClass.getDeclaredMethod("isIdle") }
  private val idleMethod by lazy { shadowLooperClass.getDeclaredMethod("idle") }
  private val nextScheduledTimeMethod by lazy {
    shadowLooperClass.getDeclaredMethod("getNextScheduledTaskTime")
  }
  private val isIdle: Boolean
    get() = isIdleMethod.invoke(shadowUiLooper) as Boolean

  override fun hasPendingTasks(): Boolean =
    getNextFutureTaskCompletionTimeMillis(fakeSystemClock.getTimeMillis()) != null

  override fun getNextFutureTaskCompletionTimeMillis(timeMillis: Long): Long? {
    val nextScheduledTime = nextScheduledTimeMethod.invoke(shadowUiLooper) as Duration
    val delayMs = nextScheduledTime.toMillis()
    if (delayMs == 0L && isIdle) {
      // If there's no delay and the looper is idle, that means there are no scheduled tasks.
      return null
    }
    return timeMillis + delayMs
  }

  override fun hasPendingCompletableTasks(): Boolean = !isIdle

  override fun setTaskIdleListener(taskIdleListener: MonitoredTaskCoordinator.TaskIdleListener) {
    error("There's no reliable way to monitor the Robolectric UI looper, so this is unsupported.")
  }

  override fun runCurrent(timeout: Long, timeoutUnit: TimeUnit) {
    runBlocking {
      withTimeout(timeoutUnit.toMillis(timeout)) {
        if (!isIdle) {
          idleMethod.invoke(shadowUiLooper)
        }
      }
    }
  }
}
