package org.oppia.app.parser

import org.oppia.app.customview.interaction.FractionInputInteractionView
import org.oppia.app.model.Fraction
import org.oppia.app.topic.FractionParsingErrors
import org.oppia.domain.util.normalizeWhitespace
import java.lang.Integer.parseInt
import java.util.regex.Pattern

/** This class contains method that helps to parse string to fraction. */
class StringToFractionParser {
  private val wholeNumberOnlyRegex = """^-? ?(\d+)$""".toRegex()
  private val fractionOnlyRegex = """^-? ?(\d+) ?/ ?(\d+)$""".toRegex()
  private val mixedNumberRegex = """^-? ?(\d+) (\d+) ?/ ?(\d+)$""".toRegex()
  private val invalidCharsRegex = """^[\d\s/-]+$""".toRegex()
  private val fractionRegex = """^\s*-?\s*((\d*\s*\d+\s*\/\s*\d+)|\d+)\s*$""".toRegex()
  /**
   * @param inputText is the user input in the [FractionInputInteractionView]
   * This method helps to validate the inputText and return [FractionParsingErrors]
   * This called on text change.
   */
  fun checkForErrors(inputText: String): FractionParsingErrors {
    var rawInput: String = inputText.normalizeWhitespace()
    if (!rawInput.matches(invalidCharsRegex))
      return FractionParsingErrors.INVALID_CHARS
    if (!rawInput.equals("-") && parseMixedNumber(rawInput) == null && parseFraction(rawInput) == null && parseWholeNumber(
        rawInput
      ) == null
    ) {
      return FractionParsingErrors.INVALID_FORMAT
    }
    return FractionParsingErrors.VALID
  }

//TODO(#377): use this method check value valid and activate submit button click

  /**
   * @param inputText is the user input in the [FractionInputInteractionView]
   * This method helps to validate the inputText and return [FractionParsingErrors]
   * This called on submit button click.
   */
  fun checkForErrorsOnSubmit(inputText: String): FractionParsingErrors {
    var denominator = 1
    var rawInput: String = inputText.normalizeWhitespace()
    if (!rawInput.matches(invalidCharsRegex))
      return FractionParsingErrors.INVALID_CHARS
    if (parseMixedNumber(rawInput) == null && parseFraction(rawInput) == null && parseWholeNumber(rawInput) == null) {
      return FractionParsingErrors.INVALID_FORMAT
    }
    rawInput = rawInput.trim();
    if (rawInput.startsWith("-")) {
      // Remove the negative char from the string.
      rawInput = rawInput.substring(1).trim()
    }
    // Filter result from split to remove empty strings.
    var numbers = Pattern.compile("[\\s|/]+").split(rawInput)
    if (numbers.size == 1) {
    } else if (numbers.size == 2) {
      denominator = parseInt(numbers[1])
    } else {
      denominator = parseInt(numbers[2])
    }
    if (denominator == 0) {
      return FractionParsingErrors.DIVISION_BY_ZERO
    }
    return FractionParsingErrors.VALID
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
}
