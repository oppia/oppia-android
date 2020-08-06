package org.oppia.app.profileprogress

import android.app.Activity.RESULT_OK
import android.app.Application
import android.app.Instrumentation
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
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
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isClickable
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.completedstorylist.CompletedStoryListActivity
import org.oppia.app.home.recentlyplayed.RecentlyPlayedActivity
import org.oppia.app.model.ProfileId
import org.oppia.app.ongoingtopiclist.OngoingTopicListActivity
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.domain.profile.ProfileTestHelper
import org.oppia.domain.topic.StoryProgressTestHelper
import org.oppia.testing.TestLogReportingModule
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

/** Tests for [ProfileProgressFragment]. */
@LooperMode(LooperMode.Mode.PAUSED)
@Config(qualifiers = "port-xxhdpi")
@RunWith(AndroidJUnit4::class)
class ProfileProgressFragmentTest {

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var storyProgressTestHelper: StoryProgressTestHelper

  @Inject
  lateinit var context: Context

  private val internalProfileId = 0

  private lateinit var profileId: ProfileId

  @Before
  @ExperimentalCoroutinesApi
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
    DaggerProfileProgressFragmentTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  private fun createProfileProgressActivityIntent(profileId: Int): Intent {
    return ProfileProgressActivity.createProfileProgressActivityIntent(
      ApplicationProvider.getApplicationContext(),
      profileId
    )
  }

