package org.oppia.util.logging

import android.content.Context
import org.oppia.app.model.EventLog

/**
 * Logger for tracking events.
 * Note that this utility may later upload them to remote services
 */
interface EventLogger {

  /**
   * Logs events to remote services.
   *
   * @param context: refers to the context of the activity where event is happening.
   * @param eventLog: refers to the log object which contains all the relevant data to be reported.
   */
  fun logEvent(context: Context, eventLog: EventLog)
}
