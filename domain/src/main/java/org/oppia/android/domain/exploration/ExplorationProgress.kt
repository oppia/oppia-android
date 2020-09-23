package org.oppia.android.domain.exploration

import org.oppia.android.app.model.Exploration
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
  internal lateinit var currentExplorationId: String
  internal lateinit var currentExploration: Exploration
  internal var playStage = PlayStage.NOT_PLAYING
  internal val stateGraph: StateGraph by lazy {
    StateGraph(currentExploration.statesMap)
  }
  internal val stateDeck: StateDeck by lazy {
    StateDeck(stateGraph.getState(currentExploration.initStateName), ::isTopStateTerminal)
  }

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
}
