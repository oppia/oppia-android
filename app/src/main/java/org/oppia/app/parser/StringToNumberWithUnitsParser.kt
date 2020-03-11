package org.oppia.app.parser

import android.content.Context
import androidx.annotation.StringRes
import org.oppia.app.R
import org.oppia.app.model.Fraction
import org.oppia.app.model.NumberUnit
import org.oppia.app.model.NumberWithUnits
import org.oppia.app.model.Unit
import org.oppia.domain.util.normalizeWhitespace
import java.lang.Double
import java.lang.Integer.parseInt
import java.util.regex.Pattern

/**
 * This class contains methods that help to parse string to number, check realtime and submit time errors.
 * reference https://github.com/oppia/oppia/blob/develop/core/templates/dev/head/domain/objects/NumberWithUnitsObjectFactory.ts#L220.
 */
class StringToNumberWithUnitsParser {
  private lateinit var value: String
  private lateinit var units: String
  private lateinit var rawInput: String
  private var ind = 0
  private val stringToFractionParser: StringToFractionParser = StringToFractionParser()
  private val stringToNumberParser: StringToNumberParser = StringToNumberParser()

  var type = ""
  var real = 0.0
  // Default fraction value.
  var fractionObj = Fraction.newBuilder().setDenominator(1).build()

  /** @return index of pattern in s or -1, if not found
   */
  fun indexOf(pattern: Pattern, s: String): Int {
    val matcher = pattern.matcher(s)
    return if (matcher.find()) matcher.start() else -1
  }

  /** Returns a [NumberWithUnits] parse from the specified raw text string. */
  fun parseNumberWithUnits(inputText: String): NumberWithUnits? {
    // Normalize whitespace to ensure that answer follows a simpler subset of possible patterns.
    rawInput = inputText.normalizeWhitespace()
    rawInput = rawInput.trim()
    return parseNumberWithUnitsNotStartingWithCurrency(rawInput)
      ?: parseNumberWithUnitsStartsWithCurrency(rawInput)

  }

  fun parseNumberWithUnitsNotStartingWithCurrency(rawInput: String): NumberWithUnits? {

    // Start with digit when there is no currency unit.
    if (rawInput.matches("^\\-?\\d.*\$".toRegex())) {
      ind = indexOf(Pattern.compile("[a-zA-Z(₹$]"), rawInput)
      if (ind == -1) {
        // There is value with no units.
        value = rawInput
        units = ""
      } else {
        ind -= 1
        value = rawInput.substring(0, ind).trim()
        units = rawInput.substring(ind).trim()
      }
    } else return null
    if (value.contains("/")) {
      type = "fraction"
      fractionObj = StringToFractionParser().parseFractionFromString(value)
    } else {
      type = "real"
      real = Double.parseDouble(value)
    }
    var unitsObj = UnitsObjectFactory().fromRawInputString(units)
    if (type.equals("fraction"))
      return NumberWithUnits.newBuilder().setFraction(fractionObj).addAllUnit(unitsObj.units.asIterable())
        .build()
    else
      return NumberWithUnits.newBuilder().setReal(real).addAllUnit(unitsObj.units.asIterable()).build()
    return NumberWithUnits.getDefaultInstance()
  }

  fun parseNumberWithUnitsStartsWithCurrency(rawInput: String): NumberWithUnits? {

    if (rawInput.matches("^\\-?\\d.*\$".toRegex()))
      return null
    else {

      ind = indexOf(Pattern.compile("[0-9]"), rawInput)

      units = rawInput.substring(0, ind).trim()

      units = units + ""
      var ind2 = indexOf(Pattern.compile("[a-zA-Z(]"), rawInput.substring(ind))
      if (ind2 != -1) {
        ind2 = rawInput.indexOf(rawInput.substring(ind).elementAt(ind2).toString(), ind)
        value = rawInput.substring(ind, ind2 - ind).trim()
        units += rawInput.substring(ind2).trim()
      } else {
        value = rawInput.substring(ind).trim()
        units = units.trim()
      }
    }
    if (value.contains("/")) {
      type = "fraction"
      fractionObj = StringToFractionParser().parseFractionFromString(value)
    } else {
      type = "real"
      real = Double.parseDouble(value)
    }
    var unitsObj = UnitsObjectFactory().fromRawInputString(units)
    if (type.equals("fraction"))
      return NumberWithUnits.newBuilder().setFraction(fractionObj).addAllUnit(unitsObj.units.asIterable())
        .build()
    else
      return NumberWithUnits.newBuilder().setReal(real).addAllUnit(unitsObj.units.asIterable()).build()
    return NumberWithUnits.getDefaultInstance()
  }

