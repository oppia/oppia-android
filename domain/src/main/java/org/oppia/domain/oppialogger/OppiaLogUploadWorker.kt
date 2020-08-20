package org.oppia.domain.oppialogger

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.oppia.domain.oppialogger.analytics.AnalyticsController
import org.oppia.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.domain.oppialogger.exceptions.toException
import org.oppia.util.data.DataProviders
import org.oppia.util.logging.firebase.FirebaseEventLogger
import org.oppia.util.logging.firebase.FirebaseExceptionLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OppiaLogUploadWorker @Inject constructor(
  context: Context,
  params: WorkerParameters,
  private val analyticsController: AnalyticsController,
  private val exceptionsController: ExceptionsController,
  private val dataProviders: DataProviders,
  private val exceptionLogger: FirebaseExceptionLogger,
  private val eventLogger: FirebaseEventLogger
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
        exceptionWork()
      }
      WorkerCase.EXCEPTION_WORKER.toString() -> {
        eventWork()
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
      e.printStackTrace()
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
      e.printStackTrace()
      Result.failure()
    }
  }
}
