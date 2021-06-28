package org.oppia.android.testing.espresso

import android.graphics.drawable.GradientDrawable
import android.view.View
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

// TODO(#59): Add tests for this matcher suite once testing-only activities can be added outside of
//  the app module.

/**
 * Generic Espresso view matchers. Note that view-type specific view matchers shouldn't go here,
 * only matchers that verify behaviors common to multiple types of views.
 */
class GenericViewMatchers {
  companion object {
    /**
     * Returns a [Matcher] that verifies a view has a fully opaque background. The view is expected
     * to have a [GradientDrawable] background.
     */
    fun withOpaqueBackground(): Matcher<View> = withColorBackgroundMatching(
      descriptionSuffix = "an opaque background"
    ) { color -> color?.extractAlpha() == 0xff }

    /**
     * Returns a [Matcher] with the specified description suffix and color matcher, matching against
     * filled background colors of views.
     */
    private fun withColorBackgroundMatching(
      @Suppress("SameParameterValue") descriptionSuffix: String,
      colorMatcher: (Long?) -> Boolean
    ): Matcher<View> {
      return object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description?) {
          description?.appendText("view has a background matching $descriptionSuffix")
        }

        override fun matchesSafely(view: View?): Boolean {
          checkNotNull(view) { "Expected non-null view to be matched" }
          val background = view.background
          check(background is GradientDrawable) { "Expected view to have gradient background" }

          // Picking the default color seems to be the best way to retrieve "solid filled"
          // GradientDrawable's fill colors.
          return colorMatcher(background.color?.defaultColor?.toUnsignedLong())
        }
      }
    }

    private fun Long.extractAlpha(): Int {
      return (this ushr 24).toInt()
    }

    /**
     * Returns a [Long] representation of this integer without a sign bit carried over from the
     * integer (which provides a way to work with the integer as though it's unsigned).
     */
    private fun Int.toUnsignedLong(): Long = toLong() and 0xffffffffL
  }
}
