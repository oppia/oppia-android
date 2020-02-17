package org.oppia.app.customview.interaction

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import org.json.JSONObject
import org.oppia.app.model.Fraction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.NumberUnit
import org.oppia.app.model.NumberWithUnits
import org.oppia.app.model.UserAnswer
import org.oppia.app.parser.StringToFractionParser
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.domain.util.normalizeWhitespace
import java.lang.Double.parseDouble
import java.util.*
import java.util.regex.Pattern

/** The custom EditText class for number input with units interaction view. */
class NumberWithUnitsInputInteractionView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = android.R.attr.editTextStyle
) : EditText(context, attrs, defStyle), InteractionAnswerHandler {
  private lateinit var type: String
  private var real: Float = 0f
  private lateinit var fractionObject: Fraction
  private lateinit var value: String
  private lateinit var units: String
  private lateinit var CURRENCY_UNITS: JSONObject
  /** @return index of pattern in s or -1, if not found
   */
  fun indexOf(pattern: Pattern, s: String): Int {
    val matcher = pattern.matcher(s)
    return if (matcher.find()) matcher.start() else -1
  }

  override fun getPendingAnswer(): UserAnswer {
    val userAnswerBuilder = UserAnswer.newBuilder()
    if (text.isNotEmpty()) {
      val answerTextString = text.toString()
      userAnswerBuilder.answer = InteractionObject.newBuilder()
        .setNumberWithUnits(getNumberWithUnits(text.toString()))
        .build()
      userAnswerBuilder.plainAnswer = answerTextString
    }
    return userAnswerBuilder.build()
  }

  fun getCurrencyUnits(): JSONObject {
    return JSONObject(
      "{\"dollar\":{\"name\":\"dollar\",\"aliases\":[\"$\",\"dollars\",\"Dollars\",\"Dollar\"," +
          "\"USD\"],\"front_units\":[\"$\"],\"base_unit\":null},\"rupee\":{\"name\":\"rupee\",\"aliases\":[\"Rs\",\"rupees\"," +
          "\"\u20b9\",\"Rupees\",\"Rupee\"],\"front_units\":[\"Rs \",\"\u20b9\"],\"base_unit\":null},\"cent\":" +
          "{\"name\":\"cent\",\"aliases\":[\"cents\",\"Cents\",\"Cent\"],\"front_units\":[],\"base_unit\":\"0.01 dollar\"}," +
          "\"paise\":{\"name\":\"paise\",\"aliases\":[\"paisa\",\"Paise\",\"Paisa\"],\"front_units\":[],\"base_unit\":\"0.01 rupee\"}}"
    )
  }

  fun getNumberWithUnits(inputText: String): NumberWithUnits {
    var rawInput: String = inputText.normalizeWhitespace()
    CURRENCY_UNITS = getCurrencyUnits()
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

        var keys = (CURRENCY_UNITS).keys()
        for (i in keys) {
          for (j in 0 until CURRENCY_UNITS.getJSONObject(i).getJSONArray("front_units").length()) {
            if (Arrays.asList(CURRENCY_UNITS.getJSONObject(i).getJSONArray("front_units")[j]).indexOf(
                Arrays.asList(
                  CURRENCY_UNITS.getJSONObject(i).getJSONArray("front_units")[j]
                )
              ) != -1
            ) {
              throw  Error(
                "INVALID_CURRENCY_FORMAT"
              )
            }
          }
        }
      } else {
        var startsWithCorrectCurrencyUnit = false
        var keys = (CURRENCY_UNITS).keys()
        for (i in keys) {
          for (j in 0 until CURRENCY_UNITS.getJSONObject(i).getJSONArray("front_units").length()) {
            if (rawInput.startsWith(CURRENCY_UNITS.getJSONObject(i).getJSONArray("front_units")[j] as String)) {
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
        keys = (CURRENCY_UNITS).keys()
        for (i in keys) {
          for (j in 0 until CURRENCY_UNITS.getJSONObject(i).getJSONArray("front_units").length()) {
            if (units == (CURRENCY_UNITS.getJSONObject(i).getJSONArray("front_units")[j] as String).trim()) {
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
        real = parseDouble(value)
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

  fun getStringOfNumberWithUnits(): String {
    var numberWithUnitsString = this.value.trim()
    var unitsString = this.units.trim()
    numberWithUnitsString =
      if (Pattern.compile("^[rs,$,₹,€,£,¥]", Pattern.CASE_INSENSITIVE).matcher(text.toString()).find())
        unitsString.trim() + " " + numberWithUnitsString else numberWithUnitsString + " " + unitsString.trim()
    numberWithUnitsString = numberWithUnitsString.trim()
    return numberWithUnitsString
  }
}
