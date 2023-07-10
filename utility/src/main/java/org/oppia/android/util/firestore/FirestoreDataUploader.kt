package org.oppia.android.util.firestore

import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager

/** Enqueues work requests for uploading stored firestore data to the remote service.  */
interface FirestoreDataUploader {
  /**
   * Enqueues a [workRequest] using the [workManager] for uploading data that is stored offline
   * in Firestore.
   */
  fun enqueueWorkRequestForFirestore(workManager: WorkManager, workRequest: PeriodicWorkRequest)
}