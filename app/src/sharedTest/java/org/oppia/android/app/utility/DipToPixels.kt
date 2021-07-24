package org.oppia.android.app.utility

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue

fun dipToPixels(dip: Float, context: Context): Float {
  val resources: Resources = context.resources
  return TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_PX,
    dip,
    resources.displayMetrics
  )
}
