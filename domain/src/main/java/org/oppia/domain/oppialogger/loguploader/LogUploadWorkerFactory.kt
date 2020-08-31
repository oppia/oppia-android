package org.oppia.domain.oppialogger.loguploader

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import javax.inject.Inject

/** Custom [WorkerFactory] for the [LogUploadWorker]. */
class LogUploadWorkerFactory @Inject constructor(
  private val workerFactory: LogUploadChildWorkerFactory
) : WorkerFactory() {

  /** Returns a [LogUploadWorker] after injecting [appContext] and [workerParameters] into [LogUploadChildWorkerFactory]. */
  override fun createWorker(
    appContext: Context,
    workerClassName: String,
    workerParameters: WorkerParameters
  ): ListenableWorker? {
    return workerFactory.create(appContext, workerParameters)
  }
}
