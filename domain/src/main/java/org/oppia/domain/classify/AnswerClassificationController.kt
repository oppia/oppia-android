package org.oppia.domain.classify

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
class AnswerClassificationController @Inject constructor() {
  // TODO(#114): Add support for classifying answers based on an actual exploration. Also, classify() should take an
  // Interaction, not a State.

  /**
   * Classifies the specified answer in the context of the specified [Interaction] and returns the [Outcome] that best
   * matches the learner's answer.
   */
  internal fun classify(currentState: State, answer: InteractionObject): Outcome {
    return when (currentState.name) {
      "Welcome!" -> simulateMultipleChoiceForWelcomeState(currentState, answer)
      "What language" -> simulateTextInputForWhatLanguageState(currentState, answer)
      "Numeric input" -> simulateNumericInputForNumericInputState(currentState, answer)
      "Things you can do" -> currentState.interaction.defaultOutcome
      else -> throw Exception("Cannot submit answer to unexpected state: ${currentState.name}.")
    }
  }

  private fun simulateMultipleChoiceForWelcomeState(currentState: State, answer: InteractionObject): Outcome {
    return when {
      answer.objectTypeCase != InteractionObject.ObjectTypeCase.NON_NEGATIVE_INT ->
        throw Exception("Expected int answer, not $answer.")
      answer.nonNegativeInt == 0 -> currentState.interaction.answerGroupsList.first().outcome
      else -> currentState.interaction.defaultOutcome
    }
  }

  private fun simulateTextInputForWhatLanguageState(currentState: State, answer: InteractionObject): Outcome {
    return when {
      answer.objectTypeCase != InteractionObject.ObjectTypeCase.NORMALIZED_STRING ->
        throw Exception("Expected string answer, not $answer.")
      answer.normalizedString.toLowerCase() == "finnish" -> currentState.interaction.getAnswerGroups(7).outcome
      else -> currentState.interaction.defaultOutcome
    }
  }

  private fun simulateNumericInputForNumericInputState(currentState: State, answer: InteractionObject): Outcome {
    return when {
      answer.objectTypeCase != InteractionObject.ObjectTypeCase.NON_NEGATIVE_INT ->
        throw Exception("Expected int answer, not $answer.")
      answer.nonNegativeInt == 121 -> currentState.interaction.answerGroupsList.first().outcome
      else -> currentState.interaction.defaultOutcome
    }
  }
}
