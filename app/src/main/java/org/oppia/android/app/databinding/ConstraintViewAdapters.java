package org.oppia.android.app.databinding;

import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.databinding.BindingAdapter;

public class ConstraintViewAdapters {
  @BindingAdapter(
      value = {
          "app:layout_conditionalConstraint_startSide",
          "app:layout_conditionalConstraint_toEndId",
          "app:layout_conditionalConstraint_endSide",
          "app:layout_conditionalConstraint_condition"
      }
  )
  public static void setConditionalConstraint(
      View view, int startSide, int endId, int endSide, boolean condition
  ) {
    ConstraintLayout layout = (ConstraintLayout) view.getParent();
    if (layout == null) return;
    ConstraintSet set = new ConstraintSet();
    set.clone(layout);
    if (condition)
      set.connect(view.getId(), startSide, endId, endSide);
    else
      set.connect(view.getId(), startSide, layout.getId(), ConstraintSet.END);
    set.applyTo(layout);

  }
}

