package org.oppia.android.app.parser

import androidx.annotation.StringRes
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.view.models.R
import org.oppia.android.util.extensions.normalizeWhitespace

/**
 * This class contains methods that help to parse string to number, check realtime and submit time
 * errors.
 */
class StringToNumberParser {
  private val validCharsRegex =
    """^[\d\s.-]+$""".toRegex()
  private val validExponentialCharsRegex =
    """^[\d\s.\-e]+$""".toRegex()

  /**
   * Returns a [NumericInputParsingError] for obvious incorrect number formatting issues for the
   * specified raw text, or [NumericInputParsingError.VALID] if no such issues are found.
   *
   * Note that this method returning a valid result does not guarantee the text is a valid number;
   * [getSubmitTimeError] should be used for that, instead. This method is meant to be used as a
   * quick sanity check for general validity, not for definite correctness.
   *
   * @param text the raw string input to validate
   * @param allowExponentialNotation whether scientific notation (e.g. 1.8e3) is allowed
   */
  fun getRealTimeAnswerError(
    text: String,
    allowExponentialNotation: Boolean = false
  ): NumericInputParsingError {
    val normalized = text.normalizeWhitespace()
    if (!allowExponentialNotation) {
      return when {
        !normalized.matches(validCharsRegex) -> NumericInputParsingError.INVALID_FORMAT
        normalized.startsWith(".") || normalized.startsWith("-.") ->
          NumericInputParsingError.STARTING_WITH_FLOATING_POINT
        normalized.count { it == '.' } > 1 -> NumericInputParsingError.INVALID_FORMAT
        normalized.lastIndexOf('-') > 0 -> NumericInputParsingError.INVALID_FORMAT
        else -> NumericInputParsingError.VALID
      }
    }

    val eIndex = normalized.indexOf('e')
    val dotIndex = normalized.indexOf('.')
    val minusIndex = normalized.indexOf('-')
    val hasInvalidMinus = minusIndex != -1 &&
      minusIndex != 0 &&
      normalized[minusIndex - 1] != 'e'

    return when {
      !normalized.matches(validExponentialCharsRegex) -> NumericInputParsingError.INVALID_FORMAT
      normalized.startsWith(".") || normalized.startsWith("-.") ->
        NumericInputParsingError.STARTING_WITH_FLOATING_POINT
      normalized.startsWith("e") || normalized.startsWith("-e") ->
        NumericInputParsingError.INVALID_FORMAT
      normalized.count { it == '.' } > 1 -> NumericInputParsingError.INVALID_FORMAT
      normalized.count { it == 'e' } > 1 -> NumericInputParsingError.INVALID_FORMAT
      eIndex != -1 && dotIndex != -1 && dotIndex > eIndex ->
        NumericInputParsingError.INVALID_FORMAT
      normalized.count { it == '-' } > 1 || hasInvalidMinus ->
        NumericInputParsingError.INVALID_FORMAT
      else -> NumericInputParsingError.VALID
    }
  }

  /**
   * Returns a [NumericInputParsingError] for the specified text input if it's an invalid number, or
   * [NumericInputParsingError.VALID] if no issues are found. Note that a valid number returned by
   * this method is guaranteed to be parsed correctly.
   *
   * This method should only be used when a user tries submitting an answer. Real-time error
   * detection should be done using [getRealTimeAnswerError], instead.
   *
   * @param text the raw string input to validate
   * @param allowExponentialNotation whether scientific notation (e.g. 1.8e3) is allowed
   */
  fun getSubmitTimeError(
    text: String,
    allowExponentialNotation: Boolean = false
  ): NumericInputParsingError {
    if (text.isBlank()) {
      return NumericInputParsingError.EMPTY_INPUT
    }
    if (text.length > 15) {
      return NumericInputParsingError.NUMBER_TOO_LONG
    }
    val realTimeError = getRealTimeAnswerError(text, allowExponentialNotation)
    if (realTimeError != NumericInputParsingError.VALID) {
      return realTimeError
    }
    if (text == "-" ||
      text.endsWith("-") ||
      text.endsWith("e") ||
      text.endsWith(".") ||
      text.contains(".e")
    ) {
      return NumericInputParsingError.INVALID_FORMAT
    }
    return try {
      val parsedDouble = text.toDouble()
      if (parsedDouble.isInfinite() || parsedDouble.isNaN()) {
        NumericInputParsingError.INVALID_FORMAT
      } else {
        NumericInputParsingError.VALID
      }
    } catch (e: Exception) {
      NumericInputParsingError.INVALID_FORMAT
    }
  }

  /** Enum to store the errors of [NumericInputInteractionView]. */
  enum class NumericInputParsingError(@StringRes private var error: Int?) {
    VALID(error = null),
    INVALID_FORMAT(error = R.string.number_error_invalid_format),
    STARTING_WITH_FLOATING_POINT(error = R.string.number_error_starting_with_floating_point),
    NUMBER_TOO_LONG(error = R.string.number_error_larger_than_fifteen_characters),
    EMPTY_INPUT(error = R.string.number_error_empty_input);

    /**
     * Returns the string corresponding to this error's string resources, or null if there is none.
     */
    fun getErrorMessageFromStringRes(resourceHandler: AppLanguageResourceHandler): String? =
      error?.let(resourceHandler::getStringInLocale)
  }
}
