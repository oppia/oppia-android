package org.oppia.android.app.devoptions

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.CoreMatchers.containsString
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
import org.oppia.android.app.devoptions.markchapterscompleted.MarkChaptersCompletedFragment
import org.oppia.android.app.devoptions.markchapterscompleted.testing.MarkChaptersCompletedTestActivity
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.model.MarkChaptersCompletedFragmentArguments
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
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
import org.oppia.android.domain.topic.StoryProgressController
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.story.StoryProgressTestHelper
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
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
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [MarkChaptersCompletedFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = MarkChaptersCompletedFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class MarkChaptersCompletedFragmentTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()
  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var storyProgressTestHelper: StoryProgressTestHelper
  @Inject
  lateinit var storyProgressController: StoryProgressController
  @Inject
  lateinit var fakeOppiaClock: FakeOppiaClock
  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject
  lateinit var context: Context
  @Inject
  lateinit var monitorFactory: DataProviderTestMonitor.Factory

  private val internalProfileId = 0
  private lateinit var profileId: ProfileId

  @Before
  fun setUp() {
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    setUpTestApplicationComponent()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    testCoroutineDispatchers.registerIdlingResource()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testMarkChaptersCompletedFragment_storiesAreShown() {
    launchMarkChaptersCompletedFragmentTestActivity(internalProfileId).use {
      testCoroutineDispatchers.runCurrent()
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 0,
        stringToMatch = "First Story",
        targetViewId = R.id.mark_chapters_completed_story_name_text_view
      )
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 4,
        stringToMatch = "Other Interesting Story",
        targetViewId = R.id.mark_chapters_completed_story_name_text_view
      )
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 6,
        stringToMatch = "Matthew Goes to the Bakery",
        targetViewId = R.id.mark_chapters_completed_story_name_text_view
      )
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 9,
        stringToMatch = "Ratios: Part 1",
        targetViewId = R.id.mark_chapters_completed_story_name_text_view
      )
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 12,
        stringToMatch = "Ratios: Part 2",
        targetViewId = R.id.mark_chapters_completed_story_name_text_view
      )
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_configChange_storiesAreShown() {
    launchMarkChaptersCompletedFragmentTestActivity(internalProfileId).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 0,
        stringToMatch = "First Story",
        targetViewId = R.id.mark_chapters_completed_story_name_text_view
      )
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 4,
        stringToMatch = "Other Interesting Story",
        targetViewId = R.id.mark_chapters_completed_story_name_text_view
      )
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 6,
        stringToMatch = "Matthew Goes to the Bakery",
        targetViewId = R.id.mark_chapters_completed_story_name_text_view
      )
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 9,
        stringToMatch = "Ratios: Part 1",
        targetViewId = R.id.mark_chapters_completed_story_name_text_view
      )
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 12,
        stringToMatch = "Ratios: Part 2",
        targetViewId = R.id.mark_chapters_completed_story_name_text_view
      )
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_chaptersAreShown() {
    launchMarkChaptersCompletedFragmentTestActivity(internalProfileId).use {
      testCoroutineDispatchers.runCurrent()
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 1,
        stringToMatch = "Prototype Exploration",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 2,
        stringToMatch = "Image Region Selection Exploration",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 3,
        stringToMatch = "Math Expressions",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 5,
        stringToMatch = "Fifth Exploration",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 7,
        stringToMatch = "What is a Fraction?",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 8,
        stringToMatch = "The Meaning of Equal Parts",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 10,
        stringToMatch = "What is a Ratio?",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 11,
        stringToMatch = "Order is important",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 13,
        stringToMatch = "Equivalent Ratios",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 14,
        stringToMatch = "Writing Ratios in Simplest Form",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_configChange_chaptersAreShown() {
    launchMarkChaptersCompletedFragmentTestActivity(internalProfileId).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 1,
        stringToMatch = "Prototype Exploration",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 2,
        stringToMatch = "Image Region Selection Exploration",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 3,
        stringToMatch = "Math Expressions",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 5,
        stringToMatch = "Fifth Exploration",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 7,
        stringToMatch = "What is a Fraction?",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 8,
        stringToMatch = "The Meaning of Equal Parts",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 10,
        stringToMatch = "What is a Ratio?",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 11,
        stringToMatch = "Order is important",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 13,
        stringToMatch = "Equivalent Ratios",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 14,
        stringToMatch = "Writing Ratios in Simplest Form",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_selectAll_isChecked() {
    launchMarkChaptersCompletedFragmentTestActivity(internalProfileId).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.mark_chapters_completed_all_check_box_container)).perform(click())
      onView(withId(R.id.mark_chapters_completed_all_check_box)).check(matches(isChecked()))
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_selectAll_configChange_isChecked() {
    launchMarkChaptersCompletedFragmentTestActivity(internalProfileId).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.mark_chapters_completed_all_check_box_container)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.mark_chapters_completed_all_check_box)).check(matches(isChecked()))
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_selectAll_selectsAllChapters() {
    launchMarkChaptersCompletedFragmentTestActivity(internalProfileId).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.mark_chapters_completed_all_check_box_container)).perform(click())
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 1)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 2)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 3)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 5)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 7)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 8)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 10)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 11)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 13)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 14)
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_deselectAllChapters_deselectsAllChapters() {
    launchMarkChaptersCompletedFragmentTestActivity(internalProfileId).use {
      testCoroutineDispatchers.runCurrent()
      // Click one to select all chapters.
      onView(withId(R.id.mark_chapters_completed_all_check_box_container)).perform(click())
      // Click a second time to unselect all chapters.
      onView(withId(R.id.mark_chapters_completed_all_check_box_container)).perform(click())
      verifyItemUncheckedOnRecyclerViewItemAtPosition(itemPosition = 1)
      verifyItemUncheckedOnRecyclerViewItemAtPosition(itemPosition = 2)
      verifyItemUncheckedOnRecyclerViewItemAtPosition(itemPosition = 3)
      verifyItemUncheckedOnRecyclerViewItemAtPosition(itemPosition = 5)
      verifyItemUncheckedOnRecyclerViewItemAtPosition(itemPosition = 7)
      verifyItemUncheckedOnRecyclerViewItemAtPosition(itemPosition = 8)
      verifyItemUncheckedOnRecyclerViewItemAtPosition(itemPosition = 10)
      verifyItemUncheckedOnRecyclerViewItemAtPosition(itemPosition = 11)
      verifyItemUncheckedOnRecyclerViewItemAtPosition(itemPosition = 13)
      verifyItemUncheckedOnRecyclerViewItemAtPosition(itemPosition = 14)
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.mark_chapters_completed_all_check_box)).check(matches(not(isChecked())))
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_selectAll_configChange_selectsAllChapters() {
    launchMarkChaptersCompletedFragmentTestActivity(internalProfileId).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.mark_chapters_completed_all_check_box_container)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 1)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 2)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 3)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 5)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 7)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 8)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 10)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 11)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 13)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 14)
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_selectChaptersOfFirstStoryInOrder_chaptersAreChecked() {
    launchMarkChaptersCompletedFragmentTestActivity(internalProfileId).use {
      testCoroutineDispatchers.runCurrent()
      clickOnRecyclerViewItemAtPosition(itemPosition = 1)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 1)
      clickOnRecyclerViewItemAtPosition(itemPosition = 2)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 2)
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_selectChaptersOfFirstStoryInOrder_configChange_chaptersAreChecked() { // ktlint-disable max-line-length
    launchMarkChaptersCompletedFragmentTestActivity(internalProfileId).use {
      testCoroutineDispatchers.runCurrent()
      clickOnRecyclerViewItemAtPosition(itemPosition = 1)
      clickOnRecyclerViewItemAtPosition(itemPosition = 2)
      onView(isRoot()).perform(orientationLandscape())
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 1)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 2)
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_firstStory_firstChapterUnchecked_secondChapterIsDisabled() {
    launchMarkChaptersCompletedFragmentTestActivity(internalProfileId).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.mark_chapters_completed_recycler_view,
          position = 1,
          targetViewId = R.id.mark_chapters_completed_chapter_check_box
        )
      ).check(matches(not(isChecked())))
      scrollToPosition(position = 2)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.mark_chapters_completed_recycler_view,
          position = 2,
          targetViewId = R.id.mark_chapters_completed_chapter_check_box
        )
      ).check(matches(not(isEnabled())))
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_land_firstStory_firstChapterUnchecked_secondChapterIsDisabled() { // ktlint-disable max-line-length
    launchMarkChaptersCompletedFragmentTestActivity(internalProfileId).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 1)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.mark_chapters_completed_recycler_view,
          position = 1,
          targetViewId = R.id.mark_chapters_completed_chapter_check_box
        )
      ).check(matches(not(isChecked())))
      scrollToPosition(position = 2)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.mark_chapters_completed_recycler_view,
          position = 2,
          targetViewId = R.id.mark_chapters_completed_chapter_check_box
        )
      ).check(matches(not(isEnabled())))
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_firstStory_selectChaptersInOrder_unselectFirstChapter_secondChapterIsDisabled() { // ktlint-disable max-line-length
    launchMarkChaptersCompletedFragmentTestActivity(internalProfileId).use {
      testCoroutineDispatchers.runCurrent()
      clickOnRecyclerViewItemAtPosition(itemPosition = 1)
      clickOnRecyclerViewItemAtPosition(itemPosition = 2)
      clickOnRecyclerViewItemAtPosition(itemPosition = 1)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.mark_chapters_completed_recycler_view,
          position = 2,
          targetViewId = R.id.mark_chapters_completed_chapter_check_box
        )
      ).check(matches(not(isEnabled())))
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_firstStory_selectChaptersInOrder_unselectFirstChapter_configChange_secondChapterIsDisabled() { // ktlint-disable max-line-length
    launchMarkChaptersCompletedFragmentTestActivity(internalProfileId).use {
      testCoroutineDispatchers.runCurrent()
      clickOnRecyclerViewItemAtPosition(itemPosition = 1)
      clickOnRecyclerViewItemAtPosition(itemPosition = 2)
      clickOnRecyclerViewItemAtPosition(itemPosition = 1)
      onView(isRoot()).perform(orientationLandscape())
      onView(
        atPositionOnView(
          recyclerViewId = R.id.mark_chapters_completed_recycler_view,
          position = 2,
          targetViewId = R.id.mark_chapters_completed_chapter_check_box
        )
      ).check(matches(not(isEnabled())))
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_selectAllChapters_allCheckBoxIsChecked() {
    launchMarkChaptersCompletedFragmentTestActivity(internalProfileId).use {
      testCoroutineDispatchers.runCurrent()
      clickOnRecyclerViewItemAtPosition(itemPosition = 1)
      clickOnRecyclerViewItemAtPosition(itemPosition = 2)
      clickOnRecyclerViewItemAtPosition(itemPosition = 3)
      clickOnRecyclerViewItemAtPosition(itemPosition = 5)
      clickOnRecyclerViewItemAtPosition(itemPosition = 7)
      clickOnRecyclerViewItemAtPosition(itemPosition = 8)
      clickOnRecyclerViewItemAtPosition(itemPosition = 10)
      clickOnRecyclerViewItemAtPosition(itemPosition = 11)
      clickOnRecyclerViewItemAtPosition(itemPosition = 13)
      clickOnRecyclerViewItemAtPosition(itemPosition = 14)
      onView(withId(R.id.mark_chapters_completed_all_check_box)).check(matches(isChecked()))
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_selectAllChapters_configChange_allCheckBoxIsChecked() {
    launchMarkChaptersCompletedFragmentTestActivity(internalProfileId).use {
      testCoroutineDispatchers.runCurrent()
      clickOnRecyclerViewItemAtPosition(itemPosition = 1)
      clickOnRecyclerViewItemAtPosition(itemPosition = 2)
      clickOnRecyclerViewItemAtPosition(itemPosition = 3)
      clickOnRecyclerViewItemAtPosition(itemPosition = 5)
      clickOnRecyclerViewItemAtPosition(itemPosition = 7)
      clickOnRecyclerViewItemAtPosition(itemPosition = 8)
      clickOnRecyclerViewItemAtPosition(itemPosition = 10)
      clickOnRecyclerViewItemAtPosition(itemPosition = 11)
      clickOnRecyclerViewItemAtPosition(itemPosition = 13)
      clickOnRecyclerViewItemAtPosition(itemPosition = 14)
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.mark_chapters_completed_all_check_box)).check(matches(isChecked()))
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_selectAllChapters_unselectOneChapter_allCheckBoxIsNotChecked() { // ktlint-disable max-line-length
    launchMarkChaptersCompletedFragmentTestActivity(internalProfileId).use {
      testCoroutineDispatchers.runCurrent()
      clickOnRecyclerViewItemAtPosition(itemPosition = 1)
      clickOnRecyclerViewItemAtPosition(itemPosition = 2)
      clickOnRecyclerViewItemAtPosition(itemPosition = 3)
      clickOnRecyclerViewItemAtPosition(itemPosition = 5)
      clickOnRecyclerViewItemAtPosition(itemPosition = 7)
      clickOnRecyclerViewItemAtPosition(itemPosition = 8)
      clickOnRecyclerViewItemAtPosition(itemPosition = 10)
      clickOnRecyclerViewItemAtPosition(itemPosition = 11)
      clickOnRecyclerViewItemAtPosition(itemPosition = 13)
      clickOnRecyclerViewItemAtPosition(itemPosition = 14)
      clickOnRecyclerViewItemAtPosition(itemPosition = 2)
      onView(withId(R.id.mark_chapters_completed_all_check_box)).check(matches(not(isChecked())))
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_selectAllChapters_unselectOneChapter_configChange_allCheckBoxIsNotChecked() { // ktlint-disable max-line-length
    launchMarkChaptersCompletedFragmentTestActivity(internalProfileId).use {
      testCoroutineDispatchers.runCurrent()
      clickOnRecyclerViewItemAtPosition(itemPosition = 1)
      clickOnRecyclerViewItemAtPosition(itemPosition = 2)
      clickOnRecyclerViewItemAtPosition(itemPosition = 3)
      clickOnRecyclerViewItemAtPosition(itemPosition = 5)
      clickOnRecyclerViewItemAtPosition(itemPosition = 7)
      clickOnRecyclerViewItemAtPosition(itemPosition = 8)
      clickOnRecyclerViewItemAtPosition(itemPosition = 10)
      clickOnRecyclerViewItemAtPosition(itemPosition = 11)
      clickOnRecyclerViewItemAtPosition(itemPosition = 13)
      clickOnRecyclerViewItemAtPosition(itemPosition = 14)
      clickOnRecyclerViewItemAtPosition(itemPosition = 2)
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.mark_chapters_completed_all_check_box)).check(matches(not(isChecked())))
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_fractionsFirstChapterIsCompleted_isCheckedAndDisabled() {
    markFractionsFirstChapterCompleted()
    launchMarkChaptersCompletedFragmentTestActivity(internalProfileId).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 7)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.mark_chapters_completed_recycler_view,
          position = 7,
          targetViewId = R.id.mark_chapters_completed_chapter_check_box
        )
      ).check(matches(isChecked())).check(matches(not(isEnabled())))
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_fractionsFirstChapterIsCompleted_configChange_isCheckedAndDisabled() { // ktlint-disable max-line-length
    markFractionsFirstChapterCompleted()
    launchMarkChaptersCompletedFragmentTestActivity(internalProfileId).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 7)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.mark_chapters_completed_recycler_view,
          position = 7,
          targetViewId = R.id.mark_chapters_completed_chapter_check_box
        )
      ).check(matches(isChecked())).check(matches(not(isEnabled())))
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_fractionsFirstChapterIsCompleted_nextChapterIsEnabled() {
    markFractionsFirstChapterCompleted()
    launchMarkChaptersCompletedFragmentTestActivity(internalProfileId).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 8)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.mark_chapters_completed_recycler_view,
          position = 8,
          targetViewId = R.id.mark_chapters_completed_chapter_check_box
        )
      ).check(matches(isEnabled()))
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_fractionsFirstChapterIsCompleted_configChange_nextChapterIsEnabled() { // ktlint-disable max-line-length
    markFractionsFirstChapterCompleted()
    launchMarkChaptersCompletedFragmentTestActivity(internalProfileId).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 8)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.mark_chapters_completed_recycler_view,
          position = 8,
          targetViewId = R.id.mark_chapters_completed_chapter_check_box
        )
      ).check(matches(isEnabled()))
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_clickMarkCompleted_activityFinishes() {
    launchMarkChaptersCompletedFragmentTestActivity(internalProfileId).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.mark_chapters_completed_mark_completed_text_view)).perform(click())

      scenario.onActivity { activity ->
        assertThat(activity.isFinishing).isTrue()
      }
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_land_clickMarkCompleted_activityFinishes() {
    launchMarkChaptersCompletedFragmentTestActivity(internalProfileId).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())

      onView(withId(R.id.mark_chapters_completed_mark_completed_text_view)).perform(click())

      scenario.onActivity { activity ->
        assertThat(activity.isFinishing).isTrue()
      }
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_allLessonsAreCompleted_allCheckboxIsChecked() {
    markAllLessonsCompleted()
    launchMarkChaptersCompletedFragmentTestActivity(internalProfileId).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.mark_chapters_completed_all_check_box)).check(matches(isChecked()))
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_allLessonsAreCompleted_configChange_allCheckboxIsChecked() {
    markAllLessonsCompleted()
    launchMarkChaptersCompletedFragmentTestActivity(internalProfileId).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.mark_chapters_completed_all_check_box)).check(matches(isChecked()))
    }
  }

  @Test
  fun testFragment_showConfirmationTrueInArgs_selectChapters_markCompleted_confirmationShown() {
    launchMarkChaptersCompletedFragmentTestActivity(
      internalProfileId, showConfirmationNotice = true
    ).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      clickOnRecyclerViewItemAtPosition(itemPosition = 1)
      clickOnRecyclerViewItemAtPosition(itemPosition = 2)
      clickOnRecyclerViewItemAtPosition(itemPosition = 3)

      onView(withId(R.id.mark_chapters_completed_mark_completed_text_view)).perform(click())

      // The confirmation notice should show & the activity should not be ending.
      onView(withText(containsString("The following chapters will be marked as finished")))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
      scenario.onActivity { activity ->
        assertThat(activity.isFinishing).isFalse()
      }
    }
  }

  @Test
  fun testFragment_showConfirmationFalseInArgs_selectChapters_markCompleted_confirmationNotShown() {
    launchMarkChaptersCompletedFragmentTestActivity(
      internalProfileId, showConfirmationNotice = false
    ).use {
      testCoroutineDispatchers.runCurrent()
      clickOnRecyclerViewItemAtPosition(itemPosition = 1)
      clickOnRecyclerViewItemAtPosition(itemPosition = 2)
      clickOnRecyclerViewItemAtPosition(itemPosition = 3)

      onView(withId(R.id.mark_chapters_completed_mark_completed_text_view)).perform(click())

      // The notice dialog should not be showing.
      onView(withText(containsString("The following chapters will be marked as finished")))
        .check(doesNotExist())
    }
  }

  @Test
  fun testFragment_withNotice_selectNoChapters_markCompleted_activityClosedWithoutConfirmation() {
    launchMarkChaptersCompletedFragmentTestActivity(
      internalProfileId, showConfirmationNotice = true
    ).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.mark_chapters_completed_mark_completed_text_view)).perform(click())

      // The notice dialog should not be showing & the activity should be ending since there are no
      // lessons being changed.
      onView(withText(containsString("The following chapters will be marked as finished")))
        .check(doesNotExist())
      scenario.onActivity { activity ->
        assertThat(activity.isFinishing).isTrue()
      }
    }
  }

  @Test
  fun testFragment_withNotice_selectOneChapter_markCompleted_confirmationIncludesLessonTitle() {
    launchMarkChaptersCompletedFragmentTestActivity(
      internalProfileId, showConfirmationNotice = true
    ).use {
      testCoroutineDispatchers.runCurrent()

      clickOnRecyclerViewItemAtPosition(itemPosition = 1)
      onView(withId(R.id.mark_chapters_completed_mark_completed_text_view)).perform(click())

      onView(
        withText(
          "The following chapters will be marked as finished: Prototype Exploration. Note that" +
            " this is irreversible and will erase any partial lesson progress."
        )
      ).inRoot(isDialog()).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testFragment_withNotice_selectTwoChapters_markCompleted_confirmationIncludesLessonTitles() {
    launchMarkChaptersCompletedFragmentTestActivity(
      internalProfileId, showConfirmationNotice = true
    ).use {
      testCoroutineDispatchers.runCurrent()

      clickOnRecyclerViewItemAtPosition(itemPosition = 1)
      clickOnRecyclerViewItemAtPosition(itemPosition = 2)
      onView(withId(R.id.mark_chapters_completed_mark_completed_text_view)).perform(click())

      onView(
        withText(
          "The following chapters will be marked as finished: Prototype Exploration and Image" +
            " Region Selection Exploration. Note that this is irreversible and will erase any" +
            " partial lesson progress."
        )
      ).inRoot(isDialog()).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testFragment_withNotice_selectThreeChapters_markCompleted_confirmationIncludesLessonTitles() {
    launchMarkChaptersCompletedFragmentTestActivity(
      internalProfileId, showConfirmationNotice = true
    ).use {
      testCoroutineDispatchers.runCurrent()

      clickOnRecyclerViewItemAtPosition(itemPosition = 1)
      clickOnRecyclerViewItemAtPosition(itemPosition = 2)
      clickOnRecyclerViewItemAtPosition(itemPosition = 3)
      onView(withId(R.id.mark_chapters_completed_mark_completed_text_view)).perform(click())

      onView(
        withText(
          "The following chapters will be marked as finished: Prototype Exploration, Image" +
            " Region Selection Exploration, and Math Expressions. Note that this is" +
            " irreversible and will erase any partial lesson progress."
        )
      ).inRoot(isDialog()).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testFragment_withNotice_selectChapters_markCompleted_cancelConfirmation_activityStillOpen() {
    launchMarkChaptersCompletedFragmentTestActivity(
      internalProfileId, showConfirmationNotice = true
    ).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      clickOnRecyclerViewItemAtPosition(itemPosition = 1)
      clickOnRecyclerViewItemAtPosition(itemPosition = 2)
      onView(withId(R.id.mark_chapters_completed_mark_completed_text_view)).perform(click())

      onView(withText("Cancel")).inRoot(isDialog()).perform(click())

      // The activity should still be open, and the dialog no longer showing.
      onView(withText(containsString("The following chapters will be marked as finished")))
        .check(doesNotExist())
      scenario.onActivity { activity ->
        assertThat(activity.isFinishing).isFalse()
      }
    }
  }

  @Test
  fun testFragment_withNotice_selectChapters_markCompleted_cancelConfirmation_expsNotCompleted() {
    launchMarkChaptersCompletedFragmentTestActivity(
      internalProfileId, showConfirmationNotice = true
    ).use {
      testCoroutineDispatchers.runCurrent()
      clickOnRecyclerViewItemAtPosition(itemPosition = 1)
      clickOnRecyclerViewItemAtPosition(itemPosition = 2)
      onView(withId(R.id.mark_chapters_completed_mark_completed_text_view)).perform(click())

      onView(withText("Cancel")).inRoot(isDialog()).perform(click())

      // The explorations should still be not completed since the confirmation was canceled.
      val exp1PlayState = retrievePlayStateInFirstTestStory(expId = "test_exp_id_2")
      val exp2PlayState = retrievePlayStateInFirstTestStory(expId = "13")
      assertThat(exp1PlayState).isEqualTo(ChapterPlayState.NOT_STARTED)
      assertThat(exp2PlayState).isEqualTo(ChapterPlayState.NOT_STARTED)
    }
  }

  @Test
  fun testFragment_withNotice_selectChapters_markCompleted_confirmSelection_activityClosed() {
    launchMarkChaptersCompletedFragmentTestActivity(
      internalProfileId, showConfirmationNotice = true
    ).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      clickOnRecyclerViewItemAtPosition(itemPosition = 1)
      clickOnRecyclerViewItemAtPosition(itemPosition = 2)
      onView(withId(R.id.mark_chapters_completed_mark_completed_text_view)).perform(click())

      onView(withText("Confirm completions")).inRoot(isDialog()).perform(click())

      // The activity should be ending since the completions were confirmed.
      scenario.onActivity { activity ->
        assertThat(activity.isFinishing).isTrue()
      }
    }
  }

  @Test
  fun testFragment_withNotice_selectChapters_markCompleted_confirmSelection_expsMarkedAsDone() {
    launchMarkChaptersCompletedFragmentTestActivity(
      internalProfileId, showConfirmationNotice = true
    ).use {
      testCoroutineDispatchers.runCurrent()
      clickOnRecyclerViewItemAtPosition(itemPosition = 1)
      clickOnRecyclerViewItemAtPosition(itemPosition = 2)
      onView(withId(R.id.mark_chapters_completed_mark_completed_text_view)).perform(click())

      onView(withText("Confirm completions")).inRoot(isDialog()).perform(click())

      // The lessons should now be marked as completed since the completions were confirmed.
      val exp1PlayState = retrievePlayStateInFirstTestStory(expId = "test_exp_id_2")
      val exp2PlayState = retrievePlayStateInFirstTestStory(expId = "13")
      assertThat(exp1PlayState).isEqualTo(ChapterPlayState.COMPLETED)
      assertThat(exp2PlayState).isEqualTo(ChapterPlayState.COMPLETED)
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_arguments_workingProperly() {
    launchMarkChaptersCompletedFragmentTestActivity(
      internalProfileId, showConfirmationNotice = true
    ).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity ->

        val fragment = activity.supportFragmentManager
          .findFragmentById(R.id.mark_chapters_completed_container) as MarkChaptersCompletedFragment
        val arguments =
          checkNotNull(fragment.arguments) {
            "Expected arguments to be passed to MarkChaptersCompletedFragment"
          }
        val args = arguments.getProto(
          "MarkChaptersCompletedFragment.arguments",
          MarkChaptersCompletedFragmentArguments.getDefaultInstance()
        )
        val receivedInternalProfileId = args?.internalProfileId
        val receivedShowConfirmationNotice = args?.showConfirmationNotice

        assertThat(receivedInternalProfileId).isEqualTo(internalProfileId)
        assertThat(receivedShowConfirmationNotice).isEqualTo(true)
      }
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_saveInstanceState_workingProperly() {
    launchMarkChaptersCompletedFragmentTestActivity(
      internalProfileId, showConfirmationNotice = true
    ).use { scenario ->

      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.mark_chapters_completed_all_check_box_container)).perform(click())
      scenario.onActivity { activity ->

        var fragment = activity.supportFragmentManager
          .findFragmentById(R.id.mark_chapters_completed_container)
          as? MarkChaptersCompletedFragment

        assertThat(fragment).isNotNull()

        val actualSelectedExplorationIds =
          fragment?.markChaptersCompletedFragmentPresenter?.serializableSelectedExplorationIds
        val actualSelectedExplorationTitles =
          fragment?.markChaptersCompletedFragmentPresenter?.serializableSelectedExplorationTitles

        activity.recreate()

        fragment = activity.supportFragmentManager
          .findFragmentById(R.id.mark_chapters_completed_container)
          as? MarkChaptersCompletedFragment

        assertThat(fragment).isNotNull()

        fragment?.let {
          val receivedSelectedExplorationIds =
            it.markChaptersCompletedFragmentPresenter.serializableSelectedExplorationIds
          val receivedSelectedExplorationTitles =
            it.markChaptersCompletedFragmentPresenter.serializableSelectedExplorationTitles

          assertThat(receivedSelectedExplorationIds).isEqualTo(actualSelectedExplorationIds)
          assertThat(receivedSelectedExplorationTitles).isEqualTo(actualSelectedExplorationTitles)
        }
      }
    }
  }

  private fun launchMarkChaptersCompletedFragmentTestActivity(
    internalProfileId: Int,
    showConfirmationNotice: Boolean = false
  ): ActivityScenario<MarkChaptersCompletedTestActivity> {
    return launch(
      MarkChaptersCompletedTestActivity.createMarkChaptersCompletedTestIntent(
        context, internalProfileId, showConfirmationNotice
      )
    )
  }

  private fun verifyItemTextOnRecyclerViewItemAtPosition(
    itemPosition: Int,
    stringToMatch: String,
    targetViewId: Int
  ) {
    scrollToPosition(position = itemPosition)
    onView(
      atPositionOnView(
        recyclerViewId = R.id.mark_chapters_completed_recycler_view,
        position = itemPosition,
        targetViewId = targetViewId
      )
    ).check(matches(withText(stringToMatch)))
  }

  private fun clickOnRecyclerViewItemAtPosition(itemPosition: Int) {
    scrollToPosition(position = itemPosition)
    onView(
      atPositionOnView(
        recyclerViewId = R.id.mark_chapters_completed_recycler_view,
        position = itemPosition,
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
    ).perform(click())
  }

  private fun verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition: Int) {
    scrollToPosition(position = itemPosition)
    onView(
      atPositionOnView(
        recyclerViewId = R.id.mark_chapters_completed_recycler_view,
        position = itemPosition,
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
    ).check(matches(isChecked()))
  }

  private fun verifyItemUncheckedOnRecyclerViewItemAtPosition(itemPosition: Int) {
    scrollToPosition(position = itemPosition)
    onView(
      atPositionOnView(
        recyclerViewId = R.id.mark_chapters_completed_recycler_view,
        position = itemPosition,
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
    ).check(matches(isNotChecked()))
  }

  private fun markFractionsFirstChapterCompleted() {
    storyProgressTestHelper.markCompletedFractionsStory0Exp0(
      profileId,
      timestampOlderThanOneWeek = false
    )
  }

  private fun markAllLessonsCompleted() {
    storyProgressTestHelper.markAllTopicsAsCompleted(
      profileId,
      timestampOlderThanOneWeek = false
    )
  }

  private fun scrollToPosition(position: Int) {
    onView(withId(R.id.mark_chapters_completed_recycler_view)).perform(
      scrollToPosition<ViewHolder>(position)
    )
  }

  private fun retrieveChapterPlayState(
    topicId: String,
    storyId: String,
    expId: String
  ): ChapterPlayState {
    val playStateProvider =
      storyProgressController.retrieveChapterPlayStateByExplorationId(
        profileId, topicId, storyId, expId
      )
    return monitorFactory.waitForNextSuccessfulResult(playStateProvider)
  }

  private fun retrievePlayStateInFirstTestStory(expId: String) =
    retrieveChapterPlayState(topicId = "test_topic_id_0", storyId = "test_story_id_0", expId)

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class, PlatformParameterModule::class,
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
      PlatformParameterSingletonModule::class,
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

    fun inject(markChaptersCompletedFragmentTest: MarkChaptersCompletedFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerMarkChaptersCompletedFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(markChaptersCompletedFragmentTest: MarkChaptersCompletedFragmentTest) {
      component.inject(markChaptersCompletedFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
