package org.oppia.domain.oppialogger.loguploader

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager

private const val OPPIA_EVENT_WORK = "OPPIA_EVENT_WORK_REQUEST"
private const val OPPIA_EXCEPTION_WORK = "OPPIA_EXCEPTION_WORK_REQUEST"

/** Enqueues work requests for uploading stored event/exception logs to the remote service. */
class FirebaseLogUploader : LogUploader {

  /** Enqueues a unique periodic work request for uploading exceptions to the remote service. */
  override fun enqueueWorkRequestForEvents(
    workManager: WorkManager,
    workRequest: PeriodicWorkRequest
  ) {
    workManager.enqueueUniquePeriodicWork(
      OPPIA_EVENT_WORK,
      ExistingPeriodicWorkPolicy.KEEP,
      workRequest
    )
  }

  /** Enqueues a unique periodic work request for uploading exceptions to the remote service. */
  override fun enqueueWorkRequestForExceptions(
    workManager: WorkManager,
    workRequest: PeriodicWorkRequest
  ) {
    workManager.enqueueUniquePeriodicWork(
      OPPIA_EXCEPTION_WORK,
      ExistingPeriodicWorkPolicy.KEEP,
      workRequest
    )
  }
}
