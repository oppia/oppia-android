package org.oppia.app.customview.interaction

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import org.oppia.app.model.Fraction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.NumberUnit
import org.oppia.app.model.NumberWithUnits
import org.oppia.app.parser.StringToFractionParser
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
        NumberWithUnits.newBuilder().setReal(real).addUnit(NumberUnit.newBuilder().setUnit(units))
      )
    }
    return interactionObjectBuilder.build()
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
