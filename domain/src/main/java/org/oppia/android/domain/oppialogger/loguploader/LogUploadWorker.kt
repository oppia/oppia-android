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
import org.oppia.android.util.networking.NetworkConnectionUtil
import org.oppia.android.util.networking.NetworkConnectionUtil.ProdConnectionStatus.NONE

/** Worker to upload cached analytics (including events, exceptions, and performance metrics). */
class LogUploadWorker private constructor(
  private val analyticsController: AnalyticsController,
  private val exceptionsController: ExceptionsController,
  private val performanceMetricsController: PerformanceMetricsController,
  private val exceptionLogger: ExceptionLogger,
  private val firestoreDataController: FirestoreDataController,
  private val performanceMetricsEventLogger: PerformanceMetricsEventLogger,
  private val consoleLogger: ConsoleLogger,
  private val syncStatusManager: SyncStatusManager,
  private val networkConnectionUtil: NetworkConnectionUtil
) : OppiaWorker<LogUploadWorker.Operation> {
  companion object {
    /** The unique name used to schedule this worker. */
    const val WORKER_NAME = "LogUploadWorker"
  }

  /** The operation types supported by [LogUploadWorker]. */
  enum class Operation(override val persistentName: String) : OppiaWorker.TaskType {
    /** Instructs the worker to upload offline-cached logged analytics events. */
    UPLOAD_EVENTS("upload_events"),
    /** Instructs the worker to upload offline-cached logged exceptions. */
    UPLOAD_EXCEPTIONS("upload_exceptions"),
    /** Instructs the worker to upload offline-cached logged performance metrics. */
    UPLOAD_PERFORMANCE_METRICS("upload_performance_metrics"),
    /** Instructs the worker to upload offline-cached logged Firestore-bound events. */
    UPLOAD_FIRESTORE_DATA("upload_firestore_data")
  }

  override suspend fun doWork(taskType: Operation): OppiaWorker.Result {
    return try {
      when (taskType) {
        Operation.UPLOAD_EVENTS -> analyticsController.uploadEventLogsAndWait()
        Operation.UPLOAD_EXCEPTIONS -> uploadExceptions()
        Operation.UPLOAD_PERFORMANCE_METRICS -> uploadPerformanceMetrics()
        Operation.UPLOAD_FIRESTORE_DATA -> uploadFirestoreEvents()
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

  private suspend fun uploadExceptions() {
    check(networkConnectionUtil.getCurrentConnectionStatus() != NONE) {
      "Cannot upload exceptions without internet connectivity."
    }
    for (exceptionLog in exceptionsController.getExceptionLogStoreList()) {
      exceptionLogger.logException(exceptionLog.toException())
      exceptionsController.removeFirstExceptionLogFromStore()
    }
  }

  private suspend fun uploadPerformanceMetrics() {
    check(networkConnectionUtil.getCurrentConnectionStatus() != NONE) {
      "Cannot upload performance metrics without internet connectivity."
    }
    for (performanceMetricsLog in performanceMetricsController.getMetricLogStoreList()) {
      performanceMetricsEventLogger.logPerformanceMetric(performanceMetricsLog)
      performanceMetricsController.removeFirstMetricLogFromStore()
    }
  }

  private suspend fun uploadFirestoreEvents() {
    check(networkConnectionUtil.getCurrentConnectionStatus() != NONE) {
      "Cannot upload Firestore events without internet connectivity."
    }
    firestoreDataController.uploadData()
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
    private val syncStatusManager: SyncStatusManager,
    private val networkConnectionUtil: NetworkConnectionUtil
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
        syncStatusManager,
        networkConnectionUtil
      )
    }
  }
}
