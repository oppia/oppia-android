package org.oppia.android.domain.exploration

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import org.oppia.android.app.model.AnswerOutcome
import org.oppia.android.app.model.CheckpointState
import org.oppia.android.app.model.EphemeralState
import org.oppia.android.app.model.Exploration
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.HelpIndex.IndexTypeCase.EVERYTHING_REVEALED
import org.oppia.android.app.model.HelpIndex.IndexTypeCase.INDEXTYPE_NOT_SET
import org.oppia.android.app.model.HelpIndex.IndexTypeCase.LATEST_REVEALED_HINT_INDEX
import org.oppia.android.app.model.HelpIndex.IndexTypeCase.NEXT_AVAILABLE_HINT_INDEX
import org.oppia.android.app.model.HelpIndex.IndexTypeCase.SHOW_SOLUTION
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.WrittenTranslationLanguageSelection
import org.oppia.android.domain.classify.AnswerClassificationController
import org.oppia.android.domain.exploration.ExplorationProgress.PlayStage.LOADING_EXPLORATION
import org.oppia.android.domain.exploration.ExplorationProgress.PlayStage.NOT_PLAYING
import org.oppia.android.domain.exploration.ExplorationProgress.PlayStage.SUBMITTING_ANSWER
import org.oppia.android.domain.exploration.ExplorationProgress.PlayStage.VIEWING_STATE
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationCheckpointController
import org.oppia.android.domain.hintsandsolution.HintHandler
import org.oppia.android.domain.oppialogger.LoggingIdentifierController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.analytics.LearnerAnalyticsLogger
import org.oppia.android.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.topic.StoryProgressController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.combineWith
import org.oppia.android.util.data.DataProviders.Companion.transform
import org.oppia.android.util.system.OppiaClock
import org.oppia.android.util.threading.BackgroundDispatcher
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val BEGIN_EXPLORATION_RESULT_PROVIDER_ID =
  "ExplorationProgressController.begin_exploration_result"
private const val FINISH_EXPLORATION_RESULT_PROVIDER_ID =
  "ExplorationProgressController.finish_exploration_result"
private const val SUBMIT_ANSWER_RESULT_PROVIDER_ID =
  "ExplorationProgressController.submit_answer_result"
private const val SUBMIT_HINT_REVEALED_RESULT_PROVIDER_ID =
  "ExplorationProgressController.submit_hint_revealed_result"
private const val SUBMIT_HINT_VIEWED_RESULT_PROVIDER_ID =
  "ExplorationProgressController.submit_hint_revealed_result"
private const val SUBMIT_SOLUTION_REVEALED_RESULT_PROVIDER_ID =
  "ExplorationProgressController.submit_solution_revealed_result"
private const val SUBMIT_SOLUTION_VIEWED_RESULT_PROVIDER_ID =
  "ExplorationProgressController.submit_solution_revealed_result"
private const val MOVE_TO_PREVIOUS_STATE_RESULT_PROVIDER_ID =
  "ExplorationProgressController.move_to_previous_state_result"
private const val MOVE_TO_NEXT_STATE_RESULT_PROVIDER_ID =
  "ExplorationProgressController.move_to_next_state_result"
private const val CURRENT_STATE_PROVIDER_ID = "ExplorationProgressController.current_state"
private const val LOCALIZED_STATE_PROVIDER_ID = "ExplorationProgressController.localized_state"
private const val UPDATE_WRITTEN_TRANSLATION_CONTENT_PROVIDER_ID =
  "ExplorationProgressController.update_written_translation_content"

/**
 * A default session ID to be used before a session has been initialized.
 *
 * This session ID will never match, so messages that are received with this ID will never be
 * processed.
 */
private const val DEFAULT_SESSION_ID = "default_session_id"

/**
 * Controller that tracks and reports the learner's ephemeral/non-persisted progress through an
 * exploration. Note that this controller only supports one active exploration at a time.
 *
 * The current exploration session is started via the exploration data controller.
 *
 * This class is not safe to use across multiple threads, and should only ever be interacted with
 * via the main thread. The controller makes use of multiple threads to offload all state
 * operations, so calls into this controller should return quickly and will never block. Each method
 * returns a [DataProvider] that can be observed for the future result of the method's corresponding
 * operation.
 *
 * Note that operations are guaranteed to execute in the order of controller method calls, internal
 * state is always kept internally consistent (so long-running [DataProvider] subscriptions for a
 * particular play session will receive updates), and state can never leak across session
 * boundaries (though re-subscription will be necessary to observe state in a new play session--see
 * [submitAnswer] and [getCurrentState] method KDocs for more details).
 */
