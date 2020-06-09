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
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.containsString
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape

/** Tests for [WalkthroughTopicListFragment]. */
@RunWith(AndroidJUnit4::class)
class WalkthroughTopicListFragmentTest {

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
  fun testWalkthroughWelcomeFragment_recyclerViewIndex0_topicHeader_topicHeaderIsCorrect() {
    launch<WalkthroughActivity>(createWalkthroughActivityIntent(0)).use {
      onView(withId(R.id.walkthrough_welcome_next_button)).perform(scrollTo(), click())
      onView(
        atPositionOnView(
          R.id.walkthrough_topic_recycler_view,
          0,
          R.id.walkthrough_topic_header_text_view
        )
      ).check(
        matches(
          withText(R.string.what_do_you_want_to_learn)
        )
      )
    }
  }

  @Test
  fun testWalkthroughWelcomeFragment_recyclerViewIndex1_topicCard_topicNameIsCorrect() {
    launch<WalkthroughActivity>(createWalkthroughActivityIntent(0)).use {
      onView(withId(R.id.walkthrough_welcome_next_button)).perform(scrollTo(), click())
      onView(withId(R.id.walkthrough_topic_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          R.id.walkthrough_topic_recycler_view,
          1,
          R.id.walkthrough_topic_name_text_view
        )
      ).check(
        matches(
          withText(containsString("First Topic"))
        )
      )
    }
  }

  /* ktlint-disable max-line-length */
  @Test
  fun testWalkthroughWelcomeFragment_recyclerViewIndex1_topicCard_configurationChanged_topicNameIsCorrect() {
    launch<WalkthroughActivity>(createWalkthroughActivityIntent(0)).use {
      onView(withId(R.id.walkthrough_welcome_next_button)).perform(scrollTo(), click())
      onView(withId(R.id.walkthrough_topic_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(
        atPositionOnView(
          R.id.walkthrough_topic_recycler_view,
          1,
          R.id.walkthrough_topic_name_text_view
        )
      ).check(
        matches(
          withText(containsString("First Topic"))
        )
      )
    }
  }
  /* ktlint-enable max-line-length */
}
