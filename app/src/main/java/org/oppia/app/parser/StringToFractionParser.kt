package org.oppia.app.parser

import org.oppia.app.model.Fraction

/** This class contains method that helps to parse string to fraction. */
class StringToFractionParser {
  public fun getFractionFromString(text: String): Fraction {
    var inputText: String = text
    var isNegative = false
    if (inputText.startsWith("-"))
      isNegative = true
    inputText = inputText.replace("-", "").trim()
    var numerator = "0"
    var denominator = "0"
    var wholeNumber = "0"
    wholeNumber = if (inputText.contains("/") && inputText.contains(" ")) inputText.substringBefore(" ")
    else if (inputText.contains("/")) wholeNumber else inputText
    inputText = inputText.replace(wholeNumber, "").replace(" ","")
    if (inputText.contains("/")) {
      numerator = inputText.substringBefore("/")
      denominator = inputText.substringAfter("/")
    }
    val fractionObjectBuilder = Fraction.newBuilder()
    fractionObjectBuilder.setIsNegative(isNegative).setNumerator(numerator.toInt())
      .setDenominator(denominator.toInt())
      .setWholeNumber(wholeNumber.toInt())
    return fractionObjectBuilder.build()
  }
}
