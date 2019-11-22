package org.oppia.app.parser

import org.oppia.app.model.Fraction
import org.oppia.app.topic.FractionParsingErrors
import org.oppia.domain.util.normalizeWhitespace
import java.lang.Integer.parseInt
import java.util.regex.Pattern

/** This class contains method that helps to parse string to fraction. */
class StringToFractionParser {
  private val wholeNumberOnlyRegex = """^-? ?(\d+)$""".toRegex()
  private val fractionOnlyRegex = """^-? ?(\d+) ?/ ?(\d)+$""".toRegex()
  private val mixedNumberRegex = """^-? ?(\d)+ ?(\d+) ?/ ?(\d)+$""".toRegex()
  private val invalidCharsRegex = """^[\d\s/-]+$""".toRegex()
  private val fractionRegex = """^\s*-?\s*((\d*\s*\d+\s*\/\s*\d+)|\d+)\s*$""".toRegex()

  object FRACTION_PARSING_ERRORS {
    const val INVALID_CHARS = "Please only use numerical digits, spaces or forward slashes (/)"
    const val INVALID_FORMAT = "Please enter a valid fraction (e.g., 5/3 or 1 2/3)"
    const val DIVISION_BY_ZERO = "Please do not put 0 in the denominator"
  }

  fun fromRawInputString(inputText: String): FractionParsingErrors {
    var denominator = 1
    var rawInput: String = inputText.normalizeWhitespace()
    if (!inputText.matches(invalidCharsRegex))
      return FractionParsingErrors.INVALID_CHARS
    if (!fractionRegex.matches(inputText)) {
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
    //for testing the validation in a single method
    fromRawInputString(text)
    // Normalize whitespace to ensure that answer follows a simpler subset of possible patterns.
    val inputText: String = text.normalizeWhitespace()
    if (inputText.matches(invalidCharsRegex))
      return parseMixedNumber(inputText)
        ?: parseFraction(inputText)
        ?: parseWholeNumber(inputText)
        ?: throw IllegalArgumentException(FractionParsingErrors.INVALID_FORMAT.toString())
    else return throw IllegalArgumentException(FractionParsingErrors.INVALID_CHARS.toString())

  }

  private fun parseMixedNumber(inputText: String): Fraction? {
    val mixedNumberMatch = mixedNumberRegex.matchEntire(inputText) ?: return null
    val (_, wholeNumberText, numeratorText, denominatorText) = mixedNumberMatch.groupValues
    if (denominatorText.toInt() == 0)
      return throw IllegalArgumentException(FractionParsingErrors.DIVISION_BY_ZERO.toString())
    else
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
    if (denominatorText.toInt() == 0)
      return throw IllegalArgumentException(FractionParsingErrors.DIVISION_BY_ZERO.toString())
    else
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
