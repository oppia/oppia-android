package org.oppia.android.domain.survey

/**
 * Tracks the dynamic behavior of the user through a survey session. This class
 * treats the survey progress like a deck of cards to simplify forward/backward navigation.
 */
class SurveyQuestionDeck constructor(
  private val totalQuestionCount: Int,
  private val isTopOfDeckTerminalChecker: (Int, Int) -> Boolean
) {
  private val viewedQuestionsCount: Int = 0
  private var questionIndex: Int = 0

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
      "Cannot navigate to next question; at terminal question"
    }
    questionIndex++
  }

  /** Returns the index of the current selected question of the deck. */
  fun getTopQuestionIndex(): Int = questionIndex

  /** Returns whether this is the first question in the survey. */
  private fun isCurrentQuestionInitial(): Boolean {
    return questionIndex == 0
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
    return isTopOfDeckTerminalChecker(questionIndex, totalQuestionCount)
  }
}
