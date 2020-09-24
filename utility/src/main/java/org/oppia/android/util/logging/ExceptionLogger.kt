package org.oppia.util.logging

import java.lang.Exception

/**
 * Logger for tracking caught exceptions.
 * Note that this utility may later upload the exceptions to remote services
 * or log the exception to a file on disk.
 */
interface ExceptionLogger {

  /**
   * Logs exceptions to remote services or log them to a file on disk.
   *
   * @param exception: non-fatal exception to be logged
   */
  fun logException(exception: Exception)
}
