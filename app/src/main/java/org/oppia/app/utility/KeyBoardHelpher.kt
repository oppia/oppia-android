package org.oppia.app.utility

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

class KeyBoardHelpher {
  companion object {
    fun hideSoftKeyboard(view: View, context: Context) {
      val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
      imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    fun showSoftKeyboard(view: View, context: Context) {
      if (view.requestFocus()) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
      }
    }
  }
}
