package org.oppia.app.walkthrough

import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.containsString
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.app.utility.ProgressMatcher.Companion.withProgress

/** Tests for [WalkthroughFinalFragment]. */
@RunWith(AndroidJUnit4::class)
class WalkthroughFinalFragmentTest {

  @Before
  fun setUp() {
    Intents.init()
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private fun createWalkthroughActivityIntent(profileId: Int): Intent {
    return WalkthroughActivity.createWalkthroughActivityIntent(
      ApplicationProvider.getApplicationContext(),
      profileId
    )
  }

  @Test
  fun testWalkthroughWelcomeFragment_recyclerViewIndex1_topicSelected_topicTitleIsCorrect() {
    launch<WalkthroughActivity>(createWalkthroughActivityIntent(0)).use {
      onView(withId(R.id.walkthrough_welcome_next_button))
        .perform(click())
      onView(withId(R.id.walkthrough_topic_recycler_view))
        .perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      onView(
        atPositionOnView(
          R.id.walkthrough_topic_recycler_view,
          1,
          R.id.walkthrough_topic_name_text_view
        )
      ).perform(click())
      onView(withId(R.id.walkthrough_final_topic_text_view)).check(
        matches(
          withText(containsString("First Test Topic"))
        )
      )
    }
  }

  @Test
  fun testWalkthroughWelcomeFragment_recyclerViewIndex2_topicSelected_topicTitleIsCorrect() {
    launch<WalkthroughActivity>(createWalkthroughActivityIntent(0)).use {
      onView(withId(R.id.walkthrough_welcome_next_button))
        .perform(click())
      onView(withId(R.id.walkthrough_topic_recycler_view))
        .perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      onView(
        atPositionOnView(
          R.id.walkthrough_topic_recycler_view,
          2,
          R.id.walkthrough_topic_name_text_view
        )
      ).perform(click())
      onView(withId(R.id.walkthrough_final_topic_text_view)).check(
        matches(
          withText(containsString("Second Test Topic"))
        )
      )
    }
  }

  @Test
  fun testWalkthroughWelcomeFragment_recyclerViewIndex2_topicSelected_configurationChanged_topicTitleIsCorrect() {
    launch<WalkthroughActivity>(createWalkthroughActivityIntent(0)).use {
      onView(withId(R.id.walkthrough_welcome_next_button))
        .perform(click())
      onView(withId(R.id.walkthrough_topic_recycler_view))
        .perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      onView(
        atPositionOnView(
          R.id.walkthrough_topic_recycler_view,
          2,
          R.id.walkthrough_topic_name_text_view
        )
      ).perform(click())
      onView(withId(R.id.walkthrough_final_topic_text_view)).check(
        matches(
          withText(containsString("Second Test Topic"))
        )
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.walkthrough_final_topic_text_view)).check(
        matches(
          withText(containsString("Second Test Topic"))
        )
      )
    }
  }

  @Test
  fun testWalkthroughWelcomeFragment_recyclerViewIndex1_topicSelected_yesNoButton_isDisplayedCorrectly() {
    launch<WalkthroughActivity>(createWalkthroughActivityIntent(0)).use {
      onView(withId(R.id.walkthrough_welcome_next_button))
        .perform(click())
      onView(withId(R.id.walkthrough_topic_recycler_view))
        .perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      onView(
        atPositionOnView(
          R.id.walkthrough_topic_recycler_view,
          2,
          R.id.walkthrough_topic_name_text_view
        )
      ).perform(click())
      onView(withId(R.id.walkthrough_final_no_button)).perform(scrollTo())
        .check(matches(isDisplayed()))
      onView(withId(R.id.walkthrough_final_yes_button)).perform(scrollTo())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testWalkthroughWelcomeFragment_recyclerViewIndex1_topicSelected_clickNoButton_worksCorrectly() {
    launch<WalkthroughActivity>(createWalkthroughActivityIntent(0)).use {
      onView(withId(R.id.walkthrough_welcome_next_button))
        .perform(click())
      onView(withId(R.id.walkthrough_topic_recycler_view))
        .perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      onView(
        atPositionOnView(
          R.id.walkthrough_topic_recycler_view,
          2,
          R.id.walkthrough_topic_name_text_view
        )
      ).perform(click())
      onView(withId(R.id.walkthrough_final_no_button)).perform(scrollTo())
        .perform(click())
      onView(withId(R.id.walkthrough_progress_bar)).check(matches(withProgress(2)))
    }
  }
}
