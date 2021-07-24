package org.oppia.android.app.utility

import android.content.res.Resources
import android.util.TypedValue

fun dipToPixels(dip: Float): Float {
  val resources: Resources = context.resources
  return TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_PX,
    dip,
    resources.displayMetrics
  )
}