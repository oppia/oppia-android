package org.oppia.domain.analytics

import androidx.lifecycle.LiveData
import org.oppia.app.model.EventLog
import org.oppia.app.model.EventLogs
import org.oppia.data.persistence.PersistentCacheStore
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProviders
import org.oppia.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogReportStorageHelper @Inject constructor(
  private val cacheStoreFactory: PersistentCacheStore.Factory,
  private val dataProviders: DataProviders,
  private val logger: Logger
) {
  private val eventLogStore =
    cacheStoreFactory.create("event_logs", EventLogs.getDefaultInstance())

  fun addEventLog(eventLog: EventLog) {
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

  suspend fun getAllOptionalEvents(): List<EventLog> =
    eventLogStore.readDataAsync().await().eventLogList
      .filter { it.priority == EventLog.Priority.OPTIONAL }

  suspend fun getLeastRecentEvent(): EventLog? =
    eventLogStore.readDataAsync().await().eventLogList.minBy { it.timestamp }

  suspend fun getLeastRecentOptionalEvent(): EventLog? =
    getAllOptionalEvents().minBy { it.timestamp }

  private suspend fun removeEvent(eventLog: EventLog?) =
    eventLogStore.readDataAsync().await().eventLogList.remove(eventLog)

  private suspend fun removeEvents(eventLogList: MutableList<EventLog>) =
    eventLogStore.readDataAsync().await().eventLogList.removeAll(eventLogList)

  private fun removeAllEvents(){
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