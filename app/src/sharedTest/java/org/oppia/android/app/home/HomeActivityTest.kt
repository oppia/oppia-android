package org.oppia.android.app.home

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Before
import org.junit.Ignore
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
import org.oppia.android.app.customview.LessonThumbnailImageView
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.home.recentlyplayed.RecentlyPlayedActivity
import org.oppia.android.app.model.OppiaLanguage.ARABIC
import org.oppia.android.app.model.OppiaLanguage.ARABIC_VALUE
import org.oppia.android.app.model.OppiaLanguage.BRAZILIAN_PORTUGUESE
import org.oppia.android.app.model.OppiaLanguage.BRAZILIAN_PORTUGUESE_VALUE
import org.oppia.android.app.model.OppiaLanguage.ENGLISH
import org.oppia.android.app.model.OppiaLanguage.ENGLISH_VALUE
import org.oppia.android.app.model.OppiaLanguage.NIGERIAN_PIDGIN
import org.oppia.android.app.model.OppiaLanguage.NIGERIAN_PIDGIN_VALUE
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.model.ScreenName
import org.oppia.android.app.model.Spotlight
import org.oppia.android.app.model.TopicActivityParams
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.profile.ProfileChooserActivity
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.hasGridColumnCount
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.hasGridItemCount
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.hasItemCount
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.topic.TopicActivity
import org.oppia.android.app.translation.AppLanguageLocaleHandler
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.translation.testing.TestActivityRecreator
import org.oppia.android.app.utility.EspressoTestsMatchers.hasProtoExtra
import org.oppia.android.app.utility.FontScaleConfigurationUtil
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
import org.oppia.android.domain.classroom.TEST_CLASSROOM_ID_0
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
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.spotlight.SpotlightStateController
import org.oppia.android.domain.topic.FRACTIONS_STORY_ID_0
import org.oppia.android.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.android.domain.topic.TEST_STORY_ID_0
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.BuildEnvironment
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.TestPlatform
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.DefineAppLanguageLocaleContext
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
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
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.extractCurrentAppScreenName
import org.oppia.android.util.logging.EventLoggingConfigurationModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.Locale
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
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = HomeActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
@DefineAppLanguageLocaleContext(
  oppiaLanguageEnumId = ENGLISH_VALUE,
  appStringIetfTag = "en",
  appStringAndroidLanguageId = "en"
)
class HomeActivityTest {
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

  @Inject
  lateinit var spotlightStateController: SpotlightStateController

  @Inject
  lateinit var appLanguageLocaleHandler: AppLanguageLocaleHandler

  @Inject
  lateinit var testActivityRecreator: TestActivityRecreator

  @Inject
  lateinit var dataProviderTestMonitor: DataProviderTestMonitor.Factory

  @Inject
  lateinit var fontScaleConfigurationUtil: FontScaleConfigurationUtil

  private val internalProfileId: Int = 0
  private val internalProfileId1: Int = 1
  private val longNameInternalProfileId: Int = 3
  private lateinit var profileId: ProfileId
  private lateinit var profileId1: ProfileId

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(internalProfileId).build()
    profileId1 = ProfileId.newBuilder().setLoggedInInternalProfileId(internalProfileId1).build()
    testCoroutineDispatchers.registerIdlingResource()
    profileTestHelper.initializeProfiles()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testActivity_createIntent_verifyScreenNameInIntent() {
    val screenName = createHomeActivityIntent(internalProfileId).extractCurrentAppScreenName()

    assertThat(screenName).isEqualTo(ScreenName.HOME_ACTIVITY)
  }

