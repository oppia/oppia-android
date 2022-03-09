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

  /**
   * Logs previously cached events to remote services via background service.
   *
   * @param eventLog: refers to the log object which contains all the relevant data to be reported.
   * Note: This method should only be used when we're logging events in a loop as it might effect
   * the sync status mechanism in production.
   */
  fun logCachedEvent(eventLog: EventLog)
}
