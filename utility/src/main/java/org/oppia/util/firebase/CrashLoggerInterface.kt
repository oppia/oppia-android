package org.oppia.util.firebase

import java.lang.Exception

interface CrashLoggerInterface {

  fun logException(exception: Exception)

  fun logMessage(message: String)

  fun setUserIdentifier(identifier: String)
}
