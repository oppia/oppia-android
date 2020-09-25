package org.oppia.android.testing

import org.oppia.android.util.logging.ExceptionLogger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.Exception

/** A test specific fake for the exception logger. */
@Singleton
class FakeExceptionLogger @Inject constructor() : ExceptionLogger {
  private val exceptionList = mutableListOf<Exception>()

  override fun logException(exception: Exception) {
    exceptionList.add(exception)
  }

  /** Returns the most recently logged exception. */
  fun getMostRecentException(): Exception = exceptionList.last()

  /** Clears all the exceptions that are currently logged. */
  fun clearAllExceptions() = exceptionList.clear()

  /** Checks if a certain exception has been logged or not. */
  fun hasExceptionLogged(exception: Exception): Boolean = exceptionList.contains(exception)

  /** Returns true if there are no exceptions logged. */
  fun noExceptionsPresent(): Boolean = exceptionList.isEmpty()
}
