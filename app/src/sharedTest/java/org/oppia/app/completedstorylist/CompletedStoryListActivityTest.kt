package org.oppia.app.completedstorylist

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.hamcrest.CoreMatchers.containsString
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.model.ProfileId
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
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

/** Tests for [CompletedStoryListActivity]. */
@RunWith(AndroidJUnit4::class)
class CompletedStoryListActivityTest {

  private val internalProfileId = 0
  @Inject lateinit var storyProfileTestHelper: StoryProgressTestHelper

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()

    val profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    storyProfileTestHelper.markFullStoryProgressForFractions(profileId)
    storyProfileTestHelper.markFullStoryPartialTopicProgressForRatios(profileId)
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    DaggerCompletedStoryListActivityTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  fun testCompletedStoryList_checkItem0_titleIsCorrect() {
    launch<CompletedStoryListActivity>(createCompletedStoryListActivityIntent(internalProfileId)).use {
      onView(withId(R.id.completed_story_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(atPositionOnView(R.id.completed_story_list, 1, R.id.topic_name_text_view)).check(
        matches(
          withText(containsString("Ratios and Proportional Reasoning"))
        )
      )
    }
  }

  @Test
  fun testCompletedStoryList_checkItem0_storyNameIsCorrect() {
    launch<CompletedStoryListActivity>(createCompletedStoryListActivityIntent(internalProfileId)).use {
      onView(withId(R.id.completed_story_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(atPositionOnView(R.id.completed_story_list, 0, R.id.completed_story_name_text_view)).check(
        matches(
          withText(containsString("Matthew Goes to the Bakery"))
        )
      )
    }
  }

  @Test
  fun testCompletedStoryList_checkItem1_titleIsCorrect() {
    launch<CompletedStoryListActivity>(createCompletedStoryListActivityIntent(internalProfileId)).use {
      onView(withId(R.id.completed_story_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(atPositionOnView(R.id.completed_story_list, 0, R.id.topic_name_text_view)).check(
        matches(
          withText(containsString("Fractions"))
        )
      )
    }
  }

  @Test
  fun testCompletedStoryList_checkItem1_storyNameIsCorrect() {
    launch<CompletedStoryListActivity>(createCompletedStoryListActivityIntent(internalProfileId)).use {
      onView(withId(R.id.completed_story_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(atPositionOnView(R.id.completed_story_list, 1, R.id.completed_story_name_text_view)).check(
        matches(
          withText(containsString("Ratios: Part 1"))
        )
      )
    }
  }

  private fun createCompletedStoryListActivityIntent(internalProfileId: Int): Intent {
    return CompletedStoryListActivity.createCompletedStoryListActivityIntent(
      ApplicationProvider.getApplicationContext(),
      internalProfileId
    )
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

    fun inject(CompletedStoryListActivityTest: CompletedStoryListActivityTest)
  }
}
