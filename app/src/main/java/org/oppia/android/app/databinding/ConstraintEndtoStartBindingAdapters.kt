package org.oppia.android.app.databinding

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.BindingAdapter

class ConstraintEndtoStartBindingAdapters {

  @BindingAdapter(
    "layout_conditionalConstraint_startSide",
    "layout_conditionalConstraint_toEndId",
    "layout_conditionalConstraint_endSide"
  )
  fun setConditionalConstraint(
    view: View, startSide: Int, endId: Int, endSide: Int
  ) {
    val constraintLayout = (view.parent as? ConstraintLayout) ?: return
    with(ConstraintSet()) {
      clone(constraintLayout)
      connect(view.id, startSide, endId, endSide)
      applyTo(constraintLayout)
    }
  }
}