package org.oppia.testing

import android.view.View
import android.widget.EditText
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher

/**
 * Appends the specified text to a view. This is needed because Robolectric doesn't seem to
 * properly input digits for text views using 'android:digits'. See
 * https://github.com/robolectric/robolectric/issues/5110 for specifics.
 */

fun appendText(text: String, testCoroutineDispatchers: TestCoroutineDispatchers): ViewAction {
  return object : ViewAction {
    override fun getDescription(): String {
      return "appendText($text)"
    }

    override fun getConstraints(): Matcher<View> {
      return CoreMatchers.allOf(ViewMatchers.isEnabled())
    }

    override fun perform(uiController: UiController?, view: View?) {
      (view as? EditText)?.append(text)
      testCoroutineDispatchers.runCurrent()
    }
  }
}