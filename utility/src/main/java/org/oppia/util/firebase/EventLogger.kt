package org.oppia.util.firebase

import android.content.Context
import android.os.Bundle

/**
 * Interface that enables event logging.
 */
interface EventLogger {

  /**
   * @param [context] : refers to the context of the activity where event is happening.
   * @param [bundle] : refers to the bundle which contains all the relevant data to be reported
   * @param [title] : refers to the title of the event that will be logged
   * This function facilitates event logging to Firebase Analytics.
   */
  fun logEvent(context: Context, bundle: Bundle, title: String)
}
