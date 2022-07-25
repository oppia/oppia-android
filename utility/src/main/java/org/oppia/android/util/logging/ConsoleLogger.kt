package org.oppia.android.util.logging

import android.content.Context
import android.util.Log
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.threading.BackgroundDispatcher

/** Wrapper class for Android logcat and file logging. All logs in the app should use this class. */
@Singleton
@OptIn(ObsoleteCoroutinesApi::class)
class ConsoleLogger @Inject constructor(
  private val context: Context,
  @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher,
  @EnableConsoleLog private val enableConsoleLog: Boolean,
  @EnableFileLog private val enableFileLog: Boolean,
  @GlobalLogLevel private val globalLogLevel: LogLevel,
  private val machineLocale: OppiaLocale.MachineLocale
) {
  private val fileLoggerCommandQueue by lazy { createBackgroundLoggingActor() }

  /** Logs a verbose message with the specified tag.*/
  fun v(tag: String, msg: String) {
    writeLog(LogLevel.VERBOSE, tag, msg)
  }

  /** Logs a verbose message with the specified tag, message and exception.*/
  fun v(tag: String, msg: String, tr: Throwable) {
    writeError(LogLevel.VERBOSE, tag, msg, tr)
  }

  /** Logs a debug message with the specified tag*/
  fun d(tag: String, msg: String) {
    writeLog(LogLevel.DEBUG, tag, msg)
  }

  /** Logs a debug message with the specified tag, message and exception.*/
  fun d(tag: String, msg: String, tr: Throwable) {
    writeError(LogLevel.DEBUG, tag, msg, tr)
  }

  /** Logs a info message with the specified tag.*/
  fun i(tag: String, msg: String) {
    writeLog(LogLevel.INFO, tag, msg)
  }

  /** Logs a info message with the specified tag, message and exception.*/
  fun i(tag: String, msg: String, tr: Throwable) {
    writeError(LogLevel.INFO, tag, msg, tr)
  }

  /** Logs a warn message with the specified tag.*/
  fun w(tag: String, msg: String) {
    writeLog(LogLevel.WARNING, tag, msg)
  }

  /** Logs a warn message with the specified tag, message and exception.*/
  fun w(tag: String, msg: String, tr: Throwable) {
    writeError(LogLevel.WARNING, tag, msg, tr)
  }

  /** Logs a error message with the specified tag.*/
  fun e(tag: String, msg: String) {
    writeLog(LogLevel.ERROR, tag, msg)
  }

  /** Logs a error message with the specified tag, message and exception.*/
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
      val enqueuedWriteToFile = fileLoggerCommandQueue.trySend(
        "${machineLocale.computeCurrentTimeString()}\t${logLevel.name}/$tag: $fullLog"
      ).isSuccess
      if (!enqueuedWriteToFile) {
        Log.println(Log.ERROR, "ConsoleLogger", "Failed to write previous line to file.")
      }
    }
  }

  /**
   * Writes the specified text line to file in a background thread to ensure that saving messages
   * don't block the main thread.
   *
   * A synchronized [SendChannel] is used to ensure messages are written in order.
   */
  private fun createBackgroundLoggingActor(): SendChannel<String> {
    val logDirectory = File(context.filesDir, "oppia_app.log")
    return CoroutineScope(backgroundDispatcher).actor(capacity = Channel.UNLIMITED) {
      for (loggedMessage in channel) {
        logDirectory.printWriter().use { out -> out.println(loggedMessage) }
      }
    }
  }
}
