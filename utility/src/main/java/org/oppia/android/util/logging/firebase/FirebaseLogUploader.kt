package org.oppia.android.util.logging.firebase

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import org.oppia.android.util.logging.LogUploader
import javax.inject.Inject

private const val OPPIA_EVENT_WORK = "OPPIA_EVENT_WORK_REQUEST"
private const val OPPIA_EXCEPTION_WORK = "OPPIA_EXCEPTION_WORK_REQUEST"
private const val OPPIA_PERFORMANCE_METRICS_WORK = "OPPIA_PERFORMANCE_METRICS_WORK"
private const val OPPIA_FIRESTORE_WORK = "OPPIA_FIRESTORE_WORK_REQUEST"

/** Enqueues work requests for uploading stored event/exception logs to the remote service. */
class FirebaseLogUploader @Inject constructor() :
  LogUploader {

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

  override fun enqueueWorkRequestForPerformanceMetrics(
    workManager: WorkManager,
    workRequest: PeriodicWorkRequest
  ) {
    workManager.enqueueUniquePeriodicWork(
      OPPIA_PERFORMANCE_METRICS_WORK,
      ExistingPeriodicWorkPolicy.KEEP,
      workRequest
    )
  }

  override fun enqueueWorkRequestForFirestore(
    workManager: WorkManager,
    workRequest: PeriodicWorkRequest
  ) {
    workManager.enqueueUniquePeriodicWork(
      OPPIA_FIRESTORE_WORK,
      ExistingPeriodicWorkPolicy.KEEP,
      workRequest
    )
  }
}
