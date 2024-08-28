package org.oppia.android.app.classroom

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
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
import org.oppia.android.app.classroom.classroomlist.ALL_CLASSROOMS_HEADER_TEST_TAG
import org.oppia.android.app.classroom.classroomlist.CLASSROOM_CARD_ICON_TEST_TAG
import org.oppia.android.app.classroom.classroomlist.CLASSROOM_LIST_TEST_TAG
import org.oppia.android.app.classroom.promotedlist.COMING_SOON_TOPIC_LIST_HEADER_TEST_TAG
import org.oppia.android.app.classroom.promotedlist.COMING_SOON_TOPIC_LIST_TEST_TAG
import org.oppia.android.app.classroom.promotedlist.PROMOTED_STORY_LIST_HEADER_TEST_TAG
import org.oppia.android.app.classroom.promotedlist.PROMOTED_STORY_LIST_TEST_TAG
import org.oppia.android.app.classroom.topiclist.ALL_TOPICS_HEADER_TEST_TAG
import org.oppia.android.app.classroom.welcome.WELCOME_TEST_TAG
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.home.recentlyplayed.RecentlyPlayedActivity
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.TopicActivityParams
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
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
import org.oppia.android.domain.classroom.TEST_CLASSROOM_ID_0
import org.oppia.android.domain.classroom.TEST_CLASSROOM_ID_1
import org.oppia.android.domain.exploration.ExplorationProgressModule
import org.oppia.android.domain.exploration.ExplorationStorageModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.AppStartupStateController
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulerModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.FRACTIONS_STORY_ID_0
import org.oppia.android.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.android.domain.topic.TEST_STORY_ID_0
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.FakeAnalyticsEventLogger
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestImageLoaderModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.firebase.TestAuthenticationModule
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
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.logging.EventLoggingConfigurationModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

// Time: Tue Apr 23 2019 23:22:00
private const val EVENING_TIMESTAMP = 1556061720000

// Time: Wed Apr 24 2019 08:22:00
private const val MORNING_TIMESTAMP = 1556094120000

// Time: Tue Apr 23 2019 14:22:00
private const val AFTERNOON_TIMESTAMP = 1556029320000

