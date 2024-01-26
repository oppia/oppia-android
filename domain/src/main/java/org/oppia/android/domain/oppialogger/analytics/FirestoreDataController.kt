package org.oppia.android.domain.oppialogger.analytics

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.OppiaEventLogs
import org.oppia.android.app.model.ProfileId
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.auth.AuthenticationController
import org.oppia.android.domain.oppialogger.FirestoreLogStorageCacheSize
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.logging.ExceptionLogger
import org.oppia.android.util.logging.firebase.FirestoreEventLogger
import org.oppia.android.util.networking.NetworkConnectionUtil
import org.oppia.android.util.system.OppiaClock
import org.oppia.android.util.threading.BlockingDispatcher
import javax.inject.Inject
import javax.inject.Singleton

/** Controller for handling event logging for Firestore-bound data. */
@Singleton
class FirestoreDataController @Inject constructor(
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val consoleLogger: ConsoleLogger,
  private val networkConnectionUtil: NetworkConnectionUtil,
  private val eventLogger: FirestoreEventLogger,
  private val exceptionLogger: ExceptionLogger,
  private val oppiaClock: OppiaClock,
  private val authenticationController: AuthenticationController,
  @BlockingDispatcher private val blockingDispatcher: CoroutineDispatcher,
  @FirestoreLogStorageCacheSize private val logStorageCacheSize: Int
) {
  private val firestoreEventsStore =
    cacheStoreFactory.create("firestore_data", OppiaEventLogs.getDefaultInstance())

  /**
   * Uploads all events pending currently for upload, and blocks until the events are uploaded. An
   * error will be thrown if something went wrong during upload.
   */
  suspend fun uploadData() {
    val eventLogsToUpload = firestoreEventsStore.readDataAsync().await().eventLogsToUploadList

    if (eventLogsToUpload.isNotEmpty()) {
      eventLogsToUpload.forEach { eventLog ->
        authenticateAndUploadToFirestore(eventLog)
      }
    }
  }

  /**
   * Logs an event defined by [eventContext] corresponding to time [timestamp].
   *
   * This will schedule a background upload of the event if there's internet connectivity, otherwise
   * it will cache the event for a later upload.
   */
  fun logEvent(
    eventContext: EventLog.Context,
    profileId: ProfileId?,
    timestamp: Long = oppiaClock.getCurrentTimeMs()
  ) {
    CoroutineScope(blockingDispatcher).async {
      uploadOrCacheEventLog(createEventLog(profileId, timestamp, eventContext))
    }.invokeOnCompletion { failure ->
      failure?.let {
        consoleLogger.w(
          "FirestoreDataController",
          "Failed to upload or cache event: $eventContext (at time $timestamp).",
          it
        )
      }
    }
  }

  /** Returns an event log containing relevant data for event reporting. */
  private fun createEventLog(
    profileId: ProfileId?,
    timestamp: Long,
    context: EventLog.Context
  ): EventLog {
    return EventLog.newBuilder().apply {
      this.timestamp = timestamp
      this.priority = EventLog.Priority.ESSENTIAL
      this.context = context
      profileId?.let { this.profileId = it }
    }.build()
  }

  /** Either uploads or caches [eventLog] depending on current internet connectivity. */
  private suspend fun uploadOrCacheEventLog(eventLog: EventLog) {
    when (networkConnectionUtil.getCurrentConnectionStatus()) {
      NetworkConnectionUtil.ProdConnectionStatus.NONE -> cacheEventForFirestore(eventLog)
      else -> authenticateAndUploadToFirestore(eventLog)
    }
  }

  private suspend fun authenticateAndUploadToFirestore(eventLog: EventLog) {
    if (authenticationController.currentFirebaseUser == null) {
      when (val signInResult = authenticationController.signInAnonymouslyWithFirebase().await()) {
        is AsyncResult.Success -> {
          consoleLogger.i("FirestoreDataController", "Sign in succeeded")
          uploadLog(eventLog)
        }
        is AsyncResult.Failure -> {
          consoleLogger.e(
            "FirestoreDataController",
            "Sign in failed with cause ${signInResult.error}"
          )
          cacheEventForFirestore(eventLog)
        }
        is AsyncResult.Pending -> {
          consoleLogger.i("FirestoreDataController", "Signing in anonymously to Firebase")
        }
      }
    } else {
      uploadLog(eventLog)
    }
  }

  private fun uploadLog(eventLog: EventLog) {
    eventLogger.uploadEvent(eventLog)
    removeFirstEventLogFromStore()
  }

  /**
   * Adds an event to the storage.
   *
   * The [eventLog] is added to the store if the size of the store isn't exceeding
   * [logStorageCacheSize]. If the limit is exceeded then the least recent event is removed from the
   * [firestoreEventsStore].
   */
  private fun cacheEventForFirestore(eventLog: EventLog) {
    firestoreEventsStore.storeDataAsync(updateInMemoryCache = true) { eventLogs ->
      val storeSize = eventLogs.eventLogsToUploadList.size
      if (storeSize + 1 > logStorageCacheSize) {
        val eventLogRemovalIndex = getLeastRecentEventIndex(eventLogs)
        if (eventLogRemovalIndex != null) {
          return@storeDataAsync eventLogs.toBuilder()
            .removeEventLogsToUpload(eventLogRemovalIndex)
            .addEventLogsToUpload(eventLog)
            .build()
        } else {
          val exception =
            IllegalStateException(
              "Least Recent Event index absent -- FirestoreLogStorageCacheSize is 0"
            )
          consoleLogger.e("FirestoreDataController", "Failure while caching event.", exception)
          exceptionLogger.logException(exception)
        }
      }
      return@storeDataAsync eventLogs.toBuilder().addEventLogsToUpload(eventLog).build()
    }.invokeOnCompletion {
      it?.let { consoleLogger.e("FirestoreDataController", "Failed to store event log.", it) }
    }
  }

  /**
   * Returns the index of the least recent event from the existing store on the basis of recency and
   * priority.
   */
  private fun getLeastRecentEventIndex(oppiaEventLogs: OppiaEventLogs): Int? =
    oppiaEventLogs.eventLogsToUploadList.withIndex().minByOrNull { it.value.timestamp }?.index

  /** Returns a data provider for log reports that have been recorded for upload. */
  fun getEventLogStore(): DataProvider<OppiaEventLogs> = firestoreEventsStore

  /** Removes the first log report that had been recorded for upload. */
  private fun removeFirstEventLogFromStore() {
    firestoreEventsStore.storeDataAsync(updateInMemoryCache = true) { oppiaEventLogs ->
      if (oppiaEventLogs.eventLogsToUploadCount > 0) {
        return@storeDataAsync oppiaEventLogs.toBuilder().removeEventLogsToUpload(0).build()
      } else {
        return@storeDataAsync oppiaEventLogs // No event logs to remove
      }
    }.invokeOnCompletion {
      it?.let {
        consoleLogger.e("FirestoreDataController", "Failed to remove event log", it)
      }
    }
  }
}
