package org.oppia.android.app.player.state.itemviewmodel

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import org.oppia.android.R
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.RawUserAnswer
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.parser.FractionParsingUiError
import org.oppia.android.app.player.state.answerhandling.AnswerErrorCategory
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.math.FractionParser
import javax.inject.Inject

/** [StateItemViewModel] for the fraction input interaction. */
class FractionInteractionViewModel private constructor(
  interaction: Interaction,
  rawUserAnswer: RawUserAnswer?,
  val hasConversationView: Boolean,
  val isSplitView: Boolean,
  private val errorOrAvailabilityCheckReceiver: InteractionAnswerErrorOrAvailabilityCheckReceiver,
  private val writtenTranslationContext: WrittenTranslationContext,
  private val resourceHandler: AppLanguageResourceHandler,
  private val translationController: TranslationController
) : StateItemViewModel(ViewType.FRACTION_INPUT_INTERACTION), InteractionAnswerHandler {
  private var pendingAnswerError: String? = null
  var answerText: CharSequence = ""
  var isAnswerAvailable = ObservableField<Boolean>(false)
  var errorMessage = ObservableField<String>("")
  val hintText: CharSequence = deriveHintText(interaction)
  private val fractionParser = FractionParser()

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
    if(rawUserAnswer!=null) {
      answerText = rawUserAnswer.fraction
    }
    errorMessage.addOnPropertyChangedCallback(callback)
    isAnswerAvailable.addOnPropertyChangedCallback(callback)
    checkPendingAnswerError(AnswerErrorCategory.REAL_TIME)
  }

  override fun getPendingAnswer(): UserAnswer = UserAnswer.newBuilder().apply {
    if (answerText.isNotEmpty()) {
      val answerTextString = answerText.toString()
      answer = InteractionObject.newBuilder().apply {
        fraction = fractionParser.parseFractionFromString(answerTextString)
      }.build()
      plainAnswer = answerTextString
      this.writtenTranslationContext = this@FractionInteractionViewModel.writtenTranslationContext
    }
  }.build()

  /** It checks the pending error for the current fraction input, and correspondingly updates the error string based on the specified error category. */
  override fun checkPendingAnswerError(category: AnswerErrorCategory): String? {
    if (answerText.isNotEmpty()) {
      when (category) {
        AnswerErrorCategory.REAL_TIME -> {
          pendingAnswerError =
            FractionParsingUiError.createFromParsingError(
              fractionParser.getRealTimeAnswerError(answerText.toString())
            ).getErrorMessageFromStringRes(resourceHandler)
        }
        AnswerErrorCategory.SUBMIT_TIME -> {
          pendingAnswerError =
            FractionParsingUiError.createFromParsingError(
              fractionParser.getSubmitTimeError(answerText.toString())
            ).getErrorMessageFromStringRes(resourceHandler)
        }
      }
      errorMessage.set(pendingAnswerError)
    }
    return pendingAnswerError
  }

  override fun getRawUserAnswer(): RawUserAnswer? = RawUserAnswer.newBuilder().apply {
    if (answerText.isNotEmpty()) {
      fraction = answerText.toString()
    }
  }.build()

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
      interaction.customizationArgsMap["customPlaceholder"]?.subtitledUnicode
    val placeholderUnicodeOption2 =
      interaction.customizationArgsMap["customPlaceholder"]?.customSchemaValue?.subtitledUnicode
    val customPlaceholder1 =
      placeholderUnicodeOption1?.let { unicode ->
        translationController.extractString(unicode, writtenTranslationContext)
      } ?: ""
    val customPlaceholder2 =
      placeholderUnicodeOption2?.let { unicode ->
        translationController.extractString(unicode, writtenTranslationContext)
      } ?: ""
    val allowNonzeroIntegerPart =
      interaction.customizationArgsMap["allowNonzeroIntegerPart"]?.boolValue ?: true
    return when {
      customPlaceholder1.isNotEmpty() -> customPlaceholder1
      customPlaceholder2.isNotEmpty() -> customPlaceholder2
      !allowNonzeroIntegerPart ->
        resourceHandler.getStringInLocale(R.string.fractions_default_hint_text_no_integer)
      else -> resourceHandler.getStringInLocale(R.string.fractions_default_hint_text)
    }
  }

  /** Implementation of [StateItemViewModel.InteractionItemFactory] for this view model. */
  class FactoryImpl @Inject constructor(
    private val resourceHandler: AppLanguageResourceHandler,
    private val translationController: TranslationController
  ) : InteractionItemFactory {
    override fun create(
      entityId: String,
      hasConversationView: Boolean,
      rawUserAnswer: RawUserAnswer?,
      interaction: Interaction,
      interactionAnswerReceiver: InteractionAnswerReceiver,
      answerErrorReceiver: InteractionAnswerErrorOrAvailabilityCheckReceiver,
      hasPreviousButton: Boolean,
      isSplitView: Boolean,
      writtenTranslationContext: WrittenTranslationContext
    ): StateItemViewModel {
      return FractionInteractionViewModel(
        interaction,
        rawUserAnswer,
        hasConversationView,
        isSplitView,
        answerErrorReceiver,
        writtenTranslationContext,
        resourceHandler,
        translationController
      )
    }
  }
}
