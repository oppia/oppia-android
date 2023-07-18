package org.oppia.android.domain.oppialogger.firestoreuploader

import androidx.annotation.VisibleForTesting
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import org.oppia.android.domain.oppialogger.analytics.AnalyticsStartupListener
import org.oppia.android.domain.oppialogger.firestoreuploader.FirestoreUploadWorker.Companion.FIRESTORE_WORKER
import org.oppia.android.domain.oppialogger.firestoreuploader.FirestoreUploadWorker.Companion.WORKER_CASE_KEY
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Enqueues unique periodic work requests for uploading events to Firestore on application creation.
 */
class FirestoreUploadWorkManagerInitializer @Inject constructor() : AnalyticsStartupListener {
  private val OPPIA_FIRESTORE_WORK = "OPPIA_FIRESTORE_WORK_REQUEST"

  private val firestoreUploadWorkerConstraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .setRequiresBatteryNotLow(true)
    .build()

  private val workerCaseForUploadingFirestoreData: Data = Data.Builder()
    .putString(
      WORKER_CASE_KEY,
      FIRESTORE_WORKER
    )
    .build()

  private val workRequestForUploadingFireStoreData: PeriodicWorkRequest =
    PeriodicWorkRequest.Builder(FirestoreUploadWorker::class.java, 6, TimeUnit.HOURS)
      .setInputData(workerCaseForUploadingFirestoreData)
      .setConstraints(firestoreUploadWorkerConstraints)
      .build()

  override fun onCreate(workManager: WorkManager) {
    workManager.enqueueUniquePeriodicWork(
      OPPIA_FIRESTORE_WORK,
      ExistingPeriodicWorkPolicy.KEEP,
      workRequestForUploadingFireStoreData
    )
  }

  /**
   * Returns the [Data] that goes into the work request that is enqueued for uploading firestore
   * data.
   */
  @VisibleForTesting
  fun getWorkRequestDataForFirestore(): Data = workerCaseForUploadingFirestoreData

  /** Returns the [UUID] of the work request that is enqueued for uploading firestore data. */
  @VisibleForTesting
  fun getWorkRequestForFirestoreId(): UUID = workRequestForUploadingFireStoreData.id
}
