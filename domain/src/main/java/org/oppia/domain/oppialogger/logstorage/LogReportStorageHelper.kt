package org.oppia.domain.oppialogger.logstorage

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.oppia.app.model.EventLog
import org.oppia.app.model.ExceptionLog
import org.oppia.app.model.OppiaEventLogs
import org.oppia.app.model.OppiaExceptionLogs
import org.oppia.data.persistence.PersistentCacheStore
import org.oppia.domain.oppialogger.EventLogStorageCacheSize
import org.oppia.domain.oppialogger.ExceptionLogStorageCacheSize
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProviders
import org.oppia.util.logging.ConsoleLogger
import org.oppia.util.threading.BackgroundDispatcher
import javax.inject.Inject
import javax.inject.Singleton

/** Helper class for storing log reports in absence of network connectivity. */
@Singleton
class LogReportStorageHelper @Inject constructor(
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val dataProviders: DataProviders,
  private val consoleLogger: ConsoleLogger,
  @BackgroundDispatcher private val backgroundCoroutineDispatcher: CoroutineDispatcher,
  @EventLogStorageCacheSize private val eventLogStorageCacheSize: Int,
  @ExceptionLogStorageCacheSize private val exceptionLogStorageCacheSize: Int
) {
  private enum class LogReportType {
    EVENT_LOG,
    EXCEPTION_LOG
  }
  private val eventLogStore =
    cacheStoreFactory.create("event_logs", OppiaEventLogs.getDefaultInstance())
  private val exceptionLogStore =
    cacheStoreFactory.create("exception_logs", OppiaExceptionLogs.getDefaultInstance())
  private val coroutineScope = CoroutineScope(backgroundCoroutineDispatcher)

  /** Adds an event to the storage. */
  fun addEventLog(eventLog: EventLog) {
    coroutineScope.launch {
      checkStoreCacheStatus(
        eventLogStore.readDataAsync().await().eventLogList.size + 1,
        LogReportType.EVENT_LOG,
        eventLogStorageCacheSize
      )
    }
    eventLogStore.storeDataAsync(updateInMemoryCache = true) {
      it.toBuilder().addEventLog(eventLog).build()
    }.invokeOnCompletion {
      it?.let {
        consoleLogger.e(
          "DOMAIN",
          "Failed to store event log",
          it
        )
      }
    }
  }

  /** Adds an exception to the storage. */
  fun addExceptionLog(exceptionLog: ExceptionLog) {
    coroutineScope.launch {
      checkStoreCacheStatus(
        exceptionLogStore.readDataAsync().await().exceptionLogList.size + 1,
        LogReportType.EXCEPTION_LOG,
        exceptionLogStorageCacheSize
      )
    }
    exceptionLogStore.storeDataAsync(updateInMemoryCache = true) {
      it.toBuilder().addExceptionLog(exceptionLog).build()
    }.invokeOnCompletion {
      it?.let {
        consoleLogger.e(
          "DOMAIN",
          "Failed to store crash log",
          it
        )
      }
    }
  }

  /** Checks the [storeSize] and removes an element from the corresponding store if the [cacheStorageLimit] is exceeded. */
  private suspend fun checkStoreCacheStatus(
    storeSize: Int,
    logReportCase: LogReportType,
    cacheStorageLimit: Int
  ) {
    if (storeSize > cacheStorageLimit) {
      when (logReportCase) {
        LogReportType.EVENT_LOG -> removeEvent(getLeastRecentEvent())
        LogReportType.EXCEPTION_LOG -> removeException(getLeastRecentException())
      }
    }
  }

  /**
   * Returns the least recent event from the existing store on the basis of recency and priority.
   * At first, it checks the least recent event which has OPTIONAL priority.
   * If that returns null, then the least recent event regardless of the priority is returned.
   */
  private suspend fun getLeastRecentEvent(): EventLog? =
    eventLogStore.readDataAsync().await().eventLogList
      .filter { it.priority == EventLog.Priority.OPTIONAL }
      .minBy { it.timestamp } ?: eventLogStore.readDataAsync().await().eventLogList
      .minBy { it.timestamp }

  /**
   * Returns the least recent exception from the existing store on the basis of recency and exception type.
   * At first, it checks the least recent exception which has NON_FATAL exception type.
   * If that returns null, then the least recent exception regardless of the exception type is returned.
   */
  private suspend fun getLeastRecentException(): ExceptionLog? =
    exceptionLogStore.readDataAsync().await().exceptionLogList
      .filter { it.exceptionType == ExceptionLog.ExceptionType.NON_FATAL }
      .minBy { it.timestamp } ?: exceptionLogStore.readDataAsync().await().exceptionLogList
      .minBy { it.timestamp }

  /** Removes an [eventLog] from the [eventLogStore]. */
  private suspend fun removeEvent(eventLog: EventLog?) =
    eventLog?.let { eventLogStore.readDataAsync().await().eventLogList.remove(eventLog) }

  /** Removes an [exceptionLog] from the [exceptionLogStore]. */
  private suspend fun removeException(exceptionLog: ExceptionLog?) =
    exceptionLog?.let {
      exceptionLogStore.readDataAsync().await().exceptionLogList.remove(exceptionLog)
    }

  /** Removes all events present in the [eventLogStore]. */
  private fun removeAllEvents() {
    eventLogStore.clearCacheAsync().invokeOnCompletion {
      it?.let {
        consoleLogger.e(
          "DOMAIN",
          "Failed to remove all event logs",
          it
        )
      }
    }
  }

  /** Removes all exceptions present in the [exceptionLogStore]. */
  private fun removeAllExceptions() {
    exceptionLogStore.clearCacheAsync().invokeOnCompletion {
      it?.let {
        consoleLogger.e(
          "DOMAIN",
          "Failed to remove all crashes",
          it
        )
      }
    }
  }

  /**
   * Returns a [LiveData] result which can be used to get [OppiaEventLogs]
   * for the purpose of uploading in the presence of network connectivity.
   */
  fun getEventLogs(): LiveData<AsyncResult<OppiaEventLogs>> {
    return dataProviders.convertToLiveData(eventLogStore)
  }

  /**
   * Returns a [LiveData] result which can be used to get [OppiaExceptionLogs]
   * for the purpose of uploading in the presence of network connectivity.
   */
  fun getExceptionLogs(): LiveData<AsyncResult<OppiaExceptionLogs>> {
    return dataProviders.convertToLiveData(exceptionLogStore)
  }
}
