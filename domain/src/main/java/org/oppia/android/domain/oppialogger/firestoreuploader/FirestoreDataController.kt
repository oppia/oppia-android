package org.oppia.android.domain.oppialogger.firestoreuploader

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.OppiaEventLogs
import org.oppia.android.app.model.ProfileId
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.FirestoreLogStorageCacheSize
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.firestore.DataUploader
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.logging.ExceptionLogger
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
  private val dataUploader: DataUploader,
  private val exceptionLogger: ExceptionLogger,
  private val oppiaClock: OppiaClock,
  @BlockingDispatcher private val blockingDispatcher: CoroutineDispatcher,
  @FirestoreLogStorageCacheSize private val logStorageCacheSize: Int
) {
  private val firestoreDataStore =
    cacheStoreFactory.create("firestore_data", OppiaEventLogs.getDefaultInstance())

  /**
   * Logs a high priority event defined by [eventContext] corresponding to time [timestamp].
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

  /**
   * Uploads all events pending currently for upload, and blocks until the events are uploaded. An
   * error will be thrown if something went wrong during upload.
   */
  suspend fun uploadData() {
    println("suspend upload data")
    firestoreDataStore.readDataAsync().await().eventLogsToUploadList.forEach { eventLog ->
      authenticateAndUploadToFirestore(eventLog)
    }
  }

  /** Either uploads or caches [eventLog] depending on current internet connectivity. */
  private fun uploadOrCacheEventLog(eventLog: EventLog) {
    when (networkConnectionUtil.getCurrentConnectionStatus()) {
      NetworkConnectionUtil.ProdConnectionStatus.NONE -> cacheEventForFirestore(eventLog)
      else -> authenticateAndUploadToFirestore(eventLog)
    }
  }

  private fun authenticateAndUploadToFirestore(eventLog: EventLog) {
    val firebaseAuth = Firebase.auth
    if (firebaseAuth.currentUser == null) {
      firebaseAuth.signInAnonymously()
        .addOnSuccessListener {
          dataUploader.uploadData(eventLog)
        }
        .addOnFailureListener {
          cacheEventForFirestore(eventLog)
          consoleLogger.e("FirestoreDataController", "Authentication Failed")
        }
    } else {
      dataUploader.uploadData(eventLog)
    }
  }

  /** Removes the first log report that had been recorded for upload. */
  fun removeFirstEventLogFromStore() {
    println("removing first event log from store")
    firestoreDataStore.storeDataAsync(updateInMemoryCache = true) { oppiaEventLogs ->
      return@storeDataAsync oppiaEventLogs.toBuilder().removeEventLogsToUpload(0).build()
    }.invokeOnCompletion {
      it?.let {
        consoleLogger.e("FirestoreDataController", "Failed to remove event log", it)
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

  /**
   * Adds an event to the storage.
   *
   * The [eventLog] is added to the store if the size of the store isn't exceeding
   * [logStorageCacheSize]. If the limit is exceeded then the least recent event is removed from the
   * [firestoreDataStore].
   */
  private fun cacheEventForFirestore(eventLog: EventLog) {
    firestoreDataStore.storeDataAsync(updateInMemoryCache = true) { eventLogs ->
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
  fun getEventLogStore(): DataProvider<OppiaEventLogs> = firestoreDataStore
}
