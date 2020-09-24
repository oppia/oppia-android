package org.oppia.app.parser

import android.text.style.CharacterStyle
import android.view.View
import android.widget.TextView
import androidx.core.text.toSpannable
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

class RichTextViewMatcher {
  companion object {
    fun containsRichText(): Matcher<View> {
      return object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description) {
          description.appendText("Checks if view contains rich text")
        }

        override fun matchesSafely(view: View): Boolean {
          return view is TextView && view.text.toSpannable()
            .getSpans(0, view.text.length, CharacterStyle::class.java).isNotEmpty()
        }
      }
    }
  }
}
