package org.oppia.domain.classify

import org.oppia.app.model.AnswerGroup
import org.oppia.app.model.Interaction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.Outcome
import org.oppia.app.model.State
import javax.inject.Inject

// TODO(#59): Restrict the visibility of this class to only other controllers.
/**
 * Controller responsible for classifying user answers to a specific outcome based on Oppia's interaction rule engine.
 * This controller is not meant to be interacted with directly by the UI. Instead, UIs wanting to submit answers should
 * do so via various progress controllers, like [org.oppia.domain.topic.StoryProgressController].
 *
 * This controller should only be interacted with via background threads.
 */
class AnswerClassificationController @Inject constructor(
  private val interactionClassifiers: Map<String, @JvmSuppressWildcards InteractionClassifier>
) {
  // TODO(#114): Add support for classifying answers based on an actual exploration. Also, classify() should take an
  // Interaction, not a State.

  // + TextInput
  //   + TextInputEqualsRuleClassifierProvider
  //   + TextInputCaseSensitiveEqualsRuleClassifierProvider
  //   + TextInputStartsWithRuleClassifierProvider
  //   + TextInputContainsRuleClassifierProvider
  //   + TextInputFuzzyEqualsRuleClassifierProvider
  // + NumericInput
  //   + NumericInputEqualsRuleClassifierProvider
  //   + NumericInputIsLessThanRuleClassifierProvider
  //   + NumericInputIsGreaterThanRuleClassifierProvider
  //   + NumericInputIsLessThanOrEqualToRuleClassifierProvider
  //   + NumericInputIsGreaterThanOrEqualToRuleClassifierProvider
  //   + NumericInputIsInclusivelyBetweenRuleClassifierProvider
  //   + NumericInputIsWithinToleranceRuleClassifierProvider
  // + NumberWithUnits
  //   + NumberWithUnitsIsEqualToRuleClassifierProvider
  //   + NumberWithUnitsIsEquivalentToRuleClassifierProvider
  // + MultipleChoiceInput
  //   + MultipleChoiceInputEqualsRuleClassifierProvider
  // + ItemSelectionInput
  //   + ItemSelectionInputEqualsRuleClassifierProvider
  //   + ItemSelectionInputContainsAtLeastOneOfRuleClassifierProvider
  //   + ItemSelectionInputDoesNotContainAtLeastOneOfRuleClassifierProvider
  //   + ItemSelectionInputIsProperSubsetOfRuleClassifierProvider
  // - FractionInput
  //   - FractionInputIsExactlyEqualToRuleClassifierProvider
  //   - FractionInputIsEquivalentToRuleClassifierProvider
  //   - FractionInputIsEquivalentToAndInSimplestFormRuleClassifierProvider
  //   - FractionInputIsLessThanRuleClassifierProvider
  //   - FractionInputIsGreaterThanRuleClassifierProvider
  //   - FractionInputHasNumeratorEqualToRuleClassifierProvider
  //   - FractionInputHasDenominatorEqualToRuleClassifierProvider
  //   - FractionInputHasIntegerPartEqualToRuleClassifierProvider
  //   - FractionInputHasNoFractionalPartRuleClassifierProvider
  //   - FractionInputHasFractionalPartExactlyEqualToRuleClassifierProvider
  // - Continue
  // - EndExploration

  /**
   * Classifies the specified answer in the context of the specified [Interaction] and returns the [Outcome] that best
   * matches the learner's answer.
   */
  internal fun classify(currentState: State, answer: InteractionObject): Outcome {
    val interaction = currentState.interaction
    val interactionClassifier = checkNotNull(interactionClassifiers[interaction.id]) {
      "Encountered unknown interaction type: ${interaction.id}, expected one of: ${interactionClassifiers.keys}"
    }
    // TODO(#207): Add support for additional classification types.
    return classifyAnswer(
      answer, interaction.answerGroupsList, interaction.defaultOutcome, interactionClassifier, interaction.id)
  }

  // Based on the Oppia web version:
  // https://github.com/oppia/oppia/blob/edb62f/core/templates/dev/head/pages/exploration-player-page/services/answer-classification.service.ts#L57.
  private fun classifyAnswer(
    answer: InteractionObject, answerGroups: List<AnswerGroup>, defaultOutcome: Outcome,
    interactionClassifier: InteractionClassifier, interactionId: String
  ): Outcome {
    for (answerGroup in answerGroups) {
      for (ruleSpec in answerGroup.ruleSpecsList) {
        val ruleClassifier = checkNotNull(interactionClassifier.getRuleClassifier(ruleSpec.ruleType)) {
          "Expected interaction $interactionId to have classifier for rule type: ${ruleSpec.ruleType}"
        }
        if (ruleClassifier.matches(answer, ruleSpec.inputMap)) {
          // Explicit classification matched.
          return answerGroup.outcome
        }
      }
    }

    // Default outcome classification.
    return defaultOutcome
  }

  private fun simulateMultipleChoiceForWelcomeStateExp5(currentState: State, answer: InteractionObject): Outcome {
    return when {
      answer.objectTypeCase != InteractionObject.ObjectTypeCase.NON_NEGATIVE_INT ->
        throw Exception("Expected non-negative int answer, not $answer.")
      answer.nonNegativeInt == 0 -> currentState.interaction.answerGroupsList[0].outcome
      answer.nonNegativeInt == 2 -> currentState.interaction.answerGroupsList[1].outcome
      else -> currentState.interaction.defaultOutcome
    }
  }

  private fun simulateTextInputForWhatLanguageStateExp5(currentState: State, answer: InteractionObject): Outcome {
    return when {
      answer.objectTypeCase != InteractionObject.ObjectTypeCase.NORMALIZED_STRING ->
        throw Exception("Expected string answer, not $answer.")
      answer.normalizedString.toLowerCase() == "finnish" -> currentState.interaction.getAnswerGroups(6).outcome
      else -> currentState.interaction.defaultOutcome
    }
  }

  private fun simulateNumericInputForNumericInputStateExp5(currentState: State, answer: InteractionObject): Outcome {
    return when {
      answer.objectTypeCase != InteractionObject.ObjectTypeCase.SIGNED_INT ->
        throw Exception("Expected signed int answer, not $answer.")
      answer.signedInt == 121 -> currentState.interaction.answerGroupsList.first().outcome
      else -> currentState.interaction.defaultOutcome
    }
  }

  private fun simulateMultipleChoiceForWelcomeStateExp6(currentState: State, answer: InteractionObject): Outcome {
    return when {
      answer.objectTypeCase != InteractionObject.ObjectTypeCase.NON_NEGATIVE_INT ->
        throw Exception("Expected non-negative int answer, not $answer.")
      answer.nonNegativeInt == 3 -> currentState.interaction.answerGroupsList[1].outcome
      answer.nonNegativeInt == 0 -> currentState.interaction.answerGroupsList[2].outcome
      else -> currentState.interaction.defaultOutcome
    }
  }
}
