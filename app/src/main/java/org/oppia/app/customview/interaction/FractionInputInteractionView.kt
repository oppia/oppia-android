package org.oppia.app.customview.interaction

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import org.oppia.app.model.Fraction
import org.oppia.app.model.InteractionObject

/** The custom EditText class for text input interaction view. */
class FractionInputInteractionView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = android.R.attr.editTextStyle
) : EditText(context, attrs, defStyle), InteractionAnswerRetriever {

  override fun getPendingAnswer(): InteractionObject {

    return if (text.isNullOrEmpty()) (InteractionObject.newBuilder().build()) else (InteractionObject.newBuilder().setFraction(
  Fraction.newBuilder().setDenominator(text.toString().substringAfterLast("/").toInt()).setNumerator(text.toString().substringBeforeLast("/").toInt())
    ).build())
  }
}
