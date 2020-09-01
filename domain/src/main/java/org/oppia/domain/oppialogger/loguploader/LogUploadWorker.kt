package org.oppia.domain.oppialogger.loguploader

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.oppia.app.model.OppiaEventLogs
import org.oppia.app.model.OppiaExceptionLogs
import org.oppia.domain.oppialogger.analytics.AnalyticsController
import org.oppia.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.domain.oppialogger.exceptions.toException
import org.oppia.util.logging.ConsoleLogger
import org.oppia.util.logging.EventLogger
import org.oppia.util.logging.ExceptionLogger
import org.oppia.util.threading.BackgroundDispatcher
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
  @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher
) : CoroutineWorker(context, params) {

  companion object {
    const val WORKER_CASE_KEY = "worker_case_key"
    const val TAG = "LogUploadWorker.tag"
    const val EVENT_WORKER = "event_worker"
    const val EXCEPTION_WORKER = "exception_worker"
  }

  override suspend fun doWork(): Result {
    return when (inputData.getString(WORKER_CASE_KEY)) {
      EVENT_WORKER -> {
        withContext(backgroundDispatcher) { uploadEvents() }
      }
      EXCEPTION_WORKER -> {
        withContext(backgroundDispatcher) { uploadExceptions() }
      }
      else -> Result.failure()
    }
  }

  /** Extracts exception logs from the cache store and logs them to the remote service. */
  private suspend fun uploadExceptions(): Result {
    return try {
      val exceptionLogs =
        exceptionsController.getExceptionLogStore().retrieveData()
          .getOrDefault(OppiaExceptionLogs.getDefaultInstance()).exceptionLogList
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
        analyticsController.getEventLogStore().retrieveData()
          .getOrDefault(OppiaEventLogs.getDefaultInstance()).eventLogList
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
    private val consoleLogger: ConsoleLogger,
    @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher
  ) : LogUploadChildWorkerFactory {

    override fun create(context: Context, params: WorkerParameters): CoroutineWorker {
      return LogUploadWorker(
        context,
        params,
        analyticsController,
        exceptionsController,
        exceptionLogger,
        eventLogger,
        consoleLogger,
        backgroundDispatcher
      )
    }
  }
}
