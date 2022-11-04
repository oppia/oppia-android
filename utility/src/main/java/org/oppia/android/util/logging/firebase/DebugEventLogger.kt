package org.oppia.android.util.logging.firebase

import org.oppia.android.app.model.EventLog
import org.oppia.android.util.logging.AnalyticsEventLogger
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A debug implementation of [AnalyticsEventLogger] used in developer-only builds of the event.
 *
 * It forwards events to a production [AnalyticsEventLogger] for real logging, but it also records logged
 * events for later retrieval (e.g. via [getEventList]).
 */
@Singleton
class DebugEventLogger @Inject constructor(
  factory: FirebaseEventLogger.Factory
) : AnalyticsEventLogger {
  private val realEventLogger by lazy { factory.create() }
  private val eventList = CopyOnWriteArrayList<EventLog>()

  override fun logEvent(eventLog: EventLog) {
    eventList.add(eventLog)
    realEventLogger.logEvent(eventLog)
  }

  /** Returns the list of all [EventLog]s logged since the app opened. */
  fun getEventList(): List<EventLog> = eventList
}
