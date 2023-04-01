package org.oppia.android.testing

import org.oppia.android.app.model.EventLog
import org.oppia.android.util.logging.AnalyticsEventLogger
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/**  A test specific fake for the event logger. */
@Singleton
class FakeAnalyticsEventLogger @Inject constructor() : AnalyticsEventLogger {
  private val eventList = CopyOnWriteArrayList<EventLog>()

  override fun logEvent(eventLog: EventLog) {
    eventList.add(eventLog)
  }

  /** Returns the oldest event that's been logged. */
  fun getOldestEvent(): EventLog = eventList.first()

  /** Returns the most recently logged event. */
  fun getMostRecentEvent(): EventLog = getMostRecentEvents(count = 1).first()

  /** Returns the most recent [count] logged events. */
  fun getMostRecentEvents(count: Int): List<EventLog> = eventList.takeLast(count)

  /** Clears all the events that are currently logged. */
  fun clearAllEvents() = eventList.clear()

  /** Returns whether a certain event has been logged or not, based on the provided [predicate]. */
  fun hasEventLogged(predicate: (EventLog) -> Boolean): Boolean = eventList.find(predicate) != null

  /** Returns the number of logged events that match the provided [predicate]. */
  fun countEvents(predicate: (EventLog) -> Boolean): Int = eventList.count(predicate)

  /** Returns true if there are no events logged. */
  fun noEventsPresent(): Boolean = eventList.isEmpty()

  /** Returns the number of events logged to date (and not cleared by [clearAllEvents]). */
  fun getEventListCount(): Int = eventList.size
}
