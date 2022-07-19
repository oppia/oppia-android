package org.oppia.android.domain.oppialogger.logscheduler

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import org.oppia.android.domain.oppialogger.analytics.PerformanceMetricsLogger
import org.oppia.android.domain.util.getStringFromData
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.threading.BackgroundDispatcher
import javax.inject.Inject

/**
 * Worker class that generates metric log reports regarding the performance of the application
 * and then stores it in in device cache.
 */
class MetricLogSchedulingWorker private constructor(
  context: Context,
  params: WorkerParameters,
  private val consoleLogger: ConsoleLogger,
  private val performanceMetricsLogger: PerformanceMetricsLogger,
  @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher
) : ListenableWorker(context, params) {

  companion object {
    private const val TAG = "MetricLogSchedulingWorker"
    /**
     * Input data for the worker consists of a key-value pair on the basis of which work is done.
     * [WORKER_CASE_KEY] is the key for that data.
     */
    const val WORKER_CASE_KEY = "metric_log_scheduling_worker_case_key"
    /**
     * When [PERIODIC_METRIC_WORKER] is the value of the worker's key-value pair, the worker
     * schedules logging of periodic performance metrics.
     */
    const val PERIODIC_METRIC_WORKER = "periodic_metric_worker"
    /**
     * When [STORAGE_USAGE_WORKER] is the value of the worker's key-value pair, the worker schedules
     * logging of storage usage performance metrics.
     */
    const val STORAGE_USAGE_WORKER = "storage_usage_worker"
    /**
     * When [MEMORY_USAGE_WORKER] is the value of the worker's key-value pair, the worker schedules
     * logging of memory usage performance metrics.
     */
    const val MEMORY_USAGE_WORKER = "memory_usage_worker"
  }

  override fun startWork(): ListenableFuture<Result> {
    val backgroundScope = CoroutineScope(backgroundDispatcher)
    val result = backgroundScope.async {
      when (inputData.getStringFromData(WORKER_CASE_KEY)) {
        PERIODIC_METRIC_WORKER -> schedulePeriodicMetricLogging()
        STORAGE_USAGE_WORKER -> scheduleStorageUsageMetricLogging()
        MEMORY_USAGE_WORKER -> scheduleMemoryUsageMetricLogging()
        else -> Result.failure()
      }
    }

    val future = SettableFuture.create<Result>()
    result.invokeOnCompletion { failure ->
      if (failure != null) {
        future.setException(failure)
      } else {
        future.set(result.getCompleted())
      }
    }
    // TODO(#3715): Add withTimeout() to avoid potential hanging.
    return future
  }

  private fun schedulePeriodicMetricLogging(): Result {
    return try {
      // TODO(#4340): Add functionality to log cpu and network usage performance metrics.
      Result.success()
    } catch (e: Exception) {
      consoleLogger.e(TAG, e.toString(), e)
      return Result.failure()
    }
  }

  private fun scheduleStorageUsageMetricLogging(): Result {
    return try {
      // TODO(#4340): Add functionality to log storage usage performance metrics.
      Result.success()
    } catch (e: Exception) {
      consoleLogger.e(TAG, e.toString(), e)
      return Result.failure()
    }
  }

  private fun scheduleMemoryUsageMetricLogging(): Result {
    return try {
      // TODO(#4340): Add functionality to log memory usage performance metrics.
      Result.success()
    } catch (e: Exception) {
      consoleLogger.e(TAG, e.toString(), e)
      return Result.failure()
    }
  }

  /** Creates an instance of [MetricLogSchedulingWorker] by properly injecting dependencies. */
  class Factory @Inject constructor(
    private val consoleLogger: ConsoleLogger,
    private val performanceMetricsLogger: PerformanceMetricsLogger,
    @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher
  ) {
    /**
     * Returns a new [MetricLogSchedulingWorker].
     *
     * This [MetricLogSchedulingWorker] implements the [ListenableWorker] for facilitating metric
     * log scheduling.
     */
    fun create(context: Context, params: WorkerParameters): ListenableWorker {
      return MetricLogSchedulingWorker(
        context,
        params,
        consoleLogger,
        performanceMetricsLogger,
        backgroundDispatcher
      )
    }
  }
}
