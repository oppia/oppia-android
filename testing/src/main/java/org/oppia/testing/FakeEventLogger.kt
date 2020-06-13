package org.oppia.testing

import android.content.Context
import android.os.Bundle
import org.oppia.app.model.EventLog
import org.oppia.util.logging.EventLogger
import java.util.ArrayList
import javax.inject.Inject
import javax.inject.Singleton

/**  A test specific fake for the event logger. */
@Singleton
class FakeEventLogger @Inject constructor() : EventLogger {
  private val eventList = ArrayList<EventLog>()

  override fun logEvent(context: Context, eventLog: EventLog){
    eventList.add(eventLog)
  }

  /** Returns the most recently logged event. */
  fun getMostRecentEvent(): EventLog = eventList.last()

  /** Clears all the events that are currently logged.. */
  fun clearAllEvents() = eventList.clear()

  /** Checks if a certain event has been logged or not. */
  fun hasEventLogged(eventLog: EventLog): Boolean = eventList.contains(eventLog)

  /** Returns true if there are no events logged. */
  fun noEventsPresent(): Boolean = eventList.isEmpty()
}