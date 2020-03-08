package org.oppia.app.faq

import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.help.faq.FAQActivity

@RunWith(AndroidJUnit4::class)
class FAQFragmentTest {

  @Test
  fun openFAQActivity_checkFeaturedQuestionsDisplayedSuccessfully() {
    launch(FAQActivity::class.java).use {
      onView(withId(R.id.featured_questions)).check(matches(withText(R.string.featured_questions)))
    }
  }
}
