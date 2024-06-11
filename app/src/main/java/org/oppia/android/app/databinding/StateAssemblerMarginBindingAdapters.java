package org.oppia.android.app.databinding;

import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import androidx.core.view.ViewCompat;
import androidx.databinding.BindingAdapter;

/**
 * Holds all custom binding adapters that set margin values for exploration view, exploration
 * split-view, question view, and question split-view.
 */
public final class StateAssemblerMarginBindingAdapters {

  /** Used to set a margin for exploration split-view. */
  @BindingAdapter(
      value = {
          "explorationSplitViewMarginApplicable",
          "explorationSplitViewMarginStart",
          "explorationSplitViewMarginTop",
          "explorationSplitViewMarginEnd",
          "explorationSplitViewMarginBottom",
      },
      requireAll = false
  )
  public static void setExplorationSplitViewMargin(
      View view,
      boolean isApplicable,
      float marginStart,
      float marginTop,
      float marginEnd,
      float marginBottom
  ) {
    setMarginIfApplicable(view, isApplicable, marginStart, marginTop, marginEnd, marginBottom);
  }

  /** Used to set a margin for exploration view. */
  @BindingAdapter(
      value = {
          "explorationViewMarginApplicable",
          "explorationViewMarginStart",
          "explorationViewMarginTop",
          "explorationViewMarginEnd",
          "explorationViewMarginBottom",
      },
      requireAll = false
  )
  public static void setExplorationViewMargin(
      View view,
      boolean isApplicable,
      float marginStart,
      float marginTop,
      float marginEnd,
      float marginBottom
  ) {
    setMarginIfApplicable(view, isApplicable, marginStart, marginTop, marginEnd, marginBottom);
  }

  /** Used to set a margin for question view. */
  @BindingAdapter(
      value = {
          "questionViewMarginApplicable",
          "questionViewMarginStart",
          "questionViewMarginTop",
          "questionViewMarginEnd",
          "questionViewMarginBottom",
      },
      requireAll = false
  )
  public static void setQuestionViewMargin(
      View view,
      boolean isApplicable,
      float marginStart,
      float marginTop,
      float marginEnd,
      float marginBottom
  ) {
    setMarginIfApplicable(view, isApplicable, marginStart, marginTop, marginEnd, marginBottom);
  }

  /** Used to set a margin for question split-view. */
  @BindingAdapter(
      value = {
          "questionSplitViewMarginApplicable",
          "questionSplitViewMarginStart",
          "questionSplitViewMarginTop",
          "questionSplitViewMarginEnd",
          "questionSplitViewMarginBottom",
      },
      requireAll = false
  )
  public static void setQuestionSplitViewMargin(
      View view,
      boolean isApplicable,
      float marginStart,
      float marginTop,
      float marginEnd,
      float marginBottom
  ) {
    setMarginIfApplicable(view, isApplicable, marginStart, marginTop, marginEnd, marginBottom);
  }

  private static void setMarginIfApplicable(
      View view,
      boolean isApplicable,
      float marginStart,
      float marginTop,
      float marginEnd,
      float marginBottom
  ) {
    if (isApplicable && view.getLayoutParams() instanceof MarginLayoutParams) {
      MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
      if (isRtlLayout(view)) {
        params.setMargins(
            (int) marginEnd,
            (int) marginTop,
            (int) marginStart,
            (int) marginBottom
        );
      } else {
        params.setMargins(
            (int) marginStart,
            (int) marginTop,
            (int) marginEnd,
            (int) marginBottom
        );
      }
      view.requestLayout();
    }
  }

  private static boolean isRtlLayout(View view) {
    return ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL;
  }
}
