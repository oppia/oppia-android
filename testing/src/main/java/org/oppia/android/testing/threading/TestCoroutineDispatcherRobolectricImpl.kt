package org.oppia.android.testing.threading

import android.annotation.SuppressLint
import androidx.annotation.GuardedBy
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.oppia.android.testing.time.FakeSystemClock
import java.util.TreeSet
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock
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
) : TestCoroutineDispatcher(), Delay {

  /** Sorted set that first sorts on when a task should be executed, then insertion order. */
  private val taskQueue = CopyOnWriteArraySet<Task>()
  private val lock = ReentrantLock()

  @GuardedBy("lock")
  private var executingTaskCount = 0

  @GuardedBy("lock")
  private var state = State.IDLE

  @GuardedBy("lock")
  private var consideringNewState = false

  @GuardedBy("lock")
  private var pendingDispatchCounts = 0
  private val totalTaskCount = AtomicInteger(0)
  private var taskIdleListener: TaskIdleListener? = null
  private val monitoredCoroutineDispatcher by lazy {
    MonitoredCoroutineDispatcher(
      delegate = realCoroutineDispatcher,
      testDispatcher = this,
      this::incrementExecutingTaskCount,
      this::decrementExecutingTaskCount
    )
  }

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

  override fun dispatch(context: CoroutineContext, block: Runnable) {
    enqueueTask(createDeferredRunnable(context, block))
  }

  override fun scheduleResumeAfterDelay(
    timeMillis: Long,
    continuation: CancellableContinuation<Unit>
  ) {
    enqueueTask(createContinuationRunnable(continuation), delayMillis = timeMillis)
  }

  override fun runCurrent(timeout: Long, timeoutUnit: TimeUnit) {
    flushTaskQueueBlocking(fakeSystemClock.getTimeMillis(), timeoutUnit.toMillis(timeout))
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
    dispatchCurrentState(lock.withLock { state })
  }

  private fun enqueueTask(block: Runnable, delayMillis: Long = 0L) {
    taskQueue += Task(
      timeMillis = fakeSystemClock.getTimeMillis() + delayMillis,
      block = block,
      insertionOrder = totalTaskCount.incrementAndGet()
    )
    maybeNotifyNewState()
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
    // Note that '{}' is used instead of a semicolon since ktlint may incorrectly remove semicolons
    // in valid cases. See #3052 for context.
    while (flushActiveTaskQueue(currentTimeMillis)) {}
    while (lock.withLock { isDispatcherActive() }) {}
    maybeNotifyNewState()
  }

  @GuardedBy("lock")
  private fun isDispatcherActive(): Boolean {
    // Note that the complicated locking mechanism below is needed since locks are held for as
    // little time as possible. The checks are combined in one lock to try and decrease cross-lock
    // inconsistencies. It's likely there are race conditions here, but they probably won't cause
    // issues in most testing circumstances, and fixing them would require either leveraging more
    // aggressive locking (which could risk deadlocking) or introducing an architecturally-enforced
    // messaging pattern like Kotlin actors.
    return executingTaskCount > 0 || consideringNewState || pendingDispatchCounts > 0
  }

  /** Flushes the current task queue and returns whether any tasks were executed. */
  private fun flushActiveTaskQueue(currentTimeMillis: Long): Boolean {
    if (isTaskQueueActive(currentTimeMillis)) {
      // Create a copy of the task queue in case it's changed during modification.
      val tasksToRemove = createSortedTaskSet().filter { task ->
        if (task.timeMillis <= currentTimeMillis) {
          // Only remove the task if it was executed.
          task.block.run()
          return@filter true
        }
        return@filter false
      }
      return taskQueue.removeAll(tasksToRemove)
    }
    return false
  }

  private fun isTaskQueueActive(currentTimeMillis: Long): Boolean {
    return taskQueue.hasPendingCompletableTasks(currentTimeMillis) ||
      lock.withLock { executingTaskCount } != 0
  }

  private fun createDeferredRunnable(context: CoroutineContext, block: Runnable): Runnable {
    return Runnable {
      monitoredCoroutineDispatcher.dispatch(context, block)
    }
  }

  private fun createContinuationRunnable(continuation: CancellableContinuation<Unit>): Runnable {
    return Runnable {
      // Delegate the continuation to the monitored dispatcher so that the executing task count can
      // be correctly decremented after the task is finished (to avoid race conditions against the
      // caller). Note that the Runnable check is for compatibility with the particular Kotlin
      // coroutine implementation. Changes to the coroutine library or migrations to alternate
      // libraries may break the mechanism used to track continuations.
      check(continuation is Runnable) { "Expected continuation to be a Runnable" }
      monitoredCoroutineDispatcher.resumeContinuation(continuation)
    }
  }

  private fun incrementExecutingTaskCount() {
    lock.withLock {
      consideringNewState = true
      executingTaskCount++
    }
    maybeNotifyNewState()
  }

  private fun decrementExecutingTaskCount() {
    lock.withLock {
      consideringNewState = true
      executingTaskCount--
    }
    maybeNotifyNewState()
  }

  @SuppressLint("NewApi") // Robolectric-only code that's not bound by SDK.
  private fun createSortedTaskSet(): Set<Task> {
    val sortedSet = TreeSet(
      Comparator.comparingLong(Task::timeMillis)
        .thenComparing(Task::insertionOrder)
    )
    sortedSet.addAll(taskQueue)
    return sortedSet
  }

  private fun maybeNotifyNewState() {
    val (previousState, newState) = lock.withLock {
      val newState = State.inferFromExecutingTaskCount(executingTaskCount)
      val previousState = state
      state = newState
      // Once state has been checked, see if a dispatch is needed.
      consideringNewState = false
      if (newState != previousState) {
        // A dispatch is needed.
        pendingDispatchCounts++
      }
      return@withLock previousState to newState
    }
    if (previousState != newState) {
      dispatchCurrentState(newState)
      // Record that the dispatch has fired to unblock any locks waiting on the state.
      lock.withLock { pendingDispatchCounts-- }
    }
  }

  private fun dispatchCurrentState(currentState: State): Unit? {
    // Use a return value to force the compiler to ensure the 'when' statement is exhaustive.
    return when (currentState) {
      State.IDLE -> {
        taskIdleListener?.onDispatcherIdle()
      }
      State.RUNNING -> {
        taskIdleListener?.onDispatcherRunning()
      }
    }
  }

  private companion object {
    private data class Task(
      val block: Runnable,
      val timeMillis: Long,
      val insertionOrder: Int
    )

    private fun CopyOnWriteArraySet<Task>.hasPendingCompletableTasks(
      currentTimeMillis: Long
    ): Boolean {
      return any { task -> task.timeMillis <= currentTimeMillis }
    }
  }

  private enum class State {
    IDLE,
    RUNNING;

    companion object {
      fun inferFromExecutingTaskCount(taskCount: Int): State {
        return when (taskCount) {
          0 -> IDLE
          else -> RUNNING
        }
      }
    }
  }

  /**
   * A proxy [CoroutineDispatcher] that delegates execution to another dispatcher, but can be
   * monitor the completion of each task (including resumed continuations).
   */
  private class MonitoredCoroutineDispatcher(
    private val delegate: CoroutineDispatcher,
    private val testDispatcher: CoroutineDispatcher,
    private val incrementExecutingTaskCount: () -> Unit,
    private val decrementExecutingTaskCount: () -> Unit,
  ) : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
      incrementExecutingTaskCount()
      delegate.dispatch(context) {
        try {
          block.run()
        } finally {
          decrementExecutingTaskCount()
        }
      }
    }

    /**
     * Resumes the specified continuation using the delegate continuation. The dispatcher will track
     * the start/ending of the continuation using the provided [incrementExecutingTaskCount] and
     * [decrementExecutingTaskCount] callbacks.
     */
    fun resumeContinuation(cancellableContinuation: CancellableContinuation<Unit>) {
      incrementExecutingTaskCount()
      try {
        cancellableContinuation.apply {
          testDispatcher.resumeUndispatched(Unit)
        }
      } finally {
        decrementExecutingTaskCount()
      }
    }
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
