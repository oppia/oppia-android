package org.oppia.android.domain.exploration

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.TopicLearningTime
import org.oppia.android.app.model.TopicLearningTimeDatabase
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleListener
import org.oppia.android.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.transform
import org.oppia.android.util.platformparameter.EnableNpsSurvey
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.system.OppiaClock
import org.oppia.android.util.threading.BackgroundDispatcher
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val CACHE_NAME = "topic_learning_time_database"
private const val RECORD_AGGREGATE_LEARNING_TIME_PROVIDER_ID =
  "record_aggregate_learning_time_provider_id"
private const val RETRIEVE_AGGREGATE_LEARNING_TIME_PROVIDER_ID =
  "retrieve_aggregate_learning_time_provider_id"
private const val BEGIN_SESSION_TIMER_PROVIDER_ID =
  "begin_session_timer_provider_id"
private const val STOP_SESSION_TIMER_PROVIDER_ID =
  "stop_session_timer_provider_id"
private const val PAUSE_SESSION_TIMER_PROVIDER_ID =
  "pause_session_timer_provider_id"
private const val RESUME_SESSION_TIMER_PROVIDER_ID =
  "resume_session_timer_provider_id"
private val LEARNING_TIME_EXPIRATION_MILLIS = TimeUnit.DAYS.toMillis(10)

/**
 * A default session ID to be used before a session has been initialized.
 *
 * This session ID will never match, so messages that are received with this ID will never be
 * processed.
 */
private const val DEFAULT_SESSION_ID = "default_session_id"

