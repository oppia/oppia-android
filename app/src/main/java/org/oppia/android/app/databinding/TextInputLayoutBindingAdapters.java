package org.oppia.android.app.databinding;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.view.View;
import android.widget.AutoCompleteTextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;
import com.google.android.material.textfield.TextInputLayout;
import org.oppia.android.app.model.OppiaLanguage;
import org.oppia.android.app.translation.AppLanguageActivityInjectorProvider;
import org.oppia.android.app.translation.AppLanguageResourceHandler;

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
  @BindingAdapter({"languageSelection", "filter"})
  public static void setLanguageSelection(
      @NonNull AutoCompleteTextView textView,
      @Nullable OppiaLanguage selectedItem,
      Boolean filter) {
    textView.setText(getAppLanguageResourceHandler(textView)
        .computeLocalizedDisplayName(selectedItem), filter);
  }

  private static AppLanguageResourceHandler getAppLanguageResourceHandler(View view) {
    AppLanguageActivityInjectorProvider provider =
        (AppLanguageActivityInjectorProvider) getAttachedActivity(view);
    return provider.getAppLanguageActivityInjector().getAppLanguageResourceHandler();
  }

  private static Activity getAttachedActivity(View view) {
    Context context = view.getContext();
    while (context != null && !(context instanceof Activity)) {
      if (!(context instanceof ContextWrapper)) {
        throw new IllegalStateException(
            "Encountered context in view (" + view + ") that doesn't wrap a parent context: "
                + context
        );
      }
      context = ((ContextWrapper) context).getBaseContext();
    }
    if (context == null) {
      throw new IllegalStateException("Failed to find base Activity for view: " + view);
    }
    return (Activity) context;
  }
}
