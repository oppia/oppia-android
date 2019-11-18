package org.oppia.app.customview.interaction

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_UP
import android.view.KeyEvent.KEYCODE_BACK
import android.view.View
import android.widget.EditText
import org.oppia.app.utility.KeyboardHelper.Companion.hideSoftKeyboard
import org.oppia.app.utility.KeyboardHelper.Companion.showSoftKeyboard

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
) : EditText(context, attrs, defStyle), View.OnFocusChangeListener {
  private val hintText: String

  init {
    onFocusChangeListener = this
    hintText = hint.toString()
  }

  override fun onFocusChange(v: View, hasFocus: Boolean) = if (hasFocus) {
    hint = ""
    typeface = Typeface.DEFAULT
    showSoftKeyboard(v, context)
  } else {
    hint = hintText
    if (text.isEmpty()) setTypeface(typeface, Typeface.ITALIC)
    hideSoftKeyboard(v, context)
  }

  override fun onKeyPreIme(key_code: Int, event: KeyEvent): Boolean {
    if (event.keyCode == KEYCODE_BACK && event.action == ACTION_UP)
      this.clearFocus()
    return super.onKeyPreIme(key_code, event)
  }
}

