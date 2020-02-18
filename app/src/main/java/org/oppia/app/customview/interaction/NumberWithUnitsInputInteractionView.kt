package org.oppia.app.customview.interaction

import android.R
import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.UserAnswer
import org.oppia.app.parser.StringToNumberWithUnitsParser
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler

/** The custom EditText class for number input with units interaction view. */
class NumberWithUnitsInputInteractionView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = R.attr.editTextStyle
) : EditText(context, attrs, defStyle), InteractionAnswerHandler {
  private val stringToNumberWithUnitsParser: StringToNumberWithUnitsParser = StringToNumberWithUnitsParser()

  override fun getPendingAnswer(): UserAnswer {
    val userAnswerBuilder = UserAnswer.newBuilder()
    if (text.isNotEmpty()) {
      val answerTextString = text.toString()
      userAnswerBuilder.answer = InteractionObject.newBuilder()
        .setNumberWithUnits(stringToNumberWithUnitsParser.getNumberWithUnits(text.toString()))
        .build()
      userAnswerBuilder.plainAnswer = answerTextString
    }
    return userAnswerBuilder.build()
  }
}
