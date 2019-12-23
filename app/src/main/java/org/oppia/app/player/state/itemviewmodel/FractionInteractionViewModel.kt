package org.oppia.app.player.state.itemviewmodel

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import androidx.databinding.Bindable
import androidx.databinding.ObservableField
import org.oppia.app.R
import org.oppia.app.model.Interaction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.UserAnswer
import org.oppia.app.parser.StringToFractionParser
import org.oppia.app.player.state.StateFragment
import org.oppia.app.player.state.answerhandling.AnswerErrorCategory
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler

/** [ViewModel] for the fraction input interaction. */
class FractionInteractionViewModel(
  interaction: Interaction, private val context: Context, private val interactionAnswerHandler: InteractionAnswerHandler
) : StateItemViewModel(ViewType.FRACTION_INPUT_INTERACTION), InteractionAnswerHandler {
  private var pendingAnswerError: String? = null
  var answerText: CharSequence = ""
  var errorMessage = ObservableField<String>("")
  val hintText: CharSequence = deriveHintText(interaction)
  val stringToFractionParser: StringToFractionParser

  init {
    stringToFractionParser = StringToFractionParser()
  }

  override fun getPendingAnswer(): UserAnswer {
    val userAnswerBuilder = UserAnswer.newBuilder()
    if (answerText.isNotEmpty()) {
      val answerTextString = answerText.toString()
      userAnswerBuilder.answer = InteractionObject.newBuilder()
        .setFraction(stringToFractionParser.getFractionFromString(answerTextString))
        .build()
      userAnswerBuilder.plainAnswer = answerTextString
    }
    return userAnswerBuilder.build()
  }

  override fun hasPendingAnswerErrorOnSubmit(): Boolean {
    setPendingAnswerError(AnswerErrorCategory.SUBMIT_TIME)
    return getPendingAnswerError() != null
  }

  /**
   * It set pendingAnswerError, send [errorMessage] and pendingAnswerError to the presenter via [StateFragment]
   */
  fun setPendingAnswerError(category: AnswerErrorCategory) {
    if (answerText.isNotEmpty()) {
      when (category) {
        AnswerErrorCategory.REAL_TIME -> pendingAnswerError =
          stringToFractionParser.getPendingAnswerErrorOnRealTime(answerText.toString()).getErrorMessageFromStringRes(
            context
          )
        AnswerErrorCategory.SUBMIT_TIME -> pendingAnswerError =
          stringToFractionParser.getSubmitTimeError(answerText.toString()).getErrorMessageFromStringRes(
            context
          )
      }
      interactionAnswerHandler.onPendingAnswerError(errorMessage, pendingAnswerError)
    }
  }

  override fun getPendingAnswerError(): String? {
    return pendingAnswerError
  }

  @Bindable
  fun getPasswordTextWatcher(): TextWatcher {
    return object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
      }

      override fun onTextChanged(answer: CharSequence, start: Int, before: Int, count: Int) {
        answerText = answer.toString().trim()
        setPendingAnswerError(AnswerErrorCategory.REAL_TIME)
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
      !allowNonzeroIntegerPart -> context.getString(R.string.fractions_default_hint_text_no_integer)
      else -> context.getString(R.string.fractions_default_hint_text)
    }
  }
}
