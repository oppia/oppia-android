package org.oppia.android.app.profileprogress

import android.app.Activity.RESULT_OK
import android.app.Application
import android.app.Instrumentation
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
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
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey
import androidx.test.espresso.intent.matcher.IntentMatchers.hasType
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.card.MaterialCardView
import com.google.common.truth.Truth.assertThat
import dagger.Component
import dagger.Module
import dagger.Provides
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.activity.route.ActivityRouterModule
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.application.testing.TestingBuildFlavorModule
import org.oppia.android.app.completedstorylist.CompletedStoryListActivity
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.home.recentlyplayed.RecentlyPlayedActivity
import org.oppia.android.app.home.recentlyplayed.RecentlyPlayedActivity.Companion.RECENTLY_PLAYED_ACTIVITY_INTENT_EXTRAS_KEY
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.RecentlyPlayedActivityParams
import org.oppia.android.app.model.RecentlyPlayedActivityTitle
import org.oppia.android.app.model.TopicActivityParams
import org.oppia.android.app.ongoingtopiclist.OngoingTopicListActivity
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.topic.TopicActivity
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.EspressoTestsMatchers.hasProtoExtra
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.android.data.backends.gae.NetworkConfigProdModule
import org.oppia.android.data.backends.gae.NetworkModule
import org.oppia.android.domain.classify.InteractionsModule
import org.oppia.android.domain.classify.rules.algebraicexpressioninput.AlgebraicExpressionInputModule
import org.oppia.android.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.android.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.android.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.android.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.android.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.android.domain.classify.rules.mathequationinput.MathEquationInputModule
import org.oppia.android.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.android.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.android.domain.classify.rules.numericexpressioninput.NumericExpressionInputModule
import org.oppia.android.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.android.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.android.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.android.domain.classroom.TEST_CLASSROOM_ID_1
import org.oppia.android.domain.exploration.ExplorationProgressModule
import org.oppia.android.domain.exploration.ExplorationStorageModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulerModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.FRACTIONS_STORY_ID_0
import org.oppia.android.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.story.StoryProgressTestHelper
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import org.oppia.android.util.profile.PROFILE_ID_INTENT_DECORATOR
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
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var storyProgressTestHelper: StoryProgressTestHelper

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var fakeOppiaClock: FakeOppiaClock

  private val internalProfileId = 0

  private lateinit var profileId: ProfileId

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
    profileTestHelper.initializeProfiles()
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
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
  fun testProfileProgressFragment_checkAccessibilityFlowIsCorrect() {
    launch<ProfileProgressActivity>(
      createProfileProgressActivityIntent(internalProfileId)
    ).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity ->
        val recyclerView = activity.findViewById<RecyclerView>(R.id.profile_progress_list)
        val headerView = recyclerView.getChildAt(0)
        val rootView =
          headerView.findViewById<ConstraintLayout>(R.id.profile_progress_header_container)
        assertThat(rootView.importantForAccessibility).isEqualTo(
          View.IMPORTANT_FOR_ACCESSIBILITY_NO
        )

        val ongoingTopicsContainer =
          headerView.findViewById<MaterialCardView>(R.id.ongoing_topics_container)
        assertThat(ongoingTopicsContainer.importantForAccessibility).isEqualTo(
          View.IMPORTANT_FOR_ACCESSIBILITY_YES
        )

        val completedStoriesContainer =
          headerView.findViewById<MaterialCardView>(R.id.completed_stories_container)
        assertThat(completedStoriesContainer.importantForAccessibility).isEqualTo(
          View.IMPORTANT_FOR_ACCESSIBILITY_YES
        )
      }
    }
  }

  @Test
  fun testProfileProgressFragment_configChange_checkAccessibilityFlowIsCorrect() {
    launch<ProfileProgressActivity>(
      createProfileProgressActivityIntent(internalProfileId)
    ).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scenario.onActivity { activity ->
        val recyclerView = activity.findViewById<RecyclerView>(R.id.profile_progress_list)
        val headerView = recyclerView.getChildAt(0)
        val rootView =
          headerView.findViewById<ConstraintLayout>(R.id.profile_progress_header_container)
        assertThat(rootView.importantForAccessibility).isEqualTo(
          View.IMPORTANT_FOR_ACCESSIBILITY_NO
        )

        val ongoingTopicsContainer =
          headerView.findViewById<MaterialCardView>(R.id.ongoing_topics_container)
        assertThat(ongoingTopicsContainer.importantForAccessibility).isEqualTo(
          View.IMPORTANT_FOR_ACCESSIBILITY_YES
        )

        val completedStoriesContainer =
          headerView.findViewById<MaterialCardView>(R.id.completed_stories_container)
        assertThat(completedStoriesContainer.importantForAccessibility).isEqualTo(
          View.IMPORTANT_FOR_ACCESSIBILITY_YES
        )
      }
    }
  }

  @Config(qualifiers = "+sw600dp")
  @Test
  fun testProfileProgressFragment_tablet_checkAccessibilityFlowIsCorrect() {
    launch<ProfileProgressActivity>(
      createProfileProgressActivityIntent(internalProfileId)
    ).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity ->
        val recyclerView = activity.findViewById<RecyclerView>(R.id.profile_progress_list)
        val headerView = recyclerView.getChildAt(0)
        val rootView =
          headerView.findViewById<ConstraintLayout>(R.id.profile_progress_header_container)
        assertThat(rootView.importantForAccessibility).isEqualTo(
          View.IMPORTANT_FOR_ACCESSIBILITY_NO
        )

        val ongoingTopicsContainer =
          headerView.findViewById<MaterialCardView>(R.id.ongoing_topics_container)
        assertThat(ongoingTopicsContainer.importantForAccessibility).isEqualTo(
          View.IMPORTANT_FOR_ACCESSIBILITY_YES
        )

        val completedStoriesContainer =
          headerView.findViewById<MaterialCardView>(R.id.completed_stories_container)
        assertThat(completedStoriesContainer.importantForAccessibility).isEqualTo(
          View.IMPORTANT_FOR_ACCESSIBILITY_YES
        )
      }
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
      hasAction(Intent.ACTION_GET_CONTENT),
      hasType("image/*")
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
      hasAction(Intent.ACTION_GET_CONTENT),
      hasType("image/*")
    )
    val activityResult = createGalleryPickActivityResultStub()
    intending(expectedIntent).respondWith(activityResult)
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      clickProfileProgressItem(itemPosition = 0, targetViewId = R.id.profile_edit_image)
      verifyTextInDialog(context.getString(R.string.profile_progress_edit_dialog_title))
      onView(withText(R.string.profile_picture_edit_alert_dialog_choose_from_library))
        .perform(click())
      intended(expectedIntent)
    }
  }

  @Test
  fun testFragment_imageSelectAvatar_configChange_profilePictureDialogIsVisible() {
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      clickProfileProgressItem(itemPosition = 0, targetViewId = R.id.profile_edit_image)
      verifyTextInDialog(context.getString(R.string.profile_progress_edit_dialog_title))
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
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
    storyProgressTestHelper.markCompletedFractionsStory0Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedRatiosStory0Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedRatiosStory1Exp0(
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
    storyProgressTestHelper.markCompletedFractionsStory0Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedRatiosStory0Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedRatiosStory1Exp0(
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
    storyProgressTestHelper.markCompletedFractionsStory0Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedRatiosStory0Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedRatiosStory1Exp0(
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
    storyProgressTestHelper.markCompletedFractionsStory0Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedRatiosStory0Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedRatiosStory1Exp0(
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
    storyProgressTestHelper.markCompletedRatiosStory0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedFractionsStory0(
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
    storyProgressTestHelper.markCompletedRatiosStory0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedFractionsStory0(
      profileId,
      timestampOlderThanOneWeek = false
    )
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
  fun testProfileProgressFragment_configChange_fractionStory_storyNameIsCorrect() {
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.profile_progress_list))
        .perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      verifyItemDisplayedOnProfileProgressListItem(
        itemPosition = 1,
        targetViewId = R.id.story_name_text_view,
        stringToMatch = "Matthew Goes to the Bakery"
      )
    }
  }

  @Test
  fun testProfileProgressFragment_fractionsStory_storyNameIsCorrect() {
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
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
        stringToMatch = "Matthew Goes to the Bakery"
      )
    }
  }

  @Test
  fun testProfileProgressFragment_fractionsStory_topicNameIsCorrect() {
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
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
        stringToMatch = "FRACTIONS"
      )
    }
  }

  @Test
  fun testProfileProgressFragment_clickFractionsStory_opensTopicActivity() {
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.profile_progress_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      testCoroutineDispatchers.runCurrent()
      clickProfileProgressItem(itemPosition = 1, targetViewId = R.id.topic_name_text_view)

      val args = TopicActivityParams.newBuilder().apply {
        this.classroomId = TEST_CLASSROOM_ID_1
        this.topicId = FRACTIONS_TOPIC_ID
        this.storyId = FRACTIONS_STORY_ID_0
      }.build()
      intended(hasComponent(TopicActivity::class.java.name))
      intended(hasProtoExtra(TopicActivity.TOPIC_ACTIVITY_PARAMS_KEY, args))
    }
  }

  @Test
  fun testClickViewAll_withLessThanTwoStories_opensRecentlyPlayedActivityWithStoriesForYouTitle() {
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      timestampOlderThanOneWeek = false
    )
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      verifyItemDisplayedOnProfileProgressListItem(
        itemPosition = 0,
        targetViewId = R.id.view_all_text_view,
        stringToMatch = "View All"
      )
      val recentlyPlayedActivityParams = RecentlyPlayedActivityParams
        .newBuilder()
        .setProfileId(ProfileId.newBuilder().setInternalId(internalProfileId).build())
        .setActivityTitle(RecentlyPlayedActivityTitle.STORIES_FOR_YOU)
        .build()
      clickProfileProgressItem(itemPosition = 0, targetViewId = R.id.view_all_text_view)
      intended(
        allOf(
          hasProtoExtra(RECENTLY_PLAYED_ACTIVITY_INTENT_EXTRAS_KEY, recentlyPlayedActivityParams),
          hasComponent(RecentlyPlayedActivity::class.java.name)
        )
      )
    }
  }

  @Test
  fun testClickViewAll_threeStoriesStarted_opensRecentlyPlayedActivityWithRecentlyPlayedTitle() {
    storyProgressTestHelper.markCompletedFractionsStory0(
      profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedRatiosStory0(
      profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressNotSavedTestTopic0Story0(
      profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      timestampOlderThanOneWeek = false
    )
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      verifyItemDisplayedOnProfileProgressListItem(
        itemPosition = 0,
        targetViewId = R.id.view_all_text_view,
        stringToMatch = "View All"
      )
      val recentlyPlayedActivityParams = RecentlyPlayedActivityParams
        .newBuilder()
        .setProfileId(ProfileId.newBuilder().setInternalId(internalProfileId).build())
        .setActivityTitle(RecentlyPlayedActivityTitle.RECENTLY_PLAYED_STORIES)
        .build()
      clickProfileProgressItem(itemPosition = 0, targetViewId = R.id.view_all_text_view)
      intended(
        allOf(
          hasProtoExtra(RECENTLY_PLAYED_ACTIVITY_INTENT_EXTRAS_KEY, recentlyPlayedActivityParams),
          hasComponent(RecentlyPlayedActivity::class.java.name)
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
          recyclerViewId = R.id.profile_progress_list,
          position = 0,
          targetViewId = R.id.ongoing_topics_container
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
          recyclerViewId = R.id.profile_progress_list,
          position = 0,
          targetViewId = R.id.completed_stories_container
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
          recyclerViewId = R.id.profile_progress_list,
          position = 0,
          targetViewId = R.id.completed_stories_container
        )
      ).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testProfileProgressFragment_clickTopicCount_opensOngoingTopicListActivity() {
    storyProgressTestHelper.markCompletedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedRatiosStory1Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    testCoroutineDispatchers.runCurrent()
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      clickProfileProgressItem(itemPosition = 0, targetViewId = R.id.ongoing_topics_container)
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(OngoingTopicListActivity::class.java.name))
      intended(hasExtraWithKey(PROFILE_ID_INTENT_DECORATOR))
    }
  }

  @Test
  fun testProfileProgressFragment_clickStoryCount_opensCompletedStoryListActivity() {
    storyProgressTestHelper.markCompletedRatiosStory0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedFractionsStory0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    testCoroutineDispatchers.runCurrent()
    launch<ProfileProgressActivity>(createProfileProgressActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      clickProfileProgressItem(itemPosition = 0, targetViewId = R.id.completed_stories_container)
      val profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
      intended(hasComponent(CompletedStoryListActivity::class.java.name))
      intended(
        hasProtoExtra(
          PROFILE_ID_INTENT_DECORATOR,
          profileId
        )
      )
    }
  }

  @Test
  fun testFragment_argumentsAreCorrect() {
    launch<ProfileProgressActivity>(
      createProfileProgressActivityIntent(internalProfileId)
    ).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity ->

        val profileProgressFragment = activity.supportFragmentManager
          .findFragmentById(R.id.profile_progress_fragment_placeholder) as ProfileProgressFragment

        val args = checkNotNull(profileProgressFragment.arguments) {
          "Expected arguments to be passed to ProfileProgressFragment"
        }
        val receivedInternalProfileId = args.extractCurrentUserProfileId().internalId

        assertThat(receivedInternalProfileId).isEqualTo(internalProfileId)
      }
    }
  }

  private fun createGalleryPickActivityResultStub(): Instrumentation.ActivityResult {
    val resources: Resources = context.resources
    val imageUri = Uri.parse(
      ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
        resources.getResourcePackageName(R.mipmap.launcher_icon) + '/' +
        resources.getResourceTypeName(R.mipmap.launcher_icon) + '/' +
        resources.getResourceEntryName(R.mipmap.launcher_icon)
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
        recyclerViewId = R.id.profile_progress_list,
        position = itemPosition,
        targetViewId = targetViewId
      )
    ).check(matches(withText(stringToMatch)))
  }

  private fun clickProfileProgressItem(itemPosition: Int, targetViewId: Int) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.profile_progress_list,
        position = itemPosition,
        targetViewId = targetViewId
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
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
      PlatformParameterModule::class, PlatformParameterSingletonModule::class,
      TestModule::class, TestDispatcherModule::class, ApplicationModule::class,
      ContinueModule::class, FractionInputModule::class, ItemSelectionInputModule::class,
      MultipleChoiceInputModule::class, NumberWithUnitsRuleModule::class,
      NumericInputRuleModule::class, TextInputRuleModule::class, DragDropSortInputModule::class,
      ImageClickInputModule::class, InteractionsModule::class, GcsResourceModule::class,
      GlideImageLoaderModule::class, ImageParsingModule::class, HtmlParserEntityTypeModule::class,
      QuestionModule::class, TestLogReportingModule::class, AccessibilityTestModule::class,
      LogStorageModule::class, CachingTestModule::class,
      ExpirationMetaDataRetrieverModule::class, ViewBindingShimModule::class,
      RatioInputModule::class, ApplicationStartupListenerModule::class,
      LogReportWorkerModule::class, WorkManagerConfigurationModule::class,
      HintsAndSolutionConfigModule::class, HintsAndSolutionProdModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, NetworkConfigProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, SplitScreenInteractionModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, MetricLogSchedulerModule::class, TestingBuildFlavorModule::class,
      ActivityRouterModule::class,
      CpuPerformanceSnapshotterModule::class, ExplorationProgressModule::class,
      TestAuthenticationModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder {
      override fun build(): TestApplicationComponent
    }

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
