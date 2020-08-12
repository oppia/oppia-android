package org.oppia.app.topic.lessons

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.util.HumanReadables
import androidx.test.espresso.util.TreeIterables
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.model.ProfileId
import org.oppia.app.player.exploration.ExplorationActivity
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.story.StoryActivity
import org.oppia.app.topic.TopicActivity
import org.oppia.app.topic.TopicTab
import org.oppia.app.utility.EspressoTestsMatchers.withDrawable
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.domain.oppialogger.LogStorageModule
import org.oppia.domain.topic.RATIOS_EXPLORATION_ID_0
import org.oppia.domain.topic.RATIOS_STORY_ID_0
import org.oppia.domain.topic.RATIOS_TOPIC_ID
import org.oppia.domain.topic.StoryProgressTestHelper
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.testing.profile.ProfileTestHelper
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.robolectric.annotation.LooperMode
import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [TopicLessonsFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class TopicLessonsFragmentTest {

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var storyProgressTestHelper: StoryProgressTestHelper

  private val internalProfileId = 0

  private lateinit var profileId: ProfileId

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    IdlingRegistry.getInstance().register(MainThreadExecutor.countingResource)
    profileTestHelper.initializeProfiles()
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    FirebaseApp.initializeApp(context)
  }

  @After
  fun tearDown() {
    IdlingRegistry.getInstance().unregister(MainThreadExecutor.countingResource)
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    DaggerTopicLessonsFragmentTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  private fun createTopicActivityIntent(internalProfileId: Int, topicId: String): Intent {
    return TopicActivity.createTopicActivityIntent(
      ApplicationProvider.getApplicationContext(),
      internalProfileId,
      topicId
    )
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_storyName_isCorrect() {
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
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
      ).check(matches(hasDescendant(withText(containsString("Ratios: Part 1")))))
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_chapterCountTextMultiple_isCorrect() {
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
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
      ).check(matches(hasDescendant(withText(containsString("2 Chapters")))))
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_completeStoryProgress_isDisplayed() {
    storyProgressTestHelper.markFullStoryPartialTopicProgressForRatios(
      profileId,
      /* timestampOlderThanAWeek= */ false
    )
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
      waitForTheView(withText("100%"))
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
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_partialStoryProgress_isDisplayed() {
    storyProgressTestHelper.markTwoPartialStoryProgressForRatios(
      profileId,
      /* timestampOlderThanAWeek= */ false
    )
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
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
      ).check(matches(hasDescendant(withText(containsString("50%")))))
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_configurationChange_storyName_isCorrect() {
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(1).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(
        atPosition(
          R.id.story_summary_recycler_view,
          1
        )
      ).check(matches(hasDescendant(withText(containsString("Ratios: Part 1")))))
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_clickStoryItem_opensStoryActivityWithCorrectIntent() {
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(1).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(
        atPositionOnView(
          R.id.story_summary_recycler_view,
          1,
          R.id.story_name_text_view
        )
      ).perform(click())
      intended(hasComponent(StoryActivity::class.java.name))
      intended(hasExtra(StoryActivity.STORY_ACTIVITY_INTENT_EXTRA_STORY_ID, RATIOS_STORY_ID_0))
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_chapterListIsNotVisible() {
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
      onView(
        atPositionOnView(R.id.story_summary_recycler_view, 1, R.id.chapter_recycler_view)
      ).check(
        matches(not(isDisplayed()))
      )
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_default_arrowDown() {
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(1).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(
        atPositionOnView(
          R.id.story_summary_recycler_view,
          1,
          R.id.chapter_list_drop_down_icon
        )
      ).check(
        matches(
          withDrawable(R.drawable.ic_arrow_drop_down_black_24dp)
        )
      )
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_clickExpandListIcon_chapterListIsVisible() {
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(1).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(
        atPositionOnView(
          R.id.story_summary_recycler_view,
          1,
          R.id.chapter_list_drop_down_icon
        )
      ).perform(click())
      onView(
        atPositionOnView(
          R.id.story_summary_recycler_view,
          1,
          R.id.chapter_recycler_view
        )
      ).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_clickChapter_opensExplorationActivity() {
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(1).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(
        atPositionOnView(
          R.id.story_summary_recycler_view,
          1,
          R.id.chapter_list_drop_down_icon
        )
      ).perform(click())
      onView(withId(R.id.story_summary_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          R.id.story_summary_recycler_view,
          1,
          R.id.chapter_recycler_view
        )
      ).check(matches(hasDescendant(withId(R.id.chapter_container)))).perform(click())
      intended(hasComponent(ExplorationActivity::class.java.name))
      intended(
        hasExtra(
          ExplorationActivity.EXPLORATION_ACTIVITY_PROFILE_ID_ARGUMENT_KEY,
          internalProfileId
        )
      )
      intended(
        hasExtra(
          ExplorationActivity.EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY,
          RATIOS_TOPIC_ID
        )
      )
      intended(
        hasExtra(
          ExplorationActivity.EXPLORATION_ACTIVITY_STORY_ID_ARGUMENT_KEY,
          RATIOS_STORY_ID_0
        )
      )
      intended(
        hasExtra(
          ExplorationActivity.EXPLORATION_ACTIVITY_EXPLORATION_ID_ARGUMENT_KEY,
          RATIOS_EXPLORATION_ID_0
        )
      )
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_clickExpandListIconIndex1_clickExpandListIconIndex2_chapterListForIndex1IsNotDisplayed() { // ktlint-disable max-line-length
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(1).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.story_summary_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          R.id.story_summary_recycler_view,
          1,
          R.id.chapter_list_drop_down_icon
        )
      ).perform(click())
      onView(withId(R.id.story_summary_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          2
        )
      )
      onView(
        atPositionOnView(
          R.id.story_summary_recycler_view,
          2,
          R.id.chapter_list_drop_down_icon
        )
      ).perform(click())
      onView(withId(R.id.story_summary_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          R.id.story_summary_recycler_view,
          1,
          R.id.chapter_recycler_view
        )
      ).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_clickExpandListIconIndex1_clickExpandListIconIndex0_chapterListForIndex0IsNotDisplayed() { // ktlint-disable max-line-length
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(1).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.story_summary_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          2
        )
      )
      onView(
        atPositionOnView(
          R.id.story_summary_recycler_view,
          2,
          R.id.chapter_list_drop_down_icon
        )
      ).perform(click())
      onView(withId(R.id.story_summary_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          R.id.story_summary_recycler_view,
          1,
          R.id.chapter_list_drop_down_icon
        )
      ).perform(click())
      onView(withId(R.id.story_summary_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          2
        )
      )
      onView(
        atPositionOnView(
          R.id.story_summary_recycler_view,
          2,
          R.id.chapter_recycler_view
        )
      ).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_clickExpandListIconIndex1_configurationChange_chapterListIsVisible() { // ktlint-disable max-line-length
    launch<TopicActivity>(createTopicActivityIntent(internalProfileId, RATIOS_TOPIC_ID)).use {
      onView(
        allOf(
          withText(TopicTab.getTabForPosition(1).name),
          isDescendantOfA(withId(R.id.topic_tabs_container))
        )
      ).perform(click())
      onView(
        atPositionOnView(
          R.id.story_summary_recycler_view,
          1,
          R.id.chapter_list_drop_down_icon
        )
      ).perform(click())
      onView(withId(R.id.story_summary_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(
        atPositionOnView(
          R.id.story_summary_recycler_view,
          1,
          R.id.chapter_recycler_view
        )
      ).check(matches(isDisplayed()))
    }
  }

  private fun waitForTheView(viewMatcher: Matcher<View>): ViewInteraction {
    return onView(isRoot()).perform(waitForMatch(viewMatcher, 30000L))
  }

  // TODO(#59): Remove these waits once we can ensure that the production executors are not depended on in tests.
  //  Sleeping is really bad practice in Espresso tests, and can lead to test flakiness. It shouldn't be necessary if we
  //  use a test executor service with a counting idle resource, but right now Gradle mixes dependencies such that both
  //  the test and production blocking executors are being used. The latter cannot be updated to notify Espresso of any
  //  active coroutines, so the test attempts to assert state before it's ready. This artificial delay in the Espresso
  //  thread helps to counter that.

  /**
   * Perform action of waiting for a specific matcher to finish. Adapted from:
   * https://stackoverflow.com/a/22563297/3689782.
   */
  private fun waitForMatch(viewMatcher: Matcher<View>, millis: Long): ViewAction {
    return object : ViewAction {
      override fun getDescription(): String {
        return "wait for a specific view with matcher <$viewMatcher> during $millis millis."
      }

      override fun getConstraints(): Matcher<View> {
        return isRoot()
      }

      override fun perform(uiController: UiController?, view: View?) {
        checkNotNull(uiController)
        uiController.loopMainThreadUntilIdle()
        val startTime = System.currentTimeMillis()
        val endTime = startTime + millis

        do {
          if (TreeIterables.breadthFirstViewTraversal(view).any { viewMatcher.matches(it) }) {
            return
          }
          uiController.loopMainThreadForAtLeast(50)
        } while (System.currentTimeMillis() < endTime)

        // Couldn't match in time.
        throw PerformException.Builder()
          .withActionDescription(description)
          .withViewDescription(HumanReadables.describe(view))
          .withCause(TimeoutException())
          .build()
      }
    }
  }

  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    // TODO(#59): Either isolate these to their own shared test module, or use the real logging
    //  module in tests to avoid needing to specify these settings for tests.
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
  @Component(modules = [
    TestModule::class, TestLogReportingModule::class, LogStorageModule::class,
    TestDispatcherModule::class
  ])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(topicLessonsFragmentTest: TopicLessonsFragmentTest)
  }

  // TODO(#59): Move this to a general-purpose testing library that replaces all CoroutineExecutors with an
  //  Espresso-enabled executor service. This service should also allow for background threads to run in both Espresso
  //  and Robolectric to help catch potential race conditions, rather than forcing parallel execution to be sequential
  //  and immediate.
  //  NB: This also blocks on #59 to be able to actually create a test-only library.

  /**
   * An executor service that schedules all [Runnable]s to run asynchronously on the main thread. This is based on:
   * https://android.googlesource.com/platform/packages/apps/TV/+/android-live-tv/src/com/android/tv/util/MainThreadExecutor.java.
   */
  private object MainThreadExecutor : AbstractExecutorService() {
    override fun isTerminated(): Boolean = false

    private val handler = Handler(Looper.getMainLooper())
    val countingResource =
      CountingIdlingResource("main_thread_executor_counting_idling_resource")

    override fun execute(command: Runnable?) {
      countingResource.increment()
      handler.post {
        try {
          command?.run()
        } finally {
          countingResource.decrement()
        }
      }
    }

    override fun shutdown() {
      throw UnsupportedOperationException()
    }

    override fun shutdownNow(): MutableList<Runnable> {
      throw UnsupportedOperationException()
    }

    override fun isShutdown(): Boolean = false

    override fun awaitTermination(timeout: Long, unit: TimeUnit?): Boolean {
      throw UnsupportedOperationException()
    }
  }
}
