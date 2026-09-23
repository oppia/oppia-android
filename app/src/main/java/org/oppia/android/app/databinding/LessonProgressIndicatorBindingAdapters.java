package org.oppia.android.app.databinding;

import androidx.databinding.BindingAdapter;
import org.oppia.android.app.customview.LessonProgressIndicatorView;

/**
 * Holds binding adapters for {@link LessonProgressIndicatorView}.
 */
public final class LessonProgressIndicatorBindingAdapters {

  /**
   * Updates all lesson-progress drawing state together so binding order cannot affect animation.
   */
  @BindingAdapter(
      value = {
          "lessonProgressCompletedCount",
          "lessonProgressTotalCount",
          "isLessonProgressBetweenCheckpoints",
          "animateLessonProgress"
      },
      requireAll = true
  )
  public static void setLessonProgress(
      LessonProgressIndicatorView view,
      int completedCount,
      int totalCount,
      boolean isBetweenCheckpoints,
      boolean shouldAnimate
  ) {
    view.setLessonProgress(
        completedCount, totalCount, isBetweenCheckpoints, shouldAnimate
    );
  }

  private LessonProgressIndicatorBindingAdapters() {}
}
