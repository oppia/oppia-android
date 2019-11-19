package org.oppia.app.player.state.itemviewmodel

import org.oppia.app.model.InteractionObject
import org.oppia.app.player.state.StateFragmentPresenter
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.domain.util.toAnswerString

class TextInputViewModel(
  existingAnswer: InteractionObject?, val isReadOnly: Boolean , private val stateFragmentPresenter: StateFragmentPresenter?
) : StateItemViewModel(), InteractionAnswerHandler {
  var answerText: CharSequence = existingAnswer?.toAnswerString() ?: ""

  override fun getPendingAnswer(): InteractionObject {
    val interactionObjectBuilder = InteractionObject.newBuilder()
    if (answerText.isNotEmpty()) {
      interactionObjectBuilder.normalizedString = answerText.toString()
    }
    return interactionObjectBuilder.build()
  }

  fun onTextChanged(text: CharSequence) {
    stateFragmentPresenter?.controlSubmitButton(text.isNotEmpty())
  }
}
