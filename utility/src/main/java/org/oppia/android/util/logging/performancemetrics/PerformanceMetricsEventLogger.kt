package org.oppia.android.util.logging.performancemetrics

import org.oppia.android.app.model.OppiaMetricLog

interface PerformanceMetricsEventLogger {
  fun logPerformanceMetric(oppiaMetricLog: OppiaMetricLog)
}
