package org.oppia.app.customview.interaction

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.NumberUnit
import org.oppia.app.model.NumberWithUnits

/** The custom EditText class for numeric input interaction view. */
class NumberWithUnitsInputInteractionView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = android.R.attr.editTextStyle
) : EditText(context, attrs, defStyle), InteractionAnswerRetriever {

  override fun getPendingAnswer(): InteractionObject {
    return if (text.isNullOrEmpty()) (InteractionObject.newBuilder().build()) else (InteractionObject.newBuilder().setNumberWithUnits(
      NumberWithUnits.newBuilder().setReal(text.toString().substringBefore(" ").toFloat()).addUnit(NumberUnit.newBuilder().setUnit(text.toString().substringAfter(" ")))
    ).build())
  }
}