  private interface IUnitsDict {
    // TODO(#7165): Replace "ArrayList<String>" with the exact type. This has been kept as
    // "ArrayList<String>" because "units" is a list with varying element types. An exact
    // type needs to be found for it.
    var units: ArrayList<String>
  }

  class Units {
    // TODO(#7165): Replace "ArrayList<String>" with the exact type. This has been kept as
    // "ArrayList<String>" because "units" is a list with varying element types. An exact
    // type needs to be found for it.
    var units: ArrayList<NumberUnit>

    constructor(unitsList: ArrayList<NumberUnit>) {
      this.units = unitsList
    }

    private fun toDict(): IUnitsDict {
      units = this.units
      return units as IUnitsDict
    }

    fun unitToString(): String {
      var unit = ""
      for (i in 0 until this.units.size) {
        var d = this.units[i]
        if (d.exponent == 1) {
          unit += d.unit + " "
        } else {
          unit += d.unit + "^" + d.exponent.toString() + " "
        }
      }
      return unit.trim()
    }
  }

  class UnitsObjectFactory {
    fun isunit(unit: String): Boolean {
      return !("/*() ".contains(unit))
    }

    fun stringToLexical(unitsString: String): ArrayList<String> {
      var units = unitsString
      units += "#"
      var unitList: ArrayList<String> = ArrayList()
      var unit = ""
      for (i in 0 until units.length) {
        if ("*/()# ".contains(units[i]) && unit != "per") {
          if (unit.length > 0) {
            if (unitList.size > 0 && this.isunit(unitList.last())) {
              unitList.add("*")
            }
            unitList.add(unit)
            unit = ""
          }
          if (!("# ".contains(units[i]))) {
            unitList.add(units[i].toString())
          }
        } else if (units[i].toString() == " " && unit == "per") {
          unitList.add("/")
          unit = ""
        } else {
          unit += units[i]
        }
      }
      return unitList
    }

    // TODO(#7165): Replace "ArrayList<String>" with the exact type. This has been kept as
    // "ArrayList<String>" because the return type is a list with varying element types. An
    // exact type needs to be found for it.
    fun unitWithMultiplier(unitList: ArrayList<String>): LinkedHashMap<String, Int> {
      var multiplier = 1
      var unitsWithMultiplier: LinkedHashMap<String, Int> = LinkedHashMap()
      var parenthesisStack: MutableMap<String, Int> = HashMap()

      for (ind in 0 until unitList.size) {
        if (unitList[ind] == "/") {
          multiplier = -multiplier
        } else if (unitList[ind] == "(") {
          if (unitList[ind - 1] == "/") {
            // If previous element was division then we need to inverse
            // multiplier when we find its corresponsing closing parenthesis.
            // Second element of pushed element is used for this purpose.
            parenthesisStack.put("(", -1)
          } else {
            // If previous element was not division then we don"t need to
            // invert the multiplier.
            parenthesisStack.put("(", 1)
          }
        } else if (unitList[ind] == ")") {
          var elem = parenthesisStack.values.last()
          multiplier = elem * multiplier
        } else if (this.isunit(unitList[ind])) {
          if ((unitsWithMultiplier).containsKey(unitList[ind])) {// this additional check and extra code is because hashmap does not support multiple keys
            unitsWithMultiplier.put(unitList[ind], unitsWithMultiplier.getValue(unitList[ind]) + multiplier)
          } else
            unitsWithMultiplier.put(unitList[ind], multiplier)
          // If previous element was division then we need to invert
          // multiplier.
          if (ind > 0 && unitList[ind - 1] == "/") {
            multiplier = -multiplier
          }
        }
      }
      return unitsWithMultiplier
    }

    // TODO(#7165): Replace "ArrayList<String>" with the exact type. This has been kept as
    // "ArrayList<String>" because "unitDict" is a dict with varying element types. An
    // exact type needs to be found for it, Once that is found the return type
    // can also be typed.
    fun convertUnitDictToList(unitDict: MutableMap<String, Int>): ArrayList<NumberUnit> {
      var unitList: ArrayList<NumberUnit> = ArrayList()
      for ((key, value) in unitDict) {
        unitList.add(NumberUnit.newBuilder().setUnit(key).setExponent(value).build())
      }
      return unitList
    }

