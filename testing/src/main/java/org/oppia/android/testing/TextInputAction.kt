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
 * Action for accessing HasErrorTest and HasNoErrorTest Functions.
 *
 * This is needed because Robolectric doesn't seem to properly input digits for text views using
 * 'android:digits' or other filters. See https://github.com/robolectric/robolectric/issues/5110
 * for specifics.
 */
class TextInputAction @Inject constructor(
  val context: Context
) {
  // TODO(#1720): Move this to a companion object & use a test-only singleton injector to retrieve
  fun hasErrorText(@StringRes expectedErrorTextId: Int): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
      override fun matchesSafely(view: View): Boolean {
        val expectedErrorText = context.resources.getString(expectedErrorTextId)
        return (view as TextInputLayout).error == expectedErrorText
      }

      override fun describeTo(description: Description) {
        description.appendText("TextInputLayout's error")
      }
    }
  }

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
