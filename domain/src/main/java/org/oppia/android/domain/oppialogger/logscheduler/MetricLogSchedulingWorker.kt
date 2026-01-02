package org.oppia.android.domain.oppialogger.logscheduler

import org.oppia.android.app.model.ScreenName.BACKGROUND_SCREEN
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleLogger
import org.oppia.android.domain.oppialogger.analytics.PerformanceMetricsLogger
import org.oppia.android.domain.workmanager.OppiaWorker
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.system.OppiaClock
import javax.inject.Inject

/**
 * Worker class that generates metric log reports regarding the performance of the application
 * and then stores it in in device cache.
 */
class MetricLogSchedulingWorker private constructor(
  private val consoleLogger: ConsoleLogger,
  private val performanceMetricsLogger: PerformanceMetricsLogger,
  private val applicationLifecycleLogger: ApplicationLifecycleLogger,
  private val oppiaClock: OppiaClock
) : OppiaWorker<MetricLogSchedulingWorker.Operation> {

  companion object {
    const val WORKER_NAME = "MetricLogSchedulingWorker"
  }

  enum class Operation(override val persistentName: String) : OppiaWorker.TaskType {
    SCHEDULE_LOG_PERIODIC_BACKGROUND_METRICS("schedule_log_periodic_background_metrics"),
    SCHEDULE_LOG_PERIODIC_UI_METRICS("schedule_log_periodic_ui_metrics"),
    SCHEDULE_LOG_STORAGE_USAGE_METRICS("schedule_log_storage_usage_metrics")
  }

  override suspend fun doWork(taskType: Operation): OppiaWorker.Result {
    val timestamp = oppiaClock.getCurrentTimeMs()
    return try {
      when (taskType) {
        Operation.SCHEDULE_LOG_PERIODIC_BACKGROUND_METRICS ->
          performanceMetricsLogger.logNetworkUsage(BACKGROUND_SCREEN, timestamp)
        Operation.SCHEDULE_LOG_STORAGE_USAGE_METRICS ->
          performanceMetricsLogger.logStorageUsage(BACKGROUND_SCREEN, timestamp)
        Operation.SCHEDULE_LOG_PERIODIC_UI_METRICS -> {
          performanceMetricsLogger.logMemoryUsage(
            applicationLifecycleLogger.getCurrentScreen(), timestamp
          )
        }
      }
      OppiaWorker.Result.SUCCESS
    } catch (e: Exception) {
      consoleLogger.e(WORKER_NAME, "Failed operation: ${taskType.persistentName}.", e)
      OppiaWorker.Result.FAILURE
    }
  }

  /** Creates an instance of [MetricLogSchedulingWorker] by properly injecting dependencies. */
  class Factory @Inject constructor(
    private val consoleLogger: ConsoleLogger,
    private val performanceMetricsLogger: PerformanceMetricsLogger,
    private val applicationLifecycleLogger: ApplicationLifecycleLogger,
    private val oppiaClock: OppiaClock
  ) : OppiaWorker.Factory<Operation> {
    override val supportedTaskTypes: List<Operation> = Operation.values().toList()

    override fun createWorker(): OppiaWorker<Operation> {
      return MetricLogSchedulingWorker(
        consoleLogger,
        performanceMetricsLogger,
        applicationLifecycleLogger,
        oppiaClock
      )
    }
  }
}
