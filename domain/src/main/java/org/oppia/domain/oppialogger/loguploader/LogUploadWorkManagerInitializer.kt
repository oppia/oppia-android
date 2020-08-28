package org.oppia.domain.oppialogger.loguploader

import android.content.Context
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import org.oppia.domain.oppialogger.ApplicationStartupListener
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val OPPIA_EXCEPTION_WORK = "OPPIA_EXCEPTION_WORK_REQUEST"
private const val OPPIA_EVENT_WORK = "OPPIA_EVENT_WORK_REQUEST"

/** Enqueues unique periodic work requests for uploading events and exceptions to the remote service on application creation. */
@Singleton
class LogUploadWorkManagerInitializer @Inject constructor(
  private val context: Context,
  private val logUploadWorkerFactory: LogUploadWorkerFactory
) : ApplicationStartupListener, Configuration.Provider {

  private val logUploadWorkerConstraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .setRequiresBatteryNotLow(true)
    .build()

  override fun onCreate() {
    WorkManager.initialize(context, workManagerConfiguration)
    val workManager = WorkManager.getInstance(context)
    enqueueWorkRequestForEvents(workManager)
    enqueueWorkRequestForExceptions(workManager)
  }

  /** Enqueues a unique periodic work request for uploading events to the remote service. */
  private fun enqueueWorkRequestForEvents(workManager: WorkManager) {
    val workerCase =
      Data.Builder()
        .putString(
          LogUploadWorker.WORKER_CASE_KEY,
          LogUploadWorker.WorkerCase.EVENT_WORKER.toString()
        )
        .build()
    val eventWorkRequest =
      PeriodicWorkRequest
        .Builder(LogUploadWorker::class.java, 6, TimeUnit.HOURS)
        .setInputData(workerCase)
        .setConstraints(logUploadWorkerConstraints)
        .build()
    workManager.enqueueUniquePeriodicWork(
      OPPIA_EVENT_WORK,
      ExistingPeriodicWorkPolicy.KEEP,
      eventWorkRequest
    )
  }

  /** Enqueues a unique periodic work request for uploading exceptions to the remote service. */
  private fun enqueueWorkRequestForExceptions(workManager: WorkManager) {
    val workerCase =
      Data.Builder()
        .putString(
          LogUploadWorker.WORKER_CASE_KEY,
          LogUploadWorker.WorkerCase.EXCEPTION_WORKER.toString()
        )
        .build()
    val exceptionWorkRequest =
      PeriodicWorkRequest
        .Builder(LogUploadWorker::class.java, 6, TimeUnit.HOURS)
        .setInputData(workerCase)
        .setConstraints(logUploadWorkerConstraints)
        .build()
    workManager.enqueueUniquePeriodicWork(
      OPPIA_EXCEPTION_WORK,
      ExistingPeriodicWorkPolicy.KEEP,
      exceptionWorkRequest
    )
  }

  /** Returns the worker constraints set for the log uploading work requests. */
  fun getLogUploadWorkerConstraints(): Constraints = logUploadWorkerConstraints

  /** [Configuration] that sets the worker factory for the work manager to [LogUploadWorkerFactory]. */
  override fun getWorkManagerConfiguration(): Configuration {
    return Configuration.Builder()
      .setWorkerFactory(logUploadWorkerFactory)
      .build()
  }
}
