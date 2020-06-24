package org.oppia.app.utility

import android.view.View
import android.widget.TextView
import androidx.annotation.CheckResult
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

// https://stackoverflow.com/questions/47398659/check-the-font-size-height-and-width-of-the-edittext-with-espresso
/** This class mainly provides a custom matcher to test whether the text matches expected font size. */
class FontSizeMatcher(private val expectedSize: Float) :
  TypeSafeMatcher<View?>(View::class.java) {
  override fun matchesSafely(target: View?): Boolean {
    if (target !is TextView) {
      return false
    }
    return target.textSize == expectedSize
  }

  override fun describeTo(description: Description) {
    description.appendText("with fontSize: ")
    description.appendValue(expectedSize)
  }

  companion object {
    /**
     * Matcher that checks font size.
     * Example usage:
     *  onView(withId(R.id.question_player_content_text_view)).check(matches(FontSizeMatcher
     *  .withFontSize(context.resources.getDimension(R.dimen.margin_16))))
     */
    @CheckResult
    fun withFontSize(fontSize: Float): Matcher<View?>? {
      return FontSizeMatcher(fontSize)
    }
  }
}
