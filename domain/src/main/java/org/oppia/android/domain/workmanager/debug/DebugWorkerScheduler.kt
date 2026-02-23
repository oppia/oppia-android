package org.oppia.android.domain.workmanager.debug

import org.oppia.android.domain.workmanager.StartupWorkerScheduleReadinessListener
import org.oppia.android.domain.workmanager.WorkManagerScheduler
import org.oppia.android.domain.workmanager.debug.DebugWorker.Companion.WORKER_NAME
import javax.inject.Inject

/**
 * Automatic scheduler for [DebugWorker] to demonstrate how to set up such a scheduler for other
 * custom workers.
 */
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
