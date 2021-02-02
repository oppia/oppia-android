package org.oppia.android.app.profileprogress

import android.app.Activity.RESULT_OK
import android.app.Application
import android.app.Instrumentation
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
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
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Component
import dagger.Module
import dagger.Provides
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.completedstorylist.CompletedStoryListActivity
import org.oppia.android.app.home.recentlyplayed.RecentlyPlayedActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.ongoingtopiclist.OngoingTopicListActivity
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.topic.TopicActivity
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.android.domain.classify.InteractionsModule
import org.oppia.android.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.android.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.android.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.android.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.android.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.android.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.android.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.android.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.android.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.android.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.domain.topic.StoryProgressTestHelper
import org.oppia.android.domain.topic.TEST_STORY_ID_0
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import org.oppia.android.testing.RobolectricModule
import org.oppia.android.testing.TestAccessibilityModule
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.parser.GlideImageLoaderModule
import org.oppia.android.util.parser.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [ProfileProgressFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = ProfileProgressFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class ProfileProgressFragmentTest {

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
    testCoroutineDispatchers.registerIdlingResource()
    profileTestHelper.initializeProfiles()
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
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
      testCoroutineDispatchers.runCurrent()
      verifyItemDisplayedOnProfileProgressListItem(
        itemPosition = 0,
        targetViewId = R.id.profile_name_text_view,
        stringToMatch = "Admin"
      )
    }
  }

  @Test
  fun testProfileProgressFragment_configChange_checkProfileName_profileNameIsCorrect() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      verifyItemDisplayedOnProfileProgressListItem(
        itemPosition = 0,
        targetViewId = R.id.profile_name_text_view,
        stringToMatch = "Admin"
      )
    }
  }

  @Test
  fun testProfileProgressFragment_profilePictureEditDialogIsDisplayed() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      clickProfileProgressItem(itemPosition = 0, targetViewId = R.id.profile_edit_image)
      testCoroutineDispatchers.runCurrent()
      verifyTextInDialog(context.getString(R.string.profile_progress_edit_dialog_title))
    }
  }

  @Test
  fun testProfileProgressFragment_openProfilePictureEditDialog_configChange_dialogIsStillOpen() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      clickProfileProgressItem(itemPosition = 0, targetViewId = R.id.profile_edit_image)
      verifyTextInDialog(context.getString(R.string.profile_progress_edit_dialog_title))
      onView(isRoot()).perform(orientationLandscape())
      onView(withText(R.string.profile_progress_edit_dialog_title)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testProfileProgressFragment_imageSelectAvatar_checkGalleryIntent() {
    val expectedIntent: Matcher<Intent> = allOf(
      hasAction(Intent.ACTION_PICK),
      hasData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    )
    val activityResult = createGalleryPickActivityResultStub()
    intending(expectedIntent).respondWith(activityResult)
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      clickProfileProgressItem(itemPosition = 0, targetViewId = R.id.profile_edit_image)
      verifyTextInDialog(context.getString(R.string.profile_progress_edit_dialog_title))
      onView(withText(R.string.profile_picture_edit_alert_dialog_choose_from_library))
        .perform(click())
      intended(expectedIntent)
    }
  }

  @Test
  fun testProfileProgressFragment_imageSelectAvatar_configChange_checkGalleryIntent() {
    val expectedIntent: Matcher<Intent> = allOf(
      hasAction(Intent.ACTION_PICK),
      hasData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    )
    val activityResult = createGalleryPickActivityResultStub()
    intending(expectedIntent).respondWith(activityResult)
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      clickProfileProgressItem(itemPosition = 0, targetViewId = R.id.profile_edit_image)
      verifyTextInDialog(context.getString(R.string.profile_progress_edit_dialog_title))
      onView(withText(R.string.profile_picture_edit_alert_dialog_choose_from_library))
        .perform(click())
      testCoroutineDispatchers.runCurrent()
      intended(expectedIntent)
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      intended(expectedIntent)
      clickProfileProgressItem(itemPosition = 0, targetViewId = R.id.profile_edit_image)
      // The dialog should still be open after a configuration change.
      verifyTextInDialog(context.getString(R.string.profile_progress_edit_dialog_title))
    }
  }

  @Test
  fun testProfileProgressFragment_noProgress_ongoingTopicCountIsZero() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      verifyItemDisplayedOnProfileProgressListItem(
        itemPosition = 0,
        targetViewId = R.id.ongoing_topics_count,
        stringToMatch = "0"
      )
    }
  }

  @Test
  fun testProfileProgressFragment_twoPartialStoryProgress_ongoingTopicCountIsTwo() {
    storyProgressTestHelper.markPartialTopicProgressForFractions(
      profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markTwoPartialStoryProgressForRatios(
      profileId,
      timestampOlderThanOneWeek = false
    )
    testCoroutineDispatchers.runCurrent()
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      verifyItemDisplayedOnProfileProgressListItem(
        itemPosition = 0,
        targetViewId = R.id.ongoing_topics_count,
        stringToMatch = "2"
      )
    }
  }

  @Test
  fun testProfileProgressFragment_configChange_twoPartialStoryProgress_ongoingTopicCountIsTwo() {
    storyProgressTestHelper.markPartialTopicProgressForFractions(
      profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markTwoPartialStoryProgressForRatios(
      profileId,
      timestampOlderThanOneWeek = false
    )
    testCoroutineDispatchers.runCurrent()
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      verifyItemDisplayedOnProfileProgressListItem(
        itemPosition = 0,
        targetViewId = R.id.ongoing_topics_count,
        stringToMatch = "2"
      )
    }
  }

  @Test
  fun testProfileProgressFragment_noProgress_ongoingTopicDescriptionIsCorrect() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      verifyItemDisplayedOnProfileProgressListItem(
        itemPosition = 0,
        targetViewId = R.id.ongoing_topics_description_text_view,
        stringToMatch = context.getString(R.string.topics_in_progress)
      )
    }
  }

  @Test
  fun testProfileProgressFragment_twoPartialStoryProgress_ongoingTopicDescriptionIsCorrect() {
    storyProgressTestHelper.markPartialTopicProgressForFractions(
      profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markTwoPartialStoryProgressForRatios(
      profileId,
      timestampOlderThanOneWeek = false
    )
    testCoroutineDispatchers.runCurrent()
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      verifyItemDisplayedOnProfileProgressListItem(
        itemPosition = 0,
        targetViewId = R.id.ongoing_topics_description_text_view,
        stringToMatch = context.getString(R.string.topics_in_progress)
      )
    }
  }

  @Test
  fun testProfileProgressFragment_configChange_ongoingTopicDescriptionIsCorrect() {
    storyProgressTestHelper.markPartialTopicProgressForFractions(
      profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markTwoPartialStoryProgressForRatios(
      profileId,
      timestampOlderThanOneWeek = false
    )
    testCoroutineDispatchers.runCurrent()
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      verifyItemDisplayedOnProfileProgressListItem(
        itemPosition = 0,
        targetViewId = R.id.ongoing_topics_description_text_view,
        stringToMatch = context.getString(R.string.topics_in_progress)
      )
    }
  }

  @Test
  fun testProfileProgressFragment_noProgress_completedStoriesCountIsZero() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      verifyItemDisplayedOnProfileProgressListItem(
        itemPosition = 0,
        targetViewId = R.id.completed_stories_count,
        stringToMatch = "0"
      )
    }
  }

  @Test
  fun testProfileProgressFragment_twoPartialStoryProgress_completedStoriesCountIsTwo() {
    storyProgressTestHelper.markFullStoryPartialTopicProgressForRatios(
      profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markFullStoryProgressForFractions(
      profileId,
      timestampOlderThanOneWeek = false
    )
    testCoroutineDispatchers.runCurrent()
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      verifyItemDisplayedOnProfileProgressListItem(
        itemPosition = 0,
        targetViewId = R.id.completed_stories_count,
        stringToMatch = "2"
      )
    }
  }

  @Test
  fun testProfileProgressFragment_noProgress_completedStoriesDescriptionIsCorrect() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      verifyItemDisplayedOnProfileProgressListItem(
        itemPosition = 0,
        targetViewId = R.id.completed_stories_description_text_view,
        stringToMatch = context.getString(R.string.stories_completed)
      )
    }
  }

  @Test
  fun testProfileProgressFragment_twoPartialStoryProgress_completedStoriesDescriptionIsCorrect() {
    storyProgressTestHelper.markFullStoryPartialTopicProgressForRatios(
      profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markFullStoryProgressForFractions(
      profileId,
      timestampOlderThanOneWeek = false
    )
    testCoroutineDispatchers.runCurrent()
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      verifyItemDisplayedOnProfileProgressListItem(
        itemPosition = 0,
        targetViewId = R.id.completed_stories_description_text_view,
        stringToMatch = context.getString(R.string.stories_completed)
      )
    }
  }

  @Test
  fun testProfileProgressFragment_configChange_firstStory_storyNameIsCorrect() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.profile_progress_list))
        .perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      verifyItemDisplayedOnProfileProgressListItem(
        itemPosition = 1,
        targetViewId = R.id.story_name_text_view,
        stringToMatch = "First Story"
      )
    }
  }

  @Test
  fun testProfileProgressFragment_firstStory_storyNameIsCorrect() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(0)).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.profile_progress_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      verifyItemDisplayedOnProfileProgressListItem(
        itemPosition = 1,
        targetViewId = R.id.story_name_text_view,
        stringToMatch = "First Story"
      )
    }
  }

  @Test
  fun testProfileProgressFragment_firstStory_topicNameIsCorrect() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.profile_progress_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      verifyItemDisplayedOnProfileProgressListItem(
        itemPosition = 1,
        targetViewId = R.id.topic_name_text_view,
        stringToMatch = "FIRST TEST TOPIC"
      )
    }
  }

  @Test
  fun testProfileProgressFragment_clickFirstStory_opensTopicActivity() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.profile_progress_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      testCoroutineDispatchers.runCurrent()
      clickProfileProgressItem(itemPosition = 1, targetViewId = R.id.topic_name_text_view)
      intended(hasComponent(TopicActivity::class.java.name))
      intended(hasExtra(TopicActivity.getProfileIdKey(), internalProfileId))
      intended(hasExtra(TopicActivity.getTopicIdKey(), TEST_TOPIC_ID_0))
      intended(hasExtra(TopicActivity.getStoryIdKey(), TEST_STORY_ID_0))
    }
  }

  @Test
  fun testProfileProgressFragment_clickViewAll_opensRecentlyPlayedActivity() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      verifyItemDisplayedOnProfileProgressListItem(
        itemPosition = 0,
        targetViewId = R.id.view_all_text_view,
        stringToMatch = "View All"
      )
      clickProfileProgressItem(itemPosition = 0, targetViewId = R.id.view_all_text_view)
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
  fun testProfileProgressFragment_noProgress_topicCountIsNotClickable() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          0,
          R.id.ongoing_topics_container
        )
      ).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testProfileProgressFragment_noProgress_storyCountIsNotClickable() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          0,
          R.id.completed_stories_container
        )
      ).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testProfileProgressFragment_configChange_noProgress_storyCountIsNotClickable() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          R.id.profile_progress_list,
          0,
          R.id.completed_stories_container
        )
      ).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testProfileProgressFragment_clickTopicCount_opensOngoingTopicListActivity() {
    storyProgressTestHelper.markPartialTopicProgressForFractions(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markTwoPartialStoryProgressForRatios(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    testCoroutineDispatchers.runCurrent()
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      clickProfileProgressItem(itemPosition = 0, targetViewId = R.id.ongoing_topics_container)
      testCoroutineDispatchers.runCurrent()
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
  fun testProfileProgressFragment_clickStoryCount_opensCompletedStoryListActivity() {
    storyProgressTestHelper.markFullStoryPartialTopicProgressForRatios(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markFullStoryProgressForFractions(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    testCoroutineDispatchers.runCurrent()
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      clickProfileProgressItem(itemPosition = 0, targetViewId = R.id.completed_stories_container)
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

  private fun verifyItemDisplayedOnProfileProgressListItem(
    itemPosition: Int,
    targetViewId: Int,
    stringToMatch: String
  ) {
    onView(
      atPositionOnView(
        R.id.profile_progress_list,
        itemPosition,
        targetViewId
      )
    ).check(matches(withText(stringToMatch)))
  }

  private fun clickProfileProgressItem(itemPosition: Int, targetViewId: Int) {
    onView(
      atPositionOnView(
        R.id.profile_progress_list,
        itemPosition,
        targetViewId
      )
    ).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun verifyTextInDialog(textInDialog: String) {
    onView(withText(textInDialog))
      .inRoot(isDialog())
      .check(matches(isDisplayed()))
  }

  @Module
  class TestModule {
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

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  // TODO(#1675): Add NetworkModule once data module is migrated off of Moshi.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
      TestModule::class, TestDispatcherModule::class, ApplicationModule::class,
      ContinueModule::class, FractionInputModule::class, ItemSelectionInputModule::class,
      MultipleChoiceInputModule::class, NumberWithUnitsRuleModule::class,
      NumericInputRuleModule::class, TextInputRuleModule::class, DragDropSortInputModule::class,
      ImageClickInputModule::class, InteractionsModule::class, GcsResourceModule::class,
      GlideImageLoaderModule::class, ImageParsingModule::class, HtmlParserEntityTypeModule::class,
      QuestionModule::class, TestLogReportingModule::class, TestAccessibilityModule::class,
      LogStorageModule::class, CachingTestModule::class, PrimeTopicAssetsControllerModule::class,
      ExpirationMetaDataRetrieverModule::class, ViewBindingShimModule::class,
      RatioInputModule::class, ApplicationStartupListenerModule::class,
      LogUploadWorkerModule::class, WorkManagerConfigurationModule::class,
      HintsAndSolutionConfigModule::class, FirebaseLogUploaderModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(profileProgressFragmentTest: ProfileProgressFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerProfileProgressFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(profileProgressFragmentTest: ProfileProgressFragmentTest) {
      component.inject(profileProgressFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
