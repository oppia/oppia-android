package org.oppia.android.domain.exploration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.withLock
import org.oppia.android.app.model.AnswerOutcome
import org.oppia.android.app.model.EphemeralState
import org.oppia.android.app.model.Exploration
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.Hint
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Solution
import org.oppia.android.app.model.State
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.domain.classify.AnswerClassificationController
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationCheckpointController
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationCheckpointState
import org.oppia.android.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.android.domain.topic.StoryProgressController
import org.oppia.android.util.data.AsyncDataSubscriptionManager
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.system.OppiaClock

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
  private val oppiaClock: OppiaClock
) {
  // TODO(#179): Add support for parameters.
  // TODO(#182): Add support for refresher explorations.
  // TODO(#90): Update the internal locking of this controller to use something like an in-memory
  //  blocking cache to simplify state locking. However, doing this correctly requires a fix in
  //  MediatorLiveData to avoid unexpected cancellations in chained cross-scope coroutines. Note
  //  that this is also essential to ensure post-load operations can be queued before load completes
  //  to avoid cases in tests where the exploration load operation needs to be fully finished before
  //  performing a post-load operation. The current state of the controller is leaking this
  //  implementation detail to tests.
  // TODO(): Update the saving mechanism for checkpoints once internal locking of this controller
  //  updated.

  /** Indicates that the checkpoint database has exceeded the allocated limit.. */
  class CheckpointDatabaseOverflowException(msg: String) : Exception(msg)

  /** Indicates that the current exploration is not completely saved. */
  class ProgressNotSavedException(msg: String) : Exception(msg)

  private val currentStateDataProvider =
    dataProviders.createInMemoryDataProviderAsync(
      CURRENT_STATE_DATA_PROVIDER_ID,
      this::retrieveCurrentStateAsync
    )

  private val explorationProgress = ExplorationProgress()
  private val explorationProgressLock = ReentrantLock()

  /** Resets this controller to begin playing the specified [Exploration]. */
  internal fun beginExplorationAsync(explorationId: String) {
    explorationProgressLock.withLock {
      check(explorationProgress.playStage == ExplorationProgress.PlayStage.NOT_PLAYING) {
        "Expected to finish previous exploration before starting a new one."
      }

      explorationProgress.currentExplorationId = explorationId
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
   * This function checks the current checkpoint state so that the learner can exit a partially
   * complete exploration. This function returns a success result if the progress for the current
   * exploration is saved and the checkpoint database has not exceeded the allocated limit.
   * Failure result is returned in any one of the following two cases,
   * if the exploration contains unsaved progress or the progress is saved but the checkpoint
   * database has exceeded the allocated size limit. In the progress is not saved the failure result
   * is returned with the exception [ProgressNotSavedException]. If the progress is saved but the
   * checkpoint database has exceeded the allocated size limit, failure result is returned with the
   * exception [CheckpointDatabaseOverflowException].
   *
   * @return a one-time [LiveData] that indicates success/failure depending upon the checkpoint
   *         state. Failure result is returned with an appropriate exception.
   */
  internal fun checkCheckpointStateToExitExploration(): LiveData<AsyncResult<Any?>> {
    return explorationProgressLock.withLock {
      when (explorationProgress.checkpointState) {
        ExplorationCheckpointState.CHECKPOINT_SAVED_DATABASE_NOT_EXCEEDED_LIMIT ->
          MutableLiveData(AsyncResult.success(null))
        ExplorationCheckpointState.CHECKPOINT_SAVED_DATABASE_EXCEEDED_LIMIT ->
          MutableLiveData(
            AsyncResult.failed(
              CheckpointDatabaseOverflowException(
                "Checkpoint database has exceeded the allocated size limit."
              )
            )
          )
        ExplorationCheckpointState.UNSAVED -> {
          MutableLiveData(
            AsyncResult.failed(
              ProgressNotSavedException("Current exploration contains unsaved progress.")
            )
          )
        }
      }
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
          }
        } finally {
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

  fun submitHintIsRevealed(
    state: State,
    hintIsRevealed: Boolean,
    hintIndex: Int
  ): LiveData<AsyncResult<Hint>> {
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
        try {
          explorationProgress.stateDeck.submitHintRevealed(state, hintIsRevealed, hintIndex)
          hint = explorationProgress.stateGraph.computeHintForResult(
            state,
            hintIsRevealed,
            hintIndex
          )
          explorationProgress.stateDeck.pushStateForHint(state, hintIndex)
        } finally {
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

  fun submitSolutionIsRevealed(
    state: State
  ): LiveData<AsyncResult<Solution>> {
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
        try {

          explorationProgress.stateDeck.submitSolutionRevealed(state)
          solution = explorationProgress.stateGraph.computeSolutionForResult(state)
          explorationProgress.stateDeck.pushStateForSolution(state)
        } finally {
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
        asyncDataSubscriptionManager.notifyChangeAsync(CURRENT_STATE_DATA_PROVIDER_ID)
      }
      return MutableLiveData(AsyncResult.success<Any?>(null))
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e)
      return MutableLiveData(AsyncResult.failed(e))
    }
  }

  /**
   *  Creates an [ExplorationCheckpoint] and then saves it using [ExplorationCheckpointController].
   *
   *  @return a [DataProvider] that indicates the success/failure of the save operation.
   */
  fun saveExplorationCheckpoint(profileId: ProfileId): DataProvider<Any?> {
    lateinit var checkpoint: ExplorationCheckpoint
    lateinit var explorationId: String
    explorationProgressLock.withLock {
      checkpoint = explorationProgress.stateDeck.createExplorationCheckpoint(
        explorationProgress.currentExploration.version,
        explorationProgress.currentExploration.title,
        oppiaClock.getCurrentTimeMs()
      )
      explorationId = explorationProgress.currentExplorationId
    }
    return explorationCheckpointController.recordExplorationCheckpoint(
      profileId,
      explorationId,
      checkpoint
    )
  }

  /**
   * This function process the result obtained upon observing the function
   * [saveExplorationCheckpoint].
   *
   * Marks the exploration as in_progress_saved or in_progress_not_saved if it is not already marked
   * correctly. This function also updates the checkpoint state of the exploration to the
   * specified new checkpoint state.
   *
   * @param profileId is the profile id currently playing the exploration.
   * @param topicId is the id of the topic which contains the story with the current exploration.
   * @param storyId is the id of the story which contains the current exploration.
   * @param lastPlayedTimestamp timestamp of the time when the checkpoints state for the exploration
   *        was last updated.
   * @param newCheckpointState the latest state obtained after saving checkpoint successfully or
   *        unsuccessfully.
   */
  fun processSaveCheckpointResult(
    profileId: ProfileId,
    topicId: String,
    storyId: String,
    explorationId: String,
    lastPlayedTimestamp: Long,
    newCheckpointState: ExplorationCheckpointState
  ) {
    explorationProgressLock.withLock {
      if (explorationProgress.checkpointState != newCheckpointState) {
        if (
          explorationProgress.checkpointState != ExplorationCheckpointState.UNSAVED &&
          newCheckpointState == ExplorationCheckpointState.UNSAVED
        )
          markExplorationAsInProgressNotSaved(
            profileId,
            topicId,
            storyId,
            explorationId,
            lastPlayedTimestamp
          )
        else if (
          explorationProgress.checkpointState == ExplorationCheckpointState.UNSAVED &&
          newCheckpointState != ExplorationCheckpointState.UNSAVED
        )
          markExplorationAsInProgressSaved(
            profileId,
            topicId,
            storyId,
            explorationId,
            lastPlayedTimestamp
          )
        explorationProgress.updateCheckpointState(newCheckpointState)
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
            AsyncResult.success(explorationProgress.stateDeck.getCurrentEphemeralState())
          } catch (e: Exception) {
            exceptionsController.logNonFatalException(e)
            AsyncResult.failed<EphemeralState>(e)
          }
        }
        ExplorationProgress.PlayStage.VIEWING_STATE ->
          AsyncResult.success(explorationProgress.stateDeck.getCurrentEphemeralState())
        ExplorationProgress.PlayStage.SUBMITTING_ANSWER -> AsyncResult.pending()
      }
    }
  }

  private fun finishLoadExploration(exploration: Exploration, progress: ExplorationProgress) {
    // The exploration must be initialized first since other lazy fields depend on it being inited.
    progress.currentExploration = exploration
    progress.stateGraph.reset(exploration.statesMap)
    progress.stateDeck.resetDeck(progress.stateGraph.getState(exploration.initStateName))

    // Advance the stage, but do not notify observers since the current state can be reported
    // immediately to the UI.
    progress.advancePlayStageTo(ExplorationProgress.PlayStage.VIEWING_STATE)
  }

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
