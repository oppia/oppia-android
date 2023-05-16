package org.oppia.android.util.logging

import org.oppia.android.app.model.EventLog

/**
 * Logger for uploading analytics events to remote services. New instances are created using the
 * implementation's [Factory].
 */
interface AnalyticsEventLogger {
  /**
   * Logs an event to remote services.
   *
   * @param eventLog refers to the log object which contains all the relevant data to be reported
   */
  fun logEvent(eventLog: EventLog)

  /** Application-injectable factory for creating new instances of this [AnalyticsEventLogger]. */
  interface Factory {
    /** Returns a new instance of the implementation [AnalyticsEventLogger]. */
    fun create(): AnalyticsEventLogger
  }
}
