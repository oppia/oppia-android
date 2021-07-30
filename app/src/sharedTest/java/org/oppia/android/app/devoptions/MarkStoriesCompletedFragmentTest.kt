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
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.devoptions.markstoriescompleted.testing.MarkStoriesCompletedTestActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.topic.PracticeTabModule
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
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.testing.AccessibilityTestRule
import org.oppia.android.testing.TestLogReportingModule
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
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [MarkStoriesCompletedFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = MarkStoriesCompletedFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class MarkStoriesCompletedFragmentTest {

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
    MarkStoriesCompletedTestActivity::class.java,
    /* initialTouchMode= */ true,
    /* launchActivity= */ false
  )

  @get:Rule
  val accessibilityTestRule = AccessibilityTestRule()

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
  fun testMarkStoriesCompletedFragment_storiesAreShown() {
    launch<MarkStoriesCompletedTestActivity>(
      createMarkStoriesCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      verifyStoryNameOnStorySummaryListItemAtPosition(
        itemPosition = 0,
        stringToMatch = "First Story"
      )
      scrollToPosition(position = 1)
      verifyStoryNameOnStorySummaryListItemAtPosition(
        itemPosition = 1,
        stringToMatch = "Other Interesting Story"
      )
      scrollToPosition(position = 2)
      verifyStoryNameOnStorySummaryListItemAtPosition(
        itemPosition = 2,
        stringToMatch = "Matthew Goes to the Bakery"
      )
      scrollToPosition(position = 3)
      verifyStoryNameOnStorySummaryListItemAtPosition(
        itemPosition = 3,
        stringToMatch = "Ratios: Part 1"
      )
      scrollToPosition(position = 4)
      verifyStoryNameOnStorySummaryListItemAtPosition(
        itemPosition = 4,
        stringToMatch = "Ratios: Part 2"
      )
    }
  }

  @Test
  fun testMarkStoriesCompletedFragment_configChange_storiesAreShown() {
    launch<MarkStoriesCompletedTestActivity>(
      createMarkStoriesCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 0)
      verifyStoryNameOnStorySummaryListItemAtPosition(
        itemPosition = 0,
        stringToMatch = "First Story"
      )
      scrollToPosition(position = 1)
      verifyStoryNameOnStorySummaryListItemAtPosition(
        itemPosition = 1,
        stringToMatch = "Other Interesting Story"
      )
      scrollToPosition(position = 2)
      verifyStoryNameOnStorySummaryListItemAtPosition(
        itemPosition = 2,
        stringToMatch = "Matthew Goes to the Bakery"
      )
      scrollToPosition(position = 3)
      verifyStoryNameOnStorySummaryListItemAtPosition(
        itemPosition = 3,
        stringToMatch = "Ratios: Part 1"
      )
      scrollToPosition(position = 4)
      verifyStoryNameOnStorySummaryListItemAtPosition(
        itemPosition = 4,
        stringToMatch = "Ratios: Part 2"
      )
    }
  }

  @Test
  fun testMarkStoriesCompletedFragment_selectAll_isChecked() {
    launch<MarkStoriesCompletedTestActivity>(
      createMarkStoriesCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.mark_stories_completed_all_check_box_container)).perform(click())
      onView(withId(R.id.mark_stories_completed_all_check_box)).check(matches(isChecked()))
    }
  }

  @Test
  fun testMarkStoriesCompletedFragment_selectAll_configChange_isChecked() {
    launch<MarkStoriesCompletedTestActivity>(
      createMarkStoriesCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.mark_stories_completed_all_check_box_container)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.mark_stories_completed_all_check_box)).check(matches(isChecked()))
    }
  }

  @Test
  fun testMarkStoriesCompletedFragment_selectAll_selectsAllStories() {
    launch<MarkStoriesCompletedTestActivity>(
      createMarkStoriesCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.mark_stories_completed_all_check_box_container)).perform(click())
      scrollToPosition(position = 0)
      verifyItemCheckedOnStorySummaryListItem(itemPosition = 0)
      scrollToPosition(position = 1)
      verifyItemCheckedOnStorySummaryListItem(itemPosition = 1)
      scrollToPosition(position = 2)
      verifyItemCheckedOnStorySummaryListItem(itemPosition = 2)
      scrollToPosition(position = 3)
      verifyItemCheckedOnStorySummaryListItem(itemPosition = 3)
      scrollToPosition(position = 4)
      verifyItemCheckedOnStorySummaryListItem(itemPosition = 4)
    }
  }

  @Test
  fun testMarkStoriesCompletedFragment_selectAll_configChange_selectsAllStories() {
    launch<MarkStoriesCompletedTestActivity>(
      createMarkStoriesCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.mark_stories_completed_all_check_box_container)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 0)
      verifyItemCheckedOnStorySummaryListItem(itemPosition = 0)
      scrollToPosition(position = 1)
      verifyItemCheckedOnStorySummaryListItem(itemPosition = 1)
      scrollToPosition(position = 2)
      verifyItemCheckedOnStorySummaryListItem(itemPosition = 2)
      scrollToPosition(position = 3)
      verifyItemCheckedOnStorySummaryListItem(itemPosition = 3)
      scrollToPosition(position = 4)
      verifyItemCheckedOnStorySummaryListItem(itemPosition = 4)
    }
  }

  @Test
  fun testMarkStoriesCompletedFragment_selectStories_storiesAreChecked() {
    launch<MarkStoriesCompletedTestActivity>(
      createMarkStoriesCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      performItemCheckOnStorySummaryListItem(itemPosition = 0)
      scrollToPosition(position = 1)
      performItemCheckOnStorySummaryListItem(itemPosition = 1)
      scrollToPosition(position = 2)
      performItemCheckOnStorySummaryListItem(itemPosition = 2)
      scrollToPosition(position = 3)
      performItemCheckOnStorySummaryListItem(itemPosition = 3)
      scrollToPosition(position = 4)
      performItemCheckOnStorySummaryListItem(itemPosition = 4)
      scrollToPosition(position = 0)
      verifyItemCheckedOnStorySummaryListItem(itemPosition = 0)
      scrollToPosition(position = 1)
      verifyItemCheckedOnStorySummaryListItem(itemPosition = 1)
      scrollToPosition(position = 2)
      verifyItemCheckedOnStorySummaryListItem(itemPosition = 2)
      scrollToPosition(position = 3)
      verifyItemCheckedOnStorySummaryListItem(itemPosition = 3)
      scrollToPosition(position = 4)
      verifyItemCheckedOnStorySummaryListItem(itemPosition = 4)
    }
  }

  @Test
  fun testMarkStoriesCompletedFragment_selectStories_configChange_storiesAreChecked() {
    launch<MarkStoriesCompletedTestActivity>(
      createMarkStoriesCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      performItemCheckOnStorySummaryListItem(itemPosition = 0)
      scrollToPosition(position = 1)
      performItemCheckOnStorySummaryListItem(itemPosition = 1)
      scrollToPosition(position = 2)
      performItemCheckOnStorySummaryListItem(itemPosition = 2)
      scrollToPosition(position = 3)
      performItemCheckOnStorySummaryListItem(itemPosition = 3)
      scrollToPosition(position = 4)
      performItemCheckOnStorySummaryListItem(itemPosition = 4)
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 0)
      verifyItemCheckedOnStorySummaryListItem(itemPosition = 0)
      scrollToPosition(position = 1)
      verifyItemCheckedOnStorySummaryListItem(itemPosition = 1)
      scrollToPosition(position = 2)
      verifyItemCheckedOnStorySummaryListItem(itemPosition = 2)
      scrollToPosition(position = 3)
      verifyItemCheckedOnStorySummaryListItem(itemPosition = 3)
      scrollToPosition(position = 4)
      verifyItemCheckedOnStorySummaryListItem(itemPosition = 4)
    }
  }

  @Test
  fun testMarkStoriesCompletedFragment_selectAllStories_allCheckBoxIsChecked() {
    launch<MarkStoriesCompletedTestActivity>(
      createMarkStoriesCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      performItemCheckOnStorySummaryListItem(itemPosition = 0)
      scrollToPosition(position = 1)
      performItemCheckOnStorySummaryListItem(itemPosition = 1)
      scrollToPosition(position = 2)
      performItemCheckOnStorySummaryListItem(itemPosition = 2)
      scrollToPosition(position = 3)
      performItemCheckOnStorySummaryListItem(itemPosition = 3)
      scrollToPosition(position = 4)
      performItemCheckOnStorySummaryListItem(itemPosition = 4)
      onView(withId(R.id.mark_stories_completed_all_check_box)).check(matches(isChecked()))
    }
  }

  @Test
  fun testMarkStoriesCompletedFragment_selectAllStories_configChange__allCheckBoxIsChecked() {
    launch<MarkStoriesCompletedTestActivity>(
      createMarkStoriesCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      performItemCheckOnStorySummaryListItem(itemPosition = 0)
      scrollToPosition(position = 1)
      performItemCheckOnStorySummaryListItem(itemPosition = 1)
      scrollToPosition(position = 2)
      performItemCheckOnStorySummaryListItem(itemPosition = 2)
      scrollToPosition(position = 3)
      performItemCheckOnStorySummaryListItem(itemPosition = 3)
      scrollToPosition(position = 4)
      performItemCheckOnStorySummaryListItem(itemPosition = 4)
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.mark_stories_completed_all_check_box)).check(matches(isChecked()))
    }
  }

  @Test
  fun testMarkStoriesCompletedFragment_selectAllStories_unselectOneStory_allCheckBoxIsNotChecked() {
    launch<MarkStoriesCompletedTestActivity>(
      createMarkStoriesCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      performItemCheckOnStorySummaryListItem(itemPosition = 0)
      scrollToPosition(position = 1)
      performItemCheckOnStorySummaryListItem(itemPosition = 1)
      scrollToPosition(position = 2)
      performItemCheckOnStorySummaryListItem(itemPosition = 2)
      scrollToPosition(position = 3)
      performItemCheckOnStorySummaryListItem(itemPosition = 3)
      scrollToPosition(position = 4)
      performItemCheckOnStorySummaryListItem(itemPosition = 4)
      scrollToPosition(position = 2)
      performItemCheckOnStorySummaryListItem(itemPosition = 2)
      onView(withId(R.id.mark_stories_completed_all_check_box)).check(matches(not(isChecked())))
    }
  }

  @Test
  fun testMarkStoriesCompletedFragment_selectAllStories_unselectOneStory_configChange_allCheckBoxIsNotChecked() { // ktlint-disable max-line-length
    launch<MarkStoriesCompletedTestActivity>(
      createMarkStoriesCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      performItemCheckOnStorySummaryListItem(itemPosition = 0)
      scrollToPosition(position = 1)
      performItemCheckOnStorySummaryListItem(itemPosition = 1)
      scrollToPosition(position = 2)
      performItemCheckOnStorySummaryListItem(itemPosition = 2)
      scrollToPosition(position = 3)
      performItemCheckOnStorySummaryListItem(itemPosition = 3)
      scrollToPosition(position = 4)
      performItemCheckOnStorySummaryListItem(itemPosition = 4)
      scrollToPosition(position = 2)
      performItemCheckOnStorySummaryListItem(itemPosition = 2)
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.mark_stories_completed_all_check_box)).check(matches(not(isChecked())))
    }
  }

  @Test
  fun testMarkStoriesCompletedFragment_ratiosFirstStoryIsCompleted_isCheckedAndDisabled() {
    markRatiosFirstStoryCompleted()
    launch<MarkStoriesCompletedTestActivity>(
      createMarkStoriesCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 3)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.mark_stories_completed_recycler_view,
          position = 3,
          targetViewId = R.id.mark_stories_completed_story_check_box
        )
      ).check(matches(isChecked())).check(matches(not(isEnabled())))
    }
  }

  @Test
  fun testMarkStoriesCompletedFragment_ratiosFirstStoryIsCompleted_configChange_isCheckedAndDisabled() { // ktlint-disable max-line-length
    markRatiosFirstStoryCompleted()
    launch<MarkStoriesCompletedTestActivity>(
      createMarkStoriesCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 3)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.mark_stories_completed_recycler_view,
          position = 3,
          targetViewId = R.id.mark_stories_completed_story_check_box
        )
      ).check(matches(isChecked())).check(matches(not(isEnabled())))
    }
  }

  @Test
  fun testMarkStoriesCompletedFragment_clickMarkCompleted_activityFinishes() {
    activityTestRule.launchActivity(createMarkStoriesCompletedTestActivityIntent(internalProfileId))
    testCoroutineDispatchers.runCurrent()
    onView(withId(R.id.mark_stories_completed_mark_completed_text_view)).perform(click())
    assertThat(activityTestRule.activity.isFinishing).isTrue()
  }

  @Test
  fun testMarkStoriesCompletedFragment_land_clickMarkCompleted_activityFinishes() {
    activityTestRule.launchActivity(createMarkStoriesCompletedTestActivityIntent(internalProfileId))
    testCoroutineDispatchers.runCurrent()
    onView(isRoot()).perform(orientationLandscape())
    onView(withId(R.id.mark_stories_completed_mark_completed_text_view)).perform(click())
    assertThat(activityTestRule.activity.isFinishing).isTrue()
  }

  @Test
  fun testMarkStoriesCompletedFragment_allLessonsAreCompleted_allCheckboxIsChecked() {
    markAllLessonsCompleted()
    launch<MarkStoriesCompletedTestActivity>(
      createMarkStoriesCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.mark_stories_completed_all_check_box)).check(matches(isChecked()))
    }
  }

  @Test
  fun testMarkStoriesCompletedFragment_allLessonsAreCompleted_configChange_allCheckboxIsChecked() {
    markAllLessonsCompleted()
    launch<MarkStoriesCompletedTestActivity>(
      createMarkStoriesCompletedTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.mark_stories_completed_all_check_box)).check(matches(isChecked()))
    }
  }

  private fun createMarkStoriesCompletedTestActivityIntent(internalProfileId: Int): Intent {
    return MarkStoriesCompletedTestActivity.createMarkStoriesCompletedTestIntent(
      context, internalProfileId
    )
  }

  private fun verifyStoryNameOnStorySummaryListItemAtPosition(
    itemPosition: Int,
    stringToMatch: String
  ) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.mark_stories_completed_recycler_view,
        position = itemPosition,
        targetViewId = R.id.mark_stories_completed_story_check_box
      )
    ).check(matches(withText(stringToMatch)))
  }

  private fun verifyItemCheckedOnStorySummaryListItem(itemPosition: Int) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.mark_stories_completed_recycler_view,
        position = itemPosition,
        targetViewId = R.id.mark_stories_completed_story_check_box
      )
    ).check(matches(isChecked()))
  }

  private fun performItemCheckOnStorySummaryListItem(itemPosition: Int) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.mark_stories_completed_recycler_view,
        position = itemPosition,
        targetViewId = R.id.mark_stories_completed_story_check_box
      )
    ).perform(click())
  }

  private fun markRatiosFirstStoryCompleted() {
    storyProgressTestHelper.markCompletedRatiosStory0(
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
    onView(withId(R.id.mark_stories_completed_recycler_view)).perform(
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

    fun inject(markStoriesCompletedFragmentTest: MarkStoriesCompletedFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerMarkStoriesCompletedFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(markStoriesCompletedFragmentTest: MarkStoriesCompletedFragmentTest) {
      component.inject(markStoriesCompletedFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
