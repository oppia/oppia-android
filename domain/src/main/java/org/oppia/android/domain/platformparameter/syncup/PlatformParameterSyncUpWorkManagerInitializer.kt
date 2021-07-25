package org.oppia.android.domain.platformparameter.syncup

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import org.oppia.android.domain.oppialogger.ApplicationStartupListener
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlatformParameterSyncUpWorkManagerInitializer @Inject constructor(
  private val context: Context,
) : ApplicationStartupListener {

  private val OPPIA_PARAMETER_WORK = "OPPIA_PARAMETER_WORK_REQUEST"

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

  private fun workRequestForSyncingUpPlatformParameters(): PeriodicWorkRequest {
    return PeriodicWorkRequest
      .Builder(PlatformParameterSyncUpWorker::class.java, 12, TimeUnit.HOURS)
      .setInputData(workerTypeForSyncingUpParameters)
      .setConstraints(platformParameterSyncUpWorkerConstraints)
      .build()
  }

  override fun onCreate() {
    val workManager = WorkManager.getInstance(context)
    workManager.enqueueUniquePeriodicWork(
      OPPIA_PARAMETER_WORK,
      ExistingPeriodicWorkPolicy.KEEP,
      workRequestForSyncingUpPlatformParameters()
    )
  }
}
