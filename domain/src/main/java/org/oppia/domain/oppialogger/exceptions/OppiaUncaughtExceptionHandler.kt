package org.oppia.domain.oppialogger.exceptions

import org.oppia.util.logging.ConsoleLogger
import org.oppia.util.system.OppiaClock
import javax.inject.Inject

/** Handler for catching fatal exceptions before the defaultUncaughtExceptionHandler. */
class OppiaUncaughtExceptionHandler @Inject constructor(
  private val exceptionsController: ExceptionsController,
  private val oppiaClock: OppiaClock,
  private val consoleLogger: ConsoleLogger
) : Thread.UncaughtExceptionHandler {

  private lateinit var existingUncaughtExceptionHandler: Thread.UncaughtExceptionHandler

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

  /** Sets the [existingUncaughtExceptionHandler] equal to the [existingHandler] */
  fun setExistingExceptionHandler(existingHandler: Thread.UncaughtExceptionHandler) {
    this.existingUncaughtExceptionHandler = existingHandler
  }
}
