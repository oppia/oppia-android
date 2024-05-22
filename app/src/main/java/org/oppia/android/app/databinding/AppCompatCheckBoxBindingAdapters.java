package org.oppia.android.app.databinding;

import android.content.res.ColorStateList;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.widget.CompoundButtonCompat;
import androidx.databinding.BindingAdapter;

/**
 * Custom data-binding adapters for {@link AppCompatCheckBox}s.
 */
public final class AppCompatCheckBoxBindingAdapters {
  /** Sets the button tint for the specified checkbox, via data-binding. */
  @BindingAdapter("buttonTint")
  public static void setButtonTint(@NonNull AppCompatCheckBox checkBox, @ColorInt int colorRgb) {
    CompoundButtonCompat.setButtonTintList(checkBox, ColorStateList.valueOf(colorRgb));
  }
}
