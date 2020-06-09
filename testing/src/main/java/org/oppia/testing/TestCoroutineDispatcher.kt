package org.oppia.testing;

import androidx.annotation.GuardedBy
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.test.DelayController
import kotlinx.coroutines.test.UncompletedCoroutinesError
import java.lang.Long.max
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock
import kotlin.coroutines.CoroutineContext

/**
 * Replacement for Kotlin's test coroutine dispatcher that can be used to replace coroutine
 * dispatching functionality in a Robolectric test in a way that can be coordinated across multiple
 * dispatchers for execution synchronization.
 */
@InternalCoroutinesApi
@Suppress("EXPERIMENTAL_API_USAGE")
class TestCoroutineDispatcher private constructor(
  private val fakeSystemClock: FakeSystemClock,
  private val realCoroutineDispatcher: CoroutineDispatcher
): CoroutineDispatcher(), Delay, DelayController {

  private val dispatcherLock = ReentrantLock()
  /** Sorted set that first sorts on when a task should be executed, then insertion order. */
  @GuardedBy("dispatcherLock") private val taskQueue = sortedSetOf(
    Comparator.comparingLong(Task::timeMillis)
      .thenComparing(Task::insertionOrder)
  )
  private val isRunning = AtomicBoolean(true) // Partially blocked on dispatcherLock.
  private val executingTaskCount = AtomicInteger(0)
  private val totalTaskCount = AtomicInteger(0)

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
    val timeToLastTask = max(0, taskQueue.last().timeMillis - fakeSystemClock.getTimeMillis())
    return advanceTimeBy(timeToLastTask)
  }

  @ExperimentalCoroutinesApi
  override fun cleanupTestCoroutines() {
    val remainingTaskCount = dispatcherLock.withLock {
      flushTaskQueue(fakeSystemClock.getTimeMillis())
      return@withLock taskQueue.size
    }
    if (remainingTaskCount != 0) {
      throw UncompletedCoroutinesError(
        "Expected no remaining tasks for test dispatcher, but found $remainingTaskCount"
      )
    }
  }

  @ExperimentalCoroutinesApi
  override fun pauseDispatcher() {
    dispatcherLock.withLock { isRunning.set(false) }
  }

  @ExperimentalCoroutinesApi
  override suspend fun pauseDispatcher(block: suspend () -> Unit) {
    // There's not a clear way to handle this block while maintaining the thread of the dispatcher,
    // so disabled it for now until it's needed later.
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

  internal fun hasPendingTasks(): Boolean {
    return dispatcherLock.withLock {
      taskQueue.isNotEmpty()
    }
  }

  internal fun hasPendingCompletableTasks(): Boolean {
    return dispatcherLock.withLock {
      taskQueue.hasPendingCompletableTasks(fakeSystemClock.getTimeMillis())
    }
  }

  private fun enqueueTask(block: Runnable, delayMillis: Long = 0L) {
    val task = Task(
      timeMillis = fakeSystemClock.getTimeMillis() + delayMillis,
      block = block,
      insertionOrder = totalTaskCount.incrementAndGet()
    )
    enqueueTask(task)
  }

  private fun enqueueTask(task: Task) {
    dispatcherLock.withLock {
      taskQueue += task
    }
  }

  private fun flushTaskQueue(currentTimeMillis: Long) {
    // TODO(#89): Add timeout support so that the dispatcher can't effectively deadlock or livelock
    // for inappropriately behaved tests.
    while (isRunning.get()) {
      if (!dispatcherLock.withLock { flushActiveTaskQueue(currentTimeMillis) }) {
        break
      }
    }
  }

  /** Flushes the current task queue and returns whether it was active. */
  @GuardedBy("dispatcherLock")
  private fun flushActiveTaskQueue(currentTimeMillis: Long): Boolean {
    if (isTaskQueueActive(currentTimeMillis)) {
      taskQueue.forEach { task ->
        if (isRunning.get()) {
          task.block.run()
        }
      }
      taskQueue.clear()
      return true
    }
    return false
  }

  private fun isTaskQueueActive(currentTimeMillis: Long): Boolean {
    return taskQueue.hasPendingCompletableTasks(currentTimeMillis) || executingTaskCount.get() != 0
  }

  private fun createDeferredRunnable(context: CoroutineContext, block: Runnable): Runnable {
    return Runnable {
      executingTaskCount.incrementAndGet()
      realCoroutineDispatcher.dispatch(context, Runnable {
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
    return Runnable {
      try {
        executingTaskCount.incrementAndGet()
        continuation.block()
      } finally {
        executingTaskCount.decrementAndGet()
      }
    }
  }

  class Factory @Inject constructor(private val fakeSystemClock: FakeSystemClock) {
    fun createDispatcher(realDispatcher: CoroutineDispatcher): TestCoroutineDispatcher {
      return TestCoroutineDispatcher(fakeSystemClock, realDispatcher)
    }
  }
}

private data class Task(
  internal val block: Runnable,
  internal val timeMillis: Long,
  internal val insertionOrder: Int
)

private fun Set<Task>.hasPendingCompletableTasks(currentTimeMilis: Long): Boolean {
  return any { task -> task.timeMillis <= currentTimeMilis }
}
