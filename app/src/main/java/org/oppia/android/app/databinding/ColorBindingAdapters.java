package org.oppia.android.app.databinding;

import android.view.View;
import androidx.databinding.BindingAdapter;

/** Holds all custom binding adapters that set color values. */
public final class ColorBindingAdapters {

  /** Binding adapter for setting the `customBackgroundColor` for a [View]. */
  @BindingAdapter("customBackgroundColor")
  public static void setCustomBackgroundColor(View view, int color) {
    view.setBackgroundColor(color);
  }
}
