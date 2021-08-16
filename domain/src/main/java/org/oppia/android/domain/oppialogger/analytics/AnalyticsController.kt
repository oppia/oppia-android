package org.oppia.android.domain.oppialogger.analytics

import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.EventLog.EventAction
import org.oppia.android.app.model.EventLog.Priority
import org.oppia.android.app.model.OppiaEventLogs
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.EventLogStorageCacheSize
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.logging.EventLogger
import org.oppia.android.util.logging.ExceptionLogger
import org.oppia.android.util.networking.NetworkConnectionUtil
import org.oppia.android.util.networking.NetworkConnectionUtil.ConnectionStatus.NONE
import javax.inject.Inject

/** Controller for handling analytics event logging.
 * [OppiaLogger] should be the only caller of this class. Any other classes that want to log
 * events should call either [OppiaLogger.logTransitionEvent] or [OppiaLogger.logClickEvent].
 */
class AnalyticsController @Inject constructor(
  private val eventLogger: EventLogger,
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val consoleLogger: ConsoleLogger,
  private val networkConnectionUtil: NetworkConnectionUtil,
  private val exceptionLogger: ExceptionLogger,
  @EventLogStorageCacheSize private val eventLogStorageCacheSize: Int
) {
  private val eventLogStore =
    cacheStoreFactory.create("event_logs", OppiaEventLogs.getDefaultInstance())

  /**
   * Logs transition events.
   * These events are given HIGH priority.
   */
  fun logTransitionEvent(
    timestamp: Long,
    eventAction: EventAction,
    eventContext: EventLog.Context?
  ) {
    uploadOrCacheEventLog(
      createEventLog(
        timestamp,
        eventAction,
        eventContext,
        Priority.ESSENTIAL
      )
    )
  }

  /**
   * Logs click events.
   * These events are given LOW priority.
   */
  fun logClickEvent(
    timestamp: Long,
    eventAction: EventAction,
    eventContext: EventLog.Context?
  ) {
    uploadOrCacheEventLog(
      createEventLog(
        timestamp,
        eventAction,
        eventContext,
        Priority.OPTIONAL
      )
    )
  }

  /** Returns an event log containing relevant data for event reporting. */
  private fun createEventLog(
    timestamp: Long,
    eventAction: EventAction,
    eventContext: EventLog.Context?,
    priority: Priority
  ): EventLog {
    val event: EventLog.Builder = EventLog.newBuilder()
    event.timestamp = timestamp
    event.actionName = eventAction
    event.priority = priority

    if (eventContext != null)
      event.context = eventContext

    return event.build()
  }

  /**
   * Checks network connectivity of the device.
   *
   * Saves the [eventLog] to the [eventLogStore] in the absence of it.
   * Uploads to remote service in the presence of it.
   */
  private fun uploadOrCacheEventLog(eventLog: EventLog) {
    when (networkConnectionUtil.getCurrentConnectionStatus()) {
      NONE -> cacheEventLog(eventLog)
      else -> eventLogger.logEvent(eventLog)
    }
  }

  /**
   * Adds an event to the storage.
   *
   * At first, it checks if the size of the store isn't exceeding [eventLogStorageCacheSize]
   * If the limit is exceeded then the least recent event is removed from the [eventLogStore]
   * After this, the [eventLog] is added to the store.
   * */
  private fun cacheEventLog(eventLog: EventLog) {
    eventLogStore.storeDataAsync(updateInMemoryCache = true) { oppiaEventLogs ->
      val storeSize = oppiaEventLogs.eventLogList.size
      if (storeSize + 1 > eventLogStorageCacheSize) {
        val eventLogRemovalIndex = getLeastRecentEventIndex(oppiaEventLogs)
        if (eventLogRemovalIndex != null) {
          return@storeDataAsync oppiaEventLogs.toBuilder()
            .removeEventLog(eventLogRemovalIndex)
            .addEventLog(eventLog)
            .build()
        } else {
          // TODO(#1433): Refactoring for logging exceptions to both console and exception loggers.
          val exception =
            NullPointerException("Least Recent Event index absent -- EventLogCacheStoreSize is 0")
          consoleLogger.e("Analytics Controller", exception.toString())
          exceptionLogger.logException(exception)
        }
      }
      return@storeDataAsync oppiaEventLogs.toBuilder().addEventLog(eventLog).build()
    }.invokeOnCompletion {
      it?.let {
        consoleLogger.e(
          "Analytics Controller",
          "Failed to store event log",
          it
        )
      }
    }
  }

  /**
   * Returns the index of the least recent event from the existing store on the basis of recency and priority.
   *
   * At first, it checks the index of the least recent event which has OPTIONAL priority.
   * If that returns null, then the index of the least recent event regardless of the priority is returned.
   */
  private fun getLeastRecentEventIndex(oppiaEventLogs: OppiaEventLogs): Int? =
    oppiaEventLogs.eventLogList.withIndex()
      .filter { it.value.priority == Priority.OPTIONAL }
      .minBy { it.value.timestamp }?.index ?: getLeastRecentGeneralEventIndex(oppiaEventLogs)

  /** Returns the index of the least recent event regardless of their priority. */
  private fun getLeastRecentGeneralEventIndex(oppiaEventLogs: OppiaEventLogs): Int? =
    oppiaEventLogs.eventLogList.withIndex()
      .minBy { it.value.timestamp }?.index

  /** Returns a data provider for log reports that have been recorded for upload. */
  fun getEventLogStore(): DataProvider<OppiaEventLogs> {
    return eventLogStore
  }

  /**
   * Returns a list of event log reports that have been recorded for upload.
   *
   * As we are using the await call on the deferred output of readDataAsync, the failure case would be caught and it'll throw an error.
   */
  suspend fun getEventLogStoreList(): MutableList<EventLog> {
    return eventLogStore.readDataAsync().await().eventLogList
  }

  /** Removes the first event log report that had been recorded for upload. */
  fun removeFirstEventLogFromStore() {
    eventLogStore.storeDataAsync(updateInMemoryCache = true) { oppiaEventLogs ->
      return@storeDataAsync oppiaEventLogs.toBuilder().removeEventLog(0).build()
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
