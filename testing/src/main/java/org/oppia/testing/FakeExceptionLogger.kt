package org.oppia.testing

import org.oppia.util.logging.ExceptionLogger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.Exception

/** A test specific fake for the exception logger. */
@Singleton
class FakeExceptionLogger @Inject constructor() : ExceptionLogger {
  private var exceptionList = ArrayList<Exception>()

  override fun logException(exception: Exception) {
    exceptionList.add(exception)
  }

  /** Returns the most recently logged exception. */
  fun getMostRecentException(): Exception = exceptionList.last()

  /** Clears all the exceptions that are currently logged. */
  fun clearAllExceptions() = exceptionList.clear()
}
