package org.oppia.android.domain.workmanager.debug

import androidx.work.Constraints
import androidx.work.NetworkType
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
        constraints = if (operation.requireConnectivity) {
          REQUIRE_CONNECTIVITY_CONSTRAINT
        } else UNREQUIRED_CONNECTIVITY_CONSTRAINT
      )
    }
  }

  private companion object {
    private val UNREQUIRED_CONNECTIVITY_CONSTRAINT = Constraints.Builder()
      .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
      .setRequiresBatteryNotLow(true)
      .build()

    private val REQUIRE_CONNECTIVITY_CONSTRAINT = Constraints.Builder()
      .setRequiredNetworkType(NetworkType.CONNECTED)
      .setRequiresBatteryNotLow(true)
      .build()
  }
}
