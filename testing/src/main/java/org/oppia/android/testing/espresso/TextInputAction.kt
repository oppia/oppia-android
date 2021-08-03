package org.oppia.android.testing.espresso

import android.view.View
import androidx.test.espresso.matcher.BoundedMatcher
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.Description
import org.hamcrest.Matcher

/**
 * Action for matching error text and performing other actions on TextInputLayout
 * in a test infrastructure-specific way.
 */
class TextInputAction {

  companion object {
    /**
     * Returns a [Matcher] that matches the string corresponding to the specified
     * [expectedErrorText] with the textInputLayout's error text.
     */
    fun hasErrorText(expectedErrorText: String) =
      object : BoundedMatcher<View, TextInputLayout>(TextInputLayout::class.java) {
        override fun matchesSafely(textInputLayout: TextInputLayout): Boolean {
          return (textInputLayout).error == expectedErrorText
        }

        override fun describeTo(description: Description) {
          description.appendText("TextInputLayout's error")
        }
      }

    /**
     *Returns a [Matcher] that matches if the textInputLayout has no error text available.
     */
    fun hasNoErrorText() =
      object : BoundedMatcher<View, TextInputLayout>(TextInputLayout::class.java) {
        override fun matchesSafely(textInputLayout: TextInputLayout): Boolean {
          return (textInputLayout).error.isNullOrEmpty()
        }

        override fun describeTo(description: Description) {
          description.appendText("")
        }
      }
  }
}
