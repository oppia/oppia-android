package org.oppia.app.parser

import android.content.Context
import androidx.annotation.StringRes
import org.oppia.app.R
import org.oppia.app.customview.interaction.FractionInputInteractionView
import org.oppia.app.model.Fraction
import org.oppia.domain.util.normalizeWhitespace
import java.lang.Integer.parseInt
import java.util.regex.Pattern

/** This class contains method that helps to parse string to fraction. */
class StringToFractionParser {
  private val wholeNumberOnlyRegex = """^-? ?(\d+)$""".toRegex()
  private val fractionOnlyRegex = """^-? ?(\d+) ?/ ?(\d+)$""".toRegex()
  private val mixedNumberRegex = """^-? ?(\d+) (\d+) ?/ ?(\d+)$""".toRegex()
  private val invalidCharsRegex = """^[\d\s/-]+$""".toRegex()

  /**
   * This method helps to validate the inputText and return [FractionParsingError]
   * This called on submit button click.
   * @param inputText is the user input in the [FractionInputInteractionView]
   * @return enum [FractionParsingError]
   */
  fun getSubmitTimeError(text: String): FractionParsingError {
    // No need to check for real-time errors since the following logically include them.
    val fraction = parseFraction(text)
    return when {
      fraction == null -> FractionParsingError.INVALID_FORMAT
      fraction.denominator == 0 -> FractionParsingError.DIVISION_BY_ZERO
      else -> FractionParsingError.VALID
    }
  }

  /**
   * This method helps to validate the inputText and return [FractionParsingError]
   * This called on text change.
   * @param inputText is the user input in the [FractionInputInteractionView]
   * @return enum [FractionParsingError]
   */
  fun getRealTimeError(text: String): FractionParsingError {
    val normalized = text.normalizeWhitespace()
    return when {
      !normalized.matches(invalidCharsRegex) -> FractionParsingError.INVALID_CHARS
      normalized.startsWith("/") -> FractionParsingError.INVALID_FORMAT
      normalized.count { it == '/' } > 1 -> FractionParsingError.INVALID_FORMAT
      normalized.indexOf('-') > 0 -> FractionParsingError.INVALID_FORMAT
      else -> FractionParsingError.VALID
    }
  }

  fun getFractionFromString(text: String): Fraction {
    // Normalize whitespace to ensure that answer follows a simpler subset of possible patterns.
    val inputText: String = text.normalizeWhitespace()
    return parseMixedNumber(inputText)
      ?: parseFraction(inputText)
      ?: parseWholeNumber(inputText)
      ?: throw IllegalArgumentException("Incorrectly formatted fraction: $text")
  }

  private fun parseMixedNumber(inputText: String): Fraction? {
    val mixedNumberMatch = mixedNumberRegex.matchEntire(inputText) ?: return null
    val (_, wholeNumberText, numeratorText, denominatorText) = mixedNumberMatch.groupValues
    return Fraction.newBuilder()
      .setIsNegative(isInputNegative(inputText))
      .setWholeNumber(wholeNumberText.toInt())
      .setNumerator(numeratorText.toInt())
      .setDenominator(denominatorText.toInt())
      .build()
  }

  private fun parseFraction(inputText: String): Fraction? {
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
  enum class FractionParsingError(@StringRes error: Int) {
    VALID(error = R.string.valid),
    INVALID_CHARS(error = R.string.invalid_chars),
    INVALID_FORMAT(error = R.string.invalid_format),
    DIVISION_BY_ZERO(error = R.string.divide_by_zero);

    private var error: Int

    init {
      this.error = error
    }

    fun getErrorMessageFromStringRes(context: Context): String {
      return context.getString(this.error)
    }
  }

  /** Categories of errors that can be inferred from a pending answer.  */
  enum class AnswerErrorCategory {
    /** Corresponds to errors that may be found while the user is trying to input an answer.  */
    REAL_TIME,
    /** Corresponds to errors that may be found only when a user tries to submit an answer.  */
    SUBMIT_TIME
  }
}
