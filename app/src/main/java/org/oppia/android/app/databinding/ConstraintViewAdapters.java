package org.oppia.android.app.databinding;

import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.databinding.BindingAdapter;

/** Holds all custom binding adapters that set constraints to views. */

public class ConstraintViewAdapters {

  /** Binding adapter for setting a [layout_constraintEndToEndOf] to a view */
  @BindingAdapter("app:layout_constraintEnd_toEndOf")
  public static void setConstraintEndToEndOf(
      View view, int constraintToId
  ) {
    ConstraintLayout constraintLayout = (ConstraintLayout) view.getParent();
    if (constraintLayout == null) {
      return;
    }
    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(constraintLayout);
    constraintSet.connect(view.getId(), ConstraintSet.END, constraintToId, ConstraintSet.END);
    constraintSet.applyTo(constraintLayout);
  }
}

