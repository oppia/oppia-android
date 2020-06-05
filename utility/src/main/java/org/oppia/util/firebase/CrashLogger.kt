package org.oppia.util.firebase

import java.lang.Exception

/**
 * Interface that enables exception logging.
 */
interface CrashLogger {

  fun logException(exception: Exception)
}
