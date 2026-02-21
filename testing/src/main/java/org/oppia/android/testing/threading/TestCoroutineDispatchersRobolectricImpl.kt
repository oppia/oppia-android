package org.oppia.android.testing.threading

import android.os.Build
import android.os.SystemClock
import androidx.annotation.RequiresApi
import java.time.Duration
import org.oppia.android.testing.time.FakeSystemClock
import java.util.TreeSet
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

/**
 * Robolectric-specific implementation of [TestCoroutineDispatchers].
 *
 * Unlike its Espresso counterpart, this implementation does not provide an idling resource.
 * Instead, tests should leverage functions like [runCurrent] and [advanceTimeBy] to run tasks in a
 * coordinated, deterministic, and thread-safe way.
 */
class TestCoroutineDispatchersRobolectricImpl @Inject constructor(
  @BackgroundTestDispatcher private val backgroundTestDispatcher: TestCoroutineDispatcher,
  @BlockingTestDispatcher private val blockingTestDispatcher: TestCoroutineDispatcher,
  private val fakeSystemClock: FakeSystemClock
) : TestCoroutineDispatchers {
  private val uiTaskCoordinator = RobolectricUiTaskCoordinator()

  override fun registerIdlingResource() {
    // Do nothing; idling resources aren't used in Robolectric.
  }

  override fun unregisterIdlingResource() {
    // Do nothing; idling resources aren't used in Robolectric.
  }

  override fun runCurrent() {
    // TODO: Re-enable this check to see how widespread the issue is.
//    check(Looper.getMainLooper().isCurrentThread) {
//      "Attempting to call runCurrent() off the main thread--this has a high chance to deadlock."
//    }
    val timeoutMillis = TimeUnit.SECONDS.toMillis(10) // TODO: Add to API?
    runBlocking {
      try {
        withTimeout(timeoutMillis) {
          runCurrentInternal(fakeSystemClock.getTimeMillis())
        }
      } catch (e: TimeoutCancellationException) {
        throw IllegalStateException(
          "Dispatcher failed to finish flush queue in ${timeoutMillis}ms", e
        )
      }
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  override fun advanceTimeBy(delayTimeMillis: Long) {
    var remainingDelayMillis = delayTimeMillis
    while (remainingDelayMillis > 0) {
      val currentTimeMillis = fakeSystemClock.getTimeMillis()
      val taskDelayMillis =
        advanceThroughNextFutureTask(currentTimeMillis, maxDelayMs = remainingDelayMillis)
      if (taskDelayMillis == null) {
        // If there are no delayed tasks, advance by the full time requested.
        fakeSystemClock.advanceTime(remainingDelayMillis)
        runCurrentInternal(fakeSystemClock.getTimeMillis()) // Time has moved.
      }
      remainingDelayMillis -= taskDelayMillis ?: remainingDelayMillis
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  override fun advanceUntilIdle() {
    // First, run through all tasks that are currently pending and can be run immediately.
    var currentTimeMillis = fakeSystemClock.getTimeMillis()
    runCurrentInternal(currentTimeMillis)

    // Now, the dispatchers can't proceed until time moves forward. Execute the next most recent
    // task schedule, and everything subsequently scheduled until the dispatchers are in a waiting
    // state again. Repeat until all tasks have been executed (and thus the dispatchers enter an
    // idle state).
    while (hasPendingTasks(currentTimeMillis)) {
      checkNotNull(advanceThroughNextFutureTask(currentTimeMillis)) {
        "Expected to find task with delay for waiting dispatchers with non-empty task queues"
      }
      currentTimeMillis = fakeSystemClock.getTimeMillis()
      runCurrentInternal(currentTimeMillis)
    }
  }

  private fun runCurrentInternal(timeMillis: Long) {
    do {
      flushNextTasks(timeMillis)
    } while (hasPendingCompletableTasks(timeMillis))
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun advanceThroughNextFutureTask(
    currentTimeMillis: Long,
    maxDelayMs: Long = Long.MAX_VALUE
  ): Long? {
    val nextFutureTimeMillis = getNextFutureTaskTimeMillis(currentTimeMillis)
    val timeToTaskMillis = nextFutureTimeMillis?.let { it - currentTimeMillis }
    val timeToAdvanceBy = timeToTaskMillis?.takeIf { it < maxDelayMs }
    return timeToAdvanceBy?.let {
      fakeSystemClock.advanceTime(it)
      runCurrentInternal(fakeSystemClock.getTimeMillis()) // Time has moved.
      return@let it
    }
  }

  private fun flushNextTasks(timeMillis: Long) {
    if (backgroundTestDispatcher.hasPendingCompletableTasks()) {
      backgroundTestDispatcher.runCurrent()
    }
    if (blockingTestDispatcher.hasPendingCompletableTasks()) {
      blockingTestDispatcher.runCurrent()
    }
    if (uiTaskCoordinator.hasPendingCompletableTasks(timeMillis)) {
      uiTaskCoordinator.runCurrent(timeMillis)
    }
  }

  /** Returns whether any of the dispatchers have any tasks to run, including in the future. */
  @RequiresApi(Build.VERSION_CODES.O)
  private fun hasPendingTasks(timeMillis: Long): Boolean {
    return backgroundTestDispatcher.hasPendingTasks() ||
      blockingTestDispatcher.hasPendingTasks() ||
      uiTaskCoordinator.hasPendingTasks(timeMillis)
  }

  /** Returns whether any of the dispatchers have tasks that can be run now. */
  @RequiresApi(Build.VERSION_CODES.O)
  private fun hasPendingCompletableTasks(timeMillis: Long): Boolean {
    return backgroundTestDispatcher.hasPendingCompletableTasks() ||
      blockingTestDispatcher.hasPendingCompletableTasks() ||
      uiTaskCoordinator.hasPendingCompletableTasks(timeMillis)
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun getNextFutureTaskTimeMillis(timeMillis: Long): Long? {
    val nextBackgroundFutureTaskTimeMills =
      backgroundTestDispatcher.getNextFutureTaskCompletionTimeMillis(timeMillis)
    val nextBlockingFutureTaskTimeMills =
      backgroundTestDispatcher.getNextFutureTaskCompletionTimeMillis(timeMillis)
    val nextUiFutureTaskTimeMills =
      uiTaskCoordinator.getNextFutureTaskCompletionTimeMillis(timeMillis)
    val futureTimes: TreeSet<Long> = sortedSetOf()
    nextBackgroundFutureTaskTimeMills?.let { futureTimes.add(it) }
    nextBlockingFutureTaskTimeMills?.let { futureTimes.add(it) }
    nextUiFutureTaskTimeMills?.let { futureTimes.add(it) }
    return futureTimes.firstOrNull()
  }

  private class RobolectricUiTaskCoordinator {
    private val shadowLooperClass by lazy { Class.forName("org.robolectric.shadows.ShadowLooper") }
    private val shadowMainLooper by lazy { shadowLooperClass.getDeclaredMethod("shadowMainLooper") }
    private val runOneTaskMethod by lazy { shadowLooperClass.getDeclaredMethod("runOneTask") }
    private val nextScheduledTimeMethod by lazy {
      shadowLooperClass.getDeclaredMethod("getNextScheduledTaskTime")
    }
    private val shadowUiLooper by lazy { shadowMainLooper.invoke(null) }

    @RequiresApi(Build.VERSION_CODES.O)
    fun hasPendingTasks(timeMillis: Long): Boolean {
      // Return if there are any tasks at all to run including in the future.
      return getNextFutureTaskCompletionTimeMillis(timeMillis) != null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun hasPendingCompletableTasks(timeMillis: Long): Boolean {
      // Simulate hasPendingCompletableTasks() by returning if there are any tasks that can be run
      // at all right now. That's determined by ensuring there's at least one next task to run and
      // that it isn't scheduled for the future (i.e. it can run now).
      return getNextFutureTaskCompletionTimeMillis(timeMillis)?.let { it <= timeMillis } ?: false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun runCurrent(timeMillis: Long) {
      // Simulate runCurrent() for a managed dispatcher by running one task at a time until there
      // are no others that can be run without advancing the clock. ShadowLooper.idle() basically
      // does this but the approach here has better symmetry with the managed dispatchers in that
      // it's consuming the task queue exactly for the current time duration.
      while (hasPendingCompletableTasks(timeMillis)) {
        runOneTaskMethod.invoke(shadowUiLooper)
      }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getNextFutureTaskCompletionTimeMillis(timeMillis: Long): Long? {
      val currentUptimeMs = SystemClock.uptimeMillis()
      val nextScheduledTime = nextScheduledTimeMethod.invoke(shadowUiLooper) as Duration
      val whenMs = nextScheduledTime.toMillis()
      if (whenMs == 0L) {
        // A timestamp of 0 indicates that there are no tasks left to run in the queue.
        return null
      }
      // Ensure the returned timestamp is relative to Oppia's clock tracking rather than
      // Robolectric's (in case they get out of sync).
      return timeMillis + (whenMs - currentUptimeMs).coerceAtLeast(0)
    }

  }
}
