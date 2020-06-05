package org.oppia.testing

import java.lang.Exception
import org.oppia.testing.TestLogReportingModule.Companion.exceptionList
import org.oppia.util.firebase.CrashLogger

/**
 * A test specific fake crash logger that adds the logged exception to a list of exceptions.
 */
class FakeCrashLogger : CrashLogger {

  override fun logException(exception: Exception) {
    exceptionList.add(exception)
  }

  /** This is used to get the most recent exception present in the list of exceptions. */
  fun getMostRecentException(): Exception {
    val size = exceptionList.size
    return if (size > 0) {
      exceptionList[size - 1]
    } else {
      exceptionList[size]
    }
  }

  /** This is used to clear all the exceptions present in the list. */
  fun clearAllExceptions() {
    exceptionList.clear()
  }
}
