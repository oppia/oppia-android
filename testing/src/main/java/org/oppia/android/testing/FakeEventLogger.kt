package org.oppia.android.testing

import org.oppia.android.app.model.EventLog
import org.oppia.android.util.logging.EventLogger
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.util.logging.SyncStatusManager

/**  A test specific fake for the event logger. */
@Singleton
class FakeEventLogger @Inject constructor() : EventLogger {
  private val eventList = ArrayList<EventLog>()
  private val cachedEventList = mutableListOf<EventLog>()

  override fun logEvent(eventLog: EventLog) {
    eventList.add(eventLog)
  }

  override fun logCachedEvent(eventLog: EventLog) {
    cachedEventList.add(eventLog)
  }

  /** Returns the most recently logged event. */
  fun getMostRecentEvent(): EventLog = eventList.last()

  /** Clears all the events that are currently logged.. */
  fun clearAllEvents() = eventList.clear()

  /** Checks if a certain event has been logged or not. */
  fun hasEventLogged(eventLog: EventLog): Boolean = eventList.contains(eventLog)

  /** Returns true if there are no events logged. */
  fun noEventsPresent(): Boolean = eventList.isEmpty()

  /** Returns the most recently logged cached event. */
  fun getMostRecentCachedEvent(): EventLog = cachedEventList.last()

  /** Clears all the cached events that are currently logged. */
  fun clearAllCachedEvents() = cachedEventList.clear()

  /** Checks if a certain cached event has been logged or not. */
  fun hasCachedEventLogged(eventLog: EventLog): Boolean = cachedEventList.contains(eventLog)

  /** Returns true if there are no cached events logged. */
  fun noCachedEventsPresent(): Boolean = cachedEventList.isEmpty()
}
