package org.oppia.app.customview.interaction

import android.app.PendingIntent.getActivity
import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_UP
import android.view.KeyEvent.KEYCODE_BACK
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import org.oppia.app.player.state.StateFragment
import org.oppia.app.player.state.StateFragmentPresenter
import org.oppia.app.player.state.listener.StateKeyboardButtonListener
import org.oppia.app.utility.KeyboardHelper.Companion.hideSoftKeyboard
import org.oppia.app.utility.KeyboardHelper.Companion.showSoftKeyboard
import javax.inject.Inject

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
  private val stateKeyboardButtonListener: StateKeyboardButtonListener

  init {
    onFocusChangeListener = this
    hintText = (hint ?: "").toString()
    stateKeyboardButtonListener = context as StateKeyboardButtonListener
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
    if (event.keyCode == KEYCODE_BACK && event.action == ACTION_UP)
      this.clearFocus()
    return super.onKeyPreIme(keyCode, event)
  }

  override fun onEditorAction(actionCode: Int) {
    if (actionCode == EditorInfo.IME_ACTION_DONE) {
      stateKeyboardButtonListener.onEditorAction(EditorInfo.IME_ACTION_DONE)
    }
    super.onEditorAction(actionCode)
  }
}
