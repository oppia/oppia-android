package org.oppia.android.domain.exploration

import org.oppia.android.app.model.AnswerOutcome
import org.oppia.android.app.model.CheckpointState
import org.oppia.android.app.model.EphemeralState
import org.oppia.android.app.model.Exploration
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.domain.classify.AnswerClassificationController
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationCheckpointController
import org.oppia.android.domain.hintsandsolution.HintHandler
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.android.domain.topic.StoryProgressController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncDataSubscriptionManager
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.transformAsync
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.system.OppiaClock
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.withLock

private const val BEGIN_EXPLORATION_RESULT_PROVIDER_ID =
  "ExplorationProgressController.begin_exploration_result"
private const val FINISH_EXPLORATION_RESULT_PROVIDER_ID =
  "ExplorationProgressController.finish_exploration_result"
private const val SUBMIT_ANSWER_RESULT_PROVIDER_ID =
  "ExplorationProgressController.submit_answer_result"
private const val SUBMIT_HINT_REVEALED_RESULT_PROVIDER_ID =
  "ExplorationProgressController.submit_hint_revealed_result"
private const val SUBMIT_SOLUTION_REVEALED_RESULT_PROVIDER_ID =
  "ExplorationProgressController.submit_solution_revealed_result"
private const val MOVE_TO_PREVIOUS_STATE_RESULT_PROVIDER_ID =
  "ExplorationProgressController.move_to_previous_state_result"
private const val MOVE_TO_NEXT_STATE_RESULT_PROVIDER_ID =
  "ExplorationProgressController.move_to_next_state_result"
private const val CURRENT_STATE_PROVIDER_ID = "ExplorationProgressController.current_state"

/**
 * Controller that tracks and reports the learner's ephemeral/non-persisted progress through an
 * exploration. Note that this controller only supports one active exploration at a time.
 *
 * The current exploration session is started via the exploration data controller.
 *
 * This class is thread-safe, but the order of applied operations is arbitrary. Calling code should
 * take care to ensure that uses of this class do not specifically depend on ordering.
 */
