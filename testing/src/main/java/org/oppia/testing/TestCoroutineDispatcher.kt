package org.oppia.testing;

import androidx.annotation.GuardedBy
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.DelayController
import kotlinx.coroutines.test.UncompletedCoroutinesError
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.lang.Long.max
import java.lang.UnsupportedOperationException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@InternalCoroutinesApi
@Suppress("EXPERIMENTAL_API_USAGE")
class TestCoroutineDispatcher private constructor(
  private val fakeSystemClock: FakeSystemClock,
  private val realCoroutineDispatcher: CoroutineDispatcher
): CoroutineDispatcher(), Delay, DelayController {

  private val realCoroutineScope = CoroutineScope(realCoroutineDispatcher)
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

  override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
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
    // There's not a clear way to handle this block while remaining the thread of the dispatcher, so disabled it for
    // now until it's needed later.
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
    // TODO: add timeout.
    while (isRunning.get() && dispatcherLock.withLock {
        if (isTaskQueueActive(currentTimeMillis)) {
          taskQueue.forEach { task ->
            if (isRunning.get()) {
              task.block.run()
            }
          }
          taskQueue.clear()
          return@withLock true
        }
        return@withLock false
      }
    );
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
//    return Runnable {
//      executingTaskCount.incrementAndGet()
//      realCoroutineScope.launch {
//        withContext(context) {
//          withTimeout(10_000) {
//            block.run()
//          }
//        }
//      }.invokeOnCompletion {
//        executingTaskCount.decrementAndGet()
//      }
//    }
  }

  private fun createContinuationRunnable(continuation: CancellableContinuation<Unit>): Runnable {
    val block: CancellableContinuation<Unit>.() -> Unit = { realCoroutineDispatcher.resumeUndispatched(Unit) }
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

private data class Task(internal val block: Runnable, internal val timeMillis: Long, internal val insertionOrder: Int)

private fun Set<Task>.hasPendingCompletableTasks(currentTimeMilis: Long): Boolean {
  return any { task -> task.timeMillis <= currentTimeMilis }
}
