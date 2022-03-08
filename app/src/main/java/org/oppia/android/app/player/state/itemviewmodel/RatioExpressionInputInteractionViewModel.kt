package org.oppia.android.app.player.state.itemviewmodel

import android.text.Editable
import android.text.TextWatcher
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import org.oppia.android.R
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.parser.StringToRatioParser
import org.oppia.android.app.player.state.answerhandling.AnswerErrorCategory
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.utility.toAccessibleAnswerString
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.math.toAnswerString

/** [StateItemViewModel] for the ratio expression input interaction. */
class RatioExpressionInputInteractionViewModel(
  interaction: Interaction,
  val hasConversationView: Boolean,
  val isSplitView: Boolean,
  private val errorOrAvailabilityCheckReceiver: InteractionAnswerErrorOrAvailabilityCheckReceiver,
  private val writtenTranslationContext: WrittenTranslationContext,
  private val resourceHandler: AppLanguageResourceHandler,
  private val translationController: TranslationController
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

  override fun getPendingAnswer(): UserAnswer = UserAnswer.newBuilder().apply {
    if (answerText.isNotEmpty()) {
      val ratioAnswer = stringToRatioParser.parseRatioOrThrow(answerText.toString())
      answer = InteractionObject.newBuilder().apply {
        ratioExpression = ratioAnswer
      }.build()
      plainAnswer = ratioAnswer.toAnswerString()
      contentDescription = ratioAnswer.toAccessibleAnswerString(resourceHandler)
      this.writtenTranslationContext =
        this@RatioExpressionInputInteractionViewModel.writtenTranslationContext
    }
  }.build()

  /** It checks the pending error for the current ratio input, and correspondingly updates the error string based on the specified error category. */
  override fun checkPendingAnswerError(category: AnswerErrorCategory): String? {
    if (answerText.isNotEmpty()) {
      when (category) {
        AnswerErrorCategory.REAL_TIME ->
          pendingAnswerError =
            stringToRatioParser.getRealTimeAnswerError(answerText.toString())
              .getErrorMessageFromStringRes(resourceHandler)
        AnswerErrorCategory.SUBMIT_TIME ->
          pendingAnswerError =
            stringToRatioParser.getSubmitTimeError(
              answerText.toString(),
              numberOfTerms = numberOfTerms
            ).getErrorMessageFromStringRes(resourceHandler)
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
    // The subtitled unicode can apparently exist in the structure in two different formats.
    val placeholderUnicodeOption1 =
      interaction.customizationArgsMap["placeholder"]?.subtitledUnicode
    val placeholderUnicodeOption2 =
      interaction.customizationArgsMap["placeholder"]?.customSchemaValue?.subtitledUnicode
    val placeholder1 =
      placeholderUnicodeOption1?.let { unicode ->
        translationController.extractString(unicode, writtenTranslationContext)
      } ?: ""
    val placeholder2 =
      placeholderUnicodeOption2?.let { unicode ->
        translationController.extractString(unicode, writtenTranslationContext)
      } ?: ""
    return when {
      placeholder1.isNotEmpty() -> placeholder1
      placeholder2.isNotEmpty() -> placeholder2
      else -> resourceHandler.getStringInLocale(R.string.ratio_default_hint_text)
    }
  }
}
