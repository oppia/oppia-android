package org.oppia.android.domain.oppialogger.loguploader

import org.oppia.android.domain.oppialogger.analytics.AnalyticsController
import org.oppia.android.domain.oppialogger.analytics.FirestoreDataController
import org.oppia.android.domain.oppialogger.analytics.PerformanceMetricsController
import org.oppia.android.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.android.domain.oppialogger.exceptions.toException
import org.oppia.android.domain.workmanager.OppiaWorker
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.logging.ExceptionLogger
import org.oppia.android.util.logging.SyncStatusManager
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsEventLogger
import javax.inject.Inject

/** Worker class that extracts log reports from the cache store and logs them to the remote service. */
class LogUploadWorker private constructor(
  private val analyticsController: AnalyticsController,
  private val exceptionsController: ExceptionsController,
  private val performanceMetricsController: PerformanceMetricsController,
  private val exceptionLogger: ExceptionLogger,
  private val dataController: FirestoreDataController,
  private val performanceMetricsEventLogger: PerformanceMetricsEventLogger,
  private val consoleLogger: ConsoleLogger,
  private val syncStatusManager: SyncStatusManager
) : OppiaWorker<LogUploadWorker.Operation> {
  companion object {
    const val WORKER_NAME = "LogUploadWorker"
  }

  enum class Operation(override val persistentName: String) : OppiaWorker.TaskType {
    UPLOAD_EVENTS("upload_events"),
    UPLOAD_EXCEPTIONS("upload_exceptions"),
    UPLOAD_PERFORMANCE_METRICS("upload_performance_metrics"),
    UPLOAD_FIRESTORE_DATA("upload_firestore_data")
  }

  override suspend fun doWork(taskType: Operation): OppiaWorker.Result {
    return try {
      when (taskType) {
        Operation.UPLOAD_EVENTS -> analyticsController.uploadEventLogsAndWait()
        Operation.UPLOAD_EXCEPTIONS -> {
          for (exceptionLog in exceptionsController.getExceptionLogStoreList()) {
            exceptionLogger.logException(exceptionLog.toException())
            exceptionsController.removeFirstExceptionLogFromStore()
          }
        }
        Operation.UPLOAD_PERFORMANCE_METRICS -> {
          for (performanceMetricsLog in performanceMetricsController.getMetricLogStoreList()) {
            performanceMetricsEventLogger.logPerformanceMetric(performanceMetricsLog)
            performanceMetricsController.removeFirstMetricLogFromStore()
          }
        }
        Operation.UPLOAD_FIRESTORE_DATA -> dataController.uploadData()
      }
      OppiaWorker.Result.SUCCESS
    } catch (e: Exception) {
      if (taskType == Operation.UPLOAD_EVENTS) {
        syncStatusManager.reportUploadError()
      }
      consoleLogger.e(WORKER_NAME, "Failed operation: ${taskType.persistentName}.", e)
      OppiaWorker.Result.FAILURE
    }
  }

  /** Creates an instance of [LogUploadWorker] by properly injecting dependencies. */
  class Factory @Inject constructor(
    private val analyticsController: AnalyticsController,
    private val exceptionsController: ExceptionsController,
    private val performanceMetricsController: PerformanceMetricsController,
    private val exceptionLogger: ExceptionLogger,
    private val dataController: FirestoreDataController,
    private val performanceMetricsEventLogger: PerformanceMetricsEventLogger,
    private val consoleLogger: ConsoleLogger,
    private val syncStatusManager: SyncStatusManager
  ) : OppiaWorker.Factory<Operation> {
    override val supportedTaskTypes: List<Operation> = Operation.values().toList()

    override fun createWorker(): OppiaWorker<Operation> {
      return LogUploadWorker(
        analyticsController,
        exceptionsController,
        performanceMetricsController,
        exceptionLogger,
        dataController,
        performanceMetricsEventLogger,
        consoleLogger,
        syncStatusManager
      )
    }
  }
}
