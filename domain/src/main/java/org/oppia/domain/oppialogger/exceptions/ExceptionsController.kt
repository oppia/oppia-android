package org.oppia.domain.oppialogger.exceptions

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
import javax.inject.Inject

/** Controller for handling exception logging. */
class ExceptionsController @Inject constructor(
  private val exceptionLogger: ExceptionLogger,
  private val dataProviders: DataProviders,
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val consoleLogger: ConsoleLogger,
  private val networkConnectionUtil: NetworkConnectionUtil,
  @ExceptionLogStorageCacheSize private val exceptionLogStorageCacheSize: Int
) {
  private val exceptionLogStore =
    cacheStoreFactory.create("exception_logs", OppiaExceptionLogs.getDefaultInstance())

  /** Logs a NON-FATAL exception. */
  fun logNonFatalException(exception: Exception, timestamp: Long) {
    uploadOrCacheExceptionLog(exception, timestamp, ExceptionLog.ExceptionType.NON_FATAL)
  }

  /** Logs a FATAL exception. */
  fun logFatalException(exception: Exception, timestamp: Long){
    uploadOrCacheExceptionLog(exception, timestamp, ExceptionLog.ExceptionType.FATAL)
  }

  /**
   * Checks network connectivity of the device.
   *
   * Saves the [exception] to the [exceptionLogStore] in the absence of it.
   * Uploads to remote service in the presence of it.
   */
  private fun uploadOrCacheExceptionLog(exception: Exception, timestamp: Long, exceptionType: ExceptionLog.ExceptionType) {
    when (networkConnectionUtil.getCurrentConnectionStatus()) {
      NetworkConnectionUtil.ConnectionStatus.NONE ->
        cacheExceptionLog(convertExceptionToExceptionLog(exception, timestamp, exceptionType))
      else -> exceptionLogger.logException(exception)
    }
  }

  /** Returns an [ExceptionLog] from an [throwable]. */
  private fun convertExceptionToExceptionLog(throwable: Throwable, timestamp: Long, exceptionType: ExceptionLog.ExceptionType): ExceptionLog {
    val exceptionLogBuilder = ExceptionLog.newBuilder()
    throwable.message?.let {
      exceptionLogBuilder.message = it
    }
    exceptionLogBuilder.timestampInMillis = timestamp
    throwable.cause?.let {
      exceptionLogBuilder.cause = convertExceptionToExceptionLog(it, timestamp, exceptionType)
    }
    throwable.stackTrace?.let {
      exceptionLogBuilder.addAllStacktraceElement(it.map(this::convertStackTraceElementToLog))
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
          consoleLogger.e("Exceptions Controller", exception.toString())
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
      .minBy { it.value.timestampInMillis }?.index ?: getLeastRecentGeneralEventIndex(oppiaExceptionLogs)

  /** Returns the index of the least recent exception regardless of their exception type. */
  private fun getLeastRecentGeneralEventIndex(oppiaExceptionLogs: OppiaExceptionLogs): Int? =
    oppiaExceptionLogs.exceptionLogList.withIndex()
      .minBy { it.value.timestampInMillis }?.index

  /**
   * Returns a [LiveData] result which can be used to get [OppiaExceptionLogs]
   * for the purpose of uploading in the presence of network connectivity.
   */
  fun getExceptionLogs(): LiveData<AsyncResult<OppiaExceptionLogs>> {
    return dataProviders.convertToLiveData(exceptionLogStore)
  }
}
