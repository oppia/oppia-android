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
  private val partialWholeNumberOnlyRegex = """^-? ?$""".toRegex()
  private val fractionOnlyRegex = """^-? ?(\d+) ?/ ?(\d+)$""".toRegex()
  private val partialFractionOnlyRegex = """^-? ?(\d+) ?/ ?$""".toRegex()
  private val mixedNumberRegex = """^-? ?(\d+) (\d+) ?/ ?(\d+)$""".toRegex()
  private val partialMixedNumberRegexFirst = """^-? ?(\d+) (\d+) ?$""".toRegex()
  private val partialMixedNumberRegexSecond = """^-? ?(\d+) (\d+) ?/ ?$""".toRegex()
  private val invalidCharsRegex = """^[\d\s/-]+$""".toRegex()
  /**
   * This method helps to validate the inputText and return [FractionParsingError]
   * This called on text change.
   * @param inputText is the user input in the [FractionInputInteractionView]
   * @return enum [FractionParsingError]
   */
  fun checkForErrors(inputText: String): FractionParsingError {
    val rawInput: String = inputText.normalizeWhitespace()
    if (!rawInput.matches(invalidCharsRegex))
      return FractionParsingError.INVALID_CHARS
    if (wholeNumberOnlyRegex.matchEntire(rawInput) == null && partialWholeNumberOnlyRegex.matchEntire(rawInput) == null &&
      fractionOnlyRegex.matchEntire(rawInput) == null && partialFractionOnlyRegex.matchEntire(rawInput) == null &&
      mixedNumberRegex.matchEntire(rawInput) == null && partialMixedNumberRegexFirst.matchEntire(rawInput) == null &&
      partialMixedNumberRegexSecond.matchEntire(rawInput) == null
    ) {
      return FractionParsingError.INVALID_FORMAT
    }
    return FractionParsingError.VALID
  }

  /**
   * This method helps to validate the inputText and return [FractionParsingError]
   * This called on submit button click.
   * @param inputText is the user input in the [FractionInputInteractionView]
   * @return enum [FractionParsingError]
   */
  fun checkForErrorsOnSubmit(inputText: String): FractionParsingError {
    var denominator = 1
    var rawInput: String = inputText.normalizeWhitespace()
    if (!rawInput.matches(invalidCharsRegex))
      return FractionParsingError.INVALID_CHARS
    if (mixedNumberRegex.matchEntire(rawInput) == null && fractionOnlyRegex.matchEntire(rawInput) == null &&
      wholeNumberOnlyRegex.matchEntire(rawInput) == null
    ) {
      return FractionParsingError.INVALID_FORMAT
    }
    rawInput = rawInput.trim();
    if (rawInput.startsWith("-")) {
      // Remove the negative char from the string.
      rawInput = rawInput.substring(1).trim()
    }
    // Filter result from split to remove empty strings.
    val numbers = Pattern.compile("[\\s|/]+").split(rawInput)
    when {
      numbers.size == 1 -> {
      }
      numbers.size == 2 -> denominator = parseInt(numbers[1])
      else -> denominator = parseInt(numbers[2])
    }
    if (denominator == 0) {
      return FractionParsingError.DIVISION_BY_ZERO
    }
    return FractionParsingError.VALID
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
