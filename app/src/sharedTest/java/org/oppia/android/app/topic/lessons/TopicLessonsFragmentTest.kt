package org.oppia.android.app.topic.lessons

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.hasContentDescription
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.viewpager2.widget.ViewPager2
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
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
import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ResumeLessonActivityParams
import org.oppia.android.app.model.Spotlight.FeatureCase.FIRST_CHAPTER
import org.oppia.android.app.model.Spotlight.FeatureCase.TOPIC_LESSON_TAB
import org.oppia.android.app.model.Spotlight.FeatureCase.TOPIC_REVISION_TAB
import org.oppia.android.app.model.StoryActivityParams
import org.oppia.android.app.model.TopicLessonsFragmentArguments
import org.oppia.android.app.player.exploration.ExplorationActivity
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.resumelesson.ResumeLessonActivity
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.story.StoryActivity
import org.oppia.android.app.story.StoryActivity.Companion.STORY_ACTIVITY_PARAMS_KEY
import org.oppia.android.app.topic.TopicActivity
import org.oppia.android.app.topic.TopicFragment
import org.oppia.android.app.topic.TopicTab
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
import org.oppia.android.domain.spotlight.SpotlightStateController
import org.oppia.android.domain.topic.FRACTIONS_EXPLORATION_ID_0
import org.oppia.android.domain.topic.FRACTIONS_STORY_ID_0
import org.oppia.android.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.android.domain.topic.RATIOS_STORY_ID_0
import org.oppia.android.domain.topic.RATIOS_TOPIC_ID
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.lightweightcheckpointing.ExplorationCheckpointTestHelper
import org.oppia.android.testing.lightweightcheckpointing.FRACTIONS_STORY_0_EXPLORATION_0_CURRENT_VERSION
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.story.StoryProgressTestHelper
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.accessibility.FakeAccessibilityService
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.oppia.android.util.platformparameter.EnableExtraTopicTabsUi
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [TopicLessonsFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = TopicLessonsFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class TopicLessonsFragmentTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var storyProgressTestHelper: StoryProgressTestHelper

  @Inject
  lateinit var fakeOppiaClock: FakeOppiaClock

  @Inject
  lateinit var fakeAccessibilityService: FakeAccessibilityService

  @Inject
  lateinit var spotlightStateController: SpotlightStateController

  @Inject
  lateinit var explorationCheckpointTestHelper: ExplorationCheckpointTestHelper

  @Inject
  lateinit var fakeExplorationRetriever: FakeExplorationRetriever

  @field:[Inject EnableExtraTopicTabsUi]
  lateinit var enableExtraTopicTabsUiValue: PlatformParameterValue<Boolean>

  private lateinit var profileId: ProfileId

  @Before
  fun setUp() {
    TestPlatformParameterModule.forceEnableExtraTopicTabsUi(true)
    Intents.init()
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
    profileId = ProfileId.newBuilder().setInternalId(0).build()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    markAllSpotlightsSeen()
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
  fun testLessonsPlayFragment_loadRatiosTopic_storyName_isCorrect() {
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      verifyTextOnStorySummaryListItemAtPosition(itemPosition = 1, stringToMatch = "Ratios: Part 1")
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_chapterCountTextMultiple_isCorrect() {
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      verifyTextOnStorySummaryListItemAtPosition(itemPosition = 2, stringToMatch = "2 Chapters")
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_completeStoryProgress_isDisplayed() {
    storyProgressTestHelper.markCompletedRatiosStory0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      verifyTextOnStorySummaryListItemAtPosition(itemPosition = 1, stringToMatch = "100%")
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_completeStoryProgress_contentDescriptionIsCorrect() {
    storyProgressTestHelper.markCompletedRatiosStory0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      verifyProgressContentDescriptionAtPosition(itemPosition = 1, stringToMatch = "100% Completed")
    }
  }

  @Test
  fun testLessonsFragment_ratiosTopic_partialStoryProgressInExp0_contentDescriptionIsCorrect() {
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      verifyProgressContentDescriptionAtPosition(itemPosition = 1, stringToMatch = "0% In Progress")
    }
  }

  @Test
  fun testLessonsFragment_loadRatiosTopic_partialStoryProgressInExp1_contentDescriptionIsCorrect() {
    storyProgressTestHelper.markCompletedRatiosStory0Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp1(
      profileId,
      timestampOlderThanOneWeek = false
    )
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      verifyProgressContentDescriptionAtPosition(
        itemPosition = 1,
        stringToMatch = "50% In Progress"
      )
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_noStoryProgress_contentDescriptionIsCorrect() {
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      verifyProgressContentDescriptionAtPosition(itemPosition = 1, stringToMatch = "0% In Progress")
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_partialStoryProgress_isDisplayed() {
    storyProgressTestHelper.markCompletedRatiosStory0Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedRatiosStory1Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      verifyTextOnStorySummaryListItemAtPosition(itemPosition = 2, stringToMatch = "50%")
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_configurationChange_storyName_isCorrect() {
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      onView(isRoot()).perform(orientationLandscape())
      verifyTextOnStorySummaryListItemAtPosition(itemPosition = 1, stringToMatch = "Ratios: Part 1")
    }
  }

  @Test
  @Config(qualifiers = "land-xxhdpi")
  fun testLessonsPlayFragment_loadRatiosTopic_configurationLandscape_storyName_isCorrect() {
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      verifyTextOnStorySummaryListItemAtPosition(itemPosition = 1, stringToMatch = "Ratios: Part 1")
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_clickStoryItem_opensStoryActivityWithCorrectIntent() {
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      clickStoryItem(position = 1, targetViewId = R.id.story_name_text_view)

      val args = StoryActivityParams.newBuilder().apply {
        this.storyId = RATIOS_STORY_ID_0
        this.topicId = RATIOS_TOPIC_ID
        this.classroomId = TEST_CLASSROOM_ID_1
      }.build()
      intended(hasComponent(StoryActivity::class.java.name))
      intended(hasProtoExtra(STORY_ACTIVITY_PARAMS_KEY, args))
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_chapterListIsNotVisible() {
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID
      )
    ).use {
      onView(withId(R.id.chapter_recycler_view)).check(doesNotExist())
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_chapterNotStartedIsCorrectlyDisplayed() {
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.chapter_recycler_view,
          position = 0,
          targetViewId = R.id.not_started_chapter_container
        )
      ).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_chapterLockedIsCorrectlyDisplayed() {
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.chapter_recycler_view,
          position = 1,
          targetViewId = R.id.locked_chapter_container
        )
      ).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_chapterIsLocked_contentDescriptionIsCorrect() {
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.chapter_recycler_view,
          position = 1,
          targetViewId = R.id.chapter_play_state_icon
        )
      ).check(matches(isDisplayed()))
        .check(matches(hasContentDescription()))
        .check(
          matches(
            withContentDescription(
              "Chapter 2: Order is important is currently locked. Please complete chapter 1: " +
                "What is a Ratio? to unlock this chapter."
            )
          )
        )
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_chapterCompletedIsCorrectlyDisplayed() {
    storyProgressTestHelper.markCompletedRatiosStory0Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.chapter_recycler_view,
          position = 0,
          targetViewId = R.id.lessons_completed_chapter_view
        )
      ).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_chapterCompleted_contentDescriptionIsCorrect() {
    storyProgressTestHelper.markCompletedRatiosStory0Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.chapter_recycler_view,
          position = 0,
          targetViewId = R.id.chapter_index
        )
      ).check(matches(isDisplayed()))
        .check(matches(hasContentDescription()))
        .check(
          matches(
            withContentDescription(
              "Chapter 1 with title What is a Ratio? is completed"
            )
          )
        )
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_chapterInProgressIsCorrectlyDisplayed() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.chapter_recycler_view,
          position = 0,
          targetViewId = R.id.lessons_in_progress_chapter_container
        )
      ).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_chapterInProgress_contentDescriptionIsCorrect() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.chapter_recycler_view,
          position = 0,
          targetViewId = R.id.chapter_play_state_icon
        )
      ).check(matches(isDisplayed()))
        .check(matches(hasContentDescription()))
        .check(
          matches(
            withContentDescription(
              "Chapter 1 with title What is a Ratio? is in progress"
            )
          )
        )
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_configChange_chapterLockedIsCorrectlyDisplayed() {
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      onView(isRoot()).perform(orientationLandscape())
      onView(
        atPositionOnView(
          recyclerViewId = R.id.chapter_recycler_view,
          position = 1,
          targetViewId = R.id.locked_chapter_container
        )
      ).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_configChange_chapterCompletedIsCorrectlyDisplayed() {
    storyProgressTestHelper.markCompletedRatiosStory0Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      onView(isRoot()).perform(orientationLandscape())
      onView(
        atPositionOnView(
          recyclerViewId = R.id.chapter_recycler_view,
          position = 0,
          targetViewId = R.id.lessons_completed_chapter_view
        )
      ).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_configChange_chapterInProgressIsCorrectlyDisplayed() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      onView(isRoot()).perform(orientationLandscape())
      onView(
        atPositionOnView(
          recyclerViewId = R.id.chapter_recycler_view,
          position = 0,
          targetViewId = R.id.lessons_in_progress_chapter_container
        )
      ).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_configChange_chapterNotStartedIsCorrectlyDisplayed() {
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      onView(isRoot()).perform(orientationLandscape())
      onView(
        atPositionOnView(
          recyclerViewId = R.id.chapter_recycler_view,
          position = 0,
          targetViewId = R.id.not_started_chapter_container
        )
      ).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_default_arrowDown() {
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_summary_recycler_view,
          position = 1,
          targetViewId = R.id.chapter_list_drop_down_icon
        )
      ).check(
        matches(
          withDrawable(R.drawable.ic_arrow_drop_down_black_24dp)
        )
      )
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_clickExpandListIcon_chapterListIsVisible() {
    launch<TopicActivity>(
      createTopicPlayStoryActivityIntent(
        profileId,
        TEST_CLASSROOM_ID_1,
        RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0
      )
    ).use {
      clickLessonTab()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_summary_recycler_view,
          position = 1,
          targetViewId = R.id.chapter_recycler_view
        )
      ).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testLessPlayFrag_loadFractionsTopic_clickChap_correctCheckpointSaved_opensResumeLessonAct() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration0(
      profileId = profileId,
      version = FRACTIONS_STORY_0_EXPLORATION_0_CURRENT_VERSION,
    )
    testCoroutineDispatchers.runCurrent()
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      scrollToPosition(position = 1)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_summary_recycler_view,
          position = 1,
          targetViewId = R.id.chapter_recycler_view
        )
      ).check(matches(hasDescendant(withId(R.id.lessons_in_progress_chapter_container))))
        .perform(click())
      testCoroutineDispatchers.runCurrent()

      val expectedParams = ResumeLessonActivityParams.newBuilder().apply {
        explorationId = FRACTIONS_EXPLORATION_ID_0
        storyId = FRACTIONS_STORY_ID_0
        topicId = FRACTIONS_TOPIC_ID
        classroomId = TEST_CLASSROOM_ID_1
        profileId = profileId
        parentScreen = ExplorationActivityParams.ParentScreen.TOPIC_SCREEN_LESSONS_TAB
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
  fun testLessPlayFrag_loadFractionsTopic_clickChap_outdatedCheckpointSaved_opensExplorationAct() {
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
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      scrollToPosition(position = 1)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_summary_recycler_view,
          position = 1,
          targetViewId = R.id.chapter_recycler_view
        )
      ).check(matches(hasDescendant(withId(R.id.lessons_in_progress_chapter_container))))
        .perform(click())
      testCoroutineDispatchers.runCurrent()

      val expectedParams = ExplorationActivityParams.newBuilder().apply {
        explorationId = FRACTIONS_EXPLORATION_ID_0
        storyId = FRACTIONS_STORY_ID_0
        topicId = FRACTIONS_TOPIC_ID
        classroomId = TEST_CLASSROOM_ID_1
        profileId = profileId
        isCheckpointingEnabled = true
        parentScreen = ExplorationActivityParams.ParentScreen.TOPIC_SCREEN_LESSONS_TAB
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
  fun testLessonsPlayFragment_loadFractionsTopic_clickChap_chapterMarkedAsNotStarted_opensExpAct() {
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      scrollToPosition(position = 1)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_summary_recycler_view,
          position = 1,
          targetViewId = R.id.chapter_recycler_view
        )
      ).check(matches(hasDescendant(withId(R.id.not_started_chapter_container))))
      onView(withId(R.id.not_started_chapter_container)).perform(click())
      testCoroutineDispatchers.runCurrent()

      val expectedParams = ExplorationActivityParams.newBuilder().apply {
        explorationId = FRACTIONS_EXPLORATION_ID_0
        storyId = FRACTIONS_STORY_ID_0
        topicId = FRACTIONS_TOPIC_ID
        classroomId = TEST_CLASSROOM_ID_1
        profileId = profileId
        isCheckpointingEnabled = true
        parentScreen = ExplorationActivityParams.ParentScreen.TOPIC_SCREEN_LESSONS_TAB
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
  fun testLessPlayFrag_loadFractionsTopic_clickChap_chapterMarkedInProgressNotSaved_opensExpAct() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markInProgressNotSavedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      scrollToPosition(position = 1)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_summary_recycler_view,
          position = 1,
          targetViewId = R.id.chapter_recycler_view
        )
      ).check(matches(hasDescendant(withId(R.id.lessons_in_progress_chapter_container))))
        .perform(click())
      testCoroutineDispatchers.runCurrent()

      val expectedParams = ExplorationActivityParams.newBuilder().apply {
        explorationId = FRACTIONS_EXPLORATION_ID_0
        storyId = FRACTIONS_STORY_ID_0
        topicId = FRACTIONS_TOPIC_ID
        classroomId = TEST_CLASSROOM_ID_1
        profileId = profileId
        isCheckpointingEnabled = true
        parentScreen = ExplorationActivityParams.ParentScreen.TOPIC_SCREEN_LESSONS_TAB
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
  fun testLessonsPlayFrag_loadFractionsTopic_clickChapter_chapterMarkedAsCompleted_opensExpAct() {
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markCompletedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      scrollToPosition(position = 1)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_summary_recycler_view,
          position = 1,
          targetViewId = R.id.chapter_recycler_view
        )
      ).check(matches(hasDescendant(withId(R.id.lessons_completed_chapter_view)))).perform(click())
      testCoroutineDispatchers.runCurrent()

      val expectedParams = ExplorationActivityParams.newBuilder().apply {
        explorationId = FRACTIONS_EXPLORATION_ID_0
        storyId = FRACTIONS_STORY_ID_0
        topicId = FRACTIONS_TOPIC_ID
        classroomId = TEST_CLASSROOM_ID_1
        profileId = profileId
        isCheckpointingEnabled = false
        parentScreen = ExplorationActivityParams.ParentScreen.TOPIC_SCREEN_LESSONS_TAB
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
  fun testLessonsPlayFragment_loadRatiosTopic_clickExpandListIconIndex1_clickExpandListIconIndex2_chapterListForIndex1IsNotDisplayed() { // ktlint-disable max-line-length
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      scrollToPosition(position = 1)
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      onView(withId(R.id.story_summary_recycler_view)).perform(
        actionOnItemAtPosition<RecyclerView.ViewHolder>(2, scrollTo())
      )
      clickStoryItem(position = 2, targetViewId = R.id.chapter_list_drop_down_icon)
      scrollToPosition(position = 1)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_summary_recycler_view,
          position = 1,
          targetViewId = R.id.chapter_recycler_view
        )
      ).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_clickExpandListIconIndex2_clickExpandListIconIndex1_chapterListForIndex2IsNotDisplayed() { // ktlint-disable max-line-length
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      scrollToPosition(position = 2)
      clickStoryItem(position = 2, targetViewId = R.id.chapter_list_drop_down_icon)
      scrollToPosition(position = 1)
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      scrollToPosition(position = 2)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_summary_recycler_view,
          position = 2,
          targetViewId = R.id.chapter_recycler_view
        )
      ).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_clickExpandListIconIndex1_configurationChange_chapterListIsVisible() { // ktlint-disable max-line-length
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      onView(isRoot()).perform(orientationLandscape())
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      scrollToPosition(position = 1)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_summary_recycler_view,
          position = 1,
          targetViewId = R.id.chapter_recycler_view
        )
      ).check(matches(isDisplayed()))
    }
  }

  @Test
  @Config(qualifiers = "land-xxhdpi")
  fun testLessonsPlayFragment_loadRatiosTopic_clickExpandListIconIndex1_configurationLandscape_chapterListIsVisible() { // ktlint-disable max-line-length
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      scrollToPosition(position = 1)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_summary_recycler_view,
          position = 1,
          targetViewId = R.id.chapter_recycler_view
        )
      ).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testLessonPlayFrag_loadRatiosTopic_partialProg_partialProgIconIsDisplayed() {
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      scrollToPosition(position = 1)
      verifyChapterPlayStateIconIsVisibleAtPosition(itemPosition = 0)
      verifyPartialProgressIconIsDisplayedAtPosition(itemPosition = 0)
    }
  }

  @Test
  fun testLessonPlayFrag_loadRatiosTopic_partialProg_configChange_partialProgIconIsDisplayed() {
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      scrollToPosition(position = 1)
      onView(isRoot()).perform(orientationLandscape())
      verifyChapterPlayStateIconIsVisibleAtPosition(itemPosition = 0)
      verifyPartialProgressIconIsDisplayedAtPosition(itemPosition = 0)
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRecentStory_default_chapterListIsVisible() {
    launch<TopicActivity>(
      createTopicPlayStoryActivityIntent(
        profileId,
        TEST_CLASSROOM_ID_1,
        RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0
      )
    ).use {
      scrollToPosition(position = 1)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_summary_recycler_view,
          position = 1,
          targetViewId = R.id.chapter_recycler_view
        )
      ).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRecentStory_clickExpandIcon_chapterListIsNotVisible() {
    launch<TopicActivity>(
      createTopicPlayStoryActivityIntent(
        profileId,
        TEST_CLASSROOM_ID_1,
        RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0
      )
    ).use {
      scrollToPosition(position = 1)
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_summary_recycler_view,
          position = 1,
          targetViewId = R.id.chapter_recycler_view
        )
      ).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRecentStory_clickExpandIcon_land_chapterListIsNotVisible() {
    launch<TopicActivity>(
      createTopicPlayStoryActivityIntent(
        profileId,
        TEST_CLASSROOM_ID_1,
        RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0
      )
    ).use {
      scrollToPosition(position = 1)
      clickStoryItem(position = 1, targetViewId = R.id.chapter_list_drop_down_icon)
      orientationLandscape()
      scrollToPosition(position = 1)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_summary_recycler_view,
          position = 1,
          targetViewId = R.id.chapter_recycler_view
        )
      ).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testLessonsPlayFragment_loadRatiosTopic_checkDropDownIconWithScreenReader_isClickable() {
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID
      )
    ).use {
      fakeAccessibilityService.setScreenReaderEnabled(true)
      clickLessonTab()
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_summary_recycler_view,
          position = 1,
          targetViewId = R.id.expand_list_icon
        )
      ).check(
        matches(isClickable())
      )
    }
  }

  @Test
  fun testLessonPlayFragment_loadRatiosTopic_checkDropDownIconWithoutScreenReader_isNotClickable() {
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_summary_recycler_view,
          position = 1,
          targetViewId = R.id.expand_list_icon
        )
      ).check(
        matches(not(isClickable()))
      )
    }
  }

  @Test
  fun testLessonPlayFragment_loadRatiosTopic_checkStoryContainerWithScreenReader_isNotClickable() {
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID
      )
    ).use {
      fakeAccessibilityService.setScreenReaderEnabled(true)
      clickLessonTab()
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_summary_recycler_view,
          position = 1,
          targetViewId = R.id.story_container
        )
      ).check(
        matches(not(isClickable()))
      )
    }
  }

  @Test
  fun testLessonPlayFragment_loadRatiosTopic_checkStoryContainerWithoutScreenReader_isClickable() {
    launch<TopicActivity>(
      createTopicActivityIntent(
        profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID
      )
    ).use {
      clickLessonTab()
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_summary_recycler_view,
          position = 1,
          targetViewId = R.id.story_container
        )
      ).check(
        matches(isClickable())
      )
    }
  }

  @Test
  fun testFragment_argumentsAreCorrect() {
    launch<TopicActivity>(
      createTopicPlayStoryActivityIntent(
        profileId,
        TEST_CLASSROOM_ID_1,
        RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0
      )
    ).use { scenario ->
      clickLessonTab()
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity ->

        val topicFragment = activity.supportFragmentManager
          .findFragmentById(R.id.topic_fragment_placeholder) as TopicFragment
        val viewPager = topicFragment.requireView()
          .findViewById<ViewPager2>(R.id.topic_tabs_viewpager)
        val topicLessonsFragment = topicFragment.childFragmentManager
          .findFragmentByTag("f${viewPager.currentItem}") as TopicLessonsFragment

        val receivedInternalProfileId = topicLessonsFragment
          .arguments?.extractCurrentUserProfileId()?.internalId ?: -1
        val args = topicLessonsFragment.arguments?.getProto(
          TopicLessonsFragment.TOPIC_LESSONS_FRAGMENT_ARGUMENTS_KEY,
          TopicLessonsFragmentArguments.getDefaultInstance()
        )
        val receivedClassroomId = checkNotNull(args?.classroomId) {
          "Expected classroom ID to be included in arguments for TopicLessonsFragment."
        }
        val receivedTopicId = checkNotNull(args?.topicId) {
          "Expected topic ID to be included in arguments for TopicLessonsFragment."
        }
        val receivedStoryId = args?.storyId ?: ""

        assertThat(receivedInternalProfileId).isEqualTo(profileId.internalId)
        assertThat(receivedClassroomId).isEqualTo(TEST_CLASSROOM_ID_1)
        assertThat(receivedTopicId).isEqualTo(RATIOS_TOPIC_ID)
        assertThat(receivedStoryId).isEqualTo(RATIOS_STORY_ID_0)
      }
    }
  }

  @Test
  fun testTopicLessonsFragment_saveInstanceState_verifyCorrectStateRestored() {
    launch<TopicActivity>(
      createTopicPlayStoryActivityIntent(
        profileId,
        TEST_CLASSROOM_ID_1,
        RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0
      )
    ).use { scenario ->
      clickLessonTab()
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(position = 2)
      clickStoryItem(position = 2, targetViewId = R.id.chapter_list_drop_down_icon)

      scenario.recreate()

      scrollToPosition(position = 2)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_summary_recycler_view,
          position = 2,
          targetViewId = R.id.chapter_recycler_view
        )
      ).check(matches(isDisplayed()))
    }
  }

  private fun markAllSpotlightsSeen() {
    spotlightStateController.markSpotlightViewed(profileId, TOPIC_LESSON_TAB)
    testCoroutineDispatchers.runCurrent()
    spotlightStateController.markSpotlightViewed(profileId, TOPIC_REVISION_TAB)
    testCoroutineDispatchers.runCurrent()
    spotlightStateController.markSpotlightViewed(profileId, FIRST_CHAPTER)
    testCoroutineDispatchers.runCurrent()
  }

  private fun createTopicActivityIntent(
    profileId: ProfileId,
    classroomId: String,
    topicId: String
  ): Intent {
    return TopicActivity.createTopicActivityIntent(
      ApplicationProvider.getApplicationContext(),
      profileId,
      classroomId,
      topicId
    )
  }

  private fun createTopicPlayStoryActivityIntent(
    profileId: ProfileId,
    classroomId: String,
    topicId: String,
    storyId: String
  ): Intent {
    return TopicActivity.createTopicPlayStoryActivityIntent(
      ApplicationProvider.getApplicationContext(),
      profileId,
      classroomId,
      topicId,
      storyId
    )
  }

  private fun clickLessonTab() {
    testCoroutineDispatchers.runCurrent()
    onView(
      allOf(
        withText(TopicTab.getTabForPosition(position = 1, enableExtraTopicTabsUiValue.value).name),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun clickStoryItem(position: Int, targetViewId: Int) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.story_summary_recycler_view,
        position = position,
        targetViewId = targetViewId
      )
    ).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun scrollToPosition(position: Int) {
    onView(withId(R.id.story_summary_recycler_view)).perform(
      scrollToPosition<RecyclerView.ViewHolder>(
        position
      )
    )
    testCoroutineDispatchers.runCurrent()
  }

  private fun verifyTextOnStorySummaryListItemAtPosition(itemPosition: Int, stringToMatch: String) {
    onView(
      atPosition(
        recyclerViewId = R.id.story_summary_recycler_view,
        position = itemPosition
      )
    ).check(matches(hasDescendant(withText(containsString(stringToMatch)))))
  }

  private fun verifyProgressContentDescriptionAtPosition(itemPosition: Int, stringToMatch: String) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.story_summary_recycler_view,
        position = itemPosition,
        targetViewId = R.id.story_progress_linear_layout
      )
    ).check(matches(withContentDescription(stringToMatch)))
  }

  private fun verifyChapterPlayStateIconIsVisibleAtPosition(itemPosition: Int) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.chapter_recycler_view,
        position = itemPosition,
        targetViewId = R.id.chapter_play_state_icon
      )
    ).check(matches(isDisplayed()))
  }

  private fun verifyChapterPlayStateIconIsNotVisibleAtPosition(itemPosition: Int) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.chapter_recycler_view,
        position = itemPosition,
        targetViewId = R.id.chapter_play_state_icon
      )
    ).check(matches(not(isDisplayed())))
  }

  private fun verifyPartialProgressIconIsDisplayedAtPosition(itemPosition: Int) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.chapter_recycler_view,
        position = itemPosition,
        targetViewId = R.id.chapter_play_state_icon
      )
    ).check(matches(withDrawable(R.drawable.ic_pending_24dp)))
  }

  private fun verifyChapterCompletedIconIsDisplayedAtPosition(itemPosition: Int) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.chapter_recycler_view,
        position = itemPosition,
        targetViewId = R.id.chapter_play_state_icon
      )
    ).check(matches(withDrawable(R.drawable.ic_check_24dp)))
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class, TestPlatformParameterModule::class,
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
      ExplorationStorageTestModule::class, NetworkModule::class, NetworkConfigProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      PlatformParameterSingletonModule::class,
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

    fun inject(topicLessonsFragmentTest: TopicLessonsFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerTopicLessonsFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(topicLessonsFragmentTest: TopicLessonsFragmentTest) {
      component.inject(topicLessonsFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
