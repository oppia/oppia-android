package org.oppia.android.util.firestore

import org.oppia.android.app.model.EventLog

/** Logger for saving data bundles to Firestore. */
interface DataLogger {
  /**
   * Logs data to Firestore or saves them on disk.
   *
   * @param eventLog which contains all the relevant data to be reported.
   */
  fun saveData(eventLog: EventLog)
}