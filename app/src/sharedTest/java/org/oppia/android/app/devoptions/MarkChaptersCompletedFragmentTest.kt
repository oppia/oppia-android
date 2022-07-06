package org.oppia.android.app.devoptions

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.devoptions.markchapterscompleted.testing.MarkChaptersCompletedTestActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.topic.PracticeTabModule
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
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
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

  private val internalProfileId = 0
  private lateinit var profileId: ProfileId

  @Inject
  lateinit var storyProgressTestHelper: StoryProgressTestHelper

  @Inject
  lateinit var fakeOppiaClock: FakeOppiaClock

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var context: Context

  @get:Rule
  val activityTestRule = ActivityTestRule(
    MarkChaptersCompletedTestActivity::class.java,
    /* initialTouchMode= */ true,
    /* launchActivity= */ false
  )

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

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
    launch<MarkChaptersCompletedTestActivity>(
      createMarkChaptersCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 0,
        stringToMatch = "First Story",
        targetViewId = R.id.mark_chapters_completed_story_name_text_view
      )
      scrollToPosition(position = 4)
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 4,
        stringToMatch = "Other Interesting Story",
        targetViewId = R.id.mark_chapters_completed_story_name_text_view
      )
      scrollToPosition(position = 6)
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 6,
        stringToMatch = "Matthew Goes to the Bakery",
        targetViewId = R.id.mark_chapters_completed_story_name_text_view
      )
      scrollToPosition(position = 9)
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 9,
        stringToMatch = "Ratios: Part 1",
        targetViewId = R.id.mark_chapters_completed_story_name_text_view
      )
      scrollToPosition(position = 12)
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 12,
        stringToMatch = "Ratios: Part 2",
        targetViewId = R.id.mark_chapters_completed_story_name_text_view
      )
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_configChange_storiesAreShown() {
    launch<MarkChaptersCompletedTestActivity>(
      createMarkChaptersCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 0)
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 0,
        stringToMatch = "First Story",
        targetViewId = R.id.mark_chapters_completed_story_name_text_view
      )
      scrollToPosition(position = 4)
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 4,
        stringToMatch = "Other Interesting Story",
        targetViewId = R.id.mark_chapters_completed_story_name_text_view
      )
      scrollToPosition(position = 6)
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 6,
        stringToMatch = "Matthew Goes to the Bakery",
        targetViewId = R.id.mark_chapters_completed_story_name_text_view
      )
      scrollToPosition(position = 9)
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 9,
        stringToMatch = "Ratios: Part 1",
        targetViewId = R.id.mark_chapters_completed_story_name_text_view
      )
      scrollToPosition(position = 12)
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 12,
        stringToMatch = "Ratios: Part 2",
        targetViewId = R.id.mark_chapters_completed_story_name_text_view
      )
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_chaptersAreShown() {
    launch<MarkChaptersCompletedTestActivity>(
      createMarkChaptersCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 1,
        stringToMatch = "Prototype Exploration",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      scrollToPosition(position = 2)
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 2,
        stringToMatch = "Image Region Selection Exploration",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      scrollToPosition(position = 3)
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 3,
        stringToMatch = "Math Expressions",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      scrollToPosition(position = 5)
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 5,
        stringToMatch = "Fifth Exploration",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      scrollToPosition(position = 7)
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 7,
        stringToMatch = "What is a Fraction?",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      scrollToPosition(position = 8)
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 8,
        stringToMatch = "The Meaning of Equal Parts",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      scrollToPosition(position = 10)
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 10,
        stringToMatch = "What is a Ratio?",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      scrollToPosition(position = 11)
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 11,
        stringToMatch = "Order is important",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      scrollToPosition(position = 13)
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 13,
        stringToMatch = "Equivalent Ratios",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      scrollToPosition(position = 14)
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 14,
        stringToMatch = "Writing Ratios in Simplest Form",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_configChange_chaptersAreShown() {
    launch<MarkChaptersCompletedTestActivity>(
      createMarkChaptersCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 1)
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 1,
        stringToMatch = "Prototype Exploration",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      scrollToPosition(position = 2)
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 2,
        stringToMatch = "Image Region Selection Exploration",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      scrollToPosition(position = 3)
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 3,
        stringToMatch = "Math Expressions",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      scrollToPosition(position = 5)
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 5,
        stringToMatch = "Fifth Exploration",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      scrollToPosition(position = 7)
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 7,
        stringToMatch = "What is a Fraction?",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      scrollToPosition(position = 8)
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 8,
        stringToMatch = "The Meaning of Equal Parts",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      scrollToPosition(position = 10)
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 10,
        stringToMatch = "What is a Ratio?",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      scrollToPosition(position = 11)
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 11,
        stringToMatch = "Order is important",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      scrollToPosition(position = 13)
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 13,
        stringToMatch = "Equivalent Ratios",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
      scrollToPosition(position = 14)
      verifyItemTextOnRecyclerViewItemAtPosition(
        itemPosition = 14,
        stringToMatch = "Writing Ratios in Simplest Form",
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_selectAll_isChecked() {
    launch<MarkChaptersCompletedTestActivity>(
      createMarkChaptersCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.mark_chapters_completed_all_check_box_container)).perform(click())
      onView(withId(R.id.mark_chapters_completed_all_check_box)).check(matches(isChecked()))
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_selectAll_configChange_isChecked() {
    launch<MarkChaptersCompletedTestActivity>(
      createMarkChaptersCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.mark_chapters_completed_all_check_box_container)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.mark_chapters_completed_all_check_box)).check(matches(isChecked()))
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_selectAll_selectsAllChapters() {
    launch<MarkChaptersCompletedTestActivity>(
      createMarkChaptersCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.mark_chapters_completed_all_check_box_container)).perform(click())
      scrollToPosition(position = 1)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 1)
      scrollToPosition(position = 2)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 2)
      scrollToPosition(position = 3)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 3)
      scrollToPosition(position = 5)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 5)
      scrollToPosition(position = 7)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 7)
      scrollToPosition(position = 8)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 8)
      scrollToPosition(position = 10)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 10)
      scrollToPosition(position = 11)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 11)
      scrollToPosition(position = 13)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 13)
      scrollToPosition(position = 14)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 14)
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_unSelectAll_unSelectsAllChapters() {
    launch<MarkChaptersCompletedTestActivity>(
      createMarkChaptersCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.mark_chapters_completed_all_check_box_container)).perform(click())
      scrollToPosition(position = 1)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 1)
      scrollToPosition(position = 2)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 2)
      scrollToPosition(position = 3)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 3)
      scrollToPosition(position = 5)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 5)
      scrollToPosition(position = 7)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 7)
      scrollToPosition(position = 8)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 8)
      scrollToPosition(position = 10)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 10)
      scrollToPosition(position = 11)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 11)
      scrollToPosition(position = 13)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 13)
      scrollToPosition(position = 14)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 14)
      onView(withId(R.id.mark_chapters_completed_all_check_box_container)).perform(click())
      scrollToPosition(position = 1)
      verifyItemUnCheckedOnRecyclerViewItemAtPosition(itemPosition = 1)
      scrollToPosition(position = 2)
      verifyItemUnCheckedOnRecyclerViewItemAtPosition(itemPosition = 2)
      scrollToPosition(position = 3)
      verifyItemUnCheckedOnRecyclerViewItemAtPosition(itemPosition = 3)
      scrollToPosition(position = 5)
      verifyItemUnCheckedOnRecyclerViewItemAtPosition(itemPosition = 5)
      scrollToPosition(position = 7)
      verifyItemUnCheckedOnRecyclerViewItemAtPosition(itemPosition = 7)
      scrollToPosition(position = 8)
      verifyItemUnCheckedOnRecyclerViewItemAtPosition(itemPosition = 8)
      scrollToPosition(position = 10)
      verifyItemUnCheckedOnRecyclerViewItemAtPosition(itemPosition = 10)
      scrollToPosition(position = 11)
      verifyItemUnCheckedOnRecyclerViewItemAtPosition(itemPosition = 11)
      scrollToPosition(position = 13)
      verifyItemUnCheckedOnRecyclerViewItemAtPosition(itemPosition = 13)
      scrollToPosition(position = 14)
      verifyItemUnCheckedOnRecyclerViewItemAtPosition(itemPosition = 14)
      println("asserts completed")
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_selectAll_configChange_selectsAllChapters() {
    launch<MarkChaptersCompletedTestActivity>(
      createMarkChaptersCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.mark_chapters_completed_all_check_box_container)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 1)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 1)
      scrollToPosition(position = 2)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 2)
      scrollToPosition(position = 3)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 3)
      scrollToPosition(position = 5)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 5)
      scrollToPosition(position = 7)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 7)
      scrollToPosition(position = 8)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 8)
      scrollToPosition(position = 10)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 10)
      scrollToPosition(position = 11)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 11)
      scrollToPosition(position = 13)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 13)
      scrollToPosition(position = 14)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 14)
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_selectChaptersOfFirstStoryInOrder_chaptersAreChecked() {
    launch<MarkChaptersCompletedTestActivity>(
      createMarkChaptersCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 1)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 1)
      scrollToPosition(position = 2)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 2)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 2)
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_selectChaptersOfFirstStoryInOrder_configChange_chaptersAreChecked() { // ktlint-disable max-line-length
    launch<MarkChaptersCompletedTestActivity>(
      createMarkChaptersCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 1)
      scrollToPosition(position = 2)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 2)
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 1)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 1)
      scrollToPosition(position = 2)
      verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition = 2)
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_firstStory_firstChapterUnchecked_secondChapterIsDisabled() {
    launch<MarkChaptersCompletedTestActivity>(
      createMarkChaptersCompletedTestActivityIntent(internalProfileId)
    ).use {
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
    launch<MarkChaptersCompletedTestActivity>(
      createMarkChaptersCompletedTestActivityIntent(internalProfileId)
    ).use {
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
    launch<MarkChaptersCompletedTestActivity>(
      createMarkChaptersCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 1)
      scrollToPosition(position = 2)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 2)
      scrollToPosition(position = 1)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 1)
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
  fun testMarkChaptersCompletedFragment_firstStory_selectChaptersInOrder_unselectFirstChapter_configChange_secondChapterIsDisabled() { // ktlint-disable max-line-length
    launch<MarkChaptersCompletedTestActivity>(
      createMarkChaptersCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 1)
      scrollToPosition(position = 2)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 2)
      scrollToPosition(position = 1)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 1)
      onView(isRoot()).perform(orientationLandscape())
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
  fun testMarkChaptersCompletedFragment_selectAllChapters_allCheckBoxIsChecked() {
    launch<MarkChaptersCompletedTestActivity>(
      createMarkChaptersCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 1)
      scrollToPosition(position = 2)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 2)
      scrollToPosition(position = 3)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 3)
      scrollToPosition(position = 5)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 5)
      scrollToPosition(position = 7)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 7)
      scrollToPosition(position = 8)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 8)
      scrollToPosition(position = 10)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 10)
      scrollToPosition(position = 11)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 11)
      scrollToPosition(position = 13)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 13)
      scrollToPosition(position = 14)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 14)
      onView(withId(R.id.mark_chapters_completed_all_check_box)).check(matches(isChecked()))
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_selectAllChapters_configChange_allCheckBoxIsChecked() {
    launch<MarkChaptersCompletedTestActivity>(
      createMarkChaptersCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 1)
      scrollToPosition(position = 2)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 2)
      scrollToPosition(position = 3)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 3)
      scrollToPosition(position = 5)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 5)
      scrollToPosition(position = 7)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 7)
      scrollToPosition(position = 8)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 8)
      scrollToPosition(position = 10)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 10)
      scrollToPosition(position = 11)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 11)
      scrollToPosition(position = 13)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 13)
      scrollToPosition(position = 14)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 14)
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.mark_chapters_completed_all_check_box)).check(matches(isChecked()))
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_selectAllChapters_unselectOneChapter_allCheckBoxIsNotChecked() { // ktlint-disable max-line-length
    launch<MarkChaptersCompletedTestActivity>(
      createMarkChaptersCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 1)
      scrollToPosition(position = 2)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 2)
      scrollToPosition(position = 3)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 3)
      scrollToPosition(position = 5)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 5)
      scrollToPosition(position = 7)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 7)
      scrollToPosition(position = 8)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 8)
      scrollToPosition(position = 10)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 10)
      scrollToPosition(position = 11)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 11)
      scrollToPosition(position = 13)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 13)
      scrollToPosition(position = 14)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 14)
      scrollToPosition(position = 2)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 2)
      onView(withId(R.id.mark_chapters_completed_all_check_box)).check(matches(not(isChecked())))
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_selectAllChapters_unselectOneChapter_configChange_allCheckBoxIsNotChecked() { // ktlint-disable max-line-length
    launch<MarkChaptersCompletedTestActivity>(
      createMarkChaptersCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 1)
      scrollToPosition(position = 2)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 2)
      scrollToPosition(position = 3)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 3)
      scrollToPosition(position = 5)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 5)
      scrollToPosition(position = 7)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 7)
      scrollToPosition(position = 8)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 8)
      scrollToPosition(position = 10)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 10)
      scrollToPosition(position = 11)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 11)
      scrollToPosition(position = 13)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 13)
      scrollToPosition(position = 14)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 14)
      scrollToPosition(position = 2)
      performItemCheckOnRecyclerViewItemAtPosition(itemPosition = 2)
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.mark_chapters_completed_all_check_box)).check(matches(not(isChecked())))
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_fractionsFirstChapterIsCompleted_isCheckedAndDisabled() {
    markFractionsFirstChapterCompleted()
    launch<MarkChaptersCompletedTestActivity>(
      createMarkChaptersCompletedTestActivityIntent(internalProfileId)
    ).use {
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
    launch<MarkChaptersCompletedTestActivity>(
      createMarkChaptersCompletedTestActivityIntent(internalProfileId)
    ).use {
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
    launch<MarkChaptersCompletedTestActivity>(
      createMarkChaptersCompletedTestActivityIntent(internalProfileId)
    ).use {
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
    launch<MarkChaptersCompletedTestActivity>(
      createMarkChaptersCompletedTestActivityIntent(internalProfileId)
    ).use {
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
    activityTestRule.launchActivity(
      createMarkChaptersCompletedTestActivityIntent(internalProfileId)
    )
    testCoroutineDispatchers.runCurrent()
    onView(withId(R.id.mark_chapters_completed_mark_completed_text_view)).perform(click())
    assertThat(activityTestRule.activity.isFinishing).isTrue()
  }

  @Test
  fun testMarkChaptersCompletedFragment_land_clickMarkCompleted_activityFinishes() {
    activityTestRule.launchActivity(
      createMarkChaptersCompletedTestActivityIntent(internalProfileId)
    )
    testCoroutineDispatchers.runCurrent()
    onView(isRoot()).perform(orientationLandscape())
    onView(withId(R.id.mark_chapters_completed_mark_completed_text_view)).perform(click())
    assertThat(activityTestRule.activity.isFinishing).isTrue()
  }

  @Test
  fun testMarkChaptersCompletedFragment_allLessonsAreCompleted_allCheckboxIsChecked() {
    markAllLessonsCompleted()
    launch<MarkChaptersCompletedTestActivity>(
      createMarkChaptersCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.mark_chapters_completed_all_check_box)).check(matches(isChecked()))
    }
  }

  @Test
  fun testMarkChaptersCompletedFragment_allLessonsAreCompleted_configChange_allCheckboxIsChecked() {
    markAllLessonsCompleted()
    launch<MarkChaptersCompletedTestActivity>(
      createMarkChaptersCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.mark_chapters_completed_all_check_box)).check(matches(isChecked()))
    }
  }

  private fun createMarkChaptersCompletedTestActivityIntent(internalProfileId: Int): Intent {
    return MarkChaptersCompletedTestActivity.createMarkChaptersCompletedTestIntent(
      context, internalProfileId
    )
  }

  private fun verifyItemTextOnRecyclerViewItemAtPosition(
    itemPosition: Int,
    stringToMatch: String,
    targetViewId: Int
  ) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.mark_chapters_completed_recycler_view,
        position = itemPosition,
        targetViewId = targetViewId
      )
    ).check(matches(withText(stringToMatch)))
  }

  private fun performItemCheckOnRecyclerViewItemAtPosition(itemPosition: Int) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.mark_chapters_completed_recycler_view,
        position = itemPosition,
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
    ).perform(click())
  }

  private fun verifyItemCheckedOnRecyclerViewItemAtPosition(itemPosition: Int) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.mark_chapters_completed_recycler_view,
        position = itemPosition,
        targetViewId = R.id.mark_chapters_completed_chapter_check_box
      )
    ).check(matches(isChecked()))
  }

  private fun verifyItemUnCheckedOnRecyclerViewItemAtPosition(itemPosition: Int) {
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
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class, WorkManagerConfigurationModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      HintsAndSolutionConfigModule::class, HintsAndSolutionProdModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class, PracticeTabModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, NetworkConfigProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      PlatformParameterSingletonModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, SplitScreenInteractionModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

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
