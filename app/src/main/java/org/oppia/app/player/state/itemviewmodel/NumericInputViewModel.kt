package org.oppia.app.player.state.itemviewmodel

import androidx.lifecycle.ViewModel
import org.oppia.app.model.InteractionObject
import org.oppia.app.player.state.listener.InteractionAnswerRetriever

class NumericInputViewModel: ViewModel(), InteractionAnswerRetriever {
  var answerText: CharSequence = ""

  override fun getPendingAnswer(): InteractionObject {
    val interactionObjectBuilder = InteractionObject.newBuilder()
    if (answerText.isNotEmpty()) {
      interactionObjectBuilder.real = answerText.toString().toDouble()
    }
    return interactionObjectBuilder.build()
  }
}
