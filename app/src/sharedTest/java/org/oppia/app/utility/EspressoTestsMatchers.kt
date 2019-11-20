package org.oppia.app.utility

import android.view.View
import org.hamcrest.Matcher
import org.oppia.app.utility.TabMatcher.Companion.matchCurrentTabTitleCheck

// https://medium.com/@dbottillo/android-ui-test-espresso-matcher-for-imageview-1a28c832626f#.4snjg8frw
/** This object mainly facilitates as bridge between test-cases and various custom test-matchers. */
object EspressoTestsMatchers {

  fun matchCurrentTabTitle(tabTitle: String): Matcher<View> {
    return matchCurrentTabTitleCheck(tabTitle)
  }

  fun withDrawable(resourceId: Int): Matcher<View> {
    return DrawableMatcher(resourceId)
  }

  @Suppress("unused")
  fun noDrawable(): Matcher<View> {
    return DrawableMatcher(DrawableMatcher.NONE)
  }
}
