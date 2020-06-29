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
class EventStorageHelper @Inject constructor(
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

  suspend fun getOptionalEvents(): MutableList<EventLog>{
    var eventLogList: MutableList<EventLog> = mutableListOf()
    val eventList = eventLogStore.readDataAsync().await().eventLogList
    for (eventLog in eventList){
      if(eventLog.priority == EventLog.Priority.OPTIONAL){
        eventLogList.add(eventLog)
      }
    }
    return eventLogList
  }

  suspend fun getLeastRecentEvent(): EventLog?{
    var timestamp: Long = 0
    var event: EventLog? = null
    val eventList = eventLogStore.readDataAsync().await().eventLogList
    for (eventLog in eventList){
      if(eventLog.timestamp > timestamp){
        timestamp = eventLog.timestamp
        event = eventLog
      }
    }
    return event
  }

  suspend fun getLeastRecentOptionalEvent(): EventLog?{
    var timestamp: Long = 0
    var event: EventLog? = null
    val eventList = eventLogStore.readDataAsync().await().eventLogList
    for (eventLog in eventList){
      if(eventLog.timestamp > timestamp && eventLog.priority == EventLog.Priority.OPTIONAL){
        timestamp = eventLog.timestamp
        event = eventLog
      }
    }
    return event
  }

  suspend fun removeEvent(eventLog: EventLog?){
    val eventList = eventLogStore.readDataAsync().await().eventLogList
    eventLog?.let {eventList.remove(eventLog)}
  }

  suspend fun removeEvents(eventLogList: MutableList<EventLog>) =
    eventLogStore.readDataAsync().await().eventLogList.removeAll(eventLogList)


  suspend fun removeAllEvents(){
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