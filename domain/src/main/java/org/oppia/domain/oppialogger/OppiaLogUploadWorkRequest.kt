package org.oppia.domain.oppialogger

import android.app.Application
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

private const val OPPIA_EXCEPTION_WORK = "OPPIA_EXCEPTION_WORK_REQUEST"
private const val OPPIA_EVENT_WORK = "OPPIA_EVENT_WORK_REQUEST"

class OppiaLogUploadWorkRequest() {

  private val workerConstraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .setRequiresBatteryNotLow(true)
    .setRequiresStorageNotLow(true)
    .build()

  fun setWorkerRequestForEvents() {
    val workManager = WorkManager.getInstance(Application().applicationContext)
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

  fun setWorkerRequestForExceptions() {
    val workManager = WorkManager.getInstance(Application().applicationContext)
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
