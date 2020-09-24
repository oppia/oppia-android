package org.oppia.domain.oppialogger.loguploader

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import javax.inject.Inject

/** Custom [WorkerFactory] for the [LogUploadWorker]. */
class LogUploadWorkerFactory @Inject constructor(
  private val workerFactory: LogUploadWorker.Factory
) : WorkerFactory() {

  /** Returns a new [LogUploadWorker] for the given context and parameters. */
  override fun createWorker(
    appContext: Context,
    workerClassName: String,
    workerParameters: WorkerParameters
  ): ListenableWorker? {
    return workerFactory.create(appContext, workerParameters)
  }
}
