package org.oppia.app.player.state.itemviewmodel

import org.oppia.app.model.InteractionObject
import org.oppia.app.model.UserAnswer
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.app.player.state.answerhandling.InteractionAnswerReceiver

// For context:
// https://github.com/oppia/oppia/blob/37285a/extensions/interactions/Continue/directives/oppia-interactive-continue.directive.ts
private const val DEFAULT_CONTINUE_INTERACTION_TEXT_ANSWER = "Please continue."

/** [ViewModel] for the 'Continue' button. */
class ContinueInteractionViewModel(
  private val interactionAnswerReceiver: InteractionAnswerReceiver
) : StateItemViewModel(ViewType.CONTINUE_INTERACTION), InteractionAnswerHandler {

  override fun isExplicitAnswerSubmissionRequired(): Boolean = false

  override fun getPendingAnswer(): UserAnswer {
    return UserAnswer.newBuilder()
      .setAnswer(InteractionObject.newBuilder().setNormalizedString(DEFAULT_CONTINUE_INTERACTION_TEXT_ANSWER))
      .setPlainAnswer(DEFAULT_CONTINUE_INTERACTION_TEXT_ANSWER)
      .build()
  }

  fun handleButtonClicked() {
    interactionAnswerReceiver.onAnswerReadyForSubmission(getPendingAnswer())
  }
}
