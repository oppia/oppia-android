package org.oppia.android.domain.oppialogger.loguploader

import org.oppia.android.domain.workmanager.StartupWorkerScheduleReadinessListener
import org.oppia.android.domain.workmanager.WorkManagerScheduler
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Scheduler for [LogUploadWorker] operations once startup-bound workers are allowed to initialize
 * during application startup.
 */
class LogReportWorkerScheduler @Inject constructor() : StartupWorkerScheduleReadinessListener {
  override fun scheduleWork(workManagerScheduler: WorkManagerScheduler) {
    for (operation in LogUploadWorker.Operation.values()) {
      workManagerScheduler.schedulePeriodicWorker(
        LogUploadWorker.WORKER_NAME, operation, repeatInterval = 6, intervalUnit = TimeUnit.HOURS
      )
    }
  }
}
