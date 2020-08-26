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
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.help.faq.FAQListActivity
import org.oppia.app.help.faq.faqsingle.FAQSingleActivity
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.robolectric.annotation.LooperMode

/** Tests for [FAQListFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class FAQListFragmentTest {

  @Before
  fun setUp() {
    Intents.init()
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  @Test
  fun openFAQListActivity_scrollRVToZeroPosition_checkFeaturedQuestionsDisplayedSuccessfully() {
    launch(FAQListActivity::class.java).use {
      onView(withId(R.id.faq_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPositionOnView(
          R.id.faq_fragment_recycler_view, 0, R.id.faq_question_text_view
        )
      ).check(matches(withText(R.string.featured_questions)))
    }
  }

  @Test
  fun openFAQListActivity_selectFAQQuestion_opensFAQSingleActivity() {
    launch(FAQListActivity::class.java).use {
      onView(atPosition(R.id.faq_fragment_recycler_view, 1)).perform(click())
      intended(
        allOf(
          hasExtra(
            FAQSingleActivity.FAQ_SINGLE_ACTIVITY_QUESTION,
            getResources().getString(R.string.faq_question_1)
          ),
          hasExtra(
            FAQSingleActivity.FAQ_SINGLE_ACTIVITY_ANSWER,
            getResources().getString(R.string.faq_answer_1)
          ),
          hasComponent(FAQSingleActivity::class.java.name)
        )
      )
    }
  }

  @Test
  fun openFAQListActivity_changeConfiguration_selectFAQQuestion_opensFAQSingleActivity() {
    launch(FAQListActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(atPosition(R.id.faq_fragment_recycler_view, 1)).perform(click())
      intended(
        allOf(
          hasExtra(
            FAQSingleActivity.FAQ_SINGLE_ACTIVITY_QUESTION,
            getResources().getString(R.string.faq_question_1)
          ),
          hasExtra(
            FAQSingleActivity.FAQ_SINGLE_ACTIVITY_ANSWER,
            getResources().getString(R.string.faq_answer_1)
          ),
          hasComponent(FAQSingleActivity::class.java.name)
        )
      )
    }
  }

  private fun getResources(): Resources {
    return ApplicationProvider.getApplicationContext<Context>().resources
  }
}
