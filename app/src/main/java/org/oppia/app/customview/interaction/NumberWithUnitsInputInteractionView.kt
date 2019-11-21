package org.oppia.app.customview.interaction

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import org.oppia.app.model.Fraction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.NumberUnit
import org.oppia.app.model.NumberWithUnits
import org.oppia.app.parser.StringToFractionParser
import org.oppia.domain.util.normalizeWhitespace
import java.util.regex.Pattern

/** The custom EditText class for number input with units interaction view. */
class NumberWithUnitsInputInteractionView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = android.R.attr.editTextStyle
) : EditText(context, attrs, defStyle), InteractionAnswerRetriever {
  private lateinit var type: String
  private var real: Float = 0f
  private lateinit var fractionObject: Fraction
  private lateinit var value: String
  private lateinit var units: String
  /** @return index of pattern in s or -1, if not found
   */
  fun indexOf(pattern: Pattern, s: String): Int {
    val matcher = pattern.matcher(s)
    return if (matcher.find()) matcher.start() else -1
  }
  override fun getPendingAnswer(): InteractionObject {
    val interactionObjectBuilder =
      InteractionObject.newBuilder().setNumberWithUnits(NumberWithUnits.getDefaultInstance())
    if (text.isNullOrEmpty() || isValidNumberWithUnits()) {
      return interactionObjectBuilder.build()
    }

    if (value.contains("/")) {
      interactionObjectBuilder.setNumberWithUnits(
        NumberWithUnits.newBuilder().setFraction(fractionObject).addUnit(NumberUnit.newBuilder().setUnit(units))
      )
    } else {
      interactionObjectBuilder.setNumberWithUnits(
        NumberWithUnits.newBuilder().setReal(real.toDouble()).addUnit(NumberUnit.newBuilder().setUnit(units))
      )
    }
    return interactionObjectBuilder.build()
  }
 fun NumberWithUnits(inputText: String){
   var rawInput: String = inputText.normalizeWhitespace()

   rawInput = rawInput.trim();
    var type = ""
    var real = 0.0
    // Default fraction value.
    var fractionObj = Fraction.newBuilder().setDenominator(1)
    var units = ""
    var value = ""
    var unitObj = []

    // Allow validation only when rawInput is not null or an empty string.
    if (rawInput.isNotEmpty() && rawInput != null) {
      // Start with digit when there is no currency unit.
      if (rawInput.matches("^\d/".toRegex())) {
        var ind = indexOf(Pattern.compile("[a-z(₹$]"),rawInput)
        if (ind === -1) {
          // There is value with no units.
          value = rawInput;
          units = ""
        } else {
          value = rawInput.substring(0, ind).trim()
          units = rawInput.substring(ind).trim()
        }

        var keys = Object.keys(CURRENCY_UNITS)
        for (var i = 0; i < keys.length; i++) {
        for (var j = 0;
        j < CURRENCY_UNITS[keys[i]].front_units.length; j++) {
        if (units.indexOf(
            CURRENCY_UNITS[keys[i]].front_units[j]) !== -1) {
          throw new Error(
              NUMBER_WITH_UNITS_PARSING_ERRORS.INVALID_CURRENCY_FORMAT);
        }
      }
      }
      } else {
        var startsWithCorrectCurrencyUnit = false;
        var keys = Object.keys(CURRENCY_UNITS);
        for (var i = 0; i < keys.length; i++) {
        for (var j = 0;
        j < CURRENCY_UNITS[keys[i]].front_units.length; j++) {
        if (rawInput.startsWith(CURRENCY_UNITS[keys[i]].front_units[j])) {
          startsWithCorrectCurrencyUnit = true;
          break;
        }
      }
      }
        if (startsWithCorrectCurrencyUnit === false) {
          throw new Error(NUMBER_WITH_UNITS_PARSING_ERRORS.INVALID_CURRENCY);
        }
        var ind = rawInput.indexOf(rawInput.match(/[0-9]/));
        if (ind === -1) {
          throw new Error(NUMBER_WITH_UNITS_PARSING_ERRORS.INVALID_CURRENCY);
        }
        units = rawInput.substr(0, ind).trim();

        startsWithCorrectCurrencyUnit = false;
        for (var i = 0; i < keys.length; i++) {
        for (var j = 0;
        j < CURRENCY_UNITS[keys[i]].front_units.length; j++) {
        if (units === CURRENCY_UNITS[keys[i]].front_units[j].trim()) {
          startsWithCorrectCurrencyUnit = true;
          break;
        }
      }
      }
        if (startsWithCorrectCurrencyUnit === false) {
          throw new Error(NUMBER_WITH_UNITS_PARSING_ERRORS.INVALID_CURRENCY);
        }
        units = units + ' ';

        var ind2 = rawInput.indexOf(
          rawInput.substr(ind).match(/[a-z(]/i));
        if (ind2 !== -1) {
          value = rawInput.substr(ind, ind2 - ind).trim();
          units += rawInput.substr(ind2).trim();
        } else {
          value = rawInput.substr(ind).trim();
          units = units.trim();
        }
      }
      // Checking invalid characters in value.
      if (value.match(/[a-z]/i) || value.match(/[*^$₹()#@]/)) {
        throw new Error(NUMBER_WITH_UNITS_PARSING_ERRORS.INVALID_VALUE);
      }

      if (value.includes('/')) {
        type = 'fraction';
        fractionObj = FractionObjectFactory.fromRawInputString(value);
      } else {
        type = 'real';
        real = parseFloat(value);
      }
      if (units !== '') {
        // Checking invalid characters in units.
        if (units.match(/[^0-9a-z/* ^()₹$-]/i)) {
            throw new Error(
              NUMBER_WITH_UNITS_PARSING_ERRORS.INVALID_UNIT_CHARS);
          }
        }
      }

      var unitsObj = UnitsObjectFactory.fromRawInputString(units);
      return new NumberWithUnits(type, real, fractionObj, unitsObj);
    };

    // TODO(ankita240796): Remove the bracket notation once Angular2 gets in.
    /* eslint-disable dot-notation */
    NumberWithUnits['fromDict'] = function(numberWithUnitsDict) {
    /* eslint-enable dot-notation */
      return new NumberWithUnits(
        numberWithUnitsDict.type,
        numberWithUnitsDict.real,
        FractionObjectFactory.fromDict(numberWithUnitsDict.fraction),
        UnitsObjectFactory.fromList(numberWithUnitsDict.units));
    };

    return NumberWithUnits;
  }
]);

  fun getStringOfNumberWithUnits(): String {
    var numberWithUnitsString = this.value.trim()
    var unitsString = this.units.trim()
    numberWithUnitsString =
      if (Pattern.compile("^[rs,$,₹,€,£,¥]", Pattern.CASE_INSENSITIVE).matcher(text.toString()).find())
        unitsString.trim() + " " + numberWithUnitsString else numberWithUnitsString + " " + unitsString.trim()
    numberWithUnitsString = numberWithUnitsString.trim()
    return numberWithUnitsString
  }

  fun isValidNumberWithUnits(): Boolean {
    var rawInput = text.toString().trim()
    // Allow validation only when rawInput is not null or an empty string.
    if (rawInput !== "" && rawInput !== null) {
      if (Pattern.compile("[A-Za-z(₹$€£¥]").matcher(rawInput).find()) {
        val m = Pattern.compile("[a-zA-Z(₹\$€£¥/^*)-?\\d]+|\\d+").matcher(rawInput)
        while (m.find()) {
          if (Pattern.compile("[a-zA-Z(₹\$€£¥]").matcher(m.group()).find()) {
            this.units = m.group()
          }
        }
        this.value = rawInput.replace(this.units, "")
      } else {
        this.value = rawInput
        this.units = ""
      }
      var startsWithCorrectCurrencyUnit =
        if (Pattern.compile("^[rs,$,₹,€,£,¥]").matcher(rawInput).find()) true else false
//      if (startsWithCorrectCurrencyUnit == false) {
//        return false
//      }
      if (startsWithCorrectCurrencyUnit && Pattern.compile("^[\\d,*,/,]").matcher(this.units).find()) {
        return false
      }
      if (Pattern.compile("[rs,$,₹,€,£,¥]").matcher(rawInput).find() && Pattern.compile("[rs,$,₹,€,£,¥]").matcher(
          rawInput
        ).start() > 0
      ) {
        return false
      }
      if (Pattern.compile("[A-Z,a-z,$,₹,*,€,£,¥,(,^,)]").matcher(value).find()) {
        return false
      }
      if (Pattern.compile("[\\D[/.]]").matcher(value).find()) {
        return false
      }
      if (Pattern.compile("/0").matcher(value).find()) {
        return false
      }
      if (value.endsWith(".") || value.endsWith("/")) {
        return false
      }
    }
    if (this.value.contains("/")) {
      this.real =  0f
      this.type="fraction"
      this.fractionObject = StringToFractionParser().getFractionFromString(this.value);
    } else {
      this.real =  this.value.toFloat()
      this.type="real"
    }
    return true
  }
}