    // TODO(#7165): Replace "ArrayList<String>" with the exact type. This has been kept as
    // "ArrayList<String>" because "unitsWithMultiplier" is a dict with varying element types.
    // An exact type needs to be found for it, Once that is found the return type
    // can also be typed.
    //5 kg / kg^2 K mol / (N m s^2) K s
    fun unitToList(unitsWithMultiplier: LinkedHashMap<String, Int>): ArrayList<NumberUnit> {
      var unitDict: LinkedHashMap<String, Int> = LinkedHashMap()
      for ((key, value) in unitsWithMultiplier) {
        var unit = key
        var multiplier = value
        var ind = unit.indexOf("^")
        var s: String
        var power: Int
        if (ind > -1) {
          s = unit.substring(0, ind).trim()
          power = parseInt(unit.substring(ind + 1))
        } else {
          s = unit.trim()
          power = 1
        }
        if (!(unitDict).containsKey(s)) {
          unitDict.set(s, 0)
        }
        unitDict.put(s, (unitDict.getValue(s) + (multiplier * power)))
      }
      return this.convertUnitDictToList(unitDict)
    }

    // TODO(#7165): Replace "ArrayList<String>" with the exact type. This has been kept as
    // "ArrayList<String>" because "units" is a list with varying element types. An exact
    // type needs to be found for it.
    fun fromList(unitsList: ArrayList<NumberUnit>): Units {
      return Units(unitsList)
    }

    // TODO(#7165): Replace "ArrayList<String>" with the exact type. This has been kept as
    // "ArrayList<String>" because the return type is to be determined once "unitToList" has
    // determined return type.
    fun fromStringToList(unitsString: String): ArrayList<NumberUnit> {
      return this.unitToList(
        this.unitWithMultiplier(this.stringToLexical(unitsString))
      )
    }

    fun createCurrencyUnits() {
      var keys = CURRENCY_UNITS.keys
      for (i in 0 until keys.size) {
//      if (CURRENCY_UNITS[keys.elementAt(i)]!!.baseUnit == null) {
//        // Base unit (like: rupees, dollar etc.).
//        createUnit(CURRENCY_UNITS[keys.elementAt(i)]!!.name, {
//          aliases: CURRENCY_UNITS[keys.elementAt(i)]!!.aliases
//        })
//      } else {
//        // Sub unit (like: paise, cents etc.).
//        createUnit(CURRENCY_UNITS[keys.elementAt(i)].name, {
//          definition: CURRENCY_UNITS[keys.elementAt(i)].base_unit,
//          aliases: CURRENCY_UNITS[keys.elementAt(i)].aliases
//        })
//      }
      }
    }

    // TODO(#7165): Replace "ArrayList<String>" with the exact type. This has been kept as
    // "ArrayList<String>" because "units" is a list with varying element types. An exact
    // type needs to be found for it.
    fun toMathjsCompatibleString(unitsString: String): String {
      var units = unitsString
      // Makes the units compatible with the math.js allowed format.
      units = units.replace("per", "/")

      // Special symbols need to be replaced as math.js doesn"t support custom
      // units starting with special symbols. Also, it doesn"t allow units
      // followed by a number as in the case of currency units.
      var keys = (CURRENCY_UNITS).keys
      for (i in 0 until keys.size) {
        for (j in 0 until CURRENCY_UNITS[keys.elementAt(i)]!!.frontUnits.size) {
          if (units.contains(CURRENCY_UNITS[keys.elementAt(i)]!!.frontUnits[j])) {
            units = units.replace(CURRENCY_UNITS[keys.elementAt(i)]!!.frontUnits[j], "")
            units = CURRENCY_UNITS[keys.elementAt(i)]!!.name + units
          }
        }

        for (j in 0 until CURRENCY_UNITS[keys.elementAt(i)]!!.aliases.size) {
          if (units.contains(CURRENCY_UNITS[keys.elementAt(i)]!!.aliases[j])) {
            units = units.replace(
              CURRENCY_UNITS[keys.elementAt(i)]!!.aliases[j],
              CURRENCY_UNITS[keys.elementAt(i)]!!.name
            )
          }
        }
      }
      return units.trim()
    }

