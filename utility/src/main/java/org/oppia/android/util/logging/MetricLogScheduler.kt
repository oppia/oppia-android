package org.oppia.android.util.logging

import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager

/** Scheduler for scheduling metric log reports related to the performance of the application. */
interface MetricLogScheduler {
  /**
   * Enqueues a [workRequest] using the [workManager] for scheduling metric collection of periodic
   * metrics like network and cpu usage.
   */
  fun enqueueWorkRequestForPeriodicBackgroundMetrics(
    workManager: WorkManager,
    workRequest: PeriodicWorkRequest
  )

  /**
   * Enqueues a [workRequest] using the [workManager] for scheduling metric collection of storage
   * usage of the application on the current device.
   */
  fun enqueueWorkRequestForStorageUsage(workManager: WorkManager, workRequest: PeriodicWorkRequest)

  /**
   * Enqueues a [workRequest] using the [workManager] for scheduling metric collection of periodic
   * ui metrics like memory usage.
   */
  fun enqueueWorkRequestForPeriodicUiMetrics(
    workManager: WorkManager,
    workRequest: PeriodicWorkRequest
  )
}
