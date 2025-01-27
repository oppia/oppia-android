package org.oppia.android.app.home

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.not
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
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.home.recentlyplayed.RecentlyPlayedActivity
import org.oppia.android.app.home.recentlyplayed.RecentlyPlayedFragment
import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.RecentlyPlayedActivityParams
import org.oppia.android.app.model.RecentlyPlayedActivityTitle
import org.oppia.android.app.model.ResumeLessonActivityParams
import org.oppia.android.app.model.ScreenName
import org.oppia.android.app.player.exploration.ExplorationActivity
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.hasGridItemCount
import org.oppia.android.app.resumelesson.ResumeLessonActivity
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.EspressoTestsMatchers.hasProtoExtra
import org.oppia.android.app.utility.EspressoTestsMatchers.withDrawable
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
import org.oppia.android.domain.exploration.testing.ExplorationStorageTestModule
import org.oppia.android.domain.exploration.testing.FakeExplorationRetriever
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
import org.oppia.android.domain.topic.FRACTIONS_EXPLORATION_ID_0
import org.oppia.android.domain.topic.FRACTIONS_STORY_ID_0
import org.oppia.android.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestImageLoaderModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.lightweightcheckpointing.ExplorationCheckpointTestHelper
import org.oppia.android.testing.lightweightcheckpointing.FRACTIONS_STORY_0_EXPLORATION_0_CURRENT_VERSION
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
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

private const val TEST_FRAGMENT_TAG = "recently_played_test_fragment"
private const val TOLERANCE = 1e-5f

