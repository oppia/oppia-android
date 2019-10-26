package org.oppia.app.player.state.itemviewmodel

import androidx.lifecycle.ViewModel
import org.oppia.app.model.InteractionObject
import org.oppia.app.player.state.listener.InteractionAnswerRetriever

class TextInputViewModel: ViewModel(), InteractionAnswerRetriever {
  var answerText: CharSequence = ""

  override fun getPendingAnswer(): InteractionObject {
    val interactionObjectBuilder = InteractionObject.newBuilder()
    if (answerText.isNotEmpty()) {
      interactionObjectBuilder.normalizedString = answerText.toString()
    }
    return interactionObjectBuilder.build()
  }
}