package org.oppia.app.databinding

import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.databinding.BindingAdapter

/** Used to set a margin-start for views. */
@BindingAdapter("app:layoutMarginStart")
fun setLayoutMarginStart(view: View, marginStart: Float) {
  if (view.layoutParams is MarginLayoutParams) {
    val params = view.layoutParams as MarginLayoutParams
    params.setMargins(marginStart.toInt(), params.topMargin, params.marginEnd, params.bottomMargin)
    view.requestLayout()
  }
}

/** Used to set a margin-end for views. */
@BindingAdapter("app:layoutMarginEnd")
fun setLayoutMarginEnd(view: View, marginEnd: Float) {
  if (view.layoutParams is MarginLayoutParams) {
    val params = view.layoutParams as MarginLayoutParams
    params.setMargins(params.marginStart, params.topMargin, marginEnd.toInt(), params.bottomMargin)
    view.requestLayout()
  }
}

/** Used to set a margin-top for views. */
@BindingAdapter("app:layoutMarginTop")
fun setLayoutMarginTop(view: View, marginTop: Float) {
  if (view.layoutParams is MarginLayoutParams) {
    val params = view.layoutParams as MarginLayoutParams
    params.setMargins(params.marginStart, marginTop.toInt(), params.marginEnd, params.bottomMargin)
    view.requestLayout()
  }
}

/** Used to set a margin-bottom for views. */
@BindingAdapter("app:layoutMarginBottom")
fun setLayoutMarginBottom(view: View, marginBottom: Float) {
  if (view.layoutParams is MarginLayoutParams) {
    val params = view.layoutParams as MarginLayoutParams
    params.setMargins(params.marginStart, params.topMargin, params.marginEnd, marginBottom.toInt())
    view.requestLayout()
  }
}

/** Used to set a margin for views. */
@BindingAdapter("app:layoutMargin")
fun setLayoutMargin(view: View, margin: Float) {
  if (view.layoutParams is MarginLayoutParams) {
    val params = view.layoutParams as MarginLayoutParams
    params.setMargins(margin.toInt(), margin.toInt(), margin.toInt(), margin.toInt())
    view.requestLayout()
  }
}
