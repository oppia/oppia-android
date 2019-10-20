package org.oppia.domain.state

import org.oppia.app.model.AnswerAndResponse
import org.oppia.app.model.CompletedState
import org.oppia.app.model.EphemeralState
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.PendingState
import org.oppia.app.model.State
import org.oppia.app.model.SubtitledHtml

// TODO(#59): Hide the visibility of this class to domain implementations.

/**
 * Tracks the progress of a dynamic playing session through a graph of State cards. This class treats the learner's
 * progress like a deck of cards to simplify forward/backward navigation.
 */
internal class StateDeck internal constructor(
  initialState: State, private val isTopOfDeckTerminalChecker: (State) -> Boolean
) {
  private var pendingTopState: State = initialState
  private val previousStates: MutableList<EphemeralState> = ArrayList()
  private val currentDialogInteractions: MutableList<AnswerAndResponse> = ArrayList()
  private var stateIndex: Int = 0

  /** Returns the number of previous states. */
  internal fun getPreviousStateCount(): Int {
    return previousStates.size
  }

  /** Resets this deck to a new, specified initial [State]. */
  internal fun resetDeck(initialState: State) {
    pendingTopState = initialState
    previousStates.clear()
    currentDialogInteractions.clear()
    stateIndex = 0
  }

  /** Navigates to the previous State in the deck, or fails if this isn't possible. */
  internal fun navigateToPreviousState() {
    check(!isCurrentStateInitial()) { "Cannot navigate to previous state; at initial state." }
    stateIndex--
  }

  /** Navigates to the next State in the deck, or fails if this isn't possible. */
  internal fun navigateToNextState() {
    check(!isCurrentStateTopOfDeck()) { "Cannot navigate to next state; at most recent state." }
    stateIndex++
  }

  /**
   * Returns the [State] corresponding to the latest card in the deck, regardless of whichever State the learner is
   * currently viewing.
   */
  internal fun getPendingTopState(): State {
    return pendingTopState
  }

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
   */
  internal fun pushState(state: State) {
    check(isCurrentStateTopOfDeck()) { "Cannot push a new state unless the learner is at the most recent state." }
    check(!isCurrentStateTerminal()) { "Cannot push another state after reaching a terminal state." }
    check(currentDialogInteractions.size != 0) { "Cannot push another state without an answer." }
    check(state.name != pendingTopState.name) { "Cannot route from the same state to itself as a new card." }
    previousStates += EphemeralState.newBuilder()
      .setState(pendingTopState)
      .setHasPreviousState(!isCurrentStateInitial())
      .setCompletedState(CompletedState.newBuilder().addAllAnswer(currentDialogInteractions))
      .build()
    currentDialogInteractions.clear()
    pendingTopState = state
  }

  /**
   * Submits an answer & feedback dialog the learner experience in the current State. This fails if the user is not at
   * the most recent State in the deck, or if the most recent State is terminal (since no answer can be submitted to a
   * terminal interaction).
   */
  internal fun submitAnswer(userAnswer: InteractionObject, feedback: SubtitledHtml) {
    check(isCurrentStateTopOfDeck()) { "Cannot submit an answer except to the most recent state." }
    check(!isCurrentStateTerminal()) { "Cannot submit an answer to a terminal state." }
    currentDialogInteractions += AnswerAndResponse.newBuilder()
      .setUserAnswer(userAnswer)
      .setFeedback(feedback)
      .build()
  }

  private fun getCurrentPendingState(): EphemeralState {
    return EphemeralState.newBuilder()
      .setState(pendingTopState)
      .setHasPreviousState(!isCurrentStateInitial())
      .setPendingState(PendingState.newBuilder().addAllWrongAnswer(currentDialogInteractions))
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
