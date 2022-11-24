package org.oppia.android.domain.platformparameter.syncup

import android.annotation.SuppressLint
import androidx.annotation.VisibleForTesting
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import org.oppia.android.domain.oppialogger.analytics.AnalyticsStartupListener
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.platformparameter.SyncUpWorkerTimePeriodHours
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Enqueues unique periodic work requests for fetching and caching latest platform parameter values
 * from the remote service on application creation.
 */
class PlatformParameterSyncUpWorkManagerInitializer @Inject constructor(
  @SyncUpWorkerTimePeriodHours private val workRequestRepeatInterval: PlatformParameterValue<Int>
) : AnalyticsStartupListener {

  private val OPPIA_PLATFORM_PARAMETER_WORK_REQUEST_NAME = "OPPIA_PLATFORM_PARAMETER_WORK_REQUEST"

  /** [Constraints] for platform parameter sync up work request. */
  private val platformParameterSyncUpWorkerConstraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .setRequiresBatteryNotLow(true)
    .build()

  /** [Data] for platform parameter sync up work request. */
  private val workerTypeForSyncingPlatformParameters: Data = Data.Builder()
    .putString(
      PlatformParameterSyncUpWorker.WORKER_TYPE_KEY,
      PlatformParameterSyncUpWorker.PLATFORM_PARAMETER_WORKER
    )
    .build()

  /** [PeriodicWorkRequest] for platform parameter sync up worker. */
  private val workRequestForSyncingPlatformParameters =
    PeriodicWorkRequest.Builder(
      PlatformParameterSyncUpWorker::class.java,
      workRequestRepeatInterval.value.toLong(),
      TimeUnit.HOURS
    )
      .addTag(PlatformParameterSyncUpWorker.TAG)
      .setInputData(workerTypeForSyncingPlatformParameters)
      .setConstraints(platformParameterSyncUpWorkerConstraints)
      .build()

  override fun onCreate(workManager: WorkManager) {
    workManager.enqueueUniquePeriodicWork(
      OPPIA_PLATFORM_PARAMETER_WORK_REQUEST_NAME,
      ExistingPeriodicWorkPolicy.KEEP,
      workRequestForSyncingPlatformParameters
    )
  }

  /** Returns the [UUID] of the work request that is enqueued to sync-up platform parameters. */
  @VisibleForTesting
  fun getSyncUpWorkRequestId(): UUID {
    return workRequestForSyncingPlatformParameters.id
  }

  /** Returns the [Data] that goes into the work request enqueued to sync-up platform parameters. */
  @VisibleForTesting
  fun getSyncUpWorkRequestData(): Data {
    return workerTypeForSyncingPlatformParameters
  }

  /** Returns the time interval of periodic work request enqueued to sync-up platform parameters. */
  @SuppressLint("RestrictedApi") // getWorkSpec is restricted; suppression is fine for tests.
  @VisibleForTesting
  fun getSyncUpWorkerTimePeriod(): Long {
    return workRequestForSyncingPlatformParameters.workSpec.intervalDuration
  }

  /** Returns the Worker [Constraints] set for the platform parameter sync-up work requests. */
  fun getSyncUpWorkerConstraints(): Constraints {
    return platformParameterSyncUpWorkerConstraints
  }
}
