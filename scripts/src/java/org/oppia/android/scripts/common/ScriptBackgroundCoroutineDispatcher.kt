package org.oppia.android.scripts.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.asCoroutineDispatcher
import java.io.Closeable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

/**
 * A [CoroutineDispatcher] that's [Closeable] and particularly tailored to be easily used in scripts
 * that need to perform parallel tasks for expensive IO. It's highly recommended to exclusively use
 * this dispatcher over any others, and to ensure that [close] is called at the end of the script to
 * avoid any potential threads hanging (causing the script to not actually close).
 *
 * Note that the dispatcher attempts to finish any ongoing tasks when [close] is called, but it will
 * reject new tasks from being scheduled and it will force terminate if any pending tasks at the
 * time of closing don't end within the configured [closeTimeout] provided.
 *
 * A simple example for using this dispatcher:
 * ```kotlin
 * ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
 *   val deferred = CoroutineScope(scriptBgDispatcher).async {
 *     // Expensive task...
 *   }
 *   // IMPORTANT: The operation must be observed before use{} ends, otherwise the dispatcher will
 *   // close and terminate any pending tasks.
 *   runBlocking { deferred.await() }
 * }
 * ```
 *
 * A more complex example for I/O operations:
 * ```kotlin
 * ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
 *   val deferred = CoroutineScope(scriptBgDispatcher).async {
 *     withContext(Dispatchers.IO) {
 *      // Perform I/O using Kotlin's highly parallelized I/O dispatcher, but wait for the result
 *      // using the background script dispatcher (since execution could continue if other I/O
 *      // operations need to be kicked off, or if other work can be done alongside the I/O).
 *     }
 *   }
 *   // IMPORTANT: The operation must be observed before use{} ends, otherwise the dispatcher will
 *   // close and terminate any pending tasks.
 *   runBlocking { deferred.await() }
 * }
 * ```
 *
 * @property closeTimeout the amount of time, in [closeTimeoutUnit] units, that should be waited
 *     when [close]ing this dispatcher before force-ending ongoing tasks
 * @property closeTimeoutUnit the unit of time used for [closeTimeout]
 */
class ScriptBackgroundCoroutineDispatcher(
  private val closeTimeout: Long = 5,
  private val closeTimeoutUnit: TimeUnit = TimeUnit.SECONDS
) : CoroutineDispatcher(), Closeable {
  private val threadPool by lazy { Executors.newCachedThreadPool() }
  private val coroutineDispatcher by lazy { threadPool.asCoroutineDispatcher() }

  override fun dispatch(context: CoroutineContext, block: Runnable) {
    coroutineDispatcher.dispatch(context, block)
  }

  override fun close() {
    threadPool.tryShutdownFully(timeout = closeTimeout, unit = closeTimeoutUnit)
    coroutineDispatcher.close()
  }

  private companion object {
    private fun ExecutorService.tryShutdownFully(timeout: Long, unit: TimeUnit) {
      // Try to fully shutdown the executor service per https://stackoverflow.com/a/33690603 and
      // https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ExecutorService.html.
      shutdown()
      try {
        if (!awaitTermination(timeout, unit)) {
          shutdownNow()
          check(awaitTermination(timeout, unit)) { "ExecutorService didn't fully shutdown: $this." }
        }
      } catch (e: InterruptedException) {
        shutdownNow()
        Thread.currentThread().interrupt()
      }
    }
  }
}
