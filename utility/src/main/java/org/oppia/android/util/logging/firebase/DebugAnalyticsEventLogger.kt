package org.oppia.android.util.logging.firebase

import org.oppia.android.app.model.EventLog
import org.oppia.android.util.logging.AnalyticsEventLogger
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject

/**
 * A debug implementation of [AnalyticsEventLogger] used in developer-only builds of the event.
 *
 * It forwards events to a delegated [AnalyticsEventLogger] for real or test logging, but it also
 * records logged events for later retrieval (e.g. via [getEventList]). New instances of this class
 * are created using its [Factory].
 */
class DebugAnalyticsEventLogger private constructor(
  factory: AnalyticsEventLogger.Factory
) : AnalyticsEventLogger {
  private val realEventLogger by lazy { factory.create() }
  private val eventList = CopyOnWriteArrayList<EventLog>()

  override fun logEvent(eventLog: EventLog) {
    eventList.add(eventLog)
    realEventLogger.logEvent(eventLog)
  }

  /** Returns the list of all [EventLog]s logged since the app opened. */
  fun getEventList(): List<EventLog> = eventList

  /** Application-injectable factory for creating new instances of [DebugAnalyticsEventLogger]. */
  class Factory @Inject constructor() {
    /**
     * Returns a new [DebugAnalyticsEventLogger] that binds to a delegated logger created using
     * [delegatedLoggerFactory].
     */
    fun create(delegatedLoggerFactory: AnalyticsEventLogger.Factory): DebugAnalyticsEventLogger =
      DebugAnalyticsEventLogger(delegatedLoggerFactory)
  }
}
