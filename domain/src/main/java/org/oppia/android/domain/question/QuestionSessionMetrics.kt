package org.oppia.android.domain.question
import org.oppia.android.app.model.Question

/**
 * Mutable data class for tracking a user's interactions with a specific question. This includes
 * the number of answers they submitted, the number of hints they used, whether they viewed the
 * solution, and the skill misconception ids related to their wrong answers.
 */
data class QuestionSessionMetrics(val question: Question) {
  var numberOfAnswersSubmitted: Int = 0
  var numberOfHintsUsed: Int = 0
  var didViewSolution: Boolean = false
  var taggedSkillMisconceptionIds: MutableList<String> = mutableListOf()
}
