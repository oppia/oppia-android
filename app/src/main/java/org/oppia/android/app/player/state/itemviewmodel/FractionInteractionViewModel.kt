package org.oppia.android.app.player.state.itemviewmodel

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import org.oppia.android.R
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.parser.StringToFractionParser
import org.oppia.android.app.player.state.answerhandling.AnswerErrorCategory
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerHandler

/** [StateItemViewModel] for the fraction input interaction. */
class FractionInteractionViewModel(
  interaction: Interaction,
  private val context: Context,
  val hasConversationView: Boolean,
  val isSplitView: Boolean,
  private val interactionAnswerErrorOrAvailabilityCheckReceiver: InteractionAnswerErrorOrAvailabilityCheckReceiver // ktlint-disable max-line-length
) : StateItemViewModel(ViewType.FRACTION_INPUT_INTERACTION), InteractionAnswerHandler {
  private var pendingAnswerError: String? = null
  var answerText = ObservableField<String>("")
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
            answerText.get()!!.isNotEmpty()
          )
        }
      }
    errorMessage.addOnPropertyChangedCallback(callback)
    isAnswerAvailable.addOnPropertyChangedCallback(callback)
  }

  override fun getPendingAnswer(): UserAnswer {
    val userAnswerBuilder = UserAnswer.newBuilder()
    if (answerText.get()!!.isNotEmpty()) {
      val answerTextString = answerText.get().toString()
      userAnswerBuilder.answer = InteractionObject.newBuilder()
        .setFraction(stringToFractionParser.parseFractionFromString(answerTextString))
        .build()
      userAnswerBuilder.plainAnswer = answerTextString
    }
    return userAnswerBuilder.build()
  }

  override fun setPendingAnswer(userAnswer: UserAnswer) {
    answerText.set(userAnswer.plainAnswer)
    Log.d("testSingleton", "i've set the value finally it is ${userAnswer.plainAnswer}")
  }

  /** It checks the pending error for the current fraction input, and correspondingly updates the error string based on the specified error category. */
  override fun checkPendingAnswerError(category: AnswerErrorCategory): String? {
    if (answerText.get()!!.isNotEmpty()) {
      when (category) {
        AnswerErrorCategory.REAL_TIME ->
          pendingAnswerError =
            stringToFractionParser.getRealTimeAnswerError(answerText.get().toString())
              .getErrorMessageFromStringRes(
                context
              )
        AnswerErrorCategory.SUBMIT_TIME ->
          pendingAnswerError =
            stringToFractionParser.getSubmitTimeError(answerText.get().toString())
              .getErrorMessageFromStringRes(
                context
              )
      }
      errorMessage.set(pendingAnswerError)
    }
    return pendingAnswerError
  }

  fun getAnswerTextWatcher(): TextWatcher {
    return object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
      }

      override fun onTextChanged(answer: CharSequence, start: Int, before: Int, count: Int) {
        if (answer.isNotEmpty()) {
          answerText.set(answer.toString().trim())
          val isAnswerTextAvailable = answerText.get()!!.isNotEmpty()
          if (isAnswerTextAvailable != isAnswerAvailable.get()) {
            isAnswerAvailable.set(isAnswerTextAvailable)
          }
          checkPendingAnswerError(AnswerErrorCategory.REAL_TIME)
        }
      }

      override fun afterTextChanged(s: Editable) {
      }
    }
  }

  private fun deriveHintText(interaction: Interaction): CharSequence {
    val customPlaceholder =
      interaction.customizationArgsMap["customPlaceholder"]?.subtitledUnicode?.unicodeStr ?: ""
    val allowNonzeroIntegerPart =
      interaction.customizationArgsMap["allowNonzeroIntegerPart"]?.boolValue ?: true
    return when {
      customPlaceholder.isNotEmpty() -> customPlaceholder
      !allowNonzeroIntegerPart -> context.getString(R.string.fractions_default_hint_text_no_integer)
      else -> context.getString(R.string.fractions_default_hint_text)
    }
  }
}
