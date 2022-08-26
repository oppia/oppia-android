package org.oppia.android.domain.oppialogger.analytics

import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.EventLog.Priority
import org.oppia.android.app.model.OppiaEventLogs
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.EventLogStorageCacheSize
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.logging.AnalyticsEventLogger
import org.oppia.android.util.logging.ExceptionLogger
import org.oppia.android.util.logging.SyncStatusManager
import org.oppia.android.util.networking.NetworkConnectionUtil
import org.oppia.android.util.networking.NetworkConnectionUtil.ProdConnectionStatus.NONE
import java.lang.IllegalStateException
import javax.inject.Inject

/**
 * Controller for handling analytics event logging.
 *
 * Callers should not use this class directly; instead, they should use ``OppiaLogger`` which
 * provides convenience log methods.
 */
class AnalyticsController @Inject constructor(
  private val analyticsEventLogger: AnalyticsEventLogger,
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val consoleLogger: ConsoleLogger,
  private val networkConnectionUtil: NetworkConnectionUtil,
  private val exceptionLogger: ExceptionLogger,
  private val syncStatusManager: SyncStatusManager,
  @EventLogStorageCacheSize private val eventLogStorageCacheSize: Int
) {
  private val eventLogStore =
    cacheStoreFactory.create("event_logs", OppiaEventLogs.getDefaultInstance())

  /**
   * Logs a high priority event defined by [eventContext] corresponding to time [timestamp].
   *
   * This will schedule a background upload of the event if there's internet connectivity, otherwise
   * it will cache the event for a later upload.
   *
   * This method should only be used for events which are important to log and should be prioritized
   * over events logged via [logLowPriorityEvent].
   */
  fun logImportantEvent(timestamp: Long, eventContext: EventLog.Context) {
    uploadOrCacheEventLog(createEventLog(timestamp, eventContext, Priority.ESSENTIAL))
  }

  /**
   * Logs a low priority event defined by [eventContext] corresponding to time [timestamp].
   *
   * This will schedule a background upload of the event if there's internet connectivity, otherwise
   * it will cache the event for a later upload.
   *
   * Low priority events may be removed from the event cache if device space is limited, and there's
   * no connectivity for immediately sending events.
   *
   * Callers should use this for events that are nice to have, but okay to miss occasionally (as
   * it's unexpected for events to actually be dropped since the app is configured to support a
   * large number of cached events at one time).
   */
  fun logLowPriorityEvent(timestamp: Long, eventContext: EventLog.Context) {
    uploadOrCacheEventLog(createEventLog(timestamp, eventContext, Priority.OPTIONAL))
  }

  /** Returns an event log containing relevant data for event reporting. */
  private fun createEventLog(
    timestamp: Long,
    context: EventLog.Context,
    priority: Priority
  ): EventLog {
    return EventLog.newBuilder().apply {
      this.timestamp = timestamp
      this.priority = priority
      this.context = context
    }.build()
  }

  /** Either uploads or caches [eventLog] depending on current internet connectivity. */
  private fun uploadOrCacheEventLog(eventLog: EventLog) {
    when (networkConnectionUtil.getCurrentConnectionStatus()) {
      NONE -> {
        syncStatusManager.setSyncStatus(SyncStatusManager.SyncStatus.NO_CONNECTIVITY)
        cacheEventLog(eventLog)
      }
      else -> {
        syncStatusManager.setSyncStatus(SyncStatusManager.SyncStatus.DATA_UPLOADING)
        analyticsEventLogger.logEvent(eventLog)
        syncStatusManager.setSyncStatus(SyncStatusManager.SyncStatus.DATA_UPLOADED)
      }
    }
  }

  /**
   * Adds an event to the storage.
   *
   * At first, it checks if the size of the store isn't exceeding [eventLogStorageCacheSize]. If the
   * limit is exceeded then the least recent event is removed from the [eventLogStore]. After this,
   * the [eventLog] is added to the store.
   */
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
            IllegalStateException("Least Recent Event index absent -- EventLogCacheStoreSize is 0")
          consoleLogger.e("AnalyticsController", "Failure while caching event.", exception)
          exceptionLogger.logException(exception)
        }
      }
      return@storeDataAsync oppiaEventLogs.toBuilder().addEventLog(eventLog).build()
    }.invokeOnCompletion {
      it?.let { consoleLogger.e("AnalyticsController", "Failed to store event log.", it) }
    }
  }

  /**
   * Returns the index of the least recent event from the existing store on the basis of recency and
   * priority.
   *
   * At first, it checks the index of the least recent event which has OPTIONAL priority. If that
   * returns null, then the index of the least recent event regardless of the priority is returned.
   */
  private fun getLeastRecentEventIndex(oppiaEventLogs: OppiaEventLogs): Int? =
    oppiaEventLogs.eventLogList.withIndex()
      .filter { it.value.priority == Priority.OPTIONAL }
      .minByOrNull { it.value.timestamp }?.index ?: getLeastRecentGeneralEventIndex(oppiaEventLogs)

  /** Returns the index of the least recent event regardless of their priority. */
  private fun getLeastRecentGeneralEventIndex(oppiaEventLogs: OppiaEventLogs): Int? =
    oppiaEventLogs.eventLogList.withIndex()
      .minByOrNull { it.value.timestamp }?.index

  /** Returns a data provider for log reports that have been recorded for upload. */
  fun getEventLogStore(): DataProvider<OppiaEventLogs> {
    return eventLogStore
  }

  /**
   * Returns a list of event log reports that have been recorded for upload.
   *
   * As we are using the await call on the deferred output of readDataAsync, the failure case would
   * be caught and it'll throw an error.
   */
  suspend fun getEventLogStoreList(): MutableList<EventLog> {
    return eventLogStore.readDataAsync().await().eventLogList
  }

  /** Removes the first event log report that had been recorded for upload. */
  fun removeFirstEventLogFromStore() {
    eventLogStore.storeDataAsync(updateInMemoryCache = true) { oppiaEventLogs ->
      return@storeDataAsync oppiaEventLogs.toBuilder().removeEventLog(0).build()
    }.invokeOnCompletion {
      it?.let { consoleLogger.e("AnalyticsController", "Failed to remove event log.", it) }
    }
  }
}
