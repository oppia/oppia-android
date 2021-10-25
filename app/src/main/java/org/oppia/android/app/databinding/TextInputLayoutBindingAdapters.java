package org.oppia.android.app.databinding;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import com.google.android.material.textfield.TextInputLayout;

/** Holds all custom binding adapters that bind to [TextInputLayout]. */
public final class TextInputLayoutBindingAdapters {

  /** Binding adapter for setting an error message. */
  @BindingAdapter("app:errorMessage")
  public static void setErrorMessage(
      @NonNull TextInputLayout textInputLayout,
      String errorMessage
  ) {
    textInputLayout.setError(errorMessage);
  }
}
