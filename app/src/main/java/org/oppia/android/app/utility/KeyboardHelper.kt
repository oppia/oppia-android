package org.oppia.android.app.utility

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

/** KeyboardHelper helps to change the visibility of softinputkeybord. */
class KeyboardHelper {
  companion object {
    /**
     * This method hides softinputkeybord
     * @param view is the input view
     * @param context context of the activity
     */
    fun hideSoftKeyboard(view: View, context: Context) {
      val imm = context.getSystemService(
        Context.INPUT_METHOD_SERVICE
      ) as InputMethodManager
      imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    /**
     * This method shows softinputkeybord
     * @param view is the input view
     * @param context context of the activity
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
