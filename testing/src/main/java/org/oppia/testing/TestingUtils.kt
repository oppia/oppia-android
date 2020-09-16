package org.oppia.testing

import android.view.View
import android.widget.EditText
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher
import javax.inject.Inject

@Inject
lateinit var testCoroutineDispatchers : TestCoroutineDispatchers

fun appendText(text: String): ViewAction {
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