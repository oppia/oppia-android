package org.oppia.app.faq

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.not
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.help.faq.FAQActivity
import org.oppia.app.recyclerview.RecyclerViewMatcher

@RunWith(AndroidJUnit4::class)
class FAQFragmentTest {

  private val itemCount: Int = 9

  @Test
  fun openFAQActivity_scrollRecyclerViewToZeroPosition_checkFeaturedQuestionsDisplayedSuccessfully() {
    launch(FAQActivity::class.java).use {
      onView(withId(R.id.faq_fragment_recycler_view)).perform(
        RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        RecyclerViewMatcher.atPositionOnView(
          R.id.faq_fragment_recycler_view,
          0,
          R.id.faq_question_text_view
        )
      ).check(
        matches(
          withText(R.string.featured_questions)
        )
      )
    }
  }

  @Test
  fun openFAQActivity_scrollRecyclerViewToLastPosition_checkDividerLineIsNotDisplayed() {
    launch(FAQActivity::class.java).use {
      onView(withId(R.id.faq_fragment_recycler_view)).perform(
        RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(
          itemCount - 1
        )
      )
      onView(
        RecyclerViewMatcher.atPositionOnView(
          R.id.faq_fragment_recycler_view,
          itemCount - 1,
          R.id.faq_question_text_view
        )
      ).check(
        matches(
          not(isDisplayed())
        )
      )
    }
  }
}
