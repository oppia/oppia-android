package org.oppia.android.util.firestore

import org.oppia.android.app.model.EventLog

/** Utility for creating Firestore Documents from [EventLog] objects. */
interface FirestoreDocumentCreator {
  fun createDocument(eventLog: EventLog): HashMap<String, Any?>
}
