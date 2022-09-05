package org.oppia.android.domain.oppialogger.loguploader

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import org.oppia.android.domain.oppialogger.analytics.AnalyticsController
import org.oppia.android.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.android.domain.oppialogger.exceptions.toException
import org.oppia.android.domain.util.getStringFromData
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.logging.EventLogger
import org.oppia.android.util.logging.ExceptionLogger
import org.oppia.android.util.logging.SyncStatusManager
import org.oppia.android.util.threading.BackgroundDispatcher
import javax.inject.Inject

/** Worker class that extracts log reports from the cache store and logs them to the remote service. */
class LogUploadWorker private constructor(
  context: Context,
  params: WorkerParameters,
  private val analyticsController: AnalyticsController,
  private val exceptionsController: ExceptionsController,
  private val exceptionLogger: ExceptionLogger,
  private val eventLogger: EventLogger,
  private val consoleLogger: ConsoleLogger,
  private val syncStatusManager: SyncStatusManager,
  @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher
) : ListenableWorker(context, params) {

  companion object {
    const val WORKER_CASE_KEY = "worker_case_key"
    const val TAG = "LogUploadWorker.tag"
    const val EVENT_WORKER = "event_worker"
    const val EXCEPTION_WORKER = "exception_worker"
  }

  @ExperimentalCoroutinesApi
  override fun startWork(): ListenableFuture<Result> {
    val backgroundScope = CoroutineScope(backgroundDispatcher)
    val result = backgroundScope.async {
      when (inputData.getStringFromData(WORKER_CASE_KEY)) {
        EVENT_WORKER -> uploadEvents()
        EXCEPTION_WORKER -> uploadExceptions()
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

  /** Extracts exception logs from the cache store and logs them to the remote service. */
  private suspend fun uploadExceptions(): Result {
    return try {
      val exceptionLogs = exceptionsController.getExceptionLogStoreList()
      exceptionLogs.let {
        for (exceptionLog in it) {
          exceptionLogger.logException(exceptionLog.toException())
          exceptionsController.removeFirstExceptionLogFromStore()
        }
      }
      Result.success()
    } catch (e: Exception) {
      consoleLogger.e(TAG, e.toString(), e)
      Result.failure()
    }
  }

  /** Extracts event logs from the cache store and logs them to the remote service. */
  private suspend fun uploadEvents(): Result {
    return try {
      syncStatusManager.setSyncStatus(SyncStatusManager.SyncStatus.DATA_UPLOADING)
      analyticsController.getEventLogStoreList().forEach { eventLog ->
        eventLogger.logEvent(eventLog)
        analyticsController.removeFirstEventLogFromStore()
      }
      syncStatusManager.setSyncStatus(SyncStatusManager.SyncStatus.DATA_UPLOADED)
      Result.success()
    } catch (e: Exception) {
      syncStatusManager.setSyncStatus(SyncStatusManager.SyncStatus.NETWORK_ERROR)
      consoleLogger.e(TAG, "Failed to upload events", e)
      Result.failure()
    }
  }

  /** Creates an instance of [LogUploadWorker] by properly injecting dependencies. */
  class Factory @Inject constructor(
    private val analyticsController: AnalyticsController,
    private val exceptionsController: ExceptionsController,
    private val exceptionLogger: ExceptionLogger,
    private val eventLogger: EventLogger,
    private val consoleLogger: ConsoleLogger,
    private val syncStatusManager: SyncStatusManager,
    @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher
  ) {

    fun create(context: Context, params: WorkerParameters): ListenableWorker {
      return LogUploadWorker(
        context,
        params,
        analyticsController,
        exceptionsController,
        exceptionLogger,
        eventLogger,
        consoleLogger,
        syncStatusManager,
        backgroundDispatcher
      )
    }
  }
}