/** Controller for tracking the amount of active time a user has spent in a topic. */
@Singleton
class ExplorationActiveTimeController @Inject constructor(
  private val oppiaClock: OppiaClock,
  private val cacheStoreFactory: PersistentCacheStore.Factory,
  private val dataProviders: DataProviders,
  private val oppiaLogger: OppiaLogger,
  private val exceptionsController: ExceptionsController,
  @BackgroundDispatcher private val backgroundCoroutineDispatcher: CoroutineDispatcher,
  @EnableNpsSurvey private val enableNpsSurvey: PlatformParameterValue<Boolean>
) : ExplorationProgressListener, ApplicationLifecycleListener {
  private var isAppInForeground: Boolean = false
  private var explorationStarted: Boolean = false

  private var mostRecentCommandQueue: SendChannel<ControllerMessage<*>>? = null

  private var mostRecentSessionId = MutableStateFlow<String?>(null)
  private val activeSessionId: String
    get() = mostRecentSessionId.value ?: DEFAULT_SESSION_ID

  /**
   * Statuses correspond to the exceptions such that if the deferred contains an error state,
   * a corresponding exception will be passed to a failed AsyncResult.
   */
  private enum class TopicLearningTimeActionStatus {
    /** Corresponds to a successful AsyncResult. */
    SUCCESS
  }

  private val cacheStoreMap =
    mutableMapOf<ProfileId, PersistentCacheStore<TopicLearningTimeDatabase>>()

  override fun onExplorationStarted(profileId: ProfileId, topicId: String) {
    this.explorationStarted = true
    if (enableNpsSurvey.value) {
      startSessionTimer(
        profileId = profileId,
        topicId = topicId,
        isAppInForeground = getIsAppInForeground(),
        explorationStarted = true
      )
    }
  }

  override fun onExplorationEnded() {
    this.explorationStarted = false
    if (enableNpsSurvey.value) {
      stopSessionTimerAsync(getIsExplorationStarted())
    }
  }

  override fun onAppInForeground() {
    this.isAppInForeground = true
    if (enableNpsSurvey.value) {
      resumeSessionTimer(getIsExplorationStarted())
    }
  }

  override fun onAppInBackground() {
    this.isAppInForeground = false
    if (enableNpsSurvey.value) {
      pauseSessionTimerAsync()
    }
  }

  private fun getIsAppInForeground() = this.isAppInForeground
  private fun getIsExplorationStarted() = this.explorationStarted

  /**
   * Begin tracking the active learning time in an exploration.
   *
   * We define the active time loosely as the time spent in an exploration when the app is in the
   * foreground.
   *
   * This method is called when the [ExplorationProgressController.beginExplorationImpl] finishes
   * executing successfully, or when the app comes back to the foreground after being previously
   * backgrounded.
   */
  private fun startSessionTimer(
    isAppInForeground: Boolean,
    explorationStarted: Boolean,
    profileId: ProfileId,
    topicId: String
  ): DataProvider<Any?> {
    val sessionId = UUID.randomUUID().toString().also {
      mostRecentCommandQueue = createControllerCommandActor()
    }
    val beginSessionTimerResultFlow = createAsyncResultStateFlow<Any?>()
    val message = ControllerMessage.InitializeController(
      isAppInForeground = isAppInForeground,
      isExplorationStarted = explorationStarted,
      profileId = profileId,
      topicId = topicId,
      sessionId = sessionId,
      callbackFlow = beginSessionTimerResultFlow
    )
    sendCommandForOperation(message) {
      "Failed to schedule command for initializing the exploration timer controller."
    }
    return beginSessionTimerResultFlow.convertToSessionProvider(BEGIN_SESSION_TIMER_PROVIDER_ID)
  }

  /**
   * Indicates that the current session being timed is now completed, and returns a
   * [DataProvider] indicating whether the cleanup was successful.
   *
   * This method is called when the [ExplorationProgressController.finishExplorationImpl] finishes
   * executing, or when the app goes to the background during an active exploration session.
   */
  private fun stopSessionTimerAsync(isExplorationStarted: Boolean): DataProvider<Any?> {
    val stopTimerResultFlow = createAsyncResultStateFlow<Any?>()
    val message = ControllerMessage.StopSessionTimer(
      sessionId = activeSessionId,
      callbackFlow = stopTimerResultFlow,
      isExplorationStarted = isExplorationStarted
    )
    sendCommandForOperation(message) {
      "Failed to schedule command for cleaning up after stopping the session timer."
    }
    return stopTimerResultFlow.convertToSessionProvider(
      STOP_SESSION_TIMER_PROVIDER_ID
    ).also {
      // Reset state to ensure post-session events don't expect any particular state from the
      // previous command queue.
      mostRecentSessionId.value = null
      mostRecentCommandQueue = null
    }
  }

  /**
   * Indicates that the timer mechanism has been paused due to the app going to the background and
   * saves the time spent in the session so far.
   *
   * @return a [DataProvider] indicating whether the pausing was successful
   */
  private fun pauseSessionTimerAsync(): DataProvider<Any?> {
    val pauseTimerResultFlow = createAsyncResultStateFlow<Any?>()
    val message = ControllerMessage.PauseSessionTimer(
      sessionId = activeSessionId,
      callbackFlow = pauseTimerResultFlow,
      isAppInForeground = false
    )
    sendCommandForOperation(message) {
      "Failed to schedule command for pausing the session timer."
    }
    return pauseTimerResultFlow.convertToSessionProvider(PAUSE_SESSION_TIMER_PROVIDER_ID)
  }

  /**
   * Resume tracking the active learning time in an exploration when the application is back in the
   * foreground after being previously backgrounded.
   */
  private fun resumeSessionTimer(isExplorationStarted: Boolean): DataProvider<Any?> {
    val resumeSessionTimerResultFlow = createAsyncResultStateFlow<Any?>()
    val message = ControllerMessage.ResumeSessionTimer(
      isAppInForeground = true,
      isExplorationStarted = isExplorationStarted,
      sessionId = activeSessionId,
      callbackFlow = resumeSessionTimerResultFlow
    )
    sendCommandForOperation(message) {
      "Failed to schedule command for resuming the session timer."
    }
    return resumeSessionTimerResultFlow.convertToSessionProvider(RESUME_SESSION_TIMER_PROVIDER_ID)
  }

  @OptIn(ObsoleteCoroutinesApi::class)
  private fun createControllerCommandActor(): SendChannel<ControllerMessage<*>> {
    lateinit var controllerState: ControllerState
    // Use an unlimited capacity buffer so that commands can be sent asynchronously without blocking
    // the main thread or scheduling an extra coroutine.
    @Suppress("JoinDeclarationAndAssignment") // Warning is incorrect in this case.
    lateinit var commandQueue: SendChannel<ControllerMessage<*>>
    commandQueue = CoroutineScope(
      backgroundCoroutineDispatcher
    ).actor(capacity = Channel.UNLIMITED) {
      for (message in channel) {
        try {
          @Suppress("UNUSED_VARIABLE") // A variable is used to create an exhaustive when statement.
          val unused = when (message) {
            is ControllerMessage.InitializeController -> {
              controllerState = ControllerState(
                TimerSessionState(),
                sessionId = message.sessionId,
                commandQueue = commandQueue
              ).also {
                it.beginTimerImpl(
                  beginTimerResultFlow = message.callbackFlow,
                  profileId = message.profileId,
                  topicId = message.topicId,
                  isAppInForeground = message.isAppInForeground,
                  isExplorationStarted = message.isExplorationStarted
                )
              }
            }
            is ControllerMessage.PauseSessionTimer -> {
              controllerState.pauseTimerImpl(message.callbackFlow, message.isAppInForeground)
            }
            is ControllerMessage.ResumeSessionTimer -> {
              controllerState.resumeTimerImpl(
                message.callbackFlow,
                message.isAppInForeground,
                message.isExplorationStarted
              )
            }
            is ControllerMessage.StopSessionTimer -> {
              try {
                controllerState.stopTimerImpl(
                  stopTimerResultFlow = message.callbackFlow,
                  message.isExplorationStarted
                )
              } finally {
                // Ensure the actor ends since the session requires no further message processing.
                break
              }
            }
          }
        } catch (e: Exception) {
          exceptionsController.logNonFatalException(e)
          oppiaLogger.w(
            "ExplorationActiveTimeController",
            "Encountered exception while processing command: $message",
            e
          )
        }
      }
    }
    return commandQueue
  }

  private suspend fun ControllerState.beginTimerImpl(
    beginTimerResultFlow: MutableStateFlow<AsyncResult<Any?>>,
    isAppInForeground: Boolean,
    isExplorationStarted: Boolean,
    profileId: ProfileId,
    topicId: String,
  ) {
    tryOperation(beginTimerResultFlow) {
      timerSessionState.apply {
        sessionStartTime = oppiaClock.getCurrentTimeMs()
        currentProfileId = profileId
        currentTopicId = topicId
        this.isAppInForeground = isAppInForeground
        this.isExplorationStarted = isExplorationStarted
      }
    }
  }

  private suspend fun ControllerState.stopTimerImpl(
    stopTimerResultFlow: MutableStateFlow<AsyncResult<Any?>>,
    isExplorationStarted: Boolean
  ) {
    tryOperation(stopTimerResultFlow) {
      check(timerSessionState.isExplorationStarted) {
        "Expected an exploration to have been started."
      }

      timerSessionState.isExplorationStarted = isExplorationStarted

      val sessionDuration = (oppiaClock.getCurrentTimeMs() - timerSessionState.sessionStartTime)
      recordAggregateTopicLearningTime(
        profileId = timerSessionState.currentProfileId,
        topicId = timerSessionState.currentTopicId,
        sessionDuration = sessionDuration
      )
    }
  }

  private suspend fun ControllerState.pauseTimerImpl(
    pauseTimerResultFlow: MutableStateFlow<AsyncResult<Any?>>,
    isAppInForeground: Boolean
  ) {
    tryOperation(pauseTimerResultFlow) {
      check(timerSessionState.isAppInForeground && timerSessionState.isExplorationStarted) {
        "Expected app to be in the foreground and an exploration to be started."
      }

      timerSessionState.isAppInForeground = isAppInForeground

      val sessionDuration = (oppiaClock.getCurrentTimeMs() - timerSessionState.sessionStartTime)
      recordAggregateTopicLearningTime(
        profileId = timerSessionState.currentProfileId,
        topicId = timerSessionState.currentTopicId,
        sessionDuration = sessionDuration
      )
    }
  }

  private suspend fun ControllerState.resumeTimerImpl(
    resumeTimerResultFlow: MutableStateFlow<AsyncResult<Any?>>,
    isAppInForeground: Boolean,
    isExplorationStarted: Boolean
  ) {
    tryOperation(resumeTimerResultFlow) {
      check(!timerSessionState.isAppInForeground && isExplorationStarted) {
        "Expected app to be in the background and an exploration to be started."
      }

      timerSessionState.apply {
        this.sessionStartTime = oppiaClock.getCurrentTimeMs()
        this.isAppInForeground = isAppInForeground
      }
    }
  }

  /**
   * Represents the current synchronized state of the controller.
   *
   * This object's instance is tied directly to a single session, and it's not thread-safe so all
   * access must be synchronized.
   *
   * @property sessionId the GUID corresponding to the session
   * @property commandQueue the actor command queue executing all messages that change this state
   */
  private class ControllerState(
    val timerSessionState: TimerSessionState,
    val sessionId: String,
    val commandQueue: SendChannel<ControllerMessage<*>>
  )

  /**
   * Represents a message that can be sent to [mostRecentCommandQueue] to process changes to
   * [ControllerState] (ensure synchronous access to the data that determines timer state).
   *
   * Messages are expected to be resolved serially (though their scheduling can occur across
   * multiple threads, so order cannot be guaranteed until they're enqueued).
   */
  private sealed class ControllerMessage<T> {
    /** The session ID corresponding to this message. */
    abstract val sessionId: String

    /**
     * The [DataProvider]-tied [MutableStateFlow] that represents the result of the operation
     * corresponding to this message, or ``null`` if the caller doesn't care about observing the
     * result.
     */
    abstract val callbackFlow: MutableStateFlow<AsyncResult<T>>?

    /** [ControllerMessage] for initializing a session timer. */
    data class InitializeController(
      val isAppInForeground: Boolean,
      val isExplorationStarted: Boolean,
      val profileId: ProfileId,
      val topicId: String,
      override val sessionId: String,
      override val callbackFlow: MutableStateFlow<AsyncResult<Any?>>
    ) : ControllerMessage<Any?>()

    /** [ControllerMessage] for stopping a session timer. */
    data class StopSessionTimer(
      override val sessionId: String,
      override val callbackFlow: MutableStateFlow<AsyncResult<Any?>>,
      val isExplorationStarted: Boolean
    ) : ControllerMessage<Any?>()

    /** [ControllerMessage] for pausing a session timer. */
    data class PauseSessionTimer(
      val isAppInForeground: Boolean,
      override val sessionId: String,
      override val callbackFlow: MutableStateFlow<AsyncResult<Any?>>
    ) : ControllerMessage<Any?>()

    /** [ControllerMessage] for resuming a session timer. */
    data class ResumeSessionTimer(
      val isAppInForeground: Boolean,
      val isExplorationStarted: Boolean,
      override val sessionId: String,
      override val callbackFlow: MutableStateFlow<AsyncResult<Any?>>
    ) : ControllerMessage<Any?>()
  }

  private fun <T> sendCommandForOperation(
    message: ControllerMessage<T>,
    lazyFailureMessage: () -> String
  ) {
    val commandQueue = mostRecentCommandQueue
    val flowResult: AsyncResult<T> = when {
      commandQueue == null ->
        AsyncResult.Failure(IllegalStateException("Session isn't initialized yet."))
      !commandQueue.trySend(message).isSuccess ->
        AsyncResult.Failure(IllegalStateException(lazyFailureMessage()))
      // Ensure that the result is first reset since there will be a delay before the message is
      // processed (if there's a flow).
      else -> AsyncResult.Pending()
    }

    // This must be assigned separately since flowResult should always be calculated, even if
    // there's no callbackFlow to report it.
    message.callbackFlow?.value = flowResult
  }

  private fun <T> StateFlow<AsyncResult<T>>.convertToSessionProvider(
    baseId: String
  ): DataProvider<T> = dataProviders.run {
    convertAsyncToAutomaticDataProvider("${baseId}_$activeSessionId")
  }

  private fun <T> createAsyncResultStateFlow(initialValue: AsyncResult<T> = AsyncResult.Pending()) =
    MutableStateFlow(initialValue)

  private suspend fun <T> ControllerState.tryOperation(
    resultFlow: MutableStateFlow<AsyncResult<T>>,
    operation: suspend ControllerState.() -> T
  ) {
    try {
      resultFlow.emit(AsyncResult.Success(operation()))
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e)
      resultFlow.emit(AsyncResult.Failure(e))
    }
  }

  /**
   * Records the tracked active exploration time.
   *
   * @param profileId the ID corresponding to the profile for which progress needs to be stored
   * @param topicId the ID corresponding to the topic for which duration needs to be stored
   * @param sessionDuration the tracked exploration duration between start and pause
   * @return a [DataProvider] that indicates the success/failure of this record operation
   */
  private fun recordAggregateTopicLearningTime(
    profileId: ProfileId,
    topicId: String,
    sessionDuration: Long
  ): DataProvider<Any?> {
    val deferred = retrieveCacheStore(profileId).storeDataWithCustomChannelAsync(
      updateInMemoryCache = true
    ) { topicLearningTimeDatabase ->
      topicLearningTimeDatabase.toBuilder().apply {
        val topicLearningTime =
          aggregateTopicLearningTimeMap.getOrDefault(
            topicId,
            TopicLearningTime.newBuilder().setTopicId(topicId).build()
          ).toBuilder().apply {
            topicLearningTimeMs = if (isLastUpdatedTimestampStale(lastUpdatedTimeMs))
              sessionDuration
            else
              topicLearningTimeMs + sessionDuration
            lastUpdatedTimeMs = oppiaClock.getCurrentTimeMs()
          }.build()
        putAggregateTopicLearningTime(topicId, topicLearningTime)
      }.build() to TopicLearningTimeActionStatus.SUCCESS
    }
    return dataProviders.createInMemoryDataProviderAsync(
      RECORD_AGGREGATE_LEARNING_TIME_PROVIDER_ID
    ) {
      return@createInMemoryDataProviderAsync getDeferredResult(deferred)
    }
  }

  private fun isLastUpdatedTimestampStale(lastUpdatedTimestamp: Long): Boolean {
    val currentTime = oppiaClock.getCurrentTimeMs()
    val differenceInMillis = currentTime - lastUpdatedTimestamp
    return differenceInMillis > LEARNING_TIME_EXPIRATION_MILLIS
  }

  /** Returns the [TopicLearningTime] [DataProvider] for a specific topicId, per-profile basis. */
  fun retrieveAggregateTopicLearningTimeDataProvider(
    profileId: ProfileId,
    topicId: String
  ): DataProvider<TopicLearningTime> {
    return retrieveCacheStore(profileId)
      .transform(RETRIEVE_AGGREGATE_LEARNING_TIME_PROVIDER_ID) { learningTimeDb ->
        learningTimeDb.aggregateTopicLearningTimeMap[topicId]
          ?: TopicLearningTime.getDefaultInstance()
      }
  }

  private suspend fun getDeferredResult(
    deferred: Deferred<TopicLearningTimeActionStatus>
  ): AsyncResult<Any?> {
    return when (deferred.await()) {
      TopicLearningTimeActionStatus.SUCCESS -> AsyncResult.Success(null)
    }
  }

  private fun retrieveCacheStore(
    profileId: ProfileId
  ): PersistentCacheStore<TopicLearningTimeDatabase> {
    return cacheStoreMap.getOrPut(profileId) {
      cacheStoreFactory.createPerProfile(
        CACHE_NAME,
        TopicLearningTimeDatabase.getDefaultInstance(),
        profileId
      ).also { cacheStore ->
        cacheStore.primeInMemoryAndDiskCacheAsync(
          updateMode = PersistentCacheStore.UpdateMode.UPDATE_IF_NEW_CACHE,
          publishMode = PersistentCacheStore.PublishMode.PUBLISH_TO_IN_MEMORY_CACHE
        ).invokeOnCompletion {
          if (it != null) {
            oppiaLogger.e(
              "ExplorationActiveTimeController",
              "Failed to prime cache ahead of data retrieval.",
              it
            )
          }
        }
      }
    }
  }
}
