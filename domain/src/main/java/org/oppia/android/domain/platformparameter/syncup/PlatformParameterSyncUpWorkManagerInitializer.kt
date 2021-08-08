package org.oppia.android.domain.platformparameter.syncup

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import org.oppia.android.domain.oppialogger.ApplicationStartupListener
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.platformparameter.SyncUpWorkerTimePeriod
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enqueues unique periodic work requests for fetching and caching latest platform parameter values
 * from the remote service on application creation.
 */
@Singleton
class PlatformParameterSyncUpWorkManagerInitializer @Inject constructor(
  private val context: Context,
  @SyncUpWorkerTimePeriod private val workRequestRepeatInterval: PlatformParameterValue<Int>
) : ApplicationStartupListener {

  private val OPPIA_PLATFORM_PARAMETER_WORK_REQUEST_NAME = "OPPIA_PLATFORM_PARAMETER_WORK_REQUEST"

  private val platformParameterSyncUpWorkerConstraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .setRequiresBatteryNotLow(true)
    .build()

  private val workerTypeForSyncingPlatformParameters: Data = Data.Builder()
    .putString(
      PlatformParameterSyncUpWorker.WORKER_TYPE_KEY,
      PlatformParameterSyncUpWorker.PLATFORM_PARAMETER_WORKER
    )
    .build()

  private val workRequestForSyncingPlatformParameters = PeriodicWorkRequest
    .Builder(
      PlatformParameterSyncUpWorker::class.java,
      workRequestRepeatInterval.value.toLong(),
      TimeUnit.HOURS
    )
    .addTag(PlatformParameterSyncUpWorker.TAG)
    .setInputData(workerTypeForSyncingPlatformParameters)
    .setConstraints(platformParameterSyncUpWorkerConstraints)
    .build()

  override fun onCreate() {
    val workManager = WorkManager.getInstance(context)
    workManager.enqueueUniquePeriodicWork(
      OPPIA_PLATFORM_PARAMETER_WORK_REQUEST_NAME,
      ExistingPeriodicWorkPolicy.KEEP,
      workRequestForSyncingPlatformParameters
    )
  }

  /** Returns the Worker [Constraints] set for the platform parameter sync-up work requests. */
  fun getSyncUpWorkerConstraints(): Constraints = platformParameterSyncUpWorkerConstraints

  /** Returns the [UUID] of the work request that is enqueued to sync-up platform parameters. */
  fun getSyncUpWorkRequestId(): UUID = workRequestForSyncingPlatformParameters.id

  /** Returns the [Data] that goes into the work request enqueued to sync-up platform parameters. */
  fun getSyncUpWorkRequestData(): Data = workerTypeForSyncingPlatformParameters

  @SuppressLint("RestrictedApi")
  fun getSyncUpWorkerTimePeriod(): Long = workRequestForSyncingPlatformParameters
    .workSpec.intervalDuration
}
