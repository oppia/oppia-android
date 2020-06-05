package org.oppia.util.firebase

import java.lang.Exception

/**
 * Interface that enables exception logging.
 */
interface CrashLogger {

  /** @param [exception] : the non fatal exception that is mostly caught in try-catch blocks.
   * This function basically facilitates custom exception logging to Crashlytics.
   */
  fun logException(exception: Exception)
}
