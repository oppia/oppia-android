package org.oppia.util.logging

import android.content.Context
import android.os.Bundle

/**
 * Logger for tracking events.
 * Note that this utility may later upload them to remote services
 * or log them to a file on disk.
 */
interface EventLogger {

  /**
   * Logs events to remote services or log them to a file on disk.
   *
   * @param context: refers to the context of the activity where event is happening.
   * @param bundle: refers to the bundle which contains all the relevant data to be reported
   * @param title: refers to the title of the event that will be logged
   */
  fun logEvent(context: Context, bundle: Bundle, title: String)
}
