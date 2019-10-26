package org.oppia.app.player.state.itemviewmodel

import androidx.lifecycle.ViewModel
import org.oppia.app.model.InteractionObject
import org.oppia.app.parser.StringToFractionParser
import org.oppia.app.player.state.listener.InteractionAnswerRetriever

class FractionInteractionViewModel: ViewModel(), InteractionAnswerRetriever {
  private var interactionObject: InteractionObject = InteractionObject.getDefaultInstance()

  var answerText: CharSequence = ""

  override fun getPendingAnswer(): InteractionObject {
    val interactionObjectBuilder = InteractionObject.newBuilder()
    if (answerText.isNotEmpty()) {
      interactionObjectBuilder.fraction = StringToFractionParser().getFractionFromString(answerText.toString())
    }
    return interactionObjectBuilder.build()
  }
}
