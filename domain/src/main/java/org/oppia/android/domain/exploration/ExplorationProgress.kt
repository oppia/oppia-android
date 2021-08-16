package org.oppia.android.domain.exploration

import org.oppia.android.app.model.CheckpointState
import org.oppia.android.app.model.EphemeralState
import org.oppia.android.app.model.Exploration
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.Hint
import org.oppia.android.app.model.HintState
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Solution
import org.oppia.android.app.model.State
import org.oppia.android.domain.state.StateDeck
import org.oppia.android.domain.state.StateGraph

// TODO(#186): Use an interaction repository to retrieve whether a specific ID corresponds to a terminal interaction.
private const val TERMINAL_INTERACTION_ID = "EndExploration"

/**
 * Private class that encapsulates the mutable state of an exploration progress controller. This class is not
 * thread-safe, so owning classes should ensure synchronized access. This class can exist across multiple exploration
 * instances, but calling code is responsible for ensuring it is properly reset.
 */
internal class ExplorationProgress {
  internal lateinit var currentProfileId: ProfileId
  internal lateinit var currentTopicId: String
  internal lateinit var currentStoryId: String
  internal lateinit var currentExplorationId: String
  internal lateinit var currentExploration: Exploration

  internal var shouldSavePartialProgress: Boolean = false
  internal lateinit var currentCheckpointState: CheckpointState
  internal lateinit var currentExplorationCheckpoint: ExplorationCheckpoint

  internal var playStage = PlayStage.NOT_PLAYING
  internal val stateGraph: StateGraph by lazy {
    StateGraph(currentExploration.statesMap)
  }
  internal val stateDeck: StateDeck by lazy {
    StateDeck(stateGraph.getState(currentExploration.initStateName), ::isTopStateTerminal)
  }

  internal lateinit var hintState: HintState

  /**
   * Advances the current play stage to the specified stage, verifying that the transition is correct.
   *
   * Calling code should prevent this method from failing by checking state ahead of calling this method and providing
   * more useful errors to UI calling code since errors thrown by this method will be more obscure. This method aims to
   * ensure the internal state of the controller remains correct. This method is not meant to be covered in unit tests
   * since none of the failures here should ever be exposed to controller callers.
   */
  internal fun advancePlayStageTo(nextPlayStage: PlayStage) {
    when (nextPlayStage) {
      PlayStage.NOT_PLAYING -> {
        // All transitions to NOT_PLAYING are valid except itself. Stopping playing can happen at any time.
        check(playStage != PlayStage.NOT_PLAYING) {
          "Cannot transition to NOT_PLAYING from NOT_PLAYING"
        }
        playStage = nextPlayStage
      }
      PlayStage.LOADING_EXPLORATION -> {
        // An exploration can only be requested to be loaded from the initial NOT_PLAYING stage.
        check(playStage == PlayStage.NOT_PLAYING) {
          "Cannot transition to LOADING_EXPLORATION from $playStage"
        }
        playStage = nextPlayStage
      }
      PlayStage.VIEWING_STATE -> {
        // A state can be viewed after loading an exploration, after viewing another state, or after submitting an
        // answer. It cannot be viewed without a loaded exploration.
        check(
          playStage == PlayStage.LOADING_EXPLORATION ||
            playStage == PlayStage.VIEWING_STATE ||
            playStage == PlayStage.SUBMITTING_ANSWER
        ) {
          "Cannot transition to VIEWING_STATE from $playStage"
        }
        playStage = nextPlayStage
      }
      PlayStage.SUBMITTING_ANSWER -> {
        // An answer can only be submitted after viewing a stage.
        check(playStage == PlayStage.VIEWING_STATE) {
          "Cannot transition to SUBMITTING_ANSWER from $playStage"
        }
        playStage = nextPlayStage
      }
    }
  }

  /**
   * Updates the checkpointState to a new state depending upon the result of save operation for
   * checkpoints.
   *
   * @param newCheckpointState is the latest checkpoint state that is returned upon
   *     completion of the save operation for checkpoints either successfully or unsuccessfully.
   */
  internal fun updateCheckpointState(newCheckpointState: CheckpointState) {
    currentCheckpointState = newCheckpointState
  }

  companion object {
    internal fun isTopStateTerminal(state: State): Boolean {
      return state.interaction.id == TERMINAL_INTERACTION_ID
    }
  }

  /** Different stages in which the progress controller can exist. */
  enum class PlayStage {
    /** No exploration is currently being played. */
    NOT_PLAYING,

    /** An exploration is being prepared to be played. */
    LOADING_EXPLORATION,

    /** The controller is currently viewing a State. */
    VIEWING_STATE,

    /** The controller is in the process of submitting an answer. */
    SUBMITTING_ANSWER
  }

