package org.oppia.app.player.state.itemviewmodel

import org.oppia.app.model.InteractionObject
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.domain.util.toAnswerString

// For context:
// https://github.com/oppia/oppia/blob/37285a/extensions/interactions/Continue/directives/oppia-interactive-continue.directive.ts
private const val DEFAULT_CONTINUE_INTERACTION_TEXT_ANSWER = "Please continue."

/** [ViewModel] for the 'Continue' button. */
class ContinueInteractionViewModel(
  private val interactionAnswerReceiver: InteractionAnswerReceiver, existingAnswer: InteractionObject?,
  val isReadOnly: Boolean
): StateItemViewModel(), InteractionAnswerHandler {
  val answerText: CharSequence = existingAnswer?.toAnswerString() ?: ""

  override fun isExplicitAnswerSubmissionRequired(): Boolean = false

  override fun getPendingAnswer(): InteractionObject {
    return InteractionObject.newBuilder().setNormalizedString(DEFAULT_CONTINUE_INTERACTION_TEXT_ANSWER).build()
  }

  fun handleButtonClicked() {
    interactionAnswerReceiver.onAnswerReadyForSubmission(getPendingAnswer())
  }
}
