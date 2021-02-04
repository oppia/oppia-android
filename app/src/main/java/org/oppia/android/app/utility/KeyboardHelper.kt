package org.oppia.android.app.utility

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

/** KeyboardHelper helps to change the visibility of the soft input keyboard. */
class KeyboardHelper {
  companion object {
    /**
     * This method hides the soft input keyboard.
     *
     * @param view the input view
     * @param context the activity context
     */
    fun hideSoftKeyboard(view: View, context: Context) {
      val imm = context.getSystemService(
        Context.INPUT_METHOD_SERVICE
      ) as InputMethodManager
      imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    /**
     * This method shows the soft input keyboard.
     *
     * @param view the input view
     * @param context the activity context
     */
    fun showSoftKeyboard(view: View, context: Context) {
      if (view.requestFocus()) {
        val imm = context.getSystemService(
          Context.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
      }
    }
  }
}
