package org.oppia.android.domain.oppialogger.exceptions

import org.oppia.android.domain.oppialogger.ApplicationStartupListener
import org.oppia.android.util.logging.ConsoleLogger
import javax.inject.Inject

/** Handler for catching fatal exceptions before the defaultUncaughtExceptionHandler. */
class UncaughtExceptionLoggerStartupListener @Inject constructor(
  private val exceptionsController: ExceptionsController,
  private val consoleLogger: ConsoleLogger
) : Thread.UncaughtExceptionHandler, ApplicationStartupListener {

  private var existingUncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null

  /** Sets up the uncaught exception handler to [UncaughtExceptionLoggerStartupListener]. */
  override fun onCreate() {
    existingUncaughtExceptionHandler = Thread.currentThread().uncaughtExceptionHandler
    Thread.currentThread().uncaughtExceptionHandler = this
  }

  /** Logs an uncaught exception to the [exceptionsController]. */
  override fun uncaughtException(thread: Thread?, throwable: Throwable?) {
    try {
      exceptionsController.logFatalException(Exception(throwable))
    } catch (e: Exception) {
      consoleLogger.e("OPPIA_EXCEPTION_HANDLER", "Problem in logging exception", e)
    } finally {
      existingUncaughtExceptionHandler?.uncaughtException(thread, throwable)
    }
  }
}
