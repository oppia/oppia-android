package org.oppia.app.story

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
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
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.util.HumanReadables
import androidx.test.espresso.util.TreeIterables
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.model.ProfileId
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.hasItemCount
import org.oppia.app.testing.StoryFragmentTestActivity
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.domain.topic.FRACTIONS_STORY_ID_0
import org.oppia.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.domain.topic.StoryProgressTestHelper
import org.oppia.testing.TestCoroutineDispatchers
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

/** Tests for [StoryFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class StoryFragmentTest {

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var storyProgressTestHelper: StoryProgressTestHelper

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  private val internalProfileId = 0

  private lateinit var profileId: ProfileId

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    IdlingRegistry.getInstance().register(MainThreadExecutor.countingResource)
    profileTestHelper.initializeProfiles()
    FirebaseApp.initializeApp(context)
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    storyProgressTestHelper.markPartialStoryProgressForFractions(
      profileId, /* timestampOlderThanAWeek= */
      false
    )
    testCoroutineDispatchers.runCurrent()
  }

  @After
  fun tearDown() {
    IdlingRegistry.getInstance().unregister(MainThreadExecutor.countingResource)
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    DaggerStoryFragmentTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  private fun createStoryActivityIntent(): Intent {
    return StoryActivity.createStoryActivityIntent(
      ApplicationProvider.getApplicationContext(),
      internalProfileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0
    )
  }

  @Test
  fun testStoryFragment_clickOnToolbarNavigationButton_closeActivity() {
    launch<StoryFragmentTestActivity>(createStoryActivityIntent()).use {
      onView(withId(R.id.story_toolbar)).perform(click())
    }
  }

  @Test
  fun testStoryFragment_toolbarTitle_isDisplayedSuccessfully() {
    launch<StoryFragmentTestActivity>(createStoryActivityIntent()).use {
      waitForTheView(withText("Chapter 1: What is a Fraction?"))
      onView(
        allOf(
          instanceOf(TextView::class.java),
          withParent(withId(R.id.story_toolbar))
        )
      ).check(matches(withText("Matthew Goes to the Bakery")))
    }
  }

  @Test
  fun testStoryFragment_correctStoryCountLoadedInHeader() {
    launch<StoryFragmentTestActivity>(createStoryActivityIntent()).use {
      val headerString: String =
        getResources().getQuantityString(R.plurals.story_total_chapters, 2, 1, 2)
      waitForTheView(withText("Chapter 1: What is a Fraction?"))
      onView(withId(R.id.story_chapter_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(
          R.id.story_chapter_list,
          0,
          R.id.story_progress_chapter_completed_text
        )
      ).check(
        matches(
          withText(headerString)
        )
      )
    }
  }

  @Test
  fun testStoryFragment_correctNumberOfStoriesLoadedInRecyclerView() {
    launch<StoryFragmentTestActivity>(createStoryActivityIntent()).use {
      waitForTheView(withText("Chapter 1: What is a Fraction?"))
      onView(withId(R.id.story_chapter_list)).check(hasItemCount(3))
    }
  }

  @Test
  fun testStoryFragment_changeConfiguration_textViewIsShownCorrectly() {
    launch<StoryFragmentTestActivity>(createStoryActivityIntent()).use {
      onView(isRoot()).perform(orientationLandscape())
      waitForTheView(withText("Chapter 1: What is a Fraction?"))
      onView(allOf(withId(R.id.story_chapter_list))).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(atPositionOnView(R.id.story_chapter_list, 1, R.id.chapter_title)).check(
        matches(
          withText("Chapter 1: What is a Fraction?")
        )
      )
    }
  }

  @Test
  fun testStoryFragment_chapterSummaryIsShownCorrectly() {
    launch<StoryFragmentTestActivity>(createStoryActivityIntent()).use {
      waitForTheView(withText("Chapter 1: What is a Fraction?"))
      onView(allOf(withId(R.id.story_chapter_list))).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(atPositionOnView(R.id.story_chapter_list, 1, R.id.chapter_summary)).check(
        matches(
          withText("This is outline/summary for What is a Fraction?")
        )
      )
    }
  }

  @Test
  fun testStoryFragment_changeConfiguration_chapterSummaryIsShownCorrectly() {
    launch<StoryFragmentTestActivity>(createStoryActivityIntent()).use {
      onView(isRoot()).perform(orientationLandscape())
      waitForTheView(withText("Chapter 1: What is a Fraction?"))
      onView(allOf(withId(R.id.story_chapter_list))).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(atPositionOnView(R.id.story_chapter_list, 1, R.id.chapter_summary)).check(
        matches(
          withText("This is outline/summary for What is a Fraction?")
        )
      )
    }
  }

  @Test
  fun testStoryFragment_changeConfiguration_correctStoryCountInHeader() {
    launch<StoryFragmentTestActivity>(createStoryActivityIntent()).use {
      onView(isRoot()).perform(orientationLandscape())
      val headerString: String =
        getResources().getQuantityString(R.plurals.story_total_chapters, 2, 1, 2)
      onView(withId(R.id.story_chapter_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      waitForTheView(withText(headerString))
      onView(withId(R.id.story_chapter_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(
          R.id.story_chapter_list,
          0,
          R.id.story_progress_chapter_completed_text
        )
      ).check(
        matches(
          withText(headerString)
        )
      )
    }
  }

  private fun getResources(): Resources {
    return ApplicationProvider.getApplicationContext<Context>().resources
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
    TestModule::class, TestLogReportingModule::class, TestDispatcherModule::class
  ])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(storyFragmentTest: StoryFragmentTest)
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
