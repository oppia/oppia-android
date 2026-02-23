package org.oppia.android.domain.platformparameter.syncup

import org.oppia.android.domain.workmanager.StartupWorkerScheduleReadinessListener
import org.oppia.android.domain.workmanager.WorkManagerScheduler
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.platformparameter.SyncUpWorkerTimePeriodHours
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Scheduler for [PlatformParameterSyncUpWorker] operations once startup-bound workers are allowed
 * to initialize during application startup.
 */
class PlatformParameterSyncUpWorkerScheduler @Inject constructor(
  @SyncUpWorkerTimePeriodHours private val workRequestRepeatInterval: PlatformParameterValue<Int>
) : StartupWorkerScheduleReadinessListener {
  override fun scheduleWork(workManagerScheduler: WorkManagerScheduler) {
    workManagerScheduler.schedulePeriodicWorker(
      PlatformParameterSyncUpWorker.WORKER_NAME,
      PlatformParameterSyncUpWorker.Operation.REFRESH_PLATFORM_PARAMETERS,
      workRequestRepeatInterval.value.toLong(),
      TimeUnit.HOURS
    )
  }
}
