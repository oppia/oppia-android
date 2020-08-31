package org.oppia.domain.oppialogger.loguploader

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.oppia.domain.oppialogger.analytics.AnalyticsController
import org.oppia.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.domain.oppialogger.exceptions.toException
import org.oppia.util.logging.ConsoleLogger
import org.oppia.util.logging.EventLogger
import org.oppia.util.logging.ExceptionLogger
import javax.inject.Inject

/** Worker class that extracts log reports from the cache store and logs them to the remote service. */
class LogUploadWorker private constructor(
  context: Context,
  params: WorkerParameters,
  private val analyticsController: AnalyticsController,
  private val exceptionsController: ExceptionsController,
  private val exceptionLogger: ExceptionLogger,
  private val eventLogger: EventLogger,
  private val consoleLogger: ConsoleLogger
) : CoroutineWorker(context, params) {

  companion object {
    const val WORKER_CASE_KEY = "worker_case_key"
    const val TAG = "LogUploadWorker.tag"
    const val EVENT_WORKER = "event_worker"
    const val EXCEPTION_WORKER = "exception_worker"
  }

  override suspend fun doWork(): Result {
    return coroutineScope {
      when (inputData.getString(WORKER_CASE_KEY)) {
        EVENT_WORKER -> {
          withContext(Dispatchers.Default) { uploadEvents() }
        }
        EXCEPTION_WORKER -> {
          withContext(Dispatchers.Default) { uploadExceptions() }
        }
        else -> Result.failure()
      }
    }
  }

  /** Extracts exception logs from the cache store and logs them to the remote service. */
  private suspend fun uploadExceptions(): Result {
    return try {
      val exceptionLogs =
        exceptionsController.getExceptionLogStore().retrieveData().getOrThrow().exceptionLogList
      exceptionLogs?.let {
        for (exceptionLog in it) {
          exceptionLogger.logException(exceptionLog.toException())
          it.remove(exceptionLog)
        }
      }
      Result.success()
    } catch (e: Exception) {
      consoleLogger.e(TAG, e.toString(), e)
      System.err.println(e)
      Result.failure()
    }
  }

  /** Extracts event logs from the cache store and logs them to the remote service. */
  private suspend fun uploadEvents(): Result {
    return try {
      val eventLogs =
        analyticsController.getEventLogStore().retrieveData().getOrThrow().eventLogList
      eventLogs?.let {
        for (eventLog in it) {
          eventLogger.logEvent(eventLog)
          it.remove(eventLog)
        }
      }
      Result.success()
    } catch (e: Exception) {
      consoleLogger.e(TAG, e.toString(), e)
      Result.failure()
    }
  }

  /** Creates an instance of [LogUploadWorker] by properly injecting dependencies. */
  class FactoryLogUpload @Inject constructor(
    private val analyticsController: AnalyticsController,
    private val exceptionsController: ExceptionsController,
    private val exceptionLogger: ExceptionLogger,
    private val eventLogger: EventLogger,
    private val consoleLogger: ConsoleLogger
  ) : LogUploadChildWorkerFactory {

    override fun create(context: Context, params: WorkerParameters): CoroutineWorker {
      return LogUploadWorker(
        context,
        params,
        analyticsController,
        exceptionsController,
        exceptionLogger,
        eventLogger,
        consoleLogger
      )
    }
  }
}
