package org.oppia.android.testing.threading

import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import org.oppia.android.testing.time.FakeSystemClock
import org.robolectric.shadows.ShadowLooper
import java.time.Duration
import java.util.TreeSet
import javax.inject.Inject

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
    check(Looper.getMainLooper().isCurrentThread) {
      "Attempting to call runCurrent() off the main thread--this has a high chance to deadlock."
    }
    do {
      flushNextTasks()
    } while (hasPendingCompletableTasks())
  }

  @RequiresApi(Build.VERSION_CODES.O)
  override fun advanceTimeBy(delayTimeMillis: Long) {
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

  @RequiresApi(Build.VERSION_CODES.O)
  override fun advanceUntilIdle() {
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

  @RequiresApi(Build.VERSION_CODES.O)
  private fun advanceToNextFutureTask(
    currentTimeMillis: Long,
    maxDelayMs: Long = Long.MAX_VALUE
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

  private fun flushNextTasks() {
    if (backgroundTestDispatcher.hasPendingCompletableTasks()) {
      backgroundTestDispatcher.runCurrent()
    }
    if (blockingTestDispatcher.hasPendingCompletableTasks()) {
      blockingTestDispatcher.runCurrent()
    }
    if (!uiTaskCoordinator.isIdle()) {
      uiTaskCoordinator.idle()
    }
  }

  /** Returns whether any of the dispatchers have any tasks to run, including in the future. */
  @RequiresApi(Build.VERSION_CODES.O)
  private fun hasPendingTasks(): Boolean {
    return backgroundTestDispatcher.hasPendingTasks() ||
      blockingTestDispatcher.hasPendingTasks() ||
      getNextUiThreadFutureTaskTimeMillis(fakeSystemClock.getTimeMillis()) != null
  }

  /** Returns whether any of the dispatchers have tasks that can be run now. */
  private fun hasPendingCompletableTasks(): Boolean {
    return backgroundTestDispatcher.hasPendingCompletableTasks() ||
      blockingTestDispatcher.hasPendingCompletableTasks() ||
      !uiTaskCoordinator.isIdle()
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun getNextFutureTaskTimeMillis(timeMillis: Long): Long? {
    val nextBackgroundFutureTaskTimeMills =
      backgroundTestDispatcher.getNextFutureTaskCompletionTimeMillis(timeMillis)
    val nextBlockingFutureTaskTimeMills =
      blockingTestDispatcher.getNextFutureTaskCompletionTimeMillis(timeMillis)
    val nextUiFutureTaskTimeMills = getNextUiThreadFutureTaskTimeMillis(timeMillis)
    val futureTimes: TreeSet<Long> = sortedSetOf()
    nextBackgroundFutureTaskTimeMills?.let { futureTimes.add(it) }
    nextBlockingFutureTaskTimeMills?.let { futureTimes.add(it) }
    nextUiFutureTaskTimeMills?.let { futureTimes.add(it) }
    return futureTimes.firstOrNull()
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun getNextUiThreadFutureTaskTimeMillis(timeMillis: Long): Long? {
    return uiTaskCoordinator.getNextUiThreadFutureTaskTimeMillis(timeMillis)
  }

  private class RobolectricUiTaskCoordinator {
    fun isIdle(): Boolean {
      return ShadowLooper.shadowMainLooper().isIdle
    }

    fun idle() {
      ShadowLooper.shadowMainLooper().idle()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getNextUiThreadFutureTaskTimeMillis(timeMillis: Long): Long? {
      val nextScheduledTime = ShadowLooper.shadowMainLooper().nextScheduledTaskTime
      val delayMs = nextScheduledTime.toMillis()
      if (delayMs == 0L && isIdle()) {
        // If there's no delay and the looper is idle, that means there are no scheduled tasks.
        return null
      }
      return timeMillis + delayMs
    }
  }
}