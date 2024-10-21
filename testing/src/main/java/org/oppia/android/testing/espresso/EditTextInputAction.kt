package org.oppia.android.testing.espresso

import android.os.Build
import android.view.View
import android.widget.EditText
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.typeText
import org.hamcrest.Matcher
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import javax.inject.Inject

/**
 * Action for inputting text into an EditText in a test infrastructure-specific way.
 *
 * This is needed because Robolectric doesn't seem to properly input digits for text views using
 * 'android:digits' or other filters. See https://github.com/robolectric/robolectric/issues/5110
 * for specifics.
 */
class EditTextInputAction @Inject constructor(
  val testCoroutineDispatchers: TestCoroutineDispatchers
) {
  // TODO(#1720): Move this to a companion object & use a test-only singleton injector to retrieve
  // the TestCoroutineDispatchers so that the outer class doesn't need to be injected.
  /**
   * Returns a [ViewAction] that appends the specified string into the view targeted by the
   * [ViewAction].
   */
  fun appendText(text: String): ViewAction = updateText(text, baseAction = typeText(text))

  /**
   * Returns a [ViewAction] that replaces the current text in the specified view with the specified
   * string.
   *
   * Note that this should only be used over [appendText] in the following cases:
   * 1. When there's existing text to first erase before adding new text
   * 2. When Unicode text needs to be inputted (since otherwise Espresso will fail to type the text)
   */
  fun replaceText(text: String): ViewAction =
    updateText(text, baseAction = ViewActions.replaceText(text))

  private fun updateText(text: String, baseAction: ViewAction): ViewAction {
    return object : ViewAction {
      override fun getDescription(): String = baseAction.description

      override fun getConstraints(): Matcher<View> = baseAction.constraints

      override fun perform(uiController: UiController?, view: View?) {
        // Appending text only works on Robolectric, whereas Espresso needs to use typeText().
        if (Build.FINGERPRINT.contains("robolectric", ignoreCase = true)) {
          (view as? EditText)?.setText(text)
          testCoroutineDispatchers.runCurrent()
        } else baseAction.perform(uiController, view)
      }
    }
  }
}
