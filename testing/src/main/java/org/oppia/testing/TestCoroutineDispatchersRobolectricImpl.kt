package org.oppia.testing

import java.lang.reflect.Method
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
    runCurrentWith(/* no extra dispatchers */)
  }

  override fun runCurrentWith(vararg additionalDispatchers: TestCoroutineDispatcher) {
    do {
      flushNextTasks(additionalDispatchers)
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
    advanceUntilIdleWith(/* no extra dispatchers */)
  }

  override fun advanceUntilIdleWith(vararg additionalDispatchers: TestCoroutineDispatcher) {
    // First, run through all tasks that are currently pending and can be run immediately.
    runCurrentWith(*additionalDispatchers)

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
      runCurrentWith(*additionalDispatchers)
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

  private fun flushNextTasks(additionalDispatchers: Array<out TestCoroutineDispatcher>) {
    if (backgroundTestDispatcher.hasPendingCompletableTasks()) {
      backgroundTestDispatcher.runCurrent()
    }
    if (blockingTestDispatcher.hasPendingCompletableTasks()) {
      blockingTestDispatcher.runCurrent()
    }
    if (!uiTaskCoordinator.isIdle()) {
      uiTaskCoordinator.idle()
    }

    // Flush the additional dispatchers last. While there are some obscure inconsistencies that can
    // occur due to the order of task flushing, it's unlikely to introduce real issues since this
    // function is run in a loop.
    additionalDispatchers.forEach {
      if (it.hasPendingCompletableTasks()) {
        it.runCurrent()
      }
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
      !uiTaskCoordinator.isIdle()
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
    return uiTaskCoordinator.getNextUiThreadFutureTaskTimeMillis(timeMillis)
  }

  private class RobolectricUiTaskCoordinator {
    private val shadowLooperClass by lazy { loadShadowLooperClass() }
    private val shadowUiLooper by lazy { loadMainShadowLooper() }
    private val isIdleMethod by lazy { loadIsIdleMethod() }
    private val idleMethod by lazy { loadIdleMethod() }
    private val nextScheduledTimeMethod by lazy { loadGetNextScheduledTaskTimeMethod() }

    internal fun isIdle(): Boolean {
      return isIdleMethod.invoke(shadowUiLooper) as Boolean
    }

    internal fun idle() {
      idleMethod.invoke(shadowUiLooper)
    }

    internal fun getNextUiThreadFutureTaskTimeMillis(timeMillis: Long): Long? {
      val nextScheduledTime = nextScheduledTimeMethod.invoke(shadowUiLooper) as Duration
      val delayMs = nextScheduledTime.toMillis()
      if (delayMs == 0L && isIdle()) {
        // If there's no delay and the looper is idle, that means there are no scheduled tasks.
        return null
      }
      return timeMillis + delayMs
    }

    private fun loadShadowLooperClass(): Class<*> {
      val classLoader = TestCoroutineDispatchers::class.java.classLoader!!
      return classLoader.loadClass("org.robolectric.shadows.ShadowLooper")
    }

    private fun loadMainShadowLooper(): Any {
      val shadowMainLooperMethod = shadowLooperClass.getDeclaredMethod("shadowMainLooper")
      return shadowMainLooperMethod.invoke(/* obj= */ null)
    }

    private fun loadIsIdleMethod(): Method {
      return shadowLooperClass.getDeclaredMethod("isIdle")
    }

    private fun loadIdleMethod(): Method {
      return shadowLooperClass.getDeclaredMethod("idle")
    }

    private fun loadGetNextScheduledTaskTimeMethod(): Method {
      return shadowLooperClass.getDeclaredMethod("getNextScheduledTaskTime")
    }
  }
}
