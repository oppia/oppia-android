package org.oppia.app.player.state.itemviewmodel

import org.oppia.app.model.InteractionObject
import org.oppia.app.model.UserAnswer
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.domain.util.toAnswerString

/** [ViewModel] for the text input interaction. */
class TextInputViewModel(
  existingAnswer: InteractionObject?, val isReadOnly: Boolean
): StateItemViewModel(ViewType.TEXT_INPUT_INTERACTION), InteractionAnswerHandler {
  var answerText: CharSequence = existingAnswer?.toAnswerString() ?: ""

  override fun getPendingAnswer(): UserAnswer {
    val userAnswerBuilder = UserAnswer.newBuilder()
    if (answerText.isNotEmpty()) {
      val answerTextString = answerText.toString()
      userAnswerBuilder.answer = InteractionObject.newBuilder().setNormalizedString(answerTextString).build()
      userAnswerBuilder.plainAnswer = answerTextString
    }
    return userAnswerBuilder.build()
  }
}
