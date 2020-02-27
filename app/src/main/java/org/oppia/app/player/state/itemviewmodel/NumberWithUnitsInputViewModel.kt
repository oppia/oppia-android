package org.oppia.app.player.state.itemviewmodel

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import org.oppia.app.R
import org.oppia.app.model.Interaction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.UserAnswer
import org.oppia.app.parser.StringToNumberWithUnitsParser
import org.oppia.app.player.state.answerhandling.AnswerErrorCategory
import org.oppia.app.player.state.answerhandling.InteractionAnswerErrorReceiver
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler

/** [ViewModel] for the fraction input interaction. */
class NumberWithUnitsInputViewModel(
  interaction: Interaction,
  private val context: Context,
  private val interactionAnswerErrorReceiver: InteractionAnswerErrorReceiver
) : StateItemViewModel(ViewType.NUMBER_WITH_UNIT_INPUT_INTERACTION), InteractionAnswerHandler {

  private var pendingAnswerError: String? = null
  var answerText: CharSequence = ""
  var errorMessage = ObservableField<String>("")

  val hintText: CharSequence = deriveHintText(interaction)
  private val stringToNumberWithUnitsParser: StringToNumberWithUnitsParser = StringToNumberWithUnitsParser()


  init {
    val callback: Observable.OnPropertyChangedCallback = object : Observable.OnPropertyChangedCallback() {
      override fun onPropertyChanged(sender: Observable, propertyId: Int) {
        interactionAnswerErrorReceiver.onPendingAnswerError(pendingAnswerError)
      }
    }
    errorMessage.addOnPropertyChangedCallback(callback)
  }

  override fun getPendingAnswer(): UserAnswer {
    val userAnswerBuilder = UserAnswer.newBuilder()
    if (answerText.isNotEmpty()) {
      val answerTexttring = answerText.toString()
      userAnswerBuilder.answer = InteractionObject.newBuilder()
        .setNumberWithUnits(stringToNumberWithUnitsParser.parseNumberWithUnits(answerTexttring))
        .build()
      userAnswerBuilder.plainAnswer = answerTexttring
    }
    return userAnswerBuilder.build()
  }

  /** It checks the pending error for the current fraction input, and correspondingly updates the error string based on the specified error category. */
  override fun checkPendingAnswerError(category: AnswerErrorCategory): String? {
    if (answerText.isNotEmpty()) {
      when (category) {
        AnswerErrorCategory.REAL_TIME -> pendingAnswerError =
          stringToNumberWithUnitsParser.getNumberWithUnitsRealTimeError(answerText.toString(),context)
        AnswerErrorCategory.SUBMIT_TIME -> pendingAnswerError =
          stringToNumberWithUnitsParser.getNumberWithUnitsSubmitTimeError(answerText.toString(),context)
      }
      errorMessage.set(pendingAnswerError)
    }
    return pendingAnswerError
  }

  @Bindable
  fun getAnswerTextWatcher(): TextWatcher {
    return object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
      }

      override fun onTextChanged(answer: CharSequence, start: Int, before: Int, count: Int) {
        answerText = answer.toString().trim()
        checkPendingAnswerError(AnswerErrorCategory.REAL_TIME)
      }

      override fun afterTextChanged(s: Editable) {
      }
    }
  }

  private fun deriveHintText(interaction: Interaction): CharSequence {
    val customPlaceholder = interaction.customizationArgsMap["customPlaceholder"]?.normalizedString ?: ""
    val allowNonzeroIntegerPart = interaction.customizationArgsMap["allowNonzeroIntegerPart"]?.boolValue ?: true
    return when {
      customPlaceholder.isNotEmpty() -> customPlaceholder
      !allowNonzeroIntegerPart -> context.getString(R.string.number_with_units_default_hint_text)
      else -> context.getString(R.string.number_with_units_default_hint_text)
    }
  }
}
