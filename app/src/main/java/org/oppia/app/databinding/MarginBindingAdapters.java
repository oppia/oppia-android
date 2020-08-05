package org.oppia.app.databinding;

import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import androidx.databinding.BindingAdapter;

import org.jetbrains.annotations.NotNull;

public final class MarginBindingAdapters {
  /** Used to set a margin-start for views. */
  @BindingAdapter("layoutMarginStart")
  public static void setLayoutMarginStart(@NotNull View view, Float marginStart) {
    if (view.getLayoutParams() instanceof MarginLayoutParams) {
      MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
      params.setMargins(marginStart.intValue(), params.topMargin, params.getMarginEnd(), params.bottomMargin);
      view.requestLayout();
    }
  }

  /** Used to set a margin-end for views. */
  @BindingAdapter("layoutMarginEnd")
  public static void setLayoutMarginEnd(@NotNull View view, Float marginEnd) {
    if (view.getLayoutParams() instanceof MarginLayoutParams) {
      MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
      params.setMargins(params.getMarginStart(), params.topMargin, marginEnd.intValue(), params.bottomMargin);
      view.requestLayout();
    }
  }

  /** Used to set a margin-top for views. */
  @BindingAdapter("layoutMarginTop")
  public static void setLayoutMarginTop(@NotNull View view, Float marginTop) {
    if (view.getLayoutParams() instanceof MarginLayoutParams) {
      MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
      params.setMargins(params.getMarginStart(), marginTop.intValue(), params.getMarginEnd(), params.bottomMargin);
      view.requestLayout();
    }
  }

  /** Used to set a margin-bottom for views. */
  @BindingAdapter("layoutMarginBottom")
  public static void setLayoutMarginBottom(@NotNull View view, Float marginBottom) {
    if (view.getLayoutParams() instanceof MarginLayoutParams) {
      MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
      params.setMargins(params.getMarginStart(), params.topMargin, params.getMarginEnd(), marginBottom.intValue());
      view.requestLayout();
    }
  }
}
