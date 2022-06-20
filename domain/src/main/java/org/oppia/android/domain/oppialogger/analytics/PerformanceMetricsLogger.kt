package org.oppia.android.domain.oppialogger.analytics

import org.oppia.android.app.model.OppiaMetricLog
import org.oppia.android.domain.oppialogger.ApplicationStartupListener
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsUtils
import org.oppia.android.util.system.OppiaClock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerformanceMetricsLogger @Inject constructor(
  private val oppiaLogger: OppiaLogger,
  private val performanceMetricsUtils: PerformanceMetricsUtils,
  private val oppiaClock: OppiaClock
) : ApplicationStartupListener {

  /**
   * Use a large Long value such that the time difference based on any timestamp will be negative
   * and thus ignored until the app records initial time during [onCreate].
   */
  private var firstTimestamp: Long = Long.MAX_VALUE

  override fun onCreate() {
    firstTimestamp = oppiaClock.getCurrentTimeMs()
  }

  fun logApkSize() {
    oppiaLogger.logLowPriorityMetricEvent(
      OppiaMetricLog.CurrentScreen.HOME_SCREEN,
      createApkSizeLoggableMetric(performanceMetricsUtils.getApkSize())
    )
  }

  fun logStorageUsage() {
    oppiaLogger.logLowPriorityMetricEvent(
      OppiaMetricLog.CurrentScreen.HOME_SCREEN,
      createStorageUsageLoggableMetric(performanceMetricsUtils.getUsedStorage())
    )
  }

  fun logStartupLatency() {
    val startupLatency = oppiaClock.getCurrentTimeMs() - firstTimestamp
    if (startupLatency >= 0) {
      oppiaLogger.logLowPriorityMetricEvent(
        OppiaMetricLog.CurrentScreen.HOME_SCREEN,
        createStartupLatencyLoggableMetric(startupLatency)
      )
    }
  }

  fun logMemoryUsage() {
    oppiaLogger.logMediumPriorityMetricEvent(
      OppiaMetricLog.CurrentScreen.SCREEN_UNSPECIFIED,
      createMemoryUsageLoggableMetric(performanceMetricsUtils.getTotalPssUsed())
    )
  }

  fun logNetworkUsage() {
    oppiaLogger.logHighPriorityMetricEvent(
      OppiaMetricLog.CurrentScreen.SCREEN_UNSPECIFIED,
      createNetworkUsageLoggableMetric(
        performanceMetricsUtils.getTotalReceivedBytes(),
        performanceMetricsUtils.getTotalSentBytes()
      )
    )
  }

  fun logCpuUsage(cpuUsage: Long) {
    oppiaLogger.logHighPriorityMetricEvent(
      OppiaMetricLog.CurrentScreen.SCREEN_UNSPECIFIED,
      createCpuUsageLoggableMetric(cpuUsage)
    )
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
    internal fun OppiaLogger.logHighPriorityMetricEvent(
      currentScreen: OppiaMetricLog.CurrentScreen,
      loggableMetric: OppiaMetricLog.LoggableMetric
    ) {
      logPerformanceMetrics(
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
    internal fun OppiaLogger.logMediumPriorityMetricEvent(
      currentScreen: OppiaMetricLog.CurrentScreen,
      loggableMetric: OppiaMetricLog.LoggableMetric
    ) {
      logPerformanceMetrics(
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
    internal fun OppiaLogger.logLowPriorityMetricEvent(
      currentScreen: OppiaMetricLog.CurrentScreen,
      loggableMetric: OppiaMetricLog.LoggableMetric
    ) {
      logPerformanceMetrics(
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
    cpuUsage: Long
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
