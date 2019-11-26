package org.oppia.app.customview.interaction;

import android.text.TextWatcher;
import android.widget.EditText;
import androidx.databinding.BindingAdapter;
/** This bindingAdapter tells the compiler how to set a TextWatcher on an EditText. */
public class EditTextBindingAdapters {

  @BindingAdapter("textChangedListener")
  public static void bindTextWatcher(EditText editText, TextWatcher textWatcher) {
    editText.addTextChangedListener(textWatcher);
  }
}
