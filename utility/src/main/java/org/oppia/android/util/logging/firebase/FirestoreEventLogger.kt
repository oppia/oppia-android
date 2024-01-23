package org.oppia.android.util.logging.firebase

import org.oppia.android.app.model.EventLog

/** Logger for uploading data bundles to Firestore. */
interface FirestoreEventLogger {
  /**
   * Converts eventLogs to Firestore documents and uploads or save them on disk.
   *
   * @param eventLog which contains all the relevant data to be reported
   */
  fun uploadEvent(eventLog: EventLog)
}
