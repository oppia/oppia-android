package org.oppia.app.customview.interaction

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.widget.EditText

// TODO(#249): These are the attributes which should be defined in XML, that are required for this interaction view to work correctly
//  digits="0123456789/-"
//  hint="Write fraction here."
//  inputType="date"
//  background="@drawable/edit_text_background"
//  maxLength="200".

/** The custom EditText class for fraction input interaction view. */
class FractionInputInteractionView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = android.R.attr.editTextStyle
) : EditText(context, attrs, defStyle), View.OnFocusChangeListener {
  private val hintText: String

  init {
    onFocusChangeListener = this
    hintText = hint.toString()
  }

  override fun onFocusChange(v: View?, hasFocus: Boolean) = if (hasFocus) {
    hint = ""
    typeface = Typeface.DEFAULT
  } else {
    hint = hintText
    setTypeface(typeface, Typeface.ITALIC)
  }
}
