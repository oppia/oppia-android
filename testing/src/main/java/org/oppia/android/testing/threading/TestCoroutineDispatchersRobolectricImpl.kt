package org.oppia.android.testing.threading

import org.oppia.android.testing.time.FakeSystemClock
import javax.inject.Inject

/**
 * Robolectric-specific implementation of [TestCoroutineDispatchers].
 *
 * Unlike its Espresso counterpart, this implementation does not provide an idling resource.
 * Instead, tests should leverage functions like [runCurrent] and [advanceTimeBy] to run tasks in a
 * coordinated, deterministic, and thread-safe way.
 */
class TestCoroutineDispatchersRobolectricImpl @Inject constructor(
  private val monitoredTaskCoordinators: Set<@JvmSuppressWildcards MonitoredTaskCoordinator>,
  private val fakeSystemClock: FakeSystemClock
) : TestCoroutineDispatchers {
  override fun registerIdlingResource() {
    // Do nothing; idling resources aren't used in Robolectric.
  }

  override fun unregisterIdlingResource() {
    // Do nothing; idling resources aren't used in Robolectric.
  }

  override fun runCurrent() {
    do {
      flushNextTasks()
    } while (hasPendingCompletableTasks())
  }

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
    // Run all executors that have completable tasks.
    monitoredTaskCoordinators.filter { it.hasPendingCompletableTasks() }.forEach { it.runCurrent() }
  }

  /** Returns whether any of the dispatchers have any tasks to run, including in the future. */
  private fun hasPendingTasks(): Boolean = monitoredTaskCoordinators.any { it.hasPendingTasks() }

  /** Returns whether any of the dispatchers have tasks that can be run now. */
  private fun hasPendingCompletableTasks(): Boolean =
    monitoredTaskCoordinators.any { it.hasPendingCompletableTasks() }

  private fun getNextFutureTaskTimeMillis(timeMillis: Long): Long? {
    // Find the soonest next task available to run in the future (after the specified time), or null
    // if there are none.
    return monitoredTaskCoordinators.mapNotNull {
      it.getNextFutureTaskCompletionTimeMillis(timeMillis)
    }.minOrNull()
  }
}
