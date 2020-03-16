package org.oppia.app.profileprogress

import android.app.Activity.RESULT_OK
import android.app.Application
import android.app.Instrumentation
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.provider.MediaStore
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
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
import org.oppia.domain.profile.ProfileTestHelper
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

/** Tests for [ProfileProgressFragment]. */
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
    profileTestHelper.initializeProfiles()
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
  }

  @After
  fun tearDown() {
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
  fun testProfileProgressFragment_clickStoryTextSize_changeTextSizeToLargeSuccessfully() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(0)).use {
      onView(withContentDescription(R.string.go_to_previous_page)).perform(click())
    }
  }

  @Test
  fun testProfileProgressFragment_checkProfileName_profileNameIsCorrect() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(0)).use {
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          0,
          R.id.profile_name_text_view
        )
      ).check(matches(withText("Sean")))
    }
  }

  @Test
  fun testProfileProgressFragment_openProfilePictureEditDialog() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(0)).use {
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          0,
          R.id.profile_edit_image
        )
      ).perform(click())
      onView(withText(R.string.profile_progress_edit_dialog_title)).check(matches(ViewMatchers.isDisplayed()))
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
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(0)).use {
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          0,
          R.id.profile_edit_image
        )
      ).perform(click())
      onView(withText(R.string.profile_progress_edit_dialog_title)).check(matches(ViewMatchers.isDisplayed()))
      onView(withId(R.string.profile_picture_edit_alert_dialog_choose_from_library)).perform(click())
      intended(expectedIntent)
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
    resultIntent.setData(imageUri)
    return Instrumentation.ActivityResult(RESULT_OK, resultIntent)
  }

  @Test
  fun testProfileProgressFragmentNoProgress_recyclerViewItem0_checkOngoingTopicsCount_countIsZero() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(0)).use {
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          0,
          R.id.ongoing_topics_count
        )
      ).check(matches(withText("0")))
    }
  }

  @Test
  fun testProfileProgressFragmentWithProgress_recyclerViewItem0_checkOngoingTopicsCount_countIsTwo() {
    storyProgressTestHelper.markPartialTopicProgressForFractions(profileId)
    storyProgressTestHelper.markTwoPartialStoryProgressForRatios(profileId)
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(0)).use {
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          0,
          R.id.ongoing_topics_count
        )
      ).check(matches(withText("2")))
    }
  }

  @Test
  fun testProfileProgressFragmentNoProgress_recyclerViewItem0_checkOngoingTopicsString_descriptionIsCorrect() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(0)).use {
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          0,
          R.id.ongoing_topics_description_text_view
        )
      ).check(matches(withText(R.string.topic_in_progress)))
    }
  }

  @Test
  fun testProfileProgressFragmentWithProgress_recyclerViewItem0_checkOngoingTopicsString_descriptionIsCorrect() {
    storyProgressTestHelper.markPartialTopicProgressForFractions(profileId)
    storyProgressTestHelper.markTwoPartialStoryProgressForRatios(profileId)
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(0)).use {
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          0,
          R.id.ongoing_topics_description_text_view
        )
      ).check(matches(withText(R.string.topics_in_progress)))
    }
  }

  @Test
  fun testProfileProgressFragmentNoProgress_recyclerViewItem0_checkCompletedStoriesCount_countIsZero() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(0)).use {
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          0,
          R.id.completed_stories_count
        )
      ).check(matches(withText("0")))
    }
  }

  @Test
  fun testProfileProgressFragmentWithProgress_recyclerViewItem0_checkCompletedStoriesCount_countIsTwo() {
    storyProgressTestHelper.markFullStoryPartialTopicProgressForRatios(profileId)
    storyProgressTestHelper.markFullStoryProgressForFractions(profileId)
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(0)).use {
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          0,
          R.id.completed_stories_count
        )
      ).check(matches(withText("2")))
    }
  }

  @Test
  fun testProfileProgressFragmentNoProgress_recyclerViewItem0_checkCompletedStoriesString_descriptionIsCorrect() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(0)).use {
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          0,
          R.id.completed_stories_description_text_view
        )
      ).check(matches(withText(R.string.story_completed)))
    }
  }

  @Test
  fun testProfileProgressFragmentWithProgress_recyclerViewItem0_checkCompletedStoriesString_descriptionIsCorrect() {
    storyProgressTestHelper.markFullStoryPartialTopicProgressForRatios(profileId)
    storyProgressTestHelper.markFullStoryProgressForFractions(profileId)
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(0)).use {
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          0,
          R.id.completed_stories_description_text_view
        )
      ).check(matches(withText(R.string.stories_completed)))
    }
  }

  @Test
  fun testProfileProgressActivity_recyclerViewItem1_chapterNameIsCorrect() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(0)).use {
      onView(withId(R.id.profile_progress_list)).perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      onView(atPositionOnView(R.id.profile_progress_list, 1, R.id.chapter_name_text_view)).check(
        matches(
          withText(
            containsString("What is a Fraction?")
          )
        )
      )
    }
  }

  @Test
  fun testProfileProgressActivity_recyclerViewItem1_storyNameIsCorrect() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(0)).use {
      onView(withId(R.id.profile_progress_list)).perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          1,
          R.id.story_name_text_view
        )
      ).check(matches(withText(containsString("Matthew Goes to the Bakery"))))
    }
  }

  @Test
  fun testProfileProgressActivity_recyclerViewItem1_topicNameIsCorrect() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(0)).use {
      onView(withId(R.id.profile_progress_list)).perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          1,
          R.id.topic_name_text_view
        )
      ).check(matches(withText(containsString("FRACTIONS"))))
    }
  }

  @Test
  fun testProfileProgressActivity_recyclerViewIndex0_clickViewAll_opensRecentlyPlayedActivity() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(0)).use {
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          0,
          R.id.view_all_text_view
        )
      ).perform(click())
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
  fun testProfileProgressActivityNoProgress_recyclerViewIndex0_clickTopicCount_opensOngoingTopicListActivity() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(0)).use {
      onView(atPositionOnView(R.id.profile_progress_list, 0, R.id.ongoing_topics_container)).check(
        matches(
          not(
            isClickable()
          )
        )
      )
    }
  }

  @Test
  fun testProfileProgressActivityNoProgress_recyclerViewIndex0_clickStoryCount_opensCompletedStoryListActivity() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(0)).use {
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          0,
          R.id.completed_stories_container
        )
      ).check(
        matches(
          not(
            isClickable()
          )
        )
      )
    }
  }

  @Test
  fun testProfileProgressActivityWithProgress_recyclerViewIndex0_clickTopicCount_opensOngoingTopicListActivity() {
    storyProgressTestHelper.markPartialTopicProgressForFractions(profileId)
    storyProgressTestHelper.markTwoPartialStoryProgressForRatios(profileId)
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(0)).use {
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
  fun testProfileProgressActivityWithProgress_recyclerViewIndex0_clickStoryCount_opensCompletedStoryListActivity() {
    storyProgressTestHelper.markFullStoryPartialTopicProgressForRatios(profileId)
    storyProgressTestHelper.markFullStoryProgressForFractions(profileId)
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(0)).use {
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

    fun inject(optionsFragmentTest: ProfileProgressFragmentTest)
  }
}
