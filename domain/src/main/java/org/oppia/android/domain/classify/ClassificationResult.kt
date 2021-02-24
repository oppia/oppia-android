package org.oppia.android.domain.classify

import org.oppia.android.app.model.Outcome

/**
 * Data class containing the result of classifying a user's answer based on Oppia's interaction rule engine.
 * This will always return the classification outcome at minimum. If the answer also has a tagged skill
 * misconception ID, it will be returned alongside the outcome.
 */
sealed class ClassificationResult {
  abstract val outcome: Outcome

  /** This is used in the scenario that the classification result has no tagged skill misconception ID. */
  data class OutcomeOnly(override val outcome: Outcome) : ClassificationResult()

  /** This is used in the scenario that the classification result has a tagged skill misconception ID. */
  data class OutcomeWithMisconception(
    override val outcome: Outcome,
    val taggedSkillMisconceptionId: String
  ) : ClassificationResult()
}
