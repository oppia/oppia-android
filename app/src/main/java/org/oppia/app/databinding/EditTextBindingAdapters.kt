package org.oppia.app.databinding

import android.text.TextWatcher
import android.widget.EditText
import androidx.databinding.BindingAdapter

/** This bindingAdapter tells the compiler how to set a TextWatcher on an EditText.  */
class EditTextBindingAdapters {

  @BindingAdapter("app:textChangedListener")
  fun bindTextWatcher(editText: EditText, textWatcher: TextWatcher) {
    editText.addTextChangedListener(textWatcher)
  }
}
