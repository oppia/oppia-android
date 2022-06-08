package org.oppia.android.util.logging.performancemetrics

import org.oppia.android.app.model.OppiaMetricLog

interface PerformanceMetricLogger {
  fun logPerformanceMetric(oppiaMetricLog: OppiaMetricLog)
}
