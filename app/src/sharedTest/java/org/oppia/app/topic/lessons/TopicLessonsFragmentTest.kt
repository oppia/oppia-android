package org.oppia.app.topic.lessons

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
import org.oppia.domain.exploration.TEST_EXPLORATION_ID_30
import org.oppia.domain.topic.TEST_STORY_ID_0
import org.oppia.domain.topic.TEST_TOPIC_ID_0

/** Tests for [TopicLessonsFragment]. */
@RunWith(AndroidJUnit4::class)
class TopicLessonsFragmentTest {

  @get:Rule
  var activityTestRule: ActivityTestRule<TopicActivity> = ActivityTestRule(
    TopicActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )
  private val internalProfileId = 0
  private var storyId = "test_story_id_0"

  @Before
  fun setUp() {
    Intents.init()
  }

  @Test
  fun testLessonsPlayFragment_loadFragmentWithTopicTestId0_storyName_isCorrect() {
    activityTestRule.launchActivity(createTopicActivityIntent(internalProfileId, TEST_TOPIC_ID_0))
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
  fun testLessonsPlayFragment_loadFragmentWithTopicTestId0_chapterCountTextSingle_isCorrect() {
    activityTestRule.launchActivity(createTopicActivityIntent(internalProfileId, TEST_TOPIC_ID_0))
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
  fun testLessonsPlayFragment_loadFragmentWithTopicTestId0_chapterCountTextMultiple_isCorrect() {
    activityTestRule.launchActivity(createTopicActivityIntent(internalProfileId, TEST_TOPIC_ID_0))
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
  @Ignore("No dummy progress supported now.") // TODO(#734): Update test case as per new StoryProgress and TopicProgress.
  fun testLessonsPlayFragment_loadFragmentWithTopicTestId0_completeStoryProgress_isDisplayed() {
    activityTestRule.launchActivity(createTopicActivityIntent(internalProfileId, TEST_TOPIC_ID_0))
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
  @Ignore("No dummy progress supported now.") // TODO(#734): Update test case as per new StoryProgress and TopicProgress.
  fun testLessonsPlayFragment_loadFragmentWithTopicTestId0_partialStoryProgress_isDisplayed() {
    activityTestRule.launchActivity(createTopicActivityIntent(internalProfileId, TEST_TOPIC_ID_0))
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
  fun testLessonsPlayFragment_loadFragmentWithTopicTestId0_configurationChange_storyName_isCorrect() {
    activityTestRule.launchActivity(createTopicActivityIntent(internalProfileId, TEST_TOPIC_ID_0))
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
  fun testLessonsPlayFragment_loadFragmentWithTopicTestId0_clickStoryItem_opensStoryActivityWithCorrectIntent() {
    activityTestRule.launchActivity(createTopicActivityIntent(internalProfileId, TEST_TOPIC_ID_0))
    onView(
      allOf(
        withText(TopicTab.getTabForPosition(1).name),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    onView(atPositionOnView(R.id.story_summary_recycler_view, 1, R.id.story_name_text_view)).perform(click())
    intended(hasComponent(StoryActivity::class.java.name))
    intended(hasExtra(StoryActivity.STORY_ACTIVITY_INTENT_EXTRA_STORY_ID, storyId))
  }

  @Test
  fun testLessonsPlayFragment_loadFragmentWithTopicTestId0_chapterListIsNotVisible() {
    activityTestRule.launchActivity(createTopicActivityIntent(internalProfileId, TEST_TOPIC_ID_0))
    onView(atPositionOnView(R.id.story_summary_recycler_view, 1, R.id.chapter_recycler_view)).check(
      matches(
        not(
          isDisplayed()
        )
      )
    )
  }

  @Test
  fun testLessonsPlayFragment_loadFragmentWithTopicTestId0_default_arrowDown() {
    activityTestRule.launchActivity(createTopicActivityIntent(internalProfileId, TEST_TOPIC_ID_0))
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
  fun testLessonsPlayFragment_loadFragmentWithTopicTestId0_clickExpandListIcon_chapterListIsVisible() {
    activityTestRule.launchActivity(createTopicActivityIntent(internalProfileId, TEST_TOPIC_ID_0))
    onView(
      allOf(
        withText(TopicTab.getTabForPosition(1).name),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    onView(atPositionOnView(R.id.story_summary_recycler_view, 1, R.id.chapter_list_drop_down_icon)).perform(click())
    onView(
      atPositionOnView(
        R.id.story_summary_recycler_view,
        1,
        R.id.chapter_recycler_view
      )
    ).check(matches(isDisplayed()))
  }

  @Test
  fun testLessonsPlayFragment_loadFragmentWithTopicTestId0_clickChapter_opensExplorationActivity() {
    activityTestRule.launchActivity(createTopicActivityIntent(internalProfileId, TEST_TOPIC_ID_0))
    onView(
      allOf(
        withText(TopicTab.getTabForPosition(1).name),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    onView(atPositionOnView(R.id.story_summary_recycler_view, 1, R.id.chapter_list_drop_down_icon)).perform(click())
    onView(
      atPositionOnView(
        R.id.story_summary_recycler_view,
        1,
        R.id.chapter_recycler_view
      )
    ).check(matches(hasDescendant(withId(R.id.chapter_container)))).perform(click())
    intended(hasComponent(ExplorationActivity::class.java.name))
    intended(hasExtra(ExplorationActivity.EXPLORATION_ACTIVITY_PROFILE_ID_ARGUMENT_KEY, internalProfileId))
    intended(hasExtra(ExplorationActivity.EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY, TEST_TOPIC_ID_0))
    intended(hasExtra(ExplorationActivity.EXPLORATION_ACTIVITY_STORY_ID_ARGUMENT_KEY, TEST_STORY_ID_0))
    intended(hasExtra(ExplorationActivity.EXPLORATION_ACTIVITY_EXPLORATION_ID_ARGUMENT_KEY, TEST_EXPLORATION_ID_30))
  }

  @Test
  fun testLessonsPlayFragment_loadFragmentWithTopicTestId0_clickExpandListIconIndex0_clickExpandListIconIndex1_chapterListForIndex0IsNotDisplayed() {
    activityTestRule.launchActivity(createTopicActivityIntent(internalProfileId, TEST_TOPIC_ID_0))
    onView(
      allOf(
        withText(TopicTab.getTabForPosition(1).name),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    onView(atPositionOnView(R.id.story_summary_recycler_view, 1, R.id.chapter_list_drop_down_icon)).perform(click())
    onView(atPositionOnView(R.id.story_summary_recycler_view, 2, R.id.chapter_list_drop_down_icon)).perform(click())
    onView(
      atPositionOnView(
        R.id.story_summary_recycler_view,
        1,
        R.id.chapter_recycler_view
      )
    ).check(matches(not(isDisplayed())))
  }

  @Test
  fun testLessonsPlayFragment_loadFragmentWithTopicTestId0_clickExpandListIconIndex1_clickExpandListIconIndex0_chapterListForIndex0IsNotDisplayed() {
    activityTestRule.launchActivity(createTopicActivityIntent(internalProfileId, TEST_TOPIC_ID_0))
    onView(
      allOf(
        withText(TopicTab.getTabForPosition(1).name),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    onView(atPositionOnView(R.id.story_summary_recycler_view, 2, R.id.chapter_list_drop_down_icon)).perform(click())
    onView(atPositionOnView(R.id.story_summary_recycler_view, 1, R.id.chapter_list_drop_down_icon)).perform(click())
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
  fun testLessonsPlayFragment_loadFragmentWithTopicTestId0_clickExpandListIconIndex0_configurationChange_chapterListIsVisible() {
    ActivityScenario.launch(TopicActivity::class.java).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(1).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(atPositionOnView(R.id.story_summary_recycler_view, 1, R.id.chapter_list_drop_down_icon)).perform(click())
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

  private fun createTopicActivityIntent(internalProfileId: Int, topicId: String): Intent {
    return TopicActivity.createTopicActivityIntent(
      ApplicationProvider.getApplicationContext(),
      internalProfileId,
      topicId
    )
  }
}
