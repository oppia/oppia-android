package org.oppia.domain.oppialogger.loguploader

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import javax.inject.Inject
import javax.inject.Provider

class LogUploadWorkerFactory @Inject constructor(
  private val workerFactories: Map<Class<out Worker>,
    @JvmSuppressWildcards Provider<LogUploadChildWorkerFactory>>
) : WorkerFactory() {

  override fun createWorker(
    appContext: Context,
    workerClassName: String,
    workerParameters: WorkerParameters
  ): ListenableWorker? {
    val workerFactoryProvider = workerFactories[OppiaLogUploadWorker::class.java]
    return workerFactoryProvider?.get()?.create(appContext, workerParameters)
  }
}
