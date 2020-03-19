package org.oppia.app.home

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
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
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.home.recentlyplayed.RecentlyPlayedActivity
import org.oppia.app.model.ProfileId
import org.oppia.app.player.exploration.ExplorationActivity
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.utility.EspressoTestsMatchers.withDrawable
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.domain.profile.ProfileTestHelper
import org.oppia.domain.topic.FRACTIONS_EXPLORATION_ID_0
import org.oppia.domain.topic.FRACTIONS_STORY_ID_0
import org.oppia.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.domain.topic.StoryProgressTestHelper
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

/** Tests for [RecentlyPlayedActivity]. */
@RunWith(AndroidJUnit4::class)
class RecentlyPlayedFragmentTest {

  @Inject lateinit var profileTestHelper: ProfileTestHelper
  @Inject lateinit var storyProgressTestHelper: StoryProgressTestHelper
  @Inject lateinit var context: Context

  private val internalProfileId = 0

  private lateinit var profileId: ProfileId

  @Before
  @ExperimentalCoroutinesApi
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles()
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    storyProgressTestHelper.markRecentlyPlayedForFractionsStory0Exploration0(profileId, timestampOlderThanAWeek = false)
    storyProgressTestHelper.markRecentlyPlayedForRatiosStory0Exploration0(profileId, timestampOlderThanAWeek = true)
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    DaggerRecentlyPlayedFragmentTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  private fun createRecentlyPlayedActivityIntent(profileId: Int): Intent {
    return RecentlyPlayedActivity.createRecentlyPlayedActivityIntent(
      ApplicationProvider.getApplicationContext(),
      profileId
    )
  }

