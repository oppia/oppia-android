package org.oppia.app.utility

import android.view.View
import android.widget.ProgressBar
import androidx.annotation.CheckResult
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description

// https://github.com/vanniktech/espresso-utils/blob/master/espresso-core-utils/src/main/java/com/vanniktech/espresso/core/utils/ProgressMatcher.java
/** This class mainly provides a custom matcher to test whether the progress is equal to some particular value in ProgressBar. */
class ProgressMatcher private constructor(private val progress: Int) :
  BoundedMatcher<View?, ProgressBar>(
    ProgressBar::class.java
  ) {
  override fun matchesSafely(progressBar: ProgressBar): Boolean {
    return progressBar.progress == progress
  }

  override fun describeTo(description: Description) {
    description.appendText("has progress: ").appendValue(progress)
  }

  companion object {
    /**
     * Matches that the given progress is displayed.
     * Example usage:
     * `onView(withId(R.id.view)).check(matches(withProgress(2)));`
     */
    @CheckResult
    fun withProgress(progress: Int): ProgressMatcher {
      return ProgressMatcher(progress)
    }
  }
}
