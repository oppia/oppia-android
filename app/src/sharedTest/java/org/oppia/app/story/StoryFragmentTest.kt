package org.oppia.app.story

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.hasItemCount
import org.oppia.app.testing.StoryFragmentTestActivity
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.domain.topic.TEST_STORY_ID_1
import org.oppia.domain.topic.TEST_TOPIC_ID_0

/** Tests for [StoryFragment]. */
@RunWith(AndroidJUnit4::class)
class StoryFragmentTest {

  private val internalProfileId = 0

  @Before
  fun setUp() {
    Intents.init()
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  @Test
  fun testStoryFragment_clickOnToolbarNavigationButton_closeActivity() {
    launch<StoryFragmentTestActivity>(
      createTestActivityIntent(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_1
      )
    ).use {
      onView(withId(R.id.story_toolbar)).perform(click())
    }
  }

  @Test
  fun testStoryFragment_toolbarTitle_isDisplayedSuccessfully() {
    launch<StoryFragmentTestActivity>(
      createTestActivityIntent(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_1
      )
    ).use {
      onView(
        allOf(
          instanceOf(TextView::class.java),
          withParent(withId(R.id.story_toolbar))
        )
      ).check(matches(withText("Second Story")))
    }
  }

  @Test
  @Ignore("No dummy progress supported now.") // TODO(#734): Update test case as per new StoryProgress and TopicProgress.
  fun testStoryFragment_correctStoryCountLoadedInHeader() {
    launch<StoryFragmentTestActivity>(
      createTestActivityIntent(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_1
      )
    ).use {
      val headerString: String = getResources().getQuantityString(R.plurals.story_total_chapters, 3, 1, 3)
      onView(withId(R.id.story_chapter_list)).perform(scrollToPosition<RecyclerView.ViewHolder>(0))
      onView(atPositionOnView(R.id.story_chapter_list, 0, R.id.story_progress_chapter_completed_text)).check(
        matches(
          withText(headerString)
        )
      )
    }
  }

  @Test
  fun testStoryFragment_correctNumberOfStoriesLoadedInRecyclerView() {
    launch<StoryFragmentTestActivity>(
      createTestActivityIntent(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_1
      )
    ).use {
      onView(withId(R.id.story_chapter_list)).check(hasItemCount(4))
    }
  }

  @Test
  fun testStoryFragment_changeConfiguration_textViewIsShownCorrectly() {
    launch<StoryFragmentTestActivity>(
      createTestActivityIntent(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_1
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.story_chapter_list))).perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      onView(atPositionOnView(R.id.story_chapter_list, 1, R.id.chapter_title)).check(
        matches(
          withText("Chapter 1: Second Exploration")
        )
      )
    }
  }

  @Test
  @Ignore("No dummy progress supported now.") // TODO(#734): Update test case as per new StoryProgress and TopicProgress.
  fun testStoryFragment_changeConfiguration_correctStoryCountInHeader() {
    launch<StoryFragmentTestActivity>(
      createTestActivityIntent(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_1
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      val headerString: String = getResources().getQuantityString(R.plurals.story_total_chapters, 3, 1, 3)
      onView(withId(R.id.story_chapter_list)).perform(scrollToPosition<RecyclerView.ViewHolder>(0))
      onView(atPositionOnView(R.id.story_chapter_list, 0, R.id.story_progress_chapter_completed_text)).check(
        matches(
          withText(headerString)
        )
      )
    }
  }

  private fun createTestActivityIntent(internalProfileId: Int, topicId: String, storyId: String): Intent {
    return StoryFragmentTestActivity.createTestActivityIntent(
      ApplicationProvider.getApplicationContext(),
      internalProfileId,
      topicId,
      storyId
    )
  }

  private fun getResources(): Resources {
    return ApplicationProvider.getApplicationContext<Context>().resources
  }
}
