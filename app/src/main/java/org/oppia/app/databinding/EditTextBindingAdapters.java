package org.oppia.app.databinding;

import android.text.TextWatcher;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;

/** Holds all custom binding adapters that bind to [EditText]. */
public final class EditTextBindingAdapters {

  /** Binding adapter for setting a [TextWatcher] as a change listener for an [EditText] */
  @BindingAdapter("app:textChangedListener")
  public static void bindTextWatcher(@NonNull EditText editText, TextWatcher textWatcher) {
    editText.addTextChangedListener(textWatcher);
  }
}
