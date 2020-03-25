package org.oppia.app.faq

import android.content.Context
import android.content.res.Resources
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.help.faq.FAQActivity
import org.oppia.app.help.faq.faqsingle.FAQSingleActivity
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView

/** Tests for [FAQFragment]. */
@RunWith(AndroidJUnit4::class)
class FAQFragmentTest {

  private val itemCount: Int = 9

  @Before
  fun setUp() {
    Intents.init()
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  @Test
  fun openFAQActivity_scrollRecyclerViewToZeroPosition_checkFeaturedQuestionsDisplayedSuccessfully() {
    launch(FAQActivity::class.java).use {
      onView(withId(R.id.faq_fragment_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(0))
      onView(
        atPositionOnView(
          R.id.faq_fragment_recycler_view, 0, R.id.faq_question_text_view
        )
      ).check(matches(withText(R.string.featured_questions)))
    }
  }

  @Test
  fun openFAQActivity_selectFAQQuestion_opensFAQSingleActivity() {
    launch(FAQActivity::class.java).use {
      onView(atPosition(R.id.faq_fragment_recycler_view, 1)).perform(click())
      intended(
        allOf(
          hasExtra(FAQSingleActivity.FAQ_SINGLE_ACTIVITY_QUESTION, getResources().getString(R.string.faq_question_1)),
          hasExtra(FAQSingleActivity.FAQ_SINGLE_ACTIVITY_ANSWER, getResources().getString(R.string.faq_answer_1)),
          hasComponent(FAQSingleActivity::class.java.name)
        )
      )
    }
  }

  @Test
  fun openFAQActivity_scrollRecyclerViewToLastPosition_checkDividerLineIsNotDisplayed() {
    launch(FAQActivity::class.java).use {
      onView(withId(R.id.faq_fragment_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(itemCount - 1))
      onView(
        atPositionOnView(
          R.id.faq_fragment_recycler_view, itemCount - 1, R.id.faq_question_divider_view
        )
      ).check(matches(not(isDisplayed())))
    }
  }

  private fun getResources(): Resources {
    return ApplicationProvider.getApplicationContext<Context>().resources
  }
}
