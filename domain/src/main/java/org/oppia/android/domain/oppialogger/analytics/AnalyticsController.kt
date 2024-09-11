package org.oppia.android.domain.oppialogger.analytics

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.EventLog.Priority
import org.oppia.android.app.model.OppiaEventLogs
import org.oppia.android.app.model.ProfileId
import org.oppia.android.data.backends.gae.NetworkLoggingInterceptor
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.data.persistence.PersistentCacheStore.PublishMode.PUBLISH_TO_IN_MEMORY_CACHE
import org.oppia.android.data.persistence.PersistentCacheStore.UpdateMode.UPDATE_IF_NEW_CACHE
import org.oppia.android.domain.oppialogger.EventLogStorageCacheSize
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.logging.AnalyticsEventLogger
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.logging.ExceptionLogger
import org.oppia.android.util.logging.SyncStatusManager
import org.oppia.android.util.networking.NetworkConnectionUtil
import org.oppia.android.util.networking.NetworkConnectionUtil.ProdConnectionStatus.NONE
import org.oppia.android.util.platformparameter.EnableLearnerStudyAnalytics
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.system.OppiaClock
import org.oppia.android.util.threading.BackgroundDispatcher
import org.oppia.android.util.threading.BlockingDispatcher
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

private const val UPLOAD_ALL_EVENTS_PROVIDER_ID = "AnalyticsController.upload_all_events"

/**
 * Controller for handling analytics event logging.
 *
 * Callers should not use this class directly; instead, they should use ``OppiaLogger`` which
 * provides convenience log methods.
 */
