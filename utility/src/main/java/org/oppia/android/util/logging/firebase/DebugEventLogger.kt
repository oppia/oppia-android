package org.oppia.android.util.logging.firebase

import java.util.concurrent.CopyOnWriteArrayList
import org.oppia.android.app.model.EventLog
import org.oppia.android.util.logging.EventLogger
import javax.inject.Inject
import javax.inject.Singleton

// TODO: update kdoc.
/**
 * A debug specific implementation for the event logger. It stores all the event logs in a list
 * instead of pushing them to Firebase.
 */
@Singleton
class DebugEventLogger @Inject constructor(
  factory: FirebaseEventLogger.Factory
) : EventLogger {
  private val realEventLogger by lazy { factory.create() }
  private val eventList = CopyOnWriteArrayList<EventLog>()

  override fun logEvent(eventLog: EventLog) {
    eventList.add(eventLog)
    realEventLogger.logEvent(eventLog)
  }

  /** Returns list of event logs. */
  fun getEventList(): List<EventLog> = eventList
}
