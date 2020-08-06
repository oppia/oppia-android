package org.oppia.app.databinding;

import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import androidx.databinding.BindingAdapter;

public class StateAssemblerMarginBindingAdapters {
  /** Used to set a margin for exploration split-view. */
  @BindingAdapter( value = {
    "explorationSplitViewMarginApplicable",
    "explorationSplitViewMarginStart",
    "explorationSplitViewMarginTop",
    "explorationSplitViewMarginEnd",
    "explorationSplitViewMarginBottom"},
    requireAll = false
  )
  public static void  setExplorationSplitViewMargin(
    View view,
    Boolean isApplicable,
    float marginStart,
    float marginTop,
    float marginEnd,
    float marginBottom
  ) {
    setMarginIfApplicable(view, isApplicable, marginStart, marginTop, marginEnd, marginBottom);
  }

  /** Used to set a margin for exploration view. */
  @BindingAdapter( value = {
    "explorationViewMarginApplicable",
    "explorationViewMarginStart",
    "explorationViewMarginTop",
    "explorationViewMarginEnd",
    "explorationViewMarginBottom"},
    requireAll = false
  )
  public static void  setExplorationViewMargin(
    View view,
    Boolean isApplicable,
    float marginStart,
    float marginTop,
    float marginEnd,
    float marginBottom
  ) {
    setMarginIfApplicable(view, isApplicable, marginStart, marginTop, marginEnd, marginBottom);
  }

  /** Used to set a margin for question view. */
  @BindingAdapter( value = {
    "questionViewMarginApplicable",
    "questionViewMarginStart",
    "questionViewMarginTop",
    "questionViewMarginEnd",
    "questionViewMarginBottom"},
    requireAll = false
  )
  public static void  setQuestionViewMargin(
    View view,
    Boolean isApplicable,
    float marginStart,
    float marginTop,
    float marginEnd,
    float marginBottom
  ) {
    setMarginIfApplicable(view, isApplicable, marginStart, marginTop, marginEnd, marginBottom);
  }

  /** Used to set a margin for question split-view. */
  @BindingAdapter( value = {
    "questionSplitViewMarginApplicable",
    "questionSplitViewMarginStart",
    "questionSplitViewMarginTop",
    "questionSplitViewMarginEnd",
    "questionSplitViewMarginBottom"},
    requireAll = false
  )
  public static void  setQuestionSplitViewMargin(
    View view,
    Boolean isApplicable,
    float marginStart,
    float marginTop,
    float marginEnd,
    float marginBottom
  ) {
    setMarginIfApplicable(view, isApplicable, marginStart, marginTop, marginEnd, marginBottom);
  }

  private static void  setMarginIfApplicable(
    View view,
    Boolean isApplicable,
    float marginStart,
    float marginTop,
    float marginEnd,
    float marginBottom
  ) {
    if (isApplicable && view.getLayoutParams() instanceof MarginLayoutParams) {
      MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
      params.setMargins(
          (int) marginStart,
          (int) marginTop,
          (int) marginEnd,
          (int) marginBottom
      );
      view.requestLayout();
    }
  }
}
