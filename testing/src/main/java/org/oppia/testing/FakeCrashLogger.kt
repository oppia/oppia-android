package org.oppia.testing

import java.lang.Exception
import org.oppia.testing.TestFirebaseModule.Companion.exceptionList
import org.oppia.util.firebase.CrashLogger

class FakeCrashLogger : CrashLogger {

  override fun logException(exception: Exception) {
    exceptionList.add(exception)
  }

  fun getMostRecentException(): Exception {
    val size = exceptionList.size
    return exceptionList[size - 1]
  }

  fun cleanExceptionList() {
    exceptionList.clear()
  }
}
