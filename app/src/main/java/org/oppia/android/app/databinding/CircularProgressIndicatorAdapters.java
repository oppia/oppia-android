package org.oppia.android.app.databinding;

import androidx.databinding.BindingAdapter;
import com.google.android.material.progressindicator.CircularProgressIndicator;

/**
 * Holds all custom binding adapters for setting properties on {@link CircularProgressIndicator}s.
 */
public final class CircularProgressIndicatorAdapters {
  /**
   * Sets the animated progress for a {@link CircularProgressIndicator}, that is, progress that will
   * be shown incrementally using an animation.
   *
   * <p>Note that progresses of 0 will be ignored. It's recommended to hide the indicator in these
   * cases, instead.
   *
   * @param indicator the {@link CircularProgressIndicator} for which to set progress
   * @param progress the numerical progress to set on the indicator (which will be displayed based
   *     on the indicator's customized minimum and maximum progress values)
   */
  @BindingAdapter("animatedProgress")
  public static void setAnimatedProgress(CircularProgressIndicator indicator, int progress) {
    if (progress > 0) {
      indicator.setProgressCompat(progress, /* animated = */ true);
    }
  }
}