    // TODO(#7165): Replace "ArrayList<String>" with the exact type. This has been kept as
    // "ArrayList<String>" because "units" is a list with varying element types. An exact
    // type needs to be found for it.
    fun fromRawInputString(unitsString: String): Units {
      var units = unitsString
      try {
        this.createCurrencyUnits()
      } catch (e: Exception) {
      }

      var compatibleUnits = this.toMathjsCompatibleString(units)

      return Units(this.fromStringToList(units))
    }
  }

  fun getNumberWithUnitsRealTimeError(rawInput: String, context: Context): String? {

    // Start with digit when there is no currency unit.
    if (rawInput.matches("^\\-?\\d.*\$".toRegex())) {
      ind = indexOf(Pattern.compile("[a-zA-Z(₹$]"), rawInput)
      if (ind == -1) {
        // There is value with no units.
        value = rawInput
        units = ""
      } else {
        ind -= 1
        value = rawInput.substring(0, ind).trim()
        units = rawInput.substring(ind).trim()
      }
    } else {
      ind = indexOf(Pattern.compile("[0-9]"), rawInput)
      if (ind != -1) {
        units = rawInput.substring(0, ind).trim()
        units = units + ""
        var ind2 = indexOf(Pattern.compile("[a-zA-Z(]"), rawInput.substring(ind))
        if (ind2 != -1) {
          ind2 = rawInput.indexOf(rawInput.substring(ind).elementAt(ind2).toString(), ind)
          value = rawInput.substring(ind, ind2).trim()
          units += rawInput.substring(ind2).trim()
        } else {
          value = rawInput.substring(ind).trim()
          units = units.trim()
        }
      } else {
        units = rawInput
        value = ""
      }
    }
    if (units != "") {
      // Checking invalid characters in units.
      if (units.matches("[^0-9a-zA-Z/* ^()₹$-]".toRegex())) {
        return NumberWithUnitsParsingError.INVALID_UNIT_CHARS.getErrorMessageFromStringRes(context)
      }
    }
    if (value.contains("/")) {
      type = "fraction"
    } else {
      type = "real"
    }
    if (value.isEmpty()) {
      return when {
        units.startsWith(".") -> NumberWithUnitsParsingError.STARTING_WITH_FLOATING_POINT.getErrorMessageFromStringRes(
          context
        )
        units.contains("/") -> NumberWithUnitsParsingError.INVALID_FORMAT.getErrorMessageFromStringRes(context)
        units.count { it == '-' } > 1 || units.count { it == '.' } > 1 -> NumberWithUnitsParsingError.INVALID_UNIT.getErrorMessageFromStringRes(
          context
        )
        else -> NumberWithUnitsParsingError.VALID.getErrorMessageFromStringRes(context)
      }
    } else
      if (type.equals("fraction"))
        return stringToFractionParser.getRealTimeAnswerError(value).getErrorMessageFromStringRes(context)
      else
        return stringToNumberParser.getRealTimeAnswerError(value).getErrorMessageFromStringRes(context)
  }

