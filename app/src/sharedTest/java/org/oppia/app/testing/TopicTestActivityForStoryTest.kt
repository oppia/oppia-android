package org.oppia.app.testing

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.containsString
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.topic.TopicTab
import org.oppia.app.utility.EspressoTestsMatchers

/** Tests for [TopicTestActivityForStory]. */
@RunWith(AndroidJUnit4::class)
class TopicTestActivityForStoryTest {

  @Test
  fun testTopicTestActivityForStory_defaultTabIsPlay_isSuccessful() {
    launch(TopicTestActivityForStory::class.java).use {
      onView(withId(R.id.topic_tabs_container)).check(
        matches(
          EspressoTestsMatchers.matchCurrentTabTitle(
            TopicTab.getTabForPosition(1).name
          )
        )
      )
    }
  }

  @Test
  fun testTopicTestActivityForStory_showsTopicPlayFragment() {
    launch(TopicTestActivityForStory::class.java).use {
      onView(withId(R.id.story_summary_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPosition(
          R.id.story_summary_recycler_view,
          0
        )
      ).check(matches(hasDescendant(withText(containsString("First Story")))))
    }
  }

  @Test
  fun testTopicTestActivityForStory_playTopicTab_storyItemIsexpanded() {
    launch(TopicTestActivityForStory::class.java).use {
      onView(
        atPositionOnView(
          R.id.story_summary_recycler_view,
          7,
          R.id.chapter_recycler_view
        )
      ).check(matches(isDisplayed()))
    }
  }
}