  @Test
  fun testProfileProgressFragment_checkProfileName_profileNameIsCorrect() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      waitForTheView(withText("Sean"))
      onView(
        atPositionOnView(R.id.profile_progress_list, 0, R.id.profile_name_text_view)
      ).check(
        matches(withText("Sean"))
      )
    }
  }

  @Test
  fun testProfileProgressFragment_configurationChange_checkProfileName_profileNameIsCorrect() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      onView(isRoot()).perform(orientationLandscape())
      waitForTheView(withText("Sean"))
      onView(
        atPositionOnView(R.id.profile_progress_list, 0, R.id.profile_name_text_view)
      ).check(
        matches(withText("Sean"))
      )
    }
  }

  @Test
  fun testProfileProgressFragment_openProfilePictureEditDialog() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      waitForTheView(withText("Sean"))
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          0,
          R.id.profile_edit_image
        )
      ).perform(click())
      onView(withText(R.string.profile_progress_edit_dialog_title)).inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testProfileProgressFragment_openProfilePictureEditDialog_configurationChange_dialogIsStillOpen() { // ktlint-disable max-line-length
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      waitForTheView(withText("Sean"))
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          0,
          R.id.profile_edit_image
        )
      ).perform(click())
      onView(withText(R.string.profile_progress_edit_dialog_title)).inRoot(isDialog())
        .check(matches(isDisplayed()))
      onView(isRoot()).perform(orientationLandscape())
      onView(withText(R.string.profile_progress_edit_dialog_title)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testAddProfileActivity_imageSelectAvatar_checkGalleryIntent() {
    val expectedIntent: Matcher<Intent> = allOf(
      hasAction(Intent.ACTION_PICK),
      hasData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    )
    val activityResult = createGalleryPickActivityResultStub()
    intending(expectedIntent).respondWith(activityResult)
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      waitForTheView(withText("Sean"))
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          0,
          R.id.profile_edit_image
        )
      ).perform(click())
      onView(withText(R.string.profile_progress_edit_dialog_title)).inRoot(isDialog())
        .check(matches(isDisplayed()))
      onView(withText(R.string.profile_picture_edit_alert_dialog_choose_from_library))
        .perform(click())
      intended(expectedIntent)
    }
  }

  @Test
  fun testProfileProgressFragmentNoProgress_recyclerViewItem0_checkOngoingTopicsCount_countIsZero() { // ktlint-disable max-line-length
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      waitForTheView(withText("0"))
      onView(
        atPositionOnView(R.id.profile_progress_list, 0, R.id.ongoing_topics_count)
      ).check(
        matches(withText("0"))
      )
    }
  }

  @Test
  fun testProfileProgressFragmentWithProgress_recyclerViewItem0_checkOngoingTopicsCount_countIsTwo() { // ktlint-disable max-line-length */
    storyProgressTestHelper.markPartialTopicProgressForFractions(
      profileId,
      timestampOlderThanAWeek = false
    )
    storyProgressTestHelper.markTwoPartialStoryProgressForRatios(
      profileId,
      timestampOlderThanAWeek = false
    )
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      waitForTheView(withText("2"))
      onView(
        atPositionOnView(R.id.profile_progress_list, 0, R.id.ongoing_topics_count)
      ).check(
        matches(withText("2"))
      )
    }
  }

  @Test
  @Config(qualifiers = "port-xxhdpi")
  fun testProfileProgressFragmentWithProgress_change_configuration_recyclerViewItem0_checkOngoingTopicsCount_countIsTwo() { // ktlint-disable max-line-length
    storyProgressTestHelper.markPartialTopicProgressForFractions(
      profileId,
      timestampOlderThanAWeek = false
    )
    storyProgressTestHelper.markTwoPartialStoryProgressForRatios(
      profileId,
      timestampOlderThanAWeek = false
    )
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      onView(isRoot()).perform(orientationLandscape())
      waitForTheView(withText("2"))
      onView(
        atPositionOnView(R.id.profile_progress_list, 0, R.id.ongoing_topics_count)
      ).check(
        matches(withText("2"))
      )
    }
  }

  @Test
  fun testProfileProgressFragmentNoProgress_recyclerViewItem0_checkOngoingTopicsString_descriptionIsCorrect() { // ktlint-disable max-line-length
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      waitForTheView(withText(R.string.topics_in_progress))
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          0, R.id.ongoing_topics_description_text_view
        )
      ).check(
        matches(withText(R.string.topics_in_progress))
      )
    }
  }

  @Test
  fun testProfileProgressFragmentWithProgress_recyclerViewItem0_checkOngoingTopicsString_descriptionIsCorrect() { // ktlint-disable max-line-length
    storyProgressTestHelper.markPartialTopicProgressForFractions(
      profileId,
      timestampOlderThanAWeek = false
    )
    storyProgressTestHelper.markTwoPartialStoryProgressForRatios(
      profileId,
      timestampOlderThanAWeek = false
    )
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      waitForTheView(withText(R.string.topics_in_progress))
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          0, R.id.ongoing_topics_description_text_view
        )
      ).check(
        matches(withText(R.string.topics_in_progress))
      )
    }
  }

  @Test
  fun testProfileProgressFragmentWithProgress_changeConfiguration_recyclerViewItem0_checkOngoingTopicsString_descriptionIsCorrect() { // ktlint-disable max-line-length
    storyProgressTestHelper.markPartialTopicProgressForFractions(
      profileId,
      timestampOlderThanAWeek = false
    )
    storyProgressTestHelper.markTwoPartialStoryProgressForRatios(
      profileId,
      timestampOlderThanAWeek = false
    )
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      onView(isRoot()).perform(orientationLandscape())
      waitForTheView(withText(R.string.topics_in_progress))
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          0, R.id.ongoing_topics_description_text_view
        )
      ).check(
        matches(withText(R.string.topics_in_progress))
      )
    }
  }

  @Test
  fun testProfileProgressFragmentNoProgress_recyclerViewItem0_checkCompletedStoriesCount_countIsZero() { // ktlint-disable max-line-length
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      waitForTheView(withText("0"))
      onView(
        atPositionOnView(R.id.profile_progress_list, 0, R.id.completed_stories_count)
      ).check(
        matches(withText("0"))
      )
    }
  }

  @Test
  fun testProfileProgressFragmentWithProgress_recyclerViewItem0_checkCompletedStoriesCount_countIsTwo() { // ktlint-disable max-line-length
    storyProgressTestHelper.markFullStoryPartialTopicProgressForRatios(
      profileId,
      timestampOlderThanAWeek = false
    )
    storyProgressTestHelper.markFullStoryProgressForFractions(
      profileId,
      timestampOlderThanAWeek = false
    )
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      waitForTheView(withText("2"))
      onView(
        atPositionOnView(R.id.profile_progress_list, 0, R.id.completed_stories_count)
      ).check(
        matches(withText("2"))
      )
    }
  }

  @Test
  fun testProfileProgressFragmentNoProgress_recyclerViewItem0_checkCompletedStoriesString_descriptionIsCorrect() { // ktlint-disable max-line-length
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      waitForTheView(withText(R.string.stories_completed))
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          0,
          R.id.completed_stories_description_text_view
        )
      ).check(
        matches(withText(R.string.stories_completed))
      )
    }
  }

  @Test
  fun testProfileProgressFragmentWithProgress_recyclerViewItem0_checkCompletedStoriesString_descriptionIsCorrect() { // ktlint-disable max-line-length
    storyProgressTestHelper.markFullStoryPartialTopicProgressForRatios(
      profileId,
      timestampOlderThanAWeek = false
    )
    storyProgressTestHelper.markFullStoryProgressForFractions(
      profileId,
      timestampOlderThanAWeek = false
    )
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      waitForTheView(withText(R.string.stories_completed))
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          0,
          R.id.completed_stories_description_text_view
        )
      ).check(
        matches(withText(R.string.stories_completed))
      )
    }
  }

  @Test
  fun testProfileProgressActivity_recyclerViewItem1_chapterNameIsCorrect() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      onView(withId(R.id.profile_progress_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      waitForTheView(withText("Prototype Exploration"))
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          1, R.id.chapter_name_text_view
        )
      ).check(
        matches(withText(containsString("Prototype Exploration")))
      )
    }
  }

  @Test
  @Config(qualifiers = "port-xxhdpi")
  fun testProfileProgressActivity_changeConfiguration_recyclerViewItem1_chapterNameIsCorrect() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.profile_progress_list))
        .perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      waitForTheView(withText("Prototype Exploration"))
      onView(
        atPositionOnView(R.id.profile_progress_list, 1, R.id.chapter_name_text_view)
      ).check(
        matches(withText(containsString("Prototype Exploration")))
      )
    }
  }

  @Test
  fun testProfileProgressActivity_recyclerViewItem1_storyNameIsCorrect() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(0)).use {
      onView(withId(R.id.profile_progress_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      waitForTheView(withText("First Story"))
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          1, R.id.story_name_text_view
        )
      ).check(
        matches(withText(containsString("First Story")))
      )
    }
  }

  @Test
  fun testProfileProgressActivity_recyclerViewItem1_topicNameIsCorrect() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      onView(withId(R.id.profile_progress_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      waitForTheView(withText("FIRST TEST TOPIC"))
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          1, R.id.topic_name_text_view
        )
      ).check(
        matches(withText(containsString("FIRST TEST TOPIC")))
      )
    }
  }

  @Test
  fun testProfileProgressActivity_recyclerViewIndex0_clickViewAll_opensRecentlyPlayedActivity() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      waitForTheView(withText("Sean"))
      onView(atPositionOnView(R.id.profile_progress_list, 0, R.id.view_all_text_view))
        .check(
          matches(withText("View All"))
        )
        .perform(click())
      intended(hasComponent(RecentlyPlayedActivity::class.java.name))
      intended(
        hasExtra(
          RecentlyPlayedActivity.RECENTLY_PLAYED_ACTIVITY_INTERNAL_PROFILE_ID_KEY,
          internalProfileId
        )
      )
    }
  }

  @Test
  fun testProfileProgressActivityNoProgress_recyclerViewIndex0_clickTopicCount_isNotClickable() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      waitForTheView(withText(R.string.topics_in_progress))
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          0, R.id.ongoing_topics_container
        )
      ).check(
        matches(not(isClickable()))
      )
    }
  }

  @Test
  fun testProfileProgressActivityNoProgress_recyclerViewIndex0_clickStoryCount_isNotClickable() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      waitForTheView(withText(R.string.stories_completed))
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          0, R.id.completed_stories_container
        )
      ).check(
        matches(not(isClickable()))
      )
    }
  }

  @Test
  fun testProfileProgressActivityNoProgress_recyclerViewIndex0_changeConfiguration_clickStoryCount_isNotClickable() { // ktlint-disable max-line-length
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      onView(isRoot()).perform(orientationLandscape())
      waitForTheView(withText(R.string.stories_completed))
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          0, R.id.completed_stories_container
        )
      ).check(
        matches(not(isClickable()))
      )
    }
  }

  @Test
  fun testProfileProgressActivityWithProgress_recyclerViewIndex0_clickTopicCount_opensOngoingTopicListActivity() { // ktlint-disable max-line-length
    storyProgressTestHelper.markPartialTopicProgressForFractions(
      profileId,
      timestampOlderThanAWeek = false
    )
    storyProgressTestHelper.markTwoPartialStoryProgressForRatios(
      profileId,
      timestampOlderThanAWeek = false
    )
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      waitForTheView(withText(R.string.topics_in_progress))
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          0,
          R.id.ongoing_topics_container
        )
      ).perform(click())
      intended(hasComponent(OngoingTopicListActivity::class.java.name))
      intended(
        hasExtra(
          OngoingTopicListActivity.ONGOING_TOPIC_LIST_ACTIVITY_PROFILE_ID_KEY,
          internalProfileId
        )
      )
    }
  }

  @Test
  fun testProfileProgressActivityWithProgress_recyclerViewIndex0_clickStoryCount_opensCompletedStoryListActivity() { // ktlint-disable max-line-length
    storyProgressTestHelper.markFullStoryPartialTopicProgressForRatios(
      profileId,
      timestampOlderThanAWeek = false
    )
    storyProgressTestHelper.markFullStoryProgressForFractions(
      profileId,
      timestampOlderThanAWeek = false
    )
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      waitForTheView(withText(R.string.stories_completed))
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          0,
          R.id.completed_stories_container
        )
      ).perform(click())
      intended(hasComponent(CompletedStoryListActivity::class.java.name))
      intended(
        hasExtra(
          CompletedStoryListActivity.COMPLETED_STORY_LIST_ACTIVITY_PROFILE_ID_KEY,
          internalProfileId
        )
      )
    }
  }

  private fun createGalleryPickActivityResultStub(): Instrumentation.ActivityResult {
    val resources: Resources = context.resources
    val imageUri = Uri.parse(
      ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
        resources.getResourcePackageName(R.mipmap.ic_launcher) + '/' +
        resources.getResourceTypeName(R.mipmap.ic_launcher) + '/' +
        resources.getResourceEntryName(R.mipmap.ic_launcher)
    )
    val resultIntent = Intent()
    resultIntent.data = imageUri
    return Instrumentation.ActivityResult(RESULT_OK, resultIntent)
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

  @Qualifier
  annotation class TestDispatcher

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
    fun provideBackgroundDispatcher(
      @TestDispatcher testDispatcher: CoroutineDispatcher
    ): CoroutineDispatcher {
      return testDispatcher
    }

    @Singleton
    @Provides
    @BlockingDispatcher
    fun provideBlockingDispatcher(
      @TestDispatcher testDispatcher: CoroutineDispatcher
    ): CoroutineDispatcher {
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
  @Component(modules = [TestModule::class, TestLogReportingModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(optionsFragmentTest: ProfileProgressFragmentTest)
  }

  /* ktlint-disable max-line-length */
  // TODO(#59): Move this to a general-purpose testing library that replaces all CoroutineExecutors with an
  //  Espresso-enabled executor service. This service should also allow for background threads to run in both Espresso
  //  and Robolectric to help catch potential race conditions, rather than forcing parallel execution to be sequential
  //  and immediate.
  //  NB: This also blocks on #59 to be able to actually create a test-only library.
  /**
   * An executor service that schedules all [Runnable]s to run asynchronously on the main thread. This is based on:
   * https://android.googlesource.com/platform/packages/apps/TV/+/android-live-tv/src/com/android/tv/util/MainThreadExecutor.java.
   */
  /* ktlint-enable max-line-length */
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
