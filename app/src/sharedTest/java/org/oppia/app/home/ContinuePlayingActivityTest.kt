package org.oppia.app.home

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.home.continueplaying.ContinuePlayingActivity
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.story.StoryActivity
import org.oppia.app.utility.EspressoTestsMatchers.withDrawable
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape

/** Tests for [ContinuePlayingActivity]. */
@RunWith(AndroidJUnit4::class)
class ContinuePlayingActivityTest {

  @get:Rule
  var activityTestRule: ActivityTestRule<ContinuePlayingActivity> = ActivityTestRule(
    ContinuePlayingActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Before
  fun setUp() {
    Intents.init()
  }

  @Test
  fun testContinuePlayingActivity_recyclerViewItem0_doesNotShowSectionDivider() {
    ActivityScenario.launch(ContinuePlayingActivity::class.java).use {
      onView(
        atPositionOnView(
          R.id.ongoing_story_recycler_view,
          0,
          R.id.divider_view
        )
      ).check(
        matches(
          not(isDisplayed())
        )
      )
    }
  }

  @Test
  fun testContinuePlayingActivity_recyclerViewItem0_showsLastWeekSectionTitle() {
    ActivityScenario.launch(ContinuePlayingActivity::class.java).use {
      onView(
        atPositionOnView(
          R.id.ongoing_story_recycler_view,
          0,
          R.id.section_title_text_view
        )
      ).check(
        matches(
          withText(R.string.ongoing_story_last_week)
        )
      )
    }
  }

  @Test
  fun testContinuePlayingActivity_recyclerViewItem1_chapterNameIsCorrect() {
    ActivityScenario.launch(ContinuePlayingActivity::class.java).use {
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          R.id.ongoing_story_recycler_view,
          1,
          R.id.chapter_name_text_view
        )
      ).check(
        matches(
          withText(containsString("The Meaning of Equal Parts"))
        )
      )
    }
  }

  @Test
  fun testContinuePlayingActivity_recyclerViewItem1_storyNameIsCorrect() {
    ActivityScenario.launch(ContinuePlayingActivity::class.java).use {
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          R.id.ongoing_story_recycler_view,
          1,
          R.id.story_name_text_view
        )
      ).check(
        matches(
          withText(containsString("Second Story"))
        )
      )
    }
  }

  @Test
  fun testContinuePlayingActivity_recyclerViewItem1_topicNameIsCorrect() {
    ActivityScenario.launch(ContinuePlayingActivity::class.java).use {
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          R.id.ongoing_story_recycler_view,
          1,
          R.id.topic_name_text_view
        )
      ).check(
        matches(
          withText(containsString("First Topic"))
        )
      )
    }
  }

  @Test
  fun testContinuePlayingActivity_recyclerViewItem1_lessonThumbnailIsCorrect() {
    ActivityScenario.launch(ContinuePlayingActivity::class.java).use {
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          R.id.ongoing_story_recycler_view,
          1,
          R.id.lesson_thumbnail
        )
      ).check(
        matches(
          withDrawable(R.drawable.lesson_thumbnail_graphic_duck_and_chicken)
        )
      )
    }
  }

  @Test
  fun testContinuePlayingActivity_recyclerViewItem3_showsLastMonthSectionTitle() {
    ActivityScenario.launch(ContinuePlayingActivity::class.java).use {
      onView(withId(R.id.ongoing_story_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(3))
      onView(
        atPositionOnView(
          R.id.ongoing_story_recycler_view,
          3,
          R.id.section_title_text_view
        )
      ).check(matches(withText(R.string.ongoing_story_last_month)))
    }
  }

  @Test
  fun testContinuePlayingActivity_recyclerViewItem3_showsSectionDivider() {
    ActivityScenario.launch(ContinuePlayingActivity::class.java).use {
      onView(withId(R.id.ongoing_story_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(3))
      onView(atPositionOnView(R.id.ongoing_story_recycler_view, 3, R.id.divider_view)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testContinuePlayingActivity_recyclerViewItem4_chapterNameIsCorrect() {
    ActivityScenario.launch(ContinuePlayingActivity::class.java).use {
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          4
        )
      )
      onView(
        atPositionOnView(
          R.id.ongoing_story_recycler_view,
          4,
          R.id.chapter_name_text_view
        )
      ).check(
        matches(
          withText(containsString("Miguel Reads a Book"))
        )
      )
    }
  }

  @Test
  fun testContinuePlayingActivity_recyclerViewItem4_clickStoryItem_opensStoryActivityWithCorrectIntent() {
    activityTestRule.launchActivity(null)
    onView(withId(R.id.ongoing_story_recycler_view)).perform(
      scrollToPosition<RecyclerView.ViewHolder>(
        4
      )
    )
    onView(atPosition(R.id.ongoing_story_recycler_view, 4)).perform(click())
    intended(hasComponent(StoryActivity::class.java.name))
    intended(hasExtra(StoryActivity.STORY_ACTIVITY_STORY_ID_ARGUMENT_KEY, "test_story_id_0"))
  }

  @Test
  fun testContinuePlayingActivity_changeConfiguration_recyclerViewItem0_showsLastWeekSectionTitle() {
    ActivityScenario.launch(ContinuePlayingActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(
        atPositionOnView(
          R.id.ongoing_story_recycler_view,
          0,
          R.id.section_title_text_view
        )
      ).check(
        matches(
          withText(R.string.ongoing_story_last_week)
        )
      )
    }
  }

  @Test
  fun testContinuePlayingActivity_changeConfiguration_recyclerViewItem3_showsLastMonthSectionTitle() {
    ActivityScenario.launch(ContinuePlayingActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.ongoing_story_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(3))
      onView(
        atPositionOnView(
          R.id.ongoing_story_recycler_view,
          3,
          R.id.section_title_text_view
        )
      ).check(matches(withText(R.string.ongoing_story_last_month)))
    }
  }

  @Test
  fun testContinuePlayingActivity_changeConfiguration_recyclerViewItem4_chapterNameIsCorrect() {
    ActivityScenario.launch(ContinuePlayingActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          4
        )
      )
      onView(
        atPositionOnView(
          R.id.ongoing_story_recycler_view,
          4,
          R.id.chapter_name_text_view
        )
      ).check(
        matches(
          withText(containsString("Miguel Reads a Book"))
        )
      )
    }
  }

  @After
  fun tearDown() {
    Intents.release()
  }
}
