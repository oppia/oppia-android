package org.oppia.app.parser

import org.oppia.app.model.Fraction
import org.oppia.domain.util.normalizeWhitespace
import java.lang.IllegalArgumentException

/** This class contains method that helps to parse string to fraction. */
class StringToFractionParser {
  // TODO(BenHenning): Find a clever way to maybe combine these patterns into one.
  private val wholeNumberOnlyRegex = """^-? ?(\d+)$""".toRegex()
  private val fractionOnlyRegex = """^-? ?(\d+) ?/ ?(\d)+$""".toRegex()
  private val mixedNumberRegex = """^-? ?(\d)+ ?(\d+) ?/ ?(\d)+$""".toRegex()

  fun getFractionFromString(text: String): Fraction {
    // Normalize whitespace to ensure that answer follows a simpler subset of possible patterns.
    val inputText: String = text.normalizeWhitespace()
    // TODO(BenHenning): Structure this to not match follow-up patterns if an earlier pattern matches.
    val mixedNumberMatch = mixedNumberRegex.matchEntire(inputText)
    val fractionOnlyMatch = fractionOnlyRegex.matchEntire(inputText)
    val wholeNumberMatch = wholeNumberOnlyRegex.matchEntire(inputText)
    return when {
      mixedNumberMatch != null -> parseMixedNumber(inputText, mixedNumberMatch)
      fractionOnlyMatch != null -> parseFraction(inputText, fractionOnlyMatch)
      wholeNumberMatch != null -> parseWholeNumber(inputText, wholeNumberMatch)
      else -> throw IllegalArgumentException("Incorrectly formatted fraction: $text")
    }
  }

  private fun parseMixedNumber(inputText: String, matchResult: MatchResult): Fraction {
    val (_, wholeNumberText, numeratorText, denominatorText) = matchResult.groupValues
    return Fraction.newBuilder()
      .setIsNegative(isInputNegative(inputText))
      .setWholeNumber(wholeNumberText.toInt())
      .setNumerator(numeratorText.toInt())
      .setDenominator(denominatorText.toInt())
      .build()
  }

  private fun parseFraction(inputText: String, matchResult: MatchResult): Fraction {
    val (_, numeratorText, denominatorText) = matchResult.groupValues
    // Fraction-only numbers imply no whole number.
    return Fraction.newBuilder()
      .setIsNegative(isInputNegative(inputText))
      .setNumerator(numeratorText.toInt())
      .setDenominator(denominatorText.toInt())
      .build()
  }

  private fun parseWholeNumber(inputText: String, matchResult: MatchResult): Fraction {
    val (_, wholeNumberText) = matchResult.groupValues
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
