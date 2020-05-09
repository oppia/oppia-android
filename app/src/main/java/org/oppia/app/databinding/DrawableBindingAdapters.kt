package org.oppia.app.databinding

import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.annotation.ColorInt
import androidx.databinding.BindingAdapter
import org.oppia.app.R

/** Used to set a rounded-rect background drawable with a data-bound color. */
@BindingAdapter("app:roundedRectDrawableWithColor")
fun setBackgroundDrawable(view: View, @ColorInt colorRgb: Int) {
  view.setBackgroundResource(R.drawable.rounded_rect_background)
  // The input color needs to have alpha channel prepended to it.
  (view.background as GradientDrawable).setColor((0xff000000 or colorRgb.toLong()).toInt())
}

@BindingAdapter("app:topRoundedRectDrawableWithColor")
fun setTopBackgroundDrawable(view: View, @ColorInt colorRgb: Int) {
  view.setBackgroundResource(R.drawable.top_rounded_rect_background)
  // The input color needs to have alpha channel prepended to it.
  (view.background as GradientDrawable).setColor((0xff000000 or colorRgb.toLong()).toInt())
}

@BindingAdapter("app:bottomRoundedRectDrawableWithColor")
fun setBottomBackgroundDrawable(view: View, @ColorInt colorRgb: Int) {
  view.setBackgroundResource(R.drawable.bottom_rounded_rect_background)
  // The input color needs to have alpha channel prepended to it.
  (view.background as GradientDrawable).setColor((0xff000000 or colorRgb.toLong()).toInt())
}

@BindingAdapter("app:rectangleDrawableWithColor")
fun setRectangleBackgroundDrawable(view: View, @ColorInt colorRgb: Int) {
  view.setBackgroundResource(R.drawable.rectangle_background)
  // The input color needs to have alpha channel prepended to it.
  (view.background as GradientDrawable).setColor((0xff000000 or colorRgb.toLong()).toInt())
}
