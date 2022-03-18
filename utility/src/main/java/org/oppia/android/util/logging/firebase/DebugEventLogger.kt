package org.oppia.android.util.logging.firebase

import org.oppia.android.app.model.EventLog
import org.oppia.android.util.logging.EventLogger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A debug specific implementation for the event logger. It stores all the event logs in a list
 * instead of pushing them to Firebase.
 */
@Singleton
class DebugEventLogger @Inject constructor() : EventLogger {

  private val eventList = mutableListOf<EventLog>()

  override fun logEvent(eventLog: EventLog) {
    eventList.add(eventLog)
  }

  /** Returns list of event logs. */
  fun getEventList(): List<EventLog> = eventList
}
