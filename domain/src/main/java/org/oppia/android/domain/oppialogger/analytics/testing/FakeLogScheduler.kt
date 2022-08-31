package org.oppia.android.domain.oppialogger.analytics.testing

import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import org.oppia.android.util.logging.MetricLogScheduler
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** A test specific fake for the log uploader. */
@Singleton
class FakeLogScheduler @Inject constructor() : MetricLogScheduler {
  private val schedulingStorageUsageMetricLoggingRequestIdList = mutableListOf<UUID>()
  private val schedulingPeriodicUiMetricLoggingRequestIdList = mutableListOf<UUID>()
  private val schedulingPeriodicBackgroundMetricsLoggingRequestIdList = mutableListOf<UUID>()

  override fun enqueueWorkRequestForPeriodicBackgroundMetrics(
    workManager: WorkManager,
    workRequest: PeriodicWorkRequest
  ) {
    schedulingPeriodicBackgroundMetricsLoggingRequestIdList.add(workRequest.id)
  }

  override fun enqueueWorkRequestForStorageUsage(
    workManager: WorkManager,
    workRequest: PeriodicWorkRequest
  ) {
    schedulingStorageUsageMetricLoggingRequestIdList.add(workRequest.id)
  }

  override fun enqueueWorkRequestForPeriodicUiMetrics(
    workManager: WorkManager,
    workRequest: PeriodicWorkRequest
  ) {
    schedulingPeriodicUiMetricLoggingRequestIdList.add(workRequest.id)
  }

  /**
   * Returns the most recent work request id that's stored in the
   * [schedulingStorageUsageMetricLoggingRequestIdList].
   */
  fun getMostRecentStorageUsageMetricLoggingRequestId() =
    schedulingStorageUsageMetricLoggingRequestIdList.last()

  /**
   * Returns the most recent work request id that's stored in the
   * [schedulingPeriodicUiMetricLoggingRequestIdList].
   */
  fun getMostRecentPeriodicUiMetricLoggingRequestId() =
    schedulingPeriodicUiMetricLoggingRequestIdList.last()

  /**
   * Returns the most recent work request id that's stored in the
   * [schedulingPeriodicBackgroundMetricsLoggingRequestIdList].
   */
  fun getMostRecentPeriodicBackgroundMetricLoggingRequestId() =
    schedulingPeriodicBackgroundMetricsLoggingRequestIdList.last()
}
