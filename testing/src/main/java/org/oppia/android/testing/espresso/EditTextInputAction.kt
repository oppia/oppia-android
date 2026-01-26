package org.oppia.android.testing.espresso

import android.os.Build
import android.view.View
import android.widget.EditText
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.typeText
import org.hamcrest.Matcher
import org.oppia.android.testing.threading.TestCoroutineDispatchersInjector

/**
 * Action for inputting text into an EditText in a test infrastructure-specific way.
 *
 * This is needed because Robolectric doesn't seem to properly input digits for text views using
 * 'android:digits' or other filters. See https://github.com/robolectric/robolectric/issues/5110
 * for specifics.
 */
class EditTextInputAction {
  companion object {
    /**
     * Returns a [ViewAction] that appends the specified string into the view targeted by the
     * [ViewAction].
     */
    fun appendText(text: String): ViewAction = updateText(
      text, baseAction = typeText(text), isAppend = true
    )

    /**
     * Returns a [ViewAction] that replaces the current text in the specified view with the specified
     * string.
     *
     * Note that this should only be used over [appendText] in the following cases:
     * 1. When there's existing text to first erase before adding new text
     * 2. When Unicode text needs to be inputted (since otherwise Espresso will fail to type the text)
     */
    fun replaceText(text: String): ViewAction = updateText(
      text, baseAction = ViewActions.replaceText(text), isAppend = false
    )

    private fun updateText(text: String, baseAction: ViewAction, isAppend: Boolean): ViewAction {
      return object : ViewAction {
        override fun getDescription(): String = baseAction.description

        override fun getConstraints(): Matcher<View> = baseAction.constraints

        override fun perform(uiController: UiController?, view: View?) {
          // Appending text only works on Robolectric, whereas Espresso needs to use typeText().
          if (Build.FINGERPRINT.contains("robolectric", ignoreCase = true)) {
            val editText = view as? EditText
            if (isAppend) {
              editText?.append(text)
            } else {
              editText?.setText(text)
            }
            TestCoroutineDispatchersInjector.getDispatcher().runCurrent()
          } else {
            baseAction.perform(uiController, view)
          }
        }
      }
    }
  }
}
