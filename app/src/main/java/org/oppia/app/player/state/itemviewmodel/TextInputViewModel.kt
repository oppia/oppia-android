package org.oppia.app.player.state.itemviewmodel

import org.oppia.app.model.Interaction
import org.oppia.app.model.InteractionObject
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.domain.util.toAnswerString

/** View model for text input interaction views. */
class TextInputViewModel(
  interaction: Interaction, existingAnswer: InteractionObject?, val isReadOnly: Boolean
): StateItemViewModel(ViewType.TEXT_INPUT_INTERACTION), InteractionAnswerHandler {
  var answerText: CharSequence = existingAnswer?.toAnswerString() ?: ""
  val hintText: CharSequence = deriveHintText(interaction)

  override fun getPendingAnswer(): InteractionObject {
    val interactionObjectBuilder = InteractionObject.newBuilder()
    if (answerText.isNotEmpty()) {
      interactionObjectBuilder.normalizedString = answerText.toString()
    }
    return interactionObjectBuilder.build()
  }

  private fun deriveHintText(interaction: Interaction): CharSequence {
    // The default placeholder for text input is empty.
    return interaction.customizationArgsMap["placeholder"]?.normalizedString ?: ""
  }
}
