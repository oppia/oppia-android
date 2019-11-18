package org.oppia.app.player.state.itemviewmodel

import org.oppia.app.model.InteractionObject
import org.oppia.app.model.UserAnswer
import org.oppia.app.parser.StringToFractionParser
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.domain.util.toAnswerString

/** [ViewModel] for the fraction input interaction. */
class FractionInteractionViewModel(
  existingAnswer: InteractionObject?, val isReadOnly: Boolean
): StateItemViewModel(ViewType.FRACTION_INPUT_INTERACTION), InteractionAnswerHandler {
  var answerText: CharSequence = existingAnswer?.toAnswerString() ?: ""

  override fun getPendingAnswer(): UserAnswer {
    val userAnswerBuilder = UserAnswer.newBuilder()
    if (answerText.isNotEmpty()) {
      val answerTextString = answerText.toString()
      userAnswerBuilder.answer = InteractionObject.newBuilder()
        .setFraction(StringToFractionParser().getFractionFromString(answerTextString))
        .build()
      userAnswerBuilder.plainAnswer = answerTextString
    }
    return userAnswerBuilder.build()
  }
}
