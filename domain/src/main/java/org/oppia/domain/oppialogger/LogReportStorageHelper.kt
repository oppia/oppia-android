package org.oppia.domain.oppialogger

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.oppia.app.model.EventLog
import org.oppia.app.model.ExceptionLog
import org.oppia.app.model.OppiaCrashLogs
import org.oppia.app.model.OppiaEventLogs
import org.oppia.app.model.OppiaExceptionLogs
import org.oppia.data.persistence.PersistentCacheStore
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProviders
import org.oppia.util.logging.Logger
import org.oppia.util.threading.BackgroundDispatcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogReportStorageHelper @Inject constructor(
  private val cacheStoreFactory: PersistentCacheStore.Factory,
  private val dataProviders: DataProviders,
  private val logger: Logger,
  @BackgroundDispatcher private val backgroundCoroutineDispatcher: CoroutineDispatcher,
  @EventLogStorageCacheSize private val eventLogStorageCacheSize: Int,
  @ExceptionLogStorageCacheSize private val exceptionLogStorageCacheSize: Int
) {
  private enum class LogReportingCase {
    EVENT_LOG,
    EXCEPTION_LOG
  }
  private val eventLogStore =
    cacheStoreFactory.create("event_logs", OppiaEventLogs.getDefaultInstance())
  private val exceptionLogStore =
    cacheStoreFactory.create("exception_logs", OppiaExceptionLogs.getDefaultInstance())
  private val coroutineScope = CoroutineScope(backgroundCoroutineDispatcher)

  fun addEventLog(eventLog: EventLog) {
    coroutineScope.launch {
      checkStoreCacheStatus(
        eventLogStore.readDataAsync().await().serializedSize.toByte(),
        LogReportingCase.EVENT_LOG,
        eventLogStorageCacheSize
      )
    }
    eventLogStore.storeDataAsync(updateInMemoryCache = true) {
      it.toBuilder().addEventLog(eventLog).build()
    }.invokeOnCompletion {
      it?.let {
        logger.e(
          "DOMAIN",
          "Failed to store event log",
          it
        )
      }
    }
  }

  fun addCrashLog(exceptionLog: ExceptionLog) {
    coroutineScope.launch {
      checkStoreCacheStatus(
        exceptionLogStore.readDataAsync().await().serializedSize.toByte(),
        LogReportingCase.EXCEPTION_LOG,
        exceptionLogStorageCacheSize
      )
    }
    exceptionLogStore.storeDataAsync(updateInMemoryCache = true) {
      it.toBuilder().addExceptionLog(exceptionLog).build()
    }.invokeOnCompletion {
      it?.let {
        logger.e(
          "DOMAIN",
          "Failed to store crash log",
          it
        )
      }
    }
  }

  private suspend fun checkStoreCacheStatus(
    storeSize: Byte,
    logReportCase: LogReportingCase,
    cacheStorageLimit: Int
  ) {
    if (storeSize / 1048576 > cacheStorageLimit) {
      when (logReportCase) {
        LogReportingCase.EVENT_LOG -> removeEvent(getLeastRecentEvent())
        LogReportingCase.EXCEPTION_LOG -> removeException(getLeastRecentException())
      }
      checkStoreCacheStatus(storeSize, logReportCase, cacheStorageLimit)
    }
  }

  private suspend fun getLeastRecentEvent(): EventLog? =
    eventLogStore.readDataAsync().await().eventLogList
      .filter { it.priority == EventLog.Priority.OPTIONAL }
      .minBy { it.timestamp } ?: eventLogStore.readDataAsync().await().eventLogList
      .minBy { it.timestamp }

  private suspend fun getLeastRecentException(): ExceptionLog? =
    exceptionLogStore.readDataAsync().await().exceptionLogList
      .filter { it.exceptionType == ExceptionLog.ExceptionType.NON_FATAL }
      .minBy { it.timestamp } ?: exceptionLogStore.readDataAsync().await().exceptionLogList
      .minBy { it.timestamp }

  private suspend fun removeEvent(eventLog: EventLog?) =
    eventLog?.let { eventLogStore.readDataAsync().await().eventLogList.remove(eventLog) }

  private suspend fun removeException(exceptionLog: ExceptionLog?) =
    exceptionLog?.let {
      exceptionLogStore.readDataAsync().await().exceptionLogList.remove(exceptionLog)
    }

  private fun removeAllEvents() {
    eventLogStore.clearCacheAsync().invokeOnCompletion {
      it?.let {
        logger.e(
          "DOMAIN",
          "Failed to remove all event logs",
          it
        )
      }
    }
  }

  private fun removeAllCrashes() {
    exceptionLogStore.clearCacheAsync().invokeOnCompletion {
      it?.let {
        logger.e(
          "DOMAIN",
          "Failed to remove all crashes",
          it
        )
      }
    }
  }

  fun getEventLogs(): LiveData<AsyncResult<OppiaEventLogs>> {
    return dataProviders.convertToLiveData(eventLogStore)
  }

  fun getCrashLogs(): LiveData<AsyncResult<OppiaCrashLogs>> {
    return dataProviders.convertToLiveData(exceptionLogStore)
  }
}