@Singleton
class ExplorationProgressController @Inject constructor(
  private val explorationRetriever: ExplorationRetriever,
  private val answerClassificationController: AnswerClassificationController,
  private val exceptionsController: ExceptionsController,
  private val explorationCheckpointController: ExplorationCheckpointController,
  private val storyProgressController: StoryProgressController,
  private val oppiaClock: OppiaClock,
  private val oppiaLogger: OppiaLogger,
  private val hintHandlerFactory: HintHandler.Factory,
  private val translationController: TranslationController,
  private val dataProviders: DataProviders,
  private val loggingIdentifierController: LoggingIdentifierController,
  private val profileManagementController: ProfileManagementController,
  private val learnerAnalyticsLogger: LearnerAnalyticsLogger,
  @BackgroundDispatcher private val backgroundCoroutineDispatcher: CoroutineDispatcher,
  private val explorationProgressListeners: Set<@JvmSuppressWildcards ExplorationProgressListener>
) {
  // TODO(#3467): Update the mechanism to save checkpoints to eliminate the race condition that may
  //  arise if the function finishExplorationAsync acquires lock before the invokeOnCompletion
  //  callback on the deferred returned on saving checkpoints. In this case ExplorationActivity will
  //  make decisions based on a value of the checkpointState which might not be up-to date.

  // TODO(#606): Replace this with a profile scope to avoid this hacky workaround (which is needed
  //  for getCurrentState).
  private lateinit var profileId: ProfileId

  private var mostRecentSessionId = MutableStateFlow<String?>(null)
  private val activeSessionId: String
    get() = mostRecentSessionId.value ?: DEFAULT_SESSION_ID

  private var mostRecentEphemeralStateFlow =
    createAsyncResultStateFlow<EphemeralState>(
      AsyncResult.Failure(IllegalStateException("Exploration is not yet initialized."))
    )

  private var mostRecentCommandQueue: SendChannel<ControllerMessage<*>>? = null

  // The amount of time to wait before the continue interaction button is animated in milliseconds.
  private val continueButtonAnimationDelay: Long = TimeUnit.SECONDS.toMillis(45)

  /**
   * Resets this controller to begin playing the specified [Exploration], and returns a
   * [DataProvider] indicating whether the start was successful.
   *
   * The returned [DataProvider] has the same lifecycle considerations as the provider returned by
   * [submitAnswer].
   */
  internal fun beginExplorationAsync(
    profileId: ProfileId,
    classroomId: String,
    topicId: String,
    storyId: String,
    explorationId: String,
    shouldSavePartialProgress: Boolean,
    explorationCheckpoint: ExplorationCheckpoint,
    isRestart: Boolean
  ): DataProvider<Any?> {
    val ephemeralStateFlow = createAsyncResultStateFlow<EphemeralState>()
    val sessionId = UUID.randomUUID().toString().also {
      mostRecentSessionId.value = it
      mostRecentEphemeralStateFlow = ephemeralStateFlow
      mostRecentCommandQueue = createControllerCommandActor()
    }
    val beginExplorationResultFlow = createAsyncResultStateFlow<Any?>()
    val message =
      ControllerMessage.InitializeController(
        profileId,
        classroomId,
        topicId,
        storyId,
        explorationId,
        shouldSavePartialProgress,
        explorationCheckpoint,
        isRestart,
        ephemeralStateFlow,
        sessionId,
        beginExplorationResultFlow
      )
    this.profileId = profileId
    sendCommandForOperation(message) {
      "Failed to schedule command for initializing the exploration progress controller."
    }
    return beginExplorationResultFlow.convertToSessionProvider(BEGIN_EXPLORATION_RESULT_PROVIDER_ID)
  }

  /**
   * Indicates that the current exploration being played is now completed, and returns a
   * [DataProvider] indicating whether the cleanup was successful.
   *
   * The returned [DataProvider] has the same lifecycle considerations as the provider returned by
   * [submitAnswer] with one additional caveat: this method does not actually need to be called when
   * a session is over. Calling it ensures all other [DataProvider]s reset to a correct
   * out-of-session state, but subsequent calls to [beginExplorationAsync] will reset the session.
   *
   * @param isCompletion whether this finish action indicates that the exploration was finished by
   *     the user
   */
  internal fun finishExplorationAsync(isCompletion: Boolean): DataProvider<Any?> {
    val finishExplorationResultFlow = createAsyncResultStateFlow<Any?>()
    val message =
      ControllerMessage.FinishExploration(
        isCompletion, activeSessionId, finishExplorationResultFlow
      )
    sendCommandForOperation(message) {
      "Failed to schedule command for cleaning up after finishing the exploration."
    }
    return finishExplorationResultFlow.convertToSessionProvider(
      FINISH_EXPLORATION_RESULT_PROVIDER_ID
    ).also {
      // Reset state to ensure post-session events don't expect any particular state from the
      // previous command queue.
      mostRecentSessionId.value = null
      mostRecentCommandQueue = null
    }
  }

  /**
   * Submits an answer to the current state and returns how the UI should respond to this answer.
   *
   * If the app undergoes a configuration change, calling code should rely on the [DataProvider]
   * from [getCurrentState] to know whether a current answer is pending. That [DataProvider] will
   * have its state changed to pending during answer submission and until answer resolution.
   *
   * Submitting an answer should result in the learner staying in the current state, moving to a new
   * state in the exploration, being shown a concept card, or being navigated to another exploration
   * altogether. Note that once a correct answer is processed, the current state reported to
   * [getCurrentState] will change from a pending state to a completed state since the learner
   * completed that card. The learner can then proceed from the current completed state to the next
   * pending state using [moveToNextState].
   *
   * ### Lifecycle behavior
   * The returned [DataProvider] will initially be pending until the operation completes (unless
   * called before a session is started). Note that a different provider is returned for each call,
   * though it's tied to the same session so it can be monitored medium-term (i.e. for the duration
   * of the play session, but not past it). Furthermore, the returned provider does not actually
   * need to be monitored in order for the operation to complete, though it's recommended since
   * [getCurrentState] can only be used to monitor the effects of the operation, not whether the
   * operation itself succeeded.
   *
   * If this is called before a session begins it will return a provider that stays failing with no
   * updates. The operation will also silently fail rather than queue up in these circumstances, so
   * starting a session will not trigger an answer submission from an older call.
   *
   * Multiple subsequent calls during a valid session will queue up and have results delivered in
   * order (though based on the eventual consistency nature of [DataProvider]s no assumptions can be
   * made about whether all results will actually be received--[getCurrentState] should be used as
   * the source of truth for the current state of the session).
   *
   * No assumptions should be made about the completion order of the returned [DataProvider] vs. the
   * [DataProvider] from [getCurrentState].
   */
  fun submitAnswer(userAnswer: UserAnswer): DataProvider<AnswerOutcome> {
    val submitResultFlow = createAsyncResultStateFlow<AnswerOutcome>()
    val message = ControllerMessage.SubmitAnswer(userAnswer, activeSessionId, submitResultFlow)
    sendCommandForOperation(message) { "Failed to schedule command for answer submission." }
    return submitResultFlow.convertToSessionProvider(SUBMIT_ANSWER_RESULT_PROVIDER_ID)
  }

  /**
   * Notifies the controller that the user wishes to reveal a hint.
   *
   * The returned [DataProvider] has the same lifecycle considerations as the provider returned by
   * [submitAnswer].
   *
   * @param hintIndex index of the hint that was revealed in the hint list of the current pending
   *     state
   * @return a [DataProvider] that indicates success/failure of the operation (the actual payload of
   *     the result isn't relevant)
   */
  fun submitHintIsRevealed(hintIndex: Int): DataProvider<Any?> {
    val submitResultFlow = createAsyncResultStateFlow<Any?>()
    val message = ControllerMessage.HintIsRevealed(hintIndex, activeSessionId, submitResultFlow)
    sendCommandForOperation(message) {
      "Failed to schedule command for revealing hint: $hintIndex."
    }
    return submitResultFlow.convertToSessionProvider(SUBMIT_HINT_REVEALED_RESULT_PROVIDER_ID)
  }

  /**
   * Notifies the controller that the user has viewed a hint.
   *
   * @param hintIndex index of the hint that is being viewed
   *
   * @return a [DataProvider] that indicates success/failure of the operation (the actual payload of
   *     the result isn't relevant)
   */

  fun submitHintIsViewed(hintIndex: Int): DataProvider<Any?> {
    val submitResultFlow = createAsyncResultStateFlow<Any?>()
    val message = ControllerMessage.LogHintIsViewed(hintIndex, activeSessionId, submitResultFlow)
    sendCommandForOperation(message) {
      "Failed to schedule command for viewing hint: $hintIndex."
    }
    return submitResultFlow.convertToSessionProvider(SUBMIT_HINT_VIEWED_RESULT_PROVIDER_ID)
  }

  /**
   * Notifies the controller that the user has revealed the solution to the current state.
   *
   * The returned [DataProvider] has the same lifecycle considerations as the provider returned by
   * [submitAnswer].
   *
   * @return a [DataProvider] that indicates success/failure of the operation (the actual payload of
   *     the result isn't relevant)
   */
  fun submitSolutionIsRevealed(): DataProvider<Any?> {
    val submitResultFlow = createAsyncResultStateFlow<Any?>()
    val message = ControllerMessage.SolutionIsRevealed(activeSessionId, submitResultFlow)
    sendCommandForOperation(message) { "Failed to schedule command for revealing the solution." }
    return submitResultFlow.convertToSessionProvider(SUBMIT_SOLUTION_REVEALED_RESULT_PROVIDER_ID)
  }

  /**
   * Notifies the controller that the user has viewed the answer.
   * @return a [DataProvider] that indicates success/failure of the operation (the actual payload of
   *     the result isn't relevant)
   */
  fun submitSolutionIsViewed(): DataProvider<Any?> {
    val submitResultFlow = createAsyncResultStateFlow<Any?>()
    val message = ControllerMessage.LogSolutionIsViewed(activeSessionId, submitResultFlow)
    sendCommandForOperation(message) { "Failed to schedule command for viewing the solution." }
    return submitResultFlow.convertToSessionProvider(SUBMIT_SOLUTION_VIEWED_RESULT_PROVIDER_ID)
  }

  /**
   * Navigates to the previous state in the graph. If the learner is currently on the initial state,
   * this method will throw an exception. Calling code is responsible for ensuring this method is
   * only called when it's possible to navigate backward.
   *
   * The returned [DataProvider] has the same lifecycle considerations as the provider returned by
   * [submitAnswer].
   *
   * @return a [DataProvider] indicating whether the movement to the previous state was successful,
   *     or a failure if state navigation was attempted at an invalid time in the state graph (e.g.
   *     if currently viewing the initial state of the exploration). It's recommended that calling
   *     code only listen to this result for failures, and instead rely on [getCurrentState] for
   *     observing a successful transition to another state.
   */
  fun moveToPreviousState(): DataProvider<Any?> {
    val moveResultFlow = createAsyncResultStateFlow<Any?>()
    val message = ControllerMessage.MoveToPreviousState(activeSessionId, moveResultFlow)
    sendCommandForOperation(message) {
      "Failed to schedule command for moving to the previous state."
    }
    return moveResultFlow.convertToSessionProvider(MOVE_TO_PREVIOUS_STATE_RESULT_PROVIDER_ID)
  }

  /**
   * Navigates to the next state in the graph. This method is only valid if the current
   * [EphemeralState] reported by [getCurrentState] is a completed state. Calling code is
   * responsible for ensuring this method is only called when it's possible to navigate forward.
   *
   * Note that if the current state is a pending state, the user needs to submit a correct answer
   * that routes to a later state via [submitAnswer] in order for the current state to change to a
   * completed state before forward navigation can occur.
   *
   * The returned [DataProvider] has the same lifecycle considerations as the provider returned by
   * [submitAnswer].
   *
   * @return a [DataProvider] indicating whether the movement to the next state was successful (see
   *     [moveToPreviousState] for details on potential failure cases)
   */
  fun moveToNextState(): DataProvider<Any?> {
    val moveResultFlow = createAsyncResultStateFlow<Any?>()
    val message = ControllerMessage.MoveToNextState(activeSessionId, moveResultFlow)
    sendCommandForOperation(message) { "Failed to schedule command for moving to the next state." }
    return moveResultFlow.convertToSessionProvider(MOVE_TO_NEXT_STATE_RESULT_PROVIDER_ID)
  }

  /**
   * Returns a [DataProvider] monitoring the current [EphemeralState] the learner is currently
   * viewing.
   *
   * If this state corresponds to a a terminal state, then the learner has completed the
   * exploration. Note that [moveToPreviousState] and [moveToNextState] will automatically update
   * observers of this data provider when the next state is navigated to.
   *
   * This [DataProvider] may initially be pending while the exploration object is loaded. It may
   * also switch from a completed to a pending result during transient operations like submitting an
   * answer via [submitAnswer]. Calling code should be made resilient to this by caching the current
   * state object to display since it may disappear temporarily during answer submission. Calling
   * code should persist this state object across configuration changes if needed since it cannot
   * rely on this [DataProvider] for immediate state reconstitution after configuration changes.
   *
   * The underlying state returned by this function can only be changed by calls to
   * [moveToNextState] and [moveToPreviousState], or the exploration data controller if another
   * exploration is loaded. UI code cannot assume that only calls from the UI layer will trigger
   * state changes here since internal domain processes may also affect state (such as hint timers).
   *
   * This method is safe to be called before the exploration has started, but the returned provider
   * is tied to the current play session (similar to the provider returned by [submitAnswer]), so
   * the returned [DataProvider] prior to [beginExplorationAsync] being called will be a permanently
   * failing provider. Furthermore, the returned provider will not be updated after the play session
   * has ended (either due to [finishExplorationAsync] being called, or a new session starting).
   * There will be a [DataProvider] available immediately after [beginExplorationAsync] returns,
   * though it may not ever provide useful data if the start of the session failed (which can only
   * be observed via the provider returned by [beginExplorationAsync]).
   *
   * This method does not actually need to be called for the [EphemeralState] to be computed; it's
   * always computed eagerly by other state-changing methods regardless of whether there's an active
   * subscription to this method's returned [DataProvider].
   */
  fun getCurrentState(): DataProvider<EphemeralState> {
    val writtenTranslationContentLocale =
      translationController.getWrittenTranslationContentLocale(profileId)
    val ephemeralStateDataProvider =
      mostRecentEphemeralStateFlow.convertToSessionProvider(CURRENT_STATE_PROVIDER_ID)
    return writtenTranslationContentLocale.combineWith(
      ephemeralStateDataProvider, LOCALIZED_STATE_PROVIDER_ID
    ) { locale, ephemeralState ->
      ephemeralState.toBuilder().apply {
        // Augment the state to include translation information (which may not necessarily be
        // up-to-date in the state deck).
        writtenTranslationContext =
          translationController.computeWrittenTranslationContext(
            state.writtenTranslationsMap, locale
          )
      }.build()
    }
  }

  /**
   * Updates the current written content language for the specified [profileId] and [selection]
   * mid-lesson.
   *
   * See [TranslationController.updateWrittenTranslationContentLanguage] for specifics.
   *
   * Note that this function should be used for special-cased in-lesson language switching (that is,
   * language switching that's only enabled via a per-profile setting and as part of a user study).
   *
   * @return a [DataProvider] that indicates the success/failure of attempting to update the content
   *     language
   */
  fun updateWrittenTranslationContentLanguageMidLesson(
    profileId: ProfileId,
    selection: WrittenTranslationLanguageSelection
  ): DataProvider<Any> {
    return translationController.updateWrittenTranslationContentLanguage(
      profileId, selection
    ).transform(UPDATE_WRITTEN_TRANSLATION_CONTENT_PROVIDER_ID) { previousSelection ->
      val explorationLogger = learnerAnalyticsLogger.explorationAnalyticsLogger.value
      val stateLogger = explorationLogger?.stateAnalyticsLogger?.value
      stateLogger?.logSwitchInLessonLanguage(
        fromLanguage = previousSelection.selectedLanguage,
        toLanguage = selection.selectedLanguage
      ) ?: Unit
    }
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
              // Synchronously fetch the learner & installation IDs (these may result in file I/O).
              val learnerId = profileManagementController.fetchLearnerId(message.profileId)
              val installationId = loggingIdentifierController.fetchInstallationId()
              val isContinueButtonAnimationSeen =
                profileManagementController.fetchContinueAnimationSeenStatus(
                  message.profileId
                ) ?: false

              // Ensure the state is completely recreated for each session to avoid leaking state
              // across sessions.
              controllerState =
                ControllerState(
                  ExplorationProgress(),
                  message.isRestart,
                  // The [message.explorationCheckpoint] is [ExplorationCheckpoint.getDefaultInstance()]
                  // in the following 3 cases.
                  //  - New exploration is started.
                  //  - Saved Exploration is restarted.
                  //  - Completed exploration is replayed.
                  // The [message.explorationCheckpoint] will contain the exploration checkpoint
                  // only when a saved exploration is resumed.
                  isResume = message.explorationCheckpoint
                    != ExplorationCheckpoint.getDefaultInstance(),
                  message.sessionId,
                  message.ephemeralStateFlow,
                  commandQueue,
                  installationId,
                  message.profileId,
                  learnerId,
                  learnerAnalyticsLogger,
                  startSessionTimeMs = oppiaClock.getCurrentTimeMs(),
                  isContinueButtonAnimationSeen
                ).also {
                  it.beginExplorationImpl(
                    message.callbackFlow,
                    message.profileId,
                    message.classroomId,
                    message.topicId,
                    message.storyId,
                    message.explorationId,
                    message.shouldSavePartialProgress,
                    message.explorationCheckpoint
                  )
                }
            }
            is ControllerMessage.FinishExploration -> {
              try {
                // Ensure finish is always executed even if the controller state isn't yet
                // initialized.
                controllerState.finishExplorationImpl(message.callbackFlow, message.isCompletion)
              } finally {
                // Ensure the actor ends since the session requires no further message processing.
                break
              }
            }
            is ControllerMessage.SubmitAnswer ->
              controllerState.submitAnswerImpl(message.callbackFlow, message.userAnswer)
            is ControllerMessage.HintIsRevealed -> {
              controllerState.submitHintIsRevealedImpl(message.callbackFlow, message.hintIndex)
            }
            is ControllerMessage.LogHintIsViewed ->
              controllerState.logViewedHintImpl(
                activeSessionId, message.hintIndex, message.callbackFlow
              )
            is ControllerMessage.SolutionIsRevealed ->
              controllerState.submitSolutionIsRevealedImpl(message.callbackFlow)
            is ControllerMessage.LogSolutionIsViewed ->
              controllerState.logViewedSolutionImpl(activeSessionId, message.callbackFlow)
            is ControllerMessage.MoveToPreviousState ->
              controllerState.moveToPreviousStateImpl(message.callbackFlow)
            is ControllerMessage.MoveToNextState ->
              controllerState.moveToNextStateImpl(message.callbackFlow)
            is ControllerMessage.LogUpdatedHelpIndex ->
              controllerState.maybeLogUpdatedHelpIndex(message.helpIndex, activeSessionId)
            is ControllerMessage.ProcessSavedCheckpointResult -> {
              controllerState.processSaveCheckpointResult(
                message.profileId,
                message.topicId,
                message.storyId,
                message.explorationId,
                message.lastPlayedTimestamp,
                message.newCheckpointState
              )
            }
            is ControllerMessage.SaveCheckpoint -> controllerState.saveExplorationCheckpoint()
            is ControllerMessage.RecomputeStateAndNotify ->
              controllerState.recomputeCurrentStateAndNotifyImpl()
          }
        } catch (e: Exception) {
          exceptionsController.logNonFatalException(e)
          oppiaLogger.w(
            "ExplorationProgressController",
            "Encountered exception while processing command: $message",
            e
          )
        }
      }
    }
    return commandQueue
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

  private suspend fun ControllerState.beginExplorationImpl(
    beginExplorationResultFlow: MutableStateFlow<AsyncResult<Any?>>,
    profileId: ProfileId,
    classroomId: String,
    topicId: String,
    storyId: String,
    explorationId: String,
    shouldSavePartialProgress: Boolean,
    explorationCheckpoint: ExplorationCheckpoint
  ) {
    tryOperation(beginExplorationResultFlow) {
      check(explorationProgress.playStage == NOT_PLAYING) {
        "Expected to finish previous exploration before starting a new one."
      }

      explorationProgress.apply {
        currentProfileId = profileId
        currentClassroomId = classroomId
        currentTopicId = topicId
        currentStoryId = storyId
        currentExplorationId = explorationId
        this.shouldSavePartialProgress = shouldSavePartialProgress
        checkpointState = CheckpointState.CHECKPOINT_UNSAVED
        this.explorationCheckpoint = explorationCheckpoint
      }
      hintHandler = hintHandlerFactory.create()
      hintHandler.getCurrentHelpIndex().onFirstAndEach {
        commandQueue.send(ControllerMessage.LogUpdatedHelpIndex(it, sessionId))

        // Fire an event to save the latest progress state in a checkpoint to avoid cross-thread
        // synchronization being required (since the state of hints/solutions has changed).
        commandQueue.send(ControllerMessage.SaveCheckpoint(sessionId))
        recomputeCurrentStateAndNotifyAsync()
      }.launchIn(CoroutineScope(backgroundCoroutineDispatcher))
      explorationProgress.advancePlayStageTo(LOADING_EXPLORATION)
      explorationProgressListeners.forEach {
        it.onExplorationStarted(
          profileId = profileId,
          topicId = explorationProgress.currentTopicId
        )
      }
    }
  }

  private suspend fun ControllerState?.finishExplorationImpl(
    finishExplorationResultFlow: MutableStateFlow<AsyncResult<Any?>>,
    isCompletion: Boolean
  ) {
    checkNotNull(this) { "Cannot finish playing an exploration that hasn't yet been started" }
    tryOperation(finishExplorationResultFlow, recomputeState = false) {
      explorationProgress.advancePlayStageTo(NOT_PLAYING)
      explorationProgressListeners.forEach(ExplorationProgressListener::onExplorationEnded)
    }

    // The only way to be sure of an exploration completion is if the user clicks the 'Return to
    // Topic' button. All other cases (even if they reached the terminal state) will result in an
    // exit action. This also matches the progress tracking for the lesson: it's only considered
    // completed when 'Return to Topic' is clicked.
    finishExplorationAndLog(isCompletion)
  }

  private suspend fun ControllerState.submitAnswerImpl(
    submitAnswerResultFlow: MutableStateFlow<AsyncResult<AnswerOutcome>>,
    userAnswer: UserAnswer
  ) {
    tryOperation(submitAnswerResultFlow) {
      check(explorationProgress.playStage != NOT_PLAYING) {
        "Cannot submit an answer if an exploration is not being played."
      }
      check(explorationProgress.playStage != LOADING_EXPLORATION) {
        "Cannot submit an answer while the exploration is being loaded."
      }
      check(explorationProgress.playStage != SUBMITTING_ANSWER) {
        "Cannot submit an answer while another answer is pending."
      }

      // Notify observers that the submitted answer is currently pending.
      explorationProgress.advancePlayStageTo(SUBMITTING_ANSWER)
      recomputeCurrentStateAndNotifySync()

      var answerOutcome: AnswerOutcome? = null
      try {
        val topPendingState = explorationProgress.stateDeck.getPendingTopState()
        val outcome =
          answerClassificationController.classify(
            topPendingState.interaction,
            userAnswer.answer,
            userAnswer.writtenTranslationContext
          ).outcome
        answerOutcome =
          explorationProgress.stateGraph.computeAnswerOutcomeForResult(topPendingState, outcome)
        explorationProgress.stateDeck.submitAnswer(
          userAnswer, answerOutcome.feedback, answerOutcome.labelledAsCorrectAnswer
        )
        stateAnalyticsLogger?.logSubmitAnswer(
          topPendingState.interaction, userAnswer, answerOutcome.labelledAsCorrectAnswer
        )

        // Log correct & incorrect answer submission in a resumed exploration.
        if (isResume) {
          if (answerOutcome.labelledAsCorrectAnswer)
            explorationAnalyticsLogger.logResumeLessonSubmitCorrectAnswer()
          else
            explorationAnalyticsLogger.logResumeLessonSubmitIncorrectAnswer()
        }

        // Follow the answer's outcome to another part of the graph if it's different.
        val ephemeralState = computeBaseCurrentEphemeralState()
        when {
          answerOutcome.destinationCase == AnswerOutcome.DestinationCase.STATE_NAME -> {
            endState()
            val newState = explorationProgress.stateGraph.getState(answerOutcome.stateName)
            explorationProgress.stateDeck.pushState(
              newState,
              prohibitSameStateName = true,
              timestamp = startSessionTimeMs + continueButtonAnimationDelay,
              isContinueButtonAnimationSeen = isContinueButtonAnimationSeen
            )
            hintHandler.finishState(newState)
          }
          ephemeralState.stateTypeCase == EphemeralState.StateTypeCase.PENDING_STATE -> {
            // Schedule, or show immediately, a new hint or solution based on the current
            // ephemeral state of the exploration because a new wrong answer was submitted.
            hintHandler.handleWrongAnswerSubmission(ephemeralState.pendingState.wrongAnswerCount)
          }
        }
      } finally {
        if (answerOutcome != null &&
          !doesInteractionAutoContinue(answerOutcome.state.interaction.id)
        ) {
          // If the answer was not submitted on behalf of the Continue interaction, update the
          // hint state and save checkpoint because it will be saved when the learner moves to the
          // next state.
          saveExplorationCheckpoint()
        }

        // Ensure that the user always returns to the VIEWING_STATE stage to avoid getting stuck
        // in an 'always submitting answer' situation. This can specifically happen if answer
        // classification throws an exception.
        explorationProgress.advancePlayStageTo(VIEWING_STATE)
      }

      return@tryOperation checkNotNull(answerOutcome) { "Expected answer outcome." }
    }
  }

  private suspend fun ControllerState.submitHintIsRevealedImpl(
    submitHintRevealedResultFlow: MutableStateFlow<AsyncResult<Any?>>,
    hintIndex: Int
  ) {
    tryOperation(submitHintRevealedResultFlow) {
      check(explorationProgress.playStage != NOT_PLAYING) {
        "Cannot submit an answer if an exploration is not being played."
      }
      check(explorationProgress.playStage != LOADING_EXPLORATION) {
        "Cannot submit an answer while the exploration is being loaded."
      }
      check(explorationProgress.playStage != SUBMITTING_ANSWER) {
        "Cannot submit an answer while another answer is pending."
      }
      try {
        hintHandler.viewHint(hintIndex)
      } finally {
        // Ensure that the user always returns to the VIEWING_STATE stage to avoid getting stuck
        // in an 'always showing hint' situation. This can specifically happen if hint throws an
        // exception.
        explorationProgress.advancePlayStageTo(VIEWING_STATE)
      }
    }
  }

  private suspend fun ControllerState.submitSolutionIsRevealedImpl(
    submitSolutionRevealedResultFlow: MutableStateFlow<AsyncResult<Any?>>
  ) {
    tryOperation(submitSolutionRevealedResultFlow) {
      check(explorationProgress.playStage != NOT_PLAYING) {
        "Cannot submit an answer if an exploration is not being played."
      }
      check(explorationProgress.playStage != LOADING_EXPLORATION) {
        "Cannot submit an answer while the exploration is being loaded."
      }
      check(explorationProgress.playStage != SUBMITTING_ANSWER) {
        "Cannot submit an answer while another answer is pending."
      }
      try {
        hintHandler.viewSolution()
      } finally {
        // Ensure that the user always returns to the VIEWING_STATE stage to avoid getting stuck
        // in an 'always showing solution' situation. This can specifically happen if solution
        // throws an exception.
        explorationProgress.advancePlayStageTo(VIEWING_STATE)
      }
    }
  }

  private suspend fun ControllerState.moveToPreviousStateImpl(
    moveToPreviousStateResultFlow: MutableStateFlow<AsyncResult<Any?>>
  ) {
    tryOperation(moveToPreviousStateResultFlow) {
      check(explorationProgress.playStage != NOT_PLAYING) {
        "Cannot navigate to a previous state if an exploration is not being played."
      }
      check(explorationProgress.playStage != LOADING_EXPLORATION) {
        "Cannot navigate to a previous state if an exploration is being loaded."
      }
      check(explorationProgress.playStage != SUBMITTING_ANSWER) {
        "Cannot navigate to a previous state if an answer submission is pending."
      }
      hintHandler.navigateToPreviousState()
      explorationProgress.stateDeck.navigateToPreviousState()
    }
  }

  private suspend fun ControllerState.moveToNextStateImpl(
    moveToNextStateResultFlow: MutableStateFlow<AsyncResult<Any?>>
  ) {
    tryOperation(moveToNextStateResultFlow) {
      check(explorationProgress.playStage != NOT_PLAYING) {
        "Cannot navigate to a next state if an exploration is not being played."
      }
      check(explorationProgress.playStage != LOADING_EXPLORATION) {
        "Cannot navigate to a next state if an exploration is being loaded."
      }
      check(explorationProgress.playStage != SUBMITTING_ANSWER) {
        "Cannot navigate to a next state if an answer submission is pending."
      }
      explorationProgress.stateDeck.navigateToNextState()

      if (explorationProgress.stateDeck.isCurrentStateTopOfDeck()) {
        hintHandler.navigateBackToLatestPendingState()

        // Only mark checkpoint if current state is pending state. This ensures that checkpoints
        // will not be marked on any of the completed states.
        saveExplorationCheckpoint()

        // Ensure the state has been started the first time it's reached.
        maybeStartState(explorationProgress.stateDeck.getViewedStateCount())
      }

      if (!isContinueButtonAnimationSeen) {
        profileManagementController.markContinueButtonAnimationSeen(profileId)
      }
      isContinueButtonAnimationSeen = true
    }
  }

  private suspend fun ControllerState.logViewedHintImpl(
    sessionId: String,
    hintIndex: Int,
    submitLogHintViewedResultFlow: MutableStateFlow<AsyncResult<Any?>>
  ) {
    tryOperation(submitLogHintViewedResultFlow) {
      check(explorationProgress.playStage != NOT_PLAYING) {
        "Cannot log hint viewed if an exploration is not being played."
      }
      check(explorationProgress.playStage != LOADING_EXPLORATION) {
        "Cannot log hint viewed if an exploration is being loaded."
      }
      check(explorationProgress.playStage != SUBMITTING_ANSWER) {
        "Cannot log hint viewed if an answer submission is pending."
      }
      maybeLogViewedHint(sessionId, hintIndex)
    }
  }

  private suspend fun ControllerState.logViewedSolutionImpl(
    sessionId: String,
    submitLogSolutionViewedResultFlow: MutableStateFlow<AsyncResult<Any?>>
  ) {
    tryOperation(submitLogSolutionViewedResultFlow) {
      check(explorationProgress.playStage != NOT_PLAYING) {
        "Cannot log solution viewed if an exploration is not being played."
      }
      check(explorationProgress.playStage != LOADING_EXPLORATION) {
        "Cannot log solution viewed while the exploration is being loaded."
      }
      check(explorationProgress.playStage != SUBMITTING_ANSWER) {
        "Cannot log solution viewed if an answer submission is pending."
      }
      maybeLogViewedSolution(sessionId)
    }
  }

  private fun ControllerState.maybeLogUpdatedHelpIndex(
    helpIndex: HelpIndex,
    activeSessionId: String
  ) {
    // Only log if the current session is active.
    if (sessionId == activeSessionId) {
      checkForChangedHintState(helpIndex)
    }
  }

  private fun ControllerState.maybeLogViewedHint(
    activeSessionId: String,
    hintIndex: Int
  ) {
    // Only log if the current session is active.
    if (sessionId == activeSessionId) {
      stateAnalyticsLogger?.logViewHint(hintIndex)
    }
  }

  private fun ControllerState.maybeLogViewedSolution(
    activeSessionId: String
  ) {
    // Only log if the current session is active.
    if (sessionId == activeSessionId) {
      stateAnalyticsLogger?.logViewSolution()
    }
  }

  private suspend fun <T> ControllerState.tryOperation(
    resultFlow: MutableStateFlow<AsyncResult<T>>,
    recomputeState: Boolean = true,
    operation: suspend ControllerState.() -> T
  ) {
    try {
      resultFlow.emit(AsyncResult.Success(operation()))
      if (recomputeState) {
        recomputeCurrentStateAndNotifySync()
      }
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e)
      resultFlow.emit(AsyncResult.Failure(e))
    }
  }

  /**
   * Immediately recomputes the current state & notifies it's been changed.
   *
   * This should only be called when the caller can guarantee that the current [ControllerState] is
   * correct and up-to-date (i.e. that this is being called via a direct call path from the actor).
   *
   * All other cases must use [recomputeCurrentStateAndNotifyAsync].
   */
  private suspend fun ControllerState.recomputeCurrentStateAndNotifySync() {
    recomputeCurrentStateAndNotifyImpl()
  }

  /**
   * Sends a message to recompute the current state & notify it's been changed.
   *
   * This must be used in cases when the current [ControllerState] may no longer be up-to-date to
   * ensure state isn't leaked across play sessions.
   */
  private suspend fun ControllerState.recomputeCurrentStateAndNotifyAsync() {
    commandQueue.send(ControllerMessage.RecomputeStateAndNotify(sessionId))
  }

  private suspend fun ControllerState.recomputeCurrentStateAndNotifyImpl() {
    ephemeralStateFlow.emit(retrieveCurrentStateAsync())
  }

  private suspend fun ControllerState.retrieveCurrentStateAsync(): AsyncResult<EphemeralState> {
    return try {
      retrieveStateWithinCache()
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e)
      AsyncResult.Failure(e)
    }
  }

  private suspend fun ControllerState.retrieveStateWithinCache(): AsyncResult<EphemeralState> {
    return when (explorationProgress.playStage) {
      NOT_PLAYING -> AsyncResult.Pending()
      LOADING_EXPLORATION -> {
        try {
          val exploration =
            explorationRetriever.loadExploration(explorationProgress.currentExplorationId)
          finishLoadExploration(exploration, explorationProgress)
          AsyncResult.Success(computeCurrentEphemeralState())
        } catch (e: Exception) {
          exceptionsController.logNonFatalException(e)
          AsyncResult.Failure(e)
        }
      }
      VIEWING_STATE -> AsyncResult.Success(computeCurrentEphemeralState())
      SUBMITTING_ANSWER -> AsyncResult.Pending()
    }
  }

  private suspend fun ControllerState.finishLoadExploration(
    exploration: Exploration,
    progress: ExplorationProgress
  ) {
    // The exploration must be initialized first since other lazy fields depend on it being inited.
    progress.currentExploration = exploration
    progress.stateGraph.reset(exploration.statesMap)
    initializeEventLogger(exploration)

    if (progress.explorationCheckpoint != ExplorationCheckpoint.getDefaultInstance()) {
      // Restore the StateDeck and the HintHandler if the exploration is being resumed.
      progress.resumeStateDeckForSavedState()
      hintHandler.resumeHintsForSavedState(
        progress.explorationCheckpoint.pendingUserAnswersCount,
        progress.explorationCheckpoint.helpIndex,
        progress.stateDeck.getCurrentState()
      )
      explorationAnalyticsLogger.logResumeExploration()
      startState(logStartCard = false)
    } else {
      // If the exploration is not being resumed, reset the StateDeck and the HintHandler.
      progress.stateDeck.resetDeck(progress.stateGraph.getState(exploration.initStateName))

      if (isRestart) {
        explorationAnalyticsLogger.logStartExplorationOver()
      }

      val state = progress.stateDeck.getCurrentState()
      hintHandler.startWatchingForHintsInNewState(state)
      startState(logStartCard = true)

      if (!isRestart) {
        explorationAnalyticsLogger.logStartExploration()
      }
    }

    // Advance the stage, but do not notify observers since the current state can be reported
    // immediately to the UI.
    progress.advancePlayStageTo(VIEWING_STATE)

    // Mark a checkpoint in the exploration once the exploration has loaded.
    saveExplorationCheckpoint()
  }

  private fun ControllerState.computeBaseCurrentEphemeralState(): EphemeralState =
    explorationProgress.stateDeck.getCurrentEphemeralState(
      retrieveCurrentHelpIndex(),
      startSessionTimeMs + continueButtonAnimationDelay,
      isContinueButtonAnimationSeen
    )

  private fun ControllerState.computeCurrentEphemeralState(): EphemeralState {
    return computeBaseCurrentEphemeralState().toBuilder().apply {
      // Ensure that the state has an up-to-date checkpoint state.
      checkpointState = explorationProgress.checkpointState
    }.build()
  }

  private fun ControllerState.retrieveCurrentHelpIndex(): HelpIndex =
    hintHandler.getCurrentHelpIndex().value

  /**
   * Checks if checkpointing is enabled, if checkpointing is enabled this function creates a
   * checkpoint with the latest progress and saves it using [ExplorationCheckpointController].
   *
   * This function also waits for the save operation to complete, upon completion this function
   * uses the function [processSaveCheckpointResult] to mark the exploration as
   * IN_PROGRESS_SAVED or IN_PROGRESS_NOT_SAVED depending upon the result.
   *
   * Note that while this is changing internal ephemeral state, it does not notify of changes (it
   * instead expects callers to do this when it's best to notify frontend observers of the changes).
   */
  @OptIn(ExperimentalCoroutinesApi::class)
  private fun ControllerState.saveExplorationCheckpoint() {
    // Do not save checkpoints if shouldSavePartialProgress is false. This is expected to happen
    // when the current exploration has been already completed previously.
    if (!explorationProgress.shouldSavePartialProgress) return
    val profileId: ProfileId = explorationProgress.currentProfileId
    val topicId: String = explorationProgress.currentTopicId
    val storyId: String = explorationProgress.currentStoryId
    val explorationId: String = explorationProgress.currentExplorationId

    val checkpoint: ExplorationCheckpoint =
      explorationProgress.stateDeck.createExplorationCheckpoint(
        explorationProgress.currentExploration.version,
        explorationProgress.currentExploration.translatableTitle.html,
        oppiaClock.getCurrentTimeMs(),
        retrieveCurrentHelpIndex()
      )

    val deferred = explorationCheckpointController.recordExplorationCheckpointAsync(
      profileId,
      explorationId,
      checkpoint
    )

    deferred.invokeOnCompletion {
      val checkpointState = if (it == null) {
        explorationAnalyticsLogger.logProgressSavingSuccess()
        deferred.getCompleted()
      } else {
        oppiaLogger.e("Lightweight checkpointing", "Failed to save checkpoint in exploration", it)
        explorationAnalyticsLogger.logProgressSavingFailure()
        // CheckpointState is marked as CHECKPOINT_UNSAVED because the deferred did not
        // complete successfully.
        CheckpointState.CHECKPOINT_UNSAVED
      }

      // Schedule an event to process the checkpoint results in a synchronized environment to avoid
      // needing to lock on ControllerState.
      val processEvent =
        ControllerMessage.ProcessSavedCheckpointResult(
          profileId,
          topicId,
          storyId,
          explorationId,
          oppiaClock.getCurrentTimeMs(),
          checkpointState,
          sessionId
        )
      sendCommandForOperation(processEvent) {
        "Failed to schedule command for processing a saved checkpoint."
      }
    }
  }

  /**
   * Processes the result obtained upon complete execution of the function
   * [saveExplorationCheckpoint].
   *
   * Marks the exploration as in_progress_saved or in_progress_not_saved if it is not already marked
   * correctly. This function also updates the checkpoint state of the exploration to the
   * specified new checkpoint state.
   *
   * @param profileId is the profile id currently playing the exploration
   * @param topicId is the id of the topic which contains the story with the current exploration
   * @param storyId is the id of the story which contains the current exploration
   * @param lastPlayedTimestamp timestamp of the time when the checkpoints state for the exploration
   *     was last updated
   * @param newCheckpointState the latest state obtained after saving checkpoint successfully or
   *     unsuccessfully
   */
  private suspend fun ControllerState.processSaveCheckpointResult(
    profileId: ProfileId,
    topicId: String,
    storyId: String,
    explorationId: String,
    lastPlayedTimestamp: Long,
    newCheckpointState: CheckpointState
  ) {
    // Only processes the result of the last save operation if the checkpointState has changed.
    if (explorationProgress.checkpointState != newCheckpointState) {
      // Mark exploration as IN_PROGRESS_SAVED or IN_PROGRESS_NOT_SAVED if the checkpointState has
      // either changed from UNSAVED to SAVED or vice versa.
      if (
        explorationProgress.checkpointState != CheckpointState.CHECKPOINT_UNSAVED &&
        newCheckpointState == CheckpointState.CHECKPOINT_UNSAVED
      ) {
        markExplorationAsInProgressNotSaved(
          profileId,
          topicId,
          storyId,
          explorationId,
          lastPlayedTimestamp
        )
      } else if (
        explorationProgress.checkpointState == CheckpointState.CHECKPOINT_UNSAVED &&
        newCheckpointState != CheckpointState.CHECKPOINT_UNSAVED
      ) {
        markExplorationAsInProgressSaved(
          profileId,
          topicId,
          storyId,
          explorationId,
          lastPlayedTimestamp
        )
      }
      explorationProgress.updateCheckpointState(newCheckpointState)

      // The ephemeral state technically changes when a checkpoint is successfully saved.
      recomputeCurrentStateAndNotifySync()
    }
  }

  /**
   * Returns whether the specified interaction automatically continues the user to the next state
   * upon completion.
   */
  private fun doesInteractionAutoContinue(interactionId: String): Boolean =
    interactionId == "Continue"

  private fun markExplorationAsInProgressSaved(
    profileId: ProfileId,
    topicId: String,
    storyId: String,
    explorationId: String,
    lastPlayedTimestamp: Long
  ) {
    storyProgressController.recordChapterAsInProgressSaved(
      profileId,
      topicId,
      storyId,
      explorationId,
      lastPlayedTimestamp
    )
  }

  private fun markExplorationAsInProgressNotSaved(
    profileId: ProfileId,
    topicId: String,
    storyId: String,
    explorationId: String,
    lastPlayedTimestamp: Long
  ) {
    storyProgressController.recordChapterAsInProgressNotSaved(
      profileId,
      topicId,
      storyId,
      explorationId,
      lastPlayedTimestamp
    )
  }

  private fun <T> createAsyncResultStateFlow(initialValue: AsyncResult<T> = AsyncResult.Pending()) =
    MutableStateFlow(initialValue)

  private fun <T> StateFlow<AsyncResult<T>>.convertToSessionProvider(
    baseId: String
  ): DataProvider<T> = dataProviders.run {
    convertAsyncToAutomaticDataProvider("${baseId}_$activeSessionId")
  }

  /**
   * Represents the current synchronized state of the controller.
   *
   * This object's instance is tied directly to a single exploration session, and it's not
   * thread-safe so all access must be synchronized.
   *
   * @property explorationProgress the [ExplorationProgress] corresponding to the session
   * @property sessionId the GUID corresponding to the session
   * @property ephemeralStateFlow the [MutableStateFlow] that the updated [EphemeralState] is
   *     delivered to
   * @property commandQueue the actor command queue executing all messages that change this state
   */
  private class ControllerState(
    val explorationProgress: ExplorationProgress,
    val isRestart: Boolean,
    val isResume: Boolean,
    val sessionId: String,
    val ephemeralStateFlow: MutableStateFlow<AsyncResult<EphemeralState>>,
    val commandQueue: SendChannel<ControllerMessage<*>>,
    private val installationId: String?,
    private val profileId: ProfileId,
    private val learnerId: String?,
    private val learnerAnalyticsLogger: LearnerAnalyticsLogger,
    val startSessionTimeMs: Long,
    var isContinueButtonAnimationSeen: Boolean,
  ) {
    /**
     * The [HintHandler] used to monitor and trigger hints in the play session corresponding to this
     * controller state.
     */
    lateinit var hintHandler: HintHandler

    private var helpIndex = HelpIndex.getDefaultInstance()
    private var availableCardCount: Int = -1

    private var hasReachedInvestedEngagement = false
    private var completedStateCount = 0

    /**
     * The [LearnerAnalyticsLogger.ExplorationAnalyticsLogger] to be used for logging
     * exploration-specific events.
     */
    lateinit var explorationAnalyticsLogger: LearnerAnalyticsLogger.ExplorationAnalyticsLogger

    /**
     * The [LearnerAnalyticsLogger.StateAnalyticsLogger] to be used for logging state-specific
     * events.
     */
    val stateAnalyticsLogger: LearnerAnalyticsLogger.StateAnalyticsLogger?
      get() = explorationAnalyticsLogger.stateAnalyticsLogger.value

    /**
     * Initializes this state for event logging for the given [Exploration].
     *
     * This allows [startState] to be used.
     */
    fun initializeEventLogger(exploration: Exploration) {
      explorationAnalyticsLogger = learnerAnalyticsLogger.beginExploration(
        installationId,
        profileId,
        learnerId,
        exploration,
        explorationProgress.currentClassroomId,
        explorationProgress.currentTopicId,
        explorationProgress.currentStoryId
      )
      availableCardCount = explorationProgress.stateDeck.getViewedStateCount()
    }

    /**
     * Indicates that a new state has started and to prepare for state-based logging, and logs the
     * new card change.
     */
    fun startState(logStartCard: Boolean = true) {
      explorationAnalyticsLogger.startCard(explorationProgress.stateDeck.getCurrentState()).also {
        if (logStartCard) {
          it.logStartCard()
        }

        // Force the card count to update.
        availableCardCount = explorationProgress.stateDeck.getViewedStateCount()

        if (!hasReachedInvestedEngagement &&
          completedStateCount >= MINIMUM_COMPLETED_STATE_COUNT_FOR_INVESTED_ENGAGEMENT
        ) {
          it.logInvestedEngagement()
          hasReachedInvestedEngagement = true
        }
      }
    }

    /**
     * Indicates that a new state has started only if forward progress in the exploration has been
     * made (i.e. that [availableCardCount] is larger than what was previously known).
     */
    fun maybeStartState(availableCardCount: Int) {
      // Only start the state if it hasn't already been started.
      if (this.availableCardCount < availableCardCount) {
        startState()
        this.availableCardCount = availableCardCount
      }
    }

    /** Ends state-based logging for the current state and logs that the card has ended. */
    fun endState() {
      stateAnalyticsLogger?.logEndCard()
      explorationAnalyticsLogger.endCard()
      completedStateCount++
    }

    /** Checks and logs for hint-based changes based on the provided [HelpIndex]. */
    fun checkForChangedHintState(newHelpIndex: HelpIndex) {
      if (helpIndex != newHelpIndex) {
        // If the index changed to the new HelpIndex, that implies that whatever is observed in the
        // new HelpIndex indicates its previous state and therefore what changed.
        when (newHelpIndex.indexTypeCase) {
          NEXT_AVAILABLE_HINT_INDEX ->
            stateAnalyticsLogger?.logHintUnlocked(newHelpIndex.nextAvailableHintIndex)
          LATEST_REVEALED_HINT_INDEX ->
            stateAnalyticsLogger?.logRevealHint(newHelpIndex.latestRevealedHintIndex)
          SHOW_SOLUTION -> stateAnalyticsLogger?.logSolutionUnlocked()
          EVERYTHING_REVEALED -> when (helpIndex.indexTypeCase) {
            SHOW_SOLUTION -> stateAnalyticsLogger?.logRevealSolution()
            NEXT_AVAILABLE_HINT_INDEX -> // No solution, so revealing the hint ends available help.
              stateAnalyticsLogger?.logRevealHint(helpIndex.nextAvailableHintIndex)
            // Nothing to do in these cases.
            LATEST_REVEALED_HINT_INDEX, EVERYTHING_REVEALED, INDEXTYPE_NOT_SET, null -> {}
          }
          INDEXTYPE_NOT_SET, null -> {} // Nothing to do here.
        }
        helpIndex = newHelpIndex
      }
    }

    /**
     * Finishes the current exploration and logs its ending, also disabling any exploration-based
     * logging capabilities.
     */
    fun finishExplorationAndLog(isCompletion: Boolean) {
      if (isCompletion) {
        explorationAnalyticsLogger.logFinishExploration()
      } else {
        explorationAnalyticsLogger.logExitExploration()
      }
      learnerAnalyticsLogger.endExploration()
    }
  }

  /**
   * Represents a message that can be sent to [mostRecentCommandQueue] to process changes to
   * [ControllerState] (since all changes must be synchronized).
   *
   * Messages are expected to be resolved serially (though their scheduling can occur across
   * multiple threads, so order cannot be guaranteed until they're enqueued).
   */
  private sealed class ControllerMessage<T> {
    /**
     * The session ID corresponding to this message (the message is expected to be ignored if it
     * doesn't correspond to an active session).
     */
    abstract val sessionId: String

    /**
     * The [DataProvider]-tied [MutableStateFlow] that represents the result of the operation
     * corresponding to this message, or ``null`` if the caller doesn't care about observing the
     * result.
     */
    abstract val callbackFlow: MutableStateFlow<AsyncResult<T>>?

    /** [ControllerMessage] for initializing a new play session. */
    data class InitializeController(
      val profileId: ProfileId,
      val classroomId: String,
      val topicId: String,
      val storyId: String,
      val explorationId: String,
      val shouldSavePartialProgress: Boolean,
      val explorationCheckpoint: ExplorationCheckpoint,
      val isRestart: Boolean,
      val ephemeralStateFlow: MutableStateFlow<AsyncResult<EphemeralState>>,
      override val sessionId: String,
      override val callbackFlow: MutableStateFlow<AsyncResult<Any?>>
    ) : ControllerMessage<Any?>()

    /** [ControllerMessage] for ending the current play session. */
    data class FinishExploration(
      val isCompletion: Boolean,
      override val sessionId: String,
      override val callbackFlow: MutableStateFlow<AsyncResult<Any?>>
    ) : ControllerMessage<Any?>()

    /** [ControllerMessage] for submitting a new [UserAnswer]. */
    data class SubmitAnswer(
      val userAnswer: UserAnswer,
      override val sessionId: String,
      override val callbackFlow: MutableStateFlow<AsyncResult<AnswerOutcome>>
    ) : ControllerMessage<AnswerOutcome>()

    /**
     * [ControllerMessage] for indicating that the user revealed the hint corresponding to
     * [hintIndex].
     */
    data class HintIsRevealed(
      val hintIndex: Int,
      override val sessionId: String,
      override val callbackFlow: MutableStateFlow<AsyncResult<Any?>>
    ) : ControllerMessage<Any?>()

    /**
     * [ControllerMessage] for indicating that the user revealed the solution for the current state.
     */
    data class SolutionIsRevealed(
      override val sessionId: String,
      override val callbackFlow: MutableStateFlow<AsyncResult<Any?>>
    ) : ControllerMessage<Any?>()

    /** [ControllerMessage] to move to the previous state in the exploration. */
    data class MoveToPreviousState(
      override val sessionId: String,
      override val callbackFlow: MutableStateFlow<AsyncResult<Any?>>
    ) : ControllerMessage<Any?>()

    /** [ControllerMessage] to move to the next state in the exploration. */
    data class MoveToNextState(
      override val sessionId: String,
      override val callbackFlow: MutableStateFlow<AsyncResult<Any?>>
    ) : ControllerMessage<Any?>()

    /**
     * [ControllerMessage] to indicate that the session's current partial completion progress should
     * be saved to disk.
     *
     * Note that this does not actually guarantee an update to the tracked progress of the
     * exploration (see [ProcessSavedCheckpointResult]).
     */
    data class SaveCheckpoint(
      override val sessionId: String,
      override val callbackFlow: MutableStateFlow<AsyncResult<Any?>>? = null
    ) : ControllerMessage<Any?>()

    /**
     * [ControllerMessage] to log cases when [HelpIndex] has updated for the current session.
     *
     * Specific measures are taken to ensure that the handler for this message does not log the
     * change if the current active session has changed (since that's generally indicative of an
     * error--hints can't continue to change after the session has ended).
     */
    data class LogUpdatedHelpIndex(
      val helpIndex: HelpIndex,
      override val sessionId: String,
      override val callbackFlow: MutableStateFlow<AsyncResult<Any?>>? = null
    ) : ControllerMessage<Any?>()

    /**
     * [ControllerMessage] to log cases when the user has viewed a hint for the current session.
     *
     * Specific measures are taken to ensure that the handler for this message does not log the
     * change if the current active session has changed (since that's generally indicative of an
     * error--hints can't continue to change after the session has ended).
     */
    data class LogHintIsViewed(
      val hintIndex: Int,
      override val sessionId: String,
      override val callbackFlow: MutableStateFlow<AsyncResult<Any?>>
    ) : ControllerMessage<Any?>()

    /**
     * [ControllerMessage] to log cases when the user has viewed the solution for the current
     * session.
     *
     * Specific measures are taken to ensure that the handler for this message does not log the
     * change if the current active session has changed.
     */
    data class LogSolutionIsViewed(
      override val sessionId: String,
      override val callbackFlow: MutableStateFlow<AsyncResult<Any?>>
    ) : ControllerMessage<Any?>()

    /**
     * [ControllerMessage] to ensure a successfully saved checkpoint is reflected in other parts of
     * the app (e.g. that an exploration is considered 'in-progress' in such circumstances).
     */
    data class ProcessSavedCheckpointResult(
      val profileId: ProfileId,
      val topicId: String,
      val storyId: String,
      val explorationId: String,
      val lastPlayedTimestamp: Long,
      val newCheckpointState: CheckpointState,
      override val sessionId: String,
      override val callbackFlow: MutableStateFlow<AsyncResult<Any?>>? = null
    ) : ControllerMessage<Any?>()

    /**
     * [ControllerMessage] which recomputes the current [EphemeralState] and notifies subscribers of
     * the [DataProvider] returned by [getCurrentState] of the change.
     *
     * This is only used in cases where an external operation trigger changes that are only
     * reflected when recomputing the state (e.g. a new hint needing to be shown).
     */
    data class RecomputeStateAndNotify(
      override val sessionId: String,
      override val callbackFlow: MutableStateFlow<AsyncResult<Any?>>? = null
    ) : ControllerMessage<Any?>()
  }

  private companion object {
    private const val MINIMUM_COMPLETED_STATE_COUNT_FOR_INVESTED_ENGAGEMENT = 3

    /**
     * Returns a collectable [Flow] that notifies [collector] for this [StateFlow]s initial state,
     * and every change after.
     *
     * It should guarantee that [collector] receives all values ever present in this flow.
     */
    private fun <T> StateFlow<T>.onFirstAndEach(
      collector: suspend (T) -> Unit
    ): Flow<T> = onStart { collector(value) }.onEach(collector)
  }
}
