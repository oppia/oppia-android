package org.oppia.app.faq

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.help.faq.faqsingle.FAQSingleActivity

/** Tests for [FAQSingleActivity]. */
@RunWith(AndroidJUnit4::class)
class FAQSingleActivityTest {

  @Test
  fun openFAQSingleActivity_checkQuestion_isDisplayed() {
    launch<FAQSingleActivity>(createFAQSingleActivity()).use {
      onView(withId(R.id.faq_question_text_view)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun openFAQSingleActivity_checkAnswer_isDisplayed() {
    launch<FAQSingleActivity>(createFAQSingleActivity()).use {
      onView(withId(R.id.faq_answer_text_view)).check(matches(isDisplayed()))
    }
  }

  private fun createFAQSingleActivity(): Intent {
    return FAQSingleActivity.createFAQSingleActivityIntent(
      ApplicationProvider.getApplicationContext(),
      getResources().getString(R.string.faq_question_1),
      getResources().getString(R.string.faq_answer_1)
    )
  }

  private fun getResources(): Resources {
    return ApplicationProvider.getApplicationContext<Context>().resources
  }
}
