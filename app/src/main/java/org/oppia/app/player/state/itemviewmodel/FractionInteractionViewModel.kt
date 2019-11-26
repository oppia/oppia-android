package org.oppia.app.player.state.itemviewmodel

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.databinding.Bindable
import org.oppia.app.BR
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

  @Bindable
  fun getPendingAnswerError(): String? {
    if (answerText.isNotEmpty() && StringToFractionParser().fromRawInputString(answerText.toString()) != FractionParsingErrors.VALID)
      return StringToFractionParser().fromRawInputString(answerText.toString()).getError()
    else
      return null
  }

  fun setAnswerText(answerText: String) {
    this.answerText = answerText;
    notifyPropertyChanged(BR.pendingAnswerError)
  }

  @Bindable
  fun getPasswordTextWatcher(): TextWatcher {
    return object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
      }

      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        setAnswerText(s.toString())
      }

      override fun afterTextChanged(s: Editable) {
      }
    }
  }
}
