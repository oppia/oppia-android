package org.oppia.android.domain.oppialogger.loguploader

import org.oppia.android.domain.workmanager.StartupWorkerScheduleReadinessListener
import org.oppia.android.domain.workmanager.WorkManagerScheduler
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Enqueues unique periodic work requests for uploading events and exceptions to the remote service
 * on application creation.
 */
class LogReportWorkerScheduler @Inject constructor() : StartupWorkerScheduleReadinessListener {
  override fun scheduleWork(workManagerScheduler: WorkManagerScheduler) {
    for (operation in LogUploadWorker.Operation.values()) {
      workManagerScheduler.schedulePeriodicWorker(
        LogUploadWorker.WORKER_NAME, operation, 6, TimeUnit.HOURS
      )
    }
  }
}
