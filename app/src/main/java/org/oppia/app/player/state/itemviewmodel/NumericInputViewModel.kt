package org.oppia.app.player.state.itemviewmodel

import org.oppia.app.model.InteractionObject
import org.oppia.app.model.UserAnswer
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler

/** [ViewModel] for the numeric input interaction. */
class NumericInputViewModel : StateItemViewModel(ViewType.NUMERIC_INPUT_INTERACTION), InteractionAnswerHandler {
  var answerText: CharSequence = ""

  override fun getPendingAnswer(): UserAnswer {
    val userAnswerBuilder = UserAnswer.newBuilder()
    if (answerText.isNotEmpty()) {
      val answerTextString = answerText.toString()
      userAnswerBuilder.answer = InteractionObject.newBuilder().setReal(answerTextString.toDouble()).build()
      userAnswerBuilder.plainAnswer = answerTextString
    }
    return userAnswerBuilder.build()
  }
}