/** Tests for [RecentlyPlayedActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = RecentlyPlayedFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class RecentlyPlayedFragmentTest {
  @get:Rule val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()
  @get:Rule val oppiaTestRule = OppiaTestRule()

  private val testFragment by lazy { RecentlyPlayedFragment() }

  @get:Rule
  val activityTestRule: ActivityTestRule<RecentlyPlayedActivity> = ActivityTestRule(
    RecentlyPlayedActivity::class.java,
    /* initialTouchMode= */ true,
    /* launchActivity= */ false
  )

  @Inject lateinit var profileTestHelper: ProfileTestHelper
  @Inject lateinit var storyProgressTestHelper: StoryProgressTestHelper
  @Inject lateinit var context: Context
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var fakeOppiaClock: FakeOppiaClock
  @Inject lateinit var explorationCheckpointTestHelper: ExplorationCheckpointTestHelper
  @Inject lateinit var fakeExplorationRetriever: FakeExplorationRetriever

  private val internalProfileId = 0

  private lateinit var profileId: ProfileId

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles()
    testCoroutineDispatchers.registerIdlingResource()
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

  private fun createRecentlyPlayedActivityIntent(
    internalProfileId: Int,
    recentlyPlayedActivityTitle: RecentlyPlayedActivityTitle =
      RecentlyPlayedActivityTitle.RECENTLY_PLAYED_STORIES
  ): Intent {
    val recentlyPlayedActivityParams =
      RecentlyPlayedActivityParams
        .newBuilder()
        .setProfileId(ProfileId.newBuilder().setInternalId(internalProfileId).build())
        .setActivityTitle(recentlyPlayedActivityTitle)
        .build()
    return RecentlyPlayedActivity.createRecentlyPlayedActivityIntent(
      context = context,
      recentlyPlayedActivityParams = recentlyPlayedActivityParams
    )
  }

  @Test
  fun testActivity_createIntent_verifyScreenNameInIntent() {
    val screenName = createRecentlyPlayedActivityIntent(internalProfileId)
      .extractCurrentAppScreenName()

    assertThat(screenName).isEqualTo(ScreenName.RECENTLY_PLAYED_ACTIVITY)
  }

  @Test
  fun testRecentlyPlayedTestActivity_clickOnToolbarNavigationButton_closeActivity() {
    activityTestRule.launchActivity(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    )
    onView(withContentDescription(R.string.navigate_up)).perform(click())
    assertThat(activityTestRule.activity.isFinishing).isTrue()
  }

  @Test
  fun testRecentlyPlayedTestAct_chapsPlayedEarlierThanAWeek_toolbarTitleIsDisplayed() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
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
  fun testRecentlyPlayedTestAct_chapsPlayedLaterThanAWeek_toolbarTitleIsDisplayed() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = true
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = true
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
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
  fun testRecentlyPlayedTestActivity_sectionDividerIsNotDisplayed() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = true
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_story_recycler_view,
          position = 0,
          targetViewId = R.id.divider_view
        )
      ).check(
        matches(not(isDisplayed()))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_lastWeekSectionTitleIsDisplayed() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = true
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_story_recycler_view,
          position = 0,
          targetViewId = R.id.section_title_text_view
        )
      ).check(
        matches(withText(R.string.ongoing_story_last_week))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_fractionsPlayed_storiesForYouToolbarTitleIsDisplayed() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId,
        RecentlyPlayedActivityTitle.STORIES_FOR_YOU
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          instanceOf(TextView::class.java),
          withParent(withId(R.id.recently_played_toolbar))
        )
      ).check(
        matches(withText(R.string.stories_for_you))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_configChange_showsRecommendedSectionTitle() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          2
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_story_recycler_view,
          position = 2,
          targetViewId = R.id.section_title_text_view
        )
      ).check(
        matches(withText(R.string.recommended_stories))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_showsRecommendedSectionTitle() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_story_recycler_view,
          position = 2,
          targetViewId = R.id.section_title_text_view
        )
      ).check(
        matches(withText(R.string.recommended_stories))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_recommendedSection_topicNameIsCorrect() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          3
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_story_recycler_view,
          position = 3,
          targetViewId = R.id.topic_name_text_view
        )
      ).check(
        matches(withText(containsString("Ratios and Proportional Reasoning")))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_disableClassrooms_recommendedSection_classroomNameIsNotDisplayed() { // ktlint-disable max-line-length
    TestPlatformParameterModule.forceEnableMultipleClassrooms(false)
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          3
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_story_recycler_view,
          position = 3,
          targetViewId = R.id.classroom_name_text_view
        )
      ).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_enableClassrooms_recommendedSection_classroomNameIsCorrect() {
    TestPlatformParameterModule.forceEnableMultipleClassrooms(true)
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          3
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_story_recycler_view,
          position = 3,
          targetViewId = R.id.classroom_name_text_view
        )
      ).check(
        matches(withText(containsString("MATHS")))
      )
    }
  }

  @Config(qualifiers = "port")
  @Test
  fun testRecentlyPlayedTestActivity_recentlyPlayedItemInRtl_rtlMarginIsCorrect() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    activityTestRule.launchActivity(
      createRecentlyPlayedActivityIntent(
        internalProfileId = profileId.internalId
      )
    )
    activityTestRule.activity.window.decorView.layoutDirection = ViewCompat.LAYOUT_DIRECTION_RTL
    testCoroutineDispatchers.runCurrent()
    val recycler: RecyclerView =
      activityTestRule.activity.findViewById(R.id.ongoing_story_recycler_view)

    assertThat(recycler.getChildAt(1).marginStart.toFloat())
      .isWithin(TOLERANCE)
      .of(28f)
    assertThat(recycler.getChildAt(1).marginEnd.toFloat())
      .isWithin(TOLERANCE)
      .of(8f)

    assertThat(recycler.getChildAt(2).marginStart.toFloat())
      .isWithin(TOLERANCE)
      .of(8f)
    assertThat(recycler.getChildAt(2).marginEnd.toFloat())
      .isWithin(TOLERANCE)
      .of(28f)
  }

  @Config(qualifiers = "land")
  @Test
  fun testRecentlyPlayedTestActivity_recentlyPlayedItemInRtl_landscape_rtlMarginIsCorrect() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedTestTopic0Story0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    activityTestRule.launchActivity(
      createRecentlyPlayedActivityIntent(
        internalProfileId = profileId.internalId
      )
    )
    activityTestRule.activity.window.decorView.layoutDirection = ViewCompat.LAYOUT_DIRECTION_RTL
    testCoroutineDispatchers.runCurrent()
    val recycler: RecyclerView =
      activityTestRule.activity.findViewById(R.id.ongoing_story_recycler_view)

    assertThat(recycler.getChildAt(1).marginStart.toFloat())
      .isWithin(TOLERANCE)
      .of(72f)
    assertThat(recycler.getChildAt(1).marginEnd.toFloat())
      .isWithin(TOLERANCE)
      .of(0f)

    assertThat(recycler.getChildAt(2).marginStart.toFloat())
      .isWithin(TOLERANCE)
      .of(36f)
    assertThat(recycler.getChildAt(2).marginEnd.toFloat())
      .isWithin(TOLERANCE)
      .of(36f)

    assertThat(recycler.getChildAt(3).marginStart.toFloat())
      .isWithin(TOLERANCE)
      .of(0f)
    assertThat(recycler.getChildAt(3).marginEnd.toFloat())
      .isWithin(TOLERANCE)
      .of(72f)
  }

  @Config(qualifiers = "sw600dp-port")
  @Test
  fun testRecentlyPlayedTestActivity_recentlyPlayedItemInRtl_tabletPortrait_rtlMarginIsCorrect() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedTestTopic0Story0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    activityTestRule.launchActivity(
      createRecentlyPlayedActivityIntent(
        internalProfileId = profileId.internalId
      )
    )
    activityTestRule.activity.window.decorView.layoutDirection = ViewCompat.LAYOUT_DIRECTION_RTL
    testCoroutineDispatchers.runCurrent()
    val recycler: RecyclerView =
      activityTestRule.activity.findViewById(R.id.ongoing_story_recycler_view)

    assertThat(recycler.getChildAt(1).marginStart.toFloat())
      .isWithin(TOLERANCE)
      .of(120f)
    assertThat(recycler.getChildAt(1).marginEnd.toFloat())
      .isWithin(TOLERANCE)
      .of(0f)

    assertThat(recycler.getChildAt(2).marginStart.toFloat())
      .isWithin(TOLERANCE)
      .of(60f)
    assertThat(recycler.getChildAt(2).marginEnd.toFloat())
      .isWithin(TOLERANCE)
      .of(60f)

    assertThat(recycler.getChildAt(3).marginStart.toFloat())
      .isWithin(TOLERANCE)
      .of(0f)
    assertThat(recycler.getChildAt(3).marginEnd.toFloat())
      .isWithin(TOLERANCE)
      .of(120f)
  }

  @Config(qualifiers = "sw600dp-land")
  @Test
  fun testRecentlyPlayedTestActivity_recentlyPlayedItemInRtl_tabletLandscape_rtlMarginIsCorrect() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedTestTopic0Story0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )

    storyProgressTestHelper.markInProgressSavedTestTopic1Story0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )

    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    activityTestRule.launchActivity(
      createRecentlyPlayedActivityIntent(
        internalProfileId = profileId.internalId
      )
    )
    activityTestRule.activity.window.decorView.layoutDirection = ViewCompat.LAYOUT_DIRECTION_RTL
    testCoroutineDispatchers.runCurrent()
    val recycler: RecyclerView =
      activityTestRule.activity.findViewById(R.id.ongoing_story_recycler_view)

    assertThat(recycler.getChildAt(1).marginStart.toFloat())
      .isWithin(TOLERANCE)
      .of(96f)
    assertThat(recycler.getChildAt(1).marginEnd.toFloat())
      .isWithin(TOLERANCE)
      .of(0f)

    assertThat(recycler.getChildAt(2).marginStart.toFloat())
      .isWithin(TOLERANCE)
      .of(64f)
    assertThat(recycler.getChildAt(2).marginEnd.toFloat())
      .isWithin(TOLERANCE)
      .of(32f)

    assertThat(recycler.getChildAt(3).marginStart.toFloat())
      .isWithin(TOLERANCE)
      .of(32f)
    assertThat(recycler.getChildAt(3).marginEnd.toFloat())
      .isWithin(TOLERANCE)
      .of(64f)

    assertThat(recycler.getChildAt(4).marginStart.toFloat())
      .isWithin(TOLERANCE)
      .of(0f)
    assertThat(recycler.getChildAt(4).marginEnd.toFloat())
      .isWithin(TOLERANCE)
      .of(96f)
  }

  @Config(qualifiers = "port")
  @Test
  fun testRecentlyPlayedTestActivity_recentlyPlayedItemInLtr_ltrMarginIsCorrect() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = true
    )
    activityTestRule.launchActivity(
      createRecentlyPlayedActivityIntent(
        internalProfileId = profileId.internalId
      )
    )
    testCoroutineDispatchers.runCurrent()
    val recycler: RecyclerView =
      activityTestRule.activity.findViewById(R.id.ongoing_story_recycler_view)

    assertThat(recycler.getChildAt(1).marginStart.toFloat())
      .isWithin(TOLERANCE)
      .of(28f)
    assertThat(recycler.getChildAt(1).marginEnd.toFloat())
      .isWithin(TOLERANCE)
      .of(8f)
  }

  @Config(qualifiers = "port")
  @Test
  fun testRecentlyPlayedTestActivity_recommendedSectionItemInRtlMode_rtlMarginIsCorrect() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )

    activityTestRule.launchActivity(
      createRecentlyPlayedActivityIntent(
        internalProfileId = profileId.internalId
      )
    )
    activityTestRule.activity.window.decorView.layoutDirection = ViewCompat.LAYOUT_DIRECTION_RTL
    testCoroutineDispatchers.runCurrent()
    val recyclerView: RecyclerView =
      activityTestRule.activity.findViewById(R.id.ongoing_story_recycler_view)
    assertThat(recyclerView.getChildAt(1).marginStart.toFloat())
      .isWithin(TOLERANCE)
      .of(28f)
    assertThat(recyclerView.getChildAt(1).marginEnd.toFloat())
      .isWithin(TOLERANCE)
      .of(8f)
  }

  @Test
  fun testRecentlyPlayedTestActivity_storyNameIsCorrect() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = true
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_story_recycler_view,
          position = 1,
          targetViewId = R.id.story_name_text_view
        )
      ).check(
        matches(withText(containsString("Matthew Goes to the Bakery")))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_topicNameIsCorrect() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = true
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_story_recycler_view,
          position = 1,
          targetViewId = R.id.topic_name_text_view
        )
      ).check(
        matches(withText(containsString("FRACTIONS")))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_disableClassrooms_classroomNameIsNotDisplayed() {
    TestPlatformParameterModule.forceEnableMultipleClassrooms(false)
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = true
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_story_recycler_view,
          position = 1,
          targetViewId = R.id.classroom_name_text_view
        )
      ).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_enableClassrooms_classroomNameIsCorrect() {
    TestPlatformParameterModule.forceEnableMultipleClassrooms(true)
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = true
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_story_recycler_view,
          position = 1,
          targetViewId = R.id.classroom_name_text_view
        )
      ).check(
        matches(withText(containsString("MATHS")))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_lessonThumbnailIsCorrect() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = true
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_story_recycler_view,
          position = 1,
          targetViewId = R.id.lesson_thumbnail
        )
      ).check(
        matches(withDrawable(R.drawable.lesson_thumbnail_graphic_duck_and_chicken))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_clickStory_correctCheckpointSaved_opensResumeLessonActivity() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration0(
      profileId,
      FRACTIONS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_story_recycler_view,
          position = 1,
          targetViewId = R.id.lesson_thumbnail
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()

      val expectedParams = ResumeLessonActivityParams.newBuilder().apply {
        explorationId = FRACTIONS_EXPLORATION_ID_0
        storyId = FRACTIONS_STORY_ID_0
        topicId = FRACTIONS_TOPIC_ID
        classroomId = TEST_CLASSROOM_ID_1
        profileId = ProfileId.newBuilder().apply { internalId = internalProfileId }.build()
        parentScreen = ExplorationActivityParams.ParentScreen.PARENT_SCREEN_UNSPECIFIED
        checkpoint = ExplorationCheckpoint.newBuilder().apply {
          explorationTitle = "What is a Fraction?"
          explorationVersion = 85
          pendingStateName = "Introduction"
          timestampOfFirstCheckpoint = 102
        }.build()
      }.build()
      intended(
        allOf(
          hasProtoExtra("ResumeLessonActivity.params", expectedParams),
          hasComponent(ResumeLessonActivity::class.java.name)
        )
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestAct_clickStory_incompatibleCheckpointSaved_opensExplorationLessonAct() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    fakeExplorationRetriever.setExplorationProxy(
      expIdToLoad = FRACTIONS_EXPLORATION_ID_0,
      expIdToLoadInstead = "test_checkpointing_exploration_multiple_updates_one_incompatible"
    )
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration0(
      profileId = profileId,
      version = 1 // Old version, but it doesn't matter since the new version is incompatible.
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_story_recycler_view,
          position = 1,
          targetViewId = R.id.lesson_thumbnail
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()

      val expectedParams = ExplorationActivityParams.newBuilder().apply {
        explorationId = FRACTIONS_EXPLORATION_ID_0
        storyId = FRACTIONS_STORY_ID_0
        topicId = FRACTIONS_TOPIC_ID
        classroomId = TEST_CLASSROOM_ID_1
        profileId = ProfileId.newBuilder().apply { internalId = internalProfileId }.build()
        isCheckpointingEnabled = true
        parentScreen = ExplorationActivityParams.ParentScreen.PARENT_SCREEN_UNSPECIFIED
      }.build()
      intended(
        allOf(
          hasProtoExtra("ExplorationActivity.params", expectedParams),
          hasComponent(ExplorationActivity::class.java.name)
        )
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestAct_clickStory_chapterAsNotStarted_opensExplorationLessonActivity() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_story_recycler_view,
          position = 1,
          targetViewId = R.id.lesson_thumbnail
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()

      val expectedParams = ExplorationActivityParams.newBuilder().apply {
        explorationId = FRACTIONS_EXPLORATION_ID_0
        storyId = FRACTIONS_STORY_ID_0
        topicId = FRACTIONS_TOPIC_ID
        classroomId = TEST_CLASSROOM_ID_1
        profileId = ProfileId.newBuilder().apply { internalId = internalProfileId }.build()
        isCheckpointingEnabled = true
        parentScreen = ExplorationActivityParams.ParentScreen.PARENT_SCREEN_UNSPECIFIED
      }.build()
      intended(
        allOf(
          hasProtoExtra("ExplorationActivity.params", expectedParams),
          hasComponent(ExplorationActivity::class.java.name)
        )
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestAct_clickStory_chapterMarkedAsInProgNotSaved_opensExplorationLessAct() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressNotSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_story_recycler_view,
          position = 1,
          targetViewId = R.id.lesson_thumbnail
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()

      val expectedParams = ExplorationActivityParams.newBuilder().apply {
        explorationId = FRACTIONS_EXPLORATION_ID_0
        storyId = FRACTIONS_STORY_ID_0
        topicId = FRACTIONS_TOPIC_ID
        classroomId = TEST_CLASSROOM_ID_1
        profileId = ProfileId.newBuilder().apply { internalId = internalProfileId }.build()
        isCheckpointingEnabled = true
        parentScreen = ExplorationActivityParams.ParentScreen.PARENT_SCREEN_UNSPECIFIED
      }.build()
      intended(
        allOf(
          hasProtoExtra("ExplorationActivity.params", expectedParams),
          hasComponent(ExplorationActivity::class.java.name)
        )
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_lastMonthSectionTitleIsDisplayed() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = true
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          2
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_story_recycler_view,
          position = 2,
          targetViewId = R.id.section_title_text_view
        )
      ).check(
        matches(withText(R.string.ongoing_story_last_month))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_sectionDividerIsDisplayed() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = true
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          2
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_story_recycler_view,
          position = 2,
          targetViewId = R.id.divider_view
        )
      ).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testRecentlyPlayedTestAct_chapsPlayedEarlierThanAWeek_configChange_toolbarTitleIsDisplayed() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(instanceOf(TextView::class.java), withParent(withId(R.id.recently_played_toolbar)))
      ).check(
        matches(withText(R.string.recently_played_activity))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestAct_chapsPlayedLaterThanAWeek_configChange_toolbarTitleIsDisplayed() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = true
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = true
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(instanceOf(TextView::class.java), withParent(withId(R.id.recently_played_toolbar)))
      ).check(
        matches(withText(R.string.recently_played_activity))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_configChange_sectionDividerIsNotDisplayed() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = true
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_story_recycler_view,
          position = 0,
          targetViewId = R.id.divider_view
        )
      ).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_configChange_lastWeekSectionTitleIsDisplayed() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = true
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_story_recycler_view,
          position = 0,
          targetViewId = R.id.section_title_text_view
        )
      ).check(
        matches(withText(R.string.ongoing_story_last_week))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_configChange_storyNameIsCorrect() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = true
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_story_recycler_view,
          position = 1,
          targetViewId = R.id.story_name_text_view
        )
      ).check(
        matches(withText(containsString("Matthew Goes to the Bakery")))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_configChange_topicNameIsCorrect() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = true
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_story_recycler_view,
          position = 1,
          targetViewId = R.id.topic_name_text_view
        )
      ).check(
        matches(withText(containsString("FRACTIONS")))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_disableClassrooms_configChange_classroomNameIsNotDisplayed() {
    TestPlatformParameterModule.forceEnableMultipleClassrooms(false)
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = true
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_story_recycler_view,
          position = 1,
          targetViewId = R.id.classroom_name_text_view
        )
      ).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_enableClassrooms_configChange_classroomNameIsCorrect() {
    TestPlatformParameterModule.forceEnableMultipleClassrooms(true)
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = true
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_story_recycler_view,
          position = 1,
          targetViewId = R.id.classroom_name_text_view
        )
      ).check(
        matches(withText(containsString("MATHS")))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_configChange_lessonThumbnailIsCorrect() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = true
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_story_recycler_view,
          position = 1,
          targetViewId = R.id.lesson_thumbnail
        )
      ).check(
        matches(withDrawable(R.drawable.lesson_thumbnail_graphic_duck_and_chicken))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_configChange_lastMonthSectionTitleIsDisplayed() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = true
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          2
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_story_recycler_view,
          position = 2,
          targetViewId = R.id.section_title_text_view
        )
      ).check(
        matches(withText(R.string.ongoing_story_last_month))
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_checkSpanForItem1_spanSizeIsOne() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = true
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(withId(R.id.ongoing_story_recycler_view)).check(
        hasGridItemCount(
          spanCount = 1,
          position = 1
        )
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_checkSpanForItem3_spanSizeIsOne() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = true
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          3
        )
      )
      onView(withId(R.id.ongoing_story_recycler_view)).check(
        hasGridItemCount(
          spanCount = 1,
          position = 3
        )
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_configChange_checkSpanForItem1_spanSizeIsOne() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = true
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(withId(R.id.ongoing_story_recycler_view)).check(
        hasGridItemCount(
          spanCount = 1,
          position = 1
        )
      )
    }
  }

  @Test
  fun testRecentlyPlayedTestActivity_configChange_checkSpanForItem3_spanSizeIsOne() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = true
    )
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.ongoing_story_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          3
        )
      )
      onView(withId(R.id.ongoing_story_recycler_view)).check(
        hasGridItemCount(
          spanCount = 1,
          position = 3
        )
      )
    }
  }

  @Test
  fun testFragment_argumentsAreCorrect() {
    ActivityScenario.launch<RecentlyPlayedActivity>(
      createRecentlyPlayedActivityIntent(
        internalProfileId = internalProfileId,
        RecentlyPlayedActivityTitle.STORIES_FOR_YOU
      )
    ).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity ->

        val recentlyPlayedFragment = activity.supportFragmentManager
          .findFragmentById(R.id.recently_played_fragment_placeholder) as RecentlyPlayedFragment

        val arguments = checkNotNull(recentlyPlayedFragment.arguments) {
          "Expected arguments to be passed to RecentlyPlayedFragment"
        }
        val profileId = arguments.extractCurrentUserProfileId()
        val receivedInternalProfileId = profileId.internalId

        assertThat(receivedInternalProfileId).isEqualTo(internalProfileId)
      }
    }
  }

  private fun setUpTestFragment(activity: RecentlyPlayedActivity) {
    activity.supportFragmentManager
      .beginTransaction()
      .add(testFragment, TEST_FRAGMENT_TAG)
      .commitNow()
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
      GcsResourceModule::class, TestImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class, WorkManagerConfigurationModule::class,
      ApplicationStartupListenerModule::class, LogReportWorkerModule::class,
      HintsAndSolutionConfigModule::class, HintsAndSolutionProdModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageTestModule::class, NetworkModule::class, NetworkConfigProdModule::class,
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

    fun inject(recentlyPlayedFragmentTest: RecentlyPlayedFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerRecentlyPlayedFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(recentlyPlayedFragmentTest: RecentlyPlayedFragmentTest) {
      component.inject(recentlyPlayedFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
