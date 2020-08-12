package org.oppia.app.home

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
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
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
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
import org.hamcrest.Matcher
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
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.hasGridItemCount
import org.oppia.app.utility.EspressoTestsMatchers.withDrawable
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.domain.oppialogger.LogStorageModule
import org.oppia.domain.topic.FRACTIONS_EXPLORATION_ID_0
import org.oppia.domain.topic.FRACTIONS_STORY_ID_0
import org.oppia.domain.topic.FRACTIONS_TOPIC_ID
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

/** Tests for [RecentlyPlayedActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class RecentlyPlayedFragmentTest {

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper
  @Inject
  lateinit var storyProgressTestHelper: StoryProgressTestHelper
  @Inject
  lateinit var context: Context

  private val internalProfileId = 0

  private lateinit var profileId: ProfileId

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    IdlingRegistry.getInstance().register(MainThreadExecutor.countingResource)
    profileTestHelper.initializeProfiles()
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    storyProgressTestHelper.markRecentlyPlayedForFractionsStory0Exploration0(
      profileId,
      timestampOlderThanAWeek = false
    )
    storyProgressTestHelper.markRecentlyPlayedForRatiosStory0Exploration0(
      profileId,
      timestampOlderThanAWeek = true
    )
    FirebaseApp.initializeApp(context)
  }

  @After
  fun tearDown() {
    IdlingRegistry.getInstance().unregister(MainThreadExecutor.countingResource)
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
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      onView(withId(R.id.recently_played_toolbar)).perform(click())
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_toolbarTitle_isDisplayedSuccessfully() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      onView(
        allOf(
          instanceOf(TextView::class.java),
          withParent(withId(R.id.recently_played_toolbar))
        )
      ).check(
        matches(withText(R.string.recently_played_activity))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_recyclerViewItem0_doesNotShowSectionDivider() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      waitForTheView(withText(R.string.ongoing_story_last_week))
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(R.id.ongoing_story_recycler_view, 0, R.id.divider_view)
      ).check(
        matches(not(isDisplayed()))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_recyclerViewItem0_showsLastWeekSectionTitle() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      waitForTheView(withText(R.string.ongoing_story_last_week))
      onView(
        atPositionOnView(R.id.ongoing_story_recycler_view, 0, R.id.section_title_text_view)
      ).check(
        matches(withText(R.string.ongoing_story_last_week))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_recyclerViewItem1_storyNameIsCorrect() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      waitForTheView(withText("Matthew Goes to the Bakery"))
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(R.id.ongoing_story_recycler_view, 1, R.id.story_name_text_view)
      ).check(
        matches(withText(containsString("Matthew Goes to the Bakery")))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_recyclerViewItem1_topicNameIsCorrect() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      waitForTheView(withText("FRACTIONS"))
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(R.id.ongoing_story_recycler_view, 1, R.id.topic_name_text_view)
      ).check(
        matches(withText(containsString("FRACTIONS")))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_recyclerViewItem1_lessonThumbnailIsCorrect() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      waitForTheView(withText("FRACTIONS"))
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(R.id.ongoing_story_recycler_view, 1, R.id.lesson_thumbnail)
      ).check(
        matches(withDrawable(R.drawable.lesson_thumbnail_graphic_duck_and_chicken))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_recyclerViewItem1_clickStory_intentsToExplorationActivity() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      waitForTheView(withText("FRACTIONS"))
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(R.id.ongoing_story_recycler_view, 1, R.id.lesson_thumbnail)
      ).perform(click())
      intended(
        allOf(
          hasExtra(
            ExplorationActivity.EXPLORATION_ACTIVITY_EXPLORATION_ID_ARGUMENT_KEY,
            FRACTIONS_EXPLORATION_ID_0
          ),
          hasExtra(
            ExplorationActivity.EXPLORATION_ACTIVITY_STORY_ID_ARGUMENT_KEY,
            FRACTIONS_STORY_ID_0
          ),
          hasExtra(
            ExplorationActivity.EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY,
            FRACTIONS_TOPIC_ID
          ),
          hasExtra(
            ExplorationActivity.EXPLORATION_ACTIVITY_PROFILE_ID_ARGUMENT_KEY,
            internalProfileId
          ),
          hasComponent(ExplorationActivity::class.java.name)
        )
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_recyclerViewItem2_showsLastMonthSectionTitle() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      waitForTheView(withText(R.string.ongoing_story_last_month))
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          2
        )
      )
      onView(
        atPositionOnView(R.id.ongoing_story_recycler_view, 2, R.id.section_title_text_view)
      ).check(
        matches(withText(R.string.ongoing_story_last_month))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_recyclerViewItem2_showsSectionDivider() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      waitForTheView(withText(R.string.ongoing_story_last_month))
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          2
        )
      )
      onView(
        atPositionOnView(
          R.id.ongoing_story_recycler_view,
          2,
          R.id.divider_view
        )
      ).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_changeConfiguration_toolbarTitle_isDisplayedSuccessfully() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(instanceOf(TextView::class.java), withParent(withId(R.id.recently_played_toolbar)))
      ).check(
        matches(withText(R.string.recently_played_activity))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_changeConfiguration_recyclerViewItem0_doesNotShowSectionDivider() { // ktlint-disable max-line-length
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      waitForTheView(withText(R.string.ongoing_story_last_week))
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(R.id.ongoing_story_recycler_view, 0, R.id.divider_view)
      ).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_changeConfiguration_recyclerViewItem0_showsLastWeekSectionTitle() { // ktlint-disable max-line-length
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      waitForTheView(withText(R.string.ongoing_story_last_week))
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(R.id.ongoing_story_recycler_view, 0, R.id.section_title_text_view)
      ).check(
        matches(withText(R.string.ongoing_story_last_week))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_changeConfiguration_recyclerViewItem1_storyNameIsCorrect() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      waitForTheView(withText("Matthew Goes to the Bakery"))
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(R.id.ongoing_story_recycler_view, 1, R.id.story_name_text_view)
      ).check(
        matches(withText(containsString("Matthew Goes to the Bakery")))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_changeConfiguration_recyclerViewItem1_topicNameIsCorrect() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      waitForTheView(withText("FRACTIONS"))
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(R.id.ongoing_story_recycler_view, 1, R.id.topic_name_text_view)
      ).check(
        matches(withText(containsString("FRACTIONS")))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_changeConfiguration_recyclerViewItem1_lessonThumbnailIsCorrect() { // ktlint-disable max-line-length
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      waitForTheView(withText("FRACTIONS"))
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(R.id.ongoing_story_recycler_view, 1, R.id.lesson_thumbnail)
      ).check(
        matches(withDrawable(R.drawable.lesson_thumbnail_graphic_duck_and_chicken))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_changeConfiguration_recyclerViewItem2_showsLastMonthSectionTitle() { // ktlint-disable max-line-length
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      waitForTheView(withText(R.string.ongoing_story_last_month))
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          2
        )
      )
      onView(
        atPositionOnView(R.id.ongoing_story_recycler_view, 2, R.id.section_title_text_view)
      ).check(
        matches(withText(R.string.ongoing_story_last_month))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_checkSpanForItem0_spanSizeIsTwoOrThree() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      waitForTheView(withText(R.string.ongoing_story_last_week))
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      if (context.resources.getBoolean(R.bool.isTablet)) {
        onView(withId(R.id.ongoing_story_recycler_view)).check(
          hasGridItemCount(
            3, 0
          )
        )
      } else {
        onView(withId(R.id.ongoing_story_recycler_view)).check(
          hasGridItemCount(
            2, 0
          )
        )
      }
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_checkSpanForItem1_spanSizeIsOne() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      waitForTheView(withText(R.string.ongoing_story_last_week))
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(withId(R.id.ongoing_story_recycler_view)).check(
        hasGridItemCount(
          1, 1
        )
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_checkSpanForItem2_spanSizeIsTwoOrThree() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      waitForTheView(withText(R.string.ongoing_story_last_week))
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          2
        )
      )
      if (context.resources.getBoolean(R.bool.isTablet)) {
        onView(withId(R.id.ongoing_story_recycler_view)).check(
          hasGridItemCount(
            3, 2
          )
        )
      } else {
        onView(withId(R.id.ongoing_story_recycler_view)).check(
          hasGridItemCount(
            2, 2
          )
        )
      }
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_checkSpanForItem3_spanSizeIsOne() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      waitForTheView(withText(R.string.ongoing_story_last_week))
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          3
        )
      )
      onView(withId(R.id.ongoing_story_recycler_view)).check(
        hasGridItemCount(
          1, 3
        )
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_configurationChange_checkSpanForItem0_spanSizeIsThreeOrFour() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      waitForTheView(withText(R.string.ongoing_story_last_week))
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      if (context.resources.getBoolean(R.bool.isTablet)) {
        onView(withId(R.id.ongoing_story_recycler_view)).check(
          hasGridItemCount(
            4, 0
          )
        )
      } else {
        onView(withId(R.id.ongoing_story_recycler_view)).check(
          hasGridItemCount(
            3, 0
          )
        )
      }
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_configurationChange_checkSpanForItem1_spanSizeIsOne() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      waitForTheView(withText(R.string.ongoing_story_last_week))
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(withId(R.id.ongoing_story_recycler_view)).check(
        hasGridItemCount(
          1, 1
        )
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_configurationChange_checkSpanForItem2_spanSizeIsThreeOrFour() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      waitForTheView(withText(R.string.ongoing_story_last_week))
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          2
        )
      )
      if (context.resources.getBoolean(R.bool.isTablet)) {
        onView(withId(R.id.ongoing_story_recycler_view)).check(
          hasGridItemCount(
            4, 2
          )
        )
      } else {
        onView(withId(R.id.ongoing_story_recycler_view)).check(
          hasGridItemCount(
            3, 2
          )
        )
      }
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_configurationChange_checkSpanForItem3_spanSizeIsOne() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId
      )
    ).use {
      waitForTheView(withText(R.string.ongoing_story_last_week))
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          3
        )
      )
      onView(withId(R.id.ongoing_story_recycler_view)).check(
        hasGridItemCount(
          1, 3
        )
      )
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

    fun inject(recentlyPlayedFragmentTest: RecentlyPlayedFragmentTest)
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
    val countingResource = CountingIdlingResource("main_thread_executor_counting_idling_resource")

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
