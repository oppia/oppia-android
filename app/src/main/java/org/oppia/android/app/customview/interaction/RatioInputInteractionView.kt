package org.oppia.android.app.customview.interaction

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.AppCompatEditText
import org.oppia.android.app.player.state.listener.StateKeyboardButtonListener
import org.oppia.android.app.utility.KeyboardHelper

/** The custom AppCompatEditText class for ratio input interaction view. */
class RatioInputInteractionView @JvmOverloads constructor(
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
    KeyboardHelper.showSoftKeyboard(v, context)
  } else {
    restoreHint()
    KeyboardHelper.hideSoftKeyboard(v, context)
  }

  override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
    if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
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
