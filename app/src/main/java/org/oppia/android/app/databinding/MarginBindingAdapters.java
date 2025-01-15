package org.oppia.android.app.databinding;

import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import androidx.annotation.NonNull;
import androidx.core.view.MarginLayoutParamsCompat;
import androidx.databinding.BindingAdapter;

/** Holds all custom binding adapters that set margin values. */
public final class MarginBindingAdapters {

  /** Sets the start margin for a view, accounting for RTL scenarios. */
  @BindingAdapter("layoutMarginStart")
  public static void setLayoutMarginStart(@NonNull View view, float marginStart) {
    if (view.getLayoutParams() instanceof MarginLayoutParams) {
      MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
      MarginLayoutParamsCompat.setMarginStart(params, (int) marginStart);
      view.setLayoutParams(params);
    }
  }

  /** Sets the end margin for a view, accounting for RTL scenarios. */
  @BindingAdapter("layoutMarginEnd")
  public static void setLayoutMarginEnd(@NonNull View view, float marginEnd) {
    if (view.getLayoutParams() instanceof MarginLayoutParams) {
      MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
      MarginLayoutParamsCompat.setMarginEnd(params, (int) marginEnd);
      view.setLayoutParams(params);
    }
  }

  /** Used to set a margin-top for views. */
  @BindingAdapter("layoutMarginTop")
  public static void setLayoutMarginTop(@NonNull View view, float marginTop) {
    if (view.getLayoutParams() instanceof MarginLayoutParams) {
      MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
      params.topMargin = (int) marginTop;
      view.setLayoutParams(params);
    }
  }

  /** Used to set a margin-bottom for views. */
  @BindingAdapter("layoutMarginBottom")
  public static void setLayoutMarginBottom(@NonNull View view, float marginBottom) {
    if (view.getLayoutParams() instanceof MarginLayoutParams) {
      MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
      params.bottomMargin = (int) marginBottom;
      view.setLayoutParams(params);
    }
  }
}
