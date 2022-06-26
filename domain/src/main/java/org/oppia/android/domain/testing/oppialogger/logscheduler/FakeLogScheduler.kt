package org.oppia.android.domain.testing.oppialogger.logscheduler

import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.util.logging.MetricLogScheduler

/** A test specific fake for the log uploader. */
@Singleton
class FakeLogScheduler @Inject constructor() : MetricLogScheduler {
  private val schedulingStorageUsageMetricLoggingRequestIdList = mutableListOf<UUID>()
  private val schedulingMemoryUsageMetricLoggingRequestIdList = mutableListOf<UUID>()
  private val schedulingPeriodicPerformanceMetricLoggingRequestIdList = mutableListOf<UUID>()

  override fun enqueueWorkRequestForPeriodicMetrics(
    workManager: WorkManager,
    workRequest: PeriodicWorkRequest
  ) {
    schedulingPeriodicPerformanceMetricLoggingRequestIdList.add(workRequest.id)
  }

  override fun enqueueWorkRequestForStorageUsage(
    workManager: WorkManager,
    workRequest: PeriodicWorkRequest
  ) {
    schedulingStorageUsageMetricLoggingRequestIdList.add(workRequest.id)
  }

  override fun enqueueWorkRequestForMemoryUsage(
    workManager: WorkManager,
    workRequest: PeriodicWorkRequest
  ) {
    schedulingMemoryUsageMetricLoggingRequestIdList.add(workRequest.id)
  }

  /**
   * Returns the most recent work request id that's stored in the
   * [schedulingStorageUsageMetricLoggingRequestIdList].
   */
  fun getMostRecentStorageUsageMetricLoggingRequestId() =
    schedulingStorageUsageMetricLoggingRequestIdList.last()

  /**
   * Returns the most recent work request id that's stored in the
   * [schedulingMemoryUsageMetricLoggingRequestIdList].
   */
  fun getMostRecentMemoryUsageMetricLoggingRequestId() =
    schedulingMemoryUsageMetricLoggingRequestIdList.last()

  /**
   * Returns the most recent work request id that's stored in the
   * [schedulingPeriodicPerformanceMetricLoggingRequestIdList].
   */
  fun getMostRecentPeriodicPerformanceMetricLoggingRequestId() =
    schedulingPeriodicPerformanceMetricLoggingRequestIdList.last()
}
