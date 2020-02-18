package org.oppia.app.parser

import org.oppia.app.model.CurrencyUnit
import org.oppia.app.model.Fraction
import org.oppia.app.model.NumberUnit
import org.oppia.app.model.NumberWithUnits
import org.oppia.app.model.Units
import org.oppia.domain.util.normalizeWhitespace
import java.lang.Double

import java.util.regex.Pattern

/** This class contains methods that help to parse string to number, check realtime and submit time errors. */
class StringToNumberWithUnitsParser {
  private lateinit var value: String
  private lateinit var units: String
  private lateinit var CURRENCY_UNITS: HashMap<String, Units>

  /** @return index of pattern in s or -1, if not found
   */
  fun indexOf(pattern: Pattern, s: String): Int {
    val matcher = pattern.matcher(s)
    return if (matcher.find()) matcher.start() else -1
  }

  fun getCurrencyUnits(): CurrencyUnit {
    var currencyUnits = HashMap<String, Units>()
    currencyUnits.put(
      "dollar",
      Units("dollar", mutableListOf("$", "dollars", "Dollars", "Dollar", "USD"), mutableListOf("$"), null)
    )
    currencyUnits.put(
      "rupee",
      Units("rupee", mutableListOf("Rs", "rupees", "₹", "Rupees", "Rupee"), mutableListOf("Rs ", "₹"), null)
    )
    currencyUnits.put(
      "cent", Units("cent", mutableListOf("cents", "Cents", "Cent"), mutableListOf(), "0.01 dollar")
    )
    currencyUnits.put(
      "paise", Units("paise", mutableListOf("paisa", "Paise", "Paisa"), mutableListOf(), "0.01 rupee")
    )
    return CurrencyUnit(currencyUnits)
  }

  fun getNumberWithUnits(inputText: String): NumberWithUnits {
    var rawInput: String = inputText.normalizeWhitespace()
    CURRENCY_UNITS = getCurrencyUnits().unitHashMap
    rawInput = rawInput.trim()
    var type = ""
    var real = 0.0
    // Default fraction value.
    var fractionObj = Fraction.newBuilder().setDenominator(1).build()
    var units = ""
    var value = ""

    // Allow validation only when rawInput is not null or an empty string.
    if (rawInput.isNotEmpty() && rawInput != null) {
      // Start with digit when there is no currency unit.
      if (rawInput.matches("^\\-?\\d.*\$".toRegex())) {
        var ind = indexOf(Pattern.compile("[a-z(₹$]"), rawInput)
        if (ind == -1) {
          // There is value with no units.
          value = rawInput;
          units = ""
        } else {
          ind -= 1
          value = rawInput.substring(0, ind).trim()
          units = rawInput.substring(ind).trim()
        }

        var keys = (CURRENCY_UNITS).keys
        for (i in keys) {
          for (j in 0 until CURRENCY_UNITS[i]!!.frontUnits.size) {
            if (units.indexOf(CURRENCY_UNITS[i]!!.frontUnits[j]) != -1) {
              throw  Error(
                "INVALID_CURRENCY_FORMAT"
              )
            }
          }
        }
      } else {
        var startsWithCorrectCurrencyUnit = false
        var keys = (CURRENCY_UNITS).keys
        for (i in keys) {
          for (j in 0 until CURRENCY_UNITS[i]!!.frontUnits.size) {
            if (rawInput.startsWith(CURRENCY_UNITS[i]!!.frontUnits[j])) {
              startsWithCorrectCurrencyUnit = true
              break
            }
          }
        }
        if (startsWithCorrectCurrencyUnit == false) {
          throw  Error("INVALID_CURRENCY")
        }

        var ind = indexOf(Pattern.compile("[0-9]"), rawInput)


        if (ind === -1) {
          throw  Error("INVALID_CURRENCY")
        }
        units = rawInput.substring(0, ind).trim()

        startsWithCorrectCurrencyUnit = false
        keys = (CURRENCY_UNITS).keys
        for (i in keys) {
          for (j in 0 until CURRENCY_UNITS[i]!!.frontUnits.size) {
            if (units == (CURRENCY_UNITS[i]!!.frontUnits[j]).trim()) {
              startsWithCorrectCurrencyUnit = true
              break
            }
          }
        }
        if (startsWithCorrectCurrencyUnit == false) {
          throw  Error("INVALID_CURRENCY")
        }
        units = units + ""

        var ind2 = indexOf(Pattern.compile("[a-z(]"), rawInput.substring(ind))
        if (ind2 != -1) {
          value = rawInput.substring(ind, ind2 - ind).trim()
          units += rawInput.substring(ind2).trim()
        } else {
          value = rawInput.substring(ind).trim()
          units = units.trim()
        }
      }
      // Checking invalid characters in value.
      if (value.matches("[a - z]".toRegex()) || value.matches("[ *^$₹()#@]/".toRegex())) {
        throw  Error("INVALID_VALUE")
      }

      if (value.contains('/')) {
        type = "fraction"
        fractionObj = StringToFractionParser().parseFractionFromString(value)
      } else {
        type = "real"
        real = Double.parseDouble(value)
      }
      if (units != "") {
        // Checking invalid characters in units.
        if (units.matches("[^0-9a-z/* ^()₹$-]".toRegex())) {
          throw Error(
            "INVALID_UNIT_CHARS"
          )
        }
      }
    }
    if (type.equals("fraction"))
      return NumberWithUnits.newBuilder().setFraction(fractionObj).addUnit(NumberUnit.newBuilder().setUnit(units))
        .build()
    else
      return NumberWithUnits.newBuilder().setReal(real).addUnit(NumberUnit.newBuilder().setUnit(units)).build()


    return NumberWithUnits.getDefaultInstance()
  }

  fun getStringOfNumberWithUnits(inputText: String): String {
    var numberWithUnitsString = this.value.trim()
    var unitsString = this.units.trim()
    numberWithUnitsString =
      if (Pattern.compile("^[rs,$,₹,€,£,¥]", Pattern.CASE_INSENSITIVE).matcher(inputText).find())
        unitsString.trim() + " " + numberWithUnitsString else numberWithUnitsString + " " + unitsString.trim()
    numberWithUnitsString = numberWithUnitsString.trim()
    return numberWithUnitsString
  }
}
