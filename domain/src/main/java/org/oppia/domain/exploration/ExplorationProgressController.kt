package org.oppia.domain.exploration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.oppia.app.model.AnswerAndResponse
import org.oppia.app.model.AnswerOutcome
import org.oppia.app.model.CompletedState
import org.oppia.app.model.EphemeralState
import org.oppia.app.model.Exploration
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.Outcome
import org.oppia.app.model.PendingState
import org.oppia.app.model.State
import org.oppia.app.model.SubtitledHtml
import org.oppia.util.data.AsyncDataSubscriptionManager
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProviders
import javax.inject.Inject
import javax.inject.Singleton

// TODO(#186): Use an interaction repository to retrieve whether a specific ID corresponds to a terminal interaction.
private const val TERMINAL_INTERACTION_ID = "EndExploration"

private const val CURRENT_STATE_DATA_PROVIDER_ID = "CurrentStateDataProvider"

/**
 * Controller that tracks and reports the learner's ephemeral/non-persisted progress through an exploration. Note that
 * this controller only supports one active exploration at a time.
 *
 * The current exploration session is started via the exploration data controller.
 */
@Singleton
class ExplorationProgressController @Inject constructor(
  private val dataProviders: DataProviders,
  private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager,
  private val explorationRetriever: ExplorationRetriever
) {
  // TODO(#180): Add support for hints.
  // TODO(#179): Add support for parameters.
  // TODO(#181): Add support for solutions.
  // TODO(#182): Add support for refresher explorations.

  // TODO(BenHenning): Make the controller's internal data structures thread-safe as necessary.
  private lateinit var currentExplorationId: String
  private lateinit var currentExploration: Exploration
  private var playStage = PlayStage.NOT_PLAYING
  private val currentStateDataProvider =
    dataProviders.createInMemoryDataProviderAsync(CURRENT_STATE_DATA_PROVIDER_ID, this::retrieveCurrentStateAsync)
  private val stateGraph: StateGraph by lazy {
    StateGraph(
      currentExploration.statesMap
    )
  }
  private val stateDeck: StateDeck by lazy {
    StateDeck(
      stateGraph.getState(currentExploration.initStateName)
    )
  }

  /** Resets this controller to begin playing the specified [Exploration]. */
  internal fun beginExploration(explorationId: String) {
    check(playStage == PlayStage.NOT_PLAYING) {
      "Expected to finish previous exploration before starting a new one."
    }

    currentExplorationId = explorationId
    advancePlayStageTo(PlayStage.START_LOAD_EXPLORATION)
  }

  internal fun finishExploration() {
    check(playStage != PlayStage.NOT_PLAYING) {
      "Cannot finish playing an exploration that hasn't yet been started"
    }
    advancePlayStageTo(PlayStage.NOT_PLAYING)
  }

  /**
   * Submits an answer to the current state and returns how the UI should respond to this answer. The returned
   * [LiveData] will only have at most two results posted: a pending result, and then a completed success/failure
   * result. Failures in this case represent a failure of the app (possibly due to networking conditions). The app
   * should report this error in a consumable way to the user so that they may take action on it. No additional values
   * will be reported to the [LiveData]. Each call to this method returns a new, distinct, [LiveData] object that must
   * be observed. Note also that the returned [LiveData] is not guaranteed to begin with a pending state.
   *
   * If the app undergoes a configuration change, calling code should rely on the [LiveData] from [getCurrentState] to
   * know whether a current answer is pending. That [LiveData] will have its state changed to pending during answer
   * submission and until answer resolution.
   *
   * Submitting an answer may result in the learner staying in the current state, moving to a new state in the
   * exploration, being shown a concept card, or being navigated to another exploration altogether. Note that once a
   * correct answer is processed, the current state reported to [getCurrentState] will change from a pending state to a
   * completed state since the learner completed that card. The learner can then proceed from the current completed
   * state to the next pending state using [moveToNextState].
   *
   * This method cannot be called until an exploration has started and [getCurrentState] returns a non-pending result
   * or an exception will be thrown. Calling code must also take care not to allow users to submit an answer while a
   * previous answer is pending. That scenario will also result in an exception being thrown.
   *
   * It's possible for the returned [LiveData] to finish after the [LiveData] from [getCurrentState] is updated.
   */
  fun submitAnswer(answer: InteractionObject): LiveData<AsyncResult<AnswerOutcome>> {
    check(playStage != PlayStage.NOT_PLAYING) { "Cannot submit an answer if an exploration is not being played." }
    check(playStage != PlayStage.SUBMITTING_ANSWER) { "Cannot submit an answer while another answer is pending." }
    check(playStage != PlayStage.START_LOAD_EXPLORATION && playStage != PlayStage.FINISH_LOAD_EXPLORATION) {
      "Cannot submit an answer while the exploration is being loaded."
    }

    // Notify observers that the submitted answer is currently pending.
    advancePlayStageTo(PlayStage.SUBMITTING_ANSWER)
    asyncDataSubscriptionManager.notifyChangeAsync(CURRENT_STATE_DATA_PROVIDER_ID)

    // TODO(#115): Perform answer classification on a background thread.
    val topPendingState = stateDeck.getPendingTopState()
    val outcome = simulateSubmitAnswer(topPendingState, answer)
    val answerOutcome = stateGraph.computeAnswerOutcomeForResult(topPendingState, outcome)
    stateDeck.submitAnswer(answer, answerOutcome.feedback)
    if (answerOutcome.correctAnswer) {
      check(answerOutcome.destinationCase == AnswerOutcome.DestinationCase.STATE_NAME) {
        "Expecting a correct answer to only route to a new state, not to: ${answerOutcome.destinationCase}."
      }
      stateDeck.pushState(stateGraph.getState(answerOutcome.stateName))
    }

    // Return to viewing the state.
    advancePlayStageTo(PlayStage.VIEWING_STATE)
    asyncDataSubscriptionManager.notifyChangeAsync(CURRENT_STATE_DATA_PROVIDER_ID)

    return MutableLiveData(AsyncResult.success(answerOutcome))
  }

  /**
   * Navigates to the previous state in the stack. If the learner is currently on the initial state, this method will
   * throw an exception. Calling code is responsible to make sure that this method is not called when it's not possible
   * to navigate to a previous card.
   *
   * This method cannot be called until an exploration has started and [getCurrentState] returns a non-pending result or
   * an exception will be thrown.
   */
  fun moveToPreviousState() {
    check(playStage != PlayStage.NOT_PLAYING) {
      "Cannot navigate to a previous state if an exploration is not being played."
    }
    check(playStage != PlayStage.START_LOAD_EXPLORATION && playStage != PlayStage.FINISH_LOAD_EXPLORATION) {
      "Cannot navigate to a previous state if an exploration is being loaded."
    }
    check(playStage != PlayStage.SUBMITTING_ANSWER) {
      "Cannot navigate to a previous state if an answer submission is pending."
    }
    stateDeck.navigateToPreviousState()
    asyncDataSubscriptionManager.notifyChangeAsync(CURRENT_STATE_DATA_PROVIDER_ID)
  }

  /**
   * Navigates to the next state in the graph. This method is only valid if the current [EphemeralState] reported by
   * [getCurrentState] is a completed state. If the current state is pending or terminal, this function will throw an
   * exception since it's not possible for the learner to actually navigate forward. Calling code is responsible for
   * ensuring this method is only called when it's possible to navigate forward.
   *
   * Note that if the current state is a pending state, the user needs to submit a correct answer that routes to a later
   * state via [submitAnswer] in order for the current state to change to a completed state.
   *
   * This method cannot be called until an exploration has been started and [getCurrentState] returns a non-pending
   * result or an exception will be thrown.
   */
  fun moveToNextState() {
    check(playStage != PlayStage.NOT_PLAYING) {
      "Cannot navigate to a next state if an exploration is not being played."
    }
    check(playStage != PlayStage.START_LOAD_EXPLORATION && playStage != PlayStage.FINISH_LOAD_EXPLORATION) {
      "Cannot navigate to a next state if an exploration is being loaded."
    }
    check(playStage != PlayStage.SUBMITTING_ANSWER) {
      "Cannot navigate to a next state if an answer submission is pending."
    }
    stateDeck.navigateToNextState()
    asyncDataSubscriptionManager.notifyChangeAsync(CURRENT_STATE_DATA_PROVIDER_ID)
  }

  /**
   * Returns a [LiveData] monitoring the current [EphemeralState] the learner is currently viewing. If this state
   * corresponds to a a terminal state, then the learner has completed the exploration. Note that [moveToPreviousState]
   * and [moveToNextState] will automatically update observers of this live data when the next state is navigated to.
   *
   * Note that the returned [LiveData] is always the same object no matter when this method is called, except
   * potentially when a new exploration is started.
   *
   * This [LiveData] may initially be pending while the exploration object is loaded. It may also switch from a
   * completed to a pending result during transient operations like submitting an answer via [submitAnswer]. Calling
   * code should be made resilient to this by caching the current state object to display since it may disappear
   * temporarily during answer submission. Calling code should persist this state object across configuration changes if
   * needed since it cannot rely on this [LiveData] for immediate state reconstitution after configuration changes.
   *
   * The underlying state returned by this function can only be changed by calls to [moveToNextState] and
   * [moveToPreviousState], or the exploration data controller if another exploration is loaded. UI code can be
   * confident only calls from the UI layer will trigger state changes here to ensure atomicity between receiving and
   * making state changes.
   *
   * This method is safe to be called before an exploration has started. If there is no ongoing exploration, it should
   * return a pending state.
   */
  fun getCurrentState(): LiveData<AsyncResult<EphemeralState>> {
    return dataProviders.convertToLiveData(currentStateDataProvider)
  }

  private suspend fun retrieveCurrentStateAsync(): AsyncResult<EphemeralState> {
    return when (playStage) {
      PlayStage.NOT_PLAYING -> AsyncResult.pending()
      PlayStage.START_LOAD_EXPLORATION -> {
        beginLoadExploration()
        AsyncResult.pending()
      }
      PlayStage.FINISH_LOAD_EXPLORATION -> {
        try {
          finishLoadExploration()
          AsyncResult.success(stateDeck.getCurrentEphemeralState())
        } catch (e: Exception) {
          AsyncResult.failed<EphemeralState>(e)
        }
      }
      PlayStage.VIEWING_STATE -> AsyncResult.success(stateDeck.getCurrentEphemeralState())
      PlayStage.SUBMITTING_ANSWER -> AsyncResult.pending()
    }
  }

  private fun beginLoadExploration() {
    // Advance the state if an exploration is still requested to be loaded, and notifies downstream observers a change
    // is coming. This is done to notify the UI that a load is pending, then use the same data provider to actually
    // perform the load rather than posting to a background thread. Note also that the async notify is intentional here
    // since the load should be asynchronous to provide time for the UI to respond to the state update.
    if (playStage == PlayStage.START_LOAD_EXPLORATION) {
      advancePlayStageTo(PlayStage.FINISH_LOAD_EXPLORATION)
      asyncDataSubscriptionManager.notifyChangeAsync(CURRENT_STATE_DATA_PROVIDER_ID)
    }
  }

  private suspend fun finishLoadExploration() {
    // Only load the exploration if the stage hasn't changed.
    if (playStage == PlayStage.FINISH_LOAD_EXPLORATION) {
      // The exploration must be initialized first since other lazy fields depend on it being inited.
      val exploration = explorationRetriever.loadExploration(currentExplorationId)
      currentExploration = exploration
      stateGraph.resetStateGraph(exploration.statesMap)
      stateDeck.resetDeck(stateGraph.getState(exploration.initStateName))

      // Advance the stage, but do not notify observers since the current state can be reported immediately to the UI.
      advancePlayStageTo(PlayStage.VIEWING_STATE)
    }
  }

  private fun simulateSubmitAnswer(currentState: State, answer: InteractionObject): Outcome {
    return when (currentState.name) {
      TEST_INIT_STATE_NAME -> currentState.interaction.answerGroupsList.first().outcome
      TEST_MIDDLE_STATE_NAME -> simulateTextInputAnswerCheck(currentState, answer)
      else -> throw Exception("Cannot submit answer to unexpected state: ${currentState.name}.")
    }
  }

  private fun simulateTextInputAnswerCheck(currentState: State, answer: InteractionObject): Outcome {
    val expectedValue = currentState.interaction.answerGroupsList.first().ruleSpecsList.first().inputsMap.getValue("x")
    return when {
      answer.objectTypeCase != InteractionObject.ObjectTypeCase.NORMALIZED_STRING ->
        throw Exception("Expected string answer, not ${answer}.")
      answer.normalizedString.toLowerCase() == expectedValue.normalizedString.toLowerCase() ->
        currentState.interaction.answerGroupsList.first().outcome
      else -> currentState.interaction.defaultOutcome
    }
  }

  /**
   * Advances the current play stage to the specified stage, verifying that the transition is correct.
   *
   * Calling code should prevent this method from failing by checking state ahead of calling this method and providing
   * more useful errors to UI calling code since errors thrown by this method will be more obscure. This method aims to
   * ensure the internal state of the controller remains correct. This method is not meant to be covered in unit tests
   * since none of the failures here should ever be exposed to controller callers.
   */
  private fun advancePlayStageTo(nextPlayStage: PlayStage) {
    when (nextPlayStage) {
      PlayStage.NOT_PLAYING -> {
        // All transitions to NOT_PLAYING are valid except itself. Stopping playing can happen at any time.
        check(playStage != PlayStage.NOT_PLAYING) { "Cannot transition to NOT_PLAYING from NOT_PLAYING" }
        playStage = nextPlayStage
      }
      PlayStage.START_LOAD_EXPLORATION -> {
        // An exploration can only be requested to be loaded from the initial NOT_PLAYING stage.
        check(playStage == PlayStage.NOT_PLAYING) { "Cannot transition to LOADING_EXPLORATION from $playStage" }
        playStage = nextPlayStage
      }
      PlayStage.FINISH_LOAD_EXPLORATION -> {
        // An exploration can only be finished loading after being requested to be started.
        check(playStage == PlayStage.START_LOAD_EXPLORATION) {
          "Cannot transition to FINISH_LOAD_EXPLORATION from $playStage"
        }
        playStage = nextPlayStage
      }
      PlayStage.VIEWING_STATE -> {
        // A state can be viewed after loading an exploration, after viewing another state, or after submitting an
        // answer. It cannot be viewed without a loaded exploration.
        check(playStage == PlayStage.FINISH_LOAD_EXPLORATION
            || playStage == PlayStage.VIEWING_STATE
            || playStage == PlayStage.SUBMITTING_ANSWER) {
          "Cannot transition to VIEWING_STATE from $playStage"
        }
        playStage = nextPlayStage
      }
      PlayStage.SUBMITTING_ANSWER -> {
        // An answer can only be submitted after viewing a stage.
        check(playStage == PlayStage.VIEWING_STATE) { "Cannot transition to SUBMITTING_ANSWER from $playStage" }
        playStage = nextPlayStage
      }
    }
  }

  /** Different stages in which the progress controller can exist. */
  private enum class PlayStage {
    /** No exploration is currently being played. */
    NOT_PLAYING,

    /** An exploration is being prepared to be played. */
    START_LOAD_EXPLORATION,

    /**
     * An exploration is being loaded. Note that this state is to allow the UI to quickly get a pending result when a
     * load begins, but not have to wait for the load to complete. This also mitigates needing to introduce another
     * thread hop when performing the load (e.g. by posting the operation on a background dispatcher).
     */
    FINISH_LOAD_EXPLORATION,

    /** The controller is currently viewing a State. */
    VIEWING_STATE,

    /** The controller is in the process of submitting an answer. */
    SUBMITTING_ANSWER
  }

  /**
   * Graph that provides lookup access for [State]s and functionality for processing the outcome of a submitted learner
   * answer.
   */
  private class StateGraph internal constructor(private var stateGraph: Map<String, State>) {
    /** Resets this graph to the new graph represented by the specified [Map]. */
    internal fun resetStateGraph(stateGraph: Map<String, State>) {
      this.stateGraph = stateGraph
    }

    /** Returns the [State] corresponding to the specified name. */
    internal fun getState(stateName: String): State {
      return stateGraph.getValue(stateName)
    }

    /** Returns an [AnswerOutcome] based on the current state and resulting [Outcome] from the learner's answer. */
    internal fun computeAnswerOutcomeForResult(currentState: State, outcome: Outcome): AnswerOutcome {
      val answerOutcomeBuilder = AnswerOutcome.newBuilder()
        .setFeedback(outcome.feedback)
        .setCorrectAnswer(outcome.labelledAsCorrect)
      when {
        outcome.refresherExplorationId.isNotEmpty() ->
          answerOutcomeBuilder.refresherExplorationId = outcome.refresherExplorationId
        outcome.missingPrerequisiteSkillId.isNotEmpty() ->
          answerOutcomeBuilder.missingPrerequisiteSkillId = outcome.missingPrerequisiteSkillId
        outcome.destStateName == currentState.name -> answerOutcomeBuilder.setSameState(true)
        else -> answerOutcomeBuilder.stateName = outcome.destStateName
      }
      return answerOutcomeBuilder.build()
    }
  }

  private class StateDeck internal constructor(initialState: State) {
    private var pendingTopState: State = initialState
    private val previousStates: MutableList<EphemeralState> = ArrayList()
    private val currentDialogInteractions: MutableList<AnswerAndResponse> = ArrayList()
    private var stateIndex: Int = 0

    /** Resets this deck to a new, specified initial [State]. */
    internal fun resetDeck(initialState: State) {
      pendingTopState = initialState
      previousStates.clear()
      currentDialogInteractions.clear()
      stateIndex = 0
    }

    /** Navigates to the previous State in the deck, or fails if this isn't possible. */
    internal fun navigateToPreviousState() {
      check(!isCurrentStateInitial()) { "Cannot navigate to previous State; at initial state." }
      stateIndex--
    }

    /** Navigates to the next State in the deck, or fails if this isn't possible. */
    internal fun navigateToNextState() {
      check(!isCurrentStateTopOfDeck()) { "Cannot navigate to next State; at most recent State." }
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
      return when {
        stateIndex == previousStates.size -> getCurrentPendingState()
        isCurrentStateTerminal() -> getCurrentTerminalState()
        else -> getPreviousState()
      }
    }

    /**
     * Pushes a new State onto the deck. This cannot happen if the learner isn't at the most recent State, if the
     * current State is not terminal, or if the learner hasn't submitted an answer to the most recent State. This
     * operation implies that the most recently submitted answer was the correct answer to the previously current State.
     */
    internal fun pushState(state: State) {
      check(isCurrentStateTopOfDeck()) { "Cannot push a new State unless the learner is at the most recent State." }
      check(!isCurrentStateTerminal()) { "Cannot push another State after reaching a terminal State." }
      check(currentDialogInteractions.size != 0) { "Cannot push another State without an answer." }
      check(state.name != pendingTopState.name) { "Cannot route from the same State to itself as a new card." }
      previousStates += EphemeralState.newBuilder()
        .setState(pendingTopState)
        .setHasPreviousState(!isCurrentStateInitial())
        .setCompletedState(CompletedState.newBuilder().addAllAnswer(currentDialogInteractions))
        .build()
      currentDialogInteractions.clear()
      pendingTopState = state
      stateIndex++
    }

    /**
     * Submits an answer & feedback dialog the learner experience in the current State. This fails if the user is not at
     * the most recent State in the deck, or if the most recent State is terminal (since no answer can be submitted to a
     * terminal interaction).
     */
    internal fun submitAnswer(userAnswer: InteractionObject, feedback: SubtitledHtml) {
      check(isCurrentStateTopOfDeck()) { "Cannot submit an answer except to the most recent State." }
      check(!isCurrentStateTerminal()) { "Cannot submit an answer to a terminal State." }
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
    private fun isCurrentStateTopOfDeck(): Boolean {
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
      return pendingTopState.interaction.id == TERMINAL_INTERACTION_ID
    }
  }
}
