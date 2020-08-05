package org.oppia.app.databinding;

import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import androidx.databinding.BindingAdapter;

public class StateAssemblerMarginBindingAdapters {
  /** Used to set a margin for exploration split-view. */
  @BindingAdapter(
    "app:explorationSplitViewMarginApplicable",
    "app:explorationSplitViewMarginStart",
    "app:explorationSplitViewMarginTop",
    "app:explorationSplitViewMarginEnd",
    "app:explorationSplitViewMarginBottom",
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
    "app:explorationViewMarginApplicable",
    "app:explorationViewMarginStart",
    "app:explorationViewMarginTop",
    "app:explorationViewMarginEnd",
    "app:explorationViewMarginBottom",
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
    "app:questionViewMarginApplicable",
    "app:questionViewMarginStart",
    "app:questionViewMarginTop",
    "app:questionViewMarginEnd",
    "app:questionViewMarginBottom",
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
    "app:questionSplitViewMarginApplicable",
    "app:questionSplitViewMarginStart",
    "app:questionSplitViewMarginTop",
    "app:questionSplitViewMarginEnd",
    "app:questionSplitViewMarginBottom",
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
