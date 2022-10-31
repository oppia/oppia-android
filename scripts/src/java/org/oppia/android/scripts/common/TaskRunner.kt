package org.oppia.android.scripts.common

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher

object TaskRunner {
  private val maxThreadCount by lazy { Runtime.getRuntime().availableProcessors() }
  val executorService by lazy {
    createThreadExecutor(
      corePoolSize = maxThreadCount,
      maximumPoolSize = maxThreadCount,
      keepAliveTime = 1_000,
      TimeUnit.SECONDS
    )
  }
  val coroutineDispatcher by lazy { executorService.asCoroutineDispatcher() }
  private val daemonThreadFactory by lazy {
    ThreadFactory { r -> Thread(r).also { it.isDaemon = true } }
  }

  private fun createThreadExecutor(
    corePoolSize: Int, maximumPoolSize: Int, keepAliveTime: Long, unit: TimeUnit
  ): ThreadPoolExecutor {
    return ThreadPoolExecutor(
      corePoolSize,
      maximumPoolSize,
      keepAliveTime,
      unit,
      LinkedBlockingQueue(),
      daemonThreadFactory
    )
  }
}