  @Test
  fun testRecentlyPlayedTestActivity_clickOnToolbarNavigationButton_closeActivity() {
    ActivityScenario.launch<RecentlyPlayedActivity>(createRecentlyPlayedActivityIntent(internalProfileId)).use {
      onView(withId(R.id.recently_played_toolbar)).perform(click())
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_toolbarTitle_isDisplayedSuccessfully() {
    ActivityScenario.launch<RecentlyPlayedActivity>(createRecentlyPlayedActivityIntent(internalProfileId)).use {
      onView(
        allOf(
          instanceOf(TextView::class.java),
          withParent(withId(R.id.recently_played_toolbar))
        )
      ).check(matches(withText(R.string.recently_played_activity)))
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_recyclerViewItem0_doesNotShowSectionDivider() {
    ActivityScenario.launch<RecentlyPlayedActivity>(createRecentlyPlayedActivityIntent(internalProfileId)).use {
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
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
  fun testRecentlyPlayedTestActivity_recyclerViewItem0_showsLastWeekSectionTitle() {
    ActivityScenario.launch<RecentlyPlayedActivity>(createRecentlyPlayedActivityIntent(internalProfileId)).use {
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
  fun testRecentlyPlayedTestActivity_recyclerViewItem1_chapterNameIsCorrect() {
    ActivityScenario.launch<RecentlyPlayedActivity>(createRecentlyPlayedActivityIntent(internalProfileId)).use {
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
          withText(containsString("What is a Fraction?"))
        )
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_recyclerViewItem1_storyNameIsCorrect() {
    ActivityScenario.launch<RecentlyPlayedActivity>(createRecentlyPlayedActivityIntent(internalProfileId)).use {
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
  fun testRecentlyPlayedTestActivity_recyclerViewItem1_topicNameIsCorrect() {
    ActivityScenario.launch<RecentlyPlayedActivity>(createRecentlyPlayedActivityIntent(internalProfileId)).use {
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
  fun testRecentlyPlayedTestActivity_recyclerViewItem1_lessonThumbnailIsCorrect() {
    ActivityScenario.launch<RecentlyPlayedActivity>(createRecentlyPlayedActivityIntent(internalProfileId)).use {
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
  fun testRecentlyPlayedTestActivity_recyclerViewItem1_clickStory_intentsToExplorationActivity() {
    ActivityScenario.launch<RecentlyPlayedActivity>(createRecentlyPlayedActivityIntent(internalProfileId)).use {
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
          hasExtra(ExplorationActivity.EXPLORATION_ACTIVITY_EXPLORATION_ID_ARGUMENT_KEY, FRACTIONS_EXPLORATION_ID_0),
          hasExtra(ExplorationActivity.EXPLORATION_ACTIVITY_STORY_ID_ARGUMENT_KEY, FRACTIONS_STORY_ID_0),
          hasExtra(ExplorationActivity.EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY, FRACTIONS_TOPIC_ID),
          hasExtra(ExplorationActivity.EXPLORATION_ACTIVITY_PROFILE_ID_ARGUMENT_KEY, internalProfileId),
          hasComponent(ExplorationActivity::class.java.name)
        )
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_recyclerViewItem2_showsLastMonthSectionTitle() {
    ActivityScenario.launch<RecentlyPlayedActivity>(createRecentlyPlayedActivityIntent(internalProfileId)).use {
      onView(withId(R.id.ongoing_story_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(2))
      onView(
        atPositionOnView(
          R.id.ongoing_story_recycler_view,
          2,
          R.id.section_title_text_view
        )
      ).check(matches(withText(R.string.ongoing_story_last_month)))
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_recyclerViewItem2_showsSectionDivider() {
    ActivityScenario.launch<RecentlyPlayedActivity>(createRecentlyPlayedActivityIntent(internalProfileId)).use {
      onView(withId(R.id.ongoing_story_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(2))
      onView(atPositionOnView(R.id.ongoing_story_recycler_view, 2, R.id.divider_view)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_changeConfiguration_toolbarTitle_isDisplayedSuccessfully() {
    ActivityScenario.launch<RecentlyPlayedActivity>(createRecentlyPlayedActivityIntent(internalProfileId)).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          instanceOf(TextView::class.java),
          withParent(withId(R.id.recently_played_toolbar))
        )
      ).check(matches(withText(R.string.recently_played_activity)))
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_changeConfiguration_recyclerViewItem0_doesNotShowSectionDivider() {
    ActivityScenario.launch<RecentlyPlayedActivity>(createRecentlyPlayedActivityIntent(internalProfileId)).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
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
  fun testRecentlyPlayedTestActivity_changeConfiguration_recyclerViewItem0_showsLastWeekSectionTitle() {
    ActivityScenario.launch<RecentlyPlayedActivity>(createRecentlyPlayedActivityIntent(internalProfileId)).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
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
  fun testRecentlyPlayedTestActivity_changeConfiguration_recyclerViewItem1_storyNameIsCorrect() {
    ActivityScenario.launch<RecentlyPlayedActivity>(createRecentlyPlayedActivityIntent(internalProfileId)).use {
      onView(isRoot()).perform(orientationLandscape())
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
  fun testRecentlyPlayedTestActivity_changeConfiguration_recyclerViewItem1_topicNameIsCorrect() {
    ActivityScenario.launch<RecentlyPlayedActivity>(createRecentlyPlayedActivityIntent(internalProfileId)).use {
      onView(isRoot()).perform(orientationLandscape())
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
  fun testRecentlyPlayedTestActivity_changeConfiguration_recyclerViewItem1_lessonThumbnailIsCorrect() {
    ActivityScenario.launch<RecentlyPlayedActivity>(createRecentlyPlayedActivityIntent(internalProfileId)).use {
      onView(isRoot()).perform(orientationLandscape())
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
  fun testRecentlyPlayedTestActivity_changeConfiguration_recyclerViewItem2_showsLastMonthSectionTitle() {
    ActivityScenario.launch<RecentlyPlayedActivity>(createRecentlyPlayedActivityIntent(internalProfileId)).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.ongoing_story_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(2))
      onView(
        atPositionOnView(
          R.id.ongoing_story_recycler_view,
          2,
          R.id.section_title_text_view
        )
      ).check(matches(withText(R.string.ongoing_story_last_month)))
    }
  }

  @Qualifier annotation class TestDispatcher

  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    @ExperimentalCoroutinesApi
    @Singleton
    @Provides
    @TestDispatcher
    fun provideTestDispatcher(): CoroutineDispatcher {
      return TestCoroutineDispatcher()
    }

    @Singleton
    @Provides
    @BackgroundDispatcher
    fun provideBackgroundDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
    }

    @Singleton
    @Provides
    @BlockingDispatcher
    fun provideBlockingDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
    }

    // TODO(#59): Either isolate these to their own shared test module, or use the real logging
    // module in tests to avoid needing to specify these settings for tests.
    @EnableConsoleLog
    @Provides
    fun provideEnableConsoleLog(): Boolean = true

    @EnableFileLog
    @Provides
    fun provideEnableFileLog(): Boolean = false

    @GlobalLogLevel
    @Provides
    fun provideGlobalLogLevel(): LogLevel = LogLevel.VERBOSE
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

    fun inject(recentlyPlayedFragmentTest: RecentlyPlayedFragmentTest)
  }
}
