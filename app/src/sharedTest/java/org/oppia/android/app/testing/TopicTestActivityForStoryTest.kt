package org.oppia.android.app.testing

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
import org.oppia.android.R
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.topic.TopicTab
import org.oppia.android.app.utility.EspressoTestsMatchers.matchCurrentTabTitle
import org.robolectric.annotation.LooperMode

/** Tests for [TopicTestActivityForStory]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class TopicTestActivityForStoryTest {

  @Test
  fun testTopicTestActivityForStory_defaultTabIsPlay_isSuccessful() {
    launch(TopicTestActivityForStory::class.java).use {
      onView(withId(R.id.topic_tabs_container)).check(
        matches(
          matchCurrentTabTitle(
            TopicTab.getTabForPosition(1).name
          )
        )
      )
    }
  }

  @Test
  fun testTopicTestActivityForStory_showsTopicPlay() {
    launch(TopicTestActivityForStory::class.java).use {
      onView(withId(R.id.story_summary_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPosition(
          R.id.story_summary_recycler_view,
          1
        )
      ).check(matches(hasDescendant(withText(containsString("First Story")))))
    }
  }

  @Test
  fun testTopicTestActivityForStory_playTopicTab_storyItemIsExpanded() {
    launch(TopicTestActivityForStory::class.java).use {
      onView(
        atPositionOnView(
          R.id.story_summary_recycler_view,
          2,
          R.id.chapter_recycler_view
        )
      ).check(matches(isDisplayed()))
    }
  }
}
