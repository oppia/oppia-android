package org.oppia.android.app.databinding;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.BindingAdapter;

public class ConstraintEndtoStartBindingAdapters {

  @BindingAdapter("app:layout_constraintEnd_toStartOf")
  public static void setConstraintEndToStartOf(@NonNull View view, boolean isCorrect) {
    if(isCorrect) {
    } else {

    }
  }

  @BindingAdapter(
      "app:layout_constraintEnd_toStartOf"
  )
  public static void setConditionalConstraint(
      @NonNull View view, int id, boolean isCorrect
  ) {
    Textview textview = (view.parent as? ConstraintLayout) ?: return
        with(ConstraintSet()) {
      clone(constraintLayout)
      if (condition) connect(view.id, startSide, endId, endSide)
      else clear(view.id, startSide)
      applyTo(constraintLayout)
    }
  }
}
