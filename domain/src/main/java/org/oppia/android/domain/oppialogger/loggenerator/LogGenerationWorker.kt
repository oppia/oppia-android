package org.oppia.android.domain.oppialogger.loggenerator

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import org.oppia.android.domain.util.getStringFromData
import org.oppia.android.util.threading.BackgroundDispatcher
import javax.inject.Inject

/**
 * Worker class that generates metric log reports regarding the performance of the application
 * and then stores it in in device cache.
 */
class LogGenerationWorker private constructor(
  context: Context,
  params: WorkerParameters,
  @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher
) : ListenableWorker(context, params) {

  companion object {
    const val WORKER_CASE_KEY = "worker_case_key"
    const val TAG = "LogGenerationWorker.tag"
    const val PERIODIC_METRIC_WORKER = "periodic_metric_worker"
    const val STORAGE_USAGE_WORKER = "storage_usage_worker"
    const val MEMORY_USAGE_WORKER = "memory_usage_worker"
  }

  override fun startWork(): ListenableFuture<Result> {
    val backgroundScope = CoroutineScope(backgroundDispatcher)
    val result = backgroundScope.async {
      when (inputData.getStringFromData(WORKER_CASE_KEY)) {
        PERIODIC_METRIC_WORKER -> generatePeriodicMetricLog()
        STORAGE_USAGE_WORKER -> generateStorageUsageMetricLog()
        MEMORY_USAGE_WORKER -> generateMemoryUsageMetricLog()
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

  private fun generatePeriodicMetricLog(): Result {
    // TODO(#4334): Add functionality to generate metric log reports for periodic events.
    return Result.failure()
  }

  private fun generateStorageUsageMetricLog(): Result {
    // TODO(#4334): Add functionality to generate metric log reports for storage usage.
    return Result.failure()
  }

  private fun generateMemoryUsageMetricLog(): Result {
    // TODO(#4334): Add functionality to generate metric log reports for memory usage.
    return Result.failure()
  }

  /** Creates an instance of [LogGenerationWorker] by properly injecting dependencies. */
  class Factory @Inject constructor(
    @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher
  ) {
    fun create(context: Context, params: WorkerParameters): ListenableWorker {
      return LogGenerationWorker(context, params, backgroundDispatcher)
    }
  }
}
