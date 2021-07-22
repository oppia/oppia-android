package org.oppia.android.app.databinding;

import android.annotation.SuppressLint;
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
          "app:explorationSplitViewMarginApplicable",
          "app:explorationSplitViewMarginStart",
          "app:explorationSplitViewMarginTop",
          "app:explorationSplitViewMarginEnd",
          "app:explorationSplitViewMarginBottom",
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
          "app:explorationViewMarginApplicable",
          "app:explorationViewMarginStart",
          "app:explorationViewMarginTop",
          "app:explorationViewMarginEnd",
          "app:explorationViewMarginBottom",
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
          "app:questionViewMarginApplicable",
          "app:questionViewMarginStart",
          "app:questionViewMarginTop",
          "app:questionViewMarginEnd",
          "app:questionViewMarginBottom",
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
          "app:questionSplitViewMarginApplicable",
          "app:questionSplitViewMarginStart",
          "app:questionSplitViewMarginTop",
          "app:questionSplitViewMarginEnd",
          "app:questionSplitViewMarginBottom",
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

  @SuppressLint("WrongConstant")
  private static boolean isRtlLayout(View view) {
    return view.getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL;
  }
}
