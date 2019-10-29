package org.oppia.app.topic.play

import android.app.Application
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.player.exploration.ExplorationActivity
import org.oppia.app.recyclerview.RecyclerViewMatcher
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.story.StoryActivity
import org.oppia.app.topic.TopicActivity
import org.oppia.app.utility.EspressoTestsMatchers.withDrawable
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Singleton

/** Tests for [TopicPlayFragment]. */
@RunWith(AndroidJUnit4::class)
class TopicPlayFragmentTest {

  // TODO(#137): Add following test-cases once story-progress function is implemented and expandable list is introduced.
  //  - Click on arrow to show and hide expandable list is working correctly.
  //  - Expandable list is showing correct chapter names.
  //  - Upon configuration change expanded list should remain expanded.
  //  - Click on story-title or entire item should open [StoryActivity].
  //  - Click on chapter in expandable list should start exploration.

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
    activityTestRule.launchActivity(null)
    onView(
      atPosition(
        R.id.story_summary_recycler_view,
        0
      )
    ).check(matches(hasDescendant(withText(containsString("First Story")))))
  }

  @Test
  fun testTopicPlayFragment_loadFragmentWithTopicTestId0_chapterCountTextSingle_isCorrect() {
    activityTestRule.launchActivity(null)
    onView(
      atPosition(
        R.id.story_summary_recycler_view,
        0
      )
    ).check(matches(hasDescendant(withText(containsString("1 Chapter")))))
  }

  @Test
  fun testTopicPlayFragment_loadFragmentWithTopicTestId0_chapterCountTextMultiple_isCorrect() {
    activityTestRule.launchActivity(null)
    onView(
      atPosition(
        R.id.story_summary_recycler_view,
        1
      )
    ).check(matches(hasDescendant(withText(containsString("3 Chapters")))))
  }

  @Test

  fun testTopicPlayFragment_loadFragmentWithTopicTestId0_completeStoryProgress_isDisplayed() {
    activityTestRule.launchActivity(null)
    onView(
      atPosition(
        R.id.story_summary_recycler_view,
        0
      )
    ).check(matches(hasDescendant(withText(containsString("100%")))))
  }

  @Test
  fun testTopicPlayFragment_loadFragmentWithTopicTestId0_partialStoryProgress_isDisplayed() {
    activityTestRule.launchActivity(null)
    onView(
      atPosition(
        R.id.story_summary_recycler_view,
        1
      )
    ).check(matches(hasDescendant(withText(containsString("33%")))))
  }

  @Test
  fun testTopicPlayFragment_loadFragmentWithTopicTestId0_configurationChange_storyName_isCorrect() {
    activityTestRule.launchActivity(null)
    activityTestRule.activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    onView(
      atPosition(
        R.id.story_summary_recycler_view,
        0
      )
    ).check(matches(hasDescendant(withText(containsString("First Story")))))
  }

  @Test
  fun testTopicPlayFragment_loadFragmentWithTopicTestId0_clickStoryItem_opensStoryActivityWithCorrectIntent() {
    activityTestRule.launchActivity(null)
    onView(atPositionOnView(R.id.story_summary_recycler_view, 0, R.id.story_name_text_view)).perform(click())
    intended(hasComponent(StoryActivity::class.java.name))
    intended(hasExtra(StoryActivity.STORY_ACTIVITY_STORY_ID_ARGUMENT_KEY, storyId))
  }

  @Test
  fun testTopicPlayFragment_loadFragmentWithTopicTestId0_chapterListIsNotVisible() {
    activityTestRule.launchActivity(null)
    onView(atPositionOnView(R.id.story_summary_recycler_view, 0, R.id.chapter_recycler_view)).check(
      matches(
        not(
          isDisplayed()
        )
      )
    )
  }

  @Test
  fun testTopicPlayFragment_loadFragmentWithTopicTestId0_clickExpandListIcon_iconChanges() {
    activityTestRule.launchActivity(null)
    onView(atPositionOnView(R.id.story_summary_recycler_view, 0, R.id.chapter_list_view_control)).check(
      matches(
        withDrawable(R.drawable.ic_keyboard_arrow_down_black_24dp)
      )
    )
    onView(atPositionOnView(R.id.story_summary_recycler_view, 0, R.id.chapter_list_view_control)).perform(click())
    onView(atPositionOnView(R.id.story_summary_recycler_view, 0, R.id.chapter_list_view_control)).check(
      matches(
        withDrawable(R.drawable.ic_keyboard_arrow_up_black_24dp)
      )
    )
  }

  @Test
  fun testTopicPlayFragment_loadFragmentWithTopicTestId0_clickExpandListIcon_chapterListIsVisible() {
    activityTestRule.launchActivity(null)
    onView(atPositionOnView(R.id.story_summary_recycler_view, 0, R.id.chapter_list_view_control)).perform(click())
    onView(
      atPositionOnView(
        R.id.story_summary_recycler_view,
        0,
        R.id.chapter_recycler_view
      )
    ).check(matches(isDisplayed()))
  }

  @Test
  fun testTopicPlayFragment_loadFragmentWithTopicTestId0_clickChapter_opensExplorationActivity() {
    activityTestRule.launchActivity(null)
    onView(atPositionOnView(R.id.story_summary_recycler_view, 1, R.id.chapter_list_view_control)).perform(click())
    onView(
      allOf(
        withId(R.id.chapter_recycler_view),
        withParent(
          atPosition(R.id.story_summary_recycler_view, 1)
        )
      )
    ).check(matches(hasDescendant(withText(containsString("Second"))))).perform(click())
    intended(hasComponent(ExplorationActivity::class.java.name))
  }

  @Test
  fun testTopicPlayFragment_loadFragmentWithTopicTestId0_clickExpandListIconIndex0_clickExpandListIconIndex1_chapterListForIndex0IsNotDisplayed() {
    activityTestRule.launchActivity(null)
    onView(atPositionOnView(R.id.story_summary_recycler_view, 0, R.id.chapter_list_view_control)).perform(click())
    onView(atPositionOnView(R.id.story_summary_recycler_view, 1, R.id.chapter_list_view_control)).perform(click())
    onView(
      atPositionOnView(
        R.id.story_summary_recycler_view,
        0,
        R.id.chapter_recycler_view
      )
    ).check(matches(not(isDisplayed())))
  }

  @Test
  fun testTopicPlayFragment_loadFragmentWithTopicTestId0_clickExpandListIconIndex0_configurationChange_chapterListIsVisible() {
    activityTestRule.launchActivity(null)
    onView(atPositionOnView(R.id.story_summary_recycler_view, 0, R.id.chapter_list_view_control)).perform(click())
    activityTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    onView(
      atPositionOnView(
        R.id.story_summary_recycler_view,
        0,
        R.id.chapter_recycler_view
      )
    ).check(matches(isDisplayed()))
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    // TODO(#89): Introduce a proper IdlingResource for background dispatchers to ensure they all complete before
    //  proceeding in an Espresso test. This solution should also be interoperative with Robolectric contexts by using a
    //  test coroutine dispatcher.

    @Singleton
    @Provides
    @BackgroundDispatcher
    fun provideBackgroundDispatcher(@BlockingDispatcher blockingDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return blockingDispatcher
    }
  }

  @Singleton
  @Component(modules = [TestModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }
  }
}
