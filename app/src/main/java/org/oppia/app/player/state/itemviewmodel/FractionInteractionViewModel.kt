package org.oppia.app.player.state.itemviewmodel

import android.util.Log
import org.oppia.app.model.InteractionObject
import org.oppia.app.parser.StringToFractionParser
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.app.topic.FractionParsingErrors
import org.oppia.domain.util.toAnswerString

class FractionInteractionViewModel(
  existingAnswer: InteractionObject?, val isReadOnly: Boolean
) : StateItemViewModel(), InteractionAnswerHandler {
  var answerText: CharSequence = existingAnswer?.toAnswerString() ?: ""

  override fun getPendingAnswer(): InteractionObject {
    val interactionObjectBuilder = InteractionObject.newBuilder()
    if (answerText.isNotEmpty() && StringToFractionParser().fromRawInputString(answerText.toString()) == FractionParsingErrors.VALID) {
      interactionObjectBuilder.fraction = StringToFractionParser().getFractionFromString(answerText.toString())
    } else if (answerText.isNotEmpty() && StringToFractionParser().fromRawInputString(answerText.toString()) != FractionParsingErrors.VALID)
      Log.e("FractionInput:", StringToFractionParser().fromRawInputString(answerText.toString()).getError())
    return interactionObjectBuilder.build()
  }
}