/** Tests for [ClassroomListFragment]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = ClassroomListFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class ClassroomListFragmentTest {
  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val composeRule = createAndroidComposeRule<ClassroomListActivity>()

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var fakeOppiaClock: FakeOppiaClock

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var machineLocale: OppiaLocale.MachineLocale

  @Inject
  lateinit var storyProgressTestHelper: StoryProgressTestHelper

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var dataProviderTestMonitor: DataProviderTestMonitor.Factory

  @Inject
  lateinit var fakeAnalyticsEventLogger: FakeAnalyticsEventLogger

  private val internalProfileId: Int = 0
  private lateinit var profileId: ProfileId

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    testCoroutineDispatchers.registerIdlingResource()
    profileTestHelper.initializeProfiles()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    TestPlatformParameterModule.reset()
    Intents.release()
  }

  @Test
  fun testFragment_onLaunch_logsEvent() {
    testCoroutineDispatchers.runCurrent()
    val event = fakeAnalyticsEventLogger.getOldestEvent()

    assertThat(event.priority).isEqualTo(EventLog.Priority.ESSENTIAL)
    assertThat(event.context.activityContextCase)
      .isEqualTo(EventLog.Context.ActivityContextCase.OPEN_HOME)
  }

  @Test
  fun testFragment_onFirstLaunch_logsCompletedOnboardingEvent() {
    val event = fakeAnalyticsEventLogger.getMostRecentEvents(2).last()

    assertThat(event.priority).isEqualTo(EventLog.Priority.OPTIONAL)
    assertThat(event.context.activityContextCase).isEqualTo(
      EventLog.Context.ActivityContextCase.COMPLETE_APP_ONBOARDING
    )
  }

  @Test
  fun testFragment_onboardingV2Enabled_onInitialLaunch_logsEndProfileOnboardingEvent() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)
    profileTestHelper.addOnlyAdminProfileWithoutPin()
    testCoroutineDispatchers.runCurrent()

    // OPEN_HOME, END_PROFILE_ONBOARDING_EVENT and COMPLETE_APP_ONBOARDING are all logged
    // concurrently, in no defined order, and the actual order depends entirely on execution time.
    val eventLog = getOneOfLastThreeEventsLogged(
      EventLog.Context.ActivityContextCase.END_PROFILE_ONBOARDING_EVENT
    )
    val eventLogContext = eventLog.context

    assertThat(eventLogContext.activityContextCase)
      .isEqualTo(EventLog.Context.ActivityContextCase.END_PROFILE_ONBOARDING_EVENT)
    assertThat(eventLogContext.endProfileOnboardingEvent.profileId.internalId).isEqualTo(
      internalProfileId
    )
  }

  @Test
  fun testFragment_allComponentsAreDisplayed() {
    composeRule.onNodeWithTag(WELCOME_TEST_TAG).assertIsDisplayed()
    composeRule.onNodeWithTag(ALL_CLASSROOMS_HEADER_TEST_TAG).assertIsDisplayed()
    composeRule.onNodeWithTag(CLASSROOM_LIST_TEST_TAG).assertIsDisplayed()
    composeRule.onNodeWithTag(ALL_TOPICS_HEADER_TEST_TAG).assertIsDisplayed()
  }

  @Test
  fun testFragment_loginTwice_allComponentsAreDisplayed() {
    logIntoAdminTwice()
    composeRule.onNodeWithTag(WELCOME_TEST_TAG).assertIsDisplayed()
    composeRule.onNodeWithTag(PROMOTED_STORY_LIST_HEADER_TEST_TAG).assertIsDisplayed()
    composeRule.onNodeWithTag(PROMOTED_STORY_LIST_TEST_TAG).assertIsDisplayed()
    composeRule.onNodeWithTag(ALL_CLASSROOMS_HEADER_TEST_TAG).assertIsDisplayed()
    composeRule.onNodeWithTag(CLASSROOM_LIST_TEST_TAG).assertIsDisplayed()

    composeRule.onNodeWithTag(CLASSROOM_LIST_SCREEN_TEST_TAG).performScrollToNode(
      hasTestTag(ALL_TOPICS_HEADER_TEST_TAG)
    )
    composeRule.onNodeWithTag(ALL_TOPICS_HEADER_TEST_TAG).assertIsDisplayed()
  }

  @Test
  fun testFragment_withAdminProfile_configChange_profileNameIsDisplayed() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    fakeOppiaClock.setCurrentTimeToSameDateTime(EVENING_TIMESTAMP)

    // Refresh the welcome text content.
    logIntoAdmin()

    onView(isRoot()).perform(orientationLandscape())

    composeRule.onNodeWithTag(WELCOME_TEST_TAG)
      .assertTextContains("Good evening, Admin!")
      .assertIsDisplayed()
  }

  @Test
  fun testFragment_morningTimestamp_goodMorningMessageIsDisplayed_withAdminProfileName() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    fakeOppiaClock.setCurrentTimeToSameDateTime(MORNING_TIMESTAMP)

    // Refresh the welcome text content.
    logIntoAdmin()

    composeRule.onNodeWithTag(WELCOME_TEST_TAG)
      .assertTextContains("Good morning, Admin!")
      .assertIsDisplayed()
  }

  @Test
  fun testFragment_afternoonTimestamp_goodAfternoonMessageIsDisplayed_withAdminProfileName() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    fakeOppiaClock.setCurrentTimeToSameDateTime(AFTERNOON_TIMESTAMP)

    // Refresh the welcome text content.
    logIntoAdmin()

    composeRule.onNodeWithTag(WELCOME_TEST_TAG)
      .assertTextContains("Good afternoon, Admin!")
      .assertIsDisplayed()
  }

  @Test
  fun testFragment_eveningTimestamp_goodEveningMessageIsDisplayed_withAdminProfileName() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    fakeOppiaClock.setCurrentTimeToSameDateTime(EVENING_TIMESTAMP)

    // Refresh the welcome text content.
    logIntoAdmin()

    composeRule.onNodeWithTag(WELCOME_TEST_TAG)
      .assertTextContains("Good evening, Admin!")
      .assertIsDisplayed()
  }

  @Test
  fun testFragment_logUserInFirstTime_checkPromotedStoriesIsNotDisplayed() {
    composeRule.onNodeWithTag(PROMOTED_STORY_LIST_HEADER_TEST_TAG).assertDoesNotExist()
    composeRule.onNodeWithTag(PROMOTED_STORY_LIST_TEST_TAG).assertDoesNotExist()
  }

  @Test
  fun testFragment_recentlyPlayedStoriesTextIsDisplayed() {
    logIntoAdminTwice()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )

    composeRule.onNodeWithTag(PROMOTED_STORY_LIST_HEADER_TEST_TAG).onChildAt(0)
      .assertTextContains(context.getString(R.string.recently_played_stories))
      .assertIsDisplayed()
  }

  @Test
  fun testFragment_viewAllTextIsDisplayed() {
    logIntoAdminTwice()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedTestTopic0Story0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )

    composeRule.onNodeWithTag(PROMOTED_STORY_LIST_HEADER_TEST_TAG).onChildAt(1)
      .assertTextContains(
        machineLocale.run { context.getString(R.string.view_all).toMachineUpperCase() }
      )
      .assertIsDisplayed()
  }

  @Test
  fun testFragment_storiesPlayedOneWeekAgo_displaysLastPlayedStoriesText() {
    logIntoAdminTwice()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = true
    )
    testCoroutineDispatchers.runCurrent()
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = true
    )

    composeRule.onNodeWithTag(PROMOTED_STORY_LIST_HEADER_TEST_TAG).onChildAt(0)
      .assertTextContains(context.getString(R.string.last_played_stories))
      .assertIsDisplayed()
  }

  @Test
  fun testFragment_markStory0DoneForFraction_displaysRecommendedStories() {
    logIntoAdminTwice()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markCompletedFractionsTopic(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )

    composeRule.onNodeWithTag(PROMOTED_STORY_LIST_HEADER_TEST_TAG).onChildAt(0)
      .assertTextContains(context.getString(R.string.recommended_stories))
      .assertIsDisplayed()

    composeRule.onNodeWithTag(PROMOTED_STORY_LIST_TEST_TAG).apply {
      onChildAt(0)
        .assertTextContains("Prototype Exploration")
        .assertTextContains("FIRST TEST TOPIC")
        .assertTextContains("SCIENCE")
        .assertIsDisplayed()

      onChildAt(1)
        .assertTextContains("What is a Ratio?")
        .assertTextContains("RATIOS AND PROPORTIONAL REASONING")
        .assertTextContains("MATHS")
        .assertIsDisplayed()
    }
  }

  @Test
  fun testFragment_markCompletedRatiosStory0_recommendsFractions() {
    logIntoAdminTwice()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markCompletedRatiosStory0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )

    composeRule.onNodeWithTag(PROMOTED_STORY_LIST_HEADER_TEST_TAG).onChildAt(0)
      .assertTextContains(context.getString(R.string.recommended_stories))
      .assertIsDisplayed()

    composeRule.onNodeWithTag(PROMOTED_STORY_LIST_TEST_TAG).onChildAt(0)
      .assertTextContains("What is a Fraction?")
      .assertTextContains("FRACTIONS")
      .assertTextContains("MATHS")
      .assertIsDisplayed()
  }

  @Test
  fun testFragment_noTopicProgress_initialRecommendationFractionsAndRatiosIsCorrect() {
    logIntoAdminTwice()

    composeRule.onNodeWithTag(PROMOTED_STORY_LIST_HEADER_TEST_TAG).onChildAt(0)
      .assertTextContains(context.getString(R.string.recommended_stories))
      .assertIsDisplayed()

    composeRule.onNodeWithTag(PROMOTED_STORY_LIST_TEST_TAG).apply {
      onChildAt(0)
        .assertTextContains("What is a Fraction?")
        .assertTextContains("FRACTIONS")
        .assertTextContains("MATHS")
        .assertIsDisplayed()

      onChildAt(1)
        .assertTextContains("What is a Ratio?")
        .assertTextContains("RATIOS AND PROPORTIONAL REASONING")
        .assertTextContains("MATHS")
        .assertIsDisplayed()
    }
  }

  @Test
  fun testFragment_forPromotedActivityList_hideViewAll() {
    logIntoAdminTwice()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )

    composeRule.onNodeWithTag(PROMOTED_STORY_LIST_HEADER_TEST_TAG).onChildAt(1)
      .assertDoesNotExist()
  }

  @Test
  fun testFragment_markStory0DoneForRatiosAndFirstTestTopic_displaysSuggestedStories() {
    logIntoAdminTwice()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markCompletedTestTopic0Story0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedRatiosStory0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )

    composeRule.onNodeWithTag(PROMOTED_STORY_LIST_HEADER_TEST_TAG).onChildAt(0)
      .assertTextContains(context.getString(R.string.recommended_stories))
      .assertIsDisplayed()

    composeRule.onNodeWithTag(PROMOTED_STORY_LIST_TEST_TAG).onChildAt(0)
      .assertTextContains("Fifth Exploration")
      .assertTextContains("SECOND TEST TOPIC")
      .assertTextContains("SCIENCE")
      .assertIsDisplayed()
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
  fun testFragment_markStory0DonePlayStory1FirstTestTopic_playFractionsTopic_orderIsCorrect() {
    logIntoAdminTwice()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markCompletedTestTopic0Story0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedTestTopic1Story0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )

    composeRule.onNodeWithTag(PROMOTED_STORY_LIST_HEADER_TEST_TAG).onChildAt(0)
      .assertTextContains(context.getString(R.string.stories_for_you))
      .assertIsDisplayed()

    composeRule.onNodeWithTag(PROMOTED_STORY_LIST_TEST_TAG).apply {
      onChildAt(0)
        .assertTextContains("Fifth Exploration")
        .assertTextContains("SECOND TEST TOPIC")
        .assertTextContains("SCIENCE")
        .assertIsDisplayed()

      onChildAt(1)
        .assertTextContains("What is a Fraction?")
        .assertTextContains("FRACTIONS")
        .assertTextContains("MATHS")
        .assertIsDisplayed()

      performScrollToIndex(2)
      onChildAt(1)
        .assertTextContains("What is a Ratio?")
        .assertTextContains("RATIOS AND PROPORTIONAL REASONING")
        .assertTextContains("MATHS")
        .assertIsDisplayed()
    }
  }

  @Test
  fun testFragment_markStory0DoneFirstTestTopic_suggestedStoriesIsCorrect() {
    logIntoAdminTwice()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markCompletedTestTopic0Story0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )

    composeRule.onNodeWithTag(PROMOTED_STORY_LIST_HEADER_TEST_TAG).onChildAt(0)
      .assertTextContains(context.getString(R.string.recommended_stories))
      .assertIsDisplayed()

    composeRule.onNodeWithTag(PROMOTED_STORY_LIST_TEST_TAG).onChildAt(0)
      .assertTextContains("What is a Ratio?")
      .assertTextContains("RATIOS AND PROPORTIONAL REASONING")
      .assertTextContains("MATHS")
      .assertIsDisplayed()
  }

  @Test
  fun testFragment_markStory0DoneForFractions_recommendedStoriesIsCorrect() {
    logIntoAdminTwice()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markCompletedFractionsStory0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )

    composeRule.onNodeWithTag(PROMOTED_STORY_LIST_HEADER_TEST_TAG).onChildAt(0)
      .assertTextContains(context.getString(R.string.recommended_stories))
      .assertIsDisplayed()

    composeRule.onNodeWithTag(PROMOTED_STORY_LIST_TEST_TAG).apply {
      onChildAt(0)
        .assertTextContains("Prototype Exploration")
        .assertTextContains("FIRST TEST TOPIC")
        .assertTextContains("SCIENCE")
        .assertIsDisplayed()

      onChildAt(1)
        .assertTextContains("What is a Ratio?")
        .assertTextContains("RATIOS AND PROPORTIONAL REASONING")
        .assertTextContains("MATHS")
        .assertIsDisplayed()
    }
  }

  @Test
  fun testFragment_clickViewAll_opensRecentlyPlayedActivity() {
    logIntoAdminTwice()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedTestTopic1(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )

    composeRule.onNodeWithTag(PROMOTED_STORY_LIST_HEADER_TEST_TAG).onChildAt(1)
      .assertIsDisplayed()
      .performClick()

    intended(hasComponent(RecentlyPlayedActivity::class.java.name))
  }

  @Test
  fun testFragment_markFullProgressForFractions_playRatios_displaysRecommendedStories() {
    logIntoAdminTwice()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedFractionsStory0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )

    composeRule.onNodeWithTag(PROMOTED_STORY_LIST_HEADER_TEST_TAG).onChildAt(0)
      .assertTextContains(context.getString(R.string.stories_for_you))
      .assertIsDisplayed()

    composeRule.onNodeWithTag(PROMOTED_STORY_LIST_TEST_TAG).apply {
      onChildAt(0)
        .assertTextContains("What is a Ratio?")
        .assertTextContains("RATIOS AND PROPORTIONAL REASONING")
        .assertTextContains("MATHS")
        .assertIsDisplayed()

      onChildAt(1)
        .assertTextContains("Prototype Exploration")
        .assertTextContains("FIRST TEST TOPIC")
        .assertTextContains("SCIENCE")
        .assertIsDisplayed()
    }
  }

  @Test
  fun testFragment_markAtLeastOneStoryCompletedForAllTopics_displaysComingSoonTopicsList() {
    logIntoAdminTwice()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markCompletedFractionsTopic(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedTestTopic0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedRatiosStory0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedTestTopic1(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )

    composeRule.onNodeWithTag(COMING_SOON_TOPIC_LIST_HEADER_TEST_TAG)
      .assertTextContains(context.getString(R.string.coming_soon))
      .assertIsDisplayed()

    composeRule.onNodeWithTag(COMING_SOON_TOPIC_LIST_TEST_TAG)
      .onChildAt(0)
      .onChildAt(1)
      .assertTextContains("Third Test Topic")
      .assertIsDisplayed()
  }

  @Test
  fun testFragment_markFullProgressForSecondTestTopic_displaysComingSoonTopicsText() {
    logIntoAdminTwice()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markCompletedTestTopic1(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )

    composeRule.onNodeWithTag(COMING_SOON_TOPIC_LIST_HEADER_TEST_TAG)
      .assertTextContains(context.getString(R.string.coming_soon))
      .assertIsDisplayed()

    composeRule.onNodeWithTag(COMING_SOON_TOPIC_LIST_TEST_TAG)
      .onChildAt(0)
      .onChildAt(1)
      .assertTextContains("Third Test Topic")
      .assertIsDisplayed()
  }

  @Test
  fun testFragment_markStory0OfRatiosAndTestTopics0And1Done_playTestTopicStory0_noPromotions() {
    logIntoAdminTwice()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markCompletedRatiosStory0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedTestTopic1(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedTestTopic0Story0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedTestTopic0Story0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )

    composeRule.onNodeWithTag(COMING_SOON_TOPIC_LIST_HEADER_TEST_TAG)
      .assertTextContains(context.getString(R.string.coming_soon))
      .assertIsDisplayed()

    composeRule.onNodeWithTag(COMING_SOON_TOPIC_LIST_TEST_TAG)
      .onChildAt(0)
      .onChildAt(1)
      .assertTextContains("Third Test Topic")
      .assertIsDisplayed()
  }

  @Test
  fun testFragment_clickPromotedStory_opensTopicActivity() {
    logIntoAdminTwice()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )

    composeRule.onNodeWithTag(PROMOTED_STORY_LIST_TEST_TAG).onChildAt(0)
      .assertIsDisplayed()
      .performClick()

    testCoroutineDispatchers.runCurrent()

    val args = TopicActivityParams.newBuilder().apply {
      this.classroomId = TEST_CLASSROOM_ID_1
      this.topicId = FRACTIONS_TOPIC_ID
      this.storyId = FRACTIONS_STORY_ID_0
    }.build()
    intended(hasComponent(TopicActivity::class.java.name))
    intended(hasProtoExtra(TopicActivity.TOPIC_ACTIVITY_PARAMS_KEY, args))
  }

  @Test
  fun testFragment_clickTopicSummary_opensTopicActivityThroughPlayIntent() {
    composeRule.onNodeWithTag(CLASSROOM_LIST_TEST_TAG).onChildAt(0).performClick()
    testCoroutineDispatchers.runCurrent()

    composeRule.onNodeWithTag(CLASSROOM_LIST_SCREEN_TEST_TAG).onChildAt(4)
      .assertTextContains("First Test Topic")
      .assertTextContains("3 Lessons")
      .assertIsDisplayed()
      .performClick()

    testCoroutineDispatchers.runCurrent()

    val args = TopicActivityParams.newBuilder().apply {
      this.classroomId = TEST_CLASSROOM_ID_0
      this.topicId = TEST_TOPIC_ID_0
      this.storyId = TEST_STORY_ID_0
    }.build()
    intended(hasComponent(TopicActivity::class.java.name))
    intended(hasProtoExtra(TopicActivity.TOPIC_ACTIVITY_PARAMS_KEY, args))
  }

  @Test
  fun testFragment_scrollToBottom_classroomListSticks_classroomListIsVisible() {
    composeRule.onNodeWithTag(CLASSROOM_LIST_SCREEN_TEST_TAG).performScrollToIndex(3)
    composeRule.onNodeWithTag(CLASSROOM_LIST_TEST_TAG).assertIsDisplayed()
  }

  @Test
  fun testFragment_scrollToBottom_classroomListCollapsesAndSticks_classroomListIsVisible() {
    composeRule.onNodeWithTag(
      CLASSROOM_CARD_ICON_TEST_TAG + "_Science",
      useUnmergedTree = true
    ).assertIsDisplayed()

    composeRule.onNodeWithTag(CLASSROOM_LIST_SCREEN_TEST_TAG).performScrollToIndex(3)

    composeRule.onNodeWithTag(
      CLASSROOM_CARD_ICON_TEST_TAG + "_Science",
      useUnmergedTree = true
    ).assertDoesNotExist()
  }

  @Test
  fun testFragment_switchClassroom_topicListUpdatesCorrectly() {
    // Click on Science classroom card.
    composeRule.onNodeWithTag(CLASSROOM_LIST_TEST_TAG).onChildAt(0).performClick()
    testCoroutineDispatchers.runCurrent()

    // Check that Science classroom's topics are displayed.
    composeRule.onNodeWithTag(CLASSROOM_LIST_SCREEN_TEST_TAG).onChildAt(4)
      .assertTextContains("First Test Topic")
      .assertTextContains("3 Lessons")
      .assertIsDisplayed()
    composeRule.onNodeWithTag(CLASSROOM_LIST_SCREEN_TEST_TAG).onChildAt(5)
      .assertTextContains("Second Test Topic")
      .assertTextContains("1 Lesson")
      .assertIsDisplayed()

    // Click on Maths classroom card.
    composeRule.onNodeWithTag(CLASSROOM_LIST_TEST_TAG).onChildAt(1).performClick()
    testCoroutineDispatchers.runCurrent()

    // Check that Maths classroom's topics are displayed.
    composeRule.onNodeWithTag(CLASSROOM_LIST_SCREEN_TEST_TAG).onChildAt(4)
      .assertTextContains("Fractions")
      .assertTextContains("2 Lessons")
      .assertIsDisplayed()
    composeRule.onNodeWithTag(CLASSROOM_LIST_SCREEN_TEST_TAG).onChildAt(5)
      .assertTextContains("Ratios and Proportional Reasoning")
      .assertTextContains("4 Lessons")
      .assertIsDisplayed()
  }

  @Test
  fun testFragment_clickOnTopicCard_returnBack_classroomSelectionIsRetained() {
    // Click on Maths classroom card.
    composeRule.onNodeWithTag(CLASSROOM_LIST_TEST_TAG).onChildAt(1).performClick()
    testCoroutineDispatchers.runCurrent()

    // Check that Fractions topic is displayed and perform click.
    composeRule.onNodeWithTag(CLASSROOM_LIST_SCREEN_TEST_TAG).onChildAt(4)
      .assertTextContains("Fractions")
      .assertTextContains("2 Lessons")
      .assertIsDisplayed()
      .performClick()

    pressBack()

    // Check that Maths classroom is selected & its topics are displayed.
    composeRule.onNodeWithTag(CLASSROOM_LIST_SCREEN_TEST_TAG).onChildAt(4)
      .assertTextContains("Fractions")
      .assertTextContains("2 Lessons")
      .assertIsDisplayed()
    composeRule.onNodeWithTag(CLASSROOM_LIST_SCREEN_TEST_TAG).onChildAt(5)
      .assertTextContains("Ratios and Proportional Reasoning")
      .assertTextContains("4 Lessons")
      .assertIsDisplayed()
  }

  @Test
  fun testFragment_switchClassrooms_topicListUpdatesCorrectly() {
    profileTestHelper.logIntoAdmin()
    testCoroutineDispatchers.runCurrent()

    // Click on Science classroom card.
    composeRule.onNodeWithTag(CLASSROOM_LIST_TEST_TAG).onChildAt(0).performClick()
    testCoroutineDispatchers.runCurrent()
    // Check that Science classroom's topics are displayed.
    composeRule.onNodeWithTag(CLASSROOM_LIST_SCREEN_TEST_TAG).onChildAt(4)
      .assertTextContains("First Test Topic")
      .assertTextContains("3 Lessons")
      .assertIsDisplayed()
    composeRule.onNodeWithTag(CLASSROOM_LIST_SCREEN_TEST_TAG).onChildAt(5)
      .assertTextContains("Second Test Topic")
      .assertTextContains("1 Lesson")
      .assertIsDisplayed()

    // Click on Maths classroom card.
    composeRule.onNodeWithTag(CLASSROOM_LIST_TEST_TAG).onChildAt(1).performClick()
    testCoroutineDispatchers.runCurrent()
    // Check that Maths classroom's topics are displayed.
    composeRule.onNodeWithTag(CLASSROOM_LIST_SCREEN_TEST_TAG).onChildAt(4)
      .assertTextContains("Fractions")
      .assertTextContains("2 Lessons")
      .assertIsDisplayed()
    composeRule.onNodeWithTag(CLASSROOM_LIST_SCREEN_TEST_TAG).onChildAt(5)
      .assertTextContains("Ratios and Proportional Reasoning")
      .assertTextContains("4 Lessons")
      .assertIsDisplayed()

    // Click on Science classroom card.
    composeRule.onNodeWithTag(CLASSROOM_LIST_TEST_TAG).onChildAt(0).performClick()
    testCoroutineDispatchers.runCurrent()
    // Check that Science classroom's topics are displayed.
    composeRule.onNodeWithTag(CLASSROOM_LIST_SCREEN_TEST_TAG).onChildAt(4)
      .assertTextContains("First Test Topic")
      .assertTextContains("3 Lessons")
      .assertIsDisplayed()
    composeRule.onNodeWithTag(CLASSROOM_LIST_SCREEN_TEST_TAG).onChildAt(5)
      .assertTextContains("Second Test Topic")
      .assertTextContains("1 Lesson")
      .assertIsDisplayed()
  }

  private fun logIntoAdmin() {
    dataProviderTestMonitor.waitForNextSuccessfulResult(profileTestHelper.logIntoAdmin())
  }

  private fun logIntoAdminTwice() {
    logIntoAdmin()
    logIntoAdmin()
  }

  private fun getOneOfLastThreeEventsLogged(
    wantedContext: EventLog.Context.ActivityContextCase
  ): EventLog {
    val events = fakeAnalyticsEventLogger.getMostRecentEvents(3)
    return when {
      events[0].context.activityContextCase == wantedContext -> events[0]
      events[1].context.activityContextCase == wantedContext -> events[1]
      else -> events[2]
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      TestPlatformParameterModule::class, RobolectricModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, ImageParsingModule::class,
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
      PlatformParameterSingletonModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, SplitScreenInteractionModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, MetricLogSchedulerModule::class, TestingBuildFlavorModule::class,
      EventLoggingConfigurationModule::class, ActivityRouterModule::class,
      CpuPerformanceSnapshotterModule::class, ExplorationProgressModule::class,
      TestAuthenticationModule::class, TestImageLoaderModule::class,
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(classroomListFragmentTest: ClassroomListFragmentTest)

    fun getAppStartupStateController(): AppStartupStateController

    fun getTestCoroutineDispatchers(): TestCoroutineDispatchers

    fun getProfileTestHelper(): ProfileTestHelper
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerClassroomListFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(classroomListFragmentTest: ClassroomListFragmentTest) {
      component.inject(classroomListFragmentTest)
    }

    public override fun attachBaseContext(base: Context?) {
      super.attachBaseContext(base)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
