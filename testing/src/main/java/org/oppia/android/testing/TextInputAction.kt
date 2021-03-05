package org.oppia.android.testing

import android.content.Context
import android.view.View
import androidx.annotation.StringRes
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import javax.inject.Inject

/**
 * Action for matching error text and performing other actions on TextInputLayout in a test infrastructure-specific way.
 */
class TextInputAction @Inject constructor(val context: Context) {
  // TODO(#1720): Move this to a companion object & use a test-only singleton injector to retrieve.

  /**
   * Returns a [Matcher] that matches the string corresponding to the specified
   * [expectedErrorTextResId] with the view's error text.
   */
  fun hasErrorText(@StringRes expectedErrorTextResId: Int): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
      override fun matchesSafely(view: View): Boolean {
        val expectedErrorText = context.resources.getString(expectedErrorTextResId)
        return (view as TextInputLayout).error == expectedErrorText
      }

      override fun describeTo(description: Description) {
        description.appendText("TextInputLayout's error")
      }
    }
  }

  /**
   * returns a [Matcher] that matches the view has no error text available.
   */
  fun hasNoErrorText(): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
      override fun matchesSafely(view: View): Boolean {
        return (view as TextInputLayout).error.isNullOrEmpty()
      }

      override fun describeTo(description: Description) {
        description.appendText("TextInputLayout's error")
      }
    }
  }
}
