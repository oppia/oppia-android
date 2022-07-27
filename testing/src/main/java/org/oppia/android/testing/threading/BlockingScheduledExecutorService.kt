package org.oppia.android.testing.threading

import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.ListenableScheduledFuture
import com.google.common.util.concurrent.ListeningScheduledExecutorService
import com.google.common.util.concurrent.MoreExecutors
import com.google.common.util.concurrent.SettableFuture
import org.oppia.android.testing.time.FakeSystemClock
import java.util.TreeSet
import java.util.concurrent.Callable
import java.util.concurrent.CancellationException
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Delayed
import java.util.concurrent.Executor
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

// TODO(#1274): Add tests for this class.
/**
 * [CoordinatedScheduledExecutorService] that defers all execution until [runCurrent] is called.
 *
 * New instances can be created by injecting [FactoryImpl] and using [FactoryImpl.create].
 *
 * Note that this class also relies on [FakeSystemClock] for its internal time coordination.
 *
 * This class should never be used directly by tests. Instead, using the codebase-wide dispatchers
 * or executors directly for task execution, or [TestCoroutineDispatchers] for execution
 * coordination.
 */
@Suppress("UnstableApiUsage") // For Guava 'Beta' classes.
class BlockingScheduledExecutorService private constructor(
  private val fakeSystemClock: FakeSystemClock,
  backingExecutorService: ScheduledExecutorService
) : CoordinatedScheduledExecutorService() {
  override val backingListenableService: ListeningScheduledExecutorService by lazy {
    MoreExecutors.listeningDecorator(backingExecutorService)
  }
  private val taskQueue = CopyOnWriteArraySet<Task<*>>()
  private var executingTaskCount = AtomicInteger()
  private var state = AtomicReference(State.IDLE)
  private var taskIdleListener: MonitoredTaskCoordinator.TaskIdleListener? = null
  private val totalTaskCount = AtomicInteger(0)
  private val terminationFuture = SettableFuture.create<Unit>()

  override fun hasPendingTasks(): Boolean = taskQueue.isNotEmpty()

  override fun getNextFutureTaskCompletionTimeMillis(timeMillis: Long): Long? {
    return createSortedTaskSet().firstOrNull { task -> task.timeMillis > timeMillis }?.timeMillis
  }

  override fun hasPendingCompletableTasks(): Boolean {
    return taskQueue.hasPendingCompletableTasks(fakeSystemClock.getTimeMillis())
  }

  override fun setTaskIdleListener(taskIdleListener: MonitoredTaskCoordinator.TaskIdleListener) {
    this.taskIdleListener = taskIdleListener
    dispatchCurrentState(state.get())
  }

  override fun runCurrent(timeout: Long, timeoutUnit: TimeUnit) {
    // Wait up to the timeout for the full known list of next tasks to complete.
    val tasksFuture = runAllActiveTasks(fakeSystemClock.getTimeMillis())
    // Catch cancellations so that they don't cause tests to fail (since it's completely valid for
    // coroutine jobs to be cancelled during regular runtime).
    val nonCancellableFuture =
      Futures.catching(tasksFuture, CancellationException::class.java, {}, backingListenableService)
    Futures.withTimeout(nonCancellableFuture, timeout, timeoutUnit, backingListenableService).get()
  }

  override fun scheduleOneOffRunnable(
    block: Runnable,
    delay: Long,
    delayUnit: TimeUnit
  ): ScheduledFuture<*> {
    return scheduleOneOffTask(
      ExecutableBlock.RunnableBlock(block),
      timeToRunMillis = fakeSystemClock.getTimeMillis() + delayUnit.toMillis(delay)
    )
  }

  override fun <T> scheduleOneOffCallable(
    block: Callable<T>,
    delay: Long,
    delayUnit: TimeUnit
  ): ScheduledFuture<T> {
    return scheduleOneOffTask(
      ExecutableBlock.CallableBlock(block),
      timeToRunMillis = fakeSystemClock.getTimeMillis() + delayUnit.toMillis(delay)
    )
  }

  override fun scheduleRecurringTask(
    block: Runnable,
    initialDelayMillis: Long,
    periodicDelayMillis: Long,
    scheduledAtFixedRate: Boolean
  ): ScheduledFuture<Unit> {
    val future =
      MonitoredScheduledFuture(
        fakeSystemClock::getTimeMillis,
        initialDelayMillis,
        periodicDelayMillis,
        scheduleTask = { runnableBlock, timeToRunMillis ->
          // Don't schedule another task if the service is shut down.
          if (!isShutdown) {
            scheduleOneOffTask(ExecutableBlock.RunnableBlock(runnableBlock), timeToRunMillis)
          } else Futures.immediateFuture(Unit)
        },
        block,
        scheduledAtFixedRate,
        executor = this,
        delayInitially = false // Delay via the initial scheduling.
      )
    return scheduleOneOffTask(ExecutableBlock.FutureBlock(future), initialDelayMillis)
  }

  override fun getTerminationFuture(): ListenableFuture<Unit> =
    terminationFuture.also { maybeNotifyNewState() }

  override fun forceEndPendingTasks(): List<Runnable> {
    // Assume the executor is terminated at this point.
    terminate()

    // Return runnables for all remaining tasks. Note that this does not attempt to interrupt any
    // ongoing tasks in the backing service.
    return taskQueue.map {
      Runnable { it.run(backingListenableService) }
    }.also { taskQueue.clear() }
  }

  // Implementation-specific methods.

  private fun <T> scheduleOneOffTask(
    block: ExecutableBlock<T>,
    timeToRunMillis: Long
  ): ListenableScheduledFuture<T> {
    if (isShutdown) throw RejectedExecutionException()
    return Task(fakeSystemClock, block, timeToRunMillis, totalTaskCount.incrementAndGet()).also {
      taskQueue += it
      maybeNotifyNewState()
    }.completionFuture
  }

  private fun incrementExecutingTaskCount() {
    executingTaskCount.incrementAndGet()
    maybeNotifyNewState()
  }

  private fun decrementExecutingTaskCount() {
    executingTaskCount.decrementAndGet()
    maybeNotifyNewState()
  }

  private fun createSortedTaskSet(): Set<Task<*>> {
    return TreeSet(compareBy(Task<*>::timeMillis).thenBy(Task<*>::insertionOrder)).apply {
      addAll(taskQueue)
    }
  }

  private fun maybeNotifyNewState() {
    val newState = State.inferFromExecutingTaskCount(executingTaskCount.get())
    if (state.getAndSet(newState) != newState) {
      dispatchCurrentState(newState)
    }

    // Check if execution has finished.
    if (newState == State.IDLE && taskQueue.isEmpty() && isShutdown) {
      terminate()
      terminationFuture.set(Unit)
    }
  }

  private fun dispatchCurrentState(currentState: State): Unit? {
    // Use a return value to force the compiler to ensure the 'when' statement is exhaustive.
    return when (currentState) {
      State.IDLE -> taskIdleListener?.onCoordinatorIdle()
      State.RUNNING -> taskIdleListener?.onCoordinatorRunning()
    }
  }

  private fun runAllActiveTasks(currentTimeMillis: Long): ListenableFuture<Unit> {
    // Execute all next scheduled tasks until no more remain. It's possible that some tasks could
    // get missed right at the end of the flush, but realistically this shouldn't happen if all
    // threads are behaving in the test.
    val taskFutures = flushActiveTaskQueue(currentTimeMillis)
    return if (taskFutures.isNotEmpty()) {
      Futures.transformAsync(
        Futures.allAsList(taskFutures),
        { runAllActiveTasks(currentTimeMillis) },
        backingListenableService
      )
    } else Futures.transform(Futures.immediateVoidFuture(), {}, MoreExecutors.directExecutor())
  }

  /** Flushes the current task queue and returns the futures of tasks that were started. */
  private fun flushActiveTaskQueue(currentTimeMillis: Long): Collection<ListenableFuture<*>> {
    if (isTaskQueueActive(currentTimeMillis)) {
      // Create a copy of the task queue in case it's changed during modification.
      val tasksToRemove = createSortedTaskSet().filter { task ->
        if (task.timeMillis <= currentTimeMillis) {
          // Only remove the task if it was executed.
          incrementExecutingTaskCount()
          try {
            task.run(backingListenableService)
          } finally {
            decrementExecutingTaskCount()
          }
          return@filter true
        }
        return@filter false
      }.toSet()
      taskQueue.removeAll(tasksToRemove)
      return tasksToRemove.map { it.completionFuture }
    }
    return listOf()
  }

  private fun isTaskQueueActive(currentTimeMillis: Long): Boolean =
    taskQueue.hasPendingCompletableTasks(currentTimeMillis) || executingTaskCount.get() != 0

  /**
   * Represents a task that will be executed at [timeMillis] and was scheduled as the
   * [insertionOrder]th task, with execution block [block].
   */
  private class Task<T>(
    fakeSystemClock: FakeSystemClock,
    private val block: ExecutableBlock<T>,
    val timeMillis: Long,
    val insertionOrder: Int
  ) {
    private val settableCompletionFuture by lazy {
      ScheduledSettableFuture<T>(fakeSystemClock, timeMillis)
    }
    /** The [ListenableScheduledFuture] which will be completed only once this task executes. */
    val completionFuture: ListenableScheduledFuture<T>
      get() = settableCompletionFuture

    /** Begins executing this task using the specified [executor]. */
    fun run(executor: Executor) {
      settableCompletionFuture.setFuture(block.toFuture(executor))
    }
  }

  /**
   * Represents an executable block of code (in various contexts--see subclasses) for a [Task].
   *
   * @param T the type of data expected to be returned by the execution block
   */
  private sealed class ExecutableBlock<T> {
    /**
     * Returns a [ListenableFuture] that represents the execution status of this block, using the
     * specified [executor] to run it.
     *
     * Implementation note: a future is used to wrap the execution block so that it doesn't actually
     * get executed until it's time to run, and on the correct executor. This also allows wrapping
     * futures to be properly canceled.
     */
    abstract fun toFuture(executor: Executor): ListenableFuture<T>

    /** An [ExecutableBlock] corresponding to a [Runnable]. */
    class RunnableBlock(private val runnable: Runnable) : ExecutableBlock<Unit>() {
      override fun toFuture(executor: Executor): ListenableFuture<Unit> =
        runnable.toDeferredFuture(executor)
    }

    /** An [ExecutableBlock] corresponding to a [Callable] with return type [V]. */
    class CallableBlock<V>(private val callable: Callable<V>) : ExecutableBlock<V>() {
      override fun toFuture(executor: Executor): ListenableFuture<V> =
        callable.toDeferredFuture(executor)
    }

    /**
     * An [ExecutableBlock] corresponding to the specified [baseFuture].
     *
     * Note that this implementation doesn't defer execution in the same way as other
     * [ExecutableBlock]s might. Instead, it provides a way to bridge an already-executing
     * [ListenableFuture] to APIs which expect an [ExecutableBlock].
     */
    class FutureBlock<V>(private val baseFuture: ListenableFuture<V>) : ExecutableBlock<V>() {
      override fun toFuture(executor: Executor): ListenableFuture<V> = baseFuture
    }

    private companion object {
      private fun <T> Executor.toDeferredFuture(block: () -> T) =
        Futures.transform(Futures.immediateVoidFuture(), { block() }, this)

      private fun Runnable.toDeferredFuture(executor: Executor) =
        executor.toDeferredFuture { Unit.also { run() } }

      private fun <T> Callable<T>.toDeferredFuture(executor: Executor) =
        executor.toDeferredFuture { call() }
    }
  }

  /** A [ListenableScheduledFuture] version of [SettableFuture]. */
  private class ScheduledSettableFuture<T>(
    private val fakeSystemClock: FakeSystemClock,
    private val runTimeMillis: Long
  ) : ListenableScheduledFuture<T> {
    private val backingFuture = SettableFuture.create<T>()

    override fun compareTo(other: Delayed?): Int =
      compareValues(getDelay(TimeUnit.MILLISECONDS), other?.getDelay(TimeUnit.MILLISECONDS))

    override fun getDelay(unit: TimeUnit?): Long {
      if (unit == null) throw NullPointerException("Expected non-null unit.")
      val remainingTime = (runTimeMillis - fakeSystemClock.getTimeMillis()).coerceAtLeast(0)
      return unit.convert(remainingTime, TimeUnit.MILLISECONDS)
    }

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean =
      backingFuture.cancel(mayInterruptIfRunning)

    override fun isCancelled(): Boolean = backingFuture.isCancelled

    override fun isDone(): Boolean = backingFuture.isDone

    override fun get(): T = backingFuture.get()

    override fun get(timeout: Long, unit: TimeUnit): T = backingFuture.get(timeout, unit)

    override fun addListener(listener: Runnable, executor: Executor) =
      backingFuture.addListener(listener, executor)

    /** Sets this future's result to that of [valueFuture]. */
    fun setFuture(valueFuture: ListenableFuture<T>) = backingFuture.setFuture(valueFuture)
  }

  /** Represents the current running state of the service. */
  private enum class State {
    /**
     * Indicates that the service is idle (i.e. no tasks are running or scheduled to immediately
     * run).
     *
     * Note that there still may be tasks that can be run in the future.
     */
    IDLE,

    /**
     * Indicates that the service has currently active tasks being run or soon-to-be-run (i.e.
     * scheduled to run now).
     */
    RUNNING;

    companion object {
      /**
       * Returns a computed [State] representing the current execution state of the service per the
       * number of active running tasks (indicated by [taskCount]).
       * */
      fun inferFromExecutingTaskCount(taskCount: Int): State = if (taskCount == 0) IDLE else RUNNING
    }
  }

  private companion object {
    private fun CopyOnWriteArraySet<Task<*>>.hasPendingCompletableTasks(currentTimeMillis: Long) =
      any { task -> task.timeMillis <= currentTimeMillis }
  }

  /**
   * [CoordinatedScheduledExecutorService.Factory] for creating new
   * [BlockingScheduledExecutorService]s.
   */
  class FactoryImpl @Inject constructor(private val fakeSystemClock: FakeSystemClock) : Factory {
    /** Returns a new [BlockingScheduledExecutorService] with the specified backing service. */
    override fun create(backingService: ScheduledExecutorService) =
      BlockingScheduledExecutorService(fakeSystemClock, backingService)
  }
}
