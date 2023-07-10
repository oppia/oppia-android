package org.oppia.android.util.firestore

/** Logger for saving data bundles to Firestore. */
interface DataLogger {
  /**
   * Logs data to Firestore.
   *
   * @param dataObject refers to the data object which contains all the relevant data to be reported.
   */
  fun saveData(dataObject: Any?)
}