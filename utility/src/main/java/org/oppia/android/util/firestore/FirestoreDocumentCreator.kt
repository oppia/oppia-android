package org.oppia.android.util.firestore

import javax.inject.Singleton
import org.oppia.android.app.model.EventLog

/**
 * Utility for creating [Documents]s from [EventLog] objects.
 *
 * This class is only expected to be used by internal logging mechanisms and should not be called
 * directly.
 */
@Singleton
class FirestoreDocumentCreator {
  /**
   * Fills the specified document with the required key value pairs needed to
   * create a complete firestore document.
   */
  fun createDocument(dataObject: Any?): HashMap<String, Any?> {
    return hashMapOf()
  }

}