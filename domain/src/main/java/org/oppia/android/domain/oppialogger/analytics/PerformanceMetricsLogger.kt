package org.oppia.android.domain.oppialogger.analytics

import android.util.Log
import org.oppia.android.app.model.OppiaMetricLog
import org.oppia.android.app.model.ScreenName
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsAssessor
import org.oppia.android.util.platformparameter.EnablePerformanceMetricsCollection
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.system.OppiaClock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Convenience logger for performance metrics related analytics events.
 *
 * This logger is meant primarily to be used directly in places where a certain performance metric
 * has to be logged.
 */
@Singleton
class PerformanceMetricsLogger @Inject constructor(
  private val performanceMetricsController: PerformanceMetricsController,
  private val performanceMetricsAssessor: PerformanceMetricsAssessor,
  private val oppiaClock: OppiaClock,
  @EnablePerformanceMetricsCollection
  private val enablePerformanceMetricsCollection: PlatformParameterValue<Boolean>
) {

  /**
   * Logs the apk size of the application as a performance metric for the current state of the app.
   * It must be noted that the value of this metric won't change across calls during the same
   * application instance.
   *
   * @param currentScreen denotes the application screen at which this metric has been logged
   */
  fun logApkSize(currentScreen: ScreenName) {
    if (enablePerformanceMetricsCollection.value) {
      performanceMetricsController.logLowPriorityMetricEvent(
        oppiaClock.getCurrentTimeMs(),
        currentScreen,
        createApkSizeLoggableMetric(performanceMetricsAssessor.getApkSize())
      )
    }
  }

  /**
   * Logs the storage usage of the application as a performance metric for the current state of the
   * app. It must be noted that the value of this metric will change across calls during the same
   * application instance.
   *
   * @param currentScreen denotes the application screen at which this metric has been logged
   */
  fun logStorageUsage(currentScreen: ScreenName) {
    if (enablePerformanceMetricsCollection.value) {
      performanceMetricsController.logLowPriorityMetricEvent(
        oppiaClock.getCurrentTimeMs(),
        currentScreen,
        createStorageUsageLoggableMetric(performanceMetricsAssessor.getUsedStorage())
      )
    }
  }

  /**
   * Logs the startup latency of the application as a performance metric for the current state of
   * the app. This metric should only be logged when the application starts.
   *
   * @param startupLatency denotes the startup latency value that'll be logged to Firebase
   * @param currentScreen denotes the application screen at which this metric has been logged
   */
  fun logStartupLatency(startupLatency: Long, currentScreen: ScreenName) {
    if (startupLatency >= 0 && enablePerformanceMetricsCollection.value) {
      performanceMetricsController.logLowPriorityMetricEvent(
        oppiaClock.getCurrentTimeMs(),
        currentScreen,
        createStartupLatencyLoggableMetric(startupLatency)
      )
    }
  }

  /**
   * Logs the memory usage of the application as a performance metric for the current state of the
   * app. It must be noted that the value of this metric will change across calls during the same
   * application instance.
   *
   * @param currentScreen denotes the application screen at which this metric has been logged
   */
  fun logMemoryUsage(currentScreen: ScreenName) {
    if (enablePerformanceMetricsCollection.value) {
      performanceMetricsController.logMediumPriorityMetricEvent(
        oppiaClock.getCurrentTimeMs(),
        currentScreen,
        createMemoryUsageLoggableMetric(performanceMetricsAssessor.getTotalPssUsed())
      )
    }
  }

  /**
   * Logs the network usage of the application as a performance metric for the current state of the
   * app. It must be noted that the value of this metric will change across calls during the same
   * application instance.
   *
   * @param currentScreen denotes the application screen at which this metric has been logged
   */
  fun logNetworkUsage(currentScreen: ScreenName) {
    if (enablePerformanceMetricsCollection.value) {
      performanceMetricsController.logHighPriorityMetricEvent(
        oppiaClock.getCurrentTimeMs(),
        currentScreen,
        createNetworkUsageLoggableMetric(
          performanceMetricsAssessor.getTotalReceivedBytes(),
          performanceMetricsAssessor.getTotalSentBytes()
        )
      )
    }
  }

  /**
   * Logs the cpu usage of the application as a performance metric for the current state of the
   * app. It must be noted that the value of this metric will change across calls during the same
   * application instance. It must also be noted that the metric will only be logged when cpuUsage
   * is positive since garbage collection can lead to negative values as well.
   *
   * @param currentScreen denotes the application screen at which this metric has been logged
   * @param cpuUsage denotes the relative cpu usage of the application which is measured across two
   * time-separated points in the application.
   */
  fun logCpuUsage(currentScreen: ScreenName, cpuUsage: Double) {
    if (enablePerformanceMetricsCollection.value) {
      performanceMetricsController.logHighPriorityMetricEvent(
        oppiaClock.getCurrentTimeMs(),
        currentScreen,
        createCpuUsageLoggableMetric(cpuUsage)
      )
    }
  }

  internal companion object {
    /**
     * Logs a high priority performance metric occurring at [currentScreen] defined by [loggableMetric].
     *
     * This will schedule a background upload of the event if there's internet connectivity, otherwise
     * it will cache the event for a later upload.
     *
     * This method should only be used for logging periodic metrics like network and cpu usage of the
     * application. These metrics are important to log and should be prioritized over metrics logged
     * via [logMediumPriorityMetricEvent] and [logLowPriorityMetricEvent].
     */
    private fun PerformanceMetricsController.logHighPriorityMetricEvent(
      timestamp: Long,
      currentScreen: ScreenName,
      loggableMetric: OppiaMetricLog.LoggableMetric
    ) {
      logPerformanceMetricsEvent(
        timestamp,
        currentScreen,
        loggableMetric,
        OppiaMetricLog.Priority.HIGH_PRIORITY
      )
    }

    /**
     * Logs a medium priority performance metric occurring at [currentScreen] defined by [loggableMetric].
     *
     * This will schedule a background upload of the event if there's internet connectivity, otherwise
     * it will cache the event for a later upload.
     *
     * Medium priority metrics may be removed from the event cache if device space is limited, and
     * there's no connectivity for immediately sending events. These metrics will however be
     * prioritised over Low priority metrics.
     *
     * This method should only be used for logging ui-specific metrics like memory usage of the
     * application. These metrics are important to log (but not as important as high priority metrics)
     * and should be prioritized over metrics logged via [logLowPriorityMetricEvent].
     */
    private fun PerformanceMetricsController.logMediumPriorityMetricEvent(
      timestamp: Long,
      currentScreen: ScreenName,
      loggableMetric: OppiaMetricLog.LoggableMetric
    ) {
      logPerformanceMetricsEvent(
        timestamp,
        currentScreen,
        loggableMetric,
        OppiaMetricLog.Priority.MEDIUM_PRIORITY
      )
    }

    /**
     * Logs a low priority performance metric occurring at [currentScreen] defined by [loggableMetric]
     * corresponding to time [timestamp].
     *
     * This will schedule a background upload of the event if there's internet connectivity, otherwise
     * it will cache the event for a later upload.
     *
     * Low priority metrics may be removed from the event cache if device space is limited, and
     * there's no connectivity for immediately sending events.
     *
     * This method should only be used for logging metrics that are to be logged at the beginning of
     * the application like apk size and storage usage.
     *
     * Callers should use this for events that are nice to have, but okay to miss occasionally (as
     * it's unexpected for events to actually be dropped since the app is configured to support a
     * large number of cached events at one time).
     */
    private fun PerformanceMetricsController.logLowPriorityMetricEvent(
      timestamp: Long,
      currentScreen: ScreenName,
      loggableMetric: OppiaMetricLog.LoggableMetric
    ) {
      logPerformanceMetricsEvent(
        timestamp,
        currentScreen,
        loggableMetric,
        OppiaMetricLog.Priority.LOW_PRIORITY
      )
    }
  }

  /**
   * Returns the loggable metric of the performance metric event log indicating the size of the
   * apk file of the application.
   */
  private fun createApkSizeLoggableMetric(
    apkSize: Long
  ): OppiaMetricLog.LoggableMetric {
    return OppiaMetricLog.LoggableMetric.newBuilder()
      .setApkSizeMetric(
        OppiaMetricLog.ApkSizeMetric.newBuilder()
          .setApkSizeBytes(apkSize)
          .build()
      ).build()
  }

  /**
   * Returns the loggable metric of the performance metric event log indicating the amount of
   * storage space used by the application on user's device.
   */
  private fun createStorageUsageLoggableMetric(
    storageUsage: Long
  ): OppiaMetricLog.LoggableMetric {
    return OppiaMetricLog.LoggableMetric.newBuilder()
      .setStorageUsageMetric(
        OppiaMetricLog.StorageUsageMetric.newBuilder()
          .setStorageUsageBytes(storageUsage)
          .build()
      ).build()
  }

  /**
   * Returns the loggable metric of the performance metric event log indicating the number of
   * milliseconds required to start up the application from a cold start.
   */
  private fun createStartupLatencyLoggableMetric(
    startupLatency: Long
  ): OppiaMetricLog.LoggableMetric {
    return OppiaMetricLog.LoggableMetric.newBuilder()
      .setStartupLatencyMetric(
        OppiaMetricLog.StartupLatencyMetric.newBuilder()
          .setStartupLatencyMillis(startupLatency)
          .build()
      ).build()
  }

  /**
   * Returns the loggable metric of the performance metric event log indicating the the amount of
   * memory used by the application on user's device.
   */
  private fun createMemoryUsageLoggableMetric(
    totalPssBytes: Long
  ): OppiaMetricLog.LoggableMetric {
    return OppiaMetricLog.LoggableMetric.newBuilder()
      .setMemoryUsageMetric(
        OppiaMetricLog.MemoryUsageMetric.newBuilder()
          .setTotalPssBytes(totalPssBytes)
          .build()
      ).build()
  }

  /**
   * Returns the loggable metric of the performance metric event log indicating the the amount of
   * CPU used by the application on user's device.
   */
  private fun createCpuUsageLoggableMetric(
    cpuUsage: Double
  ): OppiaMetricLog.LoggableMetric {
    return OppiaMetricLog.LoggableMetric.newBuilder()
      .setCpuUsageMetric(
        OppiaMetricLog.CpuUsageMetric.newBuilder()
          .setCpuUsageMetric(cpuUsage)
          .build()
      ).build()
  }

  /**
   * Returns the loggable metric of the performance metric event log indicating the the amount of
   * network used by the application on user's device.
   */
  private fun createNetworkUsageLoggableMetric(
    totalBytesReceived: Long,
    totalBytesSent: Long
  ): OppiaMetricLog.LoggableMetric {
    return OppiaMetricLog.LoggableMetric.newBuilder()
      .setNetworkUsageMetric(
        OppiaMetricLog.NetworkUsageMetric.newBuilder()
          .setBytesSent(totalBytesSent)
          .setBytesReceived(totalBytesReceived)
          .build()
      ).build()
  }
}