@Singleton
class AnalyticsController @Inject constructor(
  private val analyticsEventLogger: AnalyticsEventLogger,
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val consoleLogger: ConsoleLogger,
  private val networkConnectionUtil: NetworkConnectionUtil,
  private val exceptionLogger: ExceptionLogger,
  private val syncStatusManager: SyncStatusManager,
  private val oppiaClock: OppiaClock,
  private val oppiaLogger: OppiaLogger,
  private val translationController: TranslationController,
  private val dataProviders: DataProviders,
  private val networkLoggingInterceptor: NetworkLoggingInterceptor,
  @EventLogStorageCacheSize private val eventLogStorageCacheSize: Int,
  @BlockingDispatcher private val blockingDispatcher: CoroutineDispatcher,
  @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher,
  @EnableLearnerStudyAnalytics private val enableLearnerStudyParam: PlatformParameterValue<Boolean>
) {
  // NOTE TO DEVELOPER: This log store should not be lazy since it needs to be primed as early as
  // possible. Creating the log store with a delay (such as would happen if it were lazy delegated)
  // shouldn't affect event record integrity, but it can affect how the sync status manager reports
  // progress since it won't have a data source to properly monitor for changes.
  private val eventLogStore =
    cacheStoreFactory.create("event_logs", OppiaEventLogs.getDefaultInstance()).also { store ->
      store.primeInMemoryAndDiskCacheAsync(
        UPDATE_IF_NEW_CACHE, PUBLISH_TO_IN_MEMORY_CACHE
      ).invokeOnCompletion { error ->
        error?.let {
          consoleLogger.e("AnalyticsController", "Failed to prime event log cache.", error)
        }
      }
      syncStatusManager.initializeEventLogStore(store)
    }

  private val enableLearnerStudyAnalytics get() = enableLearnerStudyParam.value

  /**
   * Logs a high priority event defined by [eventContext] corresponding to time [timestamp].
   *
   * This will schedule a background upload of the event if there's internet connectivity, otherwise
   * it will cache the event for a later upload.
   *
   * This method should only be used for events which are important to log and should be prioritized
   * over events logged via [logLowPriorityEvent].
   */
  fun logImportantEvent(
    eventContext: EventLog.Context,
    profileId: ProfileId?,
    timestamp: Long = oppiaClock.getCurrentTimeMs()
  ) {
    logEvent(eventContext, profileId, priority = Priority.ESSENTIAL, timestamp)
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
  fun logLowPriorityEvent(
    eventContext: EventLog.Context,
    profileId: ProfileId?,
    timestamp: Long = oppiaClock.getCurrentTimeMs()
  ) {
    logEvent(eventContext, profileId, priority = Priority.OPTIONAL, timestamp)
  }

  private fun logEvent(
    eventContext: EventLog.Context,
    profileId: ProfileId?,
    priority: Priority,
    timestamp: Long
  ) {
    CoroutineScope(blockingDispatcher).async {
      uploadOrCacheEventLog(createEventLog(profileId, timestamp, eventContext, priority))
    }.invokeOnCompletion { failure ->
      failure?.let {
        consoleLogger.w(
          "AnalyticsController",
          "Failed to upload or cache $priority event: $eventContext (at time $timestamp).",
          it
        )
      }
    }
  }

  /** Returns an event log containing relevant data for event reporting. */
  private suspend fun createEventLog(
    profileId: ProfileId?,
    timestamp: Long,
    context: EventLog.Context,
    priority: Priority
  ): EventLog {
    return EventLog.newBuilder().apply {
      this.timestamp = timestamp
      this.priority = priority
      this.context = context
      profileId?.let { this.profileId = it }
      resolveProfileOperation(
        profileId, translationController::getAppLanguageSelection
      )?.let { this.appLanguageSelection = it }
      resolveProfileOperation(
        profileId, translationController::getWrittenTranslationContentLanguageSelection
      )?.let { this.writtenTranslationLanguageSelection = it }
      resolveProfileOperation(
        profileId, translationController::getAudioTranslationContentLanguageSelection
      )?.let { this.audioTranslationLanguageSelection = it }
    }.build()
  }

  /** Either uploads or caches [eventLog] depending on current internet connectivity. */
  private suspend fun uploadOrCacheEventLog(eventLog: EventLog) {
    when (networkConnectionUtil.getCurrentConnectionStatus()) {
      NONE -> cacheEventLog(eventLog)
      else -> {
        analyticsEventLogger.logEvent(eventLog)
        if (enableLearnerStudyAnalytics) {
          recordUploadedEvent(eventLog)
        }
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
  private suspend fun cacheEventLog(eventLog: EventLog) {
    eventLogStore.storeDataAsync(updateInMemoryCache = true) { oppiaEventLogs ->
      val storeSize = oppiaEventLogs.eventLogsToUploadList.size
      if (storeSize + 1 > eventLogStorageCacheSize) {
        val eventLogRemovalIndex = getLeastRecentEventIndex(oppiaEventLogs)
        if (eventLogRemovalIndex != null) {
          return@storeDataAsync oppiaEventLogs.toBuilder()
            .removeEventLogsToUpload(eventLogRemovalIndex)
            .addEventLogsToUpload(eventLog)
            .build()
        } else {
          val exception =
            IllegalStateException("Least Recent Event index absent -- EventLogCacheStoreSize is 0")
          consoleLogger.e("AnalyticsController", "Failure while caching event.", exception)
          exceptionLogger.logException(exception)
        }
      }
      return@storeDataAsync oppiaEventLogs.toBuilder().addEventLogsToUpload(eventLog).build()
    }.await()
  }

  private suspend fun recordUploadedEvent(eventLog: EventLog) {
    eventLogStore.storeDataAsync(updateInMemoryCache = true) { oppiaEventLogs ->
      oppiaEventLogs.toBuilder().addUploadedEventLogs(eventLog).build()
    }.await()
  }

  /**
   * Returns the index of the least recent event from the existing store on the basis of recency and
   * priority.
   *
   * At first, it checks the index of the least recent event which has OPTIONAL priority. If that
   * returns null, then the index of the least recent event regardless of the priority is returned.
   */
  private fun getLeastRecentEventIndex(oppiaEventLogs: OppiaEventLogs): Int? =
    oppiaEventLogs.eventLogsToUploadList.withIndex()
      .filter { it.value.priority == Priority.OPTIONAL }
      .minByOrNull { it.value.timestamp }?.index ?: getLeastRecentGeneralEventIndex(oppiaEventLogs)

  /** Returns the index of the least recent event regardless of their priority. */
  private fun getLeastRecentGeneralEventIndex(oppiaEventLogs: OppiaEventLogs): Int? =
    oppiaEventLogs.eventLogsToUploadList.withIndex().minByOrNull { it.value.timestamp }?.index

  /** Returns a data provider for log reports that have been recorded for upload. */
  fun getEventLogStore(): DataProvider<OppiaEventLogs> = eventLogStore

  /**
   * Uploads all events pending currently for upload, and blocks until the events are uploaded. An
   * error will be thrown if something went wrong during upload.
   *
   * This should be used in cases when the caller needs to pause all of its execution until the
   * events are guaranteed to synchronously be uploaded, unlike [uploadEventLogs] which can be used
   * from a UI caller to track the upload operation asynchronously.
   *
   * [SyncStatusManager] can be used to observe the start & stop moments of event uploading.
   */
  suspend fun uploadEventLogsAndWait() {
    val uploadResult = uploadAllEvents().lastOrNull()
    if (uploadResult is AsyncResult.Failure) throw uploadResult.error
  }

  /**
   * Uploads all events pending currently for upload, and returns a [DataProvider] to track the
   * results.
   *
   * Note that the returned data provider will initially provide a pending result, but then will
   * update with one or more [Pair]s with a [Pair.first] value of the current number of uploaded
   * events and a [Pair.second] value of the total number of events to upload. The operation can be
   * assumed that it's not completed until these two values are equal (and, per eventual
   * consistency, it's guaranteed that there will be no follow-up changes to the provider).
   *
   * Unlike [uploadEventLogsAndWait], this can be called asynchronously and observed in a UI. This
   * otherwise follows the same sync status update behaviors as [uploadEventLogsAndWait].
   */
  fun uploadEventLogs(): DataProvider<Pair<Int, Int>> {
    return dataProviders.run {
      // stateIn() produces a hot flow that never completes. This is fine for DataProviders, but
      // doesn't work for synchronous operations (like in uploadEventLogsAndWait).
      uploadAllEvents().stateIn(
        CoroutineScope(backgroundDispatcher),
        SharingStarted.Lazily,
        initialValue = AsyncResult.Pending()
      ).convertAsyncToAutomaticDataProvider(UPLOAD_ALL_EVENTS_PROVIDER_ID)
    }
  }

  private fun uploadAllEvents(): Flow<AsyncResult<Pair<Int, Int>>> {
    val completedEventCount = AtomicInteger()
    syncStatusManager.reportUploadingStarted()
    return retrieveEventLogCountAsync().transform { eventLogCount ->
      // Values are emitted in any order of completion (since N operations of "remove first" should
      // ensure that all logs are uploaded, and roughly in order).
      when (eventLogCount) {
        0 -> emit(AsyncResult.Success(0 to 0))
        else -> repeat(eventLogCount) {
          emitAll(
            logFirstEventLogFromStoreAsync().map {
              AsyncResult.Success(completedEventCount.incrementAndGet() to eventLogCount)
            }
          )
        }
      }
    }.onCompletion {
      // Only consider uploading successfully completed if nothing went wrong during upload.
      if (it == null) syncStatusManager.reportUploadingEnded()
    }
  }

  private fun retrieveEventLogCountAsync(): Flow<Int> =
    flow { emit(eventLogStore.readDataAsync().await().eventLogsToUploadCount) }

  private fun logFirstEventLogFromStoreAsync(): Flow<Unit> {
    return flow {
      check(networkConnectionUtil.getCurrentConnectionStatus() != NONE) {
        "Cannot upload events without internet connectivity."
      }
      emit(analyticsEventLogger.logEvent(removeFirstEventLogFromStoreAsync().await()))
    }
  }

  private fun removeFirstEventLogFromStoreAsync(): Deferred<EventLog> {
    return eventLogStore.storeDataWithCustomChannelAsync(updateInMemoryCache = true) { eventLogs ->
      eventLogs.toBuilder().apply {
        if (enableLearnerStudyAnalytics) {
          addUploadedEventLogs(eventLogs.eventLogsToUploadList.first())
        }
        removeEventLogsToUpload(0)
      }.build() to eventLogs.eventLogsToUploadList.first()
    }.also {
      it.invokeOnCompletion { error ->
        error?.let { consoleLogger.e("AnalyticsController", "Failed to remove event log.", error) }
      }
    }
  }

  /** Listens to the flow emitted by the [ConsoleLogger] and logs the error messages. */
  fun listenForConsoleErrorLogs() {
    CoroutineScope(backgroundDispatcher).launch {
      consoleLogger.logErrorMessagesFlow.collect { consoleLoggerContext ->
        logLowPriorityEvent(
          oppiaLogger.createConsoleLogContext(
            logLevel = consoleLoggerContext.logLevel,
            logTag = consoleLoggerContext.logTag,
            errorLog = consoleLoggerContext.fullErrorLog
          ),
          profileId = null
        )
      }
    }
  }

  /**
   * Listens to the flow emitted by the [NetworkLoggingInterceptor] relating to retrofit calls and
   * logs the network call information.
   */
  fun listenForNetworkCallLogs() {
    CoroutineScope(backgroundDispatcher).launch {
      networkLoggingInterceptor.logNetworkCallFlow.collect { retrofitCallContext ->
        logLowPriorityEvent(
          oppiaLogger.createRetrofitCallContext(
            url = retrofitCallContext.requestUrl,
            headers = retrofitCallContext.headers,
            body = retrofitCallContext.body,
            responseCode = retrofitCallContext.responseStatusCode,
          ),
          profileId = null
        )
      }
    }
  }

  /**
   * Listens to the flow emitted by the [NetworkLoggingInterceptor] relating to failed retrofit
   * calls and logs the network call information to the [OppiaLogger].
   */
  fun listenForFailedNetworkCallLogs() {
    CoroutineScope(backgroundDispatcher).launch {
      networkLoggingInterceptor.logFailedNetworkCallFlow.collect { retrofitFailedCallContext ->
        logLowPriorityEvent(
          oppiaLogger.createRetrofitCallFailedContext(
            url = retrofitFailedCallContext.requestUrl,
            headers = retrofitFailedCallContext.headers,
            body = retrofitFailedCallContext.body,
            responseCode = retrofitFailedCallContext.responseStatusCode,
            errorMessage = retrofitFailedCallContext.errorMessage,
          ),
          profileId = null
        )
      }
    }
  }

  /** Logs an [EventLog.CompleteAppOnboardingContext] event with the given [ProfileId]. */
  fun logAppOnboardedEvent(profileId: ProfileId?) {
    logLowPriorityEvent(
      oppiaLogger.createAppOnBoardingContext(),
      profileId = profileId
    )
  }

  /** Logs an [EventLog.ProfileOnboardingContext] event with the given [ProfileId]. */
  fun logProfileOnboardingStartedContext(profileId: ProfileId) {
    logLowPriorityEvent(
      oppiaLogger.createProfileOnboardingStartedContext(profileId),
      profileId = profileId
    )
  }

  /** Logs an [EventLog.ProfileOnboardingContext] event with the given [ProfileId]. */
  fun logProfileOnboardingEndedContext(profileId: ProfileId) {
    logLowPriorityEvent(
      oppiaLogger.createProfileOnboardingEndedContext(profileId),
      profileId = profileId
    )
  }

  private companion object {
    private suspend fun <T> resolveProfileOperation(
      profileId: ProfileId?,
      createProvider: (ProfileId) -> DataProvider<T>
    ): T? = profileId?.let { (createProvider(it).retrieveData() as? AsyncResult.Success<T>)?.value }
  }
}
