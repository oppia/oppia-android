package org.oppia.domain.oppialogger.exceptions

import org.oppia.app.model.ExceptionLog
import org.oppia.app.model.ExceptionLog.ExceptionType
import org.oppia.app.model.OppiaExceptionLogs
import org.oppia.data.persistence.PersistentCacheStore
import org.oppia.domain.oppialogger.ExceptionLogStorageCacheSize
import org.oppia.util.data.DataProvider
import org.oppia.util.logging.ConsoleLogger
import org.oppia.util.logging.ExceptionLogger
import org.oppia.util.networking.NetworkConnectionUtil
import javax.inject.Inject

private const val EXCEPTIONS_CONTROLLER = "Exceptions Controller"

/** Controller for handling exception logging. */
class ExceptionsController @Inject constructor(
  private val exceptionLogger: ExceptionLogger,
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val consoleLogger: ConsoleLogger,
  private val networkConnectionUtil: NetworkConnectionUtil,
  @ExceptionLogStorageCacheSize private val exceptionLogStorageCacheSize: Int
) {
  private val exceptionLogStore =
    cacheStoreFactory.create("exception_logs", OppiaExceptionLogs.getDefaultInstance())

  /**
   * Logs a non-fatal [exception].
   * Note that exceptions may not actually be logged depending on the network status of the device.
   * Older exceptions will be pruned to make room for newer exceptions.
   *
   * @param timestampInMillis the time, in milliseconds, when the exception occurred
   */
  fun logNonFatalException(exception: Exception, timestampInMillis: Long) {
    uploadOrCacheExceptionLog(exception, timestampInMillis, ExceptionType.NON_FATAL)
  }

  /**
   * Logs a fatal [exception].
   * Note that exceptions may not actually be logged depending on the network status of the device.
   * Older exceptions will be pruned to make room for newer exceptions.
   *
   * @param timestampInMillis the time, in milliseconds, when the exception occurred
   */
  fun logFatalException(exception: Exception, timestampInMillis: Long) {
    uploadOrCacheExceptionLog(exception, timestampInMillis, ExceptionType.FATAL)
  }

  /**
   * Checks network connectivity of the device.
   *
   * Saves the [exception] to the [exceptionLogStore] in the absence of it.
   * Uploads to remote service in the presence of it.
   */
  private fun uploadOrCacheExceptionLog(
    exception: Exception,
    timestampInMillis: Long,
    exceptionType: ExceptionType
  ) {
    when (networkConnectionUtil.getCurrentConnectionStatus()) {
      NetworkConnectionUtil.ConnectionStatus.NONE ->
        cacheExceptionLog(
          exception.toExceptionLog(
            timestampInMillis,
            exceptionType
          )
        )
      else -> exceptionLogger.logException(exception)
    }
  }

  /** Returns an [ExceptionLog] from a [throwable]. */
  private fun Throwable.toExceptionLog(
    timestampInMillis: Long,
    exceptionType: ExceptionType
  ): ExceptionLog {
    val exceptionLogBuilder = ExceptionLog.newBuilder()
    this.message?.let {
      exceptionLogBuilder.message = it
    }
    exceptionLogBuilder.timestampInMillis = timestampInMillis
    this.cause?.let {
      exceptionLogBuilder.cause = it.toExceptionLog(timestampInMillis, exceptionType)
    }
    this.stackTrace?.let {
      exceptionLogBuilder.addAllStacktraceElement(
        it.map(this@ExceptionsController::convertStackTraceElementToLog)
      )
    }
    exceptionLogBuilder.exceptionType = exceptionType
    return exceptionLogBuilder.build()
  }

  /** Builds the [ExceptionLog.StackTraceElement] from a [stackTraceElement]. */
  private fun convertStackTraceElementToLog(
    stackTraceElement: StackTraceElement
  ): ExceptionLog.StackTraceElement {
    return ExceptionLog.StackTraceElement.newBuilder()
      .setFileName(stackTraceElement.fileName)
      .setMethodName(stackTraceElement.methodName)
      .setLineNumber(stackTraceElement.lineNumber)
      .setDeclaringClass(stackTraceElement.className)
      .build()
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
          consoleLogger.e(EXCEPTIONS_CONTROLLER, exception.toString())
        }
      }
      return@storeDataAsync oppiaExceptionLogs.toBuilder().addExceptionLog(exceptionLog).build()
    }.invokeOnCompletion {
      it?.let {
        consoleLogger.e(
          EXCEPTIONS_CONTROLLER,
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
      .filter { it.value.exceptionType == ExceptionType.NON_FATAL }
      .minBy { it.value.timestampInMillis }?.index
      ?: getLeastRecentGeneralEventIndex(oppiaExceptionLogs)

  /** Returns the index of the least recent exception regardless of their exception type. */
  private fun getLeastRecentGeneralEventIndex(oppiaExceptionLogs: OppiaExceptionLogs): Int? =
    oppiaExceptionLogs.exceptionLogList.withIndex()
      .minBy { it.value.timestampInMillis }?.index

  /** Returns a data provider for exception log reports that have been recorded for upload. */
  fun getExceptionLogStore(): DataProvider<OppiaExceptionLogs> {
    return exceptionLogStore
  }

  /**
   * Returns a list of exception log reports which have been recorded for upload.
   *
   *  As we are using the await call on the deferred output of readDataAsync, the failure case would be caught and it'll throw an error.
   */
  suspend fun getExceptionLogStoreList(): MutableList<ExceptionLog> {
    return exceptionLogStore.readDataAsync().await().exceptionLogList
  }

  /** Removes the first exception log report that had been recorded for upload. */
  fun removeFirstExceptionLogFromStore() {
    exceptionLogStore.storeDataAsync(updateInMemoryCache = true) { oppiaExceptionLogs ->
      return@storeDataAsync oppiaExceptionLogs.toBuilder().removeExceptionLog(0).build()
    }.invokeOnCompletion {
      it?.let {
        consoleLogger.e(
          "Analytics Controller",
          "Failed to remove event log",
          it
        )
      }
    }
  }
}

/** Returns an [Exception] for an [ExceptionLog] object. */
fun ExceptionLog.toException(): Exception {
  val exceptionMessage = if (this.message.isEmpty()) null else this.message
  val exceptionCause: Throwable? =
    if (this.hasCause())
      this.cause.toException()
    else null
  val exception = Exception(exceptionMessage, exceptionCause)
  exception.stackTrace = createErrorStackTrace(this)
  return exception
}

/** Returns an array of [StackTraceElement] for an [exceptionLog]. */
private fun createErrorStackTrace(exceptionLog: ExceptionLog): Array<StackTraceElement> {
  return Array(
    exceptionLog.stacktraceElementCount,
    init = { i: Int ->
      StackTraceElement(
        exceptionLog.stacktraceElementList[i].declaringClass,
        exceptionLog.stacktraceElementList[i].methodName,
        exceptionLog.stacktraceElementList[i].fileName,
        exceptionLog.stacktraceElementList[i].lineNumber
      )
    }
  )
}
