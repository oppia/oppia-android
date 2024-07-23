package org.oppia.android.app.story

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.text.Spannable
import android.text.TextUtils
import android.text.style.ClickableSpan
import android.view.View
import android.view.View.TEXT_ALIGNMENT_VIEW_START
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
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
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import dagger.Component
import dagger.Module
import dagger.Provides
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Captor
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
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
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.player.exploration.ExplorationActivity
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.hasItemCount
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
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
import org.oppia.android.domain.topic.RATIOS_STORY_ID_0
import org.oppia.android.domain.topic.RATIOS_TOPIC_ID
import org.oppia.android.domain.topic.TEST_STORY_ID_0
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.DisableAccessibilityChecks
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.mockito.anyOrNull
import org.oppia.android.testing.mockito.capture
import org.oppia.android.testing.profile.ProfileTestHelper
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
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EventLoggingConfigurationModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.ImageLoader
import org.oppia.android.util.parser.image.ImageParsingModule
import org.oppia.android.util.parser.image.ImageTransformation
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [StoryFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = StoryFragmentTest.TestApplication::class, qualifiers = "port-xxhdpi")
class StoryFragmentTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

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
  lateinit var accessibilityService: FakeAccessibilityService

  @Captor
  lateinit var listCaptor: ArgumentCaptor<List<ImageTransformation>>

  @get:Rule
  var activityTestRule: ActivityTestRule<StoryActivity> = ActivityTestRule(
    StoryActivity::class.java, /* initialTouchMode= */
    true, /* launchActivity= */
    false
  )

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

  @Test // TODO(#3245): Error -> URLSpan should be used in place of ClickableSpan
  @DisableAccessibilityChecks
  fun testStoryFragment_clickOnToolbarNavigationButton_closeActivity() {
    activityTestRule.launchActivity(createFractionsStoryActivityIntent())
    testCoroutineDispatchers.runCurrent()
    onView(withContentDescription(R.string.navigate_up)).perform(click())
    assertThat(activityTestRule.activity.isFinishing).isTrue()
  }

  @Test
  fun testStoryFragment_toolbarTitle_isDisplayedSuccessfully() {
    launch<StoryActivity>(createFractionsStoryActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.story_toolbar_title))
        .check(matches(withText("Matthew Goes to the Bakery")))
    }
  }

  // TODO(#4212): Error -> Only the original thread that created a view hierarchy can touch its view
  @Test
  fun testStoryFragment_toolbarTitle_readerOff_marqueeInRtl_isDisplayedCorrectly() {
    accessibilityService.setScreenReaderEnabled(false)
    activityTestRule.launchActivity(createFractionsStoryActivityIntent())
    testCoroutineDispatchers.runCurrent()

    val storyToolbarTitle: TextView =
      activityTestRule.activity.findViewById(R.id.story_toolbar_title)
    ViewCompat.setLayoutDirection(storyToolbarTitle, ViewCompat.LAYOUT_DIRECTION_RTL)

    onView(withId(R.id.story_toolbar_title)).perform(click())
    assertThat(storyToolbarTitle.ellipsize).isEqualTo(TextUtils.TruncateAt.MARQUEE)
    assertThat(storyToolbarTitle.isSelected).isEqualTo(true)
    assertThat(storyToolbarTitle.textAlignment).isEqualTo(TEXT_ALIGNMENT_VIEW_START)
  }

  // TODO(#4212): Error -> Only the original thread that created a view hierarchy can touch its view
  @Test
  fun testStoryFragment_toolbarTitle_readerOn_marqueeInRtl_isDisplayedCorrectly() {
    accessibilityService.setScreenReaderEnabled(true)
    activityTestRule.launchActivity(createFractionsStoryActivityIntent())
    testCoroutineDispatchers.runCurrent()

    val storyToolbarTitle: TextView =
      activityTestRule.activity.findViewById(R.id.story_toolbar_title)
    ViewCompat.setLayoutDirection(storyToolbarTitle, ViewCompat.LAYOUT_DIRECTION_RTL)

    onView(withId(R.id.story_toolbar_title)).perform(click())
    assertThat(storyToolbarTitle.ellipsize).isEqualTo(TextUtils.TruncateAt.MARQUEE)
    assertThat(storyToolbarTitle.isSelected).isEqualTo(false)
    assertThat(storyToolbarTitle.textAlignment).isEqualTo(TEXT_ALIGNMENT_VIEW_START)
  }

  // TODO(#4212): Error -> Only the original thread that created a view hierarchy can touch its view
  @Test
  fun testStoryFragment_toolbarTitle_readerOff_marqueeInLtr_isDisplayedCorrectly() {
    accessibilityService.setScreenReaderEnabled(false)
    activityTestRule.launchActivity(createFractionsStoryActivityIntent())
    testCoroutineDispatchers.runCurrent()

    val storyToolbarTitle: TextView =
      activityTestRule.activity.findViewById(R.id.story_toolbar_title)
    ViewCompat.setLayoutDirection(storyToolbarTitle, ViewCompat.LAYOUT_DIRECTION_LTR)
    onView(withId(R.id.story_toolbar_title)).perform(click())
    assertThat(storyToolbarTitle.ellipsize).isEqualTo(TextUtils.TruncateAt.MARQUEE)
    assertThat(storyToolbarTitle.isSelected).isEqualTo(true)
    assertThat(storyToolbarTitle.textAlignment).isEqualTo(TEXT_ALIGNMENT_VIEW_START)
  }

  // TODO(#4212): Error -> Only the original thread that created a view hierarchy can touch its view
  @Test
  fun testStoryFragment_toolbarTitle_readerOn_marqueeInLtr_isDisplayedCorrectly() {
    accessibilityService.setScreenReaderEnabled(true)
    activityTestRule.launchActivity(createFractionsStoryActivityIntent())
    testCoroutineDispatchers.runCurrent()

    val storyToolbarTitle: TextView =
      activityTestRule.activity.findViewById(R.id.story_toolbar_title)
    ViewCompat.setLayoutDirection(storyToolbarTitle, ViewCompat.LAYOUT_DIRECTION_LTR)

    onView(withId(R.id.story_toolbar_title)).perform(click())
    assertThat(storyToolbarTitle.ellipsize).isEqualTo(TextUtils.TruncateAt.MARQUEE)
    assertThat(storyToolbarTitle.isSelected).isEqualTo(false)
    assertThat(storyToolbarTitle.textAlignment).isEqualTo(TEXT_ALIGNMENT_VIEW_START)
  }

  @Test
  fun testStoryFragment_correctStoryCountLoadedInHeader() {
    setStoryPartialProgressForFractions()
    launch<StoryActivity>(createFractionsStoryActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      val headerString: String =
        getResources().getQuantityString(R.plurals.story_total_chapters, 2, 1, 2)
      onView(withId(R.id.story_chapter_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_chapter_list,
          position = 0,
          targetViewId = R.id.story_progress_chapter_completed_text
        )
      ).check(
        matches(
          withText(headerString)
        )
      )
    }
  }

  @Test
  fun testStoryFragment_completedExp0_tickHasCorrectContentDescription() {
    setStoryPartialProgressForFractions()
    launch<StoryActivity>(createFractionsStoryActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.story_chapter_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_chapter_list,
          position = 1,
          targetViewId = R.id.chapter_completed_tick
        )
      ).check(matches(withContentDescription(R.string.chapter_completed)))
    }
  }

  @Test
  fun testStoryFragment_correctNumberOfStoriesLoadedInRecyclerView() {
    launch<StoryActivity>(createFractionsStoryActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.story_chapter_list)).check(hasItemCount(3))
    }
  }

  @Test
  fun testStoryFragment_changeConfiguration_textViewIsShownCorrectly() {
    launch<StoryActivity>(createFractionsStoryActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.story_chapter_list))).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_chapter_list,
          position = 1,
          targetViewId = R.id.chapter_title
        )
      ).check(
        matches(
          withText("Chapter 1: What is a Fraction?")
        )
      )
    }
  }

  @Test
  fun testStoryFragment_chapterSummaryIsShownCorrectly() {
    launch<StoryActivity>(createFractionsStoryActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.story_chapter_list))).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_chapter_list,
          position = 1,
          targetViewId = R.id.chapter_summary
        )
      ).check(
        matches(
          withText("Matthew learns about fractions.")
        )
      )
    }
  }

  @Test
  fun testStoryFragment_chapterSummary_ltrEnabled_textAlignmentIsCorrect() {
    launch<StoryActivity>(createFractionsStoryActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.story_chapter_list))).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_chapter_list,
          position = 1,
          targetViewId = R.id.chapter_summary
        )
      ).check { view, _ ->
        ViewCompat.setLayoutDirection(view, ViewCompat.LAYOUT_DIRECTION_LTR)
        val chapterSummayTextview: TextView = view.findViewById<TextView>(
          R.id.chapter_summary
        )
        assertThat(chapterSummayTextview.textAlignment).isEqualTo(TEXT_ALIGNMENT_VIEW_START)
      }
    }
  }

  @Test
  fun testStoryFragment_chapterSummary_rtlEnabled_textAlignmentIsCorrect() {
    launch<StoryActivity>(createFractionsStoryActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.story_chapter_list))).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_chapter_list,
          position = 1,
          targetViewId = R.id.chapter_summary
        )
      ).check { view, _ ->
        ViewCompat.setLayoutDirection(view, ViewCompat.LAYOUT_DIRECTION_RTL)
        val chapterSummayTextview: TextView = view.findViewById<TextView>(
          R.id.chapter_summary
        )
        assertThat(chapterSummayTextview.textAlignment).isEqualTo(TEXT_ALIGNMENT_VIEW_START)
      }
    }
  }

  @Test
  fun testStoryFragment_changeConfiguration_chapterSummaryIsShownCorrectly() {
    launch<StoryActivity>(createFractionsStoryActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.story_chapter_list))).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_chapter_list,
          position = 1,
          targetViewId = R.id.chapter_summary
        )
      ).check(
        matches(
          withText("Matthew learns about fractions.")
        )
      )
    }
  }

  @Test
  fun testStoryFragment_chapterLongSummaryIsShownCorrectly() {
    launch<StoryActivity>(createTestStoryActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.story_chapter_list))).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_chapter_list,
          position = 1,
          targetViewId = R.id.chapter_summary
        )
      ).check(
        matches(
          withText(
            "This is the outline/summary for the first exploration of the story. It is very long" +
              " but it has to be fully visible. You wil be learning about Oppia interactions." +
              " There is no second story to follow-up, but there is a second chapter."
          )
        )
      )
    }
  }

  @Test
  fun testStoryFragment_changeConfiguration_chapterLongSummaryIsShownCorrectly() {
    launch<StoryActivity>(createTestStoryActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.story_chapter_list))).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_chapter_list,
          position = 1,
          targetViewId = R.id.chapter_summary
        )
      ).check(
        matches(
          withText(
            "This is the outline/summary for the first exploration of the story. It is very long" +
              " but it has to be fully visible. You wil be learning about Oppia interactions." +
              " There is no second story to follow-up, but there is a second chapter."
          )
        )
      )
    }
  }

  @Test
  fun testStoryFragment_chapterMissingPrerequisiteThumbnailIsBlurred() {
    launch<StoryActivity>(createFractionsStoryActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.story_chapter_list))).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          2
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_chapter_list,
          position = 2,
          targetViewId = R.id.chapter_thumbnail
        )
      ).check { view, _ ->
        var lessonThumbnailImageView = view.findViewById<LessonThumbnailImageView>(
          R.id.chapter_thumbnail
        )
        verify(lessonThumbnailImageView.imageLoader, atLeastOnce()).loadDrawable(
          imageDrawableResId = anyInt(),
          target = anyOrNull(),
          transformations = capture(listCaptor)
        )
        assertThat(listCaptor.value).contains(ImageTransformation.BLUR)
      }
    }
  }

  @Test
  fun testStoryFragment_chapterMissingPrerequisiteIsShownCorrectly() {
    launch<StoryActivity>(createFractionsStoryActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.story_chapter_list))).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          2
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_chapter_list,
          position = 2,
          targetViewId = R.id.chapter_summary
        )
      ).check(
        matches(
          withText("Complete Chapter 1: What is a Fraction? to unlock this chapter.")
        )
      )
    }
  }

  @Test
  fun testStoryFragment_changeConfiguration_chapterMissingPrerequisiteIsShownCorrectly() {
    launch<StoryActivity>(createFractionsStoryActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.story_chapter_list))).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          2
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_chapter_list,
          position = 2,
          targetViewId = R.id.chapter_summary
        )
      ).check(
        matches(
          withText("Complete Chapter 1: What is a Fraction? to unlock this chapter.")
        )
      )
    }
  }

  @Test
  fun testStoryFragment_checkClickableSpanWithoutScreenReader_isClickable() {
    launch<StoryActivity>(createFractionsStoryActivityIntent()).use {
      accessibilityService.setScreenReaderEnabled(false)
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.story_chapter_list))).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          2
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_chapter_list,
          position = 2,
          targetViewId = R.id.chapter_summary
        )
      ).check(
        matches(hasClickableSpanWithText("Chapter 1: What is a Fraction?"))
      )
    }
  }

  @Test
  fun testStoryFragment_checkClickableSpanWithScreenReader_isNotClickable() {
    launch<StoryActivity>(createFractionsStoryActivityIntent()).use {
      accessibilityService.setScreenReaderEnabled(true)
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.story_chapter_list))).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          2
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_chapter_list,
          position = 2,
          targetViewId = R.id.chapter_summary
        )
      ).check(
        matches(not(hasClickableSpanWithText("Chapter 1: What is a Fraction?")))
      )
    }
  }

  @Test
  fun testStoryFragment_clickPrerequisiteChapter_prerequisiteChapterCardIsDisplayed() {
    launch<StoryActivity>(createFractionsStoryActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.story_chapter_list))).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          2
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_chapter_list,
          position = 2,
          targetViewId = R.id.chapter_summary
        )
      ).perform(
        openClickableSpan(
          "Chapter 1: What is a Fraction?"
        )
      )
      onView(
        atPosition(
          recyclerViewId = R.id.story_chapter_list,
          position = 1
        )
      ).check(
        matches(
          isDisplayed()
        )
      )
    }
  }

  @Test
  fun testStoryFragment_configChange_clickPrerequisiteChapter_prerequisiteChapterCardIsDisplayed() {
    launch<StoryActivity>(createFractionsStoryActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.story_chapter_list))).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          2
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_chapter_list,
          position = 2,
          targetViewId = R.id.chapter_summary
        )
      ).perform(
        openClickableSpan(
          "Chapter 1: What is a Fraction?"
        )
      )
      onView(
        atPosition(
          recyclerViewId = R.id.story_chapter_list,
          position = 1
        )
      ).check(
        matches(
          isDisplayed()
        )
      )
    }
  }

  @Test // TODO(#3245): Error -> View falls below the minimum recommended size for touch targets and
  // URLSpan should be used in place of ClickableSpan
  @DisableAccessibilityChecks
  fun testStoryFragment_changeConfiguration_explorationStartCorrectly() {
    launch<StoryActivity>(createFractionsStoryActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.story_chapter_list))).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_chapter_list,
          position = 1,
          targetViewId = R.id.story_chapter_card
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(ExplorationActivity::class.java.name))
    }
  }

  @Test
  fun testStoryFragment_changeConfiguration_correctStoryCountInHeader() {
    setStoryPartialProgressForFractions()
    launch<StoryActivity>(createFractionsStoryActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      val headerString: String =
        getResources().getQuantityString(R.plurals.story_total_chapters, 2, 1, 2)
      onView(withId(R.id.story_chapter_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(withId(R.id.story_chapter_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_chapter_list,
          position = 0,
          targetViewId = R.id.story_progress_chapter_completed_text
        )
      ).check(
        matches(
          withText(headerString)
        )
      )
    }
  }

  @Config(qualifiers = "+sw600dp")
  @Test // TODO(#4212): Error -> No views in hierarchy found matching
  fun testStoryFragment_completedChapter_checkProgressDrawableIsCorrect() {
    setStoryPartialProgressForFractions()
    launch<StoryActivity>(createFractionsStoryActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_chapter_list,
          position = 1,
          targetViewId = R.id.progress_image_view
        )
      ).check(matches(withDrawable(R.drawable.circular_solid_color_primary_32dp)))
    }
  }

  @Config(qualifiers = "+sw600dp")
  @Test // TODO(#4212): Error -> No views in hierarchy found matching
  fun testStoryFragment_notStartedChapter_checkProgressDrawableIsCorrect() {
    launch<StoryActivity>(createFractionsStoryActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_chapter_list,
          position = 1,
          targetViewId = R.id.progress_image_view
        )
      ).check(matches(withDrawable(R.drawable.circular_stroke_2dp_color_primary_32dp)))
    }
  }

  @Config(qualifiers = "+sw600dp")
  @Test // TODO(#4212): Error -> No views in hierarchy found matching
  fun testStoryFragment_lockedChapter_checkProgressDrawableIsCorrect() {
    launch<StoryActivity>(createRatiosStoryActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_chapter_list,
          position = 1,
          targetViewId = R.id.progress_image_view
        )
      ).check(matches(withDrawable(R.drawable.circular_stroke_2dp_grey_32dp)))
    }
  }

  @Config(qualifiers = "+sw600dp")
  @Test // TODO(#4212): Error -> No views in hierarchy found matching
  fun testStoryFragment_completedChapter_pawIconIsVisible() {
    launch<StoryActivity>(createFractionsStoryActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_chapter_list,
          position = 1,
          targetViewId = R.id.completed_chapter_image_view
        )
      ).check(matches(withDrawable(R.drawable.ic_lessons_icon_24dp)))
    }
  }

  @Config(qualifiers = "+sw600dp")
  @Test // TODO(#4212): Error -> No views in hierarchy found matching
  fun testStoryFragment_pendingChapter_pawIconIsGone() {
    launch<StoryActivity>(createFractionsStoryActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_chapter_list,
          position = 2,
          targetViewId = R.id.completed_chapter_image_view
        )
      ).check(matches(not(isDisplayed())))
    }
  }

  @Config(qualifiers = "+sw600dp")
  @Test // TODO(#4212): Error -> No views in hierarchy found matching
  fun testStoryFragment_completedChapter_verticalDashedLineIsVisible() {
    launch<StoryActivity>(createFractionsStoryActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_chapter_list,
          position = 1,
          targetViewId = R.id.verticalDashedLineView
        )
      ).check(matches(isDisplayed()))
    }
  }

  @Config(qualifiers = "+sw600dp")
  // TODO(#4212): Error -> No views in hierarchy found matching
  @Test
  fun testStoryFragment_lastChapter_verticalDashedLineIsGone() {
    launch<StoryActivity>(createFractionsStoryActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.story_chapter_list,
          position = 2,
          targetViewId = R.id.verticalDashedLineView
        )
      ).check(matches(not(isDisplayed())))
    }
  }

  /**
   * Returns an action that finds a TextView containing the specific text, finds a ClickableSpan
   * within that text view that contains the specified text, then clicks it. The need for this was
   * inspired by https://stackoverflow.com/q/38314077.
   */
  @Suppress("SameParameterValue")
  private fun openClickableSpan(text: String): ViewAction {
    return object : ViewAction {
      override fun getDescription(): String = "openClickableSpan"

      override fun getConstraints(): Matcher<View> = hasClickableSpanWithText(text)

      override fun perform(uiController: UiController?, view: View?) {
        // The view shouldn't be null if the constraints are being met.
        (view as? TextView)?.getClickableSpans()?.findMatchingTextOrNull(text)?.onClick(view)
      }
    }
  }

  /**
   * Returns a matcher that matches against text views with clickable spans that contain the
   * specified text.
   */
  private fun hasClickableSpanWithText(text: String): Matcher<View> {
    return object : TypeSafeMatcher<View>(TextView::class.java) {
      override fun describeTo(description: Description?) {
        description?.appendText("has ClickableSpan with text")?.appendValue(text)
      }

      override fun matchesSafely(item: View?): Boolean {
        return (item as? TextView)?.getClickableSpans()?.findMatchingTextOrNull(text) != null
      }
    }
  }

  private fun TextView.getClickableSpans(): List<Pair<String, ClickableSpan>> {
    val viewText = text
    return (viewText as Spannable).getSpans(
      /* start= */ 0, /* end= */
      text.length,
      ClickableSpan::class.java
    ).map {
      viewText.subSequence(viewText.getSpanStart(it), viewText.getSpanEnd(it)).toString() to it
    }
  }

  private fun List<Pair<String, ClickableSpan>>.findMatchingTextOrNull(
    text: String
  ): ClickableSpan? {
    return find { text in it.first }?.second
  }

  private fun createFractionsStoryActivityIntent(): Intent {
    return StoryActivity.createStoryActivityIntent(
      ApplicationProvider.getApplicationContext(),
      internalProfileId,
      TEST_CLASSROOM_ID_1,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0
    )
  }

  private fun createTestStoryActivityIntent(): Intent {
    return StoryActivity.createStoryActivityIntent(
      ApplicationProvider.getApplicationContext(),
      internalProfileId,
      TEST_CLASSROOM_ID_1,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0
    )
  }

  private fun createRatiosStoryActivityIntent(): Intent {
    return StoryActivity.createStoryActivityIntent(
      ApplicationProvider.getApplicationContext(),
      internalProfileId,
      TEST_CLASSROOM_ID_1,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0
    )
  }

  private fun setStoryPartialProgressForFractions() {
    storyProgressTestHelper.markCompletedFractionsStory0Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun getResources(): Resources {
    return ApplicationProvider.getApplicationContext<Context>().resources
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
      PlatformParameterModule::class, PlatformParameterSingletonModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, TestModule::class, ImageParsingModule::class,
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

    fun inject(storyFragmentTest: StoryFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerStoryFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(storyFragmentTest: StoryFragmentTest) {
      component.inject(storyFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }

  /** Provides test dependencies (including a mock for [ImageLoader] to capture its operations). */
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideMockImageLoader() = mock(ImageLoader::class.java)
  }
}
