package org.oppia.android.domain.exploration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.oppia.android.app.model.AnswerOutcome
import org.oppia.android.app.model.CheckpointState
import org.oppia.android.app.model.EphemeralState
import org.oppia.android.app.model.Exploration
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.Hint
import org.oppia.android.app.model.HintState
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Solution
import org.oppia.android.app.model.State
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.domain.classify.AnswerClassificationController
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationCheckpointController
import org.oppia.android.domain.hintsandsolution.HintHandler
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.android.domain.topic.StoryProgressController
import org.oppia.android.util.data.AsyncDataSubscriptionManager
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.system.OppiaClock
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.withLock

private const val CURRENT_STATE_DATA_PROVIDER_ID = "current_state_data_provider_id"

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
  dataProviders: DataProviders,
  private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager,
  private val explorationRetriever: ExplorationRetriever,
  private val answerClassificationController: AnswerClassificationController,
  private val exceptionsController: ExceptionsController,
  private val explorationCheckpointController: ExplorationCheckpointController,
  private val storyProgressController: StoryProgressController,
  private val oppiaClock: OppiaClock,
  private val oppiaLogger: OppiaLogger,
  private val hintHandler: HintHandler
) {
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

  private val currentStateDataProvider =
    dataProviders.createInMemoryDataProviderAsync(
      CURRENT_STATE_DATA_PROVIDER_ID,
      this::retrieveCurrentStateAsync
    )
  private val explorationProgress = ExplorationProgress()
  private val explorationProgressLock = ReentrantLock()

  /** Resets this controller to begin playing the specified [Exploration]. */
  internal fun beginExplorationAsync(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    shouldSavePartialProgress: Boolean,
    explorationCheckpoint: ExplorationCheckpoint
  ) {
    explorationProgressLock.withLock {
      check(explorationProgress.playStage == ExplorationProgress.PlayStage.NOT_PLAYING) {
        "Expected to finish previous exploration before starting a new one."
      }

      explorationProgress.apply {
        currentProfileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
        currentTopicId = topicId
        currentStoryId = storyId
        currentExplorationId = explorationId
        this.shouldSavePartialProgress = shouldSavePartialProgress
        hintState = HintState.getDefaultInstance()
        currentCheckpointState = CheckpointState.CHECKPOINT_UNSAVED
        currentExplorationCheckpoint = explorationCheckpoint
      }
      explorationProgress.advancePlayStageTo(ExplorationProgress.PlayStage.LOADING_EXPLORATION)
      asyncDataSubscriptionManager.notifyChangeAsync(CURRENT_STATE_DATA_PROVIDER_ID)
    }
  }

  /** Indicates that the current exploration being played is now completed. */
  internal fun finishExplorationAsync() {
    explorationProgressLock.withLock {
      check(explorationProgress.playStage != ExplorationProgress.PlayStage.NOT_PLAYING) {
        "Cannot finish playing an exploration that hasn't yet been started"
      }
      explorationProgress.advancePlayStageTo(ExplorationProgress.PlayStage.NOT_PLAYING)
    }
  }

  /**
   * Submits an answer to the current state and returns how the UI should respond to this answer.
   * The returned [LiveData] will only have at most two results posted: a pending result, and then a
   * completed success/failure result. Failures in this case represent a failure of the app
   * (possibly due to networking conditions). The app should report this error in a consumable way
   * to the user so that they may take action on it. No additional values will be reported to the
   * [LiveData]. Each call to this method returns a new, distinct, [LiveData] object that must be
   * observed. Note also that the returned [LiveData] is not guaranteed to begin with a pending
   * state.
   *
   * If the app undergoes a configuration change, calling code should rely on the [LiveData] from
   * [getCurrentState] to know whether a current answer is pending. That [LiveData] will have its
   * state changed to pending during answer submission and until answer resolution.
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
   * No assumptions should be made about the completion order of the returned [LiveData] vs. the
   * [LiveData] from  [getCurrentState]. Also note that the returned [LiveData] will only have a
   * single value and not be reused after that point.
   */
  fun submitAnswer(userAnswer: UserAnswer): LiveData<AsyncResult<AnswerOutcome>> {
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
        asyncDataSubscriptionManager.notifyChangeAsync(CURRENT_STATE_DATA_PROVIDER_ID)

        lateinit var answerOutcome: AnswerOutcome
        try {
          val topPendingState = explorationProgress.stateDeck.getPendingTopState()
          val outcome =
            answerClassificationController.classify(
              topPendingState.interaction,
              userAnswer.answer
            ).outcome
          answerOutcome =
            explorationProgress.stateGraph.computeAnswerOutcomeForResult(topPendingState, outcome)
          explorationProgress.stateDeck.submitAnswer(userAnswer, answerOutcome.feedback)
          // Follow the answer's outcome to another part of the graph if it's different.
          if (answerOutcome.destinationCase == AnswerOutcome.DestinationCase.STATE_NAME) {
            explorationProgress.stateDeck.pushState(
              explorationProgress.stateGraph.getState(answerOutcome.stateName),
              prohibitSameStateName = true
            )
            // Reset the hintState if pending top state has changed.
            explorationProgress.hintState = hintHandler.reset()
          } else {
            // Schedule a new hints or solution or show a new hint or solution immediately based on
            // the current ephemeral state of the exploration because a new wrong answer was
            // submitted.
            val ephemeralState = explorationProgress.stateDeck.getCurrentEphemeralState()
            explorationProgress.hintState =
              hintHandler.maybeScheduleShowHint(
                ephemeralState.state,
                ephemeralState.pendingState.wrongAnswerCount
              )
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

        asyncDataSubscriptionManager.notifyChangeAsync(CURRENT_STATE_DATA_PROVIDER_ID)

        return MutableLiveData(AsyncResult.success(answerOutcome))
      }
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e)
      return MutableLiveData(AsyncResult.failed(e))
    }
  }

  fun submitHintIsRevealed(hintIsRevealed: Boolean, hintIndex: Int): LiveData<AsyncResult<Hint>> {
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
        lateinit var hint: Hint
        val ephemeralState = explorationProgress.stateDeck.getCurrentEphemeralState()
        try {
          explorationProgress.stateDeck.submitHintRevealed(
            ephemeralState.state,
            hintIsRevealed,
            hintIndex
          )
          hint = explorationProgress.stateGraph.computeHintForResult(
            ephemeralState.state,
            hintIsRevealed,
            hintIndex
          )
          explorationProgress.stateDeck.pushStateForHint(ephemeralState.state, hintIndex)
        } finally {
          hintHandler.notifyHintIsRevealed(hintIndex)
          // Schedule a new hints or solution or show a new hint or solution immediately based on
          // the current ephemeral state of the exploration because the last hint was revealed.
          explorationProgress.hintState =
            hintHandler.maybeScheduleShowHint(
              ephemeralState.state,
              ephemeralState.pendingState.wrongAnswerCount
            )
          // Mark a checkpoint in the exploration everytime a new hint is revealed.
          saveExplorationCheckpoint()
          // Ensure that the user always returns to the VIEWING_STATE stage to avoid getting stuck
          // in an 'always showing hint' situation. This can specifically happen if hint throws an
          // exception.
          explorationProgress.advancePlayStageTo(ExplorationProgress.PlayStage.VIEWING_STATE)
        }
        asyncDataSubscriptionManager.notifyChangeAsync(CURRENT_STATE_DATA_PROVIDER_ID)
        return MutableLiveData(AsyncResult.success(hint))
      }
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e)
      return MutableLiveData(AsyncResult.failed(e))
    }
  }

  fun submitSolutionIsRevealed(): LiveData<AsyncResult<Solution>> {
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
        lateinit var solution: Solution
        val ephemeralState = explorationProgress.stateDeck.getCurrentEphemeralState()
        try {
          explorationProgress.stateDeck.submitSolutionRevealed(ephemeralState.state)
          solution = explorationProgress.stateGraph.computeSolutionForResult(ephemeralState.state)
          explorationProgress.stateDeck.pushStateForSolution(ephemeralState.state)
        } finally {
          hintHandler.notifySolutionIsRevealed()
          // Update the hintState because the solution was revealed.
          explorationProgress.hintState =
            hintHandler.maybeScheduleShowHint(
              ephemeralState.state,
              ephemeralState.pendingState.wrongAnswerCount
            )
          // Mark a checkpoint in the exploration if the solution is revealed.
          saveExplorationCheckpoint()
          // Ensure that the user always returns to the VIEWING_STATE stage to avoid getting stuck
          // in an 'always showing solution' situation. This can specifically happen if solution
          // throws an exception.
          explorationProgress.advancePlayStageTo(ExplorationProgress.PlayStage.VIEWING_STATE)
        }

        asyncDataSubscriptionManager.notifyChangeAsync(CURRENT_STATE_DATA_PROVIDER_ID)
        return MutableLiveData(AsyncResult.success(solution))
      }
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e)
      return MutableLiveData(AsyncResult.failed(e))
    }
  }

  /**
   * Navigates to the previous state in the graph. If the learner is currently on the initial state,
   * this method will throw an exception. Calling code is responsible for ensuring this method is
   * only called when it's possible to navigate backward.
   *
   * @return a one-time [LiveData] indicating whether the movement to the previous state was
   *     successful, or a failure if state navigation was attempted at an invalid time in the state
   *     graph (e.g. if currently viewing the initial state of the exploration). It's recommended
   *     that calling code only listen to this result for failures, and instead rely on
   *     [getCurrentState] for observing a successful transition to another state.
   */
  fun moveToPreviousState(): LiveData<AsyncResult<Any?>> {
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
        explorationProgress.stateDeck.navigateToPreviousState()
        asyncDataSubscriptionManager.notifyChangeAsync(CURRENT_STATE_DATA_PROVIDER_ID)
      }
      return MutableLiveData(AsyncResult.success<Any?>(null))
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e)
      return MutableLiveData(AsyncResult.failed(e))
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
   * @return a one-time [LiveData] indicating whether the movement to the next state was successful,
   *     or a failure if state navigation was attempted at an invalid time in the state graph (e.g.
   *     if the current state is pending or terminal). It's recommended that calling code only
   *     listen to this result for failures, and instead rely on [getCurrentState] for observing a
   *     successful transition to another state.
   */

  fun moveToNextState(): LiveData<AsyncResult<Any?>> {
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
          // Update the hint state and maybe schedule new help when user moves to the pending top
          // state.
          val ephemeralState = explorationProgress.stateDeck.getCurrentEphemeralState()
          explorationProgress.hintState =
            hintHandler.maybeScheduleShowHint(
              ephemeralState.state,
              ephemeralState.pendingState.wrongAnswerCount
            )
          // Only mark checkpoint if current state is pending state. This ensures that checkpoints
          // will not be marked on any of the completed states.
          saveExplorationCheckpoint()
        }
        asyncDataSubscriptionManager.notifyChangeAsync(CURRENT_STATE_DATA_PROVIDER_ID)
      }
      return MutableLiveData(AsyncResult.success<Any?>(null))
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e)
      return MutableLiveData(AsyncResult.failed(e))
    }
  }

  /** Stops any new hints and solution from showing up. */
  fun stopNewHintsAndSolutionFromShowingUp() {
    explorationProgressLock.withLock {
      hintHandler.hideHintsAndSolution()
    }
  }

  /**
   * Notifies the [HintHandler] that a scheduled task to show new help has finished.
   *
   * @param trackedSequenceNumber the ID used to identify each task scheduled to show help
   * @param state the state on which the hint is shown
   */
  fun hintAndSolutionTimerCompleted(trackedSequenceNumber: Int, state: State) {
    explorationProgressLock.withLock {
      explorationProgress.hintState =
        hintHandler.showNewHintAndSolution(state, trackedSequenceNumber)
      if (
        explorationProgress.hintState.helpIndex.indexTypeCase ==
        HelpIndex.IndexTypeCase.HINT_INDEX ||
        explorationProgress.hintState.helpIndex.indexTypeCase ==
        HelpIndex.IndexTypeCase.SHOW_SOLUTION
      ) {
        // Only notify the currentState dataProvider the hint or solution is available is actually
        // new.
        asyncDataSubscriptionManager.notifyChangeAsync(CURRENT_STATE_DATA_PROVIDER_ID)
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
  fun getCurrentState(): DataProvider<EphemeralState> = currentStateDataProvider

  private suspend fun retrieveCurrentStateAsync(): AsyncResult<EphemeralState> {
    return try {
      retrieveCurrentStateWithinCacheAsync()
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e)
      AsyncResult.failed(e)
    }
  }

  private suspend fun retrieveCurrentStateWithinCacheAsync(): AsyncResult<EphemeralState> {
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
        ExplorationProgress.PlayStage.NOT_PLAYING -> AsyncResult.pending()
        ExplorationProgress.PlayStage.LOADING_EXPLORATION -> {
          try {
            // The exploration must be available for this stage since it was loaded above.
            finishLoadExploration(exploration!!, explorationProgress)
            AsyncResult.success(
              explorationProgress.stateDeck.getCurrentEphemeralState()
                .toBuilder()
                .setHintState(explorationProgress.hintState)
                .setCheckpointState(explorationProgress.currentCheckpointState)
                .build()
            )
          } catch (e: Exception) {
            exceptionsController.logNonFatalException(e)
            AsyncResult.failed<EphemeralState>(e)
          }
        }
        ExplorationProgress.PlayStage.VIEWING_STATE ->
          AsyncResult.success(
            explorationProgress.stateDeck.getCurrentEphemeralState()
              .toBuilder()
              .setHintState(explorationProgress.hintState)
              .setCheckpointState(explorationProgress.currentCheckpointState)
              .build()
          )
        ExplorationProgress.PlayStage.SUBMITTING_ANSWER -> AsyncResult.pending()
      }
    }
  }

  private fun finishLoadExploration(exploration: Exploration, progress: ExplorationProgress) {
    // The exploration must be initialized first since other lazy fields depend on it being inited.
    progress.currentExploration = exploration
    progress.stateGraph.reset(exploration.statesMap)
    // Restore StateDeck with the saved checkpoint if the exploration has to be resumed otherwise,
    // reset it to play the exploration from the beginning.
    loadStateDeck(progress, exploration)
    // Restore the HintState to resume showing hints or reset it if the exploration does not have
    // to be resumed.
    loadHintState(progress)

    // Advance the stage, but do not notify observers since the current state can be reported
    // immediately to the UI.
    progress.advancePlayStageTo(ExplorationProgress.PlayStage.VIEWING_STATE)

    // Update hint state to schedule task to show new help.
    val ephemeralState = progress.stateDeck.getCurrentEphemeralState()
    progress.hintState = hintHandler.maybeScheduleShowHint(
      ephemeralState.state,
      ephemeralState.pendingState.wrongAnswerCount
    )

    // Mark a checkpoint in the exploration once the exploration has loaded.
    saveExplorationCheckpoint()
  }

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
        explorationProgress.hintState.helpIndex,
        explorationProgress.currentExploration.version,
        explorationProgress.currentExploration.title,
        oppiaClock.getCurrentTimeMs()
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
      if (explorationProgress.currentCheckpointState != newCheckpointState) {
        // Mark exploration as IN_PROGRESS_SAVED or IN_PROGRESS_NOT_SAVED if the checkpointState has
        // either changed from UNSAVED to SAVED or vice versa.
        if (
          explorationProgress.currentCheckpointState != CheckpointState.CHECKPOINT_UNSAVED &&
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
          explorationProgress.currentCheckpointState == CheckpointState.CHECKPOINT_UNSAVED &&
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
        asyncDataSubscriptionManager.notifyChangeAsync(CURRENT_STATE_DATA_PROVIDER_ID)
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

  private fun loadHintState(progress: ExplorationProgress) {
    progress.hintState =
      if (progress.currentExplorationCheckpoint == ExplorationCheckpoint.getDefaultInstance()) {
        HintState.getDefaultInstance()
      } else {
        HintState.newBuilder().apply {
          helpIndex = progress.currentExplorationCheckpoint.helpIndex
          trackedAnswerCount = progress.currentExplorationCheckpoint.pendingUserAnswersCount
          hintSequenceNumber = 0
          delayToShowNextHintAndSolution = -1
        }.build()
      }
  }

  /**
   * Initializes the variables of [StateDeck]. If the [ExplorationCheckpoint] is of type default
   * instance, the values of [StateDeck] are reset. Otherwise, the variables of [StateDeck] are
   * re-initialized with the values created from the saved [ExplorationCheckpoint].
   *
   * This function expects explorationProgress.hintState to be initialized with the correct values,
   * so it should only be called after the function [loadHintState] has executed.
   */
  private fun loadStateDeck(progress: ExplorationProgress, exploration: Exploration) {
    if (progress.currentExplorationCheckpoint == ExplorationCheckpoint.getDefaultInstance()) {
      progress.stateDeck.resetDeck(progress.stateGraph.getState(exploration.initStateName))
    } else {
      progress.stateDeck.resumeDeck(
        createPendingTopStateFromCheckpoint(progress),
        getPreviousStatesFromCheckpoint(progress),
        progress.currentExplorationCheckpoint.pendingUserAnswersList,
        progress.currentExplorationCheckpoint.stateIndex
      )
    }
  }

  /**
   * Creates a pending top state for the current exploration as it was when the checkpoint was
   * created.
   *
   * @return the pending [State] for the current exploration
   */
  private fun createPendingTopStateFromCheckpoint(progress: ExplorationProgress): State {
    val pendingTopState =
      progress.stateGraph.getState(progress.currentExplorationCheckpoint.pendingStateName)
    val hintList = createHintListFromCheckpoint(
      pendingTopState.interaction.hintList,
      progress.currentExplorationCheckpoint.helpIndex
    )
    val solution = createSolutionFromCheckpoint(pendingTopState, progress.hintState.helpIndex)
    val interactionBuilder =
      pendingTopState.interaction.toBuilder()
        .clearHint()
        .addAllHint(hintList)
        .setSolution(solution)
        .build()
    return pendingTopState.toBuilder().setInteraction(interactionBuilder).build()
  }

  /**
   * Mark all hints as reveled in the pendingState that were revealed for the current state pending
   * state before the checkpoint was saved.
   *
   * @param pendingStateHintList the list of hint for the current pending state
   * @param helpIndex the state of hints for the exploration which was generated using the saved
   *     checkpoint
   */
  private fun createHintListFromCheckpoint(
    pendingStateHintList: List<Hint>,
    helpIndex: HelpIndex
  ): List<Hint> {
    val updatedHintList: MutableList<Hint> = ArrayList()
    when (helpIndex.indexTypeCase) {
      HelpIndex.IndexTypeCase.HINT_INDEX -> {
        pendingStateHintList.forEachIndexed { index, hint ->
          if (index < helpIndex.hintIndex.index) {
            // Mark all hints as visible and revealed which have an index less than that stored in
            // the HintState.
            updatedHintList.add(hint.toBuilder().setHintIsRevealed(true).build())
          } else if (index == helpIndex.hintIndex.index) {
            // Add the currently shown hint to the updated hint list and set hintIsRevealed
            // depending upon if the current hint was revealed or not.
            updatedHintList.add(
              hint.toBuilder().setHintIsRevealed(helpIndex.hintIndex.isHintRevealed).build()
            )
          } else {
            // Add all the remaining hints that are not yet visible to the user to the updated hint
            // list.
            updatedHintList.add(
              hint.toBuilder().setHintIsRevealed(false).build()
            )
          }
        }
      }
      HelpIndex.IndexTypeCase.SHOW_SOLUTION, HelpIndex.IndexTypeCase.EVERYTHING_REVEALED -> {
        // All the hints are visible and revealed if helpIndex.indexTypeCase is equal to
        // SHOW_SOLUTION or EVERYTHING_REVEALED.
        pendingStateHintList.forEach { hint ->
          updatedHintList.add(hint.toBuilder().setHintIsRevealed(true).build())
        }
      }
      else -> updatedHintList.addAll(pendingStateHintList)
    }
    return updatedHintList
  }

  /**
   * Set solution is reveled in the pendingState to true or false depending upon if solution was
   * revealed for the current state pending state before the checkpoint was saved.
   *
   * @param pendingTopState the pending state created from the checkpoint
   * @param helpIndex the state of solution for the exploration which was generated using the saved
   *     checkpoint
   */
  private fun createSolutionFromCheckpoint(
    pendingTopState: State,
    helpIndex: HelpIndex
  ): Solution {
    return when (helpIndex.indexTypeCase) {
      HelpIndex.IndexTypeCase.SHOW_SOLUTION -> {
        pendingTopState.interaction.solution.toBuilder()
          .setSolutionIsRevealed(false)
          .build()
      }
      HelpIndex.IndexTypeCase.EVERYTHING_REVEALED -> {
        pendingTopState.interaction.solution.toBuilder()
          .setSolutionIsRevealed(true)
          .build()
      }
      else -> pendingTopState.interaction.solution
    }
  }

  /**
   * Creates a list of completed states from the saved [ExplorationCheckpoint].
   *
   * @return [List] of [EphemeralState] containing all the states that were completed before the
   *     checkpoint was created
   */
  private fun getPreviousStatesFromCheckpoint(
    progress: ExplorationProgress
  ): List<EphemeralState> {
    val previousStates: MutableList<EphemeralState> = ArrayList()
    progress.currentExplorationCheckpoint.completedStatesInCheckpointList
      .forEachIndexed { index, state ->
        previousStates.add(
          EphemeralState.newBuilder()
            .setState(progress.stateGraph.getState(state.stateName))
            .setHasPreviousState(index != 0)
            .setCompletedState(state.completedState)
            .setHasNextState(index != progress.currentExplorationCheckpoint.stateIndex)
            .build()
        )
      }
    return previousStates
  }
}
