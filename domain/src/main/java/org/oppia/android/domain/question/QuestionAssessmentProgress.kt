package org.oppia.domain.question

import org.oppia.app.model.Question
import org.oppia.app.model.State
import org.oppia.domain.state.StateDeck
import org.oppia.domain.state.StateList

/**
 * Private class that encapsulates the mutable state of a question progress controller. This class is not thread-safe,
 * so owning classes should ensure synchronized access. This class can exist across multiple training session instances,
 * but calling code is responsible for ensuring it is properly reset.
 */
internal class QuestionAssessmentProgress {
  internal var trainStage = TrainStage.NOT_IN_TRAINING_SESSION
  private var questionsList: List<Question> = listOf()
  internal val stateList: StateList by lazy { StateList(questionsList) }
  internal val stateDeck: StateDeck by lazy {
    StateDeck(
      stateList.getFirstState(),
      this::isTopStateTerminal
    )
  }
  private var isTopQuestionCompleted: Boolean = false

  /** Initialize the assessment with the specified list of questions. */
  internal fun initialize(questionsList: List<Question>) {
    advancePlayStageTo(TrainStage.VIEWING_STATE)
    this.questionsList = questionsList
    stateList.reset(questionsList)
    stateDeck.resetDeck(stateList.getFirstState())
    isTopQuestionCompleted = false
  }

  /** Processes when the current question card has just been completed. */
  internal fun completeCurrentQuestion() {
    isTopQuestionCompleted = true
  }

  /** Processes when a new pending question card has been navigated to. */
  internal fun processNavigationToNewQuestion() {
    isTopQuestionCompleted = false
  }

  /**
   * Advances the current play stage to the specified stage, verifying that the transition is correct.
   *
   * Calling code should prevent this method from failing by checking state ahead of calling this method and providing
   * more useful errors to UI calling code since errors thrown by this method will be more obscure. This method aims to
   * ensure the internal state of the controller remains correct. This method is not meant to be covered in unit tests
   * since none of the failures here should ever be exposed to controller callers.
   */
  internal fun advancePlayStageTo(nextTrainStage: TrainStage) {
    when (nextTrainStage) {
      TrainStage.NOT_IN_TRAINING_SESSION -> {
        // All transitions to NOT_IN_TRAINING_SESSION are valid except itself. Stopping playing can happen at any time.
        check(trainStage != TrainStage.NOT_IN_TRAINING_SESSION) {
          "Cannot transition to NOT_IN_TRAINING_SESSION from NOT_IN_TRAINING_SESSION"
        }
        trainStage = nextTrainStage
      }
      TrainStage.LOADING_TRAINING_SESSION -> {
        // A session can only begun being loaded when not previously in a training session.
        check(trainStage == TrainStage.NOT_IN_TRAINING_SESSION) {
          "Cannot transition to LOADING_TRAINING_SESSION from $trainStage"
        }
        trainStage = nextTrainStage
      }
      TrainStage.VIEWING_STATE -> {
        // A state can be viewed after loading a training session, after viewing another state, or after submitting an
        // answer. It cannot be viewed without a loaded session.
        check(
          trainStage == TrainStage.LOADING_TRAINING_SESSION ||
            trainStage == TrainStage.VIEWING_STATE ||
            trainStage == TrainStage.SUBMITTING_ANSWER
        ) {
          "Cannot transition to VIEWING_STATE from $trainStage"
        }
        trainStage = nextTrainStage
      }
      TrainStage.SUBMITTING_ANSWER -> {
        // An answer can only be submitted after viewing a stage.
        check(trainStage == TrainStage.VIEWING_STATE) {
          "Cannot transition to SUBMITTING_ANSWER from $trainStage"
        }
        trainStage = nextTrainStage
      }
    }
  }

  /** Returns whether the learner has completed the assessment. */
  internal fun isAssessmentCompleted(): Boolean {
    return getCurrentQuestionIndex() == getTotalQuestionCount() - 1 && isTopQuestionCompleted
  }

  /** Returns the index of the current question being played. */
  internal fun getCurrentQuestionIndex(): Int {
    return stateDeck.getTopStateIndex()
  }

  /** Returns the next [State] that should be played. */
  internal fun getNextState(): State {
    return stateList.getState(getCurrentQuestionIndex() + 1)
  }

  /** Returns whether the learner is currently viewing the most recent question card. */
  internal fun isViewingMostRecentQuestion(): Boolean {
    return stateDeck.isCurrentStateTopOfDeck()
  }

  /** Returns the number of questions in the assessment. */
  internal fun getTotalQuestionCount(): Int {
    return questionsList.size
  }

  private fun isTopStateTerminal(@Suppress("UNUSED_PARAMETER") state: State): Boolean {
    // There's a synthetic card at the end of the assessment to represent the terminal state.
    return stateDeck.isCurrentStateTopOfDeck() &&
      getCurrentQuestionIndex() == getTotalQuestionCount()
  }

  /** Different stages in which the progress controller can exist. */
  enum class TrainStage {
    /** No session is currently being played. */
    NOT_IN_TRAINING_SESSION,

    /** A training session is currently waiting to be loaded. */
    LOADING_TRAINING_SESSION,

    /** The controller is currently viewing a State. */
    VIEWING_STATE,

    /** The controller is in the process of submitting an answer. */
    SUBMITTING_ANSWER
  }
}
