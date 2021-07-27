package org.oppia.android.domain.oppialogger.loguploader

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import org.oppia.android.domain.oppialogger.ApplicationStartupListener
import org.oppia.android.util.logging.LogUploader
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.domain.platformparameter.syncup.PlatformParameterSyncUpWorker

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
    startWorker(workManager)
//    logUploader.enqueueWorkRequestForEvents(workManager, workRequestForUploadingEvents)
//    logUploader.enqueueWorkRequestForExceptions(workManager, workRequestForUploadingExceptions)
  }
  fun startWorker(workManager : WorkManager){
    val workRequest = OneTimeWorkRequest.Builder(PlatformParameterSyncUpWorker::class.java)
      .setInputData(workerTypeForSyncingUpParameters)
      .setConstraints(platformParameterSyncUpWorkerConstraints)
      .build()
    workManager.enqueueUniqueWork(
      "PlatformParameterWork",
      ExistingWorkPolicy.REPLACE,
      workRequest
    )
  }

  private val platformParameterSyncUpWorkerConstraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .setRequiresBatteryNotLow(true)
    .build()

  private val workerTypeForSyncingUpParameters: Data = Data.Builder()
    .putString(
      PlatformParameterSyncUpWorker.WORKER_TYPE_KEY,
      PlatformParameterSyncUpWorker.PLATFORM_PARAMETER_WORKER
    )
    .build()

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
