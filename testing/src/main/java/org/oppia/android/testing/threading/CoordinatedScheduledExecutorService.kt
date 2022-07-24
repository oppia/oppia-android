package org.oppia.android.testing.threading

import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.ListeningScheduledExecutorService
import com.google.common.util.concurrent.MoreExecutors.directExecutor
import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicBoolean

// TODO(#1274): Add tests for this class.
/**
 * [MonitoredTaskCoordinator] that also acts as a [ScheduledExecutorService].
 *
 * This class provides a general unification between task coordination and scheduling these tasks
 * for execution. It does not, however, define specifics on how execution itself should be handled.
 * Implementations of this class are responsible for defining how and when scheduled tasks are
 * executed.
 *
 * This class should, generally, never be called outside of this package. It should instead only
 * ever be referenced via its super interfaces depending on the specific situation in which it's
 * being used. It's expected, for example, that implementations of this class can be step-in
 * replacements for codebase-wide executor services (including tying these back to coroutines using
 * an executor-converted coroutine dispatcher).
 *
 * Implementations should be injectable in the application component via implementations of
 * [Factory].
 *
 * Finally, it's expected that implementations use another backing executor service to perform
 * actual task execution. Closing this service will not close this backing service as multiple
 * [CoordinatedScheduledExecutorService]s may share the same backing service.
 */
