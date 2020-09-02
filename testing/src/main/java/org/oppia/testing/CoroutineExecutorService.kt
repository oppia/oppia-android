package org.oppia.testing

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Listener for being notified when [CoroutineExecutorService] has arranged state and is immediately
 * about to block some sort of thread that requires the calling test to coordinate to avoid a
 * deadlock.
 */
typealias BlockingCallback = () -> Unit

private typealias TimeoutBlock<T> = suspend CoroutineScope.() -> T

// https://github.com/Kotlin/kotlinx.coroutines/issues/1450 for reference on using a coroutine
// dispatcher an an executor service.
/**
 * An [ExecutorService] that uses Oppia's [CoroutineDispatcher]s for interoperability with tests.
 *
 * Note while this service is being thoroughly tested, both the implementation and tests are based
 * on a specific interpretation of the [ExecutorService] API. As a result, This class is
 * **NOT PRODUCTION READY**. It should _only_ be used for testing purposes. This class should be
 * used for APIs that rely on a Java executor for background activity that must be synchronized with
 * other Oppia operations in tests. Uses of this class will automatically be compatible with
 * [TestCoroutineDispatchers] and its idling resource.
 *
 * Note also that the built-in executor service (as suggested by
 * https://github.com/Kotlin/kotlinx.coroutines/issues/1450) is not used because that assumes the
 * underlying dispatcher is an ExecutorService which may not necessarily be the case, and it doesn't
 * allow cooperation with Oppia's test coroutine dispatchers utility (which is the purpose of this
 * class).
 */
