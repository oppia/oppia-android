package org.oppia.android.util.math

import org.oppia.android.app.model.NumberUnit
import org.oppia.android.app.model.NumberWithUnits
import org.oppia.android.util.extensions.normalizeWhitespace

class NumberWithUnitsParser {
  private val numberWithTrailingUnitsRegex =
    """^(-?\d+(\.\d+)?)( [a-zA-Z]+( [a-zA-Z]+)*)$""".toRegex()
  private val numberWithPrecedingUnitsRegex =
    """^([a-zA-Z]+( [a-zA-Z]+)*) (-?\d+(\.\d+)?)$""".toRegex()
  fun parseNumberWithUnits(text: String): NumberWithUnits {
    val inputText = text.normalizeWhitespace()
    return NumberWithUnits.newBuilder().apply {
      real = 2.0
      addUnit(
        NumberUnit.newBuilder().apply {
          unit = inputText
          exponent = 1
        }.build()
      )
    }.build()
  }

  enum class NumberWithUnitsParsingError {
    VALID,
    EMPTY_INPUT,
    INVALID_FORMAT,
    NUMBER_TOO_LONG
  }
}
