package org.oppia.android.app.databinding;

import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.Button;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import org.oppia.android.R;

/** Holds all custom binding adapters that set background drawables. */
public final class DrawableBindingAdapters {

  /** Used to set a rounded-rect background drawable with a data-bound color. */
  @BindingAdapter("app:roundedRectDrawableWithColor")
  public static void setBackgroundDrawable(@NonNull View view, @ColorInt int colorRgb) {
    view.setBackgroundResource(R.drawable.rounded_rect_background);
    // The input color needs to have alpha channel prepended to it.
    GradientDrawable background = (GradientDrawable) view.getBackground();
    background.setColor(0xff000000 | colorRgb);
  }

  /** Used to set a top rounded-rect background drawable with a data-bound color. */
  @BindingAdapter("app:topRoundedRectDrawableWithColor")
  public static void setTopBackgroundDrawable(@NonNull View view, @ColorInt int colorRgb) {
    view.setBackgroundResource(R.drawable.top_rounded_rect_background);
    // The input color needs to have alpha channel prepended to it.
    GradientDrawable background = (GradientDrawable) view.getBackground();
    background.setColor(0xff000000 | colorRgb);
  }

  /** Used to set a bottom rounded-rect background drawable with a data-bound color. */
  @BindingAdapter("app:bottomRoundedRectDrawableWithColor")
  public static void setBottomBackgroundDrawable(@NonNull View view, @ColorInt int colorRgb) {
    view.setBackgroundResource(R.drawable.bottom_rounded_rect_background);
    // The input color needs to have alpha channel prepended to it.
    GradientDrawable background = (GradientDrawable) view.getBackground();
    background.setColor(0xff000000 | colorRgb);
  }

  /** Used to set a rectangular background drawable with a data-bound color. */
  @BindingAdapter("app:rectangleDrawableWithColor")
  public static void setRectangleBackgroundDrawable(@NonNull View view, @ColorInt int colorRgb) {
    view.setBackgroundResource(R.drawable.rectangle_background);
    // The input color needs to have alpha channel prepended to it.
    GradientDrawable background = (GradientDrawable) view.getBackground();
    background.setColor(0xff000000 | colorRgb);
  }

  /** Used to set a resource background to a button. */
  @BindingAdapter("android:button")
  public static void setBackgroundResource(@NonNull Button button, @DrawableRes int resource) {
    button.setBackgroundResource(resource);
  }
}
