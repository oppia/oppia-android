package org.oppia.app.home

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.home.recentlyplayed.RecentlyPlayedActivity
import org.oppia.app.profile.ProfileActivity
import org.oppia.app.profileprogress.ProfileProgressActivity
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.testing.HomeInjectionActivity
import org.oppia.app.topic.TopicActivity
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.domain.profile.ProfileTestHelper
import org.oppia.domain.topic.FRACTIONS_STORY_ID_0
import org.oppia.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.domain.topic.TEST_TOPIC_ID_0
import org.oppia.testing.TestLogReportingModule
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.system.OppiaClock
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import javax.inject.Singleton

// Time: Tue Apr 23 2019 23:22:00
private const val EVENING_TIMESTAMP = 1556061720000

// Time: Wed Apr 24 2019 08:22:00
private const val MORNING_TIMESTAMP = 1556094120000

// Time: Tue Apr 23 2019 14:22:00
private const val AFTERNOON_TIMESTAMP = 1556029320000

/** Tests for [HomeActivity]. */
@RunWith(AndroidJUnit4::class)
class HomeActivityTest {

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var context: Context

  private val internalProfileId: Int = 1
  private lateinit var oppiaClock: OppiaClock

  @Before
  @ExperimentalCoroutinesApi
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    IdlingRegistry.getInstance().register(MainThreadExecutor.countingResource)
    profileTestHelper.initializeProfiles()
    FirebaseApp.initializeApp(context)
  }

  @After
  fun tearDown() {
    IdlingRegistry.getInstance().unregister(MainThreadExecutor.countingResource)
    Intents.release()
  }

  private fun getApplicationDependencies() {
    launch(HomeInjectionActivity::class.java).use {
      it.onActivity { activity ->
        oppiaClock = activity.oppiaClock
      }
    }
  }

  private fun setUpTestApplicationComponent() {
    DaggerHomeActivityTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  fun testHomeActivity_clickNavigationDrawerHamburger_clickOnHeader_opensProfileProgressActivity() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withContentDescription(R.string.drawer_open_content_description)).check(
        matches(isCompletelyDisplayed())
      ).perform(click())
      onView(withId(R.id.nav_header_profile_name)).perform(click())
      intended(hasComponent(ProfileProgressActivity::class.java.name))
      intended(
        hasExtra(
          ProfileProgressActivity.PROFILE_PROGRESS_ACTIVITY_PROFILE_ID_KEY,
          internalProfileId
        )
      )
    }
  }

  @Test
  @Ignore(
    "This test case is incorrect as it depends on internalProfileId " +
      "which is not guaranteed to be 0 for admin."
  )
  fun testHomeActivity_recyclerViewIndex0_withProfileId0_displayProfileName_profileNameDisplayedSuccessfully() { // ktlint-disable max-line-length
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(
        atPositionOnView(
          R.id.home_recycler_view,
          0,
          R.id.profile_name_textview
        )
      ).check(matches(withText("Sean!")))
    }
  }

  @Test
  fun testHomeActivity_recyclerViewIndex0_displayGreetingMessageBasedOnTime_goodMorningMessageDisplayedSuccessful() { // ktlint-disable max-line-length
    getApplicationDependencies()
    oppiaClock.setCurrentTimeMs(MORNING_TIMESTAMP)
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPositionOnView(
          R.id.home_recycler_view,
          0,
          R.id.welcome_text_view
        )
      ).check(matches(withText("Good morning,")))
    }
  }

  @Test
  fun testHomeActivity_recyclerViewIndex0_displayGreetingMessageBasedOnTime_goodAfternoonMessageDisplayedSuccessful() { // ktlint-disable max-line-length
    getApplicationDependencies()
    oppiaClock.setCurrentTimeMs(AFTERNOON_TIMESTAMP)
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPositionOnView(
          R.id.home_recycler_view,
          0,
          R.id.welcome_text_view
        )
      ).check(matches(withText("Good afternoon,")))
    }
  }

  @Test
  fun testHomeActivity_recyclerViewIndex0_displayGreetingMessageBasedOnTime_goodEveningMessageDisplayedSuccessful() { // ktlint-disable max-line-length
    getApplicationDependencies()
    oppiaClock.setCurrentTimeMs(EVENING_TIMESTAMP)
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPositionOnView(
          R.id.home_recycler_view,
          0,
          R.id.welcome_text_view
        )
      ).check(matches(withText("Good evening,")))
    }
  }

  @Test
  fun testHomeActivity_recyclerViewIndex0_configurationChange_displaysWelcomeMessageCorrectly() {
    launch<HomeActivity>(createHomeActivityIntent(0)).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPositionOnView(
          R.id.home_recycler_view,
          0,
          R.id.profile_name_textview
        )
      ).check(matches(withText("Sean!")))
    }
  }

  @Test
  fun testHomeActivity_recyclerViewIndex1_displaysRecentlyPlayedStoriesText() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(
        atPositionOnView(
          R.id.home_recycler_view,
          1,
          R.id.recently_played_stories_text_view
        )
      ).check(
        matches(
          withText(R.string.recently_played_stories)
        )
      )
    }
  }

  @Test
  fun testHomeActivity_recyclerViewIndex1_displaysViewAllText() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(atPositionOnView(R.id.home_recycler_view, 1, R.id.view_all_text_view)).check(
        matches(
          withText(R.string.view_all)
        )
      )
    }
  }

  @Test
  fun testHomeActivity_recyclerViewIndex1_clickViewAll_opensRecentlyPlayedActivity() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(
        atPositionOnView(
          R.id.home_recycler_view, 1, R.id.view_all_text_view
        )
      ).perform(click())
      intended(hasComponent(RecentlyPlayedActivity::class.java.name))
    }
  }

  @Test
  fun testHomeActivity_recyclerViewIndex1_promotedCard_chapterNameIsCorrect() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(
        allOf(
          withId(R.id.promoted_story_list_recycler_view),
          ViewMatchers.withParent(
            atPosition(R.id.home_recycler_view, 1)
          )
        )
      ).check(matches(hasDescendant(withText(containsString("What is a Fraction?")))))
    }
  }

  @Test
  fun testHomeActivity_recyclerViewIndex1_promotedCard_storyNameIsCorrect() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(atPositionOnView(R.id.home_recycler_view, 1, R.id.story_name_text_view)).check(
        matches(
          withText(containsString("Matthew Goes to the Bakery"))
        )
      )
    }
  }

  @Test
  fun testHomeActivity_recyclerViewIndex1_configurationChange_promotedCard_storyNameIsCorrect() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(atPositionOnView(R.id.home_recycler_view, 1, R.id.story_name_text_view)).check(
        matches(
          withText(containsString("Matthew Goes to the Bakery"))
        )
      )
    }
  }

  @Test
  fun testHomeActivity_recyclerViewIndex1_clickPromotedStory_opensTopicActivity() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(
        Matchers.allOf(
          withId(R.id.promoted_story_list_recycler_view),
          ViewMatchers.withParent(
            atPosition(R.id.home_recycler_view, 1)
          )
        )
      ).perform(click())
      intended(hasComponent(TopicActivity::class.java.name))
      intended(hasExtra(TopicActivity.getProfileIdKey(), internalProfileId))
      intended(hasExtra(TopicActivity.getTopicIdKey(), FRACTIONS_TOPIC_ID))
      intended(hasExtra(TopicActivity.getStoryIdKey(), FRACTIONS_STORY_ID_0))
    }
  }

  @Test
  fun testHomeActivity_recyclerViewIndex1_promotedCard_topicNameIsCorrect() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(atPositionOnView(R.id.home_recycler_view, 1, R.id.topic_name_text_view)).check(
        matches(
          withText(containsString("FRACTIONS"))
        )
      )
    }
  }

  @Test
  fun testHomeActivity_recyclerViewIndex3_topicSummary_topicNameIsCorrect() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(3)
      )
      onView(atPositionOnView(R.id.home_recycler_view, 3, R.id.topic_name_text_view)).check(
        matches(
          withText(containsString("First Test Topic"))
        )
      )
    }
  }

  @Test
  fun testHomeActivity_recyclerViewIndex3_topicSummary_lessonCountIsCorrect() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(3)
      )
      onView(
        atPositionOnView(
          R.id.home_recycler_view,
          3, R.id.lesson_count_text_view
        )
      ).check(
        matches(
          withText(containsString("4 Lessons"))
        )
      )
    }
  }

  @Test
  fun testHomeActivity_recyclerViewIndex4_topicSummary_topicNameIsCorrect() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(4)
      )
      onView(atPositionOnView(R.id.home_recycler_view, 4, R.id.topic_name_text_view)).check(
        matches(
          withText(containsString("Second Test Topic"))
        )
      )
    }
  }

  @Test
  fun testHomeActivity_recyclerViewIndex4_topicSummary_lessonCountIsCorrect() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(4)
      )
      onView(
        atPositionOnView(
          R.id.home_recycler_view,
          4, R.id.lesson_count_text_view
        )
      ).check(
        matches(
          withText(containsString("1 Lesson"))
        )
      )
    }
  }

  @Test
  fun testHomeActivity_recyclerViewIndex4_topicSummary_configurationChange_lessonCountIsCorrect() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(4)
      )
      onView(
        atPositionOnView(
          R.id.home_recycler_view,
          4, R.id.lesson_count_text_view
        )
      ).check(
        matches(
          withText(containsString("1 Lesson"))
        )
      )
    }
  }

  @Test
  fun testHomeActivity_recyclerViewIndex3_clickTopicSummary_opensTopicActivity() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(3)
      )
      onView(atPosition(R.id.home_recycler_view, 3)).perform(click())
      intended(hasComponent(TopicActivity::class.java.name))
      intended(hasExtra(TopicActivity.getTopicIdKey(), TEST_TOPIC_ID_0))
    }
  }

  @Test
  fun testHomeActivity_onBackPressed_showsExitToProfileChooserDialog() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      pressBack()
      onView(withText(R.string.home_activity_back_dialog_message)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testHomeActivity_onBackPressed_clickExit_checkOpensProfileActivity() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      pressBack()
      onView(withText(R.string.home_activity_back_dialog_exit)).perform(click())
      intended(hasComponent(ProfileActivity::class.java.name))
    }
  }

  @Test
  fun testHomeActivity_checkSpanForItem0_spanSizeIsTwoOrThree() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      if (context.resources.getBoolean(R.bool.isTablet)) {
        onView(withId(R.id.home_recycler_view)).check(hasGridItemCount(3, 0))
      } else {
        onView(withId(R.id.home_recycler_view)).check(hasGridItemCount(2, 0))
      }
    }
  }

  @Test
  fun testHomeActivity_checkSpanForItem4_spanSizeIsOne() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withId(R.id.home_recycler_view)).check(hasGridItemCount(1, 4))
    }
  }

  @Test
  fun testHomeActivity_configurationChange_checkSpanForItem0_spanSizeIsThreeOrFour() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(isRoot()).perform(orientationLandscape())
      if (context.resources.getBoolean(R.bool.isTablet)) {
        onView(withId(R.id.home_recycler_view)).check(hasGridItemCount(4, 0))
      } else {
        onView(withId(R.id.home_recycler_view)).check(hasGridItemCount(3, 0))
      }
    }
  }

  @Test
  fun testHomeActivity_configurationChange_checkSpanForItem4_spanSizeIsOne() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.home_recycler_view)).check(hasGridItemCount(1, 4))
    }
  }

  private fun createHomeActivityIntent(profileId: Int): Intent {
    return HomeActivity.createHomeActivity(ApplicationProvider.getApplicationContext(), profileId)
  }

  /** Returns span count ViewAssertion for a recycler view that use GridLayoutManager. */
  private fun hasGridItemCount(spanCount: Int, position: Int): ViewAssertion {
    return RecyclerViewGridItemCountAssertion(spanCount, position)
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

    // TODO(#89): Introduce a proper IdlingResource for background dispatchers to ensure they all complete before
    //  proceeding in an Espresso test. This solution should also be interoperative with Robolectric contexts by using a
    //  test coroutine dispatcher.

    @Singleton
    @Provides
    @BackgroundDispatcher
    fun provideBackgroundDispatcher(
      @BlockingDispatcher blockingDispatcher: CoroutineDispatcher
    ): CoroutineDispatcher {
      return blockingDispatcher
    }

    @Singleton
    @Provides
    @BlockingDispatcher
    fun provideBlockingDispatcher(): CoroutineDispatcher {
      return MainThreadExecutor.asCoroutineDispatcher()
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
  @Component(modules = [TestModule::class, TestLogReportingModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun getProfileManagementController(): ProfileManagementController
    fun inject(homeActivityTest: HomeActivityTest)
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

  /** Custom class to check number of spans occupied by an item at a given position. */
  private class RecyclerViewGridItemCountAssertion(
    private val count: Int,
    private val position: Int
  ) : ViewAssertion {
    override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
      if (noViewFoundException != null) {
        throw noViewFoundException
      }
      check(view is RecyclerView) { "The asserted view is not RecyclerView" }
      check(view.layoutManager is GridLayoutManager) { "RecyclerView must use GridLayoutManager" }
      val spanCount = (view.layoutManager as GridLayoutManager)
        .spanSizeLookup.getSpanSize(position)
      assertThat(
        "RecyclerViewGrid span count", spanCount, equalTo(count)
      )
    }
  }
}
