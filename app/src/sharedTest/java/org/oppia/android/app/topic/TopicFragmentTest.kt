package org.oppia.android.app.topic

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
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
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Spotlight
import org.oppia.android.app.model.Spotlight.FeatureCase.FIRST_CHAPTER
import org.oppia.android.app.model.Spotlight.FeatureCase.TOPIC_LESSON_TAB
import org.oppia.android.app.model.Spotlight.FeatureCase.TOPIC_REVISION_TAB
import org.oppia.android.app.model.TopicFragmentArguments
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.spotlight.SpotlightFragment
import org.oppia.android.app.spotlight.SpotlightManager.Companion.SPOTLIGHT_FRAGMENT_TAG
import org.oppia.android.app.test.R
import org.oppia.android.app.testing.activity.TestActivity
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.EspressoTestsMatchers.matchCurrentTabTitle
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.android.data.backends.gae.NetworkConfigProdModule
import org.oppia.android.data.backends.gae.RetrofitModule
import org.oppia.android.data.backends.gae.RetrofitServiceModule
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
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.spotlight.SpotlightStateController
import org.oppia.android.domain.topic.FRACTIONS_STORY_ID_0
import org.oppia.android.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.android.domain.topic.RATIOS_STORY_ID_0
import org.oppia.android.domain.topic.RATIOS_TOPIC_ID
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.FakeAnalyticsEventLogger
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.logging.EventLogSubject.Companion.assertThat
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
import org.oppia.android.util.extensions.putProto
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
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

private const val INFO_TAB_POSITION = 0
private const val LESSON_TAB_POSITION = 1
private const val PRACTICE_TAB_POSITION = 2
private const val REVISION_TAB_POSITION = 3
private const val LESSON_TAB_POSITION_EXTRA_TABS_DISABLED = 0
private const val REVISION_TAB_POSITION_EXTRA_TABS_DISABLED = 1

