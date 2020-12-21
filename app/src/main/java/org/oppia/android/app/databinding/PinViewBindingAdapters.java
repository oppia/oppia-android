package org.oppia.android.app.databinding;

import androidx.databinding.BindingAdapter;
import com.chaos.view.PinView;
import org.jetbrains.annotations.NotNull;

/** Holds all custom binding adapters that bind to [PinView]. */
public final class PinViewBindingAdapters {

  /** Binds the show/hide state of the characters of the [PinView]. */
  @BindingAdapter("android:showPassword")
  public static void setPasswordVisibility(@NotNull PinView pinView, boolean isPasswordShown) {
    pinView.setPasswordHidden(!isPasswordShown);
  }
}
