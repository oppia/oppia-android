package org.oppia.app.parser

import android.content.Context
import androidx.annotation.StringRes
import javax.inject.Inject
import org.oppia.app.R
import org.oppia.domain.util.normalizeWhitespace
import org.oppia.util.firebase.CrashlyticsWrapper

/** This class contains methods that help to parse string to number, check realtime and submit time errors. */
class StringToNumberParser @Inject constructor(
  private val crashlyticsWrapper: CrashlyticsWrapper
) {
  private val validCharsRegex = """^[\d\s.-]+$""".toRegex()

  /**
   * Returns a [NumericInputParsingError] for obvious incorrect number formatting issues for the specified raw text, or
   * [NumericInputParsingError.VALID] if not such issues are found.
   *
   * Note that this method returning a valid result does not guarantee the text is a valid number
   * [getSubmitTimeError] should be used for that, instead. This method is meant to be used as a quick sanity check for
   * general validity, not for definite correctness.
   */
  fun getRealTimeAnswerError(text: String): NumericInputParsingError {
    val normalized = text.normalizeWhitespace()
    return when {
      !normalized.matches(validCharsRegex) -> NumericInputParsingError.INVALID_FORMAT
      normalized.startsWith(".") -> NumericInputParsingError.STARTING_WITH_FLOATING_POINT
      normalized.count { it == '.' } > 1 -> NumericInputParsingError.INVALID_FORMAT
      normalized.lastIndexOf('-') > 0 -> NumericInputParsingError.INVALID_FORMAT
      else -> NumericInputParsingError.VALID
    }
  }

  /**
   * Returns a [NumericInputParsingError] for the specified text input if it's an invalid number, or
   * [NumericInputParsingError.VALID] if no issues are found. Note that a valid number returned by this method is guaranteed
   * to be parsed correctly.
   *
   * This method should only be used when a user tries submitting an answer. Real-time error detection should be done
   * using [getRealTimeAnswerError], instead.
   */
  fun getSubmitTimeError(text: String): NumericInputParsingError {
    if (text.length > 15) {
      return NumericInputParsingError.NUMBER_TOO_LONG
    }
    return try {
      text.toDouble()
      NumericInputParsingError.VALID
    } catch (e: Exception) {
      crashlyticsWrapper.logException(e)
      NumericInputParsingError.INVALID_FORMAT
    }
  }

  /** Enum to store the errors of [NumericInputInteractionView]. */
  enum class NumericInputParsingError(@StringRes private var error: Int?) {
    VALID(error = null),
    INVALID_FORMAT(error = R.string.number_error_invalid_format),
    STARTING_WITH_FLOATING_POINT(error = R.string.number_error_starting_with_floating_point),
    NUMBER_TOO_LONG(error = R.string.number_error_larger_than_fifteen_characters);

    /** Returns the string corresponding to this error's string resources, or null if there is none. */
    fun getErrorMessageFromStringRes(context: Context): String? {
      return error?.let(context::getString)
    }
  }
}