class CoroutineExecutorService(
  private val backgroundDispatcher: CoroutineDispatcher
) : ExecutorService {
  private val serviceLock = ReentrantLock()
  private val taskCount = AtomicInteger()
  private var isShutdown = false
  private val pendingTasks = mutableMapOf<Int, Task<*>>()
  private val cachedThreadPool by lazy { Executors.newCachedThreadPool() }
  /**
   * Coroutine dispatcher for executing consecutive tasks for blocking the calling thread and without
   * interfering with other potentially blocked operations leveraging this scope. This is done using
   * a cached thread pool that creates new threads as others become blocked.
   */
  private val cachedThreadCoroutineDispatcher by lazy { cachedThreadPool.asCoroutineDispatcher() }
  private var priorToBlockingCallback: BlockingCallback? = null
  private var afterSelectionSetupCallback: BlockingCallback? = null

  override fun shutdown() {
    serviceLock.withLock { isShutdown = true }
  }

  override fun <T : Any?> submit(task: Callable<T>?): Future<T> {
    return dispatchAsync(task ?: throw NullPointerException()).toFuture()
  }

  override fun <T : Any?> submit(task: Runnable?, result: T): Future<T> {
    if (task == null) {
      throw NullPointerException()
    }
    return submit(
      Callable<T> {
        task.run()
        return@Callable result
      }
    )
  }

  override fun submit(task: Runnable?): Future<*> {
    return dispatchAsync(task ?: throw NullPointerException()).toFuture()
  }

  override fun shutdownNow(): MutableList<Runnable> {
    shutdown()
    val incompleteTasks = serviceLock.withLock { pendingTasks.values }
    incompleteTasks.map { it.deferred }.forEach { it.cancel() }
    return incompleteTasks.map { it.runnable }.toMutableList()
  }

  override fun isShutdown(): Boolean = serviceLock.withLock { isShutdown }

  override fun awaitTermination(timeout: Long, unit: TimeUnit?): Boolean {
    check(serviceLock.withLock { isShutdown })
    val incompleteTasks = serviceLock.withLock { pendingTasks.values }
    val timeoutMillis = unit?.toMillis(timeout) ?: 0

    // Create a separate scope in case one of the operations fails--it shouldn't cause later
    // operations to fail.
    val cachedThreadCoroutineScope = CoroutineScope(cachedThreadCoroutineDispatcher)

    // Wait for each task to complete within the specified time. Note that this behaves similarly to
    // invokeAll() below.
    val futureTasks = incompleteTasks.map { task ->
      cachedThreadCoroutineScope.async {
        maybeWithTimeoutOrNull(timeoutMillis) {
          // Wait for the task to be completed.
          task.deferred.await()
        }
      }
    }
    priorToBlockingCallback?.invoke()
    return runBlocking { futureTasks.awaitAll() }.mapNotNull { it }.isEmpty() // Null = timed out.
  }

  override fun <T : Any?> invokeAny(tasks: MutableCollection<out Callable<T>>?): T =
    invokeAny(tasks, 0, TimeUnit.MILLISECONDS)

  override fun <T : Any?> invokeAny(
    tasks: MutableCollection<out Callable<T>>?,
    timeout: Long,
    unit: TimeUnit?
  ): T {
    if (tasks == null) {
      throw NullPointerException()
    }
    // The channel to receive completed tasks. Note that a channel is used over a combination of
    // select + awaitAll() or per-deferred onAwait clauses since this approach allows manual
    // handling of failures: a failure should not result in all other tasks being cancelled (which
    // is the default behavior for select).
    val resultChannel = Channel<T>()
    val taskDeferreds = tasks.map { dispatchAsync(it) }
    taskDeferreds.forEach { deferred ->
      @Suppress("DeferredResultUnused") // Intentionally silence failures (including the service's).
      // Create a separate scope in case one of the operations fails--it shouldn't cause later
      // operations to fail.
      CoroutineScope(cachedThreadCoroutineDispatcher).async {
        try {
          val result = deferred.await()
          resultChannel.send(result)
        } catch (e: Exception) {
          // Ignore the exception, and send nothing to the result channel. This catch avoids the
          // cached thread scope from entering a broken state.
        }
      }
    }

    // Wait until the first result is posted to the channel. Note that this is slightly different
    // than the expected behavior of this function: it can exit before all tasks are completed.
    priorToBlockingCallback?.invoke()
    return runBlocking {
      select<T> {
        // Use a timeout here instead of wrapping the select since select does not support
        // cooperative cancellation. That approach leads to a race between the timeout and the
        // selection actually completing in time whereas this ensures early cancellation from
        // timeout due to cooperation.
        val timeoutMillis = unit?.toMillis(timeout) ?: 0
        if (timeoutMillis > 0) {
          @Suppress("EXPERIMENTAL_API_USAGE")
          onTimeout(timeoutMillis) { throw TimeoutException("Timed out after $timeoutMillis") }
        }
        resultChannel.onReceive { it }
        afterSelectionSetupCallback?.invoke()
      } ?: throw ExecutionException(IllegalStateException("All tasks failed to run"))
    }
  }

  override fun isTerminated(): Boolean {
    return serviceLock.withLock { isShutdown && pendingTasks.isEmpty() }
  }

  override fun <T : Any?> invokeAll(
    tasks: MutableCollection<out Callable<T>>?
  ): MutableList<Future<T>> = invokeAll(tasks, 0, TimeUnit.MILLISECONDS)

  override fun <T : Any?> invokeAll(
    tasks: MutableCollection<out Callable<T>>?,
    timeout: Long,
    unit: TimeUnit?
  ): MutableList<Future<T>> {
    if (tasks == null) {
      throw NullPointerException()
    }
    val timeoutMillis = unit?.toMillis(timeout) ?: 0
    val deferredTasks = tasks.map { dispatchAsync(it) }

    // Wait for each task to complete within the specified time, otherwise cancel the task. Note
    // that the timeout needs to be set up for each task in parallel to avoid a sequentialization of
    // the tasks being executed (potentially causing tasks later in the list to not time out).
    val futureTasks = deferredTasks.map { task ->
      // Create a separate scope in case one of the operations fails--it shouldn't cause later
      // operations to fail.
      CoroutineScope(cachedThreadCoroutineDispatcher).async {
        // Note that the 'or null' part may unfortunately interfere with a legitimate null return
        // for the underlying callable.
        val result = maybeWithTimeoutOrNull(timeoutMillis) {
          return@maybeWithTimeoutOrNull try {
            task.await()
          } catch (e: Exception) {
            // Do not allow failures to cause the coroutine scope to fail.
            null
          }
        }
        val future = task.toFuture()
        if (result == null && timeoutMillis > 0) {
          // Cancel the operation if it's taking too long, but only if there's a timeout set. This
          // won't cancel the future if the deferred is completed with a failure, or passing with a
          // null result.
          check(future.cancel(/* mayInterruptIfRunning= */ true)) { "Failed to cancel task." }
        }
        return@async future
      }
    }
    priorToBlockingCallback?.invoke()
    return runBlocking { futureTasks.awaitAll() }.toMutableList()
  }

  @Suppress("DeferredResultUnused") // Cleanup is handled in dispatchAsync.
  override fun execute(command: Runnable?) {
    dispatchAsync(command ?: throw NullPointerException())
  }

  /**
   * Sets a [BlockingCallback] to observe this service's internal state. Note that since this is an
   * intentional backdoor built into the service, it should only be used for very specific
   * circumstances (such as testing blocking operations of this service).
   *
   * Tracks when the service is about to immediately block the calling thread.
   */
  @VisibleForTesting
  fun setPriorToBlockingCallback(priorToBlockingCallback: BlockingCallback) {
    this.priorToBlockingCallback = priorToBlockingCallback
  }

  /**
   * Sets a [BlockingCallback] to observe this service's internal state. Note that since this is an
   * intentional backdoor built into the service, it should only be used for very specific
   * circumstances (such as testing blocking operations of this service).
   *
   * Tracks when the service has finished setting up a Kotlin select block. This should be used in
   * relevant cases instead of [setPriorToBlockingCallback] to avoid the selection automatically
   * completing for relevant tasks (which will happen if blocking tasks are fully resolved before
   * setting up the select block).
   */
  @VisibleForTesting
  fun setAfterSelectionSetupCallback(afterSelectionSetupCallback: BlockingCallback) {
    this.afterSelectionSetupCallback = afterSelectionSetupCallback
  }

  private fun dispatchAsync(command: Runnable): Deferred<*> {
    return dispatchAsync(command.let { Callable<Unit> { it.run() } })
  }

  private fun <T> dispatchAsync(command: Callable<T>): Deferred<T> {
    return command.let { dispatchAsync { it.call() } }
  }

  private fun <T> dispatchAsync(command: suspend () -> T): Deferred<T> {
    if (serviceLock.withLock { isShutdown }) {
      throw RejectedExecutionException()
    }

    // A new scope is created to allow the underlying async task to fail without affecting future
    // tasks. An alternative approach would be to use a supervised scope that's allowed to fail.
    // This would required monitoring using a separate dispatcher scope that can fail without
    // affecting future operations.
    val taskId = taskCount.incrementAndGet()
    val deferred = CoroutineScope(backgroundDispatcher).async { runAsync(taskId, command) }

    // Note: this Runnable is *probably* incorrect, but ExecutorService doesn't indicate which
    // Runnables are provided, what they should do when run, or how they tie back to submitted
    // Callables.
    val task = Task(Runnable { runBlocking { command() } }, deferred)
    serviceLock.withLock { pendingTasks.put(taskId, task) }
    deferred.invokeOnCompletion {
      serviceLock.withLock { pendingTasks.remove(taskId) }
    }
    return deferred
  }

  private suspend fun <T> runAsync(taskId: Int, command: suspend () -> T): T {
    // This should never fail since cleanup of tasks only happens after this completes, or is
    // cancelled. It can't execute after being cleaned up with the current implementation.
    check(serviceLock.withLock { taskId in pendingTasks })
    return command()
  }

  private data class Task<T>(val runnable: Runnable, val deferred: Deferred<T>)

  /**
   * Returns a new [Future] based on a [Deferred]. Note that the APIs between these two async
   * constructs are different, so there may be some subtle inconsistencies in practice.
   */
  private fun <T> Deferred<T>.toFuture(): Future<T> {
    val deferred: Deferred<T> = this
    return object : Future<T> {
      override fun isDone(): Boolean = deferred.isCompleted

      override fun get(): T = get(/* timeout= */ 0, TimeUnit.MILLISECONDS)

      override fun get(timeout: Long, unit: TimeUnit?): T {
        return runBlocking {
          try {
            maybeWithTimeout(unit?.toMillis(timeout) ?: 0) {
              deferred.await()
            }
          } catch (e: Exception) {
            // Rethrow the failure if the computation failed.
            throw ExecutionException(e)
          }
        }
      }

      override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        return if (!deferred.isCompleted) {
          deferred.cancel()
          true
        } else {
          false
        }
      }

      override fun isCancelled(): Boolean = deferred.isCancelled
    }
  }

  private companion object {
    /**
     * Wraps the specified block in a withTimeout() only if the specified timeout is larger than 0.
     */
    private suspend fun <T> maybeWithTimeout(
      timeoutMillis: Long,
      block: TimeoutBlock<T>
    ): T {
      return maybeWithTimeoutDelegated(timeoutMillis, block, ::withTimeout)
    }

    /**
     * Wraps the specified block in a withTimeoutOrNull() only if the specified timeout is larger
     * than 0.
     */
    private suspend fun <T> maybeWithTimeoutOrNull(
      timeoutMillis: Long,
      block: TimeoutBlock<T>
    ): T? {
      return maybeWithTimeoutDelegated<T, T?>(timeoutMillis, block, ::withTimeoutOrNull)
    }

    private suspend fun <T : R, R> maybeWithTimeoutDelegated(
      timeoutMillis: Long,
      block: TimeoutBlock<T>,
      withTimeoutDelegate: suspend (Long, TimeoutBlock<T>) -> R
    ): R {
      return coroutineScope {
        if (timeoutMillis > 0) {
          try {
            withTimeoutDelegate(timeoutMillis, block)
          } catch (e: TimeoutCancellationException) {
            // Treat timeouts in this service as a standard TimeoutException (which should result in
            // the coroutine being completed with a failure).
            throw TimeoutException(e.message)
          }
        } else {
          block()
        }
      }
    }
  }
}
