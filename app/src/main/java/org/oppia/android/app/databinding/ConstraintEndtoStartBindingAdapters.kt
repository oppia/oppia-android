package org.oppia.android.app.databinding

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.BindingAdapter

class ConstraintEndtoStartBindingAdapters {

  @BindingAdapter(
    "layout_conditionalConstraint_startSide",
    "layout_conditionalConstraint_toEndId",
    "layout_conditionalConstraint_endSide",
    "layout_conditionalConstraint_condition"
  )
  fun setConditionalConstraint(
    view: View, startSide: Int, endId: Int, endSide: Int, condition: Boolean
  ) {
    val textView = (view.parent as? TextView) ?: return
    with(ConstraintSet()) {
//      clone(textView)
      if (condition) connect(view.id, startSide, endId, endSide)
      else clear(view.id, startSide)
//      applyTo(textView)
    }
  }
}