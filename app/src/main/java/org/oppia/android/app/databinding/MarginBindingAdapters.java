package org.oppia.android.app.databinding;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.databinding.BindingAdapter;

/** Holds all custom binding adapters that set margin values. */
public final class MarginBindingAdapters {

  /** Sets the start margin for a view, accounting for RTL scenarios. */
  @BindingAdapter("app:layoutMarginStart")
  public static void setLayoutMarginStart(@NonNull View view, float marginStart) {
    if (view.getLayoutParams() instanceof MarginLayoutParams) {
      MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
      float marginEnd = params.getMarginEnd();
      if (isRtlLayout(view)) {
        setLayoutDirectionalMargins(view, (int) marginEnd, (int) marginStart);
      } else {
        setLayoutDirectionalMargins(view, (int) marginStart, (int) marginEnd);
      }
      view.requestLayout();
    }
  }

  /** Sets the end margin for a view, accounting for RTL scenarios. */
  @BindingAdapter("app:layoutMarginEnd")
  public static void setLayoutMarginEnd(@NonNull View view, float marginEnd) {
    if (view.getLayoutParams() instanceof MarginLayoutParams) {
      MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
      float marginStart = params.getMarginStart();
      if (isRtlLayout(view)) {
        setLayoutDirectionalMargins(view, (int) marginEnd, (int) marginStart);
      } else {
        setLayoutDirectionalMargins(view, (int) marginStart, (int) marginEnd);
      }
      view.requestLayout();
    }
  }

  private static void setLayoutDirectionalMargins(
      @NonNull View view,
      int marginStart,
      int marginEnd
  ) {
    MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
    params.setMargins(
        marginStart,
        params.topMargin,
        marginEnd,
        params.bottomMargin
    );
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

  @SuppressLint("WrongConstant")
  private static boolean isRtlLayout(View view) {
    return view.getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL;
  }
}
