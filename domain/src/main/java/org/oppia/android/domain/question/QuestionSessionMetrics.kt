package org.oppia.android.domain.question
import org.oppia.android.app.model.Question

/**
 * Mutable data class for tracking a user's interactions with a specific question.
 */
data class QuestionSessionMetrics(val question: Question) {
  /** Tracks the number of answers the user submitted for this question. */
  var numberOfAnswersSubmitted: Int = 0

  /** Tracks the number of hints the user viewed for this question. */
  var numberOfHintsUsed: Int = 0

  /** Tracks whether the user viewed the solution for this question. */
  var didViewSolution: Boolean = false

  /**
   * Tracks the skill IDs corresponding to the misconceptions of the user's wrong answers for this
   * question. These skill IDs identify which skills the user needs to work on, based on which
   * misconceptions the user had when they submitted wrong answers for this question.
   */
  var taggedMisconceptionSkillIds: MutableList<String> = mutableListOf()
}
