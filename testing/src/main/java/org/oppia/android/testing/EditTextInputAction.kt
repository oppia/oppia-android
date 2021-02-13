package org.oppia.android.testing

import android.content.Context
import android.os.Build
import android.view.View
import android.widget.EditText
import androidx.annotation.StringRes
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.typeText
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import javax.inject.Inject

/**
 * Action for inputting text into an EditText in a test infrastructure-specific way.
 *
 * This is needed because Robolectric doesn't seem to properly input digits for text views using
 * 'android:digits' or other filters. See https://github.com/robolectric/robolectric/issues/5110
 * for specifics.
 */
class EditTextInputAction @Inject constructor(
  val testCoroutineDispatchers: TestCoroutineDispatchers , val context: Context
) {
  // TODO(#1720): Move this to a companion object & use a test-only singleton injector to retrieve
  // the TestCoroutineDispatchers so that the outer class doesn't need to be injected.
  /**
   * Returns a [ViewAction] that appends the specified string into the view targeted by the
   * [ViewAction].
   */
  fun appendText(text: String): ViewAction {
    val typeTextViewAction = typeText(text)
    return object : ViewAction {
      override fun getDescription(): String = typeTextViewAction.description

      override fun getConstraints(): Matcher<View> = typeTextViewAction.constraints

      override fun perform(uiController: UiController?, view: View?) {
        // Appending text only works on Robolectric, whereas Espresso needs to use typeText().
        if (Build.FINGERPRINT.contains("robolectric", ignoreCase = true)) {
          (view as? EditText)?.append(text)
          testCoroutineDispatchers.runCurrent()
        } else {
          typeTextViewAction.perform(uiController, view)
        }
      }
    }
  }
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
        description.appendText("")
      }
    }
  }
}
