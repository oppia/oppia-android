package org.oppia.app.databinding;

import androidx.constraintlayout.widget.Guideline;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.BindingAdapter;
import org.jetbrains.annotations.NotNull;

public class GuidelineBinding {
  /** Binding adapter for setting the `guidePercent` for a [Guideline]. */
  @BindingAdapter("app:layout_constraintGuide_percent")
  public static void setGuidelinePercentage(@NotNull Guideline guideline, Float percentage) {
    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) guideline.getLayoutParams();
    params.guidePercent = percentage;
    guideline.setLayoutParams(params);
  }
}
