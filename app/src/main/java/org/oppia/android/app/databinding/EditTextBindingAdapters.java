package org.oppia.android.app.databinding;

import android.os.Build;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;

/** Holds all custom binding adapters that bind to [EditText]. */
public final class EditTextBindingAdapters {

  /** Binding adapter for setting a [TextWatcher] as a change listener for an [EditText]. */
  @BindingAdapter("textChangedListener")
  public static void bindTextWatcher(@NonNull EditText editText, TextWatcher textWatcher) {
    editText.addTextChangedListener(textWatcher);
  }

  /** Binding adapter for disabling autofill. */
  @BindingAdapter("disableAutofill")
  public static void disableAutofill(@NonNull EditText editText, boolean disable) {
    if (disable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      editText.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
    }
  }
}
