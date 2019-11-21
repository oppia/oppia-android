package org.oppia.app.parser

import org.json.JSONObject
import org.oppia.app.model.Fraction
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

  fun getJsonCurrencyUnits() {
    jsonAssetRetriever.loadJsonFromAsset("fractions_topic.json")?.getJSONObject("topic")
  }
  fun fromRawInputString(inputText: String): Fraction {
    var rawInput: String = inputText.normalizeWhitespace()
    if (!inputText.matches(invalidCharsRegex))
      return throw IllegalArgumentException(FRACTION_PARSING_ERRORS.INVALID_CHARS)
    if (!fractionRegex.matches(inputText)) {
      throw IllegalArgumentException(FRACTION_PARSING_ERRORS.INVALID_FORMAT)
    }
    var isNegative = false
    var wholeNumber = 0
    var numerator = 0
    var denominator = 1

    rawInput = rawInput.trim();
    if (rawInput.startsWith("-")) {
      isNegative = true
      // Remove the negative char from the string.
      rawInput = rawInput.substring(1).trim()
    }
    // Filter result from split to remove empty strings.
//    var numbers = rawInput.split(/\/|\s/g).filter((token)
    var numbers = Pattern.compile("[\\s|/]+").split(rawInput)
    if (numbers.size == 1) {
      wholeNumber = parseInt(numbers[0])
    } else if (numbers.size == 2) {
      numerator = parseInt(numbers[0])
      denominator = parseInt(numbers[1])
    } else {
      // numbers.length == 3
      wholeNumber = parseInt(numbers[0])
      numerator = parseInt(numbers[1])
      denominator = parseInt(numbers[2])
    }
    if (denominator == 0) {
      throw IllegalArgumentException(FRACTION_PARSING_ERRORS.DIVISION_BY_ZERO)
    }
    return Fraction.newBuilder()
      .setIsNegative(isNegative)
      .setWholeNumber(wholeNumber)
      .setNumerator(numerator)
      .setDenominator(denominator)
      .build()
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
        ?: throw IllegalArgumentException(FRACTION_PARSING_ERRORS.INVALID_FORMAT)
    else return throw IllegalArgumentException(FRACTION_PARSING_ERRORS.INVALID_CHARS)

  }

  private fun parseMixedNumber(inputText: String): Fraction? {
    val mixedNumberMatch = mixedNumberRegex.matchEntire(inputText) ?: return null
    val (_, wholeNumberText, numeratorText, denominatorText) = mixedNumberMatch.groupValues
    if (denominatorText.toInt() == 0)
      return throw IllegalArgumentException(FRACTION_PARSING_ERRORS.DIVISION_BY_ZERO)
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
      return throw IllegalArgumentException(FRACTION_PARSING_ERRORS.DIVISION_BY_ZERO)
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
    // Whole number fractions imply "0/1" fractional parts.
    return Fraction.newBuilder()
      .setIsNegative(isInputNegative(inputText))
      .setWholeNumber(wholeNumberText.toInt())
      .setNumerator(0)
      .setDenominator(1)
      .build()
  }

  private fun isInputNegative(inputText: String): Boolean = inputText.startsWith("-")
}
