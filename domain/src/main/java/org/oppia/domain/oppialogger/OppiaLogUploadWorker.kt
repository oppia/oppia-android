package org.oppia.domain.oppialogger

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
import javax.inject.Singleton

const val LOG_UPLOAD_WORKER = "log_upload_worker"

@Singleton
class OppiaLogUploadWorker @Inject constructor(
  context: Context,
  params: WorkerParameters,
  private val analyticsController: AnalyticsController,
  private val exceptionsController: ExceptionsController,
  private val dataProviders: DataProviders,
  private val exceptionLogger: ExceptionLogger,
  private val eventLogger: EventLogger,
  private val consoleLogger: ConsoleLogger
) : Worker(context, params) {

  enum class WorkerCase {
    EVENT_WORKER,
    EXCEPTION_WORKER
  }

  companion object {
    const val WORKER_CASE_KEY = "worker_case_key"
  }

  override fun doWork(): Result {
    return when (inputData.getString(WORKER_CASE_KEY)) {
      WorkerCase.EVENT_WORKER.toString() -> {
        eventWork()
      }
      WorkerCase.EXCEPTION_WORKER.toString() -> {
        exceptionWork()
      }
      else -> Result.failure()
    }
  }

  private fun exceptionWork(): Result {
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
      consoleLogger.e(LOG_UPLOAD_WORKER, e.toString(), e)
      Result.failure()
    }
  }

  private fun eventWork(): Result {
    return try {
      val eventStore = analyticsController.getEventLogs()
      val eventLogs = eventStore.value?.getOrThrow()?.eventLogList
      eventLogs?.let {
        for (eventLog in it) {
          eventLogger.logEvent(eventLog)
          it.remove(eventLog)
        }
      }
      Result.success()
    } catch (e: Exception) {
      System.err.println(e)
      consoleLogger.e(LOG_UPLOAD_WORKER, e.toString(), e)
      Result.failure()
    }
  }

  class Factory @Inject constructor(
    private val analyticsController: AnalyticsController,
    private val exceptionsController: ExceptionsController,
    private val dataProviders: DataProviders,
    private val exceptionLogger: ExceptionLogger,
    private val eventLogger: EventLogger,
    private val consoleLogger: ConsoleLogger
  ) : ChildWorkerFactory {

    override fun create(context: Context, params: WorkerParameters): Worker {
      return OppiaLogUploadWorker(
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
