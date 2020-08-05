package org.oppia.app.databinding;

import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import androidx.databinding.BindingAdapter;

public class StateAssemblerMarginBindingAdapters {
  /** Used to set a margin for exploration split-view. */
  @BindingAdapter(
    "explorationSplitViewMarginApplicable",
    "explorationSplitViewMarginStart",
    "explorationSplitViewMarginTop",
    "explorationSplitViewMarginEnd",
    "explorationSplitViewMarginBottom",
    requireAll = false
  )
  public static void  setExplorationSplitViewMargin(
    View view,
    Boolean isApplicable,
    Float marginStart,
    Float marginTop,
    Float marginEnd,
    Float marginBottom
  ) {
    setMarginIfApplicable(view, isApplicable, marginStart, marginTop, marginEnd, marginBottom);
  }

  /** Used to set a margin for exploration view. */
  @BindingAdapter(
    "explorationViewMarginApplicable",
    "explorationViewMarginStart",
    "explorationViewMarginTop",
    "explorationViewMarginEnd",
    "explorationViewMarginBottom",
    requireAll = false
  )
  public static void  setExplorationViewMargin(
    View view,
    Boolean isApplicable,
    Float marginStart,
    Float marginTop,
    Float marginEnd,
    Float marginBottom
  ) {
    setMarginIfApplicable(view, isApplicable, marginStart, marginTop, marginEnd, marginBottom);
  }

  /** Used to set a margin for question view. */
  @BindingAdapter(
    "questionViewMarginApplicable",
    "questionViewMarginStart",
    "questionViewMarginTop",
    "questionViewMarginEnd",
    "questionViewMarginBottom",
    requireAll = false
  )
  public static void  setQuestionViewMargin(
    View view,
    Boolean isApplicable,
    Float marginStart,
    Float marginTop,
    Float marginEnd,
    Float marginBottom
  ) {
    setMarginIfApplicable(view, isApplicable, marginStart, marginTop, marginEnd, marginBottom);
  }

  /** Used to set a margin for question split-view. */
  @BindingAdapter(
    "questionSplitViewMarginApplicable",
    "questionSplitViewMarginStart",
    "questionSplitViewMarginTop",
    "questionSplitViewMarginEnd",
    "questionSplitViewMarginBottom",
    requireAll = false
  )
  public static void  setQuestionSplitViewMargin(
    View view,
    Boolean isApplicable,
    Float marginStart,
    Float marginTop,
    Float marginEnd,
    Float marginBottom
  ) {
    setMarginIfApplicable(view, isApplicable, marginStart, marginTop, marginEnd, marginBottom);
  }

  private static void  setMarginIfApplicable(
    View view,
    Boolean isApplicable,
    Float marginStart,
    Float marginTop,
    Float marginEnd,
    Float marginBottom
  ) {
    if (isApplicable && view.getLayoutParams() instanceof MarginLayoutParams) {
      MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
      params.setMargins(
          marginStart.intValue(),
          marginTop.intValue(),
          marginEnd.intValue(),
          marginBottom.intValue()
      );
      view.requestLayout();
    }
  }
}
