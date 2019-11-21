package org.oppia.app.player.state.itemviewmodel

import org.oppia.app.model.InteractionObject
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.domain.util.toAnswerString

class NumericInputViewModel(
  existingAnswer: InteractionObject?, val isReadOnly: Boolean
): StateItemViewModel(ViewType.NUMERIC_INPUT_INTERACTION), InteractionAnswerHandler {
  var answerText: CharSequence = existingAnswer?.toAnswerString() ?: ""

  override fun getPendingAnswer(): InteractionObject {
    val interactionObjectBuilder = InteractionObject.newBuilder()
    if (answerText.isNotEmpty()) {
      interactionObjectBuilder.real = answerText.toString().toDouble()
    }
    return interactionObjectBuilder.build()
  }
}
