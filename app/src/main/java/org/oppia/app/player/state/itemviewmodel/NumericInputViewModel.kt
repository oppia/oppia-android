package org.oppia.app.player.state.itemviewmodel

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import androidx.annotation.StringRes
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import org.oppia.app.R
import org.oppia.app.customview.interaction.FractionInputInteractionView
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.UserAnswer
import org.oppia.app.parser.StringToFractionParser
import org.oppia.app.player.state.answerhandling.AnswerErrorCategory
import org.oppia.app.player.state.answerhandling.InteractionAnswerErrorReceiver
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.domain.util.normalizeWhitespace

/** [ViewModel] for the numeric input interaction. */
class NumericInputViewModel(
  private val context: Context,
  private val interactionAnswerErrorReceiver: InteractionAnswerErrorReceiver
) : StateItemViewModel(ViewType.NUMERIC_INPUT_INTERACTION), InteractionAnswerHandler {
  var answerText: CharSequence = ""
  private var pendingAnswerError: String? = null
  var errorMessage = ObservableField<String>("")
  private val invalidCharsRegex = """^[\d\s+.-]+$""".toRegex()
  private val invalidCharsLengthRegex = "\\d{8,}".toRegex()

  init {
    val callback: Observable.OnPropertyChangedCallback = object : Observable.OnPropertyChangedCallback() {
      override fun onPropertyChanged(sender: Observable, propertyId: Int) {
        interactionAnswerErrorReceiver.onPendingAnswerError(pendingAnswerError)
      }
    }
    errorMessage.addOnPropertyChangedCallback(callback)
  }

  /** It checks the pending error for the current numeric input, and correspondingly updates the error string based on the specified error category. */
  override fun checkPendingAnswerError(category: AnswerErrorCategory): String? {
    if (answerText.isNotEmpty()) {
      when (category) {
        AnswerErrorCategory.REAL_TIME -> pendingAnswerError =
          getRealTimeAnswerError(answerText.toString()).getErrorMessageFromStringRes(
            context
          )
        AnswerErrorCategory.SUBMIT_TIME -> pendingAnswerError =
          getSubmitTimeError(answerText.toString()).getErrorMessageFromStringRes(
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
        checkPendingAnswerError(AnswerErrorCategory.REAL_TIME)
      }

      override fun afterTextChanged(s: Editable) {
      }
    }
  }

  override fun getPendingAnswer(): UserAnswer {
    val userAnswerBuilder = UserAnswer.newBuilder()
    if (answerText.isNotEmpty()) {
      val answerTextString = answerText.toString()
      userAnswerBuilder.answer = InteractionObject.newBuilder().setReal(answerTextString.toDouble()).build()
      userAnswerBuilder.plainAnswer = answerTextString
    }
    return userAnswerBuilder.build()
  }

  /**
   * Returns a [NumericInpurParsingError] for obvious incorrect fraction formatting issues for the specified raw text, or
   * [NumericInpurParsingError.VALID] if not such issues are found.
   *
   * Note that this method returning a valid result does not guarantee the text is a valid fraction--
   * [getSubmitTimeError] should be used for that, instead. This method is meant to be used as a quick sanity check for
   * general validity, not for definite correctness.
   */
  fun getRealTimeAnswerError(text: String): NumericInpurParsingError {
    val normalized = text.normalizeWhitespace()
    return when {
      !normalized.matches(invalidCharsRegex) -> NumericInpurParsingError.INVALID_CHARS
      normalized.startsWith(".") -> NumericInpurParsingError.INVALID_FORMAT
      normalized.count { it == '.' } > 1 -> NumericInpurParsingError.INVALID_FORMAT
      normalized.lastIndexOf('-') > 0 -> NumericInpurParsingError.INVALID_FORMAT
      normalized.lastIndexOf('+') > 0 -> NumericInpurParsingError.INVALID_FORMAT
      else -> NumericInpurParsingError.VALID
    }
  }

  /**
   * Returns a [FractionParsingError] for the specified text input if it's an invalid fraction, or
   * [FractionParsingError.VALID] if no issues are found. Note that a valid fraction returned by this method is guaranteed
   * to be parsed correctly by [parseRegularFraction].
   *
   * This method should only be used when a user tries submitting an answer. Real-time error detection should be done
   * using [getRealTimeAnswerError], instead.
   */
  fun getSubmitTimeError(text: String): NumericInpurParsingError {
    if (invalidCharsLengthRegex.find(text) != null)
      return NumericInpurParsingError.NUMBER_TOO_LONG
    val fraction = text.toDouble()
    return when {
      fraction == null -> NumericInpurParsingError.INVALID_FORMAT
      else -> NumericInpurParsingError.VALID
    }
  }

  /** Enum to store the errors of [NumericInputInteractionView]. */
  enum class NumericInpurParsingError(@StringRes private var error: Int?) {
    VALID(error = null),
    INVALID_CHARS(error = R.string.number_error_invalid_chars),
    INVALID_FORMAT(error = R.string.number_error_invalid_format),
    NUMBER_TOO_LONG(error = R.string.number_error_larger_than_seven_digits);

    /** Returns the string corresponding to this error's string resources, or null if there is none. */
    fun getErrorMessageFromStringRes(context: Context): String? {
      return error?.let(context::getString)
    }
  }
}
