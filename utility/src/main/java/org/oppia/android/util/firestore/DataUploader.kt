package org.oppia.android.util.firestore

import org.oppia.android.app.model.EventLog

/** Logger for uploading data bundles to Firestore. */
interface DataUploader {
  /**
   * Logs data to Firestore or saves them on disk.
   *
   * @param eventLog which contains all the relevant data to be reported.
   */
  fun uploadData(eventLog: EventLog)
}
