package org.oppia.domain.oppialogger

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.oppia.domain.oppialogger.analytics.AnalyticsController
import org.oppia.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.domain.oppialogger.exceptions.toException
import org.oppia.util.data.DataProviders
import org.oppia.util.logging.firebase.FirebaseEventLogger
import org.oppia.util.logging.firebase.FirebaseExceptionLogger
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val OPPIA_EXCEPTION_WORK = "OPPIA_EXCEPTION_WORK_REQUEST"
private const val OPPIA_EVENT_WORK = "OPPIA_EVENT_WORK_REQUEST"

@Singleton
class OppiaWorkManager @Inject constructor(
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

  fun setWorkerRequestForEvents() {
    val workManager = WorkManager.getInstance(applicationContext)
    val workerCase =
      Data.Builder()
        .putString(WORKER_CASE_KEY, WorkerCase.EVENT_WORKER.toString())
        .build()
    val eventWorkRequest =
      PeriodicWorkRequest
        .Builder(OppiaWorkManager::class.java, 6, TimeUnit.HOURS)
        .setInputData(workerCase)
        .build()
    workManager.enqueueUniquePeriodicWork(
      OPPIA_EVENT_WORK,
      ExistingPeriodicWorkPolicy.REPLACE,
      eventWorkRequest
    )
  }

  fun setWorkerRequestForException() {
    val workManager = WorkManager.getInstance(applicationContext)
    val workerCase =
      Data.Builder()
        .putString(WORKER_CASE_KEY, WorkerCase.EXCEPTION_WORKER.toString())
        .build()
    val exceptionWorkRequest =
      PeriodicWorkRequest
        .Builder(OppiaWorkManager::class.java, 6, TimeUnit.HOURS)
        .setInputData(workerCase)
        .build()
    workManager.enqueueUniquePeriodicWork(
      OPPIA_EXCEPTION_WORK,
      ExistingPeriodicWorkPolicy.REPLACE,
      exceptionWorkRequest
    )
  }

  private fun exceptionWork(): Result{
    val storeLiveData = dataProviders.convertToLiveData(exceptionsController.getExceptionLogStore())
    val storeSize = storeLiveData.value?.getOrThrow()?.exceptionLogCount?.minus(1)
    for (i in 0..storeSize!!) {
      val exceptionLog = storeLiveData.value?.getOrThrow()?.getExceptionLog(i)
      exceptionLog?.toException()?.let { exceptionLogger.logException(it) }
      storeLiveData.value?.getOrThrow()!!.exceptionLogList.remove(exceptionLog)
    }
    return when(storeSize){
      0 -> Result.success()
      else -> Result.failure()
    }
  }

  private fun eventWork(): Result{
    val storeLiveData = analyticsController.getEventLogs()
    val storeSize = storeLiveData.value?.getOrThrow()?.eventLogCount?.minus(1)
    for (i in 0..storeSize!!) {
      val eventLog = storeLiveData.value?.getOrThrow()?.getEventLog(i)
      eventLog?.let { eventLogger.logEvent(it) }
      storeLiveData.value?.getOrThrow()!!.eventLogList.remove(eventLog)
    }
    return when(storeSize){
      0 -> Result.success()
      else -> Result.failure()
    }
  }
}
