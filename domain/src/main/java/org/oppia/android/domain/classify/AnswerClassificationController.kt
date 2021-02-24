package org.oppia.android.domain.classify

import org.oppia.android.app.model.AnswerGroup
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.Outcome
import javax.inject.Inject

// TODO(#59): Restrict the visibility of this class to only other controllers.
/**
 * Controller responsible for classifying user answers to a specific outcome based on Oppia's interaction rule engine.
 * This controller is not meant to be interacted with directly by the UI. Instead, UIs wanting to submit answers should
 * do so via various progress controllers, like [org.oppia.android.domain.topic.StoryProgressController].
 *
 * This controller should only be interacted with via background threads.
 */
class AnswerClassificationController @Inject constructor(
  private val interactionClassifiers: Map<String, @JvmSuppressWildcards InteractionClassifier>
) {
  /**
   * Classifies the specified answer in the context of the specified [Interaction] and returns the [ClassificationResult] that best
   * matches the learner's answer.
   */
  // TODO(#1580): Re-restrict access using Bazel visibilities
  fun classify(interaction: Interaction, answer: InteractionObject): ClassificationResult {
    val interactionClassifier = checkNotNull(
      interactionClassifiers[interaction.id]
    ) {
      "Encountered unknown interaction type: ${interaction.id}, " +
        "expected one of: ${interactionClassifiers.keys}"
    }
    // TODO(#207): Add support for additional classification types.
    return classifyAnswer(
      answer,
      interaction.answerGroupsList,
      interaction.defaultOutcome,
      interactionClassifier,
      interaction.id
    )
  }

  // Based on the Oppia web version:
  // https://github.com/oppia/oppia/blob/edb62f/core/templates/dev/head/pages/exploration-player-page/services/answer-classification.service.ts#L57.
  private fun classifyAnswer(
    answer: InteractionObject,
    answerGroups: List<AnswerGroup>,
    defaultOutcome: Outcome,
    interactionClassifier: InteractionClassifier,
    interactionId: String
  ): ClassificationResult {
    for (answerGroup in answerGroups) {
      for (ruleSpec in answerGroup.ruleSpecsList) {
        val ruleClassifier =
          checkNotNull(interactionClassifier.getRuleClassifier(ruleSpec.ruleType)) {
            "Expected interaction $interactionId to have classifier for " +
              "rule type: ${ruleSpec.ruleType}, but only" +
              " has: ${interactionClassifier.getRuleTypes()}"
          }
        try {
          if (ruleClassifier.matches(answer, ruleSpec.inputMap)) {
            // Explicit classification matched.
            return if (answerGroup.taggedSkillMisconceptionId.isEmpty()) {
              ClassificationResult.OutcomeOnly(answerGroup.outcome)
            } else {
              ClassificationResult.OutcomeWithMisconception(
                answerGroup.outcome,
                answerGroup.taggedSkillMisconceptionId
              )
            }
          }
        } catch (e: Exception) {
          throw IllegalStateException(
            "Failed when classifying answer $answer for interaction $interactionId",
            e
          )
        }
      }
    }
    // Answer group with default outcome classification.
    return ClassificationResult.OutcomeOnly(defaultOutcome)
  }
}
