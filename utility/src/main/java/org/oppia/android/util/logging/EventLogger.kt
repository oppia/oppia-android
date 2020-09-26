package org.oppia.android.util.logging

import org.oppia.android.app.model.EventLog

/**
 * Logger for tracking events.
 * Note that this utility may later upload them to remote services
 */
interface EventLogger {

  /**
   * Logs events to remote services.
   *
   * @param eventLog: refers to the log object which contains all the relevant data to be reported.
   */
  fun logEvent(eventLog: EventLog)
}
