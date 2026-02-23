package org.oppia.android.testing

import org.oppia.android.util.logging.ExceptionLogger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.Exception

/** A test specific fake for the exception logger. */
@Singleton
class FakeExceptionLogger @Inject constructor() : ExceptionLogger {
  private val exceptionList = mutableListOf<Exception>()
  private var failureToThrow: Exception? = null

  override fun logException(exception: Exception) {
    val possibleFailure = failureToThrow
    if (possibleFailure != null) throw possibleFailure
    exceptionList.add(exception)
  }

  /**
   * Sets an [Exception] to throw the next time(s) [logPerformanceMetric] are called, or `null` if
   * failure mode should be cleared.
   */
  fun setFailure(failure: Exception?) {
    failureToThrow = failure
  }

  /** Returns the most recently logged exception. */
  fun getMostRecentException(): Exception = exceptionList.last()

  /** Returns the most recently logged exceptions. */
  fun getMostRecentExceptions(count: Int): List<Exception> = exceptionList.takeLast(count)

  /** Clears all the exceptions that are currently logged. */
  fun clearAllExceptions() = exceptionList.clear()

  /** Checks if a certain exception has been logged or not. */
  fun hasExceptionLogged(exception: Exception): Boolean = exceptionList.contains(exception)

  /** Returns true if there are no exceptions logged. */
  fun noExceptionsPresent(): Boolean = exceptionList.isEmpty()

  /** Returns the number of exceptions logged to date (and not cleared by [clearAllExceptions]). */
  fun getExceptionCount(): Int = exceptionList.size
}
