package org.oppia.android.app.databinding;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.databinding.BindingAdapter;

/** Holds all custom binding adapters that bind to [Guideline]. */
public final class GuidelineBindingAdapters {

  /** Binding adapter for setting the `guidePercent` for a [Guideline]. */
  @BindingAdapter("app:layout_constraintGuide_percent")
  public static void setGuidelinePercentage(@NonNull Guideline guideline, float percentage) {
    ConstraintLayout.LayoutParams params =
        (ConstraintLayout.LayoutParams) guideline.getLayoutParams();
    params.guidePercent = percentage;
    guideline.setLayoutParams(params);
  }

  /** Binding adapter for setting the `guideEnd` for a [Guideline]. */
  @BindingAdapter("app:layout_constraintGuide_end")
  public static void setConstraintGuidelineEnd(@NonNull Guideline guideline, float guideEndPx) {
    ConstraintLayout.LayoutParams params =
        (ConstraintLayout.LayoutParams) guideline.getLayoutParams();
    params.guideEnd = (int) guideEndPx;
    guideline.setLayoutParams(params);
  }
}
