package org.oppia.app.player.state.itemviewmodel

import androidx.lifecycle.ViewModel
import org.oppia.app.model.InteractionObject
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.domain.util.toAnswerString

class NumericInputViewModel(
  existingAnswer: InteractionObject?, val isReadOnly: Boolean
): ViewModel(), InteractionAnswerHandler {
  var answerText: CharSequence = existingAnswer?.toAnswerString() ?: ""

  override fun getPendingAnswer(): InteractionObject {
    val interactionObjectBuilder = InteractionObject.newBuilder()
    if (answerText.isNotEmpty()) {
      interactionObjectBuilder.real = answerText.toString().toDouble()
    }
    return interactionObjectBuilder.build()
  }
}
