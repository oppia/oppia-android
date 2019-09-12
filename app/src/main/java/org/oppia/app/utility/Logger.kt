package org.oppia.app.utility

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import org.oppia.app.application.ApplicationContext
import org.oppia.util.threading.BlockingDispatcher
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/** This Wrapper class is for Android Logs and to perform file logging. */
@Singleton
class Logger @Inject constructor(@ApplicationContext context: Context,@BlockingDispatcher private val blockingDispatcher: CoroutineDispatcher) {

  private val blockingScope = CoroutineScope(blockingDispatcher)

  private val ENABLE_CONSOLE_LOG = true
  private val ENABLE_FILE_LOG = true
  private val GLOBAL_LOG_LEVEL = LogLevel.VERBOSE
  private val LOG_DIRECTORY = File(context.filesDir, "oppia_app.log")

  private enum class LogLevel private constructor(val logLevel: Int) {
    VERBOSE(Log.VERBOSE),
    DEBUG(Log.DEBUG),
    INFO(Log.INFO),
    WARNING(Log.WARN),
    ERROR(Log.ERROR),
    ASSERT(Log.ASSERT)
  }

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
  fun e(tag: String, msg: String, tr: Throwable) {
    writeError(LogLevel.ERROR, tag, msg, tr)
  }

  private fun isLogEnable(logLevel: LogLevel): Boolean {
    return GLOBAL_LOG_LEVEL.logLevel < logLevel.logLevel
  }

  private fun writeLog(logLevel: LogLevel, tag: String, log: String) {
    writeInternal(logLevel, tag, log)
  }

  private fun writeError(logLevel: LogLevel, tag: String, log: String, tr: Throwable) {
    writeInternal(logLevel, tag, "$log\n${Log.getStackTraceString(tr)}")
  }

  private fun writeInternal(logLevel: LogLevel, tag: String, fullLog: String) {
    if (isLogEnable(logLevel) && ENABLE_CONSOLE_LOG) {
      Log.println(logLevel.logLevel, tag, fullLog)
    }
    if (isLogEnable(logLevel) && ENABLE_FILE_LOG) {
      val msg = "${Calendar.getInstance().time}\t${logLevel.name}/$tag: $fullLog"

      // To mange background threads
      blockingScope.launch { write(msg) }
    }
  }

  private suspend fun write(text: String) {
    println("debug ${text} ${Thread.currentThread().name}")
    LOG_DIRECTORY.printWriter().use { out -> out.println(text) }

  }
}
