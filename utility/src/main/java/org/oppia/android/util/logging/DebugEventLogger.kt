package org.oppia.android.util.logging

import org.oppia.android.app.model.EventLog
import javax.inject.Inject
import javax.inject.Singleton

/**  A debug specific implementation for the event logger. */
@Singleton
class DebugEventLogger @Inject constructor() : EventLogger {

  private val eventList = ArrayList<EventLog>()

  override fun logEvent(eventLog: EventLog) {
    eventList.add(eventLog)
  }

  /** Returns eventList. */
  fun getEventList(): ArrayList<EventLog> = eventList
}
