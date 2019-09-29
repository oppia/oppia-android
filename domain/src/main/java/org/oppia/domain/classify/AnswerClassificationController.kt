package org.oppia.domain.classify

import org.oppia.app.model.Interaction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.Outcome
import javax.inject.Inject

// TODO(#59): Restrict the visibility of this class to only other controllers.
/**
 * Controller responsible for classifying user answers to a specific outcome based on Oppia's interaction rule engine.
 * This controller is not meant to be interacted with directly by the UI. Instead, UIs wanting to submit answers should
 * do so via various progress controllers, like [StoryProgressController].
 *
 * This controller should only be interacted with via background threads.
 */
class AnswerClassificationController @Inject constructor() {
  /**
   * Classifies the specified answer in the context of the specified [Interaction] and returns the [Outcome] that best
   * matches the learner's answer.
   */
  internal fun classify(interaction: Interaction, answer: InteractionObject): Outcome {
    // Assume only the default outcome is returned currently since this stubbed implementation is not actually used by
    // downstream stubbed progress controllers.
    return interaction.defaultOutcome
  }
}
