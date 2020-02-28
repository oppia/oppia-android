package org.oppia.app.utility;

import androidx.annotation.CheckResult;
import androidx.test.espresso.matcher.BoundedMatcher;
import android.view.View;
import android.widget.ProgressBar;
import org.hamcrest.Description;


public final class ProgressMatcher extends BoundedMatcher<View, ProgressBar> {
  /**
   * Matches that the given progress is displayed.
   *
   * <p>Example usage:</p>
   * <code>onView(withId(R.id.view)).check(matches(withProgress(2)));</code>
   */
  @CheckResult
  public static ProgressMatcher withProgress(final int progress) {
    return new ProgressMatcher(progress);
  }

  private final int progress;

  private ProgressMatcher(final int progress) {
    super(ProgressBar.class);

    this.progress = progress;
  }

  @Override protected boolean matchesSafely(final ProgressBar progressBar) {
    return progressBar.getProgress() == progress;
  }

  @Override public void describeTo(final Description description) {
    description.appendText("has progress: ").appendValue(progress);
  }
}
