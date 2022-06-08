package org.oppia.android.domain.oppialogger.loguploader

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import org.oppia.android.domain.oppialogger.ApplicationStartupListener
import org.oppia.android.util.logging.LogUploader
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.domain.oppialogger.loggenerator.LogGenerationWorker
import org.oppia.android.util.logging.LogGenerator
import org.oppia.android.util.platformparameter.PerformanceMetricsCollectionHighFrequentRecordTimeIntervalInMinutes
import org.oppia.android.util.platformparameter.PerformanceMetricsCollectionLessFrequentRecordTimeIntervalInMinutes
import org.oppia.android.util.platformparameter.PlatformParameterValue

/** Enqueues unique periodic work requests for uploading events and exceptions to the remote service on application creation. */
@Singleton
class LogUploadWorkManagerInitializer @Inject constructor(
  private val context: Context,
  private val logUploader: LogUploader,
  private val logGenerator: LogGenerator,
  @PerformanceMetricsCollectionHighFrequentRecordTimeIntervalInMinutes
  performanceMetricsCollectionHighFrequencyTimeInterval: PlatformParameterValue<Int>,
  @PerformanceMetricsCollectionLessFrequentRecordTimeIntervalInMinutes
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
      LogGenerationWorker.WORKER_CASE_KEY,
      LogGenerationWorker.PERIODIC_METRIC_WORKER
    )
    .build()

  private val workerCaseForCreatingStorageUsageMetricLogs: Data = Data.Builder()
    .putString(
      LogGenerationWorker.WORKER_CASE_KEY,
      LogGenerationWorker.STORAGE_USAGE_WORKER
    )
    .build()

  private val workerCaseForCreatingMemoryUsageMetricLogs: Data = Data.Builder()
    .putString(
      LogGenerationWorker.WORKER_CASE_KEY,
      LogGenerationWorker.MEMORY_USAGE_WORKER
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
      LogGenerationWorker::class.java,
      performanceMetricsCollectionHighFrequencyTimeInterval.value.toLong(),
      TimeUnit.MINUTES
    )
    .setInputData(workerCaseForCreatingPeriodicMetricLogs)
    .setConstraints(logReportWorkerConstraints)
    .build()

  private val workRequestForGeneratingStorageUsageMetricLogs: PeriodicWorkRequest =
    PeriodicWorkRequest
      .Builder(
        LogGenerationWorker::class.java,
        performanceMetricCollectionLowFrequencyTimeInterval.value.toLong(),
        TimeUnit.MINUTES
      )
      .setInputData(workerCaseForCreatingStorageUsageMetricLogs)
      .setConstraints(logReportWorkerConstraints)
      .build()

  private val workRequestForGeneratingMemoryUsageMetricLogs: PeriodicWorkRequest =
    PeriodicWorkRequest
      .Builder(
        LogGenerationWorker::class.java,
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
    logGenerator.enqueueWorkRequestForPeriodicMetrics(
      workManager,
      workRequestForGeneratingPeriodicMetricLogs
    )
    logGenerator.enqueueWorkRequestForStorageUsage(
      workManager,
      workRequestForGeneratingStorageUsageMetricLogs
    )
    logGenerator.enqueueWorkRequestForMemoryUsage(
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
