package org.oppia.android.testing

import android.view.View
import nl.dionsegijn.konfetti.KonfettiView
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

/** A custom class that provides matching for [KonfettiView]s. */
class KonfettiViewMatcher {
  companion object {
    /**
     * This function checks that the view being checked is a [KonfettiView] with an active confetti animation.
     *
     * @returns a [TypeSafeMatcher] that checks if the given view is a [KonfettiView] that isActive with
     *          confetti
     */
    fun hasActiveConfetti(): TypeSafeMatcher<View> {
      return ActiveConfettiMatcher()
    }

    /**
     * A custom [TypeSafeMatcher] class to check that the associated view is a [KonfettiView] and isActive().
     */
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
