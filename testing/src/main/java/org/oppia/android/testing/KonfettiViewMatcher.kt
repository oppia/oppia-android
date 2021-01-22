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
      var view: KonfettiView? = null

      override fun describeTo(description: Description) {
        if (view != null) {
          description.appendText(
            String.format(
              "KonfettiView with %s active confetti systems", view!!.getActiveSystems()
            )
          )
        }
      }

      override fun matchesSafely(view: View): Boolean {
        if (view !is KonfettiView) {
          return false
        }
        System.out.println("ACTIVE SYSTEMS = " + view.getActiveSystems())
        this.view = view
        return this.view!!.isActive()
      }
    }
  }
}
