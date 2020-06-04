package org.oppia.util.firebase

import java.lang.Exception

interface CrashLogger {

  fun logException(exception: Exception)
}
