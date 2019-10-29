package org.oppia.app.player.state.itemviewmodel

import androidx.lifecycle.ViewModel
import org.oppia.app.model.InteractionObject
import org.oppia.app.player.state.listener.InteractionAnswerRetriever
import org.oppia.domain.util.toAnswerString

class TextInputViewModel(
  existingAnswer: InteractionObject?, val isReadOnly: Boolean
): ViewModel(), InteractionAnswerRetriever {
  var answerText: CharSequence = existingAnswer?.toAnswerString() ?: ""

  override fun getPendingAnswer(): InteractionObject {
    val interactionObjectBuilder = InteractionObject.newBuilder()
    if (answerText.isNotEmpty()) {
      interactionObjectBuilder.normalizedString = answerText.toString()
    }
    return interactionObjectBuilder.build()
  }
}