@Suppress("UnstableApiUsage") // For Guava 'Beta' classes.
abstract class CoordinatedScheduledExecutorService :
  MonitoredTaskCoordinator, ScheduledExecutorService {
  private val shutdown = AtomicBoolean()
  private val terminated = AtomicBoolean()

  /** Represents the executor service that is backing all execution for the implemented service. */
  protected abstract val backingListenableService: ListeningScheduledExecutorService

  /**
   * Schedules a one-off [Runnable] to run with the specified [delay] (with unit indicated by
   * [delayUnit]), and returns the [ScheduledFuture] tracking its execution state.
   */
  protected abstract fun scheduleOneOffRunnable(
    block: Runnable,
    delay: Long = 0L,
    delayUnit: TimeUnit = TimeUnit.MILLISECONDS
  ): ScheduledFuture<*>

  /**
   * Schedules a one-off [Callable] to run with the specified [delay] (with unit indicated by
   * [delayUnit]), and returns the [ScheduledFuture] tracking its execution state and result (per
   * the [block] [Callable]).
   */
  protected abstract fun <T> scheduleOneOffCallable(
    block: Callable<T>,
    delay: Long = 0L,
    delayUnit: TimeUnit = TimeUnit.MILLISECONDS
  ): ScheduledFuture<T>

  /**
   * Schedules a recurring [Runnable] for execution.
   *
   * @param block the [Runnable] block that will be periodically executed
   * @param initialDelayMillis the number of milliseconds to wait before the first execution
   * @param periodicDelayMillis the time spent between runs (with exact behavior determined by
   *     [scheduledAtFixedRate])
   * @param scheduledAtFixedRate if true, [periodicDelayMillis] defines how much time is spent
   *     between [block] being started (i.e. a fixed rate of execution). If false,
   *     [periodicDelayMillis] defines how much time is spent between [block]s being run (i.e. a
   *     fixed delay of execution).
   * @return a [ScheduledFuture] which can be used to cancel the recurring tasks, but never
   *     represents an actual result and will never finish so long as the tasks are still running
   */
  protected abstract fun scheduleRecurringTask(
    block: Runnable,
    initialDelayMillis: Long,
    periodicDelayMillis: Long,
    scheduledAtFixedRate: Boolean
  ): ScheduledFuture<Unit>

  /**
   * Returns a [ListenableFuture] which tracks the termination state of this service.
   *
   * Note that that calling this does not actually initiate termination ([shutdown] or [shutdownNow]
   * must be called first to actually end the service).
   */
  protected abstract fun getTerminationFuture(): ListenableFuture<Unit>

  /**
   * Forcibly ends task execution and returns the list of [Runnable]s that were not executed, or
   * weren't finished being executed.
   */
  protected abstract fun forceEndPendingTasks(): List<Runnable>

  // Methods overridden from Executor.

  final override fun execute(command: Runnable?) {
    if (command == null) throw NullPointerException("Expected non-null command.")
    scheduleOneOffRunnable(command)
  }

  // Methods overridden from ExecutorService.

  final override fun awaitTermination(timeout: Long, unit: TimeUnit?): Boolean {
    if (unit == null) throw NullPointerException("Expected non-null unit.")
    shutdown()

    val terminationFuture = Futures.transform(getTerminationFuture(), { true }, directExecutor())
    val timedFuture =
      Futures.withTimeout(terminationFuture, timeout, unit, backingListenableService)
    val alwaysPassingFuture =
      Futures.catching(timedFuture, TimeoutException::class.java, { false }, directExecutor())
    // This .get() is expected to return quickly after the specified timeout in the worst-case
    // scenario, but the additional timeout is added as a safety mechanism.
    return alwaysPassingFuture.get(timeout * 2, unit)
  }

  // Blocking methods like this cannot be supported since they will be waiting on the service to
  // finish executing, but the service waits until it's told to execute (by the test). This will
  // cause a deadlock.
  final override fun <T : Any?> invokeAll(
    tasks: MutableCollection<out Callable<T>>?
  ): Nothing = error("This method is not supported as it will cause a deadlock in tests.")

  // See invokeAll for an explanation on why this is unsupported.
  final override fun <T : Any?> invokeAll(
    tasks: MutableCollection<out Callable<T>>?,
    timeout: Long,
    unit: TimeUnit?
  ): Nothing = error("This method is not supported as it will cause a deadlock in tests.")

  // See invokeAll for an explanation on why this is unsupported.
  final override fun <T : Any?> invokeAny(tasks: MutableCollection<out Callable<T>>?): Nothing =
    error("This method is not supported as it will cause a deadlock in tests.")

  // See invokeAll for an explanation on why this is unsupported.
  final override fun <T : Any?> invokeAny(
    tasks: MutableCollection<out Callable<T>>?,
    timeout: Long,
    unit: TimeUnit?
  ): Nothing = error("This method is not supported as it will cause a deadlock in tests.")

  final override fun isShutdown(): Boolean = shutdown.get()

  final override fun isTerminated(): Boolean = terminated.get()

  final override fun shutdown() {
    shutdown.set(true)
  }

  final override fun shutdownNow(): MutableList<Runnable> {
    shutdown()
    return forceEndPendingTasks().toMutableList()
  }

  final override fun submit(task: Runnable?): Future<*> {
    if (task == null) throw NullPointerException("Expected non-null task.")
    return scheduleOneOffRunnable(task)
  }

  final override fun <T : Any?> submit(task: Runnable?, result: T): Future<T> {
    if (task == null) throw NullPointerException("Expected non-null task.")
    return submit(Callable<T> { result.also { task.run() } })
  }

  final override fun <T : Any?> submit(task: Callable<T>?): Future<T> {
    if (task == null) throw NullPointerException("Expected non-null task.")
    return scheduleOneOffCallable(task)
  }

  // Methods overridden from ScheduledExecutorService.

  final override fun schedule(
    command: Runnable?,
    delay: Long,
    unit: TimeUnit?
  ): ScheduledFuture<*> {
    if (command == null) throw NullPointerException("Expected non-null command.")
    if (unit == null) throw NullPointerException("Expected non-null unit.")
    return scheduleOneOffRunnable(command, delay, unit)
  }

  final override fun <V : Any?> schedule(
    callable: Callable<V>?,
    delay: Long,
    unit: TimeUnit?
  ): ScheduledFuture<V> {
    if (callable == null) throw NullPointerException("Expected non-null callable.")
    if (unit == null) throw NullPointerException("Expected non-null unit.")
    return scheduleOneOffCallable(callable, delay, unit)
  }

  final override fun scheduleAtFixedRate(
    command: Runnable?,
    initialDelay: Long,
    period: Long,
    unit: TimeUnit?
  ): ScheduledFuture<*> {
    if (command == null) throw NullPointerException("Expected non-null command.")
    if (unit == null) throw NullPointerException("Expected non-null unit.")
    return scheduleRecurringTask(
      command, unit.toMillis(initialDelay), unit.toMillis(initialDelay), scheduledAtFixedRate = true
    )
  }

  final override fun scheduleWithFixedDelay(
    command: Runnable?,
    initialDelay: Long,
    delay: Long,
    unit: TimeUnit?
  ): ScheduledFuture<*> {
    if (command == null) throw NullPointerException("Expected non-null command.")
    if (unit == null) throw NullPointerException("Expected non-null unit.")
    return scheduleRecurringTask(
      command, unit.toMillis(initialDelay), unit.toMillis(delay), scheduledAtFixedRate = false
    )
  }

  // Callbacks for implementations.

  /** Sets this service as now terminated (per [isTerminated]). */
  protected fun terminate() {
    terminated.set(true)
  }

  /** Factory for creating new [CoordinatedScheduledExecutorService]s. */
  interface Factory {
    /**
     * Returns a new [CoordinatedScheduledExecutorService] with the specified
     * [backingService] as the executor service used to actually execute tasks coordinated by the
     * returned service.
     */
    fun create(backingService: ScheduledExecutorService): CoordinatedScheduledExecutorService
  }
}
