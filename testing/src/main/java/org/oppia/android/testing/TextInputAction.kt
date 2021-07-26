package org.oppia.android.testing

import android.content.Context
import androidx.annotation.StringRes
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import javax.inject.Inject

class TextInputAction @Inject constructor(
  val context: Context
) {
  fun hasErrorText(@StringRes expectedErrorTextId: Int): Matcher<TextInputLayout> {
    return object : TypeSafeMatcher<TextInputLayout>() {
      override fun matchesSafely(textInputLayout: TextInputLayout): Boolean {
        val expectedErrorText = context.resources.getString(expectedErrorTextId)
        return (textInputLayout).error == expectedErrorText
      }

      override fun describeTo(description: Description) {
        description.appendText("TextInputLayout's error")
      }
    }
  }

  fun hasNoErrorText(): Matcher<TextInputLayout> {
    return object : TypeSafeMatcher<TextInputLayout>() {
      override fun matchesSafely(textInputLayout: TextInputLayout): Boolean {
        return (textInputLayout).error.isNullOrEmpty()
      }

      override fun describeTo(description: Description) {
        description.appendText("")
      }
    }
  }
}
