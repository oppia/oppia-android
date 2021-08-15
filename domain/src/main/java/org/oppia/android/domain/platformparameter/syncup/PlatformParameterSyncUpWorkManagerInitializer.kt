package org.oppia.android.domain.platformparameter.syncup

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
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Enqueues unique periodic work requests for fetching and caching latest platform parameter values
 * from the remote service on application creation.
 */
class PlatformParameterSyncUpWorkManagerInitializer @Inject constructor(
  private val context: Context,
  @SyncUpWorkerTimePeriod private val workRequestRepeatInterval: PlatformParameterValue<Int>
) : ApplicationStartupListener {

  private val OPPIA_PLATFORM_PARAMETER_WORK_REQUEST_NAME = "OPPIA_PLATFORM_PARAMETER_WORK_REQUEST"

  val platformParameterSyncUpWorkerConstraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .setRequiresBatteryNotLow(true)
    .build()

  val workerTypeForSyncingPlatformParameters: Data = Data.Builder()
    .putString(
      PlatformParameterSyncUpWorker.WORKER_TYPE_KEY,
      PlatformParameterSyncUpWorker.PLATFORM_PARAMETER_WORKER
    )
    .build()

  val workRequestForSyncingPlatformParameters =
    PeriodicWorkRequest.Builder(
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
}
