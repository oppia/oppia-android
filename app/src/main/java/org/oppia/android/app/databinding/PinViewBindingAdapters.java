package org.oppia.android.app.databinding;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import com.chaos.view.PinView;

/** Holds all custom binding adapters that bind to [PinView]. */
public final class PinViewBindingAdapters {

  /** Binds the show/hide state of the characters of the [PinView]. */
  @BindingAdapter("android:showPassword")
  public static void setPasswordVisibility(@NonNull PinView pinView, boolean isPasswordShown) {
    pinView.setPasswordHidden(!isPasswordShown);
  }
}
