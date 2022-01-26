package org.oppia.android.util.math

import org.oppia.android.app.model.Fraction
import org.oppia.android.domain.util.normalizeWhitespace

/** String parser for [Fraction]s. */
class FractionParser private constructor() {
  companion object {
    private val wholeNumberOnlyRegex = """^-? ?(\d+)$""".toRegex()
    private val fractionOnlyRegex = """^-? ?(\d+) ?/ ?(\d+)$""".toRegex()
    private val mixedNumberRegex = """^-? ?(\d+) (\d+) ?/ ?(\d+)$""".toRegex()

    /**
     * Returns a [Fraction] parse from the specified raw text string.
     *
     * Unlike [tryParseFraction] this function will throw if the provided text is invalid.
     */
    fun parseFraction(text: String): Fraction {
      return tryParseFraction(text)
        ?: throw IllegalArgumentException("Incorrectly formatted fraction: $text")
    }

    /**
     * Returns a [Fraction] parse from the specified raw text string, or null if the provided text
     * doesn't correctly represent a fraction.
     */
    fun tryParseFraction(text: String): Fraction? {
      // Normalize whitespace to ensure that answer follows a simpler subset of possible patterns.
      val inputText: String = text.normalizeWhitespace()
      return parseMixedNumber(inputText)
        ?: parseRegularFraction(inputText)
        ?: parseWholeNumber(inputText)
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
  }
}
