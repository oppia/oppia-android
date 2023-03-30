package org.oppia.android.domain.classify

import org.oppia.android.app.model.AnswerGroup
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.Outcome
import org.oppia.android.app.model.WrittenTranslationContext
import javax.inject.Inject

// TODO(#59): Restrict the visibility of this class to only other controllers.
/**
 * Controller responsible for classifying user answers to a specific outcome based on Oppia's
 * interaction rule engine. This controller is not meant to be interacted with directly by the UI.
 * Instead, UIs wanting to submit answers should do so via various progress controllers, like
 * [org.oppia.android.domain.exploration.ExplorationProgressController].
 *
 * This controller should only be interacted with via background threads.
 */
class AnswerClassificationController @Inject constructor(
  private val interactionClassifiers: Map<String, @JvmSuppressWildcards InteractionClassifier>
) {
  // Add a new classifier to the interactionClassifiers map
  init {
    interactionClassifiers["AlgebraicExpressionInput"] = AlgebraicExpressionInputClassifier()
  }
  /**
   * Classifies the specified answer in the context of the specified [Interaction] and returns the
   * [ClassificationResult] that best matches the learner's answer.
   */
  // TODO(#1580): Re-restrict access using Bazel visibilities
  fun classify(
    interaction: Interaction,
    answer: InteractionObject,
    writtenTranslationContext: WrittenTranslationContext
  ): ClassificationResult {
    val interactionClassifier = checkNotNull(
      interactionClassifiers[interaction.id]
    ) {
      "Encountered unknown interaction type: ${interaction.id}, " +
        "expected one of: ${interactionClassifiers.keys}"
    }
    return classifyAnswer(
      answer,
      interaction.answerGroupsList,
      interaction.defaultOutcome,
      interactionClassifier,
      interaction.id,
      ClassificationContext(writtenTranslationContext, interaction.customizationArgsMap)
    )
  }

  // Based on the Oppia web version:
  // https://github.com/oppia/oppia/blob/edb62f/core/templates/dev/head/pages/exploration-player-page/services/answer-classification.service.ts#L57.
  private fun classifyAnswer(
    answer: InteractionObject,
    answerGroups: List<AnswerGroup>,
    defaultOutcome: Outcome,
    interactionClassifier: InteractionClassifier,
    interactionId: String,
    classificationContext: ClassificationContext
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
          if (ruleClassifier.matches(answer, ruleSpec.inputMap, classificationContext)) {
            // Explicit classification matched.
            return if (!answerGroup.hasTaggedSkillMisconception()) {
              ClassificationResult.OutcomeOnly(answerGroup.outcome)
            } else {
              ClassificationResult.OutcomeWithMisconception(
                answerGroup.outcome,
                answerGroup.taggedSkillMisconception.skillId,
                answerGroup.taggedSkillMisconception.misconceptionId,
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
