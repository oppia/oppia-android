package org.oppia.domain.analytics

import androidx.lifecycle.LiveData
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.oppia.app.model.EventLog
import org.oppia.app.model.ExceptionLog
import org.oppia.app.model.OppiaCrashLogs
import org.oppia.app.model.OppiaEventLogs
import org.oppia.app.model.OppiaLog
import org.oppia.app.model.OppiaLog.LogReportCase.EVENT_LOG
import org.oppia.app.model.OppiaLog.LogReportCase.EXCEPTION_LOG
import org.oppia.app.model.OppiaLog.LogReportCase.LOGREPORT_NOT_SET
import org.oppia.data.persistence.PersistentCacheStore
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProviders
import org.oppia.util.logging.Logger
import org.oppia.util.threading.BackgroundDispatcher

@Singleton
class LogReportStorageHelper @Inject constructor(
  private val cacheStoreFactory: PersistentCacheStore.Factory,
  private val dataProviders: DataProviders,
  private val logger: Logger,
  private val logReportingConstantsProvider: LogReportingConstantsProvider,
  @BackgroundDispatcher private val backgroundCoroutineDispatcher: CoroutineDispatcher
) {
  private val eventLogStore =
    cacheStoreFactory.create("event_logs", OppiaEventLogs.getDefaultInstance())
  private val exceptionLogStore =
    cacheStoreFactory.create("crash_logs", OppiaCrashLogs.getDefaultInstance())
  private val coroutineScope = CoroutineScope(backgroundCoroutineDispatcher)

  fun addEventLog(eventLog: EventLog) {
    coroutineScope.launch {
      checkStoreCacheStatus(
        eventLogStore.readDataAsync().await().serializedSize.toByte(),
        EVENT_LOG
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
        EXCEPTION_LOG
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
    logReportCase: OppiaLog.LogReportCase
  ) {
    if (storeSize / 1048576 > logReportingConstantsProvider.getLogReportingCacheSize()) {
      removeEvent(getLeastRecentReport(logReportCase))
      checkStoreCacheStatus(storeSize, logReportCase)
    }
  }

  private suspend fun getLeastRecentEvent(): EventLog? =
    eventLogStore.readDataAsync().await().eventLogList
      .filter { it.priority == EventLog.Priority.OPTIONAL }
      .minBy { it.timestamp } ?: eventLogStore.readDataAsync().await().eventLogList
      .minBy { it.timestamp }

  private suspend fun getLeastRecentCrash(): ExceptionLog? =
    exceptionLogStore.readDataAsync().await().exceptionLogList
      .filter { it.exceptionType == ExceptionLog.ExceptionType.NON_FATAL }
      .minBy { it.timestamp } ?: exceptionLogStore.readDataAsync().await().exceptionLogList
      .minBy { it.timestamp }

  private suspend fun getLeastRecentReport(logReportsCase: OppiaLog.LogReportCase): OppiaLog? {
    return when (logReportsCase) {
      EVENT_LOG -> {
        OppiaLog.newBuilder().setEventLog(getLeastRecentEvent()).build()
      }
      EXCEPTION_LOG -> {
        OppiaLog.newBuilder().setExceptionLog(getLeastRecentCrash()).build()
      }
      LOGREPORT_NOT_SET -> null
    }
  }

  private suspend fun removeEvent(oppiaLog: OppiaLog?) {
    oppiaLog?.eventLog?.let {
      eventLogStore.readDataAsync().await().eventLogList.remove(oppiaLog.eventLog)
    }
    oppiaLog?.exceptionLog?.let {
      exceptionLogStore.readDataAsync().await().exceptionLogList.remove(oppiaLog.exceptionLog)
    }
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
