package org.oppia.util.logging

import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager

/** Uploader for uploading events and exceptions to the remote service. */
interface LogUploader {

  /** Enqueues a [workRequest] using the [workManager] for uploading event logs that are stored in the cache store. */
  fun enqueueWorkRequestForEvents(workManager: WorkManager, workRequest: PeriodicWorkRequest)

  /** Enqueues a [workRequest] using the [workManager] for uploading exception logs that are stored in the cache store. */
  fun enqueueWorkRequestForExceptions(workManager: WorkManager, workRequest: PeriodicWorkRequest)
}
