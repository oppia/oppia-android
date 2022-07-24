package org.oppia.android.testing.threading

import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.ListenableScheduledFuture
import com.google.common.util.concurrent.ListeningScheduledExecutorService
import com.google.common.util.concurrent.MoreExecutors
import com.google.common.util.concurrent.SettableFuture
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

// TODO(#1274): Add tests for this class.
/**
 * [CoordinatedScheduledExecutorService] that performs all execution immediately.
 *
 * New instances can be created by injecting [FactoryImpl] and using [FactoryImpl.create].
 *
 * This class should never be used directly by tests. Instead, using the codebase-wide dispatchers
 * or executors directly for task execution, or [TestCoroutineDispatchers] for execution
 * coordination.
 */
@Suppress("UnstableApiUsage") // For Guava 'Beta' classes.
class RealTimeScheduledExecutorService private constructor(
  backingExecutorService: ScheduledExecutorService
) : CoordinatedScheduledExecutorService() {
  override val backingListenableService: ListeningScheduledExecutorService by lazy {
    MoreExecutors.listeningDecorator(backingExecutorService)
  }
  private val executingTasks = CopyOnWriteArrayList<Task>()
  private val totalTaskCount = AtomicInteger(0)
  /** Map of task ID (based on [totalTaskCount]) to the time in millis when that task will run. */
  private val taskCompletionTimes = ConcurrentHashMap<Int, Long>()
  private var taskIdleListener: MonitoredTaskCoordinator.TaskIdleListener? = null
  private val terminationFuture = SettableFuture.create<Unit>()

  override fun hasPendingTasks(): Boolean = executingTasks.isNotEmpty()

  override fun getNextFutureTaskCompletionTimeMillis(timeMillis: Long): Long? {
    // Find the next most recent task completion time that's after the specified time.
    return taskCompletionTimes.values.filter { it > timeMillis }.minOrNull()
  }

  override fun hasPendingCompletableTasks(): Boolean {
    // Any pending tasks are always considered completable since the dispatcher runs in real-time.
    return hasPendingTasks()
  }

  override fun setTaskIdleListener(taskIdleListener: MonitoredTaskCoordinator.TaskIdleListener) {
    this.taskIdleListener = taskIdleListener
    if (executingTasks.isNotEmpty()) {
      notifyIfRunning()
    } else {
      notifyIfIdle()
    }
  }

  override fun runCurrent(timeout: Long, timeoutUnit: TimeUnit) {
    // Nothing to do; the queue is always continuously running.
  }

  override fun scheduleOneOffRunnable(
    block: Runnable,
    delay: Long,
    delayUnit: TimeUnit
  ): ListenableScheduledFuture<*> {
    return scheduleOneOffTask(
      block, backingListenableService.schedule(block, delay, delayUnit), delayUnit.toMillis(delay)
    )
  }

  override fun <T> scheduleOneOffCallable(
    block: Callable<T>,
    delay: Long,
    delayUnit: TimeUnit
  ): ListenableScheduledFuture<T> {
    return scheduleOneOffTask(
      runnableBlock = { block.call() },
      backingListenableService.schedule(block, delay, delayUnit),
      delayUnit.toMillis(delay)
    )
  }

  override fun scheduleRecurringTask(
    block: Runnable,
    initialDelayMillis: Long,
    periodicDelayMillis: Long,
    scheduledAtFixedRate: Boolean
  ): ListenableScheduledFuture<Unit> {
    return MonitoredScheduledFuture(
      System::currentTimeMillis,
      initialDelayMillis,
      periodicDelayMillis,
      scheduleTask = this::scheduleOneOffTaskAtTime,
      block,
      scheduledAtFixedRate,
      executor = this,
      delayInitially = true
    )
  }

  override fun getTerminationFuture(): ListenableFuture<Unit> =
    terminationFuture.also { notifyIfIdle() }

  override fun forceEndPendingTasks(): List<Runnable> {
    // Assume the service should be immediately terminated.
    terminate()

    // All tasks that are current scheduled should be forcibly canceled.
    return executingTasks.map { (taskRunnable, taskFuture) ->
      taskRunnable.also { taskFuture.cancel(/* mayInterruptIfRunning= */ true) }
    }.also { executingTasks.clear() }
  }

  // Implementation-specific methods.

  private fun <T> scheduleOneOffTask(
    runnableBlock: Runnable,
    taskFuture: ListenableScheduledFuture<T>,
    expectedDelayMillis: Long
  ): ListenableScheduledFuture<T> {
    if (isShutdown) throw RejectedExecutionException()

    val taskId = totalTaskCount.incrementAndGet()
    taskCompletionTimes[taskId] = System.currentTimeMillis() + expectedDelayMillis

    // Tasks immediately will start running, so track the task immediately.
    val task = Task(runnableBlock, taskFuture)
    executingTasks += task
    notifyIfRunning()
    Futures.addCallback(
      taskFuture,
      object : FutureCallback<T> {
        override fun onSuccess(value: T?) {
          finishTask()
        }

        override fun onFailure(error: Throwable) {
          finishTask()
        }

        private fun finishTask() {
          executingTasks -= task
          taskCompletionTimes.remove(taskId)
          notifyIfIdle()
        }
      },
      backingListenableService
    )
    return taskFuture
  }

  private fun scheduleOneOffTaskAtTime(
    task: Runnable,
    timeToRunMillis: Long
  ): ListenableFuture<Unit> {
    // If the service is shutdown, do nothing.
    if (isShutdown) return Futures.immediateFuture(Unit)
    val delayMillis = (timeToRunMillis - System.currentTimeMillis()).coerceAtLeast(0)
    val taskFuture = backingListenableService.schedule(task, delayMillis, TimeUnit.MILLISECONDS)
    val scheduledFuture = scheduleOneOffTask(task, taskFuture, delayMillis)
    return Futures.transform(scheduledFuture, {}, MoreExecutors.directExecutor())
  }

  private fun notifyIfRunning() {
    taskIdleListener?.takeIf { executingTasks.isNotEmpty() }?.onCoordinatorRunning()
  }

  private fun notifyIfIdle() {
    if (executingTasks.isEmpty()) {
      taskIdleListener?.onCoordinatorIdle()

      // If there are no remaining tasks to run and the service is requested to be shut down, then
      // terminate it.
      if (isShutdown) {
        terminate()
        terminationFuture.set(Unit)
      }
    }
  }

  private data class Task(val block: Runnable, val taskFuture: ListenableScheduledFuture<*>)

  /**
   * [CoordinatedScheduledExecutorService.Factory] for creating new
   * [RealTimeScheduledExecutorService]s.
   */
  class FactoryImpl @Inject constructor() : Factory {
    /** Returns a new [RealTimeScheduledExecutorService] with the specified backing service. */
    override fun create(backingService: ScheduledExecutorService) =
      RealTimeScheduledExecutorService(backingService)
  }
}
