package org.oppia.app.customview.interaction

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import org.oppia.app.model.InteractionObject

// TODO(#249): These are the attributes which should be defined in XML, that are required for this interaction view to work correctly
//  digits="0123456789."
//  hint="Write the digit here."
//  inputType="numberDecimal"
//  background="@drawable/edit_text_background"
//  maxLength="200".

/** The custom EditText class for numeric input interaction view. */
class NumericInputInteractionView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = android.R.attr.editTextStyle
) : EditText(context, attrs, defStyle), InteractionAnswerRetriever {

  override fun getPendingAnswer(): InteractionObject {
    val interactionObjectBuilder = InteractionObject.newBuilder()
    if (!text.isNullOrEmpty()) {
      interactionObjectBuilder.setReal(text.toString().toDouble())
    }
    return interactionObjectBuilder.build()
  }
}
