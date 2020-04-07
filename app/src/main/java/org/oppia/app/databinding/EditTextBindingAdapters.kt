package org.oppia.app.databinding

import android.text.TextWatcher
import android.widget.EditText
import androidx.databinding.BindingAdapter

/** Binding adapter for setting a [TextWatcher] as a change listener for an [EditText]. */
@BindingAdapter("app:textChangedListener")
fun bindTextWatcher(editText: EditText, textWatcher: TextWatcher) {
  editText.addTextChangedListener(textWatcher)
}
