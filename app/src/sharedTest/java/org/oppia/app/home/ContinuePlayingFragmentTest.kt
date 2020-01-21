package org.oppia.app.home

import android.widget.TextView
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
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.player.exploration.EXPLORATION_ACTIVITY_EXPLORATION_ID_ARGUMENT_KEY
import org.oppia.app.player.exploration.EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY
import org.oppia.app.player.exploration.ExplorationActivity
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.testing.ContinuePlayingFragmentTestActivity
import org.oppia.app.utility.EspressoTestsMatchers.withDrawable
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.domain.topic.FRACTIONS_EXPLORATION_ID_1
import org.oppia.domain.topic.FRACTIONS_TOPIC_ID

/** Tests for [ContinuePlayingFragmentTestActivity]. */
@RunWith(AndroidJUnit4::class)
class ContinuePlayingFragmentTest {

  @Before
  fun setUp() {
    Intents.init()
  }

  @Test
  fun testContinuePlayingTestActivity_clickOnToolbarNavigationButton_closeActivity() {
    ActivityScenario.launch(ContinuePlayingFragmentTestActivity::class.java).use {
      onView(withId(R.id.continue_playing_toolbar)).perform(click())
    }
  }

  @Test
  fun testContinuePlayingTestActivity_toolbarTitle_isDisplayedSuccessfully() {
    ActivityScenario.launch(ContinuePlayingFragmentTestActivity::class.java).use {
      onView(
        allOf(
          instanceOf(TextView::class.java),
          withParent(withId(R.id.continue_playing_toolbar))
        )
      ).check(matches(withText(R.string.continue_playing_activity)))
    }
  }

  @Test
  fun testContinuePlayingTestActivity_recyclerViewItem0_doesNotShowSectionDivider() {
    ActivityScenario.launch(ContinuePlayingFragmentTestActivity::class.java).use {
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
  fun testContinuePlayingTestActivity_recyclerViewItem0_showsLastWeekSectionTitle() {
    ActivityScenario.launch(ContinuePlayingFragmentTestActivity::class.java).use {
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
  fun testContinuePlayingTestActivity_recyclerViewItem1_chapterNameIsCorrect() {
    ActivityScenario.launch(ContinuePlayingFragmentTestActivity::class.java).use {
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
  fun testContinuePlayingTestActivity_recyclerViewItem1_storyNameIsCorrect() {
    ActivityScenario.launch(ContinuePlayingFragmentTestActivity::class.java).use {
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
          withText(containsString("Matthew Goes to the Bakery"))
        )
      )
    }
  }

  @Test
  fun testContinuePlayingTestActivity_recyclerViewItem1_topicNameIsCorrect() {
    ActivityScenario.launch(ContinuePlayingFragmentTestActivity::class.java).use {
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
          withText(containsString("FRACTIONS"))
        )
      )
    }
  }

  @Test
  fun testContinuePlayingTestActivity_recyclerViewItem1_lessonThumbnailIsCorrect() {
    ActivityScenario.launch(ContinuePlayingFragmentTestActivity::class.java).use {
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
  fun testContinuePlayingTestActivity_recyclerViewItem1_clickStory_intentsToExplorationActivity() {
    ActivityScenario.launch(ContinuePlayingFragmentTestActivity::class.java).use {
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
      ).perform(click())

      intended(
        allOf(
          hasExtra(EXPLORATION_ACTIVITY_EXPLORATION_ID_ARGUMENT_KEY, FRACTIONS_EXPLORATION_ID_1),
          hasExtra(EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY, FRACTIONS_TOPIC_ID),
          hasComponent(ExplorationActivity::class.java.name)
        )
      )
    }
  }

  @Test
  @Ignore("Only one item available from domain layer") // TODO(#77): Reenable this test
  fun testContinuePlayingTestActivity_recyclerViewItem3_showsLastMonthSectionTitle() {
    ActivityScenario.launch(ContinuePlayingFragmentTestActivity::class.java).use {
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
  @Ignore("Only one item available from domain layer") // TODO(#77): Reenable this test
  fun testContinuePlayingTestActivity_recyclerViewItem3_showsSectionDivider() {
    ActivityScenario.launch(ContinuePlayingFragmentTestActivity::class.java).use {
      onView(withId(R.id.ongoing_story_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(3))
      onView(atPositionOnView(R.id.ongoing_story_recycler_view, 3, R.id.divider_view)).check(matches(isDisplayed()))
    }
  }

  @Test
  @Ignore("Only one item available from domain layer") // TODO(#77): Reenable this test
  fun testContinuePlayingTestActivity_recyclerViewItem4_chapterNameIsCorrect() {
    ActivityScenario.launch(ContinuePlayingFragmentTestActivity::class.java).use {
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
  @Ignore("Landscape not properly supported") // TODO(#56): Reenable once landscape is supported.
  fun testContinuePlayingTestActivity_changeConfiguration_recyclerViewItem0_showsLastWeekSectionTitle() {
    ActivityScenario.launch(ContinuePlayingFragmentTestActivity::class.java).use {
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
  @Ignore("Landscape not properly supported") // TODO(#56): Reenable once landscape is supported.
  fun testContinuePlayingTestActivity_changeConfiguration_recyclerViewItem3_showsLastMonthSectionTitle() {
    ActivityScenario.launch(ContinuePlayingFragmentTestActivity::class.java).use {
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
  @Ignore("Landscape not properly supported") // TODO(#56): Reenable once landscape is supported.
  fun testContinuePlayingTestActivity_changeConfiguration_recyclerViewItem4_chapterNameIsCorrect() {
    ActivityScenario.launch(ContinuePlayingFragmentTestActivity::class.java).use {
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
