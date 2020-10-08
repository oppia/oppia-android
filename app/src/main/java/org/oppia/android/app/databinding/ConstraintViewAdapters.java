package org.oppia.android.app.databinding;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.databinding.BindingAdapter;

/** Holds all custom binding adapters that set constraints to views. */
public final class ConstraintViewAdapters {

  /** Binding adapter for setting a [layout_constraintEndToEndOf] to a view */
  @BindingAdapter("app:layout_constraintEnd_toEndOf")
  public static void setConstraintEndToEndOf(
      @NonNull View view, int constraintToId
  ) {
    ConstraintLayout constraintLayout = (ConstraintLayout) view.getParent();
    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(constraintLayout);
    constraintSet.connect(view.getId(), ConstraintSet.END, constraintToId, ConstraintSet.END);
    constraintSet.applyTo(constraintLayout);
  }

  @BindingAdapter("app:layout_constraintStart_toStartOf")
  public static void setConstraintStartToStartOf(
      @NonNull View view, boolean cond
  ) {
    ConstraintLayout constraintLayout = (ConstraintLayout) view.getParent();
    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(constraintLayout);
    if(cond) {
      constraintSet.connect(view.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
    } else {
      constraintSet.clear(view.getId(), ConstraintSet.START);
    }
    constraintSet.applyTo(constraintLayout);
  }

}

