package org.oppia.domain.oppialogger.loguploader

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import javax.inject.Inject
import javax.inject.Provider

/** Custom [WorkerFactory] for the [LogUploadWorker]. */
class LogUploadWorkerFactory @Inject constructor(
  private val workerFactories: Map<Class<out Worker>,
    @JvmSuppressWildcards Provider<LogUploadChildWorkerFactory>>
) : WorkerFactory() {

  /** Returns a [LogUploadWorker] after injecting [appContext] and [workerParameters] into [LogUploadChildWorkerFactory]. */
  override fun createWorker(
    appContext: Context,
    workerClassName: String,
    workerParameters: WorkerParameters
  ): ListenableWorker? {
    val workerFactoryProvider = workerFactories[LogUploadWorker::class.java]
    return workerFactoryProvider?.get()?.create(appContext, workerParameters)
  }
}
