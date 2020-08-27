package org.oppia.domain.oppialogger.loguploader

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import org.oppia.domain.oppialogger.ApplicationStartupListener
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val OPPIA_EXCEPTION_WORK = "OPPIA_EXCEPTION_WORK_REQUEST"
private const val OPPIA_EVENT_WORK = "OPPIA_EVENT_WORK_REQUEST"

class LogUploadWorkRequest @Inject constructor(
  private val context: Context
) : ApplicationStartupListener {

  private val workerConstraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .setRequiresBatteryNotLow(true)
    .setRequiresStorageNotLow(true)
    .build()

  override fun onCreate() {
    val workManager = WorkManager.getInstance(context)
    setWorkerRequestForEvents(workManager)
    setWorkerRequestForExceptions(workManager)
  }

  private fun setWorkerRequestForEvents(workManager: WorkManager) {
    val workerCase =
      Data.Builder()
        .putString(
          OppiaLogUploadWorker.WORKER_CASE_KEY,
          OppiaLogUploadWorker.WorkerCase.EVENT_WORKER.toString()
        )
        .build()
    val eventWorkRequest =
      PeriodicWorkRequest
        .Builder(OppiaLogUploadWorker::class.java, 6, TimeUnit.HOURS)
        .setInputData(workerCase)
        .setConstraints(workerConstraints)
        .build()

    workManager.enqueueUniquePeriodicWork(
      OPPIA_EVENT_WORK,
      ExistingPeriodicWorkPolicy.KEEP,
      eventWorkRequest
    )
  }

  private fun setWorkerRequestForExceptions(workManager: WorkManager) {
    val workerCase =
      Data.Builder()
        .putString(
          OppiaLogUploadWorker.WORKER_CASE_KEY,
          OppiaLogUploadWorker.WorkerCase.EXCEPTION_WORKER.toString()
        )
        .build()
    val exceptionWorkRequest =
      PeriodicWorkRequest
        .Builder(OppiaLogUploadWorker::class.java, 6, TimeUnit.HOURS)
        .setInputData(workerCase)
        .setConstraints(workerConstraints)
        .build()
    workManager.enqueueUniquePeriodicWork(
      OPPIA_EXCEPTION_WORK,
      ExistingPeriodicWorkPolicy.KEEP,
      exceptionWorkRequest
    )
  }
}