/** Tests for [TopicFragment]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = TopicFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class TopicFragmentTest {
  @get:Rule val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()
  @get:Rule val oppiaTestRule = OppiaTestRule()

  @Inject lateinit var context: Context
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var spotlightStateController: SpotlightStateController
  @Inject lateinit var fakeOppiaClock: FakeOppiaClock
  @Inject lateinit var storyProgressTestHelper: StoryProgressTestHelper
  @Inject lateinit var fakeAnalyticsEventLogger: FakeAnalyticsEventLogger
  @Inject lateinit var fakeAccessibilityService: FakeAccessibilityService

  @field:[Inject EnableExtraTopicTabsUi]
  lateinit var enableExtraTopicTabsUi: PlatformParameterValue<Boolean>

  private val profileId = ProfileId.newBuilder().setInternalId(0).build()
  private val TOPIC_NAME = "Fractions"

  @Before
  fun setUp() {
    Intents.init()
    TestPlatformParameterModule.reset()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  @Test
  fun testTopicFragment_toolbarTitle_isDisplayedSuccessfully() {
    initializeApplicationComponent(enableExtraTabsUi = false)
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.topic_toolbar_title)).check(matches(withText("Topic: Fractions")))
    }
  }

  @Test
  fun testLessonsTabSpotlight_spotlightAlreadySeen_checkSpotlightNotShown() {
    initializeApplicationComponent(false)
    markSpotlightSeen(FIRST_CHAPTER)
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      // Mark lessons spotlight seen.
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.close_spotlight_button)).perform(click())
    }

    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      testCoroutineDispatchers.runCurrent()
      onView(withText(R.string.topic_lessons_tab_spotlight_hint)).check(doesNotExist())
    }
  }

  @Test
  fun testTopicLessonTabSpotlight_spotlightNotSeenBefore_checkSpotlightIsShown() {
    initializeApplicationComponent(false)
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      testCoroutineDispatchers.runCurrent()
      onView(withText(R.string.topic_lessons_tab_spotlight_hint)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testFirstChapterSpotlight_setToShowOnFirstLogin_checkSpotlightShown() {
    initializeApplicationComponent(false)
    markSpotlightSeen(TOPIC_LESSON_TAB)
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      testCoroutineDispatchers.runCurrent()
      onView(withText(R.string.first_chapter_spotlight_hint)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testFirstChapterSpotlight_setToShowOnFirstLogin_alreadySeen_checkSpotlightNotShown() {
    initializeApplicationComponent(false)
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      // Mark first chapter spotlight seen.
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.close_spotlight_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.close_spotlight_button)).perform(click())
    }

    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      testCoroutineDispatchers.runCurrent()
      onView(withText(R.string.first_chapter_spotlight_hint)).check(doesNotExist())
    }
  }

  @Test
  fun testRevisionTabSpotlight_setToShowAfterAtleast3ChaptersCompleted_notSeenBefore_checkShown() {
    initializeApplicationComponent(false)
    markSpotlightSeen(FIRST_CHAPTER)
    markSpotlightSeen(TOPIC_LESSON_TAB)
    storyProgressTestHelper.markCompletedFractionsStory0Exp0(profileId, false)
    storyProgressTestHelper.markCompletedRatiosStory0Exp0(profileId, false)
    storyProgressTestHelper.markCompletedRatiosStory1Exp0(profileId, false)
    testCoroutineDispatchers.runCurrent()
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID, RATIOS_STORY_ID_0
    ) {
      testCoroutineDispatchers.runCurrent()
      onView(withText(R.string.topic_revision_tab_spotlight_hint)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testRevisionTabSpotlight_setToShowAfterAtleast3ChaptersCompleted_notComplete_checkNotShown() {
    initializeApplicationComponent(false)
    markSpotlightSeen(TOPIC_LESSON_TAB)
    markSpotlightSeen(FIRST_CHAPTER)
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID, RATIOS_STORY_ID_0
    ) {
      testCoroutineDispatchers.runCurrent()
      onView(withText(R.string.topic_revision_tab_spotlight_hint)).check(doesNotExist())
    }
  }

  @Test
  fun testRevisionTabSpotlight_setToShowAfterAtleast3ChaptersCompleted_alreadySeen_checkNotShown() {
    initializeApplicationComponent(false)
    markSpotlightSeen(TOPIC_LESSON_TAB)
    markSpotlightSeen(FIRST_CHAPTER)
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProgressTestHelper.markCompletedFractionsStory0Exp0(profileId, false)
    storyProgressTestHelper.markCompletedRatiosStory0Exp0(profileId, false)
    storyProgressTestHelper.markCompletedRatiosStory1Exp0(profileId, false)
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID, RATIOS_STORY_ID_0
    ) {
      testCoroutineDispatchers.runCurrent()
      // Mark revision tab spotlight seen.
      onView(withId(R.id.close_spotlight_button)).perform(click())
    }

    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, RATIOS_TOPIC_ID, RATIOS_STORY_ID_0
    ) {
      testCoroutineDispatchers.runCurrent()
      onView(withText(R.string.topic_revision_tab_spotlight_hint)).check(doesNotExist())
    }
  }

  @Test
  fun testTopicFragment_toolbarTitle_readerOff_marqueeInRtl_isDisplayedCorrectly() {
    initializeApplicationComponent(false)
    markSpotlightSeen(TOPIC_LESSON_TAB)
    fakeAccessibilityService.setScreenReaderEnabled(false)
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, storyId = null
    ) {
      testCoroutineDispatchers.runCurrent()
      onActivity { activity ->
        val topicToolbarTitle: TextView = activity.findViewById(R.id.topic_toolbar_title)
        ViewCompat.setLayoutDirection(topicToolbarTitle, ViewCompat.LAYOUT_DIRECTION_RTL)

        onView(withId(R.id.topic_toolbar_title)).perform(click())
        assertThat(topicToolbarTitle.ellipsize).isEqualTo(TextUtils.TruncateAt.MARQUEE)
        assertThat(topicToolbarTitle.isSelected).isTrue()
        assertThat(topicToolbarTitle.textAlignment).isEqualTo(View.TEXT_ALIGNMENT_VIEW_START)
      }
    }
  }

  @Test
  fun testTopicFragment_toolbarTitle_readerOn_marqueeInRtl_isDisplayedCorrectly() {
    initializeApplicationComponent(false)
    markSpotlightSeen(TOPIC_LESSON_TAB)
    fakeAccessibilityService.setScreenReaderEnabled(true)
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      testCoroutineDispatchers.runCurrent()
      onActivity { activity ->
        val topicToolbarTitle: TextView = activity.findViewById(R.id.topic_toolbar_title)
        ViewCompat.setLayoutDirection(topicToolbarTitle, ViewCompat.LAYOUT_DIRECTION_RTL)

        onView(withId(R.id.topic_toolbar_title)).perform(click())
        assertThat(topicToolbarTitle.ellipsize).isEqualTo(TextUtils.TruncateAt.MARQUEE)
        assertThat(topicToolbarTitle.isSelected).isFalse()
        assertThat(topicToolbarTitle.textAlignment).isEqualTo(View.TEXT_ALIGNMENT_VIEW_START)
      }
    }
  }

  @Test
  fun testTopicFragment_toolbarTitle_readerOff_marqueeInLtr_isDisplayedCorrectly() {
    initializeApplicationComponent(false)
    markSpotlightSeen(TOPIC_LESSON_TAB)
    fakeAccessibilityService.setScreenReaderEnabled(false)
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, storyId = null
    ) {
      testCoroutineDispatchers.runCurrent()
      onActivity { activity ->
        val topicToolbarTitle: TextView = activity.findViewById(R.id.topic_toolbar_title)
        ViewCompat.setLayoutDirection(topicToolbarTitle, ViewCompat.LAYOUT_DIRECTION_LTR)
        onView(withId(R.id.topic_toolbar_title)).perform(click())
        assertThat(topicToolbarTitle.ellipsize).isEqualTo(TextUtils.TruncateAt.MARQUEE)
        assertThat(topicToolbarTitle.isSelected).isTrue()
        assertThat(topicToolbarTitle.textAlignment).isEqualTo(View.TEXT_ALIGNMENT_VIEW_START)
      }
    }
  }

  @Test
  fun testTopicFragment_toolbarTitle_readerOn_marqueeInLtr_isDisplayedCorrectly() {
    initializeApplicationComponent(false)
    markSpotlightSeen(TOPIC_LESSON_TAB)
    fakeAccessibilityService.setScreenReaderEnabled(true)
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      testCoroutineDispatchers.runCurrent()
      onActivity { activity ->
        val topicToolbarTitle: TextView = activity.findViewById(R.id.topic_toolbar_title)
        ViewCompat.setLayoutDirection(topicToolbarTitle, ViewCompat.LAYOUT_DIRECTION_LTR)
        onView(withId(R.id.topic_toolbar_title)).perform(click())
        assertThat(topicToolbarTitle.ellipsize).isEqualTo(TextUtils.TruncateAt.MARQUEE)
        assertThat(topicToolbarTitle.isSelected).isFalse()
        assertThat(topicToolbarTitle.textAlignment).isEqualTo(View.TEXT_ALIGNMENT_VIEW_START)
      }
    }
  }

  @Test
  fun testTopicFragment_clickOnToolbarNavigationButton_closeActivity() {
    initializeApplicationComponent(false)
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      onView(withContentDescription(R.string.navigate_up)).perform(click())
      onActivity { assertThat(it.isFinishing).isTrue() }
    }
  }

  @Test
  fun testTopicFragment_showsTopicFragmentWithMultipleTabs() {
    initializeApplicationComponent(enableExtraTabsUi = false)
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      onView(withId(R.id.topic_tabs_container)).perform(click()).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testTopicFragment_swipePage_hasSwipedPage() {
    initializeApplicationComponent(enableExtraTabsUi = false)
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      onView(withId(R.id.topic_tabs_viewpager)).check(matches(isDisplayed()))
      onView(withId(R.id.topic_tabs_viewpager)).perform(swipeLeft())
      verifyTabTitleAtPosition(position = 1)
    }
  }

  @Test
  fun testTopicFragment_enableExtraTabs_infoTopicTab_isDisplayedInTabLayout() {
    initializeApplicationComponent(enableExtraTabsUi = true)
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      onView(
        withText(
          TopicTab.getTabForPosition(
            position = INFO_TAB_POSITION,
            enableExtraTopicTabsUi.value
          ).name
        )
      ).check(matches(isDescendantOfA(withId(R.id.topic_tabs_container))))
    }
  }

  @Test
  fun testTopicFragment_disableExtraTabs_infoTopicTab_isNotDisplayedInTabLayout() {
    initializeApplicationComponent(enableExtraTabsUi = false)
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      onView(withText(TopicTab.getTabForPosition(position = INFO_TAB_POSITION, true).name))
        .check(doesNotExist())
    }
  }

  @Test
  fun testTopicFragment_disableExtraTabs_defaultTabIsLessons() {
    initializeApplicationComponent(enableExtraTabsUi = false)
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      testCoroutineDispatchers.runCurrent()
      verifyTabTitleAtPosition(position = LESSON_TAB_POSITION_EXTRA_TABS_DISABLED)
    }
  }

  @Test
  fun testTopicFragment_enableExtraTabs_defaultTabIsLessons() {
    initializeApplicationComponent(enableExtraTabsUi = true)
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      testCoroutineDispatchers.runCurrent()
      verifyTabTitleAtPosition(position = LESSON_TAB_POSITION)
    }
  }

  @Test
  fun testTopicFragment_disableExtraTabs_clickOnLessonsTab_showsPlayTabSelected() {
    initializeApplicationComponent(enableExtraTabsUi = false)
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      clickTabAtPosition(position = LESSON_TAB_POSITION_EXTRA_TABS_DISABLED)
      verifyTabTitleAtPosition(position = LESSON_TAB_POSITION_EXTRA_TABS_DISABLED)
    }
  }

  @Test
  fun testTopicFragment_clickOnLessonsTab_showsPlayTabWithContentMatched() {
    initializeApplicationComponent(enableExtraTabsUi = true)
    markAllSpotlightsSeen()
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      testCoroutineDispatchers.runCurrent()
      clickTabAtPosition(position = LESSON_TAB_POSITION)
      testCoroutineDispatchers.runCurrent()
      matchStringOnListItem(
        recyclerView = R.id.story_summary_recycler_view,
        itemPosition = 1,
        targetViewId = R.id.story_name_text_view,
        stringToMatch = "Matthew Goes to the Bakery"
      )
    }
  }

  @Test
  fun testTopicFragment_practiceTabEnabled_practiceTopicTabIsDisplayedInTabLayout() {
    initializeApplicationComponent(enableExtraTabsUi = true)
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      val practiceTab =
        TopicTab.getTabForPosition(position = PRACTICE_TAB_POSITION, enableExtraTopicTabsUi.value)
      onView(withText(practiceTab.name)).check(
        matches(
          isDescendantOfA(
            withId(
              R.id.topic_tabs_container
            )
          )
        )
      )
    }
  }

  @Test
  fun testTopicFragment_disableExtraTabs_practiceTopicTabIsNotDisplayedInTabLayout() {
    initializeApplicationComponent(enableExtraTabsUi = false)
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      // Unconditionally retrieve the practice tab name since this test is verifying that it's not
      // enabled.
      val practiceTab =
        TopicTab.getTabForPosition(position = PRACTICE_TAB_POSITION, enableExtraTopicTabsUi = true)
      onView(withText(practiceTab.name)).check(doesNotExist())
    }
  }

  @Test
  fun testTopicFragment_disableExtraTabs_configChange_practiceTopicTabIsNotDisplayed() {
    initializeApplicationComponent(enableExtraTabsUi = false)
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()

      // Unconditionally retrieve the practice tab name since this test is verifying that it's not
      // enabled.
      val practiceTab =
        TopicTab.getTabForPosition(position = PRACTICE_TAB_POSITION, enableExtraTopicTabsUi = true)
      // The tab should still not be visible even after a configuration change.
      onView(withText(practiceTab.name)).check(doesNotExist())
    }
  }

  @Test
  fun testTopicFragment_enableExtraTabs_clickOnPracticeTab_showsPracticeTabSelected() {
    initializeApplicationComponent(enableExtraTabsUi = true)
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      clickTabAtPosition(position = PRACTICE_TAB_POSITION)
      verifyTabTitleAtPosition(position = PRACTICE_TAB_POSITION)
    }
  }

  @Test
  fun testTopicFragment_enableExtraTabs_clickOnPracticeTab_showsPracticeTabWithContentMatched() {
    initializeApplicationComponent(enableExtraTabsUi = true)
    markAllSpotlightsSeen()
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      testCoroutineDispatchers.runCurrent()
      clickTabAtPosition(position = PRACTICE_TAB_POSITION)
      testCoroutineDispatchers.runCurrent()
      matchStringOnListItem(
        recyclerView = R.id.topic_practice_skill_list,
        itemPosition = 0,
        targetViewId = R.id.master_skills_text_view,
        stringToMatch = "Master These Skills"
      )
    }
  }

  @Test
  fun testTopicFragment_clickOnReviewTab_showsReviewTabSelected() {
    initializeApplicationComponent(enableExtraTabsUi = false)
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      clickTabAtPosition(position = REVISION_TAB_POSITION_EXTRA_TABS_DISABLED)
      verifyTabTitleAtPosition(position = REVISION_TAB_POSITION_EXTRA_TABS_DISABLED)
    }
  }

  @Test
  fun testTopicFragment_clickOnReviewTab_showsReviewTabWithContentMatched() {
    initializeApplicationComponent(enableExtraTabsUi = false)
    markAllSpotlightsSeen()
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      testCoroutineDispatchers.runCurrent()
      clickTabAtPosition(position = REVISION_TAB_POSITION_EXTRA_TABS_DISABLED)
      testCoroutineDispatchers.runCurrent()
      matchStringOnListItem(
        recyclerView = R.id.revision_recycler_view,
        itemPosition = 0,
        targetViewId = R.id.subtopic_title,
        stringToMatch = "What is a Fraction?"
      )
    }
  }

  @Test
  fun testTopicFragment_enableExtraTabs_clickOnReviewTab_thenInfoTab_showsInfoTab() {
    initializeApplicationComponent(enableExtraTabsUi = true)
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      clickTabAtPosition(position = REVISION_TAB_POSITION)
      clickTabAtPosition(position = INFO_TAB_POSITION)
      verifyTabTitleAtPosition(position = INFO_TAB_POSITION)
    }
  }

  @Test
  fun enableExtraTabs_clickOnReviewTab_thenInfoTab_showsInfoTabWithContentMatched() {
    initializeApplicationComponent(enableExtraTabsUi = true)
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, storyId = null
    ) {
      testCoroutineDispatchers.runCurrent()
      clickTabAtPosition(position = REVISION_TAB_POSITION)
      testCoroutineDispatchers.runCurrent()
      clickTabAtPosition(position = INFO_TAB_POSITION)
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.topic_name_text_view)).check(
        matches(
          withText(containsString(TOPIC_NAME))
        )
      )
    }
  }

  @Test
  fun testTopicFragment_clickOnLessonsTab_configChange_showsSameTabAndItsContent() {
    initializeApplicationComponent(enableExtraTabsUi = false)
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      testCoroutineDispatchers.runCurrent()
      clickTabAtPosition(position = LESSON_TAB_POSITION_EXTRA_TABS_DISABLED)
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      verifyTabTitleAtPosition(position = LESSON_TAB_POSITION_EXTRA_TABS_DISABLED)
      matchStringOnListItem(
        recyclerView = R.id.story_summary_recycler_view,
        itemPosition = 1,
        targetViewId = R.id.story_name_text_view,
        stringToMatch = "Matthew Goes to the Bakery"
      )
    }
  }

  @Test
  fun enableExtraTabs_clickOnPracticeTab_configChange_showsSameTabAndItsContent() {
    initializeApplicationComponent(enableExtraTabsUi = true)
    markAllSpotlightsSeen()
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      testCoroutineDispatchers.runCurrent()
      clickTabAtPosition(position = PRACTICE_TAB_POSITION)
      testCoroutineDispatchers.runCurrent()
      matchStringOnListItem(
        recyclerView = R.id.topic_practice_skill_list,
        itemPosition = 0,
        targetViewId = R.id.master_skills_text_view,
        stringToMatch = "Master These Skills"
      )
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      verifyTabTitleAtPosition(position = PRACTICE_TAB_POSITION)
      matchStringOnListItem(
        recyclerView = R.id.topic_practice_skill_list,
        itemPosition = 0,
        targetViewId = R.id.master_skills_text_view,
        stringToMatch = "Master These Skills"
      )
    }
  }

  @Test
  fun testTopicFragment_clickOnReviewTab_configChange_showsSameTabAndItsContent() {
    initializeApplicationComponent(enableExtraTabsUi = false)
    markAllSpotlightsSeen()
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      testCoroutineDispatchers.runCurrent()
      clickTabAtPosition(position = REVISION_TAB_POSITION_EXTRA_TABS_DISABLED)
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      verifyTabTitleAtPosition(position = REVISION_TAB_POSITION_EXTRA_TABS_DISABLED)
      matchStringOnListItem(
        recyclerView = R.id.revision_recycler_view,
        itemPosition = 0,
        targetViewId = R.id.subtopic_title,
        stringToMatch = "What is a Fraction?"
      )
    }
  }

  @Test
  fun testTopicFragment_enableExtraTabs_configChange_showsDefaultTabAndItsContent() {
    initializeApplicationComponent(enableExtraTabsUi = true)
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      verifyTabTitleAtPosition(position = LESSON_TAB_POSITION)
      matchStringOnListItem(
        recyclerView = R.id.story_summary_recycler_view,
        itemPosition = 1,
        targetViewId = R.id.story_name_text_view,
        stringToMatch = "Matthew Goes to the Bakery"
      )
    }
  }

  @Test
  fun testTopicFragment_disableExtraTabs_configChange_showsDefaultTabAndItsContent() {
    initializeApplicationComponent(enableExtraTabsUi = false)
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      verifyTabTitleAtPosition(position = LESSON_TAB_POSITION_EXTRA_TABS_DISABLED)
      matchStringOnListItem(
        recyclerView = R.id.story_summary_recycler_view,
        itemPosition = 1,
        targetViewId = R.id.story_name_text_view,
        stringToMatch = "Matthew Goes to the Bakery"
      )
    }
  }

  @Test
  fun enableExtraTabs_withStoryId_clickOnPracticeTab_configChange_showsSameTabAndItsContent() {
    initializeApplicationComponent(enableExtraTabsUi = true)
    markAllSpotlightsSeen()
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      testCoroutineDispatchers.runCurrent()
      clickTabAtPosition(position = PRACTICE_TAB_POSITION)
      testCoroutineDispatchers.runCurrent()
      matchStringOnListItem(
        recyclerView = R.id.topic_practice_skill_list,
        itemPosition = 0,
        targetViewId = R.id.master_skills_text_view,
        stringToMatch = "Master These Skills"
      )
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      verifyTabTitleAtPosition(position = PRACTICE_TAB_POSITION)
      matchStringOnListItem(
        recyclerView = R.id.topic_practice_skill_list,
        itemPosition = 0,
        targetViewId = R.id.master_skills_text_view,
        stringToMatch = "Master These Skills"
      )
    }
  }

  @Test
  fun testOpenFragment_lessonsTabDefaulted_logsLessonsTabOpen() {
    initializeApplicationComponent(enableExtraTabsUi = false)
    markAllSpotlightsSeen()
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      testCoroutineDispatchers.runCurrent()
      assertThat(fakeAnalyticsEventLogger.getMostRecentEvent())
        .hasOpenLessonsTabContextThat()
        .hasTopicIdThat()
        .isEqualTo(FRACTIONS_TOPIC_ID)
    }
  }

  @Test
  fun testOpenFragment_lessonsTabDefaulted_switchToRevisionTab_logsRevisionTabOpen() {
    initializeApplicationComponent(enableExtraTabsUi = false)
    markAllSpotlightsSeen()
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      testCoroutineDispatchers.runCurrent()
      clickTabAtPosition(position = REVISION_TAB_POSITION_EXTRA_TABS_DISABLED)
      testCoroutineDispatchers.runCurrent()

      assertThat(fakeAnalyticsEventLogger.getMostRecentEvent())
        .hasOpenRevisionTabContextThat()
        .hasTopicIdThat()
        .isEqualTo(FRACTIONS_TOPIC_ID)
    }
  }

  @Test
  fun testOpenFragment_lessonsTabDefaulted_switchToRevisionTabThenBack_logsLessonsTabOpenAgain() {
    initializeApplicationComponent(enableExtraTabsUi = false)
    markAllSpotlightsSeen()
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      testCoroutineDispatchers.runCurrent()
      clickTabAtPosition(position = REVISION_TAB_POSITION_EXTRA_TABS_DISABLED)
      testCoroutineDispatchers.runCurrent()
      clickTabAtPosition(position = LESSON_TAB_POSITION_EXTRA_TABS_DISABLED)
      testCoroutineDispatchers.runCurrent()

      assertThat(fakeAnalyticsEventLogger.getMostRecentEvent())
        .hasOpenLessonsTabContextThat()
        .hasTopicIdThat()
        .isEqualTo(FRACTIONS_TOPIC_ID)
    }
  }

  @Test
  fun testOpenFragment_extraTabs_openInfoTab_logsInfoTabOpen() {
    initializeApplicationComponent(enableExtraTabsUi = true)
    markAllSpotlightsSeen()
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      testCoroutineDispatchers.runCurrent()
      clickTabAtPosition(position = INFO_TAB_POSITION)
      testCoroutineDispatchers.runCurrent()

      assertThat(fakeAnalyticsEventLogger.getMostRecentEvent())
        .hasOpenInfoTabContextThat()
        .hasTopicIdThat()
        .isEqualTo(FRACTIONS_TOPIC_ID)
    }
  }

  @Test
  fun testOpenFragment_extraTabs_openQuestionsTab_logsInfoQuestionsOpen() {
    initializeApplicationComponent(enableExtraTabsUi = true)
    markAllSpotlightsSeen()
    runWithLaunchedActivityAndAddedFragment(
      profileId, TEST_CLASSROOM_ID_1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0
    ) {
      testCoroutineDispatchers.runCurrent()
      clickTabAtPosition(position = PRACTICE_TAB_POSITION)
      testCoroutineDispatchers.runCurrent()

      assertThat(fakeAnalyticsEventLogger.getMostRecentEvent())
        .hasOpenPracticeTabContextThat()
        .hasTopicIdThat()
        .isEqualTo(FRACTIONS_TOPIC_ID)
    }
  }

  private fun clickTabAtPosition(position: Int) {
    onView(
      allOf(
        withText(TopicTab.getTabForPosition(position, enableExtraTopicTabsUi.value).name),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
  }

  private fun verifyTabTitleAtPosition(position: Int) {
    onView(withId(R.id.topic_tabs_container)).check(
      matches(
        matchCurrentTabTitle(
          TopicTab.getTabForPosition(position, enableExtraTopicTabsUi.value).name
        )
      )
    )
  }

  // TODO(#2208): Create helper function in Test for RecyclerView
  private fun matchStringOnListItem(
    recyclerView: Int,
    itemPosition: Int,
    targetViewId: Int,
    stringToMatch: String
  ) {
    onView(
      atPositionOnView(
        recyclerView,
        itemPosition,
        targetViewId
      )
    ).check(
      matches(
        withText(
          containsString(
            stringToMatch
          )
        )
      )
    )
  }

  private fun markAllSpotlightsSeen() {
    markSpotlightSeen(TOPIC_LESSON_TAB)
    markSpotlightSeen(FIRST_CHAPTER)
    markSpotlightSeen(TOPIC_REVISION_TAB)
  }

  private fun markSpotlightSeen(feature: Spotlight.FeatureCase) {
    spotlightStateController.markSpotlightViewed(profileId, feature)
    testCoroutineDispatchers.runCurrent()
  }

  private fun initializeApplicationComponent(enableExtraTabsUi: Boolean) {
    TestPlatformParameterModule.forceEnableExtraTopicTabsUi(enableExtraTabsUi)
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun runWithLaunchedActivityAndAddedFragment(
    profileId: ProfileId,
    classroomId: String,
    topicId: String,
    storyId: String?,
    testBlock: ActivityScenario<TopicFragmentTestActivity>.() -> Unit
  ) {
    val fragment = createTopicFragment(profileId, classroomId, topicId, storyId)
    val intent = Intent(context, TopicFragmentTestActivity::class.java)
    TestActivity.registerWithPackageManager<TopicFragmentTestActivity>(context)
    ActivityScenario.launch<TopicFragmentTestActivity>(intent).use { scenario ->
      scenario.onActivity { activity ->
        activity.setContentView(R.layout.test_activity)
        activity.supportFragmentManager.beginTransaction()
          .add(R.id.test_fragment_placeholder, fragment)
          .add(SpotlightFragment.newInstance(profileId.internalId), SPOTLIGHT_FRAGMENT_TAG)
          .commitNow()
      }
      // Note that some tests are sensitive to early runCurrent execution, so it's skipped here.
      scenario.testBlock()
    }
  }

  private fun createTopicFragment(
    profileId: ProfileId,
    classroomId: String,
    topicId: String,
    storyId: String?
  ): TopicFragment {
    return TopicFragment().apply {
      this.arguments = Bundle().apply {
        val args = TopicFragmentArguments.newBuilder().apply {
          this.classroomId = classroomId
          this.topicId = topicId
          storyId?.let { this.storyId = it }
        }.build()
        putProto(TOPIC_FRAGMENT_ARGUMENTS_KEY, args)
        decorateWithUserProfileId(profileId)
      }
    }
  }

  class TopicFragmentTestActivity :
    TestActivity(),
    RouteToQuestionPlayerListener,
    RouteToStoryListener,
    RouteToExplorationListener,
    RouteToResumeLessonListener,
    RouteToRevisionCardListener {
    override fun routeToExploration(
      profileId: ProfileId,
      classroomId: String,
      topicId: String,
      storyId: String,
      explorationId: String,
      parentScreen: ExplorationActivityParams.ParentScreen,
      isCheckpointingEnabled: Boolean
    ) {
    }

    override fun routeToQuestionPlayer(skillIdList: ArrayList<String>) {
    }

    override fun routeToResumeLesson(
      profileId: ProfileId,
      classroomId: String,
      topicId: String,
      storyId: String,
      explorationId: String,
      parentScreen: ExplorationActivityParams.ParentScreen,
      explorationCheckpoint: ExplorationCheckpoint
    ) {
    }

    override fun routeToRevisionCard(
      profileId: ProfileId,
      topicId: String,
      subtopicId: Int,
      subtopicListSize: Int
    ) {
    }

    override fun routeToStory(
      internalProfileId: Int,
      classroomId: String,
      topicId: String,
      storyId: String
    ) {
    }
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
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class, WorkManagerConfigurationModule::class,
      ApplicationStartupListenerModule::class, LogReportWorkerModule::class,
      HintsAndSolutionConfigModule::class, HintsAndSolutionProdModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, RetrofitModule::class, RetrofitServiceModule::class,
      NetworkConfigProdModule::class,
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

    fun inject(topicFragmentTest: TopicFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerTopicFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(topicFragmentTest: TopicFragmentTest) {
      component.inject(topicFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
