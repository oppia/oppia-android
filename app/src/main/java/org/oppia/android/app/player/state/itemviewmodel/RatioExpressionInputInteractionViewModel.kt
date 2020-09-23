package org.oppia.android.app.player.state.itemviewmodel

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import org.oppia.android.app.R
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.parser.StringToRatioParser
import org.oppia.android.app.player.state.answerhandling.AnswerErrorCategory
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.android.app.utility.toAccessibleAnswerString
import org.oppia.android.domain.util.toAnswerString

/** [StateItemViewModel] for the ratio expression input interaction. */
class RatioExpressionInputInteractionViewModel(
  interaction: Interaction,
  private val context: Context,
  val hasConversationView: Boolean,
  val isSplitView: Boolean,
  private val errorOrAvailabilityCheckReceiver: InteractionAnswerErrorOrAvailabilityCheckReceiver
) : StateItemViewModel(ViewType.RATIO_EXPRESSION_INPUT_INTERACTION), InteractionAnswerHandler {
  private var pendingAnswerError: String? = null
  var answerText: CharSequence = ""
  var isAnswerAvailable = ObservableField<Boolean>(false)
  var errorMessage = ObservableField<String>("")

  val hintText: CharSequence = deriveHintText(interaction)
  private val stringToRatioParser: StringToRatioParser = StringToRatioParser()
  private val numberOfTerms =
    interaction.customizationArgsMap["numberOfTerms"]?.signedInt ?: 0

  init {
    val callback: Observable.OnPropertyChangedCallback =
      object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable, propertyId: Int) {
          errorOrAvailabilityCheckReceiver.onPendingAnswerErrorOrAvailabilityCheck(
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
      val ratioAnswer = stringToRatioParser.parseRatioOrThrow(answerText.toString())
      userAnswerBuilder.answer = InteractionObject.newBuilder()
        .setRatioExpression(ratioAnswer)
        .build()
      userAnswerBuilder.plainAnswer = ratioAnswer.toAnswerString()
      userAnswerBuilder.contentDescription = ratioAnswer.toAccessibleAnswerString(context)
    }
    return userAnswerBuilder.build()
  }

  /** It checks the pending error for the current ratio input, and correspondingly updates the error string based on the specified error category. */
  override fun checkPendingAnswerError(category: AnswerErrorCategory): String? {
    if (answerText.isNotEmpty()) {
      when (category) {
        AnswerErrorCategory.REAL_TIME ->
          pendingAnswerError =
            stringToRatioParser.getRealTimeAnswerError(answerText.toString())
              .getErrorMessageFromStringRes(
                context
              )
        AnswerErrorCategory.SUBMIT_TIME ->
          pendingAnswerError =
            stringToRatioParser.getSubmitTimeError(
              answerText.toString(),
              numberOfTerms = numberOfTerms
            ).getErrorMessageFromStringRes(
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
    val placeholder =
      interaction.customizationArgsMap["placeholder"]?.subtitledUnicode?.unicodeStr ?: ""
    return when {
      placeholder.isNotEmpty() -> placeholder
      else -> context.getString(R.string.ratio_default_hint_text)
    }
  }
}
