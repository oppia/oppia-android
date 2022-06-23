package org.oppia.android.util.logging.performancemetrics

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import javax.inject.Inject
import org.oppia.android.util.logging.MetricLogScheduler

private const val OPPIA_PERIODIC_METRIC_WORK = "OPPIA_PERIODIC_METRIC_WORK"
private const val OPPIA_STORAGE_USAGE_WORK = "OPPIA_STORAGE_USAGE_WORK"
private const val OPPIA_MEMORY_USAGE_WORK = "OPPIA_MEMORY_USAGE_WORK"

/**
 * Enqueues work requests for generating metric log reports for gaining an insight regarding into
 * the performance of the application.
 */
class PerformanceMetricsLogScheduler @Inject constructor() : MetricLogScheduler {
  override fun enqueueWorkRequestForPeriodicMetrics(
    workManager: WorkManager,
    workRequest: PeriodicWorkRequest
  ) {
    workManager.enqueueUniquePeriodicWork(
      OPPIA_PERIODIC_METRIC_WORK,
      ExistingPeriodicWorkPolicy.KEEP,
      workRequest
    )
  }

  override fun enqueueWorkRequestForStorageUsage(
    workManager: WorkManager,
    workRequest: PeriodicWorkRequest
  ) {
    workManager.enqueueUniquePeriodicWork(
      OPPIA_STORAGE_USAGE_WORK,
      ExistingPeriodicWorkPolicy.KEEP,
      workRequest
    )
  }

  override fun enqueueWorkRequestForMemoryUsage(
    workManager: WorkManager,
    workRequest: PeriodicWorkRequest
  ) {
    workManager.enqueueUniquePeriodicWork(
      OPPIA_MEMORY_USAGE_WORK,
      ExistingPeriodicWorkPolicy.KEEP,
      workRequest
    )
  }
}
