package org.oppia.android.app.player.state.itemviewmodel

import android.text.Editable
import android.text.TextWatcher
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.RawUserAnswer
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.parser.StringToNumberParser
import org.oppia.android.app.player.state.answerhandling.AnswerErrorCategory
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.android.app.translation.AppLanguageResourceHandler
import javax.inject.Inject

/** [StateItemViewModel] for the numeric input interaction. */
class NumericInputViewModel private constructor(
  val hasConversationView: Boolean,
  rawUserAnswer: RawUserAnswer,
  private val interactionAnswerErrorOrAvailabilityCheckReceiver: InteractionAnswerErrorOrAvailabilityCheckReceiver, // ktlint-disable max-line-length
  val isSplitView: Boolean,
  private val writtenTranslationContext: WrittenTranslationContext,
  private val resourceHandler: AppLanguageResourceHandler
) : StateItemViewModel(ViewType.NUMERIC_INPUT_INTERACTION), InteractionAnswerHandler {
  var answerText: CharSequence = rawUserAnswer.numeric ?: ""
  private var pendingAnswerError: String? = null
  val errorMessage = ObservableField<String>("")
  var isAnswerAvailable = ObservableField<Boolean>(false)
  private val stringToNumberParser: StringToNumberParser = StringToNumberParser()

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
    checkPendingAnswerError(AnswerErrorCategory.REAL_TIME)
  }

  /** It checks the pending error for the current numeric input, and correspondingly updates the error string based on the specified error category. */
  override fun checkPendingAnswerError(category: AnswerErrorCategory): String? {
    if (answerText.isNotEmpty()) {
      pendingAnswerError = when (category) {
        AnswerErrorCategory.REAL_TIME ->
          stringToNumberParser.getRealTimeAnswerError(answerText.toString())
            .getErrorMessageFromStringRes(resourceHandler)
        AnswerErrorCategory.SUBMIT_TIME ->
          stringToNumberParser.getSubmitTimeError(answerText.toString())
            .getErrorMessageFromStringRes(resourceHandler)
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
        real = answerTextString.toDouble()
      }.build()
      plainAnswer = answerTextString
      this.writtenTranslationContext = this@NumericInputViewModel.writtenTranslationContext
    }
  }.build()

  override fun getRawUserAnswer(): RawUserAnswer = RawUserAnswer.newBuilder().apply {
    if (answerText.isNotEmpty()) {
      numeric = answerText.toString()
    }
  }.build()

  /** Implementation of [StateItemViewModel.InteractionItemFactory] for this view model. */
  class FactoryImpl @Inject constructor(
    private val resourceHandler: AppLanguageResourceHandler
  ) : InteractionItemFactory {
    override fun create(
      entityId: String,
      hasConversationView: Boolean,
      rawUserAnswer: RawUserAnswer,
      interaction: Interaction,
      isSubmitAnswerEnabled: Boolean,
      interactionAnswerReceiver: InteractionAnswerReceiver,
      answerErrorReceiver: InteractionAnswerErrorOrAvailabilityCheckReceiver,
      hasPreviousButton: Boolean,
      isSplitView: Boolean,
      writtenTranslationContext: WrittenTranslationContext
    ): StateItemViewModel {
      return NumericInputViewModel(
        hasConversationView,
        rawUserAnswer,
        answerErrorReceiver,
        isSplitView,
        writtenTranslationContext,
        resourceHandler
      )
    }
  }
}
