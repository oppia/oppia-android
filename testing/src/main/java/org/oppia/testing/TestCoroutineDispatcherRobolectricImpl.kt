package org.oppia.testing

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.DelayController
import kotlinx.coroutines.test.UncompletedCoroutinesError
import kotlinx.coroutines.withTimeout
import java.lang.IllegalStateException
import java.util.TreeSet
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import kotlin.Comparator
import kotlin.coroutines.CoroutineContext

// TODO(#89): Audit & adjust the thread safety of this class, and determine if there's a way to move
//  off of the internal coroutine API.

/**
 * Robolectric-specific implementation of [TestCoroutineDispatcher].
 *
 * This implementation makes use of a fake clock & event queue to manage tasks scheduled both for
 * the present and the future. It executes tasks on a real coroutine dispatcher, but only when it's
 * time for the task to run per the fake clock & the task's location in the event queue.
 *
 * Note that not all functionality in [TestCoroutineDispatcher]'s superclasses are implemented here,
 * and other functionality is delegated to [TestCoroutineDispatchers] to ensure proper thread
 * safety.
 */
@InternalCoroutinesApi
@ExperimentalCoroutinesApi
class TestCoroutineDispatcherRobolectricImpl private constructor(
  private val fakeSystemClock: FakeSystemClock,
  private val realCoroutineDispatcher: CoroutineDispatcher
) : TestCoroutineDispatcher(), Delay, DelayController {

  /** Sorted set that first sorts on when a task should be executed, then insertion order. */
  private val taskQueue = CopyOnWriteArraySet<Task>()
  private val isRunning = AtomicBoolean(true)
  private val executingTaskCount = AtomicInteger(0)
  private val totalTaskCount = AtomicInteger(0)
  private var taskIdleListener: TaskIdleListener? = null

  /**
   * A coroutine dispatcher used to monitor flushing the dispatcher queue. This is not used as a
   * true coroutine dispatcher since interactions with it always block the calling thread.
   */
  private val queueCoroutineDispatcher by lazy {
    Executors.newSingleThreadExecutor().asCoroutineDispatcher()
  }
  private val queueCoroutineScope by lazy {
    CoroutineScope(queueCoroutineDispatcher)
  }

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
    flushTaskQueueBlocking(
      fakeSystemClock.advanceTime(delayTimeMillis),
      DEFAULT_TIMEOUT_UNIT.toMillis(DEFAULT_TIMEOUT_SECONDS)
    )
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
    flushTaskQueueBlocking(
      fakeSystemClock.getTimeMillis(), DEFAULT_TIMEOUT_UNIT.toMillis(DEFAULT_TIMEOUT_SECONDS)
    )
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
    flushTaskQueueBlocking(
      fakeSystemClock.getTimeMillis(), DEFAULT_TIMEOUT_UNIT.toMillis(DEFAULT_TIMEOUT_SECONDS)
    )
  }

  @ExperimentalCoroutinesApi
  override fun runCurrent() {
    runCurrent(DEFAULT_TIMEOUT_SECONDS, DEFAULT_TIMEOUT_UNIT)
  }

  override fun runCurrent(timeout: Long, timeoutUnit: TimeUnit) {
    flushTaskQueueBlocking(fakeSystemClock.getTimeMillis(), timeoutUnit.toMillis(timeout))
  }

  override fun runUntilIdle(timeout: Long, timeoutUnit: TimeUnit) {
    val runUntilIdleDeferred = queueCoroutineScope.async {
      var nextTaskTimeMillis: Long?
      do {
        val currentTimeMillis = fakeSystemClock.getTimeMillis()
        flushTaskQueueNonBlocking(fakeSystemClock.getTimeMillis())
        nextTaskTimeMillis = getNextFutureTaskCompletionTimeMillis(currentTimeMillis)
        if (nextTaskTimeMillis != null) {
          fakeSystemClock.advanceTime(nextTaskTimeMillis - currentTimeMillis)
        }
      } while (nextTaskTimeMillis != null)
    }
    runBlocking {
      val timeoutMillis = timeoutUnit.toMillis(timeout)
      try {
        withTimeout(timeoutMillis) {
          runUntilIdleDeferred.await()
        }
      } catch (e: TimeoutCancellationException) {
        throw IllegalStateException("Dispatcher failed to idle in ${timeoutMillis}ms", e)
      }
    }
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

  private fun flushTaskQueueBlocking(currentTimeMillis: Long, timeoutMillis: Long) {
    val flushTaskDeferred = queueCoroutineScope.async {
      flushTaskQueueNonBlocking(currentTimeMillis)
    }
    runBlocking {
      try {
        withTimeout(timeoutMillis) {
          flushTaskDeferred.await()
        }
      } catch (e: TimeoutCancellationException) {
        throw IllegalStateException(
          "Dispatcher failed to finish flush queue in ${timeoutMillis}ms", e
        )
      }
    }
  }

  @Suppress("ControlFlowWithEmptyBody")
  private fun flushTaskQueueNonBlocking(currentTimeMillis: Long) {
    while (isRunning.get()) {
      if (!flushActiveTaskQueue(currentTimeMillis)) {
        break
      }
    }
    while (executingTaskCount.get() > 0)
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
      realCoroutineDispatcher.dispatch(
        context,
        kotlinx.coroutines.Runnable {
          try {
            block.run()
          } finally {
            executingTaskCount.decrementAndGet()
          }
        }
      )
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

  /**
   * Injectable implementation of [TestCoroutineDispatcher.Factory] for
   * [TestCoroutineDispatcherEspressoImpl].
   */
  class FactoryImpl @Inject constructor(
    private val fakeSystemClock: FakeSystemClock
  ) : Factory {
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
