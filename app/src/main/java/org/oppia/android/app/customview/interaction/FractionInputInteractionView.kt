package org.oppia.android.app.customview.interaction

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_UP
import android.view.KeyEvent.KEYCODE_BACK
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.AppCompatEditText
import org.oppia.android.app.player.state.listener.StateKeyboardButtonListener
import org.oppia.android.app.utility.KeyboardHelper.Companion.hideSoftKeyboard
import org.oppia.android.app.utility.KeyboardHelper.Companion.showSoftKeyboard

// TODO(#249): These are the attributes which should be defined in XML, that are required for this interaction view to work correctly
//  digits="0123456789/-"
//  hint="Write fraction here."
//  inputType="text"
//  background="@drawable/edit_text_background"
//  maxLength="200".

/** The custom AppCompatEditText class for fraction input interaction view. */
class FractionInputInteractionView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyle), View.OnFocusChangeListener {
  private var hintText: CharSequence = ""
  private val stateKeyboardButtonListener: StateKeyboardButtonListener

  init {
    onFocusChangeListener = this
    // Assume multi-line for the purpose of properly showing long hints.
    isSingleLine = hint != null
    stateKeyboardButtonListener = context as StateKeyboardButtonListener
  }

  // TODO(#4574): Add tests to verify that the placeholder correctly shows/doesn’t show when expected
  override fun onFocusChange(v: View, hasFocus: Boolean) = if (hasFocus) {
    hintText = hint
    hideHint()
    showSoftKeyboard(v, context)
  } else {
    restoreHint()
    hideSoftKeyboard(v, context)
  }

  override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
    if (event.keyCode == KEYCODE_BACK && event.action == ACTION_UP) {
      clearFocus()
      restoreHint()
    }
    return super.onKeyPreIme(keyCode, event)
  }

  override fun onEditorAction(actionCode: Int) {
    if (actionCode == EditorInfo.IME_ACTION_DONE) {
      stateKeyboardButtonListener.onEditorAction(EditorInfo.IME_ACTION_DONE)
    }
    super.onEditorAction(actionCode)
  }

  private fun hideHint() {
    hint = ""
    typeface = Typeface.DEFAULT
    isSingleLine = true
  }

  private fun restoreHint() {
    hint = hintText
    if (text?.isEmpty() == true) setTypeface(typeface, Typeface.ITALIC)
    isSingleLine = false
  }
}
