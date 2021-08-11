package org.oppia.android.app.home

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
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
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.home.recentlyplayed.RecentlyPlayedActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.player.exploration.ExplorationActivity
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.hasGridItemCount
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.topic.PracticeTabModule
import org.oppia.android.app.utility.EspressoTestsMatchers.withDrawable
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
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.FRACTIONS_EXPLORATION_ID_0
import org.oppia.android.domain.topic.FRACTIONS_STORY_ID_0
import org.oppia.android.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.testing.AccessibilityTestRule
import org.oppia.android.testing.TestImageLoaderModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.story.StoryProgressTestHelper
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [RecentlyPlayedActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = RecentlyPlayedFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class RecentlyPlayedFragmentTest {
  @get:Rule
  val accessibilityTestRule = AccessibilityTestRule()

  @get:Rule
  val activityTestRule: ActivityTestRule<RecentlyPlayedActivity> = ActivityTestRule(
    RecentlyPlayedActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

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

  private fun createRecentlyPlayedActivityIntent(internalProfileId: Int): Intent {
    return RecentlyPlayedActivity.createRecentlyPlayedActivityIntent(
      context = context,
      internalProfileId = internalProfileId
    )
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
  fun testRecentlyPlayedTestActivity_clickStory_opensExplorationActivity() {
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

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
      PlatformParameterModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, TestImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class, PracticeTabModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

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
