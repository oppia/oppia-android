package org.oppia.domain.oppialogger.loguploader

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import org.oppia.domain.oppialogger.ApplicationStartupListener
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Enqueues unique periodic work requests for uploading events and exceptions to the remote service on application creation. */
@Singleton
class LogUploadWorkManagerInitializer @Inject constructor(
  private val context: Context,
  private val logUploader: LogUploader
) : ApplicationStartupListener {

  private val logUploadWorkerConstraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .setRequiresBatteryNotLow(true)
    .build()

  private val workerCaseForUploadingEvents: Data = Data.Builder()
    .putString(
      LogUploadWorker.WORKER_CASE_KEY,
      LogUploadWorker.EVENT_WORKER
    )
    .build()

  private val workerCaseForUploadingExceptions: Data = Data.Builder()
    .putString(
      LogUploadWorker.WORKER_CASE_KEY,
      LogUploadWorker.EXCEPTION_WORKER
    )
    .build()

  private val workRequestForUploadingEvents: PeriodicWorkRequest = PeriodicWorkRequest
    .Builder(LogUploadWorker::class.java, 6, TimeUnit.HOURS)
    .setInputData(workerCaseForUploadingEvents)
    .setConstraints(logUploadWorkerConstraints)
    .build()

  private val workRequestForUploadingExceptions: PeriodicWorkRequest = PeriodicWorkRequest
    .Builder(LogUploadWorker::class.java, 6, TimeUnit.HOURS)
    .setInputData(workerCaseForUploadingExceptions)
    .setConstraints(logUploadWorkerConstraints)
    .build()

  override fun onCreate() {
    val workManager = WorkManager.getInstance(context)
    logUploader.enqueueWorkRequestForEvents(workManager, workRequestForUploadingEvents)
    logUploader.enqueueWorkRequestForExceptions(workManager, workRequestForUploadingExceptions)
  }

  /** Returns the worker constraints set for the log uploading work requests. */
  fun getLogUploadWorkerConstraints(): Constraints = logUploadWorkerConstraints

  /** Returns the [UUID] of the work request that is enqueued for uploading event logs. */
  fun getWorkRequestForEventsId(): UUID = workRequestForUploadingEvents.id

  /** Returns the [UUID] of the work request that is enqueued for uploading exception logs. */
  fun getWorkRequestForExceptionsId(): UUID = workRequestForUploadingExceptions.id

  /** Returns the [Data] that goes into the work request that is enqueued for uploading event logs. */
  fun getWorkRequestDataForEvents(): Data = workerCaseForUploadingEvents

  /** Returns the [Data] that goes into the work request that is enqueued for uploading exception logs. */
  fun getWorkRequestDataForExceptions(): Data = workerCaseForUploadingExceptions
}
