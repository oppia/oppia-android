package org.oppia.app.topic.play

import android.content.Intent
import android.content.res.Configuration
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.player.exploration.ExplorationActivity
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.story.StoryActivity
import org.oppia.app.topic.TopicActivity
import org.oppia.app.topic.TopicTab
import org.oppia.app.utility.EspressoTestsMatchers.withDrawable
import org.oppia.domain.topic.TEST_TOPIC_ID_0

/** Tests for [TopicPlayFragment]. */
@RunWith(AndroidJUnit4::class)
class TopicPlayFragmentTest {

  @get:Rule
  var activityTestRule: ActivityTestRule<TopicActivity> = ActivityTestRule(
    TopicActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )
  private var storyId = "test_story_id_0"

  @Before
  fun setUp() {
    Intents.init()
  }

  @Test
  fun testTopicPlayFragment_loadFragmentWithTopicTestId0_storyName_isCorrect() {
    activityTestRule.launchActivity(createTopicActivityIntent(TEST_TOPIC_ID_0))
    onView(
      allOf(
        withText(TopicTab.getTabForPosition(1).name),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    onView(
      atPosition(
        R.id.story_summary_recycler_view,
        1
      )
    ).check(matches(hasDescendant(withText(containsString("First Story")))))
  }

  @Test
  fun testTopicPlayFragment_loadFragmentWithTopicTestId0_chapterCountTextSingle_isCorrect() {
    activityTestRule.launchActivity(createTopicActivityIntent(TEST_TOPIC_ID_0))
    onView(
      allOf(
        withText(TopicTab.getTabForPosition(1).name),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    onView(
      atPosition(
        R.id.story_summary_recycler_view,
        1
      )
    ).check(matches(hasDescendant(withText(containsString("1 Chapter")))))
  }

  @Test
  fun testTopicPlayFragment_loadFragmentWithTopicTestId0_chapterCountTextMultiple_isCorrect() {
    activityTestRule.launchActivity(createTopicActivityIntent(TEST_TOPIC_ID_0))
    onView(
      allOf(
        withText(TopicTab.getTabForPosition(1).name),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    onView(
      atPosition(
        R.id.story_summary_recycler_view,
        2
      )
    ).check(matches(hasDescendant(withText(containsString("3 Chapters")))))
  }

  @Test

  fun testTopicPlayFragment_loadFragmentWithTopicTestId0_completeStoryProgress_isDisplayed() {
    activityTestRule.launchActivity(createTopicActivityIntent(TEST_TOPIC_ID_0))
    onView(
      allOf(
        withText(TopicTab.getTabForPosition(1).name),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    onView(
      atPosition(
        R.id.story_summary_recycler_view,
        1
      )
    ).check(matches(hasDescendant(withText(containsString("100%")))))
  }

  @Test
  fun testTopicPlayFragment_loadFragmentWithTopicTestId0_partialStoryProgress_isDisplayed() {
    activityTestRule.launchActivity(createTopicActivityIntent(TEST_TOPIC_ID_0))
    onView(
      allOf(
        withText(TopicTab.getTabForPosition(1).name),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    onView(
      atPosition(
        R.id.story_summary_recycler_view,
        2
      )
    ).check(matches(hasDescendant(withText(containsString("33%")))))
  }

  @Test
  @Ignore("Landscape not properly supported") // TODO(#56): Reenable once landscape is supported.
  fun testTopicPlayFragment_loadFragmentWithTopicTestId0_configurationChange_storyName_isCorrect() {
    activityTestRule.launchActivity(createTopicActivityIntent(TEST_TOPIC_ID_0))
    onView(
      allOf(
        withText(TopicTab.getTabForPosition(1).name),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    activityTestRule.activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    onView(
      atPosition(
        R.id.story_summary_recycler_view,
        1
      )
    ).check(matches(hasDescendant(withText(containsString("First Story")))))
  }

  @Test
  fun testTopicPlayFragment_loadFragmentWithTopicTestId0_clickStoryItem_opensStoryActivityWithCorrectIntent() {
    activityTestRule.launchActivity(createTopicActivityIntent(TEST_TOPIC_ID_0))
    onView(
      allOf(
        withText(TopicTab.getTabForPosition(1).name),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    onView(atPositionOnView(R.id.story_summary_recycler_view, 1, R.id.story_name_text_view)).perform(click())
    intended(hasComponent(StoryActivity::class.java.name))
    intended(hasExtra(StoryActivity.STORY_ACTIVITY_INTENT_EXTRA, storyId))
  }

  @Test
  fun testTopicPlayFragment_loadFragmentWithTopicTestId0_chapterListIsNotVisible() {
    activityTestRule.launchActivity(createTopicActivityIntent(TEST_TOPIC_ID_0))
    onView(atPositionOnView(R.id.story_summary_recycler_view, 1, R.id.chapter_recycler_view)).check(
      matches(
        not(
          isDisplayed()
        )
      )
    )
  }

  @Test
  fun testTopicPlayFragment_loadFragmentWithTopicTestId0_default_arrowDown() {
    activityTestRule.launchActivity(createTopicActivityIntent(TEST_TOPIC_ID_0))
    onView(
      allOf(
        withText(TopicTab.getTabForPosition(1).name),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    onView(atPositionOnView(R.id.story_summary_recycler_view, 1, R.id.chapter_list_drop_down_icon)).check(
      matches(
        withDrawable(R.drawable.ic_arrow_drop_down_black_24dp)
      )
    )
  }

  @Test
  fun testTopicPlayFragment_loadFragmentWithTopicTestId0_clickExpandListIcon_chapterListIsVisible() {
    activityTestRule.launchActivity(createTopicActivityIntent(TEST_TOPIC_ID_0))
    onView(
      allOf(
        withText(TopicTab.getTabForPosition(1).name),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    onView(atPositionOnView(R.id.story_summary_recycler_view, 1, R.id.story_container)).perform(click())
    onView(
      atPositionOnView(
        R.id.story_summary_recycler_view,
        1,
        R.id.chapter_recycler_view
      )
    ).check(matches(isDisplayed()))
  }

  @Test
  fun testTopicPlayFragment_loadFragmentWithTopicTestId0_clickChapter_opensExplorationActivity() {
    activityTestRule.launchActivity(createTopicActivityIntent(TEST_TOPIC_ID_0))
    onView(
      allOf(
        withText(TopicTab.getTabForPosition(1).name),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    onView(atPositionOnView(R.id.story_summary_recycler_view, 1, R.id.story_container)).perform(click())
    onView(
      atPositionOnView(
        R.id.story_summary_recycler_view,
        1,
        R.id.chapter_recycler_view
      )
    ).check(matches(hasDescendant(withId(R.id.chapter_container)))).perform(click())
    intended(hasComponent(ExplorationActivity::class.java.name))
  }

  @Test
  fun testTopicPlayFragment_loadFragmentWithTopicTestId0_clickExpandListIconIndex0_clickExpandListIconIndex1_chapterListForIndex0IsNotDisplayed() {
    activityTestRule.launchActivity(createTopicActivityIntent(TEST_TOPIC_ID_0))
    onView(
      allOf(
        withText(TopicTab.getTabForPosition(1).name),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    onView(atPositionOnView(R.id.story_summary_recycler_view, 1, R.id.story_container)).perform(click())
    onView(atPositionOnView(R.id.story_summary_recycler_view, 2, R.id.story_container)).perform(click())
    onView(
      atPositionOnView(
        R.id.story_summary_recycler_view,
        1,
        R.id.chapter_recycler_view
      )
    ).check(matches(not(isDisplayed())))
  }

  @Test
  fun testTopicPlayFragment_loadFragmentWithTopicTestId0_clickExpandListIconIndex1_clickExpandListIconIndex0_chapterListForIndex0IsNotDisplayed() {
    activityTestRule.launchActivity(createTopicActivityIntent(TEST_TOPIC_ID_0))
    onView(
      allOf(
        withText(TopicTab.getTabForPosition(1).name),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    onView(atPositionOnView(R.id.story_summary_recycler_view, 2, R.id.story_container)).perform(click())
    onView(atPositionOnView(R.id.story_summary_recycler_view, 1, R.id.story_container)).perform(click())
    onView(
      atPositionOnView(
        R.id.story_summary_recycler_view,
        2,
        R.id.chapter_recycler_view
      )
    ).check(matches(not(isDisplayed())))
  }

  @Test
  @Ignore("Landscape not properly supported") // TODO(#56): Reenable once landscape is supported.
  fun testTopicPlayFragment_loadFragmentWithTopicTestId0_clickExpandListIconIndex0_configurationChange_chapterListIsVisible() {
    ActivityScenario.launch(TopicActivity::class.java).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(1).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(atPositionOnView(R.id.story_summary_recycler_view, 1, R.id.story_container)).perform(click())
      it.onActivity { activity ->
        activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
      }
      onView(
        atPositionOnView(
          R.id.story_summary_recycler_view,
          1,
          R.id.chapter_recycler_view
        )
      ).check(matches(isDisplayed()))
    }
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private fun createTopicActivityIntent(topicId: String): Intent {
    return TopicActivity.createTopicActivityIntent(
      ApplicationProvider.getApplicationContext(), topicId
    )
  }
}
