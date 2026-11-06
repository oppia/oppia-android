package org.oppia.android.app.player.state.itemviewmodel

import android.text.Editable
import android.text.TextWatcher
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import org.oppia.android.app.model.AnswerAndResponse
import org.oppia.android.app.model.AnswerErrorCategory
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.UserAnswerState
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.view.models.R
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.domain.util.toNumberWithUnits
import org.oppia.android.util.math.NumberWithUnitsParser
import javax.inject.Inject

/** [StateItemViewModel] for the number with units input interaction. */
class NumberWithUnitsInputViewModel private constructor(
  interaction: Interaction,
  val hasConversationView: Boolean,
  private val interactionAnswerErrorOrAvailabilityCheckReceiver: InteractionAnswerErrorOrAvailabilityCheckReceiver, // ktlint-disable max-line-length
  val isSplitView: Boolean,
  private val writtenTranslationContext: WrittenTranslationContext,
  private val resourceHandler: AppLanguageResourceHandler,
  private val translationController: TranslationController,
  userAnswerState: UserAnswerState
) : StateItemViewModel(ViewType.NUMBER_WITH_UNITS_INPUT_INTERACTION), InteractionAnswerHandler {
  /** The current text entered by the learner. */
  var answerText: CharSequence = userAnswerState.textInputAnswer
  private var answerErrorCategory: AnswerErrorCategory = AnswerErrorCategory.NO_ERROR
  /** The hint text displayed for the input field. */
  val hintText: CharSequence = deriveHintText(interaction)
  private var pendingAnswerError: String? = null

  /** Whether the input field contains an answer. */
  var isAnswerAvailable = ObservableField<Boolean>(false)
  /** The error message displayed for the current answer. */
  val errorMessage = ObservableField<String>("")

  init {
    val callback: Observable.OnPropertyChangedCallback =
      object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable, propertyId: Int) {
          interactionAnswerErrorOrAvailabilityCheckReceiver.onPendingAnswerErrorOrAvailabilityCheck(
            pendingAnswerError = pendingAnswerError,
            inputAnswerAvailable = true // Allow submit on empty answer.
          )
        }
      }
    isAnswerAvailable.addOnPropertyChangedCallback(callback)
    errorMessage.addOnPropertyChangedCallback(callback)

    // Initializing with default values so that submit button is enabled by default.
    interactionAnswerErrorOrAvailabilityCheckReceiver.onPendingAnswerErrorOrAvailabilityCheck(
      pendingAnswerError = null,
      inputAnswerAvailable = true
    )
    checkPendingAnswerError(userAnswerState.answerErrorCategory)
  }

  override fun checkPendingAnswerError(category: AnswerErrorCategory): String? {
    answerErrorCategory = category
    val parsedAnswer = NumberWithUnitsParser.parseNumberWithUnits(answerText.toString())
    pendingAnswerError = when (category) {
      AnswerErrorCategory.REAL_TIME -> null
      AnswerErrorCategory.SUBMIT_TIME -> {
        if (parsedAnswer is NumberWithUnitsParser.Companion.NumberWithUnitsParsingResult.Failure) {
          org.oppia.android.app.parser.NumberWithUnitsParsingUiError
            .createFromParsingError(parsedAnswer.error)
            .getErrorMessageFromStringRes(resourceHandler)
        } else {
          null
        }
      }
      else -> null
    }
    errorMessage.set(pendingAnswerError)
    return pendingAnswerError
  }

  /** Returns a [TextWatcher] that updates the answer and its validation state. */
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
      val parsedAnswer = NumberWithUnitsParser.parseNumberWithUnits(answerTextString)
      if (parsedAnswer is NumberWithUnitsParser.Companion.NumberWithUnitsParsingResult.Success) {
        answer = InteractionObject.newBuilder()
          .setNumberWithUnits(parsedAnswer.result.toNumberWithUnits())
          .build()
      }
      // If parsing fails, we don't populate the answer field.
      // The submit time error check will catch this and prevent submission.
      plainAnswer = answerTextString
      writtenTranslationContext = this@NumberWithUnitsInputViewModel.writtenTranslationContext
    }
  }.build()

  override fun getUserAnswerState(): UserAnswerState {
    return UserAnswerState.newBuilder().apply {
      this.textInputAnswer = answerText.toString()
      this.answerErrorCategory = answerErrorCategory
    }.build()
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
      } ?: "" // The default placeholder for text input is empty.
    return when {
      placeholder1.isNotEmpty() -> placeholder1
      placeholder2.isNotEmpty() -> placeholder2
      else -> resourceHandler.getStringInLocale(R.string.number_with_units_input_hint_text)
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
      timeToStartNoticeAnimationMs: Long?,
      userAnswerState: UserAnswerState,
      wrongAnswerList: List<AnswerAndResponse>
    ): StateItemViewModel {
      return NumberWithUnitsInputViewModel(
        interaction,
        hasConversationView,
        answerErrorReceiver,
        isSplitView,
        writtenTranslationContext,
        resourceHandler,
        translationController,
        userAnswerState
      )
    }
  }
}
