package org.oppia.android.util.firestore

import org.oppia.android.app.model.EventLog

/** Utility for creating Firestore Documents from [EventLog] objects. */
interface FirestoreDocumentCreator {
  /**
   * Converts an event log to a a hash map that is to be uploaded as a Firestore Document.
   *
   * @param eventLog refers to the log object which contains all the relevant data to be reported
   */
  fun createDocument(eventLog: EventLog): HashMap<String, Any?>
}
