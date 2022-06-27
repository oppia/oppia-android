package org.oppia.android.domain.oppialogger.loguploader

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import org.oppia.android.domain.oppialogger.ApplicationStartupListener
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulingWorker
import org.oppia.android.util.logging.LogUploader
import org.oppia.android.util.logging.MetricLogScheduler
import org.oppia.android.util.platformparameter.PerformanceMetricsCollectionHighFrequencyTimeIntervalInMinutes
import org.oppia.android.util.platformparameter.PerformanceMetricsCollectionLowFrequencyTimeIntervalInMinutes
import org.oppia.android.util.platformparameter.PlatformParameterValue
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Enqueues unique periodic work requests for uploading events and exceptions to the remote service on application creation. */
@Singleton
class LogUploadWorkManagerInitializer @Inject constructor(
  private val context: Context,
  private val logUploader: LogUploader,
  private val metricLogScheduler: MetricLogScheduler,
  @PerformanceMetricsCollectionHighFrequencyTimeIntervalInMinutes
  performanceMetricsCollectionHighFrequencyTimeInterval: PlatformParameterValue<Int>,
  @PerformanceMetricsCollectionLowFrequencyTimeIntervalInMinutes
  performanceMetricCollectionLowFrequencyTimeInterval: PlatformParameterValue<Int>
) : ApplicationStartupListener {

  private val logReportWorkerConstraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .setRequiresBatteryNotLow(true)
    .build()

  private val workerCaseForUploadingEvents: Data = Data.Builder()
    .putString(
      LogUploadWorker.WORKER_CASE_KEY,
      LogUploadWorker.EVENT_WORKER
    )
    .build()

  private val workerCaseForUploadingExceptions: Data = Data.Builder()
    .putString(
      LogUploadWorker.WORKER_CASE_KEY,
      LogUploadWorker.EXCEPTION_WORKER
    )
    .build()

  private val workerCaseForUploadingPerformanceMetrics: Data = Data.Builder()
    .putString(
      LogUploadWorker.WORKER_CASE_KEY,
      LogUploadWorker.PERFORMANCE_METRICS_WORKER
    )
    .build()

  private val workerCaseForSchedulingPeriodicMetricLogs: Data = Data.Builder()
    .putString(
      MetricLogSchedulingWorker.WORKER_CASE_KEY,
      MetricLogSchedulingWorker.PERIODIC_METRIC_WORKER
    )
    .build()

  private val workerCaseForSchedulingStorageUsageMetricLogs: Data = Data.Builder()
    .putString(
      MetricLogSchedulingWorker.WORKER_CASE_KEY,
      MetricLogSchedulingWorker.STORAGE_USAGE_WORKER
    )
    .build()

  private val workerCaseForSchedulingMemoryUsageMetricLogs: Data = Data.Builder()
    .putString(
      MetricLogSchedulingWorker.WORKER_CASE_KEY,
      MetricLogSchedulingWorker.MEMORY_USAGE_WORKER
    )
    .build()

  private val workRequestForUploadingEvents: PeriodicWorkRequest = PeriodicWorkRequest
    .Builder(LogUploadWorker::class.java, 6, TimeUnit.HOURS)
    .setInputData(workerCaseForUploadingEvents)
    .setConstraints(logReportWorkerConstraints)
    .build()

  private val workRequestForUploadingExceptions: PeriodicWorkRequest = PeriodicWorkRequest
    .Builder(LogUploadWorker::class.java, 6, TimeUnit.HOURS)
    .setInputData(workerCaseForUploadingExceptions)
    .setConstraints(logReportWorkerConstraints)
    .build()

  private val workRequestForUploadingPerformanceMetrics: PeriodicWorkRequest = PeriodicWorkRequest
    .Builder(LogUploadWorker::class.java, 6, TimeUnit.HOURS)
    .setInputData(workerCaseForUploadingPerformanceMetrics)
    .setConstraints(logReportWorkerConstraints)
    .build()


  private val workRequestForSchedulingPeriodicMetricLogs: PeriodicWorkRequest = PeriodicWorkRequest
    .Builder(
      MetricLogSchedulingWorker::class.java,
      performanceMetricsCollectionHighFrequencyTimeInterval.value.toLong(),
      TimeUnit.MINUTES
    )
    .setInputData(workerCaseForSchedulingPeriodicMetricLogs)
    .setConstraints(logReportWorkerConstraints)
    .build()

  private val workRequestForSchedulingStorageUsageMetricLogs: PeriodicWorkRequest =
    PeriodicWorkRequest
      .Builder(
        MetricLogSchedulingWorker::class.java,
        performanceMetricCollectionLowFrequencyTimeInterval.value.toLong(),
        TimeUnit.MINUTES
      )
      .setInputData(workerCaseForSchedulingStorageUsageMetricLogs)
      .setConstraints(logReportWorkerConstraints)
      .build()

  private val workRequestForSchedulingMemoryUsageMetricLogs: PeriodicWorkRequest =
    PeriodicWorkRequest
      .Builder(
        MetricLogSchedulingWorker::class.java,
        performanceMetricsCollectionHighFrequencyTimeInterval.value.toLong(),
        TimeUnit.MINUTES
      )
      .setInputData(workerCaseForSchedulingMemoryUsageMetricLogs)
      .setConstraints(logReportWorkerConstraints)
      .build()

  override fun onCreate() {
    val workManager = WorkManager.getInstance(context)
    logUploader.enqueueWorkRequestForEvents(workManager, workRequestForUploadingEvents)
    logUploader.enqueueWorkRequestForExceptions(workManager, workRequestForUploadingExceptions)
    logUploader.enqueueWorkRequestForPerformanceMetrics(
      workManager,
      workRequestForUploadingPerformanceMetrics
    )
    metricLogScheduler.enqueueWorkRequestForPeriodicMetrics(
      workManager,
      workRequestForSchedulingPeriodicMetricLogs
    )
    metricLogScheduler.enqueueWorkRequestForStorageUsage(
      workManager,
      workRequestForSchedulingStorageUsageMetricLogs
    )
    metricLogScheduler.enqueueWorkRequestForMemoryUsage(
      workManager,
      workRequestForSchedulingMemoryUsageMetricLogs
    )
  }

  /** Returns the worker constraints set for the log uploading work requests. */
  fun getLogUploadWorkerConstraints(): Constraints = logReportWorkerConstraints

  /** Returns the [UUID] of the work request that is enqueued for uploading event logs. */
  fun getWorkRequestForEventsId(): UUID = workRequestForUploadingEvents.id

  /** Returns the [UUID] of the work request that is enqueued for uploading exception logs. */
  fun getWorkRequestForExceptionsId(): UUID = workRequestForUploadingExceptions.id

  /**
   * Returns the [UUID] of the work request that is enqueued for scheduling memory usage
   * performance metrics collection.
   */
  fun getWorkRequestForSchedulingMemoryUsageMetricLogsId(): UUID =
    workRequestForSchedulingMemoryUsageMetricLogs.id

  /**
   * Returns the [UUID] of the work request that is enqueued for scheduling storage usage
   * performance metrics collection.
   */
  fun getWorkRequestForSchedulingStorageUsageMetricLogsId(): UUID =
    workRequestForSchedulingStorageUsageMetricLogs.id

  /**
   * Returns the [UUID] of the work request that is enqueued for scheduling periodic performance
   * metrics collection.
   */
  fun getWorkRequestForSchedulingPeriodicPerformanceMetricLogsId(): UUID =
    workRequestForSchedulingPeriodicMetricLogs.id

  /** Returns the [Data] that goes into the work request that is enqueued for uploading event logs. */
  fun getWorkRequestDataForEvents(): Data = workerCaseForUploadingEvents

  /** Returns the [Data] that goes into the work request that is enqueued for uploading exception logs. */
  fun getWorkRequestDataForExceptions(): Data = workerCaseForUploadingExceptions

  /**
   * Returns the [Data] that goes into the work request that is enqueued for scheduling storage
   * usage performance metrics collection.
   */
  fun getWorkRequestDataForSchedulingStorageUsageMetricLogs(): Data =
    workerCaseForSchedulingStorageUsageMetricLogs

  /**
   * Returns the [Data] that goes into the work request that is enqueued for scheduling memory
   * usage performance metrics collection.
   */
  fun getWorkRequestDataForSchedulingMemoryUsageMetricLogs(): Data =
    workerCaseForSchedulingMemoryUsageMetricLogs

  /**
   * Returns the [Data] that goes into the work request that is enqueued for scheduling periodic
   * performance metrics collection.
   */
  fun getWorkRequestDataForSchedulingPeriodicPerformanceMetricLogs(): Data =
    workerCaseForSchedulingPeriodicMetricLogs
}