  fun getNumberWithUnitsSubmitTimeError(rawInput: String, context: Context): String? {

    // Allow validation only when rawInput is not null or an empty string.
    // Start with digit when there is no currency unit.
    if (rawInput.matches("^\\-?\\d.*\$".toRegex())) {
      if (ind == -1) {
        // There is value with no units.
        value = rawInput
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
            return NumberWithUnitsParsingError.INVALID_CURRENCY_FORMAT.getErrorMessageFromStringRes(context)
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
        return NumberWithUnitsParsingError.INVALID_CURRENCY.getErrorMessageFromStringRes(context)
      }

      if (ind == -1) {
        return NumberWithUnitsParsingError.INVALID_CURRENCY.getErrorMessageFromStringRes(context)
      }

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
        return NumberWithUnitsParsingError.INVALID_CURRENCY.getErrorMessageFromStringRes(context)
      }

      // Checking invalid characters in value.
      if (value.matches("[a-zA-Z]".toRegex()) || value.matches("[ *^$₹()#@]/".toRegex())) {
        return NumberWithUnitsParsingError.INVALID_VALUE.getErrorMessageFromStringRes(context)
      }

      if (units != "") {
        // Checking invalid characters in units.
        if (units.matches("[^0-9a-zA-Z/* ^()₹$-]".toRegex())) {
          return NumberWithUnitsParsingError.INVALID_UNIT_CHARS.getErrorMessageFromStringRes(context)
        }
      }
    }
    if (type.equals("fraction"))
      return stringToFractionParser.getSubmitTimeError(value).getErrorMessageFromStringRes(
        context
      )
    else
      return stringToNumberParser.getSubmitTimeError(value).getErrorMessageFromStringRes(
        context
      )
    return NumberWithUnitsParsingError.VALID.getErrorMessageFromStringRes(context)
  }

  fun getStringOfNumberWithUnits(inputText: String): String {
    var numberWithUnitsString = this.value.trim()
    var unitsString = this.units.trim()
    numberWithUnitsString =
      if (Pattern.compile("^[rs,$,₹]", Pattern.CASE_INSENSITIVE).matcher(inputText).find())
        unitsString.trim() + " " + numberWithUnitsString else numberWithUnitsString + " " + unitsString.trim()
    numberWithUnitsString = numberWithUnitsString.trim()
    return numberWithUnitsString
  }
 fun numberWithUnitsToString(type: String,numberWithUnits: NumberWithUnits): String {
    lateinit var  numberWithUnitsString: String
    // The NumberWithUnits class is allowed to have 4 properties namely
    // type, real, fraction and units. Hence, we cannot inject
    // UnitsObjectFactory, since that'll lead to creation of 5th property
    // which isn't allowed. Refer objects.py L#956.
    var unitsString: String = Units(ArrayList(numberWithUnits.unitList.toList())).unitToString()
    if (unitsString.contains("$")) {
      unitsString = unitsString.replace("$", "")
      numberWithUnitsString += "$" + " "
    }
    if (unitsString.contains("Rs")) {
      unitsString = unitsString.replace("Rs", "")
      numberWithUnitsString += "Rs" + " "
    }
    if (unitsString.contains("₹")) {
      unitsString = unitsString.replace("₹", "")
      numberWithUnitsString += "₹" + " "
    }

    if (type == "real") {
      numberWithUnitsString += numberWithUnits.real.toString()+" "
    } else if (type == "fraction") {
      numberWithUnitsString += numberWithUnits.fraction.toString() + " "
    }
    numberWithUnitsString += unitsString.trim()
    numberWithUnitsString = numberWithUnitsString.trim()
    return numberWithUnitsString
  }
  /** Enum to store the errors of [NumberWithUnitsInputInteractionView]. */
  enum class NumberWithUnitsParsingError(@StringRes private var error: Int?) {
    VALID(error = null),
    INVALID_CURRENCY(error = R.string.number_with_units_error_invalid_currency),
    INVALID_CURRENCY_FORMAT(error = R.string.number_with_units_error_invalid_currency_format),
    INVALID_FORMAT(error = R.string.number_error_invalid_format),
    INVALID_UNIT_CHARS(error = R.string.number_with_units_error_invalid_unit_chars),
    INVALID_UNIT(error = R.string.number_with_units_error_invalid_unit),
    INVALID_VALUE(error = R.string.number_with_units_error_invalid_value),
    STARTING_WITH_FLOATING_POINT(error = R.string.number_error_starting_with_floating_point);

    /** Returns the string corresponding to this error"s string resources, or null if there is none. */
    fun getErrorMessageFromStringRes(context: Context): String? {
      return error?.let(context::getString)
    }
  }

  companion object {
    private val CURRENCY_UNITS = mapOf(
      "dollar" to Unit(
        name = "dollar",
        aliases = listOf("$", "dollars", "Dollars", "Dollar", "USD"),
        frontUnits = listOf("$"),
        baseUnit = null
      ),
      "rupee" to Unit(
        name = "rupee",
        aliases = listOf("Rs", "rupees", "₹", "Rupees", "Rupee"),
        frontUnits = listOf("Rs ", "₹"),
        baseUnit = null
      ),
      "cent" to Unit(
        name = "cent",
        aliases = listOf("cents", "Cents", "Cent"),
        frontUnits = listOf(),
        baseUnit = "0.01 dollar"
      ),
      "paise" to Unit(
        name = "paise",
        aliases = listOf("paisa", "Paise", "Paisa"),
        frontUnits = listOf(),
        baseUnit = "0.01 rupee"
      )
    )
  }
}
