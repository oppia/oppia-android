package org.oppia.android.domain.state

import org.oppia.android.app.model.AnsweredQuestionOutcome
import org.oppia.android.app.model.Outcome
import org.oppia.android.app.model.Question
import org.oppia.android.app.model.State

/**
 * List that provides lookup access for [State]s based on a list of [Question]s and functionality for processing the
 * outcome of a submitted learner answer in the context of a question training session.
 */
class StateList(
  private var questionsList: List<Question>
) {
  /** Resets this list according to the specified list of [Question]s. */
  fun reset(questionsList: List<Question>) {
    this.questionsList = questionsList
  }

  /** Returns the first [State] taht should be played. */
  fun getFirstState(): State {
    return getState(/* questionIndex= */ 0)
  }

  /** Returns the [State] corresponding to the specified question by index. */
  fun getState(questionIndex: Int): State {
    return questionsList[questionIndex].questionState
  }

  /** Returns an [AnsweredQuestionOutcome] based on the resulting [Outcome] from the learner's answer. */
  fun computeAnswerOutcomeForResult(outcome: Outcome): AnsweredQuestionOutcome {
    return AnsweredQuestionOutcome.newBuilder()
      .setFeedback(outcome.feedback)
      .setIsCorrectAnswer(outcome.labelledAsCorrect)
      .build()
  }
}
