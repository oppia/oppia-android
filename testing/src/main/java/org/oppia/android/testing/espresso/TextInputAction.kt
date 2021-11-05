package org.oppia.android.testing.espresso

import android.view.View
import androidx.test.espresso.matcher.BoundedMatcher
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.Description

/**
 * Action for matching error text and performing other actions on [TextInputLayout] in a test
 * infrastructure-specific way.
 */
class TextInputAction {

  companion object {
    /**
     * Checks that the TextInputLayout view has the [expectedErrorText] error text.
     *
     * @returns a [BoundedMatcher] that matches the [expectedErrorText] with the TextInputLayout's
     *     error text
     */
    fun hasErrorText(expectedErrorText: String): BoundedMatcher<View, TextInputLayout> {
      return ErrorTextExistsMatcher(expectedErrorText)
    }

    /**
     * Checks that the TextInputLayout view has an empty error text.
     *
     * @returns a [BoundedMatcher] that matches if error text is null or empty
     */
    fun hasNoErrorText(): BoundedMatcher<View, TextInputLayout> {
      return ErrorTextDoesNotExistMatcher()
    }

    /**
     * Class which inherits [BoundedMatcher] and overrides [matchesSafely] function to match the
     * [expectedErrorText] with the TextInputLayout's error text.
     */
    private class ErrorTextExistsMatcher(private val expectedErrorText: String) :
      BoundedMatcher<View, TextInputLayout>(TextInputLayout::class.java) {
      override fun matchesSafely(textInputLayout: TextInputLayout): Boolean {
        return textInputLayout.error == expectedErrorText
      }

      override fun describeTo(description: Description) {
        description.appendText("The expected error text is '$expectedErrorText'")
      }
    }

    /**
     * Class which inherits [BoundedMatcher] and overrides [matchesSafely] function to check if the
     * error text is null of empty.
     */
    private class ErrorTextDoesNotExistMatcher :
      BoundedMatcher<View, TextInputLayout>(TextInputLayout::class.java) {
      override fun matchesSafely(textInputLayout: TextInputLayout): Boolean {
        return textInputLayout.error.isNullOrEmpty()
      }

      override fun describeTo(description: Description) {
        description.appendText("There is no error text.")
      }
    }
  }
}
