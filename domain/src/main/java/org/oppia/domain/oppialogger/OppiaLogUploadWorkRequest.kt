package org.oppia.domain.oppialogger

import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

private const val OPPIA_EXCEPTION_WORK = "OPPIA_EXCEPTION_WORK_REQUEST"
private const val OPPIA_EVENT_WORK = "OPPIA_EVENT_WORK_REQUEST"

class OppiaLogUploadWorkRequest {
/*
  private val workerConstraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .setRequiresBatteryNotLow(true)
    .setRequiresStorageNotLow(true)
    .build()*/

  fun setWorkerRequestForEvents(workManager: WorkManager): OneTimeWorkRequest {
    val workerCase =
      Data.Builder()
        .putString(
          OppiaLogUploadWorker.WORKER_CASE_KEY,
          OppiaLogUploadWorker.WorkerCase.EVENT_WORKER.toString()
        )
        .build()
    val eventWorkRequest =
      OneTimeWorkRequest
        .Builder(OppiaLogUploadWorker::class.java)//, 6, TimeUnit.HOURS)
        .setInputData(workerCase)
        .build()
        //.setConstraints(workerConstraints)

    workManager.enqueueUniqueWork(
      OPPIA_EVENT_WORK,
      ExistingWorkPolicy.KEEP,
      eventWorkRequest
    )
    return eventWorkRequest
  }

  fun setWorkerRequestForExceptions(workManager: WorkManager) {
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
        .build()
        //.setConstraints(workerConstraints)
    workManager.enqueueUniquePeriodicWork(
      OPPIA_EXCEPTION_WORK,
      ExistingPeriodicWorkPolicy.KEEP,
      exceptionWorkRequest
    )
  }
}
