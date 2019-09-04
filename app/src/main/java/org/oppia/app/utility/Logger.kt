package org.oppia.app.utility

import android.content.Context
import android.util.Log
import org.oppia.app.application.ApplicationContext
import org.oppia.app.application.OppiaApplication

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

/** Wrapper class for Android Log. */
class Logger @Inject constructor(@ApplicationContext context: Context) {

  private val ENABLE_CONSOLE_LOG = true
  private val ENABLE_FILE_LOG = true
  private val GLOBAL_LOG_LEVEL = LogLevel.VERBOSE
  private val LOG_DIRECTORY =
    context.getExternalFilesDir(null).toString() + File.separator + "OppiaAppLog" + File.separator

  private enum class LogLevel private constructor(val logLevel: Int) {
    VERBOSE(Log.VERBOSE),
    DEBUG(Log.DEBUG),
    INFO(Log.INFO),
    WARNING(Log.WARN),
    ERROR(Log.ERROR),
    ASSERT(Log.ASSERT)
  }

  /**
   *
   * @param tag Used to identify the source of a log message.  It usually identifies
   * the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   */
  fun v(tag: String, msg: String) {
    write(LogLevel.VERBOSE, tag, msg)
  }

  /**
   *
   * @param tag Used to identify the source of a log message.  It usually identifies
   * the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   * @param tr  An exception to log
   */
  fun v(tag: String, msg: String, tr: Throwable) {
    write(LogLevel.VERBOSE, tag, msg, tr)
  }

  /**
   *
   * @param tag Used to identify the source of a log message.  It usually identifies
   * the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   */
  fun d(tag: String, msg: String) {
    write(LogLevel.DEBUG, tag, msg)
  }

  /**
   *
   * @param tag Used to identify the source of a log message.  It usually identifies
   * the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   * @param tr  An exception to log
   */
  fun d(tag: String, msg: String, tr: Throwable) {
    write(LogLevel.DEBUG, tag, msg, tr)
  }

  /**
   *
   * @param tag Used to identify the source of a log message.  It usually identifies
   * the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   */
  fun i(tag: String, msg: String) {
    write(LogLevel.INFO, tag, msg)
  }

  /**
   *
   * @param tag Used to identify the source of a log message.  It usually identifies
   * the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   * @param tr  An exception to log
   */
  fun i(tag: String, msg: String, tr: Throwable) {
    write(LogLevel.INFO, tag, msg, tr)
  }

  /**
   *
   * @param tag Used to identify the source of a log message.  It usually identifies
   * the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   */
  fun w(tag: String, msg: String) {
    write(LogLevel.WARNING, tag, msg)
  }

  /**
   *
   * @param tag Used to identify the source of a log message.  It usually identifies
   * the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   * @param tr  An exception to log
   */
  fun w(tag: String, msg: String, tr: Throwable) {
    write(LogLevel.WARNING, tag, msg, tr)
  }

  /**
   *
   * @param tag Used to identify the source of a log message.  It usually identifies
   * the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   */
  fun e(tag: String, msg: String) {
    write(LogLevel.ERROR, tag, msg)
  }

  /**
   *
   * @param tag Used to identify the source of a log message.  It usually identifies
   * the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   * @param tr  An exception to log
   */
  fun e(tag: String, msg: String, tr: Throwable) {
    write(LogLevel.ERROR, tag, msg, tr)
  }

  private fun isLogEnable(logLevel: LogLevel): Boolean {
    return GLOBAL_LOG_LEVEL.logLevel < logLevel.logLevel
  }

  private fun write(logLevel: LogLevel, tag: String, log: String) {
    if (isLogEnable(logLevel) && ENABLE_CONSOLE_LOG) {
      Log.println(logLevel.logLevel, tag, log)
    }
    if (isLogEnable(logLevel) && ENABLE_FILE_LOG) {
      val msg = Calendar.getInstance().time.toString() + "\t" + logLevel.name + "/" + tag + ": " + log
      write(msg)
    }
  }

  private fun write(logLevel: LogLevel, tag: String, log: String, tr: Throwable) {
    if (isLogEnable(logLevel) && ENABLE_CONSOLE_LOG) {
      Log.println(logLevel.logLevel, tag, log + "\n" + Log.getStackTraceString(tr))
    }
    if (isLogEnable(logLevel) && ENABLE_FILE_LOG) {
      val msg =
        Calendar.getInstance().time.toString() + "\t" + logLevel.name + "/" + tag + ": " + log + "\n" + Log.getStackTraceString(
          tr
        )
      write(msg)
    }
  }

  private fun write(text: String) {
    var out: BufferedWriter? = null
    var filePath = LOG_DIRECTORY
    try {

      val df = SimpleDateFormat("dd_MMM_yyyy", Locale.ENGLISH)
      val formattedDate = df.format(System.currentTimeMillis())

      if (!File(LOG_DIRECTORY).exists())
        File(LOG_DIRECTORY).mkdir()

      filePath = "$LOG_DIRECTORY$formattedDate.log"
      if (!File(filePath).exists())
        File(filePath).createNewFile()

      val fStream = FileWriter(filePath, true)
      out = BufferedWriter(fStream)
      out.write(text + "\n")
      out.flush()
    } catch (e: IOException) {
      Log.e("Log", "Path:$filePath")
      e.printStackTrace()
    } finally {
      try {
        out?.close()

      } catch (e: IOException) {
        e.printStackTrace()
      }

    }
  }

}
