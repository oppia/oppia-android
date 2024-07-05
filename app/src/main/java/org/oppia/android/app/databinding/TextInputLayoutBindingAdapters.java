package org.oppia.android.app.databinding;

import android.widget.AutoCompleteTextView;
import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import com.google.android.material.textfield.TextInputLayout;

/** Holds all custom binding adapters that bind to [TextInputLayout]. */
public final class TextInputLayoutBindingAdapters {

  /** Binding adapter for setting an error message. */
  @BindingAdapter("errorMessage")
  public static void setErrorMessage(
      @NonNull TextInputLayout textInputLayout,
      String errorMessage
  ) {
    textInputLayout.setError(errorMessage);
  }


  /** Binding adapter for setting the text of an [AutoCompleteTextView]. */
  @BindingAdapter({"selection", "filter"})
  public static void setSelection(
      @NonNull AutoCompleteTextView textView,
      String selectedItem,
      Boolean filter) {
    textView.setText(selectedItem, filter);
  }
}
