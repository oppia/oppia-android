package org.oppia.android.domain.survey

import org.oppia.android.app.model.EphemeralSurveyQuestion
import org.oppia.android.app.model.SurveyQuestion

/**
 * Tracks the dynamic behavior of the user through a survey session. This class
 * treats the survey progress like a deck of cards to simplify forward/backward navigation.
 */
class SurveyQuestionDeck constructor(
  private val totalQuestionCount: Int,
  initialQuestion: SurveyQuestion,
  private val isTopOfDeckTerminalChecker: (SurveyQuestion) -> Boolean
) {
  private var pendingTopQuestion = initialQuestion
  private var viewedQuestionsCount: Int = 0
  private var questionIndex: Int = 0

  /** Sets this deck to a specific question. */
  fun updateDeck(pendingTopQuestion: SurveyQuestion) {
    this.pendingTopQuestion = pendingTopQuestion
  }

  /** Navigates to the previous question in the deck or fails if it is not possible. */
  fun navigateToPreviousQuestion() {
    check(!isCurrentQuestionInitial()) {
      "Cannot navigate to previous question; at initial question."
    }
    questionIndex--
  }

  /** Navigates to the next question in the deck or fails if it is not possible. */
  fun navigateToNextQuestion() {
    check(!isCurrentQuestionTerminal()) {
      "Cannot navigate to next question; at terminal question."
    }
    questionIndex++
    viewedQuestionsCount++
  }

  /** Returns the index of the current selected question of the deck. */
  fun getTopQuestionIndex(): Int = questionIndex

  /** Returns whether this is the first question in the survey. */
  private fun isCurrentQuestionInitial(): Boolean {
    return questionIndex == 0
  }

  /** Returns the current [EphemeralSurveyQuestion] the learner is viewing. */
  fun getCurrentEphemeralQuestion(): EphemeralSurveyQuestion {
    return if (isCurrentQuestionTerminal()) {
      getCurrentTerminalQuestion()
    } else {
      getCurrentPendingQuestion()
    }
  }

  private fun getCurrentPendingQuestion(): EphemeralSurveyQuestion {
    return EphemeralSurveyQuestion.newBuilder()
      .setHasPreviousQuestion(!isCurrentQuestionInitial())
      .setHasNextQuestion(!isCurrentQuestionTerminal())
      .setQuestion(pendingTopQuestion)
      .setPendingQuestion(true)
      .setCurrentQuestionIndex(questionIndex)
      .setTotalQuestionCount(totalQuestionCount)
      .build()
  }

  private fun getCurrentTerminalQuestion(): EphemeralSurveyQuestion {
    return EphemeralSurveyQuestion.newBuilder()
      .setHasPreviousQuestion(!isCurrentQuestionInitial())
      .setHasNextQuestion(false)
      .setQuestion(pendingTopQuestion)
      .setTerminalQuestion(true)
      .setCurrentQuestionIndex(questionIndex)
      .setTotalQuestionCount(totalQuestionCount)
      .build()
  }

  /** Returns whether this is the most recent question in the survey. */
  fun isCurrentQuestionTopOfDeck(): Boolean {
    return questionIndex == viewedQuestionsCount
  }

  /** Returns whether this is the last question in the survey. */
  fun isCurrentQuestionTerminal(): Boolean {
    return isCurrentQuestionTopOfDeck() && isTopOfDeckTerminal()
  }

  /** Returns whether the most recent card on the deck is terminal. */
  private fun isTopOfDeckTerminal(): Boolean {
    return isTopOfDeckTerminalChecker(pendingTopQuestion)
  }
}
