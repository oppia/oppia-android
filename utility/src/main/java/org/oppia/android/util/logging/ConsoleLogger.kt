package org.oppia.android.util.logging

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.oppia.android.app.model.EventLog.ConsoleLoggerContext
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.threading.BlockingDispatcher
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import javax.inject.Inject
import javax.inject.Singleton

/** Wrapper class for Android logcat and file logging. All logs in the app should use this class. */
@Singleton
class ConsoleLogger @Inject constructor(
  context: Context,
  @BlockingDispatcher private val blockingDispatcher: CoroutineDispatcher,
  @EnableConsoleLog private val enableConsoleLog: Boolean,
  @EnableFileLog private val enableFileLog: Boolean,
  @GlobalLogLevel private val globalLogLevel: LogLevel,
  private val machineLocale: OppiaLocale.MachineLocale
) {
  private val blockingScope = CoroutineScope(blockingDispatcher)
  private val logDirectory = File(context.filesDir, "oppia_app.log")

  private val _logErrorMessagesFlow = MutableSharedFlow<ConsoleLoggerContext>()
  /**
   * A flow that emits a [ConsoleLoggerContext] when a error message is logged.
   */
  val logErrorMessagesFlow: SharedFlow<ConsoleLoggerContext> = _logErrorMessagesFlow

  private var printWriter: PrintWriter? = null

  /** Logs a verbose message with the specified tag. */
  fun v(tag: String, msg: String) {
    writeLog(LogLevel.VERBOSE, tag, msg)
  }

  /** Logs a verbose message with the specified tag, message and exception. */
  fun v(tag: String, msg: String, tr: Throwable) {
    writeError(LogLevel.VERBOSE, tag, msg, tr)
  }

  /** Logs a debug message with the specified tag. */
  fun d(tag: String, msg: String) {
    writeLog(LogLevel.DEBUG, tag, msg)
  }

  /** Logs a debug message with the specified tag, message and exception. */
  fun d(tag: String, msg: String, tr: Throwable) {
    writeError(LogLevel.DEBUG, tag, msg, tr)
  }

  /** Logs a info message with the specified tag. */
  fun i(tag: String, msg: String) {
    writeLog(LogLevel.INFO, tag, msg)
  }

  /** Logs a info message with the specified tag, message and exception. */
  fun i(tag: String, msg: String, tr: Throwable) {
    writeError(LogLevel.INFO, tag, msg, tr)
  }

  /** Logs a warn message with the specified tag. */
  fun w(tag: String, msg: String) {
    writeLog(LogLevel.WARNING, tag, msg)
  }

  /** Logs a warn message with the specified tag, message and exception. */
  fun w(tag: String, msg: String, tr: Throwable) {
    writeError(LogLevel.WARNING, tag, msg, tr)
  }

  /** Logs an error message with the specified tag. */
  fun e(tag: String, msg: String) {
    writeLog(LogLevel.ERROR, tag, msg)
  }

  /** Logs an error message with the specified tag, message and exception. */
  fun e(tag: String, msg: String, tr: Throwable?) {
    writeError(LogLevel.ERROR, tag, msg, tr)
  }

  private fun isLogEnable(logLevel: LogLevel): Boolean {
    return globalLogLevel.logLevel <= logLevel.logLevel
  }

  private fun writeLog(logLevel: LogLevel, tag: String, log: String) {
    writeInternal(logLevel, tag, log)
  }

  private fun writeError(logLevel: LogLevel, tag: String, log: String, tr: Throwable?) {
    writeInternal(logLevel, tag, "$log\n${Log.getStackTraceString(tr)}")
  }

  private fun writeInternal(logLevel: LogLevel, tag: String, fullLog: String) {
    if (!isLogEnable(logLevel)) {
      return
    }
    if (enableConsoleLog) {
      Log.println(logLevel.logLevel, tag, fullLog)
    }
    if (enableFileLog) {
      logToFileInBackground(
        "${machineLocale.computeCurrentTimeString()}\t${logLevel.name}/$tag: $fullLog"
      )
    }

    // Add the log to the error message flow so it can be logged to firebase.
    blockingScope.launch {
      // Only log error messages to firebase.
      if (logLevel == LogLevel.ERROR) {
        _logErrorMessagesFlow.emit(
          ConsoleLoggerContext.newBuilder()
            .setLogLevel(logLevel.toString())
            .setLogTag(tag)
            .setFullErrorLog(fullLog)
            .build()
        )
      }
    }
  }

  /**
   * Writes the specified text line to file in a background thread to ensure that saving messages
   * doesn't block the main thread. A blocking dispatcher is used to ensure messages are written
   * in order.
   */
  private fun logToFileInBackground(text: String) {
    blockingScope.launch {
      if (printWriter == null) {
        printWriter = PrintWriter(FileWriter(logDirectory, true)) // Open in append mode.
      }
      printWriter?.println(text)
      printWriter?.flush()
    }
  }

  /** Close the log file when logging is finished. */
  private fun closeLogFile() {
    printWriter?.close()
    printWriter = null
  }
}
