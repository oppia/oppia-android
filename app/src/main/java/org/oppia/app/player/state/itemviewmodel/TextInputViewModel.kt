package org.oppia.app.player.state.itemviewmodel

import org.oppia.app.model.Interaction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.UserAnswer
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler

/** [StateItemViewModel] for the text input interaction. */
class TextInputViewModel(
  interaction: Interaction
) : StateItemViewModel(ViewType.TEXT_INPUT_INTERACTION), InteractionAnswerHandler {
  var answerText: CharSequence = ""
  val hintText: CharSequence = deriveHintText(interaction)

  override fun getPendingAnswer(): UserAnswer {
    val userAnswerBuilder = UserAnswer.newBuilder()
    if (answerText.isNotEmpty()) {
      val answerTextString = answerText.toString()
      userAnswerBuilder.answer = InteractionObject.newBuilder().setNormalizedString(answerTextString).build()
      userAnswerBuilder.plainAnswer = answerTextString
    }
    return userAnswerBuilder.build()
  }

  private fun deriveHintText(interaction: Interaction): CharSequence {
    // The default placeholder for text input is empty.
    return interaction.customizationArgsMap["placeholder"]?.normalizedString ?: ""
  }
}
