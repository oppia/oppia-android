package org.oppia.domain.oppialogger.exceptions

import org.oppia.util.system.OppiaClock
import javax.inject.Inject

/** Handler for catching fatal exceptions before the defaultUncaughtExceptionHandler. */
class OppiaUncaughtExceptionHandler @Inject constructor(
  private var exceptionsController: ExceptionsController,
  private var oppiaClock: OppiaClock
) : Thread.UncaughtExceptionHandler {

  /** Logs an uncaught exception to the [exceptionsController]. */
  override fun uncaughtException(thread: Thread?, throwable: Throwable?) {
    try {
      exceptionsController.logFatalException(
        Exception(throwable),
        oppiaClock.getCurrentCalendar().timeInMillis
      )
    } catch (e: Exception) {
      e.printStackTrace()
    } finally {
      Thread.getDefaultUncaughtExceptionHandler().uncaughtException(thread, throwable)
    }
  }
}
