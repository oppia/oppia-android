package org.oppia.android.domain.oppialogger.exceptions

import java.io.PrintWriter
import java.io.StringWriter
import org.oppia.android.domain.oppialogger.ApplicationStartupListener
import org.oppia.android.util.logging.ConsoleLogger
import javax.inject.Inject
import javax.inject.Provider

/** Handler for catching fatal exceptions before the defaultUncaughtExceptionHandler. */
class UncaughtExceptionLoggerStartupListener @Inject constructor(
  private val exceptionsControllerProvider: Provider<ExceptionsController>,
  private val consoleLogger: ConsoleLogger // Should be safe for early app access.
) : Thread.UncaughtExceptionHandler, ApplicationStartupListener {

  private var existingUncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null
  private var canLogExceptions: Boolean = false

  override fun onCreateStarted() {
    // This should be set up immediately to try and capture exceptions that occur early, but it may
    // not be safe to log them until after app initialization completes. This will catch *all*
    // uncaught exceptions, not just those from the main thread.
    existingUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler(this)
  }

  override fun onCompletedInitialization() {
    canLogExceptions = true
  }

  override fun uncaughtException(thread: Thread, throwable: Throwable) {
    try {
      consoleLogger.w("OPPIA_EXCEPTION_HANDLER", "Caught uncaught exception:", throwable)
      if (canLogExceptions) {
        exceptionsControllerProvider.get().logFatalException(Exception(throwable))
      } else {
        consoleLogger.e(
          "OPPIA_EXCEPTION_HANDLER",
          "Skipped logging exception due to app not being fully initialized yet."
        )
      }
    } catch (e: Exception) {
      consoleLogger.e("OPPIA_EXCEPTION_HANDLER", "Problem in logging exception", e)
    } finally {
      existingUncaughtExceptionHandler?.uncaughtException(thread, throwable)
    }
  }
}
