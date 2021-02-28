package org.oppia.android.domain.question
import org.oppia.android.app.model.Question

/**
 * Mutable data class for tracking a user's interactions with a specific question.
 */
internal data class QuestionSessionMetrics(val question: Question) {
  /** Tracks the number of answers the user submitted for this question. */
  var numberOfAnswersSubmitted: Int = 0

  /** Tracks the number of hints the user viewed for this question. */
  var numberOfHintsUsed: Int = 0

  /** Tracks whether the user viewed the solution for this question. */
  var didViewSolution: Boolean = false

  /**
   * Tracks the skill misconception IDs corresponding to the user's wrong answers for this question.
   * A skill misconception ID is a skill ID which identifies which skill the user needs to work on,
   * based on which misconception the user had when they submitted a wrong answer for a question.
   */
  var taggedSkillMisconceptionIds: MutableList<String> = mutableListOf()
}
