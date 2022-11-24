package org.oppia.android.util.logging

import org.oppia.android.app.model.EventLog

/** Logger for uploading analytics events to remote services. */
interface AnalyticsEventLogger {
  /**
   * Logs an event to remote services.
   *
   * @param eventLog refers to the log object which contains all the relevant data to be reported
   */
  fun logEvent(eventLog: EventLog)
}
