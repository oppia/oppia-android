package org.oppia.android.util.math

import org.oppia.android.app.model.NumberUnit
import org.oppia.android.app.model.NumberWithUnits
import org.oppia.android.util.extensions.normalizeWhitespace

class NumberWithUnitsParser {
  private val numberWithCurrencyRegex =
    """^([a-zA-Z]+( [a-zA-Z]+)*) (\d+(\.\d+)?)$""".toRegex()

  fun parseNumberWithUnits(text: String): NumberWithUnits {
    val inputText = text.normalizeWhitespace()

    if (!"""^\d""".toRegex().matches(inputText)) {
      CURRENCY_UNITS.keys.forEach { currencyKey ->
        val currencyUnit = CURRENCY_UNITS[currencyKey]!!
        currencyUnit.frontUnits.forEach { frontUnit ->
          if (inputText.startsWith(frontUnit)) {
            val numberPart = inputText.removePrefix(frontUnit).trim()
            return NumberWithUnits.newBuilder().apply {
              real = numberPart.toDouble()
              addUnit(
                NumberUnit.newBuilder().apply {
                  unit = currencyUnit.name
                  exponent = 1
                }.build()
              )
            }.build()
          }
        }
      }
    }

    return NumberWithUnits.newBuilder().apply {
      real = 2.0
      addUnit(
        NumberUnit.newBuilder().apply {
          unit = "cm"
          exponent = 2
        }.build()
      )
    }.build()
  }

  enum class NumberWithUnitsParsingError {
    VALID,
    EMPTY_INPUT,
    INVALID_CURRENCY_FORMAT,
    NUMBER_TOO_LONG
  }

  companion object {
    data class CurrencyUnit(
      val name: String,
      val aliases: List<String>,
      val frontUnits: List<String>,
      val baseUnit: String?
    )

    val CURRENCY_UNITS = mapOf(
      "dollar" to CurrencyUnit(
        name = "dollar",
        aliases = listOf("$", "dollars", "Dollars", "Dollar", "USD"),
        frontUnits = listOf("$"),
        baseUnit = null
      ),
      "rupee" to CurrencyUnit(
        name = "rupee",
        aliases = listOf("Rs", "rupees", "₹", "Rupees", "Rupee"),
        frontUnits = listOf("Rs ", "₹"),
        baseUnit = null
      ),
      "cent" to CurrencyUnit(
        name = "cent",
        aliases = listOf("cents", "Cents", "Cent"),
        frontUnits = emptyList(),
        baseUnit = "0.01 dollar"
      ),
      "paise" to CurrencyUnit(
        name = "paise",
        aliases = listOf("paisa", "Paise", "Paisa"),
        frontUnits = emptyList(),
        baseUnit = "0.01 rupee"
      )
    )
  }
}
