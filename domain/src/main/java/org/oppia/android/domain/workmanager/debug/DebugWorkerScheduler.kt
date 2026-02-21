package org.oppia.android.domain.workmanager.debug

import org.oppia.android.domain.workmanager.StartupWorkerScheduleReadinessListener
import org.oppia.android.domain.workmanager.WorkManagerScheduler
import org.oppia.android.domain.workmanager.debug.DebugWorker.Companion.WORKER_NAME
import javax.inject.Inject

class DebugWorkerScheduler @Inject constructor() : StartupWorkerScheduleReadinessListener {
  override fun scheduleWork(workManagerScheduler: WorkManagerScheduler) {
    for (operation in DebugWorker.Operation.values()) {
      workManagerScheduler.schedulePeriodicWorker(
        WORKER_NAME,
        operation,
        operation.period,
        operation.periodUnit,
        requireNetworkConnectivity = operation.requireConnectivity
      )
    }
  }
}
