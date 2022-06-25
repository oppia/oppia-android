package org.oppia.android.testing

import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.app.model.OppiaMetricLog
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsEventLogger

/**  A test specific fake for the event logger. */
@Singleton
class FakePerformanceMetricsEventLogger @Inject constructor() : PerformanceMetricsEventLogger {
  private val performanceMetricsEventList = CopyOnWriteArrayList<OppiaMetricLog>()

  override fun logPerformanceMetric(oppiaMetricLog: OppiaMetricLog) {
    performanceMetricsEventList.add(oppiaMetricLog)
  }

  /** Returns the oldest event that's been logged. */
  fun getOldestPerformanceMetricsEvent(): OppiaMetricLog = performanceMetricsEventList.first()

  /** Returns the most recently logged event. */
  fun getMostRecentPerformanceMetricsEvent(): OppiaMetricLog =
    getMostRecentPerformanceMetricsEvents(count = 1).first()

  /** Returns the most recent [count] logged events. */
  fun getMostRecentPerformanceMetricsEvents(count: Int): List<OppiaMetricLog> =
    performanceMetricsEventList.takeLast(count)

  /** Clears all the events that are currently logged. */
  fun clearAllPerformanceMetricsEvents() = performanceMetricsEventList.clear()

  /** Checks if a certain event has been logged or not. */
  fun hasPerformanceMetricsEventLogged(oppiaMetricLog: OppiaMetricLog): Boolean =
    performanceMetricsEventList.contains(oppiaMetricLog)

  /** Returns true if there are no events logged. */
  fun noPerformanceMetricsEventsPresent(): Boolean = performanceMetricsEventList.isEmpty()

  /** Returns the number of events logged to date (and not cleared by [clearAllPerformanceMetricsEvents]). */
  fun getPerformanceMetricsEventListCount(): Int = performanceMetricsEventList.size
}
