package org.oppia.android.domain.oppialogger.logscheduler

import org.oppia.android.domain.workmanager.StartupWorkerScheduleReadinessListener
import org.oppia.android.domain.workmanager.WorkManagerScheduler
import org.oppia.android.util.platformparameter.PerformanceMetricsCollectionHighFrequencyTimeIntervalInMinutes
import org.oppia.android.util.platformparameter.PerformanceMetricsCollectionLowFrequencyTimeIntervalInMinutes
import org.oppia.android.util.platformparameter.PlatformParameterValue
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Scheduler for [MetricLogSchedulingWorker] operations once startup-bound workers are allowed to
 * initialize during application startup.
 */
class MetricLogSchedulingWorkerScheduler @Inject constructor(
  @PerformanceMetricsCollectionHighFrequencyTimeIntervalInMinutes
  private val performanceMetricsCollectionHighFrequencyTimeInterval: PlatformParameterValue<Int>,
  @PerformanceMetricsCollectionLowFrequencyTimeIntervalInMinutes
  private val performanceMetricCollectionLowFrequencyTimeInterval: PlatformParameterValue<Int>
) : StartupWorkerScheduleReadinessListener {
  override fun scheduleWork(workManagerScheduler: WorkManagerScheduler) {
    workManagerScheduler.schedulePeriodicWorker(
      MetricLogSchedulingWorker.WORKER_NAME,
      MetricLogSchedulingWorker.Operation.SCHEDULE_LOG_PERIODIC_BACKGROUND_METRICS,
      performanceMetricsCollectionHighFrequencyTimeInterval.value.toLong(),
      TimeUnit.MINUTES
    )
    workManagerScheduler.schedulePeriodicWorker(
      MetricLogSchedulingWorker.WORKER_NAME,
      MetricLogSchedulingWorker.Operation.SCHEDULE_LOG_PERIODIC_UI_METRICS,
      performanceMetricsCollectionHighFrequencyTimeInterval.value.toLong(),
      TimeUnit.MINUTES
    )
    workManagerScheduler.schedulePeriodicWorker(
      MetricLogSchedulingWorker.WORKER_NAME,
      MetricLogSchedulingWorker.Operation.SCHEDULE_LOG_STORAGE_USAGE_METRICS,
      performanceMetricCollectionLowFrequencyTimeInterval.value.toLong(),
      TimeUnit.MINUTES
    )
  }
}
