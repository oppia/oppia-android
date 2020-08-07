package org.oppia.app.databinding

import android.text.TextWatcher;
import android.widget.EditText;
import androidx.databinding.BindingAdapter;
import org.jetbrains.annotations.NotNull;

public final class EditTextBindingAdapters {
  /**
   * Binding adapter for setting a [TextWatcher] as a change listener for an [EditText].
   */
  @BindingAdapter("app:textChangedListener")
  public static void bindTextWatcher(@NotNull EditText editText, TextWatcher textWatcher) {
    editText.addTextChangedListener(textWatcher);
  }
}
