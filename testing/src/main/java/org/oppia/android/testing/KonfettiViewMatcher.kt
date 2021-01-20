package org.oppia.android.testing

import android.view.View
import nl.dionsegijn.konfetti.KonfettiView
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class KonfettiViewMatcher {
  companion object {
    // Returns a matcher that matches for active confetti.
    fun hasActiveConfetti(): TypeSafeMatcher<View> {
      return ActiveConfettiMatcher()
    }

    // Custom class to check if a KonfettiView isActive().
    private class ActiveConfettiMatcher() : TypeSafeMatcher<View>() {
      override fun describeTo(description: Description) {
        description.appendText("KonfettiView with active confetti animation")
      }

      override fun matchesSafely(view: View): Boolean {
        return view is KonfettiView && view.isActive()
      }
    }
  }
}
