package org.oppia.app.home

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.containsString
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R

/** Tests for [ContinuePlayingActivity]. */
@RunWith(AndroidJUnit4::class)
class ContinuePlayingActivityTest {

  @Test
  fun testContinuePlayingActivity_loadContinuePlayingFragment_hasDummyString() {
    ActivityScenario.launch(ContinuePlayingActivity::class.java).use {
      onView(withId(R.id.dummy_text_view)).check(matches(withText(containsString("Dummy Continue Playing Activity"))))
    }
  }
}
