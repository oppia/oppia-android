package org.oppia.domain.oppialogger.crashlytics

import androidx.lifecycle.LiveData
import org.oppia.app.model.ExceptionLog
import org.oppia.app.model.OppiaExceptionLogs
import org.oppia.data.persistence.PersistentCacheStore
import org.oppia.domain.oppialogger.ExceptionLogStorageCacheSize
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProviders
import org.oppia.util.logging.ConsoleLogger
import org.oppia.util.logging.ExceptionLogger
import org.oppia.util.networking.NetworkConnectionUtil
import org.oppia.util.system.OppiaClock
import javax.inject.Inject

class ExceptionsController @Inject constructor(
  private val exceptionLogger: ExceptionLogger,
  private val dataProviders: DataProviders,
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val consoleLogger: ConsoleLogger,
  private val networkConnectionUtil: NetworkConnectionUtil,
  private val oppiaClock: OppiaClock,
  @ExceptionLogStorageCacheSize private val exceptionLogStorageCacheSize: Int
) {
  private val exceptionLogStore =
    cacheStoreFactory.create("exception_logs", OppiaExceptionLogs.getDefaultInstance())

  lateinit var exceptionCause: Throwable

  fun logException(exception: Exception, timestamp: Long) {
    uploadOrCacheExceptionLog(exceptionToExceptionLog(exception, timestamp))
  }

  /**
   * Checks network connectivity of the device.
   *
   * Saves the [exceptionLog] to the [exceptionLogStore] in the absence of it.
   * Uploads to remote service in the presence of it.
   */
  private fun uploadOrCacheExceptionLog(exceptionLog: ExceptionLog) {
    when (networkConnectionUtil.getCurrentConnectionStatus()) {
      NetworkConnectionUtil.ConnectionStatus.NONE -> cacheExceptionLog(exceptionLog)
      else -> exceptionLogger.logException(exceptionLogToException(exceptionLog))
    }
  }

  /** Returns an [ExceptionLog] from an [exception]. */
  private fun exceptionToExceptionLog(exception: Exception, timestamp: Long): ExceptionLog {
    val exceptionLogBuilder = ExceptionLog.newBuilder()
    val exceptionLogCauseBuilder = ExceptionLog.newBuilder()
    exceptionLogBuilder.message = exception.message
    exceptionLogBuilder.timestamp = timestamp
    val stackTraceSize = exception.stackTrace.size
    buildStackTraceElement(stackTraceSize, exceptionLogBuilder, exception)
    exception.cause?.message?.let {
      exceptionLogCauseBuilder.setMessage(it)
    }
    val causeStackTraceSize = exception.cause?.stackTrace?.size
    if (causeStackTraceSize != null) {
      buildStackTraceElement(causeStackTraceSize, exceptionLogCauseBuilder, exception)
    }
    exceptionLogBuilder.cause = exceptionLogCauseBuilder.build()
    return exceptionLogBuilder.build()
  }

  /** Returns an [Exception] from an [exceptionLog]. */
  private fun exceptionLogToException(exceptionLog: ExceptionLog): Exception {
    val exceptionMessage = exceptionLog.message
    exceptionCause = if (exceptionLog.cause.message != "") {
      Throwable(exceptionLog.cause.message)
    } else {
      Throwable()
    }
    exceptionCause.stackTrace = createErrorStackTrace(exceptionLog.cause)
    val exception = Exception(exceptionMessage, exceptionCause)
    exception.stackTrace = createErrorStackTrace(exceptionLog)
    return exception
  }

  /** Builds the [ExceptionLog.StackTraceElement] for an [exception] with relevant data and adds it to the [exceptionLogBuilder]. */
  private fun buildStackTraceElement(
    stackTraceSize: Int,
    exceptionLogBuilder: ExceptionLog.Builder,
    exception: Exception
  ) {
    for (i in 0 until stackTraceSize) {
      val stackTraceElement = ExceptionLog.StackTraceElement.newBuilder()
      stackTraceElement.fileName = exception.stackTrace[i].fileName
      stackTraceElement.methodName = exception.stackTrace[i].methodName
      stackTraceElement.lineNumber = exception.stackTrace[i].lineNumber
      stackTraceElement.declaringClass = exception.stackTrace[i].className
      exceptionLogBuilder.addStacktrace(i, stackTraceElement.build())
    }
  }

  /** Returns exception stacktrace for the [exceptionLog]. */
  private fun createErrorStackTrace(exceptionLog: ExceptionLog): Array<StackTraceElement> {
    return Array(
      exceptionLog.stacktraceCount,
      init = { i: Int ->
        StackTraceElement(
          exceptionLog.stacktraceList[i].declaringClass,
          exceptionLog.stacktraceList[i].methodName,
          exceptionLog.stacktraceList[i].fileName,
          exceptionLog.stacktraceList[i].lineNumber
        )
      }
    )
  }

  /**
   * Adds an exception to the storage.
   *
   * At first, it checks if the size of the store isn't exceeding [exceptionLogStorageCacheSize].
   * If the limit is exceeded then the least recent exception is removed from the [exceptionLogStore].
   * After this, the [exceptionLog] is added to the store.
   * */
  private fun cacheExceptionLog(exceptionLog: ExceptionLog) {
    exceptionLogStore.storeDataAsync(true) { oppiaExceptionLogs ->
      val storeSize = oppiaExceptionLogs.exceptionLogList.size
      if (storeSize + 1 > exceptionLogStorageCacheSize) {
        val exceptionLogRemovalIndex = getLeastRecentExceptionIndex(oppiaExceptionLogs)
        if (exceptionLogRemovalIndex != null) {
          return@storeDataAsync oppiaExceptionLogs.toBuilder()
            .removeExceptionLog(exceptionLogRemovalIndex)
            .addExceptionLog(exceptionLog)
            .build()
        } else {
          // TODO (#1433): Refactoring for logging exceptions to both console and exception loggers.
          val exception =
            NullPointerException(
              "Least Recent Exception index absent -- ExceptionLogCacheStoreSize is 0"
            )
          consoleLogger.e("Exceptions Controller", exception.toString())
          logException(exception, oppiaClock.getCurrentCalendar().timeInMillis)
        }
      }
      return@storeDataAsync oppiaExceptionLogs.toBuilder().addExceptionLog(exceptionLog).build()
    }.invokeOnCompletion {
      it?.let {
        consoleLogger.e(
          "Exceptions Controller",
          "Failed to store exception log",
          it
        )
      }
    }
  }

  /**
   * Returns the index of the least recent exception from the existing store on the basis of recency and exception type.
   *
   * At first, it checks the index of the least recent exception which has NON_FATAL exception type.
   * If that returns null, then the index of the least recent exception regardless of the type is returned.
   */
  private fun getLeastRecentExceptionIndex(oppiaExceptionLogs: OppiaExceptionLogs): Int? =
    oppiaExceptionLogs.exceptionLogList.withIndex()
      .filter { it.value.exceptionType == ExceptionLog.ExceptionType.NON_FATAL }
      .minBy { it.value.timestamp }?.index ?: getLeastRecentGeneralEventIndex(oppiaExceptionLogs)

  /** Returns the index of the least recent exception regardless of their exception type. */
  private fun getLeastRecentGeneralEventIndex(oppiaExceptionLogs: OppiaExceptionLogs): Int? =
    oppiaExceptionLogs.exceptionLogList.withIndex()
      .minBy { it.value.timestamp }?.index

  /**
   * Returns a [LiveData] result which can be used to get [OppiaExceptionLogs]
   * for the purpose of uploading in the presence of network connectivity.
   */
  fun getExceptionLogs(): LiveData<AsyncResult<OppiaExceptionLogs>> {
    return dataProviders.convertToLiveData(exceptionLogStore)
  }
}
