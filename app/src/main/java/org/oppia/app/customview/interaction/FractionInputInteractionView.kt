package org.oppia.app.customview.interaction

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import org.oppia.app.model.InteractionObject
import org.oppia.app.parser.StringToFractionParser

// TODO(#249): These are the attributes which should be defined in XML, that are required for this interaction view to work correctly
//  digits="0123456789/-"
//  hint="Write fraction here."
//  inputType="text"
//  background="@drawable/edit_text_background"
//  maxLength="200".

/** The custom EditText class for fraction input interaction view. */
class FractionInputInteractionView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = android.R.attr.editTextStyle
) : EditText(context, attrs, defStyle), InteractionAnswerRetriever {

  override fun getPendingAnswer(): InteractionObject {
    val interactionObjectBuilder = InteractionObject.newBuilder()
    if (!text.isNullOrEmpty()) {
      interactionObjectBuilder.fraction = StringToFractionParser().getFractionFromString(text = text.toString())
    }
    return interactionObjectBuilder.build()
  }
}
