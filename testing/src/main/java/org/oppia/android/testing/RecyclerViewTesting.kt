package org.oppia.android.testing

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import org.oppia.android.testing.RecyclerViewMatcher.Companion.atPositionOnView

class RecyclerViewTesting {
  companion object {
    fun verifyItemDisplayedOnRecyclerView(
      recyclerView: Int,
      itemPosition: Int,
      targetView: Int
    ) {
      onView(
        atPositionOnView(
          recyclerViewId = recyclerView,
          position = itemPosition,
          targetViewId = targetView
        )
      ).check(matches(isDisplayed()))
    }
  }
}
