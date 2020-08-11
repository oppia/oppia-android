package org.oppia.app.databinding;

import android.view.View;
import androidx.databinding.BindingAdapter;

/**
 * Holds all the custom binding adapters that set padding values for exploration view, exploration
 * split-veiw, question view, and question split-view.
 */
public final class StateAssemblerPaddingBindingAdapters {

  /** Used to set a padding for exploration split-view. */
  @BindingAdapter(
      value = {
          "app:explorationSplitViewPaddingApplicable",
          "app:explorationSplitViewPaddingStart",
          "app:explorationSplitViewPaddingTop",
          "app:explorationSplitViewPaddingEnd",
          "app:explorationSplitViewPaddingBottom",
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
          "app:explorationViewPaddingApplicable",
          "app:explorationViewPaddingStart",
          "app:explorationViewPaddingTop",
          "app:explorationViewPaddingEnd",
          "app:explorationViewPaddingBottom",
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
          "app:questionViewPaddingApplicable",
          "app:questionViewPaddingStart",
          "app:questionViewPaddingTop",
          "app:questionViewPaddingEnd",
          "app:questionViewPaddingBottom",
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
          "app:questionSplitViewPaddingApplicable",
          "app:questionSplitViewPaddingStart",
          "app:questionSplitViewPaddingTop",
          "app:questionSplitViewPaddingEnd",
          "app:questionSplitViewPaddingBottom",
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
      view.setPadding(
          (int) paddingStart,
          (int) paddingTop,
          (int) paddingEnd,
          (int) paddingBottom
      );
      view.requestLayout();
    }
  }
}
