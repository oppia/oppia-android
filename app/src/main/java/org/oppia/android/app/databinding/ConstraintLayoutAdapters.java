package org.oppia.android.app.databinding;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.databinding.BindingAdapter;

/** Custom binding adapters that set constraints to views. */
public final class ConstraintLayoutAdapters {

  /** Binding adapter for setting a layout_constraintEnd_toEndOf to a view. */
  @BindingAdapter("app:layout_constraintEnd_toEndOf")
  public static void setConstraintEndToEndOf(@NonNull View view, int constraintToId) {
    ConstraintLayout constraintLayout = (ConstraintLayout) view.getParent();
    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(constraintLayout);
    constraintSet.connect(view.getId(), ConstraintSet.END, constraintToId, ConstraintSet.END);
    constraintSet.applyTo(constraintLayout);
  }

  /** Binding adapter for setting layout_constraintHorizontal_bias to a view. */
  @BindingAdapter("app:layout_constraintHorizontal_bias")
  public static void setHorizontalBias(@NonNull View view, float value) {
    ConstraintLayout constraintLayout = (ConstraintLayout) view.getParent();
    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(constraintLayout);
    constraintSet.setHorizontalBias(view.getId(), value);
    constraintSet.applyTo(constraintLayout);
  }
}
