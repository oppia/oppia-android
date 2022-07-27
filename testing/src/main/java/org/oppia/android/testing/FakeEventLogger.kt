package org.oppia.android.testing

import java.util.concurrent.ConcurrentSkipListSet
import org.oppia.android.app.model.EventLog
import org.oppia.android.util.logging.EventLogger
import javax.inject.Inject
import javax.inject.Singleton

/**  A test specific fake for the event logger. */
@Singleton
class FakeEventLogger @Inject constructor() : EventLogger {
  /**
   * The list of [EventLog]s sorted by recency (i.e. the first item in the list will be the most
   * recent, and the last will be the least recent).
   */
  private val eventList = ConcurrentSkipListSet(EVENT_LIST_COMPARATOR)

  override fun logEvent(eventLog: EventLog) {
    eventList.add(eventLog)
  }

  /** Returns the oldest event that's been logged, relative to the event's logged time. */
  fun getOldestEvent(): EventLog = eventList.last()

  /** Returns the most recently logged event, relative to the event's logged time. */
  fun getMostRecentEvent(): EventLog = getMostRecentEvents(count = 1).first()

  /** Returns the most recently [count] logged events, relative to the event's logged time. */
  fun getMostRecentEvents(count: Int): List<EventLog> = eventList.take(count)

  /** Clears all the events that are currently logged. */
  fun clearAllEvents() = eventList.clear()

  /** Checks if a certain event has been logged or not. */
  fun hasEventLogged(eventLog: EventLog): Boolean = eventLog in eventList

  /** Returns true if there are no events logged. */
  fun noEventsPresent(): Boolean = eventList.isEmpty()

  /** Returns the number of events logged to date (and not cleared by [clearAllEvents]). */
  fun getEventListCount(): Int = eventList.size

  private companion object {
    private val EVENT_LIST_COMPARATOR = compareBy<EventLog> { it.timestamp }
  }
}
