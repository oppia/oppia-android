package org.oppia.android.util.logging.firebase

import org.oppia.android.app.model.EventLog

/** Logger for debug implementations of Firestore functionality. */
interface DebugFirestoreEventLogger {
  /**
   * Converts eventLogs to Firestore documents and uploads or save them on disk.
   *
   * @param eventLog which contains all the relevant data to be reported.
   */
  fun uploadEvent(eventLog: EventLog)

  /** Returns the list of all [EventLog]s logged since the app opened. */
  fun getEventList(): List<EventLog>
}
