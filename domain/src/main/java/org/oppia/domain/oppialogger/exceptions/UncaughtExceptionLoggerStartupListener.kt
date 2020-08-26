package org.oppia.domain.oppialogger.exceptions

import org.oppia.domain.oppialogger.ApplicationStartupListener
import org.oppia.util.logging.ConsoleLogger
import org.oppia.util.system.OppiaClock
import javax.inject.Inject

/** Handler for catching fatal exceptions before the defaultUncaughtExceptionHandler. */
class UncaughtExceptionLoggerStartupListener @Inject constructor(
  private val exceptionsController: ExceptionsController,
  private val oppiaClock: OppiaClock,
  private val consoleLogger: ConsoleLogger
) : Thread.UncaughtExceptionHandler, ApplicationStartupListener {

  private lateinit var existingUncaughtExceptionHandler: Thread.UncaughtExceptionHandler

  override fun onCreate() {
    existingUncaughtExceptionHandler = Thread.currentThread().uncaughtExceptionHandler
    Thread.currentThread().uncaughtExceptionHandler = this
  }

  /** Logs an uncaught exception to the [exceptionsController]. */
  override fun uncaughtException(thread: Thread?, throwable: Throwable?) {
    try {
      exceptionsController.logFatalException(
        Exception(throwable),
        oppiaClock.getCurrentCalendar().timeInMillis
      )
    } catch (e: Exception) {
      consoleLogger.e("OPPIA_EXCEPTION_HANDLER", "Problem in logging exception", e)
    } finally {
      existingUncaughtExceptionHandler.uncaughtException(thread, throwable)
    }
  }
}