  /**
   * Updates the hint state with the one created from the saved checkpoint or the default instance
   * of hint state if exploration is not being resumed.
   */
  internal fun loadHintState() {
    hintState =
      if (currentExplorationCheckpoint == ExplorationCheckpoint.getDefaultInstance()) {
        HintState.getDefaultInstance()
      } else {
        HintState.newBuilder().apply {
          helpIndex = currentExplorationCheckpoint.helpIndex
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
   *
   * @param exploration the current [Exploration] that has to be played
   */
  internal fun loadStateDeck(exploration: Exploration) {
    if (currentExplorationCheckpoint == ExplorationCheckpoint.getDefaultInstance()) {
      stateDeck.resetDeck(stateGraph.getState(exploration.initStateName))
    } else {
      stateDeck.resumeDeck(
        createPendingTopStateFromCheckpoint(),
        getPreviousStatesFromCheckpoint(),
        currentExplorationCheckpoint.pendingUserAnswersList,
        currentExplorationCheckpoint.stateIndex
      )
    }
  }

  /**
   * Creates a pending top state for the current exploration as it was when the checkpoint was
   * created.
   *
   * @return the pending [State] for the current exploration
   */
  private fun createPendingTopStateFromCheckpoint(): State {
    val pendingTopState =
      stateGraph.getState(currentExplorationCheckpoint.pendingStateName)
    val hintList = createHintListFromCheckpoint(
      pendingTopState.interaction.hintList,
      currentExplorationCheckpoint.helpIndex
    )
    val solution =
      createSolutionFromCheckpoint(pendingTopState, currentExplorationCheckpoint.helpIndex)
    val interactionBuilder = Interaction.newBuilder().addAllHint(hintList).setSolution(solution)

    return pendingTopState.toBuilder().setInteraction(interactionBuilder.build()).build()
  }

  /**
   * Mark all hints as reveled in the pendingState that were revealed for the current state pending
   * state before the checkpoint was saved.
   *
   * @param pendingStateHintList the list of hint for the current pending state
   * @param helpIndex the state of hints for the exploration which was generated using the saved
   *     checkpoint
   *
   * @return the updated list of [Hint]s for the pending state created from the saved checkpoint
   */
  private fun createHintListFromCheckpoint(
    pendingStateHintList: List<Hint>,
    helpIndex: HelpIndex
  ): List<Hint> {
    return when (helpIndex.indexTypeCase) {
      HelpIndex.IndexTypeCase.LATEST_REVEALED_HINT_INDEX -> {
        pendingStateHintList.mapIndexed { index, hint ->
          hint.toBuilder().setHintIsRevealed(index <= helpIndex.latestRevealedHintIndex).build()
        }
      }
      HelpIndex.IndexTypeCase.AVAILABLE_NEXT_HINT_INDEX -> {
        pendingStateHintList.mapIndexed { index, hint ->
          hint.toBuilder().setHintIsRevealed(index < helpIndex.availableNextHintIndex).build()
        }
      }
      HelpIndex.IndexTypeCase.SHOW_SOLUTION, HelpIndex.IndexTypeCase.EVERYTHING_REVEALED -> {
        // All the hints are visible and revealed if helpIndex.indexTypeCase is equal to
        // SHOW_SOLUTION or EVERYTHING_REVEALED.
        pendingStateHintList.map { hint ->
          hint.toBuilder().setHintIsRevealed(true).build()
        }
      }
      else -> pendingStateHintList
    }
  }

  /**
   * Set solution is reveled in the pendingState to true or false depending upon if solution was
   * revealed for the current state pending state before the checkpoint was saved.
   *
   * @param pendingTopState the pending state created from the checkpoint
   * @param helpIndex the state of solution for the exploration which was generated using the saved
   *     checkpoint
   *
   * @return the updated [Solution] for the pending state created from the checkpoint
   */
  private fun createSolutionFromCheckpoint(
    pendingTopState: State,
    helpIndex: HelpIndex
  ): Solution {
    val solutionBuilder = pendingTopState.interaction.solution.toBuilder()
    return when (helpIndex.indexTypeCase) {
      HelpIndex.IndexTypeCase.SHOW_SOLUTION -> {
        solutionBuilder.setSolutionIsRevealed(false).build()
      }
      HelpIndex.IndexTypeCase.EVERYTHING_REVEALED -> {
        solutionBuilder.setSolutionIsRevealed(true).build()
      }
      else -> solutionBuilder.build()
    }
  }

  /**
   * Creates a list of completed states from the saved [ExplorationCheckpoint].
   *
   * @return [List] of [EphemeralState]s containing all the states that were completed before the
   *     checkpoint was created
   */
  private fun getPreviousStatesFromCheckpoint(): List<EphemeralState> {
    return currentExplorationCheckpoint.completedStatesInCheckpointList
      .mapIndexed { index, state ->
        EphemeralState.newBuilder()
          .setState(stateGraph.getState(state.stateName))
          .setHasPreviousState(index != 0)
          .setCompletedState(state.completedState)
          .setHasNextState(index != currentExplorationCheckpoint.stateIndex)
          .build()
      }
  }
}