  @Test
  fun testHomeActivity_loadingItemsPending_progressbarIsDisplayed() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withId(R.id.home_fragment_progress_bar)).check(
        matches(
          isDisplayed()
        )
      )
    }
  }

  @Test
  fun testHomeActivity_loadingItemsSuccess_checkProgressbarIsNotDisplayed() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.home_fragment_progress_bar)).check(
        matches(
          not(
            isDisplayed()
          )
        )
      )
    }
  }

  @Test
  fun testHomeActivity_hasCorrectActivityLabel() {
    launch(HomeActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        val title = activity.title
        assertThat(title).isEqualTo(context.getString(R.string.home_activity_title))
      }
    }
  }

  @Test
  fun testHomeActivity_withAdminProfile_configChange_profileNameIsDisplayed() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    fakeOppiaClock.setCurrentTimeToSameDateTime(EVENING_TIMESTAMP)
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 0)
      verifyExactTextOnHomeListItemAtPosition(
        itemPosition = 0,
        targetViewId = R.id.welcome_text_view,
        stringToMatch = "Good evening, Admin!"
      )
    }
  }

  @Test
  fun testHomeActivity_morningTimestamp_goodMorningMessageIsDisplayed_withAdminProfileName() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    fakeOppiaClock.setCurrentTimeToSameDateTime(MORNING_TIMESTAMP)
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      verifyExactTextOnHomeListItemAtPosition(
        itemPosition = 0,
        targetViewId = R.id.welcome_text_view,
        stringToMatch = "Good morning, Admin!"
      )
    }
  }

  @Test
  fun testHomeActivity_afternoonTimestamp_goodAfternoonMessageIsDisplayed_withAdminProfileName() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    fakeOppiaClock.setCurrentTimeToSameDateTime(AFTERNOON_TIMESTAMP)
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      verifyExactTextOnHomeListItemAtPosition(
        itemPosition = 0,
        targetViewId = R.id.welcome_text_view,
        stringToMatch = "Good afternoon, Admin!"
      )
    }
  }

  @Test
  fun testHomeActivity_eveningTimestamp_goodEveningMessageIsDisplayed_withAdminProfileName() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    fakeOppiaClock.setCurrentTimeToSameDateTime(EVENING_TIMESTAMP)
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      verifyExactTextOnHomeListItemAtPosition(
        itemPosition = 0,
        targetViewId = R.id.welcome_text_view,
        stringToMatch = "Good evening, Admin!"
      )
    }
  }

  @Test
  fun testHomeActivity_logUserInFirstTime_checkPromotedStoriesIsNotDisplayed() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.recently_played_stories_text_view)).check(doesNotExist())
    }
  }

  @Test
  fun testPromotedStorySpotlight_setToShowOnSecondLogin_notSeenBefore_checkSpotlightShown() {
    logIntoUserTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      onView(withText(R.string.promoted_story_spotlight_hint))
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPromotedStoriesSpotlight_setToShowOnSecondLogin_pressDone_checkSpotlightNotShown() {
    logIntoUserTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.close_spotlight_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
    }

    // Re-launch the activity.
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      onView(withText(R.string.promoted_story_spotlight_hint)).check(doesNotExist())
    }
  }

  @Test
  fun testPromotedStoriesSpotlight_setToShowOnSecondLogin_checkNotShownOnFirstLogin() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      onView(withText(R.string.promoted_story_spotlight_hint)).check(doesNotExist())
    }
  }

  @Test
  fun testHomeActivity_recentlyPlayedStoriesTextIsDisplayed() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    logIntoUserTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      verifyExactTextOnHomeListItemAtPosition(
        itemPosition = 1,
        targetViewId = R.id.recently_played_stories_text_view,
        stringToMatch = context.getString(R.string.recently_played_stories)
      )
    }
  }

  @Test
  fun testHomeActivity_viewAllTextIsDisplayed() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = true
    )
    logIntoUserTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(position = 1)
      verifyExactTextOnHomeListItemAtPosition(
        itemPosition = 1,
        targetViewId = R.id.view_all_text_view,
        stringToMatch = context.getString(R.string.view_all)
      )
    }
  }

  @Test
  fun testHomeActivity_storiesPlayedOneWeekAgo_displaysLastPlayedStoriesText() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = true
    )
    testCoroutineDispatchers.runCurrent()
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = true
    )
    logIntoUserTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      verifyExactTextOnHomeListItemAtPosition(
        itemPosition = 1,
        targetViewId = R.id.recently_played_stories_text_view,
        stringToMatch = context.getString(R.string.last_played_stories)
      )
    }
  }

  @Test
  fun testHomeActivity_markStory0DoneForFraction_displaysRecommendedStories() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markCompletedFractionsTopic(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    logIntoUserTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {

      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      verifyExactTextOnHomeListItemAtPosition(
        itemPosition = 1,
        targetViewId = R.id.recently_played_stories_text_view,
        stringToMatch = context.getString(R.string.recommended_stories)
      )
      scrollToPositionOfPromotedList(position = 1)
      verifyTextOnPromotedListItemAtPosition(
        itemPosition = 0,
        targetViewId = R.id.topic_name_text_view,
        stringToMatch = "First Test Topic"
      )
      scrollToPositionOfPromotedList(position = 1)
      verifyTextOnPromotedListItemAtPosition(
        itemPosition = 1,
        targetViewId = R.id.topic_name_text_view,
        stringToMatch = "Ratios and Proportional Reasoning"
      )
    }
  }

  @Test
  fun testHomeActivity_markCompletedRatiosStory0_recommendsFractions() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markCompletedRatiosStory0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    logIntoUserTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      verifyExactTextOnHomeListItemAtPosition(
        itemPosition = 1,
        targetViewId = R.id.recently_played_stories_text_view,
        stringToMatch = context.getString(R.string.recommended_stories)
      )
      scrollToPositionOfPromotedList(position = 1)
      verifyTextOnPromotedListItemAtPosition(
        itemPosition = 0,
        targetViewId = R.id.topic_name_text_view,
        stringToMatch = "Fractions"
      )
    }
  }

  @Test
  fun testHomeActivity_noTopicProgress_initialRecommendationFractionsAndRatiosIsCorrect() {
    logIntoUserTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      verifyExactTextOnHomeListItemAtPosition(
        itemPosition = 1,
        targetViewId = R.id.recently_played_stories_text_view,
        stringToMatch = context.getString(R.string.recommended_stories)
      )
      scrollToPositionOfPromotedList(position = 1)
      verifyTextOnPromotedListItemAtPosition(
        itemPosition = 0,
        targetViewId = R.id.topic_name_text_view,
        stringToMatch = "Fractions"
      )
      scrollToPositionOfPromotedList(position = 1)
      verifyTextOnPromotedListItemAtPosition(
        itemPosition = 1,
        targetViewId = R.id.topic_name_text_view,
        stringToMatch = "Ratios and Proportional Reasoning"
      )
    }
  }

  @Test
  fun testHomeActivity_forPromotedActivityList_hideViewAll() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    logIntoUserTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      onView(
        allOf(
          withId(R.id.view_all_text_view),
          withEffectiveVisibility(Visibility.GONE)
        )
      )
    }
  }

  @Test
  fun testHomeActivity_markStory0DoneForRatiosAndFirstTestTopic_displaysRecommendedStories() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markCompletedTestTopic0Story0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedRatiosStory0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    logIntoUserTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      verifyExactTextOnHomeListItemAtPosition(
        itemPosition = 1,
        targetViewId = R.id.recently_played_stories_text_view,
        stringToMatch = context.getString(R.string.recommended_stories)
      )
      scrollToPositionOfPromotedList(position = 1)
      verifyTextOnPromotedListItemAtPosition(
        itemPosition = 0,
        targetViewId = R.id.topic_name_text_view,
        stringToMatch = "Second Test Topic"
      )
    }
  }

  @Test
  fun testHomeActivity_markAtLeastOneStoryCompletedForAllTopics_displaysComingSoonTopicsList() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markCompletedFractionsTopic(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedTestTopic0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedRatiosStory0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedTestTopic1(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    logIntoUserTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      verifyExactTextOnHomeListItemAtPosition(
        itemPosition = 1,
        targetViewId = R.id.coming_soon_topic_text_view,
        stringToMatch = context.getString(R.string.coming_soon)
      )
      scrollToPositionOfComingSoonList(position = 1)
      verifyTextOnComingSoonItemAtPosition(
        itemPosition = 0,
        targetViewId = R.id.topic_name_text_view,
        stringToMatch = "Third Test Topic"
      )
    }
  }

  @Test
  fun testHomeActivity_markFullProgressForSecondTestTopic_displaysComingSoonTopicsText() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markCompletedTestTopic1(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    logIntoUserTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      verifyExactTextOnHomeListItemAtPosition(
        itemPosition = 1,
        targetViewId = R.id.coming_soon_topic_text_view,
        stringToMatch = context.getString(R.string.coming_soon)
      )
      verifyTextOnHomeListItemAtPosition(
        itemPosition = 1,
        targetViewId = R.id.topic_name_text_view,
        stringToMatch = "Third Test Topic"
      )
    }
  }

  /*
   * # Dependency graph:
   *
   *      Fractions
   *         |
   *        |
   *       v
   * Test topic 0                     Ratios
   *    \                              /
   *     \                           /
   *       -----> Test topic 1 <----
   *
   * # Logic for recommendation system
   *
   * We always recommend the next topic that all dependencies are completed for. If a topic with
   * prerequisites is completed out-of-order (e.g. test topic 1 above) then we assume fractions is
   * already done. In the same way, finishing test topic 2 means there's nothing else to recommend.
   */
  @Test
  fun testHomeActivity_markStory0DonePlayStory1FirstTestTopic_playFractionsTopic_orderIsCorrect() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markCompletedTestTopic0Story0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedTestTopic1Story0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    logIntoUserTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      verifyExactTextOnHomeListItemAtPosition(
        itemPosition = 1,
        targetViewId = R.id.recently_played_stories_text_view,
        stringToMatch = context.getString(R.string.stories_for_you)
      )
      scrollToPositionOfPromotedList(position = 0)
      verifyTextOnPromotedListItemAtPosition(
        itemPosition = 0,
        targetViewId = R.id.topic_name_text_view,
        stringToMatch = "Second Test Topic"
      )
      scrollToPositionOfPromotedList(position = 1)
      verifyTextOnPromotedListItemAtPosition(
        itemPosition = 1,
        targetViewId = R.id.topic_name_text_view,
        stringToMatch = "Fractions"
      )
      scrollToPositionOfPromotedList(position = 2)
      verifyTextOnPromotedListItemAtPosition(
        itemPosition = 2,
        targetViewId = R.id.topic_name_text_view,
        stringToMatch = "Ratios and Proportional Reasoning"
      )
    }
  }

  @Test
  fun testHomeActivity_markStory0OfRatiosAndTestTopics0And1Done_playTestTopicStory0_noPromotions() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markCompletedRatiosStory0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedTestTopic1(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedTestTopic0Story0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedTestTopic0Story0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    logIntoUserTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      verifyExactTextOnHomeListItemAtPosition(
        itemPosition = 1,
        targetViewId = R.id.coming_soon_topic_text_view,
        stringToMatch = context.getString(R.string.coming_soon)
      )
      scrollToPositionOfComingSoonList(position = 1)
      verifyTextOnComingSoonItemAtPosition(
        itemPosition = 0,
        targetViewId = R.id.topic_name_text_view,
        stringToMatch = "Third Test Topic"
      )
    }
  }

  @Test
  fun testHomeActivity_markStory0DoneFirstTestTopic_recommendedStoriesIsCorrect() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markCompletedTestTopic0Story0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    logIntoUserTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      verifyExactTextOnHomeListItemAtPosition(
        itemPosition = 1,
        targetViewId = R.id.recently_played_stories_text_view,
        stringToMatch = context.getString(R.string.recommended_stories)
      )
      scrollToPositionOfPromotedList(position = 1)
      verifyTextOnPromotedListItemAtPosition(
        itemPosition = 0,
        targetViewId = R.id.topic_name_text_view,
        stringToMatch = "Ratios and Proportional Reasoning"
      )
    }
  }

  @Test
  fun testHomeActivity_markStory0DoneForFrac_recommendedStoriesIsCorrect() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markCompletedFractionsStory0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    logIntoUserTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(1)
      verifyTextOnHomeListItemAtPosition(
        itemPosition = 1,
        targetViewId = R.id.recently_played_stories_text_view,
        stringToMatch = context.getString(R.string.recommended_stories)
      )
      scrollToPositionOfPromotedList(position = 1)
      verifyTextOnPromotedListItemAtPosition(
        itemPosition = 0,
        targetViewId = R.id.topic_name_text_view,
        stringToMatch = "First Test Topic"
      )
      scrollToPositionOfPromotedList(position = 1)
      verifyTextOnPromotedListItemAtPosition(
        itemPosition = 1,
        targetViewId = R.id.topic_name_text_view,
        stringToMatch = "Ratios and Proportional Reasoning"
      )
    }
  }

  @Test
  fun testHomeActivity_clickViewAll_opensRecentlyPlayedActivity() {
    markSpotlightSeen(profileId1)
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedTestTopic1(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    logIntoUserTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.home_recycler_view,
          position = 1,
          targetViewId = R.id.view_all_text_view
        )
      ).perform(click())
      intended(hasComponent(RecentlyPlayedActivity::class.java.name))
    }
  }

  @Test
  fun testHomeActivity_promotedCard_chapterNameIsCorrect() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    logIntoUserTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      verifyTextOnHomeListItemAtPosition(
        itemPosition = 1,
        targetViewId = R.id.chapter_name_text_view,
        stringToMatch = "What is a Fraction?"
      )
    }
  }

  @Test
  fun testHomeActivity_promotedCard_storyNameIsCorrect() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    logIntoUserTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      verifyTextOnHomeListItemAtPosition(
        itemPosition = 1,
        targetViewId = R.id.story_name_text_view,
        stringToMatch = "Matthew Goes to the Bakery"
      )
    }
  }

  @Test
  fun testHomeActivity_configChange_promotedCard_storyNameIsCorrect() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = true
    )
    logIntoUserTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      onView(isRoot()).perform(orientationLandscape())
      verifyTextOnHomeListItemAtPosition(
        itemPosition = 1,
        targetViewId = R.id.story_name_text_view,
        stringToMatch = "Matthew Goes to the Bakery"
      )
    }
  }

  @Test
  fun testHomeActivity_markFullProgressForFractions_playRatios_displaysRecommendedStories() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedFractionsStory0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    logIntoUserTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      verifyExactTextOnHomeListItemAtPosition(
        itemPosition = 1,
        targetViewId = R.id.recently_played_stories_text_view,
        stringToMatch = context.getString(R.string.stories_for_you)
      )
      scrollToPositionOfPromotedList(1)
      verifyTextOnPromotedListItemAtPosition(
        itemPosition = 0,
        targetViewId = R.id.topic_name_text_view,
        stringToMatch = "Ratios and Proportional Reasoning"
      )
      scrollToPositionOfPromotedList(2)
      verifyTextOnPromotedListItemAtPosition(
        itemPosition = 1,
        targetViewId = R.id.topic_name_text_view,
        stringToMatch = "First Test Topic"
      )
    }
  }

  @Test
  fun testHomeActivity_clickPromotedStory_opensTopicActivity() {
    markSpotlightSeen(profileId1)
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    logIntoUserTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.home_recycler_view,
          position = 1,
          targetViewId = R.id.promoted_story_list_recycler_view
        )
      ).perform(click())

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
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#4700): Make this test work on Espresso.
  fun testHomeActivity_promotedStoryHasScalableWidth() {
    fontScaleConfigurationUtil.adjustFontScale(context, ReadingTextSize.EXTRA_LARGE_TEXT_SIZE)
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    logIntoUserTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.home_recycler_view,
          position = 1,
          targetViewId = R.id.promoted_story_list_recycler_view
        )
      ).check { view, _ ->
        val promotedStoryCard =
          view.findViewById<LessonThumbnailImageView>(R.id.lesson_thumbnail)
        val promotedStoryCardWidth = promotedStoryCard?.width?.toFloat()
        val expectedWidthInPixels = TypedValue.applyDimension(
          TypedValue.COMPLEX_UNIT_SP,
          280F,
          context.resources.displayMetrics
        )
        assertThat(promotedStoryCardWidth).isWithin(1e-5f).of(expectedWidthInPixels)
      }
    }
  }

  @Test
  fun testHomeActivity_promotedCard_topicNameIsCorrect() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = true
    )
    logIntoUserTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      verifyTextOnHomeListItemAtPosition(
        itemPosition = 1,
        targetViewId = R.id.topic_name_text_view,
        stringToMatch = "Fractions"
      )
    }
  }

  @Test
  fun testHomeActivity_firstTestTopic_topicSummary_opensTopicActivityThroughPlayIntent() {
    logIntoUserTwice()
    markSpotlightSeen(profileId1)
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(5)
      onView(
        atPositionOnView(
          R.id.home_recycler_view,
          5,
          R.id.topic_name_text_view
        )
      ).check(matches(withText(containsString("Fractions")))).perform(click())

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
  fun testHomeActivity_firstTestTopic_topicSummary_topicNameIsCorrect() {
    logIntoUserTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 3)
      verifyTextOnHomeListItemAtPosition(
        itemPosition = 3,
        targetViewId = R.id.topic_name_text_view,
        stringToMatch = "First Test Topic"
      )
    }
  }

  @Test
  fun testHomeActivity_fiveLessons_topicSummary_lessonCountIsCorrect() {
    logIntoUserTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 3)
      verifyTextOnHomeListItemAtPosition(
        itemPosition = 3,
        targetViewId = R.id.lesson_count_text_view,
        stringToMatch = "3 Lessons"
      )
    }
  }

  @Test
  fun testHomeActivity_secondTestTopic_topicSummary_allTopics_topicNameIsCorrect() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    logIntoUserTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 4)
      verifyTextOnHomeListItemAtPosition(
        itemPosition = 4,
        targetViewId = R.id.topic_name_text_view,
        stringToMatch = "Second Test Topic"
      )
    }
  }

  @Test
  fun testHomeActivity_oneLesson_topicSummary_lessonCountIsCorrect() {
    logIntoUserTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 4)
      verifyTextOnHomeListItemAtPosition(
        itemPosition = 4,
        targetViewId = R.id.lesson_count_text_view,
        stringToMatch = "1 Lesson"
      )
    }
  }

  // TODO(#2057): Remove when TextViews are properly measured in Robolectric.
  @RunOn(TestPlatform.ESPRESSO) // Incorrectly passes on Robolectric and shouldn't be re-enabled
  @Config(qualifiers = "+port-mdpi")
  @Test
  fun testHomeActivity_longProfileName_welcomeMessageIsDisplayed() {
    launch<HomeActivity>(createHomeActivityIntent(longNameInternalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      onView(
        atPositionOnView(
          R.id.home_recycler_view,
          0,
          R.id.welcome_text_view
        )
      ).check(matches(not(isEllipsized())))
    }
  }

  // TODO(#2057): Remove when TextViews are properly measured in Robolectric.
  @RunOn(TestPlatform.ESPRESSO) // Incorrectly passes on Robolectric and shouldn't be re-enabled
  @Config(qualifiers = "+land-mdpi")
  @Test
  fun testHomeActivity_configChange_longProfileName_welcomeMessageIsDisplayed() {
    launch<HomeActivity>(createHomeActivityIntent(longNameInternalProfileId)).use {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      onView(
        atPositionOnView(
          R.id.home_recycler_view,
          0,
          R.id.welcome_text_view
        )
      ).check(matches(not(isEllipsized())))
    }
  }

  // TODO(#2057): Remove when TextViews are properly measured in Robolectric.
  @RunOn(TestPlatform.ESPRESSO) // Incorrectly passes on Robolectric and shouldn't be re-enabled
  @Config(qualifiers = "+sw600dp-port")
  @Test
  fun testHomeActivity_longProfileName_tabletPortraitWelcomeMessageIsDisplayed() {
    launch<HomeActivity>(createHomeActivityIntent(longNameInternalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      onView(
        atPositionOnView(
          R.id.home_recycler_view,
          0,
          R.id.welcome_text_view
        )
      ).check(matches(not(isEllipsized())))
    }
  }

  // TODO(#2057): Remove when TextViews are properly measured in Robolectric.
  @RunOn(TestPlatform.ESPRESSO) // Incorrectly passes on Robolectric and shouldn't be re-enabled
  @Config(qualifiers = "+sw600dp-land")
  @Test
  fun testHomeActivity_longProfileName_tabletLandscapeWelcomeMessageIsDisplayed() {
    launch<HomeActivity>(createHomeActivityIntent(longNameInternalProfileId)).use {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      onView(
        atPositionOnView(
          R.id.home_recycler_view,
          0,
          R.id.welcome_text_view
        )
      ).check(matches(not(isEllipsized())))
    }
  }

  @Test
  fun testHomeActivity_oneLesson_topicSummary_configChange_lessonCountIsCorrect() {
    logIntoUserTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 4)
      verifyTextOnHomeListItemAtPosition(
        itemPosition = 4,
        targetViewId = R.id.lesson_count_text_view,
        stringToMatch = "1 Lesson"
      )
    }
  }

  @Test
  fun testHomeActivity_clickTopicSummary_opensTopicActivity() {
    logIntoUserTwice()
    markSpotlightSeen(profileId1)
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 3)
      onView(atPosition(R.id.home_recycler_view, 3)).perform(click())

      val args = TopicActivityParams.newBuilder().apply {
        this.classroomId = TEST_CLASSROOM_ID_0
        this.topicId = TEST_TOPIC_ID_0
        this.storyId = TEST_STORY_ID_0
      }.build()
      intended(hasComponent(TopicActivity::class.java.name))
      intended(hasProtoExtra(TopicActivity.TOPIC_ACTIVITY_PARAMS_KEY, args))
    }
  }

  @Test
  fun testHomeActivity_onBackPressed_exitToProfileChooserDialogIsDisplayed() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      pressBack()
      onView(withText(R.string.home_activity_back_dialog_message))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testHomeActivity_onBackPressed_configChange_exitToProfileChooserDialogIsDisplayed() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {

      testCoroutineDispatchers.runCurrent()
      pressBack()
      onView(isRoot()).perform(orientationLandscape())
      onView(withText(R.string.home_activity_back_dialog_message))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testHomeActivity_onBackPressed_clickExit_opensProfileActivity() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      pressBack()
      onView(withText(R.string.home_activity_back_dialog_exit))
        .inRoot(isDialog())
        .perform(click())
      intended(hasComponent(ProfileChooserActivity::class.java.name))
    }
  }

  @Test
  fun testHomeActivity_checkSpanForItem0_spanSizeIsTwoOrThree() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      if (context.resources.getBoolean(R.bool.isTablet)) {
        onView(withId(R.id.home_recycler_view)).check(hasGridItemCount(3, 0))
      } else {
        onView(withId(R.id.home_recycler_view)).check(hasGridItemCount(2, 0))
      }
    }
  }

  @Test
  fun testHomeActivity_checkSpanForItem4_spanSizeIsOne() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.home_recycler_view)).check(hasGridItemCount(1, 4))
    }
  }

  @Test
  fun testHomeActivity_configChange_checkSpanForItem4_spanSizeIsOne() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.home_recycler_view)).check(hasGridItemCount(1, 4))
    }
  }

  @Test
  fun testHomeActivity_allTopicsCompleted_hidesPromotedStories() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markAllTopicsAsCompleted(
      profileId = createProfileId(internalProfileId),
      timestampOlderThanOneWeek = false
    )
    logIntoAdminTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      onView(
        atPositionOnView(
          R.id.home_recycler_view,
          2,
          R.id.promoted_story_list_recycler_view
        )
      ).check(doesNotExist())
    }
  }

  @Test
  fun testHomeActivity_partialProgressForFractionsAndRatios_showsRecentlyPlayedStories() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
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
    logIntoAdminTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      verifyExactTextOnHomeListItemAtPosition(
        itemPosition = 1,
        targetViewId = R.id.recently_played_stories_text_view,
        stringToMatch = context.getString(R.string.recently_played_stories)
      )
    }
  }

  @Test
  fun testHomeActivity_allTopicsCompleted_displaysAllTopicsHeader() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markAllTopicsAsCompleted(
      profileId = createProfileId(internalProfileId),
      timestampOlderThanOneWeek = false
    )
    logIntoAdminTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 2)
      verifyExactTextOnHomeListItemAtPosition(
        itemPosition = 2,
        targetViewId = R.id.all_topics_text_view,
        stringToMatch = context.getString((R.string.select_a_topic_to_start))
      )
    }
  }

  @Config(qualifiers = "+port")
  @Test
  fun testHomeActivity_allTopicsCompleted_mobilePortrait_displaysAllTopicCardsIn2Columns() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markAllTopicsAsCompleted(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    logIntoAdminTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 3)
      verifyHomeRecyclerViewHasGridColumnCount(columnCount = 2)
    }
  }

  @Config(qualifiers = "+land")
  @Test
  fun testHomeActivity_allTopicsCompleted_mobileLandscape_displaysAllTopicCardsIn3Columns() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markAllTopicsAsCompleted(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    logIntoAdminTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 3)
      verifyHomeRecyclerViewHasGridColumnCount(columnCount = 3)
    }
  }

  @Config(qualifiers = "+sw600dp-port")
  @Test
  fun testHomeActivity_allTopicsCompleted_tabletPortrait_displaysAllTopicCardsIn3Columns() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markAllTopicsAsCompleted(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    logIntoAdminTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 3)
      verifyHomeRecyclerViewHasGridColumnCount(columnCount = 3)
    }
  }

  @Config(qualifiers = "+sw600dp-land")
  @Test
  fun testHomeActivity_allTopicsCompleted_tabletLandscape_displaysAllTopicCardsIn4Columns() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markAllTopicsAsCompleted(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    logIntoAdminTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 3)
      verifyHomeRecyclerViewHasGridColumnCount(columnCount = 4)
    }
  }

  @Test
  fun testHomeActivity_noTopicsCompleted_displaysAllTopicsHeader() {
    // Only new users will have no progress for any topics.
    logIntoAdminTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 2)
      verifyExactTextOnHomeListItemAtPosition(
        itemPosition = 2,
        targetViewId = R.id.all_topics_text_view,
        stringToMatch = context.getString((R.string.select_a_topic_to_start))
      )
    }
  }

  @Config(qualifiers = "+port")
  @Test
  fun testHomeActivity_noTopicsStarted_mobilePortraitDisplaysTopicsIn2Columns() {
    // Only new users will have no progress for any topics.
    logIntoAdminTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      if (context.resources.getBoolean(R.bool.isTablet)) {
        verifyHomeRecyclerViewHasGridColumnCount(columnCount = 3)
      } else {
        verifyHomeRecyclerViewHasGridColumnCount(columnCount = 2)
      }

      scrollToPosition(position = 3)
      onView(withId(R.id.home_recycler_view))
        .check(hasGridItemCount(spanCount = 1, position = 3))
    }
  }

  @Config(qualifiers = "+land")
  @Test
  fun testHomeActivity_noTopicsStarted_mobileLandscapeDisplaysTopicsIn3Columns() {
    // Only new users will have no progress for any topics.
    logIntoAdminTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      if (context.resources.getBoolean(R.bool.isTablet)) {
        verifyHomeRecyclerViewHasGridColumnCount(columnCount = 4)
      } else {
        verifyHomeRecyclerViewHasGridColumnCount(columnCount = 3)
      }

      scrollToPosition(position = 3)
      onView(withId(R.id.home_recycler_view))
        .check(hasGridItemCount(spanCount = 1, position = 3))
    }
  }

  @Config(qualifiers = "+sw600dp-port")
  @Test
  fun testHomeActivity_noTopicsStarted_tabletPortraitDisplaysTopicsIn3Columns() {
    // Only new users will have no progress for any topics.
    logIntoAdminTwice()
    markSpotlightSeen(profileId)
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      verifyHomeRecyclerViewHasGridColumnCount(columnCount = 3)

      scrollToPosition(position = 3)
      onView(withId(R.id.home_recycler_view))
        .check(hasGridItemCount(spanCount = 1, position = 3))
    }
  }

  @Config(qualifiers = "+sw600dp-land")
  @Test
  fun testHomeActivity_noTopicsStarted_tabletLandscapeDisplaysTopicsIn4Columns() {
    // Only new users will have no progress for any topics.
    logIntoAdminTwice()
    markSpotlightSeen(profileId)
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      verifyHomeRecyclerViewHasGridColumnCount(columnCount = 4)

      scrollToPosition(position = 3)
      onView(withId(R.id.home_recycler_view))
        .check(hasGridItemCount(spanCount = 1, position = 3))
    }
  }

  @Test
  fun testHomeActivity_multipleRecentlyPlayedStories_mobileShows3PromotedStories() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedTestTopic0Story0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedTestTopic1Story2Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedFractionsStory0Exp0(
      profileId = profileId1, timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedRatiosStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedRatiosStory1Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    logIntoAdminTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.home_recycler_view,
          position = 1,
          targetViewId = R.id.promoted_story_list_recycler_view
        )
      ).check(hasItemCount(count = 3))
    }
  }

  @Config(qualifiers = "+sw600dp-port")
  @Test
  fun testHomeActivity_multipleRecentlyPlayedStories_tabletPortraitShows3PromotedStories() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedTestTopic0Story0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressNotSavedTestTopic1Story2Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    val profileId1 = createProfileId(internalProfileId)
    storyProgressTestHelper.markCompletedFractionsStory0Exp0(
      profileId = profileId1, timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedRatiosStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedRatiosStory1Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    logIntoAdminTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.home_recycler_view,
          position = 1,
          targetViewId = R.id.promoted_story_list_recycler_view
        )
      ).check(hasItemCount(count = 3))
    }
  }

  @Config(qualifiers = "+sw600dp-land")
  @Test
  fun testHomeActivity_multipleRecentlyPlayedStories_tabletLandscapeShows4PromotedStories() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedTestTopic0Story0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedTestTopic1Story2Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedFractionsStory0Exp0(
      profileId = profileId, timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedRatiosStory1Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    logIntoAdminTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 1)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.home_recycler_view,
          position = 1,
          targetViewId = R.id.promoted_story_list_recycler_view
        )
      ).check(hasItemCount(count = 4))
    }
  }

  @Test
  fun testHomeActivity_onScrollDown_promotedStoryListViewStillShows() {
    // This test is to catch a bug introduced and then fixed in #2246
    // (see https://github.com/oppia/oppia-android/pull/2246#pullrequestreview-565964462)
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markCompletedFractionsStory0Exp0(
      profileId = profileId, timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedRatiosStory1Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    logIntoAdminTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 6)

      scrollToPosition(position = 0)
      onView(withId(R.id.promoted_story_list_recycler_view))
        .check(hasItemCount(count = 3))
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3840): Make this test work on Espresso.
  fun testHomeActivity_defaultState_displaysStringsInEnglish() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    fakeOppiaClock.setCurrentTimeToSameDateTime(MORNING_TIMESTAMP)
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(position = 0)

      verifyExactTextOnHomeListItemAtPosition(
        itemPosition = 0,
        targetViewId = R.id.welcome_text_view,
        stringToMatch = "Good morning, Admin!"
      )
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3840): Make this test work on Espresso.
  fun testHomeActivity_defaultState_hasEnglishAndroidLocale() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()

      // Verify that the locale is set up correctly (for string resource selection).
      val locale = Locale.getDefault()
      assertThat(locale.language).isEqualTo("en")
    }
  }

  // TODO(#3840): Make this test work on Espresso & Robolectric.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testHomeActivity_defaultState_hasEnglishDisplayLocale() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()

      // Verify that the display locale is set up correctly (for string formatting).
      val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
      val localeContext = displayLocale.localeContext
      assertThat(localeContext.languageDefinition.language).isEqualTo(ENGLISH)
    }
  }

  // TODO(#3840): Make this test work on Espresso & Robolectric.
  @Test
  @Ignore("Current language switching mechanism doesn't work correctly in Robolectric")
  fun testHomeActivity_changeSystemLocaleAndConfigChange_recreatesActivity() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    fakeOppiaClock.setCurrentTimeToSameDateTime(MORNING_TIMESTAMP)
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      // Sanity check to ensure that no recreations initially happen.
      assertThat(testActivityRecreator.getRecreateCount()).isEqualTo(0)

      // Attempt to change the configuration's locale & recreate it (to simulate a locale-based
      // configuration change).
      val newLocale = BRAZIL_PORTUGUESE_LOCALE
      scenario.onActivity { activity ->
        val newConfiguration = Configuration(activity.resources.configuration)
        newConfiguration.setLocale(newLocale)
        activity.resources.configuration.updateFrom(newConfiguration)
      }
      context.resources.configuration.setLocale(newLocale)
      Locale.setDefault(newLocale)
      scenario.recreate()
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(position = 0)
      verifyExactTextOnHomeListItemAtPosition(
        itemPosition = 0,
        targetViewId = R.id.welcome_text_view,
        stringToMatch = "Bom dia, Admin!"
      )
      assertThat(testActivityRecreator.getRecreateCount()).isEqualTo(1)
      // Verify that the display locale is set up correctly (for string formatting).
      val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
      val localeContext = displayLocale.localeContext
      assertThat(localeContext.languageDefinition.language).isEqualTo(BRAZILIAN_PORTUGUESE)
    }
  }

  @Test
  @DefineAppLanguageLocaleContext(
    oppiaLanguageEnumId = ARABIC_VALUE,
    appStringIetfTag = "ar",
    appStringAndroidLanguageId = "ar"
  )
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3840): Make this test work on Espresso & Robolectric.
  fun testHomeActivity_initialArabicContext_displaysStringsInArabic() {
    // Ensure the system locale matches the initial locale context.
    forceDefaultLocale(EGYPT_ARABIC_LOCALE)
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    fakeOppiaClock.setCurrentTimeToSameDateTime(MORNING_TIMESTAMP)
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(position = 0)

      verifyExactTextOnHomeListItemAtPosition(
        itemPosition = 0,
        targetViewId = R.id.welcome_text_view,
        stringToMatch = "صباح الخير \u200F\u202AAdmin\u202C\u200F!"
      )
    }
  }

  @Test
  @DefineAppLanguageLocaleContext(
    oppiaLanguageEnumId = ARABIC_VALUE,
    appStringIetfTag = "ar",
    appStringAndroidLanguageId = "ar"
  )
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3840): Make this test work on Espresso & Robolectric.
  fun testHomeActivity_initialArabicContext_isInRtlLayout() {
    // Ensure the system locale matches the initial locale context.
    forceDefaultLocale(EGYPT_ARABIC_LOCALE)
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    fakeOppiaClock.setCurrentTimeToSameDateTime(MORNING_TIMESTAMP)
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()

      val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
      val layoutDirection = displayLocale.getLayoutDirection()
      assertThat(layoutDirection).isEqualTo(ViewCompat.LAYOUT_DIRECTION_RTL)
    }
  }

  // TODO(#3840): Make this test work on Espresso & Robolectric.
  @Test
  @DefineAppLanguageLocaleContext(
    oppiaLanguageEnumId = ARABIC_VALUE,
    appStringIetfTag = "ar",
    appStringAndroidLanguageId = "ar"
  )
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testHomeActivity_initialArabicContext_hasArabicDisplayLocale() {
    // Ensure the system locale matches the initial locale context.
    forceDefaultLocale(EGYPT_ARABIC_LOCALE)
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    fakeOppiaClock.setCurrentTimeToSameDateTime(MORNING_TIMESTAMP)
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()

      // Verify that the display locale is set up correctly (for string formatting).
      val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
      val localeContext = displayLocale.localeContext
      assertThat(localeContext.languageDefinition.language).isEqualTo(ARABIC)
    }
  }

  @Test
  @DefineAppLanguageLocaleContext(
    oppiaLanguageEnumId = BRAZILIAN_PORTUGUESE_VALUE,
    appStringIetfTag = "pt-BR",
    appStringAndroidLanguageId = "pt",
    appStringAndroidRegionId = "BR"
  )
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3840): Make this test work on Espresso & Robolectric.
  fun testHomeActivity_initialBrazilianPortugueseContext_displayStringsInPortuguese() {
    // Ensure the system locale matches the initial locale context.
    forceDefaultLocale(BRAZIL_PORTUGUESE_LOCALE)
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    fakeOppiaClock.setCurrentTimeToSameDateTime(MORNING_TIMESTAMP)
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(position = 0)

      verifyExactTextOnHomeListItemAtPosition(
        itemPosition = 0,
        targetViewId = R.id.welcome_text_view,
        stringToMatch = "Bom dia, Admin!"
      )
    }
  }

  @Test
  @DefineAppLanguageLocaleContext(
    oppiaLanguageEnumId = BRAZILIAN_PORTUGUESE_VALUE,
    appStringIetfTag = "pt-BR",
    appStringAndroidLanguageId = "pt",
    appStringAndroidRegionId = "BR"
  )
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3840): Make this test work on Espresso & Robolectric.
  fun testHomeActivity_initialBrazilianPortugueseContext_isInLtrLayout() {
    // Ensure the system locale matches the initial locale context.
    forceDefaultLocale(BRAZIL_PORTUGUESE_LOCALE)
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    fakeOppiaClock.setCurrentTimeToSameDateTime(MORNING_TIMESTAMP)
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()

      val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
      val layoutDirection = displayLocale.getLayoutDirection()
      assertThat(layoutDirection).isEqualTo(ViewCompat.LAYOUT_DIRECTION_LTR)
    }
  }

  // TODO(#3840): Make this test work on Espresso & Robolectric.
  @Test
  @DefineAppLanguageLocaleContext(
    oppiaLanguageEnumId = BRAZILIAN_PORTUGUESE_VALUE,
    appStringIetfTag = "pt-BR",
    appStringAndroidLanguageId = "pt",
    appStringAndroidRegionId = "BR"
  )
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testHomeActivity_initialBrazilianPortugueseContext_hasPortugueseDisplayLocale() {
    // Ensure the system locale matches the initial locale context.
    forceDefaultLocale(BRAZIL_PORTUGUESE_LOCALE)
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    fakeOppiaClock.setCurrentTimeToSameDateTime(MORNING_TIMESTAMP)
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()

      // Verify that the display locale is set up correctly (for string formatting).
      val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
      val localeContext = displayLocale.localeContext
      assertThat(localeContext.languageDefinition.language).isEqualTo(BRAZILIAN_PORTUGUESE)
    }
  }

  @Test
  @DefineAppLanguageLocaleContext(
    oppiaLanguageEnumId = NIGERIAN_PIDGIN_VALUE,
    appStringIetfTag = "pcm",
    appStringAndroidLanguageId = "pcm",
    appStringAndroidRegionId = "NG"
  )
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3840): Make this test work on Espresso & Robolectric.
  fun testHomeActivity_initialNigerianPidginContext_isInLtrLayout() {
    // Ensure the system locale matches the initial locale context.
    forceDefaultLocale(NIGERIA_NAIJA_LOCALE)
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    fakeOppiaClock.setCurrentTimeToSameDateTime(MORNING_TIMESTAMP)
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()

      val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
      val layoutDirection = displayLocale.getLayoutDirection()
      assertThat(layoutDirection).isEqualTo(ViewCompat.LAYOUT_DIRECTION_LTR)
    }
  }

  // TODO(#3840): Make this test work on Espresso & Robolectric.
  @Test
  @DefineAppLanguageLocaleContext(
    oppiaLanguageEnumId = NIGERIAN_PIDGIN_VALUE,
    appStringIetfTag = "pcm",
    appStringAndroidLanguageId = "pcm",
    appStringAndroidRegionId = "NG"
  )
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testHomeActivity_initialNigerianPidginContext_hasNaijaDisplayLocale() {
    // Ensure the system locale matches the initial locale context.
    forceDefaultLocale(NIGERIA_NAIJA_LOCALE)
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    fakeOppiaClock.setCurrentTimeToSameDateTime(MORNING_TIMESTAMP)
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()

      // Verify that the display locale is set up correctly (for string formatting).
      val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
      val localeContext = displayLocale.localeContext
      assertThat(localeContext.languageDefinition.language).isEqualTo(NIGERIAN_PIDGIN)
    }
  }

  private fun markSpotlightSeen(profileId: ProfileId) {
    spotlightStateController.markSpotlightViewed(profileId, Spotlight.FeatureCase.PROMOTED_STORIES)
    testCoroutineDispatchers.runCurrent()
  }

  private fun createHomeActivityIntent(internalProfileId: Int): Intent {
    val profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(internalProfileId).build()
    return HomeActivity.createHomeActivity(context, profileId)
  }

  // Reference - https://stackoverflow.com/a/61455336/12215015
  private fun isEllipsized() = object : TypeSafeMatcher<View>() {
    override fun describeTo(description: Description) {
      description.appendText("with ellipsized text")
    }

    override fun matchesSafely(view: View): Boolean {
      return view is TextView && with((view).layout) {
        lineCount > 0 && getEllipsisCount(lineCount - 1) > 0
      }
    }
  }

  private fun scrollToPosition(position: Int) {
    onView(withId(R.id.home_recycler_view)).perform(
      scrollToPosition<RecyclerView.ViewHolder>(
        position
      )
    )
  }

  private fun scrollToPositionOfPromotedList(position: Int) {
    onView(withId(R.id.promoted_story_list_recycler_view)).perform(
      scrollToPosition<RecyclerView.ViewHolder>(
        position
      )
    )
  }

  private fun scrollToPositionOfComingSoonList(position: Int) {
    onView(withId(R.id.coming_soon_topic_list_recycler_view)).perform(
      scrollToPosition<RecyclerView.ViewHolder>(
        position
      )
    )
  }

  private fun forceDefaultLocale(locale: Locale) {
    context.applicationContext.resources.configuration.setLocale(locale)
    Locale.setDefault(locale)
  }

  private fun verifyTextOnHomeListItemAtPosition(
    itemPosition: Int,
    targetViewId: Int,
    stringToMatch: String
  ) {
    onView(
      atPositionOnView(
        R.id.home_recycler_view,
        itemPosition,
        targetViewId
      )
    ).check(matches(withText(containsString(stringToMatch))))
  }

  private fun verifyTextOnPromotedListItemAtPosition(
    itemPosition: Int,
    targetViewId: Int,
    stringToMatch: String
  ) {
    onView(
      atPositionOnView(
        R.id.promoted_story_list_recycler_view,
        itemPosition,
        targetViewId
      )
    ).check(matches(withText(containsString(stringToMatch))))
  }

  private fun verifyTextOnComingSoonItemAtPosition(
    itemPosition: Int,
    targetViewId: Int,
    stringToMatch: String
  ) {
    onView(
      atPositionOnView(
        R.id.coming_soon_topic_list_recycler_view,
        itemPosition,
        targetViewId
      )
    ).check(matches(withText(containsString(stringToMatch))))
  }

  private fun verifyExactTextOnHomeListItemAtPosition(
    itemPosition: Int,
    targetViewId: Int,
    stringToMatch: String
  ) {
    onView(
      atPositionOnView(
        R.id.home_recycler_view,
        itemPosition,
        targetViewId
      )
    ).check(matches(withText(stringToMatch)))
  }

  private fun verifyHomeRecyclerViewHasGridColumnCount(columnCount: Int) {
    onView(withId(R.id.home_recycler_view)).check(hasGridColumnCount(columnCount))
  }

  private fun createProfileId(internalProfileId: Int): ProfileId {
    return ProfileId.newBuilder().setLoggedInInternalProfileId(internalProfileId).build()
  }

  private fun logIntoAdminTwice() {
    dataProviderTestMonitor.waitForNextSuccessfulResult(profileTestHelper.logIntoAdmin())
    dataProviderTestMonitor.waitForNextSuccessfulResult(profileTestHelper.logIntoAdmin())
  }

  private fun logIntoUserTwice() {
    dataProviderTestMonitor.waitForNextSuccessfulResult(profileTestHelper.logIntoUser())
    dataProviderTestMonitor.waitForNextSuccessfulResult(profileTestHelper.logIntoUser())
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
      TestPlatformParameterModule::class, PlatformParameterSingletonModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class, WorkManagerConfigurationModule::class,
      ApplicationStartupListenerModule::class, LogReportWorkerModule::class,
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
      EventLoggingConfigurationModule::class, ActivityRouterModule::class,
      CpuPerformanceSnapshotterModule::class, ExplorationProgressModule::class,
      TestAuthenticationModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder {
      override fun build(): TestApplicationComponent
    }

    fun inject(homeActivityTest: HomeActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerHomeActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(homeActivityTest: HomeActivityTest) {
      component.inject(homeActivityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }

  private companion object {
    private val BRAZIL_PORTUGUESE_LOCALE = Locale("pt", "BR")
    private val EGYPT_ARABIC_LOCALE = Locale("ar", "EG")
    private val NIGERIA_NAIJA_LOCALE = Locale("pcm", "NG")
  }
}
