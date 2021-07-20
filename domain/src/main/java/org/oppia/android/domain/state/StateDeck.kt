package org.oppia.android.domain.state

import org.oppia.android.app.model.AnswerAndResponse
import org.oppia.android.app.model.CompletedState
import org.oppia.android.app.model.CompletedStateInCheckpoint
import org.oppia.android.app.model.EphemeralState
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.Hint
import org.oppia.android.app.model.PendingState
import org.oppia.android.app.model.Solution
import org.oppia.android.app.model.State
import org.oppia.android.app.model.SubtitledHtml
import org.oppia.android.app.model.UserAnswer

// TODO(#59): Hide the visibility of this class to domain implementations.

/**
 * Tracks the progress of a dynamic playing session through a graph of State cards. This class treats the learner's
 * progress like a deck of cards to simplify forward/backward navigation.
 */
internal class StateDeck internal constructor(
  initialState: State,
  private val isTopOfDeckTerminalChecker: (State) -> Boolean
) {
  private var pendingTopState: State = initialState
  private val previousStates: MutableList<EphemeralState> = ArrayList()
  private val currentDialogInteractions: MutableList<AnswerAndResponse> = ArrayList()
  private val hintList: MutableList<Hint> = ArrayList()
  private lateinit var solution: Solution
  private var stateIndex: Int = 0
  // The value -1 indicates that hint has not been revealed yet.
  private var revealedHintIndex: Int = -1
  private var solutionIsRevealed: Boolean = false

  /** Resets this deck to a new, specified initial [State]. */
  internal fun resetDeck(initialState: State) {
    pendingTopState = initialState
    previousStates.clear()
    currentDialogInteractions.clear()
    hintList.clear()
    stateIndex = 0
    // Initialize the variable revealedHintIndex with -1 to indicate that no hint has been
    // revealed yet.
    revealedHintIndex = -1
    solutionIsRevealed = false
  }

  /** Navigates to the previous State in the deck, or fails if this isn't possible. */
  internal fun navigateToPreviousState() {
    check(!isCurrentStateInitial()) { "Cannot navigate to previous state; at initial state." }
    stateIndex--
  }

  /** Navigates to the next State in the deck, or fails if this isn't possible. */
  internal fun navigateToNextState() {
    check(!isCurrentStateTopOfDeck()) { "Cannot navigate to next state; at most recent state." }
    val previousState = previousStates[stateIndex]
    stateIndex++
    if (!previousState.hasNextState) {
      // Update the previous state to indicate that it has a next state now that its next state has actually been
      // 'created' by navigating to it.
      previousStates[stateIndex - 1] = previousState.toBuilder().setHasNextState(true).build()
    }
  }

  /**
   * Returns the [State] corresponding to the latest card in the deck, regardless of whichever State the learner is
   * currently viewing.
   */
  internal fun getPendingTopState(): State = pendingTopState

  /** Returns the index of the current selected card of the deck. */
  internal fun getTopStateIndex(): Int = stateIndex

  /** Returns the current [EphemeralState] the learner is viewing. */
  internal fun getCurrentEphemeralState(): EphemeralState {
    // Note that the terminal state is evaluated first since it can only return true if the current state is the top
    // of the deck, and that state is the terminal one. Otherwise the terminal check would never be triggered since
    // the second case assumes the top of the deck must be pending.
    return when {
      isCurrentStateTerminal() -> getCurrentTerminalState()
      stateIndex == previousStates.size -> getCurrentPendingState()
      else -> getPreviousState()
    }
  }

  /**
   * Pushes a new State onto the deck. This cannot happen if the learner isn't at the most recent State, if the
   * current State is not terminal, or if the learner hasn't submitted an answer to the most recent State. This
   * operation implies that the most recently submitted answer was the correct answer to the previously current State.
   * This does NOT change the user's position in the deck, it just marks the current state as completed.
   *
   * @param prohibitSameStateName whether to enable a sanity check to ensure the same state isn't routed to twice
   */
  internal fun pushState(state: State, prohibitSameStateName: Boolean) {
    check(isCurrentStateTopOfDeck()) {
      "Cannot push a new state unless the learner is at the most recent state."
    }
    check(!isCurrentStateTerminal()) {
      "Cannot push another state after reaching a terminal state."
    }
    check(currentDialogInteractions.size != 0) {
      "Cannot push another state without an answer."
    }
    if (prohibitSameStateName) {
      check(state.name != pendingTopState.name) {
        "Cannot route from the same state to itself as a new card."
      }
    }
    // NB: This technically has a 'next' state, but it's not marked until it's first navigated away since the new state
    // doesn't become fully realized until navigated to.
    previousStates += EphemeralState.newBuilder()
      .setState(pendingTopState)
      .setHasPreviousState(!isCurrentStateInitial())
      .setCompletedState(CompletedState.newBuilder().addAllAnswer(currentDialogInteractions))
      .build()
    currentDialogInteractions.clear()
    hintList.clear()
    pendingTopState = state
    // Re-initialize the variable revealedHintIndex with -1 to indicate that no hint has been
    // revealed on the new pendingTopState.
    revealedHintIndex = -1
    solutionIsRevealed = false
  }

  internal fun pushStateForHint(state: State, hintIndex: Int): EphemeralState {
    val interactionBuilder = state.interaction.toBuilder().setHint(
      hintIndex,
      hintList.get(0)
    )
    val newState = state.toBuilder().setInteraction(interactionBuilder).build()
    val ephemeralState = EphemeralState.newBuilder()
      .setState(newState)
      .setHasPreviousState(!isCurrentStateInitial())
      .setPendingState(
        PendingState.newBuilder().addAllWrongAnswer(currentDialogInteractions).addAllHint(hintList)
      )
      .build()
    pendingTopState = newState
    hintList.clear()
    // Increment the value of revealHintIndex by 1 every-time a new hint is revealed.
    revealedHintIndex++
    return ephemeralState
  }

  internal fun pushStateForSolution(state: State): EphemeralState {
    val interactionBuilder = state.interaction.toBuilder().setSolution(solution)
    val newState = state.toBuilder().setInteraction(interactionBuilder).build()
    val ephemeralState = EphemeralState.newBuilder()
      .setState(newState)
      .setHasPreviousState(!isCurrentStateInitial())
      .setPendingState(
        PendingState.newBuilder().addAllWrongAnswer(currentDialogInteractions).addAllHint(hintList)
      )
      .build()
    pendingTopState = newState
    solutionIsRevealed = true
    return ephemeralState
  }

  /**
   * Submits an answer & feedback dialog the learner experience in the current State. This fails if the user is not at
   * the most recent State in the deck, or if the most recent State is terminal (since no answer can be submitted to a
   * terminal interaction).
   */
  internal fun submitAnswer(userAnswer: UserAnswer, feedback: SubtitledHtml) {
    check(isCurrentStateTopOfDeck()) { "Cannot submit an answer except to the most recent state." }
    check(!isCurrentStateTerminal()) { "Cannot submit an answer to a terminal state." }
    currentDialogInteractions += AnswerAndResponse.newBuilder()
      .setUserAnswer(userAnswer)
      .setFeedback(feedback)
      .build()
  }

  internal fun submitHintRevealed(state: State, hintIsRevealed: Boolean, hintIndex: Int) {
    hintList += Hint.newBuilder()
      .setHintIsRevealed(hintIsRevealed)
      .setHintContent(state.interaction.getHint(hintIndex).hintContent)
      .build()
  }

  internal fun submitSolutionRevealed(state: State) {
    solution = Solution.newBuilder()
      .setSolutionIsRevealed(true)
      .setAnswerIsExclusive(state.interaction.solution.answerIsExclusive)
      .setCorrectAnswer(state.interaction.solution.correctAnswer)
      .setExplanation(state.interaction.solution.explanation)
      .build()
  }

  /**
   * Returns an [ExplorationCheckpoint] which contains all the latest values of variables of the
   * [StateDeck] that are used in light weight checkpointing.
   */
  internal fun createExplorationCheckpoint(
    explorationVersion: Int,
    explorationTitle: String,
    timestamp: Long
  ): ExplorationCheckpoint {
    return ExplorationCheckpoint.newBuilder().apply {
      addAllCompletedStatesInCheckpoint(
        previousStates.map { state ->
          CompletedStateInCheckpoint.newBuilder().apply {
            completedState = state.completedState
            stateName = state.state.name
          }.build()
        }
      )
      pendingStateName = pendingTopState.name
      hintIndex = revealedHintIndex
      addAllPendingUserAnswers(currentDialogInteractions)
      this.solutionIsRevealed = this@StateDeck.solutionIsRevealed
      this.stateIndex = this@StateDeck.stateIndex
      this.explorationVersion = explorationVersion
      this.explorationTitle = explorationTitle
      timestampOfFirstCheckpoint = timestamp
    }.build()
  }

  private fun getCurrentPendingState(): EphemeralState {
    return EphemeralState.newBuilder()
      .setState(pendingTopState)
      .setHasPreviousState(!isCurrentStateInitial())
      .setPendingState(
        PendingState.newBuilder().addAllWrongAnswer(currentDialogInteractions).addAllHint(hintList)
      )
      .build()
  }

  private fun getCurrentTerminalState(): EphemeralState {
    return EphemeralState.newBuilder()
      .setState(pendingTopState)
      .setHasPreviousState(!isCurrentStateInitial())
      .setTerminalState(true)
      .build()
  }

  private fun getPreviousState(): EphemeralState {
    return previousStates[stateIndex]
  }

  /** Returns whether the current scrolled State is the first State of the exploration. */
  private fun isCurrentStateInitial(): Boolean {
    return stateIndex == 0
  }

  /** Returns whether the current scrolled State is the most recent State played by the learner. */
  fun isCurrentStateTopOfDeck(): Boolean {
    return stateIndex == previousStates.size
  }

  /** Returns whether the current State is terminal. */
  private fun isCurrentStateTerminal(): Boolean {
    // Cards not on top of the deck cannot be terminal/the terminal card must be the last card in the deck, if it's
    // present.
    return isCurrentStateTopOfDeck() && isTopOfDeckTerminal()
  }

  /** Returns whether the most recent card on the deck is terminal. */
  private fun isTopOfDeckTerminal(): Boolean {
    return isTopOfDeckTerminalChecker(pendingTopState)
  }
}
