package org.oppia.android.util.firestore

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import javax.inject.Inject

private const val OPPIA_FIRESTORE_WORK = "OPPIA_FIRESTORE_WORK_REQUEST"

/** Uploader for uploading survey-related data to Firestore. */
class SurveyFirestoreDataUploader @Inject constructor() : FirestoreDataUploader {
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