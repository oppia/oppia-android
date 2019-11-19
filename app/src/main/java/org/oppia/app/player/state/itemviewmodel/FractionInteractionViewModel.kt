package org.oppia.app.player.state.itemviewmodel

import org.oppia.app.model.InteractionObject
import org.oppia.app.parser.StringToFractionParser
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.domain.util.toAnswerString

class FractionInteractionViewModel(
  existingAnswer: InteractionObject?, val isReadOnly: Boolean
) : StateItemViewModel(), InteractionAnswerHandler {
  var answerText: CharSequence = existingAnswer?.toAnswerString() ?: ""

  override fun getPendingAnswer(): InteractionObject {
    val interactionObjectBuilder = InteractionObject.newBuilder()
    if (answerText.isNotEmpty()) {
      interactionObjectBuilder.fraction = StringToFractionParser().getFractionFromString(answerText.toString())
    }
    return interactionObjectBuilder.build()
  }

  fun onTextChanged(text: CharSequence) {
  }
}
