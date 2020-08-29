package org.oppia.domain.oppialogger.loguploader

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.oppia.domain.oppialogger.analytics.AnalyticsController
import org.oppia.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.domain.oppialogger.exceptions.toException
import org.oppia.util.data.DataProviders
import org.oppia.util.logging.ConsoleLogger
import org.oppia.util.logging.EventLogger
import org.oppia.util.logging.ExceptionLogger
import javax.inject.Inject

/** [Worker] class that extracts log reports from the cache store and logs them to the remote service. */
class LogUploadWorker @Inject constructor(
  context: Context,
  params: WorkerParameters,
  private val analyticsController: AnalyticsController,
  private val exceptionsController: ExceptionsController,
  private val dataProviders: DataProviders,
  private val exceptionLogger: ExceptionLogger,
  private val eventLogger: EventLogger,
  private val consoleLogger: ConsoleLogger
) : Worker(context, params) {

  companion object {
    const val WORKER_CASE_KEY = "worker_case_key"
    const val TAG = "LogUploadWorker.tag"
    const val EVENT_WORKER = "event_worker"
    const val EXCEPTION_WORKER = "exception_worker"
  }

  override fun doWork(): Result {
    return when (inputData.getString(WORKER_CASE_KEY)) {
      EVENT_WORKER -> {
        uploadEvents()
      }
      EXCEPTION_WORKER -> {
        uploadExceptions()
      }
      else -> Result.failure()
    }
  }

  /** Extracts exception logs from the cache store and logs them to the remote service. */
  private fun uploadExceptions(): Result {
    return try {
      val exceptionStore =
        dataProviders.convertToLiveData(exceptionsController.getExceptionLogStore())
      val exceptionLogs = exceptionStore.value?.getOrThrow()?.exceptionLogList
      exceptionLogs?.let {
        for (exceptionLog in it) {
          exceptionLogger.logException(exceptionLog.toException())
          it.remove(exceptionLog)
        }
      }
      Result.success()
    } catch (e: Exception) {
      consoleLogger.e(TAG, e.toString(), e)
      Result.failure()
    }
  }

  /** Extracts event logs from the cache store and logs them to the remote service. */
  private fun uploadEvents(): Result {
    return try {
      val eventStore = dataProviders.convertToLiveData(analyticsController.getEventLogStore())
      val eventLogs = eventStore.value?.getOrThrow()?.eventLogList
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
    private val dataProviders: DataProviders,
    private val exceptionLogger: ExceptionLogger,
    private val eventLogger: EventLogger,
    private val consoleLogger: ConsoleLogger
  ) : LogUploadChildWorkerFactory {

    override fun create(context: Context, params: WorkerParameters): Worker {
      return LogUploadWorker(
        context,
        params,
        analyticsController,
        exceptionsController,
        dataProviders,
        exceptionLogger,
        eventLogger,
        consoleLogger
      )
    }
  }
}
