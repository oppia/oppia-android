package org.oppia.android.app.utility

import android.content.Intent
import android.view.View
import com.google.protobuf.MessageLite
import org.hamcrest.Matcher
import org.oppia.android.app.utility.ProtoExtraMatcher.Companion.hasProtoExtraCheck
import org.oppia.android.app.utility.TabMatcher.Companion.matchCurrentTabTitleCheck

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

  fun <T : MessageLite> hasProtoExtra(keyName: String, expectedProto: T): Matcher<Intent> {
    return hasProtoExtraCheck(keyName, expectedProto)
  }
}
