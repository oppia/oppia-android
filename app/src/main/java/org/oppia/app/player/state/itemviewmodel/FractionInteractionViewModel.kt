package org.oppia.app.player.state.itemviewmodel

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import org.oppia.app.vm.R
import org.oppia.app.model.Interaction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.UserAnswer
import org.oppia.app.parser.StringToFractionParser
import org.oppia.app.player.state.answerhandling.AnswerErrorCategory
import org.oppia.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler

/** [StateItemViewModel] for the fraction input interaction. */
class FractionInteractionViewModel(
  interaction: Interaction,
  private val context: Context,
  private val interactionAnswerErrorOrAvailabilityCheckReceiver: InteractionAnswerErrorOrAvailabilityCheckReceiver // ktlint-disable max-line-length
) : StateItemViewModel(ViewType.FRACTION_INPUT_INTERACTION), InteractionAnswerHandler {
  private var pendingAnswerError: String? = null
  var answerText: CharSequence = ""
  var isAnswerAvailable = ObservableField<Boolean>(false)
  var errorMessage = ObservableField<String>("")

  val hintText: CharSequence = deriveHintText(interaction)
  private val stringToFractionParser: StringToFractionParser = StringToFractionParser()

  init {
    val callback: Observable.OnPropertyChangedCallback =
      object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable, propertyId: Int) {
          interactionAnswerErrorOrAvailabilityCheckReceiver.onPendingAnswerErrorOrAvailabilityCheck(
            pendingAnswerError,
            answerText.isNotEmpty()
          )
        }
      }
    errorMessage.addOnPropertyChangedCallback(callback)
    isAnswerAvailable.addOnPropertyChangedCallback(callback)
  }

  override fun getPendingAnswer(): UserAnswer {
    val userAnswerBuilder = UserAnswer.newBuilder()
    if (answerText.isNotEmpty()) {
      val answerTextString = answerText.toString()
      userAnswerBuilder.answer = InteractionObject.newBuilder()
        .setFraction(stringToFractionParser.parseFractionFromString(answerTextString))
        .build()
      userAnswerBuilder.plainAnswer = answerTextString
    }
    return userAnswerBuilder.build()
  }

  /** It checks the pending error for the current fraction input, and correspondingly updates the error string based on the specified error category. */
  override fun checkPendingAnswerError(category: AnswerErrorCategory): String? {
    if (answerText.isNotEmpty()) {
      when (category) {
        AnswerErrorCategory.REAL_TIME ->
          pendingAnswerError =
            stringToFractionParser.getRealTimeAnswerError(answerText.toString())
              .getErrorMessageFromStringRes(
                context
              )
        AnswerErrorCategory.SUBMIT_TIME ->
          pendingAnswerError =
            stringToFractionParser.getSubmitTimeError(answerText.toString())
              .getErrorMessageFromStringRes(
                context
              )
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
        val isAnswerTextAvailable = answerText.isNotEmpty()
        if (isAnswerTextAvailable != isAnswerAvailable.get()) {
          isAnswerAvailable.set(isAnswerTextAvailable)
        }
        checkPendingAnswerError(AnswerErrorCategory.REAL_TIME)
      }

      override fun afterTextChanged(s: Editable) {
      }
    }
  }

  private fun deriveHintText(interaction: Interaction): CharSequence {
    val customPlaceholder =
      interaction.customizationArgsMap["customPlaceholder"]?.normalizedString ?: ""
    val allowNonzeroIntegerPart =
      interaction.customizationArgsMap["allowNonzeroIntegerPart"]?.boolValue ?: true
    return when {
      customPlaceholder.isNotEmpty() -> customPlaceholder
      !allowNonzeroIntegerPart -> context.getString(R.string.fractions_default_hint_text_no_integer)
      else -> context.getString(R.string.fractions_default_hint_text)
    }
  }
}
