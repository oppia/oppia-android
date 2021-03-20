package org.oppia.android.testing

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView

class VerifyItemDisplayedInRecyclerView {
  companion object {
    private fun verifyItemDisplayedOnRecyclerView(
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