@Singleton
class ExplorationProgressController @Inject constructor(
  private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager,
  private val explorationRetriever: ExplorationRetriever,
  private val answerClassificationController: AnswerClassificationController,
  private val exceptionsController: ExceptionsController,
  private val explorationCheckpointController: ExplorationCheckpointController,
  private val storyProgressController: StoryProgressController,
  private val oppiaClock: OppiaClock,
  private val oppiaLogger: OppiaLogger,
  private val hintHandlerFactory: HintHandler.Factory,
  private val translationController: TranslationController,
  private val dataProviders: DataProviders
) : HintHandler.HintMonitor {
  // TODO(#179): Add support for parameters.
  // TODO(#3622): Update the internal locking of this controller to use something like an in-memory
  //  blocking cache to simplify state locking. However, doing this correctly requires a fix in
  //  MediatorLiveData to avoid unexpected cancellations in chained cross-scope coroutines. Note
  //  that this is also essential to ensure post-load operations can be queued before load completes
  //  to avoid cases in tests where the exploration load operation needs to be fully finished before
  //  performing a post-load operation. The current state of the controller is leaking this
  //  implementation detail to tests.
  // TODO(#3467): Update the mechanism to save checkpoints to eliminate the race condition that may
  //  arise if the function finishExplorationAsync acquires lock before the invokeOnCompletion
  //  callback on the deferred returned on saving checkpoints. In this case ExplorationActivity will
  //  make decisions based on a value of the checkpointState which might not be up-to date.

  private val explorationProgress = ExplorationProgress()
  private val explorationProgressLock = ReentrantLock()
  private lateinit var hintHandler: HintHandler

  /**
   * Resets this controller to begin playing the specified [Exploration], and returns a
   * [DataProvider] indicating whether the start was successful.
   */
  internal fun beginExplorationAsync(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    shouldSavePartialProgress: Boolean,
    explorationCheckpoint: ExplorationCheckpoint
  ): DataProvider<Any?> {
    return explorationProgressLock.withLock {
      try {
        check(explorationProgress.playStage == ExplorationProgress.PlayStage.NOT_PLAYING) {
          "Expected to finish previous exploration before starting a new one."
        }

        explorationProgress.apply {
          currentProfileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
          currentTopicId = topicId
          currentStoryId = storyId
          currentExplorationId = explorationId
          this.shouldSavePartialProgress = shouldSavePartialProgress
          checkpointState = CheckpointState.CHECKPOINT_UNSAVED
          this.explorationCheckpoint = explorationCheckpoint
        }
        hintHandler = hintHandlerFactory.create(this)
        explorationProgress.advancePlayStageTo(ExplorationProgress.PlayStage.LOADING_EXPLORATION)
        asyncDataSubscriptionManager.notifyChangeAsync(CURRENT_STATE_PROVIDER_ID)
        return@withLock dataProviders.createInMemoryDataProvider(
          BEGIN_EXPLORATION_RESULT_PROVIDER_ID
        ) { null }
      } catch (e: Exception) {
        exceptionsController.logNonFatalException(e)
        return@withLock dataProviders.createInMemoryDataProviderAsync(
          BEGIN_EXPLORATION_RESULT_PROVIDER_ID
        ) { AsyncResult.Failure(e) }
      }
    }
  }

  /**
   * Indicates that the current exploration being played is now completed, and returns a
   * [DataProvider] indicating whether the cleanup was successful.
   */
  internal fun finishExplorationAsync(): DataProvider<Any?> {
    return explorationProgressLock.withLock {
      try {
        check(explorationProgress.playStage != ExplorationProgress.PlayStage.NOT_PLAYING) {
          "Cannot finish playing an exploration that hasn't yet been started"
        }
        explorationProgress.advancePlayStageTo(ExplorationProgress.PlayStage.NOT_PLAYING)
        return@withLock dataProviders.createInMemoryDataProvider(
          FINISH_EXPLORATION_RESULT_PROVIDER_ID
        ) { null }
      } catch (e: Exception) {
        exceptionsController.logNonFatalException(e)
        return@withLock dataProviders.createInMemoryDataProviderAsync(
          FINISH_EXPLORATION_RESULT_PROVIDER_ID
        ) { AsyncResult.Failure(e) }
      }
    }
  }

  override fun onHelpIndexChanged() {
    explorationProgressLock.withLock {
      saveExplorationCheckpoint()
    }
    asyncDataSubscriptionManager.notifyChangeAsync(CURRENT_STATE_PROVIDER_ID)
  }

  /**
   * Submits an answer to the current state and returns how the UI should respond to this answer.
   *
   * The returned [DataProvider] will only have at most two results posted: a pending result, and
   * then a completed success/failure result. Failures in this case represent a failure of the app
   * (possibly due to networking conditions). The app should report this error in a consumable way
   * to the user so that they may take action on it. No additional values will be reported to the
   * [DataProvider]. Each call to this method returns a new, distinct, [DataProvider] object that
   * must be observed. Note also that the returned [DataProvider] is not guaranteed to begin with a
   * pending state.
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
   * This method cannot be called until an exploration has started and [getCurrentState] returns a
   * non-pending result or the result will fail. Calling code must also take care not to allow users
   * to submit an answer while a previous answer is pending. That scenario will also result in a
   * failed answer submission.
   *
   * No assumptions should be made about the completion order of the returned [DataProvider] vs. the
   * [DataProvider] from [getCurrentState]. Also note that the returned [DataProvider] will only
   * have a single value and not be reused after that point.
   */
  fun submitAnswer(userAnswer: UserAnswer): DataProvider<AnswerOutcome> {
    try {
      explorationProgressLock.withLock {
        check(
          explorationProgress.playStage !=
            ExplorationProgress.PlayStage.NOT_PLAYING
        ) {
          "Cannot submit an answer if an exploration is not being played."
        }
        check(
          explorationProgress.playStage !=
            ExplorationProgress.PlayStage.LOADING_EXPLORATION
        ) {
          "Cannot submit an answer while the exploration is being loaded."
        }
        check(
          explorationProgress.playStage !=
            ExplorationProgress.PlayStage.SUBMITTING_ANSWER
        ) {
          "Cannot submit an answer while another answer is pending."
        }

        // Notify observers that the submitted answer is currently pending.
        explorationProgress.advancePlayStageTo(ExplorationProgress.PlayStage.SUBMITTING_ANSWER)
        asyncDataSubscriptionManager.notifyChangeAsync(CURRENT_STATE_PROVIDER_ID)

        lateinit var answerOutcome: AnswerOutcome
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
          explorationProgress.stateDeck.submitAnswer(userAnswer, answerOutcome.feedback)

          // Follow the answer's outcome to another part of the graph if it's different.
          val ephemeralState = computeBaseCurrentEphemeralState()
          when {
            answerOutcome.destinationCase == AnswerOutcome.DestinationCase.STATE_NAME -> {
              val newState = explorationProgress.stateGraph.getState(answerOutcome.stateName)
              explorationProgress.stateDeck.pushState(newState, prohibitSameStateName = true)
              hintHandler.finishState(newState)
            }
            ephemeralState.stateTypeCase == EphemeralState.StateTypeCase.PENDING_STATE -> {
              // Schedule, or show immediately, a new hint or solution based on the current
              // ephemeral state of the exploration because a new wrong answer was submitted.
              hintHandler.handleWrongAnswerSubmission(ephemeralState.pendingState.wrongAnswerCount)
            }
          }
        } finally {
          if (!doesInteractionAutoContinue(answerOutcome.state.interaction.id)) {
            // If the answer was not submitted on behalf of the Continue interaction, update the
            // hint state and save checkpoint because it will be saved when the learner moves to the
            // next state.
            saveExplorationCheckpoint()
          }

          // Ensure that the user always returns to the VIEWING_STATE stage to avoid getting stuck
          // in an 'always submitting answer' situation. This can specifically happen if answer
          // classification throws an exception.
          explorationProgress.advancePlayStageTo(ExplorationProgress.PlayStage.VIEWING_STATE)
        }

        asyncDataSubscriptionManager.notifyChangeAsync(CURRENT_STATE_PROVIDER_ID)

        return dataProviders.createInMemoryDataProvider(SUBMIT_ANSWER_RESULT_PROVIDER_ID) {
          answerOutcome
        }
      }
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e)
      return dataProviders.createInMemoryDataProviderAsync(SUBMIT_ANSWER_RESULT_PROVIDER_ID) {
        AsyncResult.Failure(e)
      }
    }
  }

  /**
   * Notifies the controller that the user wishes to reveal a hint.
   *
   * @param hintIndex index of the hint that was revealed in the hint list of the current pending
   *     state
   * @return a one-time [DataProvider] that indicates success/failure of the operation (the actual
   *     payload of the result isn't relevant)
   */
  fun submitHintIsRevealed(hintIndex: Int): DataProvider<Any?> {
    try {
      explorationProgressLock.withLock {
        check(
          explorationProgress.playStage !=
            ExplorationProgress.PlayStage.NOT_PLAYING
        ) {
          "Cannot submit an answer if an exploration is not being played."
        }
        check(
          explorationProgress.playStage !=
            ExplorationProgress.PlayStage.LOADING_EXPLORATION
        ) {
          "Cannot submit an answer while the exploration is being loaded."
        }
        check(
          explorationProgress.playStage !=
            ExplorationProgress.PlayStage.SUBMITTING_ANSWER
        ) {
          "Cannot submit an answer while another answer is pending."
        }
        try {
          hintHandler.viewHint(hintIndex)
        } finally {
          // Ensure that the user always returns to the VIEWING_STATE stage to avoid getting stuck
          // in an 'always showing hint' situation. This can specifically happen if hint throws an
          // exception.
          explorationProgress.advancePlayStageTo(ExplorationProgress.PlayStage.VIEWING_STATE)
        }
        asyncDataSubscriptionManager.notifyChangeAsync(CURRENT_STATE_PROVIDER_ID)
        return dataProviders.createInMemoryDataProvider(SUBMIT_HINT_REVEALED_RESULT_PROVIDER_ID) {
          null
        }
      }
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e)
      return dataProviders.createInMemoryDataProviderAsync(
        SUBMIT_HINT_REVEALED_RESULT_PROVIDER_ID
      ) { AsyncResult.Failure(e) }
    }
  }

  /**
   * Notifies the controller that the user has revealed the solution to the current state.
   *
   * @return a one-time [DataProvider] that indicates success/failure of the operation (the actual
   *     payload of the result isn't relevant)
   */
  fun submitSolutionIsRevealed(): DataProvider<Any?> {
    try {
      explorationProgressLock.withLock {
        check(
          explorationProgress.playStage !=
            ExplorationProgress.PlayStage.NOT_PLAYING
        ) {
          "Cannot submit an answer if an exploration is not being played."
        }
        check(
          explorationProgress.playStage !=
            ExplorationProgress.PlayStage.LOADING_EXPLORATION
        ) {
          "Cannot submit an answer while the exploration is being loaded."
        }
        check(
          explorationProgress.playStage !=
            ExplorationProgress.PlayStage.SUBMITTING_ANSWER
        ) {
          "Cannot submit an answer while another answer is pending."
        }
        try {
          hintHandler.viewSolution()
        } finally {
          // Ensure that the user always returns to the VIEWING_STATE stage to avoid getting stuck
          // in an 'always showing solution' situation. This can specifically happen if solution
          // throws an exception.
          explorationProgress.advancePlayStageTo(ExplorationProgress.PlayStage.VIEWING_STATE)
        }

        asyncDataSubscriptionManager.notifyChangeAsync(CURRENT_STATE_PROVIDER_ID)
        return dataProviders.createInMemoryDataProvider(
          SUBMIT_SOLUTION_REVEALED_RESULT_PROVIDER_ID
        ) { null }
      }
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e)
      return dataProviders.createInMemoryDataProviderAsync(
        SUBMIT_SOLUTION_REVEALED_RESULT_PROVIDER_ID
      ) { AsyncResult.Failure(e) }
    }
  }

  /**
   * Navigates to the previous state in the graph. If the learner is currently on the initial state,
   * this method will throw an exception. Calling code is responsible for ensuring this method is
   * only called when it's possible to navigate backward.
   *
   * @return a one-time [DataProvider] indicating whether the movement to the previous state was
   *     successful, or a failure if state navigation was attempted at an invalid time in the state
   *     graph (e.g. if currently viewing the initial state of the exploration). It's recommended
   *     that calling code only listen to this result for failures, and instead rely on
   *     [getCurrentState] for observing a successful transition to another state.
   */
  fun moveToPreviousState(): DataProvider<Any?> {
    try {
      explorationProgressLock.withLock {
        check(
          explorationProgress.playStage !=
            ExplorationProgress.PlayStage.NOT_PLAYING
        ) {
          "Cannot navigate to a previous state if an exploration is not being played."
        }
        check(
          explorationProgress.playStage !=
            ExplorationProgress.PlayStage.LOADING_EXPLORATION
        ) {
          "Cannot navigate to a previous state if an exploration is being loaded."
        }
        check(
          explorationProgress.playStage !=
            ExplorationProgress.PlayStage.SUBMITTING_ANSWER
        ) {
          "Cannot navigate to a previous state if an answer submission is pending."
        }
        hintHandler.navigateToPreviousState()
        explorationProgress.stateDeck.navigateToPreviousState()
        asyncDataSubscriptionManager.notifyChangeAsync(CURRENT_STATE_PROVIDER_ID)
      }
      return dataProviders.createInMemoryDataProvider(MOVE_TO_PREVIOUS_STATE_RESULT_PROVIDER_ID) {
        null
      }
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e)
      return dataProviders.createInMemoryDataProviderAsync(
        MOVE_TO_PREVIOUS_STATE_RESULT_PROVIDER_ID
      ) { AsyncResult.Failure(e) }
    }
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
   * @return a one-time [DataProvider] indicating whether the movement to the next state was
   *     successful, or a failure if state navigation was attempted at an invalid time in the state
   *     graph (e.g. if the current state is pending or terminal). It's recommended that calling
   *     code only listen to this result for failures, and instead rely on [getCurrentState] for
   *     observing a successful transition to another state.
   */
  fun moveToNextState(): DataProvider<Any?> {
    try {
      explorationProgressLock.withLock {
        check(
          explorationProgress.playStage !=
            ExplorationProgress.PlayStage.NOT_PLAYING
        ) {
          "Cannot navigate to a next state if an exploration is not being played."
        }
        check(
          explorationProgress.playStage !=
            ExplorationProgress.PlayStage.LOADING_EXPLORATION
        ) {
          "Cannot navigate to a next state if an exploration is being loaded."
        }
        check(
          explorationProgress.playStage !=
            ExplorationProgress.PlayStage.SUBMITTING_ANSWER
        ) {
          "Cannot navigate to a next state if an answer submission is pending."
        }
        explorationProgress.stateDeck.navigateToNextState()

        if (explorationProgress.stateDeck.isCurrentStateTopOfDeck()) {
          hintHandler.navigateBackToLatestPendingState()

          // Only mark checkpoint if current state is pending state. This ensures that checkpoints
          // will not be marked on any of the completed states.
          saveExplorationCheckpoint()
        }
        asyncDataSubscriptionManager.notifyChangeAsync(CURRENT_STATE_PROVIDER_ID)
      }
      return dataProviders.createInMemoryDataProvider(MOVE_TO_NEXT_STATE_RESULT_PROVIDER_ID) {
        null
      }
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e)
      return dataProviders.createInMemoryDataProviderAsync(MOVE_TO_NEXT_STATE_RESULT_PROVIDER_ID) {
        AsyncResult.Failure(e)
      }
    }
  }

  /**
   * Returns a [DataProvider] monitoring the current [EphemeralState] the learner is currently
   * viewing. If this state corresponds to a a terminal state, then the learner has completed the
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
   * exploration is loaded. UI code can be confident only calls from the UI layer will trigger state
   * changes here to ensure atomicity between receiving and making state changes.
   *
   * This method is safe to be called before an exploration has started. If there is no ongoing
   * exploration, it should return a pending state.
   */
  fun getCurrentState(): DataProvider<EphemeralState> {
    return translationController.getWrittenTranslationContentLocale(
      explorationProgress.currentProfileId
    ).transformAsync(CURRENT_STATE_PROVIDER_ID) { contentLocale ->
      return@transformAsync retrieveCurrentStateAsync(contentLocale)
    }
  }

  private suspend fun retrieveCurrentStateAsync(
    writtenTranslationContentLocale: OppiaLocale.ContentLocale
  ): AsyncResult<EphemeralState> {
    return try {
      retrieveCurrentStateWithinCacheAsync(writtenTranslationContentLocale)
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e)
      AsyncResult.Failure(e)
    }
  }

  @Suppress("RedundantSuspendModifier") // Function is 'suspend' to restrict calling some methods.
  private suspend fun retrieveCurrentStateWithinCacheAsync(
    writtenTranslationContentLocale: OppiaLocale.ContentLocale
  ): AsyncResult<EphemeralState> {
    val explorationId: String? = explorationProgressLock.withLock {
      if (explorationProgress.playStage == ExplorationProgress.PlayStage.LOADING_EXPLORATION) {
        explorationProgress.currentExplorationId
      } else null
    }

    val exploration = explorationId?.let(explorationRetriever::loadExploration)

    explorationProgressLock.withLock {
      // It's possible for the exploration ID or stage to change between critical sections. However,
      // this is the only way to ensure the exploration is loaded since suspended functions cannot
      // be called within a mutex. Note that it's also possible for the stage to change between
      // critical sections, sometimes due to this suspend function being called multiple times and a
      // former call finishing the exploration load.
      check(
        exploration == null ||
          explorationProgress.currentExplorationId == explorationId
      ) {
        "Encountered race condition when retrieving exploration. ID changed from $explorationId" +
          " to ${explorationProgress.currentExplorationId}"
      }
      return when (explorationProgress.playStage) {
        ExplorationProgress.PlayStage.NOT_PLAYING -> AsyncResult.Pending()
        ExplorationProgress.PlayStage.LOADING_EXPLORATION -> {
          try {
            // The exploration must be available for this stage since it was loaded above.
            finishLoadExploration(exploration!!, explorationProgress)
            AsyncResult.Success(computeCurrentEphemeralState(writtenTranslationContentLocale))
          } catch (e: Exception) {
            exceptionsController.logNonFatalException(e)
            AsyncResult.Failure(e)
          }
        }
        ExplorationProgress.PlayStage.VIEWING_STATE ->
          AsyncResult.Success(computeCurrentEphemeralState(writtenTranslationContentLocale))
        ExplorationProgress.PlayStage.SUBMITTING_ANSWER -> AsyncResult.Pending()
      }
    }
  }

  private fun finishLoadExploration(exploration: Exploration, progress: ExplorationProgress) {
    // The exploration must be initialized first since other lazy fields depend on it being inited.
    progress.currentExploration = exploration
    progress.stateGraph.reset(exploration.statesMap)

    if (progress.explorationCheckpoint != ExplorationCheckpoint.getDefaultInstance()) {
      // Restore the StateDeck and the HintHandler if the exploration is being resumed.
      progress.resumeStateDeckForSavedState(exploration)
      hintHandler.resumeHintsForSavedState(
        progress.explorationCheckpoint.pendingUserAnswersCount,
        progress.explorationCheckpoint.helpIndex,
        progress.stateDeck.getCurrentState()
      )
    } else {
      // If the exploration is not being resumed, reset the StateDeck and the HintHandler.
      progress.stateDeck.resetDeck(progress.stateGraph.getState(exploration.initStateName))
      hintHandler.startWatchingForHintsInNewState(progress.stateDeck.getCurrentState())
    }

    // Advance the stage, but do not notify observers since the current state can be reported
    // immediately to the UI.
    progress.advancePlayStageTo(ExplorationProgress.PlayStage.VIEWING_STATE)

    // Mark a checkpoint in the exploration once the exploration has loaded.
    saveExplorationCheckpoint()
  }

  private fun computeBaseCurrentEphemeralState(): EphemeralState =
    explorationProgress.stateDeck.getCurrentEphemeralState(computeCurrentHelpIndex())

  private fun computeCurrentEphemeralState(
    writtenTranslationContentLocale: OppiaLocale.ContentLocale
  ): EphemeralState {
    return computeBaseCurrentEphemeralState().toBuilder().apply {
      // Ensure that the state has an up-to-date checkpoint state & translation context (which may
      // not necessarily be up-to-date in the state deck).
      checkpointState = explorationProgress.checkpointState
      writtenTranslationContext =
        translationController.computeWrittenTranslationContext(
          state.writtenTranslationsMap, writtenTranslationContentLocale
        )
    }.build()
  }

  private fun computeCurrentHelpIndex(): HelpIndex = hintHandler.getCurrentHelpIndex()

  /**
   * Checks if checkpointing is enabled, if checkpointing is enabled this function creates a
   * checkpoint with the latest progress and saves it using [ExplorationCheckpointController].
   *
   * This function also waits for the save operation to complete, upon completion this function
   * uses the function [processSaveCheckpointResult] to mark the exploration as
   * IN_PROGRESS_SAVED or IN_PROGRESS_NOT_SAVED depending upon the result.
   */
  private fun saveExplorationCheckpoint() {
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
        explorationProgress.currentExploration.title,
        oppiaClock.getCurrentTimeMs(),
        computeCurrentHelpIndex()
      )

    val deferred = explorationCheckpointController.recordExplorationCheckpointAsync(
      profileId,
      explorationId,
      checkpoint
    )

    deferred.invokeOnCompletion {
      val checkpointState = if (it == null) {
        deferred.getCompleted()
      } else {
        oppiaLogger.e("Lightweight checkpointing", "Failed to save checkpoint in exploration", it)
        // CheckpointState is marked as CHECKPOINT_UNSAVED because the deferred did not
        // complete successfully.
        CheckpointState.CHECKPOINT_UNSAVED
      }
      processSaveCheckpointResult(
        profileId,
        topicId,
        storyId,
        explorationId,
        oppiaClock.getCurrentTimeMs(),
        checkpointState
      )
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
  private fun processSaveCheckpointResult(
    profileId: ProfileId,
    topicId: String,
    storyId: String,
    explorationId: String,
    lastPlayedTimestamp: Long,
    newCheckpointState: CheckpointState
  ) {
    explorationProgressLock.withLock {
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
        // Notify observers that the checkpoint state has changed.
        asyncDataSubscriptionManager.notifyChangeAsync(CURRENT_STATE_PROVIDER_ID)
      }
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
}
