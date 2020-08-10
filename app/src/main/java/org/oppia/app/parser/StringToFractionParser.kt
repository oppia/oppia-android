package org.oppia.app.parser

import android.content.Context
import androidx.annotation.StringRes
import org.oppia.app.model.Fraction
import org.oppia.app.vm.R
import org.oppia.domain.util.normalizeWhitespace

/** This class contains method that helps to parse string to fraction. */
class StringToFractionParser {
  private val wholeNumberOnlyRegex =
    """^-? ?(\d+)$""".toRegex()
  private val fractionOnlyRegex =
    """^-? ?(\d+) ?/ ?(\d+)$""".toRegex()
  private val mixedNumberRegex =
    """^-? ?(\d+) (\d+) ?/ ?(\d+)$""".toRegex()
  private val invalidCharsRegex =
    """^[\d\s/-]+$""".toRegex()
  private val invalidCharsLengthRegex = "\\d{8,}".toRegex()

  /**
   * Returns a [FractionParsingError] for the specified text input if it's an invalid fraction, or
   * [FractionParsingError.VALID] if no issues are found. Note that a valid fraction returned by this method is guaranteed
   * to be parsed correctly by [parseRegularFraction].
   *
   * This method should only be used when a user tries submitting an answer. Real-time error detection should be done
   * using [getRealTimeAnswerError], instead.
   */
  fun getSubmitTimeError(text: String): FractionParsingError {
    if (invalidCharsLengthRegex.find(text) != null)
      return FractionParsingError.NUMBER_TOO_LONG
    val fraction = parseFraction(text)
    return when {
      fraction == null -> FractionParsingError.INVALID_FORMAT
      fraction.denominator == 0 -> FractionParsingError.DIVISION_BY_ZERO
      else -> FractionParsingError.VALID
    }
  }

  /**
   * Returns a [FractionParsingError] for obvious incorrect fraction formatting issues for the specified raw text, or
   * [FractionParsingError.VALID] if not such issues are found.
   *
   * Note that this method returning a valid result does not guarantee the text is a valid fraction--
   * [getSubmitTimeError] should be used for that, instead. This method is meant to be used as a quick sanity check for
   * general validity, not for definite correctness.
   */
  fun getRealTimeAnswerError(text: String): FractionParsingError {
    val normalized = text.normalizeWhitespace()
    return when {
      !normalized.matches(invalidCharsRegex) -> FractionParsingError.INVALID_CHARS
      normalized.startsWith("/") -> FractionParsingError.INVALID_FORMAT
      normalized.count { it == '/' } > 1 -> FractionParsingError.INVALID_FORMAT
      normalized.lastIndexOf('-') > 0 -> FractionParsingError.INVALID_FORMAT
      else -> FractionParsingError.VALID
    }
  }

  /** Returns a [Fraction] parse from the specified raw text string. */
  fun parseFraction(text: String): Fraction? {
    // Normalize whitespace to ensure that answer follows a simpler subset of possible patterns.
    val inputText: String = text.normalizeWhitespace()
    return parseMixedNumber(inputText)
      ?: parseRegularFraction(inputText)
      ?: parseWholeNumber(inputText)
  }

  /** Returns a [Fraction] parse from the specified raw text string. */
  fun parseFractionFromString(text: String): Fraction {
    return parseFraction(text)
      ?: throw IllegalArgumentException("Incorrectly formatted fraction: $text")
  }

  private fun parseMixedNumber(inputText: String): Fraction? {
    val mixedNumberMatch = mixedNumberRegex.matchEntire(inputText) ?: return null
    val (_, wholeNumberText, numeratorText, denominatorText) =
      mixedNumberMatch.groupValues
    return Fraction.newBuilder()
      .setIsNegative(isInputNegative(inputText))
      .setWholeNumber(wholeNumberText.toInt())
      .setNumerator(numeratorText.toInt())
      .setDenominator(denominatorText.toInt())
      .build()
  }

  private fun parseRegularFraction(inputText: String): Fraction? {
    val fractionOnlyMatch = fractionOnlyRegex.matchEntire(inputText) ?: return null
    val (_, numeratorText, denominatorText) = fractionOnlyMatch.groupValues
    // Fraction-only numbers imply no whole number.
    return Fraction.newBuilder()
      .setIsNegative(isInputNegative(inputText))
      .setNumerator(numeratorText.toInt())
      .setDenominator(denominatorText.toInt())
      .build()
  }

  private fun parseWholeNumber(inputText: String): Fraction? {
    val wholeNumberMatch = wholeNumberOnlyRegex.matchEntire(inputText) ?: return null
    val (_, wholeNumberText) = wholeNumberMatch.groupValues
    // Whole number fractions imply '0/1' fractional parts.
    return Fraction.newBuilder()
      .setIsNegative(isInputNegative(inputText))
      .setWholeNumber(wholeNumberText.toInt())
      .setNumerator(0)
      .setDenominator(1)
      .build()
  }

  private fun isInputNegative(inputText: String): Boolean = inputText.startsWith("-")

  /** Enum to store the errors of [FractionInputInteractionView]. */
  enum class FractionParsingError(@StringRes private var error: Int?) {
    VALID(error = null),
    INVALID_CHARS(error = R.string.fraction_error_invalid_chars),
    INVALID_FORMAT(error = R.string.fraction_error_invalid_format),
    DIVISION_BY_ZERO(error = R.string.fraction_error_divide_by_zero),
    NUMBER_TOO_LONG(error = R.string.fraction_error_larger_than_seven_digits);

    /** Returns the string corresponding to this error's string resources, or null if there is none. */
    fun getErrorMessageFromStringRes(context: Context): String? {
      return error?.let(context::getString)
    }
  }
}
