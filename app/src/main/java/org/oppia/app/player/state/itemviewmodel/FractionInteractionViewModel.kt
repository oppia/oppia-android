package org.oppia.app.player.state.itemviewmodel

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import androidx.databinding.Bindable
import androidx.databinding.ObservableField
import org.oppia.app.model.InteractionObject
import org.oppia.app.parser.StringToFractionParser
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.app.topic.FractionParsingErrors
import org.oppia.domain.util.toAnswerString

class FractionInteractionViewModel(
  existingAnswer: InteractionObject?,
  val isReadOnly: Boolean,
  val context: Context
) : StateItemViewModel(), InteractionAnswerHandler {
  var answerText: CharSequence = existingAnswer?.toAnswerString() ?: ""
  var errorMessage = ObservableField<String>("")

  override fun getPendingAnswer(): InteractionObject {
    val interactionObjectBuilder = InteractionObject.newBuilder()
    if (answerText.isNotEmpty()) {
      interactionObjectBuilder.fraction = StringToFractionParser().getFractionFromString(answerText.toString())
    }
    return interactionObjectBuilder.build()
  }

  override fun getPendingAnswerError(): String? {
    if (answerText.isNotEmpty() && StringToFractionParser().checkForErrors(answerText.toString()) != FractionParsingErrors.VALID)
      return StringToFractionParser().checkForErrors(answerText.toString()).getErrorMessageFromStringRes(context)
    else
      return null
  }

  @Bindable
  fun getPasswordTextWatcher(): TextWatcher {
    return object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
      }

      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        errorMessage.set(
          StringToFractionParser().checkForErrors(answerText.toString()).getErrorMessageFromStringRes(
            context
          )
        )
      }

      override fun afterTextChanged(s: Editable) {
      }
    }
  }
}
