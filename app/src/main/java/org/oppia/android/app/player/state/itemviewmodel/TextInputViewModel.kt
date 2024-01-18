package org.oppia.android.app.player.state.itemviewmodel

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.annotation.StringRes
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import org.oppia.android.R
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.state.answerhandling.AnswerErrorCategory
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.math.FractionParser.FractionParsingError
import javax.inject.Inject
import org.oppia.android.app.parser.TextParsingError
import org.oppia.android.app.parser.TextParsingUiError.Companion.createFromParsingError

/** [StateItemViewModel] for the text input interaction. */
class TextInputViewModel private constructor(
  interaction: Interaction,
  val hasConversationView: Boolean,
  private val interactionAnswerErrorOrAvailabilityCheckReceiver: InteractionAnswerErrorOrAvailabilityCheckReceiver, // ktlint-disable max-line-length
  val isSplitView: Boolean,
  private val writtenTranslationContext: WrittenTranslationContext,
  private val resourceHandler: AppLanguageResourceHandler,
  private val translationController: TranslationController
) : StateItemViewModel(ViewType.TEXT_INPUT_INTERACTION), InteractionAnswerHandler {
  var answerText: CharSequence = ""
  val hintText: CharSequence = deriveHintText(interaction)
  private var pendingAnswerError: String? = null

  var isAnswerAvailable = ObservableField<Boolean>(false)
  val errorMessage = ObservableField<String>("")

  init {
    val callback: Observable.OnPropertyChangedCallback =
      object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable, propertyId: Int) {
          interactionAnswerErrorOrAvailabilityCheckReceiver.onPendingAnswerErrorOrAvailabilityCheck(
            /* pendingAnswerError= */ pendingAnswerError,
            inputAnswerAvailable = true
          )
        }
      }
    isAnswerAvailable.addOnPropertyChangedCallback(callback)
    errorMessage.addOnPropertyChangedCallback(callback)
    interactionAnswerErrorOrAvailabilityCheckReceiver.onPendingAnswerErrorOrAvailabilityCheck(
      /* pendingAnswerError= */ null,
      inputAnswerAvailable = true
    )
  }

  override fun checkPendingAnswerError(category: AnswerErrorCategory): String? {
    when (category) {
      AnswerErrorCategory.REAL_TIME -> {

        if (answerText.isNotEmpty()) {
        } else {
          pendingAnswerError = null
        }
      }
      AnswerErrorCategory.SUBMIT_TIME -> {

        pendingAnswerError =
          createFromParsingError(
            getSubmitTimeError(answerText.toString())
          ).getErrorMessageFromStringRes(resourceHandler)
      }
    }

    errorMessage.set(pendingAnswerError)
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

  override fun getPendingAnswer(): UserAnswer = UserAnswer.newBuilder().apply {
    if (answerText.isNotEmpty()) {
      val answerTextString = answerText.toString()
      answer = InteractionObject.newBuilder().apply {
        normalizedString = answerTextString
      }.build()
      plainAnswer = answerTextString
      writtenTranslationContext = this@TextInputViewModel.writtenTranslationContext
    }
  }.build()

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
      } ?: "" // The default placeholder for text input is empty.
    return when {
      placeholder1.isNotEmpty() -> placeholder1
      placeholder2.isNotEmpty() -> placeholder2
      else -> resourceHandler.getStringInLocale(R.string.text_input_default_hint_text)
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
      interaction: Interaction,
      interactionAnswerReceiver: InteractionAnswerReceiver,
      answerErrorReceiver: InteractionAnswerErrorOrAvailabilityCheckReceiver,
      hasPreviousButton: Boolean,
      isSplitView: Boolean,
      writtenTranslationContext: WrittenTranslationContext,
      timeToStartNoticeAnimationMs: Long?
    ): StateItemViewModel {
      return TextInputViewModel(
        interaction,
        hasConversationView,
        answerErrorReceiver,
        isSplitView,
        writtenTranslationContext,
        resourceHandler,
        translationController
      )
    }
  }

  /**
   * Returns a [FractionParsingError] for the specified text input if it's an invalid fraction, or
   * [FractionParsingError.VALID] if no issues are found. Note that a valid fraction returned by
   * this method is guaranteed to be parsed correctly by [parseRegularFraction].
   *
   * This method should only be used when a user tries submitting an answer. Real-time error
   * detection should be done using [getRealTimeAnswerError], instead.
   */
  fun getSubmitTimeError(text: String): TextParsingError {
    if (text.isNullOrBlank()) {
      return TextParsingError.EMPTY_INPUT
    }
    return TextParsingError.VALID
  }

}
