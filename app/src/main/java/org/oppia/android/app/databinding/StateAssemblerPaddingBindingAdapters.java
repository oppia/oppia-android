package org.oppia.android.app.databinding;

import android.view.View;
import androidx.core.view.ViewCompat;
import androidx.databinding.BindingAdapter;

/**
 * Holds all custom binding adapters that set padding values for exploration view, exploration
 * split-veiw, question view, and question split-view.
 */
public final class StateAssemblerPaddingBindingAdapters {

  /** Used to set a padding for exploration split-view. */
  @BindingAdapter(
      value = {
          "explorationSplitViewPaddingApplicable",
          "explorationSplitViewPaddingStart",
          "explorationSplitViewPaddingTop",
          "explorationSplitViewPaddingEnd",
          "explorationSplitViewPaddingBottom",
      },
      requireAll = false
  )
  public static void setExplorationSplitViewPadding(
      View view,
      boolean isApplicable,
      float paddingStart,
      float paddingTop,
      float paddingEnd,
      float paddingBottom
  ) {
    setPaddingIfApplicable(view, isApplicable, paddingStart, paddingTop, paddingEnd, paddingBottom);
  }

  /** Used to set a padding for exploration view. */
  @BindingAdapter(
      value = {
          "explorationViewPaddingApplicable",
          "explorationViewPaddingStart",
          "explorationViewPaddingTop",
          "explorationViewPaddingEnd",
          "explorationViewPaddingBottom",
      },
      requireAll = false
  )
  public static void setExplorationViewPadding(
      View view,
      boolean isApplicable,
      float paddingStart,
      float paddingTop,
      float paddingEnd,
      float paddingBottom
  ) {
    setPaddingIfApplicable(view, isApplicable, paddingStart, paddingTop, paddingEnd, paddingBottom);
  }

  /** Used to set a padding for question view. */
  @BindingAdapter(
      value = {
          "questionViewPaddingApplicable",
          "questionViewPaddingStart",
          "questionViewPaddingTop",
          "questionViewPaddingEnd",
          "questionViewPaddingBottom",
      },
      requireAll = false
  )
  public static void setQuestionViewPadding(
      View view,
      boolean isApplicable,
      float paddingStart,
      float paddingTop,
      float paddingEnd,
      float paddingBottom
  ) {
    setPaddingIfApplicable(view, isApplicable, paddingStart, paddingTop, paddingEnd, paddingBottom);
  }

  /** Used to set a padding for question split-view. */
  @BindingAdapter(
      value = {
          "questionSplitViewPaddingApplicable",
          "questionSplitViewPaddingStart",
          "questionSplitViewPaddingTop",
          "questionSplitViewPaddingEnd",
          "questionSplitViewPaddingBottom",
      },
      requireAll = false
  )
  public static void setQuestionSplitViewPadding(
      View view,
      boolean isApplicable,
      float paddingStart,
      float paddingTop,
      float paddingEnd,
      float paddingBottom
  ) {
    setPaddingIfApplicable(view, isApplicable, paddingStart, paddingTop, paddingEnd, paddingBottom);
  }

  private static void setPaddingIfApplicable(
      View view,
      boolean isApplicable,
      float paddingStart,
      float paddingTop,
      float paddingEnd,
      float paddingBottom
  ) {
    if (isApplicable) {
      if (isRtlLayout(view)) {
        view.setPadding(
            (int) paddingEnd,
            (int) paddingTop,
            (int) paddingStart,
            (int) paddingBottom
        );
      } else {
        view.setPadding(
            (int) paddingStart,
            (int) paddingTop,
            (int) paddingEnd,
            (int) paddingBottom
        );
      }
      view.requestLayout();
    }
  }

  private static boolean isRtlLayout(View view) {
    return ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL;
  }
}
