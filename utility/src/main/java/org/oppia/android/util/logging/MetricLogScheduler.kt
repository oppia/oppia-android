package org.oppia.android.util.logging

import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager

/** Scheduler for scheduling metric log reports related to the performance of the application. */
interface MetricLogScheduler {
  /**
   * Enqueues a [workRequest] using the [workManager] for generating metric logs of periodic
   * metrics like network and cpu usage.
   */
  fun enqueueWorkRequestForPeriodicMetrics(
    workManager: WorkManager,
    workRequest: PeriodicWorkRequest
  )

  /**
   * Enqueues a [workRequest] using the [workManager] for generating metric logs for getting to
   * know the storage usage of the application on the current device.
   */
  fun enqueueWorkRequestForStorageUsage(workManager: WorkManager, workRequest: PeriodicWorkRequest)

  /**
   * Enqueues a [workRequest] using the [workManager] for generating metric logs for getting to
   * know the memory usage of the application on the current device.
   */
  fun enqueueWorkRequestForMemoryUsage(workManager: WorkManager, workRequest: PeriodicWorkRequest)
}
