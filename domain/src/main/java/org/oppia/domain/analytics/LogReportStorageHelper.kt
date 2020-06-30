package org.oppia.domain.analytics

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.oppia.app.model.EventLog
import org.oppia.app.model.EventLogs
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
  private val persistentCacheStore: PersistentCacheStore<EventLogs>,
  private val dataProviders: DataProviders,
  private val logger: Logger,
  @BackgroundDispatcher private val backgroundCoroutineDispatcher: CoroutineDispatcher
) {
  private val eventLogStore =
    cacheStoreFactory.create("event_logs", EventLogs.getDefaultInstance())
  private val coroutineScope = CoroutineScope(backgroundCoroutineDispatcher)
  private val storeSizeLimit = 10

  fun addEventLog(eventLog: EventLog) {
    coroutineScope.launch { checkStoreCacheStatus() }
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

  private suspend fun checkStoreCacheStatus() {
    val storeSize = persistentCacheStore.readDataAsync().await().serializedSize.toByte()
    if (storeSize / 1048576 > storeSizeLimit) {
      val eventLog = getLeastRecentOptionalEvent() ?: getLeastRecentEvent()
      removeEvent(eventLog)
      checkStoreCacheStatus()
    }
  }

  private suspend fun getAllOptionalEvents(): List<EventLog> =
    eventLogStore.readDataAsync().await().eventLogList
      .filter { it.priority == EventLog.Priority.OPTIONAL }

  private suspend fun getLeastRecentEvent(): EventLog? =
    eventLogStore.readDataAsync().await().eventLogList.minBy { it.timestamp }

  private suspend fun getLeastRecentOptionalEvent(): EventLog? =
    getAllOptionalEvents().minBy { it.timestamp }

  private suspend fun removeEvent(eventLog: EventLog?) =
    eventLogStore.readDataAsync().await().eventLogList.remove(eventLog)

  private suspend fun removeEvents(eventLogList: MutableList<EventLog>) =
    eventLogStore.readDataAsync().await().eventLogList.removeAll(eventLogList)

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

  fun getEventLogs(): LiveData<AsyncResult<EventLogs>> {
    return dataProviders.convertToLiveData(eventLogStore)
  }
}
