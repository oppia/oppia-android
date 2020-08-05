package org.oppia.app.databinding;

import android.view.View;
import androidx.databinding.BindingAdapter;

import org.jetbrains.annotations.NotNull;

public class StateAssemblerPaddingBindingAdapters {
  /** Used to set a padding for exploration split-view. */
  @BindingAdapter(
    "explorationSplitViewPaddingApplicable",
    "explorationSplitViewPaddingStart",
    "explorationSplitViewPaddingTop",
    "explorationSplitViewPaddingEnd",
    "explorationSplitViewPaddingBottom",
    requireAll = false
  )
  public static void setExplorationSplitViewPadding(
    View view,
    Boolean isApplicable,
    Float paddingStart,
    Float paddingTop,
    Float paddingEnd,
    Float paddingBottom
  ) {
    setPaddingIfApplicable(view, isApplicable, paddingStart, paddingTop, paddingEnd, paddingBottom);
  }

  /** Used to set a padding for exploration view. */
  @BindingAdapter(
    "explorationViewPaddingApplicable",
    "explorationViewPaddingStart",
    "explorationViewPaddingTop",
    "explorationViewPaddingEnd",
    "explorationViewPaddingBottom",
    requireAll = false
  )
  public static void setExplorationViewPadding(
    View view,
    Boolean isApplicable,
    Float paddingStart,
    Float paddingTop,
    Float paddingEnd,
    Float paddingBottom
  ) {
    setPaddingIfApplicable(view, isApplicable, paddingStart, paddingTop, paddingEnd, paddingBottom);
  }

  /** Used to set a padding for question view. */
  @BindingAdapter(
    "questionViewPaddingApplicable",
    "questionViewPaddingStart",
    "questionViewPaddingTop",
    "questionViewPaddingEnd",
    "questionViewPaddingBottom",
    requireAll = false
  )
  public static void setQuestionViewPadding(
    View view,
    Boolean isApplicable,
    Float paddingStart,
    Float paddingTop,
    Float paddingEnd,
    Float paddingBottom
  ) {
    setPaddingIfApplicable(view, isApplicable, paddingStart, paddingTop, paddingEnd, paddingBottom);
  }

  /** Used to set a padding for question split-view. */
  @BindingAdapter(
    "questionSplitViewPaddingApplicable",
    "questionSplitViewPaddingStart",
    "questionSplitViewPaddingTop",
    "questionSplitViewPaddingEnd",
    "questionSplitViewPaddingBottom",
    requireAll = false
  )
  public static void setQuestionSplitViewPadding(
    View view,
    Boolean isApplicable,
    Float paddingStart,
    Float paddingTop,
    Float paddingEnd,
    Float paddingBottom
  ) {
    setPaddingIfApplicable(view, isApplicable, paddingStart, paddingTop, paddingEnd, paddingBottom);
  }

  private static void setPaddingIfApplicable(
      View view,
      @NotNull Boolean isApplicable,
      Float paddingStart,
      Float paddingTop,
      Float paddingEnd,
      Float paddingBottom
  ) {
    if (isApplicable) {
      view.setPadding(
          paddingStart.intValue(),
          paddingTop.intValue(),
          paddingEnd.intValue(),
          paddingBottom.intValue()
      );
      view.requestLayout();
    }
  }
}
