package org.oppia.android.util.logging.performancemetrics

import org.oppia.android.app.model.OppiaMetricLog

/** Logger for uploading performance metrics event logs to remote services. */
interface PerformanceMetricsEventLogger {
  /**
   * Logs a performance metric to remote services.
   *
   * @param oppiaMetricLog refers to the log object which contains all the relevant data to be reported.
   */
  fun logPerformanceMetric(oppiaMetricLog: OppiaMetricLog)
}
