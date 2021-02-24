package org.oppia.android.app.home

import android.app.Application
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Component
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsNot.not
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
import org.oppia.android.app.home.recentlyplayed.RecentlyPlayedActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.profile.ProfileChooserActivity
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.hasGridColumnCount
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.hasGridItemCount
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.hasItemCount
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
import org.oppia.android.domain.topic.FRACTIONS_STORY_ID_0
import org.oppia.android.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import org.oppia.android.testing.RobolectricModule
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestAccessibilityModule
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.TestPlatform
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.testing.story.StoryProgressTestHelper
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.parser.GlideImageLoaderModule
import org.oppia.android.util.parser.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.ImageParsingModule
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

/** Tests for [HomeActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = HomeActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class HomeActivityTest {

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

  private val internalProfileId: Int = 0
  private val internalProfileId1: Int = 1
  private val longNameInternalProfileId: Int = 3
  private lateinit var profileId: ProfileId
  private lateinit var profileId1: ProfileId

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    profileId1 = ProfileId.newBuilder().setInternalId(internalProfileId1).build()
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
  fun testHomeActivity_withAdminProfile_profileNameIsDisplayed() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      verifyExactTextOnHomeListItemAtPosition(
        itemPosition = 0,
        targetViewId = R.id.profile_name_textview,
        stringToMatch = "Admin!"
      )
    }
  }

  @Test
  fun testHomeActivity_withAdminProfile_configChange_profileNameIsDisplayed() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 0)
      verifyExactTextOnHomeListItemAtPosition(
        itemPosition = 0,
        targetViewId = R.id.profile_name_textview,
        stringToMatch = "Admin!"
      )
    }
  }

  @Test
  fun testHomeActivity_morningTimestamp_goodMorningMessageIsDisplayed() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    fakeOppiaClock.setCurrentTimeToSameDateTime(MORNING_TIMESTAMP)
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      verifyExactTextOnHomeListItemAtPosition(
        itemPosition = 0,
        targetViewId = R.id.welcome_text_view,
        stringToMatch = "Good morning,"
      )
    }
  }

  @Test
  fun testHomeActivity_afternoonTimestamp_goodAfternoonMessageIsDisplayed() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    fakeOppiaClock.setCurrentTimeToSameDateTime(AFTERNOON_TIMESTAMP)
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      verifyExactTextOnHomeListItemAtPosition(
        itemPosition = 0,
        targetViewId = R.id.welcome_text_view,
        stringToMatch = "Good afternoon,"
      )
    }
  }

  @Test
  fun testHomeActivity_eveningTimestamp_goodEveningMessageIsDisplayed() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    fakeOppiaClock.setCurrentTimeToSameDateTime(EVENING_TIMESTAMP)
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      verifyExactTextOnHomeListItemAtPosition(
        itemPosition = 0,
        targetViewId = R.id.welcome_text_view,
        stringToMatch = "Good evening,"
      )
    }
  }

  @Test
  fun testHomeActivity_recentlyPlayedStoriesTextIsDisplayed() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markRecentlyPlayedFractionsStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markRecentlyPlayedRatiosStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
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
    storyProgressTestHelper.markRecentlyPlayedFractionsStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markRecentlyPlayedRatiosStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = true
    )
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
    storyProgressTestHelper.markRecentlyPlayedFractionsStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = true
    )
    testCoroutineDispatchers.runCurrent()
    storyProgressTestHelper.markRecentlyPlayedRatiosStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = true
    )
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
    storyProgressTestHelper.markRecentlyPlayedFractionsStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
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
   * prerequisites is completed out-of-order (e.g. test topic 1 above) then we assume fractions is already done.
   * In the same way, finishing test topic 2 means there's nothing else to recommend.
   */
  @Test
  fun testHomeActivity_markStory0DonePlayStory1FirstTestTopic_playFractionsTopic_orderIsCorrect() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markCompletedTestTopic0Story0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markRecentlyPlayedFractionsStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markRecentlyPlayedTestTopic0Story1Exp1(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      verifyExactTextOnHomeListItemAtPosition(
        itemPosition = 1,
        targetViewId = R.id.recently_played_stories_text_view,
        stringToMatch = context.getString(R.string.stories_for_you)
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
      scrollToPositionOfPromotedList(position = 2)
      verifyTextOnPromotedListItemAtPosition(
        itemPosition = 2,
        targetViewId = R.id.topic_name_text_view,
        stringToMatch = "First Test Topic"
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
    storyProgressTestHelper.markRecentlyPlayedTestTopic0Story0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
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
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markRecentlyPlayedFractionsStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markRecentlyPlayedRatiosStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markRecentlyPlayedTestTopic1(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
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
    storyProgressTestHelper.markRecentlyPlayedFractionsStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
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
    storyProgressTestHelper.markRecentlyPlayedFractionsStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
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
    storyProgressTestHelper.markRecentlyPlayedFractionsStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markRecentlyPlayedRatiosStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = true
    )
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
    storyProgressTestHelper.markRecentlyPlayedRatiosStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedFractionsStory0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
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
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markRecentlyPlayedFractionsStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
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
      intended(hasComponent(TopicActivity::class.java.name))
      intended(hasExtra(TopicActivity.getProfileIdKey(), internalProfileId1))
      intended(hasExtra(TopicActivity.getTopicIdKey(), FRACTIONS_TOPIC_ID))
      intended(hasExtra(TopicActivity.getStoryIdKey(), FRACTIONS_STORY_ID_0))
    }
  }

  @Test
  fun testHomeActivity_promotedCard_topicNameIsCorrect() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markRecentlyPlayedFractionsStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markRecentlyPlayedRatiosStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = true
    )
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
  fun testHomeActivity_firstTestTopic_topicSummary_topicNameIsCorrect() {
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
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 3)
      verifyTextOnHomeListItemAtPosition(
        itemPosition = 3,
        targetViewId = R.id.lesson_count_text_view,
        stringToMatch = "5 Lessons"
      )
    }
  }

  @Test
  fun testHomeActivity_secondTestTopic_topicSummary_allTopics_topicNameIsCorrect() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markRecentlyPlayedFractionsStory0Exp0(
      profileId = profileId1,
      timestampOlderThanOneWeek = false
    )
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
  @Config(qualifiers = "port-mdpi")
  @Test
  fun testHomeActivity_longProfileName_welcomeMessageIsDisplayed() {
    launch<HomeActivity>(createHomeActivityIntent(longNameInternalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      onView(
        atPositionOnView(
          R.id.home_recycler_view,
          0,
          R.id.profile_name_textview
        )
      ).check(matches(not(isEllipsized())))
    }
  }

  // TODO(#2057): Remove when TextViews are properly measured in Robolectric.
  @RunOn(TestPlatform.ESPRESSO) // Incorrectly passes on Robolectric and shouldn't be re-enabled
  @Config(qualifiers = "land-mdpi")
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
          R.id.profile_name_textview
        )
      ).check(matches(not(isEllipsized())))
    }
  }

  // TODO(#2057): Remove when TextViews are properly measured in Robolectric.
  @RunOn(TestPlatform.ESPRESSO) // Incorrectly passes on Robolectric and shouldn't be re-enabled
  @Config(qualifiers = "sw600dp-port")
  @Test
  fun testHomeActivity_longProfileName_tabletPortraitWelcomeMessageIsDisplayed() {
    launch<HomeActivity>(createHomeActivityIntent(longNameInternalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(0)
      onView(
        atPositionOnView(
          R.id.home_recycler_view,
          0,
          R.id.profile_name_textview
        )
      ).check(matches(not(isEllipsized())))
    }
  }

  // TODO(#2057): Remove when TextViews are properly measured in Robolectric.
  @RunOn(TestPlatform.ESPRESSO) // Incorrectly passes on Robolectric and shouldn't be re-enabled
  @Config(qualifiers = "sw600dp-land")
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
          R.id.profile_name_textview
        )
      ).check(matches(not(isEllipsized())))
    }
  }

  @Test
  fun testHomeActivity_oneLesson_topicSummary_configChange_lessonCountIsCorrect() {
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
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 3)
      onView(atPosition(R.id.home_recycler_view, 3)).perform(click())
      intended(hasComponent(TopicActivity::class.java.name))
      intended(hasExtra(TopicActivity.getTopicIdKey(), TEST_TOPIC_ID_0))
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
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 2)
      verifyExactTextOnHomeListItemAtPosition(
        itemPosition = 2,
        targetViewId = R.id.all_topics_text_view,
        stringToMatch = context.getString((R.string.all_topics))
      )
    }
  }

  @Test
  fun testHomeActivity_allTopicsCompleted_displaysAllTopicCards() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markAllTopicsAsCompleted(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 3)
      if (context.resources.getBoolean(R.bool.isTablet)) {
        verifyHomeRecyclerViewHasGridColumnCount(columnCount = 3)
      } else {
        verifyHomeRecyclerViewHasGridColumnCount(columnCount = 2)
      }
    }
  }

  @Test
  fun testHomeActivity_noTopicsCompleted_displaysAllTopicsHeader() {
    // Only new users will have no progress for any topics.
    profileTestHelper.logIntoNewUser()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 2)
      verifyExactTextOnHomeListItemAtPosition(
        itemPosition = 2,
        targetViewId = R.id.all_topics_text_view,
        stringToMatch = context.getString((R.string.all_topics))
      )
    }
  }

  @Config(qualifiers = "port")
  @Test
  fun testHomeActivity_noTopicsStarted_mobilePortraitDisplaysTopicsIn2Columns() {
    // Only new users will have no progress for any topics.
    profileTestHelper.logIntoNewUser()
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

  @Config(qualifiers = "land")
  @Test
  fun testHomeActivity_noTopicsStarted_mobileLandscapeDisplaysTopicsIn3Columns() {
    // Only new users will have no progress for any topics.
    profileTestHelper.logIntoNewUser()
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

  @Config(qualifiers = "sw600dp-port")
  @Test
  fun testHomeActivity_noTopicsStarted_tabletPortraitDisplaysTopicsIn3Columns() {
    // Only new users will have no progress for any topics.
    profileTestHelper.logIntoNewUser()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      verifyHomeRecyclerViewHasGridColumnCount(columnCount = 3)

      scrollToPosition(position = 3)
      onView(withId(R.id.home_recycler_view))
        .check(hasGridItemCount(spanCount = 1, position = 3))
    }
  }

  @Config(qualifiers = "sw600dp-land")
  @Test
  fun testHomeActivity_noTopicsStarted_tabletLandscapeDisplaysTopicsIn4Columns() {
    // Only new users will have no progress for any topics.
    profileTestHelper.logIntoNewUser()
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
    storyProgressTestHelper.markRecentlyPlayedTestTopic0Story0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markRecentlyPlayedTestTopic1Story2Exp0(
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

  @Config(qualifiers = "sw600dp-port")
  @Test
  fun testHomeActivity_multipleRecentlyPlayedStories_tabletPortraitShows3PromotedStories() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markRecentlyPlayedTestTopic0Story0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markRecentlyPlayedTestTopic1Story2Exp0(
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

  @Config(qualifiers = "sw600dp-land")
  @Test
  fun testHomeActivity_multipleRecentlyPlayedStories_tabletLandscapeShows4PromotedStories() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markRecentlyPlayedTestTopic0Story0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markRecentlyPlayedTestTopic1Story2Exp0(
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
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 6)

      scrollToPosition(position = 0)
      onView(withId(R.id.promoted_story_list_recycler_view))
        .check(hasItemCount(count = 3))
    }
  }

  private fun createHomeActivityIntent(profileId: Int): Intent {
    return HomeActivity.createHomeActivity(context, profileId)
  }

  // Refrence - https://stackoverflow.com/a/61455336/12215015
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
    return ProfileId.newBuilder().setInternalId(internalProfileId).build()
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
// TODO(#1675): Add NetworkModule once data module is migrated off of Moshi.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      TestAccessibilityModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

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
}
