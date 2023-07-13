package org.oppia.android.domain.oppialogger.firestoreuploader

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import javax.inject.Inject

/** Custom [WorkerFactory] for the [FirestoreUploadWorker]. */
class FirestoreUploadWorkerFactory @Inject constructor(
  private val workerFactory: FirestoreUploadWorker.Factory
) : WorkerFactory() {
  override fun createWorker(
    appContext: Context,
    workerClassName: String,
    workerParameters: WorkerParameters
  ): ListenableWorker? {
    return workerFactory.create(appContext, workerParameters)
  }
}
