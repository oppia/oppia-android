package org.oppia.android.domain.oppialogger.loguploader

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import org.oppia.android.domain.oppialogger.ApplicationStartupListener
import org.oppia.android.domain.oppialogger.loggenerator.MetricLogSchedulingWorker
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

  private val workerCaseForCreatingPeriodicMetricLogs: Data = Data.Builder()
    .putString(
      MetricLogSchedulingWorker.WORKER_CASE_KEY,
      MetricLogSchedulingWorker.PERIODIC_METRIC_WORKER
    )
    .build()

  private val workerCaseForCreatingStorageUsageMetricLogs: Data = Data.Builder()
    .putString(
      MetricLogSchedulingWorker.WORKER_CASE_KEY,
      MetricLogSchedulingWorker.STORAGE_USAGE_WORKER
    )
    .build()

  private val workerCaseForCreatingMemoryUsageMetricLogs: Data = Data.Builder()
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

  private val workRequestForGeneratingPeriodicMetricLogs: PeriodicWorkRequest = PeriodicWorkRequest
    .Builder(
      MetricLogSchedulingWorker::class.java,
      performanceMetricsCollectionHighFrequencyTimeInterval.value.toLong(),
      TimeUnit.MINUTES
    )
    .setInputData(workerCaseForCreatingPeriodicMetricLogs)
    .setConstraints(logReportWorkerConstraints)
    .build()

  private val workRequestForGeneratingStorageUsageMetricLogs: PeriodicWorkRequest =
    PeriodicWorkRequest
      .Builder(
        MetricLogSchedulingWorker::class.java,
        performanceMetricCollectionLowFrequencyTimeInterval.value.toLong(),
        TimeUnit.MINUTES
      )
      .setInputData(workerCaseForCreatingStorageUsageMetricLogs)
      .setConstraints(logReportWorkerConstraints)
      .build()

  private val workRequestForGeneratingMemoryUsageMetricLogs: PeriodicWorkRequest =
    PeriodicWorkRequest
      .Builder(
        MetricLogSchedulingWorker::class.java,
        performanceMetricsCollectionHighFrequencyTimeInterval.value.toLong(),
        TimeUnit.MINUTES
      )
      .setInputData(workerCaseForCreatingMemoryUsageMetricLogs)
      .setConstraints(logReportWorkerConstraints)
      .build()

  override fun onCreate() {
    val workManager = WorkManager.getInstance(context)
    logUploader.enqueueWorkRequestForEvents(workManager, workRequestForUploadingEvents)
    logUploader.enqueueWorkRequestForExceptions(workManager, workRequestForUploadingExceptions)
    metricLogScheduler.enqueueWorkRequestForPeriodicMetrics(
      workManager,
      workRequestForGeneratingPeriodicMetricLogs
    )
    metricLogScheduler.enqueueWorkRequestForStorageUsage(
      workManager,
      workRequestForGeneratingStorageUsageMetricLogs
    )
    metricLogScheduler.enqueueWorkRequestForMemoryUsage(
      workManager,
      workRequestForGeneratingMemoryUsageMetricLogs
    )
  }

  /** Returns the worker constraints set for the log uploading work requests. */
  fun getLogUploadWorkerConstraints(): Constraints = logReportWorkerConstraints

  /** Returns the [UUID] of the work request that is enqueued for uploading event logs. */
  fun getWorkRequestForEventsId(): UUID = workRequestForUploadingEvents.id

  /** Returns the [UUID] of the work request that is enqueued for uploading exception logs. */
  fun getWorkRequestForExceptionsId(): UUID = workRequestForUploadingExceptions.id

  /** Returns the [Data] that goes into the work request that is enqueued for uploading event logs. */
  fun getWorkRequestDataForEvents(): Data = workerCaseForUploadingEvents

  /** Returns the [Data] that goes into the work request that is enqueued for uploading exception logs. */
  fun getWorkRequestDataForExceptions(): Data = workerCaseForUploadingExceptions
}
