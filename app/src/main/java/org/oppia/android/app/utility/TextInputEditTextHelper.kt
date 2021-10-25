package org.oppia.android.app.utility

import android.text.Editable
import android.text.TextWatcher
import com.google.android.material.textfield.TextInputEditText

/** TextViewHelper helps to set the [TextChangedListener] to [TextInputEditText] or any [EditText]. */
class TextInputEditTextHelper {
  companion object {
    /**
     * This method helps to perform action on [onTextChanged] function of [TextChangedListener]
     * @param onTextChanged it is a lambda function
     */
    fun TextInputEditText.onTextChanged(onTextChanged: (String?) -> Unit) {
      this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(editable: Editable?) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
          onTextChanged.invoke(s.toString())
        }
      })
    }
  }
}
