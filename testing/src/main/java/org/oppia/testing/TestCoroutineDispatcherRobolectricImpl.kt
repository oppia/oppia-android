package org.oppia.testing

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.test.UncompletedCoroutinesError
import java.util.TreeSet
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import kotlin.Comparator
import kotlin.coroutines.CoroutineContext

// TODO(#89): Audit & adjust the thread safety of this class, and determine if there's a way to move
//  off of the internal coroutine API.

/**
 * Replacement for Kotlin's test coroutine dispatcher that can be used to replace coroutine
 * dispatching functionality in a Robolectric test in a way that can be coordinated across multiple
 * dispatchers for execution synchronization.
 *
 * Developers should never use this dispatcher directly. Integrating with it should be done via
 * [TestDispatcherModule] and ensuring thread synchronization should be done via
 * [TestCoroutineDispatchers]. Attempting to interact directly with this dispatcher may cause timing
 * inconsistencies between the UI thread and other application coroutine dispatchers.
 */
@InternalCoroutinesApi
@ExperimentalCoroutinesApi
class TestCoroutineDispatcherRobolectricImpl private constructor(
  private val fakeSystemClock: FakeSystemClock,
  private val realCoroutineDispatcher: CoroutineDispatcher
): TestCoroutineDispatcher() {

  /** Sorted set that first sorts on when a task should be executed, then insertion order. */
  private val taskQueue = CopyOnWriteArraySet<Task>()
  private val isRunning = AtomicBoolean(true)
  private val executingTaskCount = AtomicInteger(0)
  private val totalTaskCount = AtomicInteger(0)
  private var taskIdleListener: TaskIdleListener? = null

  @ExperimentalCoroutinesApi
  override val currentTime: Long
    get() = fakeSystemClock.getTimeMillis()

  override fun dispatch(context: CoroutineContext, block: Runnable) {
    enqueueTask(createDeferredRunnable(context, block))
  }

  override fun scheduleResumeAfterDelay(
    timeMillis: Long,
    continuation: CancellableContinuation<Unit>
  ) {
    enqueueTask(createContinuationRunnable(continuation), delayMillis = timeMillis)
  }

  @ExperimentalCoroutinesApi
  override fun advanceTimeBy(delayTimeMillis: Long): Long {
    flushTaskQueue(fakeSystemClock.advanceTime(delayTimeMillis))
    return delayTimeMillis
  }

  @ExperimentalCoroutinesApi
  override fun advanceUntilIdle(): Long {
    throw UnsupportedOperationException(
      "Use TestCoroutineDispatchers.advanceUntilIdle() to ensure the dispatchers are properly " +
        "coordinated"
    )
  }

  @ExperimentalCoroutinesApi
  override fun cleanupTestCoroutines() {
    flushTaskQueue(fakeSystemClock.getTimeMillis())
    val remainingTaskCount = taskQueue.size
    if (remainingTaskCount != 0) {
      throw UncompletedCoroutinesError(
        "Expected no remaining tasks for test dispatcher, but found $remainingTaskCount"
      )
    }
  }

  @ExperimentalCoroutinesApi
  override fun pauseDispatcher() {
    isRunning.set(false)
  }

  @ExperimentalCoroutinesApi
  override suspend fun pauseDispatcher(block: suspend () -> Unit) {
    // There's not a clear way to handle this block while maintaining the thread of the dispatcher,
    // so disable it for now until it's later needed.
    throw UnsupportedOperationException()
  }

  @ExperimentalCoroutinesApi
  override fun resumeDispatcher() {
    isRunning.set(true)
    flushTaskQueue(fakeSystemClock.getTimeMillis())
  }

  @ExperimentalCoroutinesApi
  override fun runCurrent() {
    flushTaskQueue(fakeSystemClock.getTimeMillis())
  }

  override fun hasPendingTasks(): Boolean = taskQueue.isNotEmpty()

  override fun getNextFutureTaskCompletionTimeMillis(timeMillis: Long): Long? {
    return createSortedTaskSet().firstOrNull { task -> task.timeMillis > timeMillis }?.timeMillis
  }

  override fun hasPendingCompletableTasks(): Boolean {
    return taskQueue.hasPendingCompletableTasks(fakeSystemClock.getTimeMillis())
  }

  override fun setTaskIdleListener(taskIdleListener: TaskIdleListener) {
    this.taskIdleListener = taskIdleListener
    if (executingTaskCount.get() > 0) {
      notifyIfRunning()
    } else {
      notifyIfIdle()
    }
  }

  private fun enqueueTask(block: Runnable, delayMillis: Long = 0L) {
    taskQueue += Task(
      timeMillis = fakeSystemClock.getTimeMillis() + delayMillis,
      block = block,
      insertionOrder = totalTaskCount.incrementAndGet()
    )
    notifyIfRunning()
  }

  @Suppress("ControlFlowWithEmptyBody")
  private fun flushTaskQueue(currentTimeMillis: Long) {
    // TODO(#89): Add timeout support so that the dispatcher can't effectively deadlock or livelock
    // for inappropriately behaved tests.
    while (isRunning.get()) {
      if (!flushActiveTaskQueue(currentTimeMillis)) {
        break
      }
    }
    while (executingTaskCount.get() > 0);

    notifyIfIdle()
  }

  /** Flushes the current task queue and returns whether any tasks were executed. */
  private fun flushActiveTaskQueue(currentTimeMillis: Long): Boolean {
    if (isTaskQueueActive(currentTimeMillis)) {
      // Create a copy of the task queue in case it's changed during modification.
      val tasksToRemove = createSortedTaskSet().filter { task ->
        if (isRunning.get()) {
          if (task.timeMillis <= currentTimeMillis) {
            // Only remove the task if it was executed.
            task.block.run()
            return@filter true
          }
        }
        return@filter false
      }
      return taskQueue.removeAll(tasksToRemove)
    }
    return false
  }

  private fun isTaskQueueActive(currentTimeMillis: Long): Boolean {
    return taskQueue.hasPendingCompletableTasks(currentTimeMillis) || executingTaskCount.get() != 0
  }

  private fun createDeferredRunnable(context: CoroutineContext, block: Runnable): Runnable {
    return kotlinx.coroutines.Runnable {
      executingTaskCount.incrementAndGet()
      realCoroutineDispatcher.dispatch(context, kotlinx.coroutines.Runnable {
        try {
          block.run()
        } finally {
          executingTaskCount.decrementAndGet()
        }
      })
    }
  }

  private fun createContinuationRunnable(continuation: CancellableContinuation<Unit>): Runnable {
    val block: CancellableContinuation<Unit>.() -> Unit = {
      realCoroutineDispatcher.resumeUndispatched(Unit)
    }
    return kotlinx.coroutines.Runnable {
      try {
        executingTaskCount.incrementAndGet()
        continuation.block()
      } finally {
        executingTaskCount.decrementAndGet()
      }
    }
  }

  private fun createSortedTaskSet(): Set<Task> {
    val sortedSet = TreeSet(
      Comparator.comparingLong(Task::timeMillis)
        .thenComparing(Task::insertionOrder)
    )
    sortedSet.addAll(taskQueue)
    return sortedSet
  }

  private fun notifyIfRunning() {
    taskIdleListener?.takeIf { executingTaskCount.get() > 0 }?.onDispatcherRunning()
  }

  private fun notifyIfIdle() {
    taskIdleListener?.takeIf { executingTaskCount.get() == 0 }?.onDispatcherIdle()
  }

  class FactoryImpl @Inject constructor(
    private val fakeSystemClock: FakeSystemClock
  ) : TestCoroutineDispatcher.Factory {
    override fun createDispatcher(realDispatcher: CoroutineDispatcher): TestCoroutineDispatcher {
      return TestCoroutineDispatcherRobolectricImpl(fakeSystemClock, realDispatcher)
    }
  }
}

private data class Task(
  internal val block: Runnable,
  internal val timeMillis: Long,
  internal val insertionOrder: Int
)

private fun CopyOnWriteArraySet<Task>.hasPendingCompletableTasks(currentTimeMillis: Long): Boolean {
  return any { task -> task.timeMillis <= currentTimeMillis }
}
