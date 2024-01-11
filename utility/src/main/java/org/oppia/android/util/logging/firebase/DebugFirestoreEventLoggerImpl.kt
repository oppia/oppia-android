package org.oppia.android.util.logging.firebase

import org.oppia.android.app.model.EventLog
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A debug implementation of [FirestoreEventLogger] used in developer-only builds of the app.
 *
 * It forwards events to a production [FirestoreEventLogger] for real logging, but it also records logged
 * events for later retrieval (e.g. via [getEventList]).
 */
@Singleton
class DebugFirestoreEventLoggerImpl @Inject constructor(
  private val realEventLogger: FirestoreEventLoggerProdImpl
) : FirestoreEventLogger {
  private val eventList = CopyOnWriteArrayList<EventLog>()

  override fun uploadEvent(eventLog: EventLog) {
    eventList.add(eventLog)
    realEventLogger.uploadEvent(eventLog)
  }

  /** Returns the list of all [EventLog]s logged for Firestore. */
  fun getEventList(): List<EventLog> = eventList

  /** Returns the most recently logged event. */
  fun getMostRecentEvent(): EventLog = getEventList().last()

  /** Clears all the events that are currently logged. */
  fun clearAllEvents() = eventList.clear()
}
