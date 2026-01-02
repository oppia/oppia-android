package org.oppia.android.domain.platformparameter.syncup

import androidx.work.Constraints
import androidx.work.NetworkType
import org.oppia.android.domain.workmanager.StartupWorkerScheduleReadinessListener
import org.oppia.android.domain.workmanager.WorkManagerScheduler
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.platformparameter.SyncUpWorkerTimePeriodHours
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Enqueues unique periodic work requests for fetching and caching latest platform parameter values
 * from the remote service on application creation.
 */
class PlatformParameterSyncUpWorkerScheduler @Inject constructor(
  @SyncUpWorkerTimePeriodHours private val workRequestRepeatInterval: PlatformParameterValue<Int>
) : StartupWorkerScheduleReadinessListener {
  override fun scheduleWork(workManagerScheduler: WorkManagerScheduler) {
    workManagerScheduler.schedulePeriodicWorker(
      PlatformParameterSyncUpWorker.WORKER_NAME,
      PlatformParameterSyncUpWorker.Operation.REFRESH_PLATFORM_PARAMETERS,
      workRequestRepeatInterval.value.toLong(),
      TimeUnit.HOURS,
      constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .build()
    )
  }
}
