package org.oppia.app.databinding

import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.databinding.BindingAdapter

/** Used to control value of layout_width. */
@BindingAdapter("android:layout_width")
fun setLayoutWidth(view: View, isMatchParent: Boolean) {
  val params = view.layoutParams
  params.width = if (isMatchParent) MATCH_PARENT else WRAP_CONTENT
  view.layoutParams = params
}
