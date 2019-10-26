package org.oppia.app.customview.interaction

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import org.oppia.app.model.InteractionObject

// TODO(#249): These are the attributes which should be defined in XML, that are required for this interaction view to work correctly
//  hint="Write here."
//  inputType="text"
//  background="@drawable/edit_text_background"
//  maxLength="200".

/** The custom EditText class for text input interaction view. */
class TextInputInteractionView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = android.R.attr.editTextStyle
) : EditText(context, attrs, defStyle)
