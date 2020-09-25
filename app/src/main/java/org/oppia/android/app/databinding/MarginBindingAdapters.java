package org.oppia.android.app.databinding;

import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;

/** Holds all custom binding adapters that set margin values. */
public final class MarginBindingAdapters {

  /** Used to set a margin-start for views. */
  @BindingAdapter("app:layoutMarginStart")
  public static void setLayoutMarginStart(@NonNull View view, float marginStart) {
    if (view.getLayoutParams() instanceof MarginLayoutParams) {
      MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
      params.setMargins(
          (int) marginStart,
          params.topMargin,
          params.getMarginEnd(),
          params.bottomMargin
      );
      view.requestLayout();
    }
  }

  /** Used to set a margin-end for views. */
  @BindingAdapter("app:layoutMarginEnd")
  public static void setLayoutMarginEnd(@NonNull View view, float marginEnd) {
    if (view.getLayoutParams() instanceof MarginLayoutParams) {
      MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
      params.setMargins(
          params.getMarginStart(),
          params.topMargin,
          (int) marginEnd,
          params.bottomMargin
      );
      view.requestLayout();
    }
  }

  /** Used to set a margin-top for views. */
  @BindingAdapter("app:layoutMarginTop")
  public static void setLayoutMarginTop(@NonNull View view, float marginTop) {
    if (view.getLayoutParams() instanceof MarginLayoutParams) {
      MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
      params.setMargins(
          params.getMarginStart(),
          (int) marginTop,
          params.getMarginEnd(),
          params.bottomMargin
      );
      view.requestLayout();
    }
  }

  /** Used to set a margin-bottom for views. */
  @BindingAdapter("app:layoutMarginBottom")
  public static void setLayoutMarginBottom(@NonNull View view, float marginBottom) {
    if (view.getLayoutParams() instanceof MarginLayoutParams) {
      MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
      params.setMargins(
          params.getMarginStart(),
          params.topMargin,
          params.getMarginEnd(),
          (int) marginBottom
      );
      view.requestLayout();
    }
  }

  /** Used to set a margin for views. */
  @BindingAdapter("app:layoutMargin")
  public static void setLayoutMargin(@NonNull View view, float margin) {
    if (view.getLayoutParams() instanceof MarginLayoutParams) {
      MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
      params.setMargins(
          (int) margin,
          (int) margin,
          (int) margin,
          (int) margin
      );
      view.requestLayout();
    }
  }
}
