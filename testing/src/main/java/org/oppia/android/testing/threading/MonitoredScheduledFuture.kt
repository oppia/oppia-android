package org.oppia.android.testing.threading

import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.ListenableScheduledFuture
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Delayed
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

// TODO(#1274): Add tests for this class.
/**
 * A [ListenableScheduledFuture] that supports running a [scheduleTask] on a specified [executor]
 * with configured delays and periodic scheduling.
 *
 * Note that the actual scheduling doesn't begin on until this future is started (per its [run]
 * method). Furthermore, cancelling this future will cease future scheduling.
 *
 * This future is only meant to ever be used by [CoordinatedScheduledExecutorService] and its
 * implementations.
 */
@Suppress("UnstableApiUsage") // For Guava 'Beta' classes.
class MonitoredScheduledFuture<T>(
  private val getCurrentTimeMillis: () -> Long,
  private val runTimeMillis: Long,
  private val periodicTimeMillis: Long,
  private val scheduleTask: (Runnable, Long) -> ListenableFuture<T>,
  private val executableBlock: Runnable,
  private val scheduleAtFixedRate: Boolean,
  private val executor: Executor,
  private val delayInitially: Boolean
) : ListenableScheduledFuture<T>, Runnable {
  private val cancelled = AtomicBoolean()
  private val currentTask = AtomicReference<ListenableFuture<T>>()
  private val listeners = CopyOnWriteArrayList<Pair<Runnable, Executor>>()

  override fun compareTo(other: Delayed?): Int =
    compareValues(getDelay(TimeUnit.MILLISECONDS), other?.getDelay(TimeUnit.MILLISECONDS))

  override fun getDelay(unit: TimeUnit?): Long {
    if (unit == null) throw NullPointerException("Expected non-null unit.")
    val remainingTime = (runTimeMillis - getCurrentTimeMillis()).coerceAtLeast(0)
    return unit.convert(remainingTime, TimeUnit.MILLISECONDS)
  }

  override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
    finish()
    return currentTask.get()?.cancel(mayInterruptIfRunning) ?: true
  }

  override fun isCancelled(): Boolean = cancelled.get()

  // This future is only ever 'done' when it's been cancelled.
  override fun isDone(): Boolean = isCancelled

  override fun get(): Nothing = error("This future does not support .get() operations.")

  override fun get(timeout: Long, unit: TimeUnit): Nothing =
    error("This future does not support .get() operations.")

  override fun addListener(listener: Runnable, executor: Executor) {
    if (isCancelled) {
      // Future is already finished.
      executor.execute(listener)
    } else listeners += listener to executor
  }

  override fun run() {
    if (!isCancelled) {
      // Immediately execute the block if the wrapping future was already delayed.
      val initialDelay = if (delayInitially) runTimeMillis else 0
      val initialFuture =
        scheduleTask(executableBlock, getCurrentTimeMillis() + initialDelay)
      currentTask.set(initialFuture)
      Futures.addCallback(initialFuture, createFutureCallback(), executor)
    }
  }

  private fun finish() {
    cancelled.set(true)
    listeners.forEach { (listener, executor) -> executor.execute(listener) }
    listeners.clear()
  }

  private fun createFutureCallback(): FutureCallback<T> {
    return object : FutureCallback<T> {
      private var timeLastTaskStarted = getCurrentTimeMillis()

      override fun onSuccess(unused: T?) {
        if (!isCancelled) {
          val timeLastTaskFinished = getCurrentTimeMillis()
          val timeToStart = if (scheduleAtFixedRate) {
            timeLastTaskStarted + periodicTimeMillis
          } else timeLastTaskFinished + periodicTimeMillis // Otherwise, fixed delay.
          val nextFuture = scheduleTask(executableBlock, timeToStart)
          currentTask.set(nextFuture)
          timeLastTaskStarted = getCurrentTimeMillis()
          Futures.addCallback(nextFuture, this, executor)
        }
      }

      override fun onFailure(throwable: Throwable) {
        finish()
      }
    }
  }
}
