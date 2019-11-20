package org.oppia.app.customview.interaction

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import org.oppia.app.utility.KeyboardHelper.Companion.hideSoftKeyboard
import org.oppia.app.utility.KeyboardHelper.Companion.showSoftKeyboard

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
) : EditText(context, attrs, defStyle), View.OnFocusChangeListener {
  private val hintText: String

  init {
    onFocusChangeListener = this
    hintText = (hint ?: "").toString()
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

  override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
    if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP)
      this.clearFocus()
    return super.onKeyPreIme(keyCode, event)
  }
}

