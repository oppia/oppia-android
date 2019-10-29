package org.oppia.app.player.state.itemviewmodel

import androidx.lifecycle.ViewModel
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.NumberUnit
import org.oppia.app.model.NumberWithUnits
import org.oppia.app.parser.StringToFractionParser
import org.oppia.app.player.state.listener.InteractionAnswerRetriever
import org.oppia.domain.util.toAnswerString
import java.util.regex.Pattern

class NumberWithUnitsViewModel(
  existingAnswer: InteractionObject?, val isReadOnly: Boolean
): ViewModel(), InteractionAnswerRetriever {
  var answerText: CharSequence = existingAnswer?.toAnswerString() ?: ""

  override fun getPendingAnswer(): InteractionObject {
    var units = ""
    lateinit var value: String
    val rawInput = answerText.toString()
    val interactionObjectBuilder = InteractionObject.newBuilder()
    if (answerText.isEmpty()) {
      return interactionObjectBuilder.build()
    }
    if (Pattern.compile("[^A-Za-z(₹$€£¥]").matcher(rawInput).find()) {
      val m = Pattern.compile("[a-zA-Z(₹\$€£¥/^*)-?\\d]+|\\d+").matcher(rawInput)
      while (m.find()) {
        if (Pattern.compile("[a-zA-Z(₹\$€£¥]").matcher(m.group()).find()) {
          units = m.group()
        }
      }
      value = rawInput.replace(units, "")
    } else {
      value = rawInput
      units = ""
    }
    // TODO: implement exponent.
    if (!value.contains(".")) {
      interactionObjectBuilder.setNumberWithUnits(
        NumberWithUnits.newBuilder().setFraction(StringToFractionParser().getFractionFromString(value)).addUnit(
          NumberUnit.newBuilder().setUnit(units).setExponent(1)
        )
      )
    } else {
      interactionObjectBuilder.numberWithUnits =
        NumberWithUnits.newBuilder()
          .setReal(value.toDouble())
          .addUnit(NumberUnit.newBuilder().setUnit(units).setExponent(1))
          .build()
    }
    return interactionObjectBuilder.build()
  }
}
