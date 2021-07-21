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
import org.oppia.android.app.devoptions.marktopicscompleted.MarkTopicsCompletedActivity
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
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

// TODO(#3422): Separate MarkTopicsCompletedActivityTest into activity and fragment test files.
/** Tests for [MarkTopicsCompletedActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = MarkTopicsCompletedActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class MarkTopicsCompletedActivityTest {

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
    MarkTopicsCompletedActivity::class.java,
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
  fun testMarkTopicsCompletedActivity_hasCorrectActivityLabel() {
    activityTestRule.launchActivity(createMarkTopicsCompletedActivityIntent(internalProfileId))
    val title = activityTestRule.activity.title

    // Verify that the activity label is correct as a proxy to verify TalkBack will announce the
    // correct string when it's read out.
    assertThat(title).isEqualTo(context.getString(R.string.mark_topics_completed_activity_title))
  }

  @Test
  fun testMarkTopicsCompletedActivity_topicsAreShown() {
    launch<MarkTopicsCompletedActivity>(
      createMarkTopicsCompletedActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      verifyTopicNameOnTopicSummaryListItemAtPosition(
        itemPosition = 0,
        stringToMatch = "First Test Topic"
      )
      scrollToPosition(position = 1)
      verifyTopicNameOnTopicSummaryListItemAtPosition(
        itemPosition = 1,
        stringToMatch = "Second Test Topic"
      )
      scrollToPosition(position = 2)
      verifyTopicNameOnTopicSummaryListItemAtPosition(
        itemPosition = 2,
        stringToMatch = "Fractions"
      )
      scrollToPosition(position = 3)
      verifyTopicNameOnTopicSummaryListItemAtPosition(
        itemPosition = 3,
        stringToMatch = "Ratios and Proportional Reasoning"
      )
    }
  }

  @Test
  fun testMarkTopicsCompletedActivity_configChange_topicsAreShown() {
    launch<MarkTopicsCompletedActivity>(
      createMarkTopicsCompletedActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 0)
      verifyTopicNameOnTopicSummaryListItemAtPosition(
        itemPosition = 0,
        stringToMatch = "First Test Topic"
      )
      scrollToPosition(position = 1)
      verifyTopicNameOnTopicSummaryListItemAtPosition(
        itemPosition = 1,
        stringToMatch = "Second Test Topic"
      )
      scrollToPosition(position = 2)
      verifyTopicNameOnTopicSummaryListItemAtPosition(
        itemPosition = 2,
        stringToMatch = "Fractions"
      )
      scrollToPosition(position = 3)
      verifyTopicNameOnTopicSummaryListItemAtPosition(
        itemPosition = 3,
        stringToMatch = "Ratios and Proportional Reasoning"
      )
    }
  }

  @Test
  fun testMarkTopicsCompletedActivity_selectAll_isChecked() {
    launch<MarkTopicsCompletedActivity>(
      createMarkTopicsCompletedActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.mark_topics_completed_all_check_box_container)).perform(click())
      onView(withId(R.id.mark_topics_completed_all_check_box)).check(matches(isChecked()))
    }
  }

  @Test
  fun testMarkTopicsCompletedActivity_selectAll_configChange_isChecked() {
    launch<MarkTopicsCompletedActivity>(
      createMarkTopicsCompletedActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.mark_topics_completed_all_check_box_container)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.mark_topics_completed_all_check_box)).check(matches(isChecked()))
    }
  }

  @Test
  fun testMarkTopicsCompletedActivity_selectAll_selectsAllTopics() {
    launch<MarkTopicsCompletedActivity>(
      createMarkTopicsCompletedActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.mark_topics_completed_all_check_box_container)).perform(click())
      scrollToPosition(position = 0)
      performItemCheckOnTopicSummaryListItem(itemPosition = 0)
      scrollToPosition(position = 1)
      performItemCheckOnTopicSummaryListItem(itemPosition = 1)
      scrollToPosition(position = 2)
      performItemCheckOnTopicSummaryListItem(itemPosition = 2)
      scrollToPosition(position = 3)
      performItemCheckOnTopicSummaryListItem(itemPosition = 3)
    }
  }

  @Test
  fun testMarkTopicsCompletedActivity_selectAll_configChange_selectsAllTopics() {
    launch<MarkTopicsCompletedActivity>(
      createMarkTopicsCompletedActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.mark_topics_completed_all_check_box_container)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 0)
      performItemCheckOnTopicSummaryListItem(itemPosition = 0)
      scrollToPosition(position = 1)
      performItemCheckOnTopicSummaryListItem(itemPosition = 1)
      scrollToPosition(position = 2)
      performItemCheckOnTopicSummaryListItem(itemPosition = 2)
      scrollToPosition(position = 3)
      performItemCheckOnTopicSummaryListItem(itemPosition = 3)
    }
  }

  @Test
  fun testMarkTopicsCompletedActivity_selectTopics_topicsAreChecked() {
    launch<MarkTopicsCompletedActivity>(
      createMarkTopicsCompletedActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      performItemCheckOnTopicSummaryListItem(itemPosition = 0)
      scrollToPosition(position = 1)
      performItemCheckOnTopicSummaryListItem(itemPosition = 1)
      scrollToPosition(position = 2)
      performItemCheckOnTopicSummaryListItem(itemPosition = 2)
      scrollToPosition(position = 3)
      performItemCheckOnTopicSummaryListItem(itemPosition = 3)
      scrollToPosition(position = 0)
      verifyItemCheckedOnTopicSummaryListItem(itemPosition = 0)
      scrollToPosition(position = 1)
      verifyItemCheckedOnTopicSummaryListItem(itemPosition = 1)
      scrollToPosition(position = 2)
      verifyItemCheckedOnTopicSummaryListItem(itemPosition = 2)
      scrollToPosition(position = 3)
      verifyItemCheckedOnTopicSummaryListItem(itemPosition = 3)
    }
  }

  @Test
  fun testMarkTopicsCompletedActivity_selectTopics_configChange_topicsAreChecked() {
    launch<MarkTopicsCompletedActivity>(
      createMarkTopicsCompletedActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      performItemCheckOnTopicSummaryListItem(itemPosition = 0)
      scrollToPosition(position = 1)
      performItemCheckOnTopicSummaryListItem(itemPosition = 1)
      scrollToPosition(position = 2)
      performItemCheckOnTopicSummaryListItem(itemPosition = 2)
      scrollToPosition(position = 3)
      performItemCheckOnTopicSummaryListItem(itemPosition = 3)
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 0)
      verifyItemCheckedOnTopicSummaryListItem(itemPosition = 0)
      scrollToPosition(position = 1)
      verifyItemCheckedOnTopicSummaryListItem(itemPosition = 1)
      scrollToPosition(position = 2)
      verifyItemCheckedOnTopicSummaryListItem(itemPosition = 2)
      scrollToPosition(position = 3)
      verifyItemCheckedOnTopicSummaryListItem(itemPosition = 3)
    }
  }

  @Test
  fun testMarkTopicsCompletedActivity_selectAllTopics_allCheckBoxIsChecked() {
    launch<MarkTopicsCompletedActivity>(
      createMarkTopicsCompletedActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      performItemCheckOnTopicSummaryListItem(itemPosition = 0)
      scrollToPosition(position = 1)
      performItemCheckOnTopicSummaryListItem(itemPosition = 1)
      scrollToPosition(position = 2)
      performItemCheckOnTopicSummaryListItem(itemPosition = 2)
      scrollToPosition(position = 3)
      performItemCheckOnTopicSummaryListItem(itemPosition = 3)
      onView(withId(R.id.mark_topics_completed_all_check_box)).check(matches(isChecked()))
    }
  }

  @Test
  fun testMarkTopicsCompletedActivity_selectAllTopics_configChange_allCheckBoxIsChecked() {
    launch<MarkTopicsCompletedActivity>(
      createMarkTopicsCompletedActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      performItemCheckOnTopicSummaryListItem(itemPosition = 0)
      scrollToPosition(position = 1)
      performItemCheckOnTopicSummaryListItem(itemPosition = 1)
      scrollToPosition(position = 2)
      performItemCheckOnTopicSummaryListItem(itemPosition = 2)
      scrollToPosition(position = 3)
      performItemCheckOnTopicSummaryListItem(itemPosition = 3)
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.mark_topics_completed_all_check_box)).check(matches(isChecked()))
    }
  }

  @Test
  fun testMarkTopicsCompletedActivity_selectAllTopics_unselectOneTopic_allCheckBoxIsNotChecked() {
    launch<MarkTopicsCompletedActivity>(
      createMarkTopicsCompletedActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      performItemCheckOnTopicSummaryListItem(itemPosition = 0)
      scrollToPosition(position = 1)
      performItemCheckOnTopicSummaryListItem(itemPosition = 1)
      scrollToPosition(position = 2)
      performItemCheckOnTopicSummaryListItem(itemPosition = 2)
      scrollToPosition(position = 3)
      performItemCheckOnTopicSummaryListItem(itemPosition = 3)
      scrollToPosition(position = 1)
      performItemCheckOnTopicSummaryListItem(itemPosition = 1)
      onView(withId(R.id.mark_topics_completed_all_check_box)).check(matches(not(isChecked())))
    }
  }

  @Test
  fun testMarkTopicsCompletedActivity_selectAllTopics_unselectOneTopic_configChange_allCheckBoxIsNotChecked() { // ktlint-disable max-line-length
    launch<MarkTopicsCompletedActivity>(
      createMarkTopicsCompletedActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      performItemCheckOnTopicSummaryListItem(itemPosition = 0)
      scrollToPosition(position = 1)
      performItemCheckOnTopicSummaryListItem(itemPosition = 1)
      scrollToPosition(position = 2)
      performItemCheckOnTopicSummaryListItem(itemPosition = 2)
      scrollToPosition(position = 3)
      performItemCheckOnTopicSummaryListItem(itemPosition = 3)
      scrollToPosition(position = 1)
      performItemCheckOnTopicSummaryListItem(itemPosition = 1)
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.mark_topics_completed_all_check_box)).check(matches(not(isChecked())))
    }
  }

  @Test
  fun testMarkTopicsCompletedActivity_fractionsTopicIsCompleted_isCheckedAndDisabled() {
    markFractionsTopicCompleted()
    launch<MarkTopicsCompletedActivity>(
      createMarkTopicsCompletedActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 2)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.mark_topics_completed_recycler_view,
          position = 2,
          targetViewId = R.id.mark_topics_completed_topic_check_box
        )
      ).check(matches(isChecked())).check(matches(not(isEnabled())))
    }
  }

  @Test
  fun testMarkTopicsCompletedActivity_fractionsTopicIsCompleted_configChange_isCheckedAndDisabled() { // ktlint-disable max-line-length
    markFractionsTopicCompleted()
    launch<MarkTopicsCompletedActivity>(
      createMarkTopicsCompletedActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 2)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.mark_topics_completed_recycler_view,
          position = 2,
          targetViewId = R.id.mark_topics_completed_topic_check_box
        )
      ).check(matches(isChecked())).check(matches(not(isEnabled())))
    }
  }

  @Test
  fun testMarkTopicsCompletedActivity_clickMarkCompleted_activityFinishes() {
    activityTestRule.launchActivity(createMarkTopicsCompletedActivityIntent(internalProfileId))
    testCoroutineDispatchers.runCurrent()
    onView(withId(R.id.mark_topics_completed_mark_completed_text_view)).perform(click())
    assertThat(activityTestRule.activity.isFinishing).isTrue()
  }

  @Test
  fun testMarkTopicsCompletedActivity_configChange_clickMarkCompleted_activityFinishes() {
    activityTestRule.launchActivity(createMarkTopicsCompletedActivityIntent(internalProfileId))
    testCoroutineDispatchers.runCurrent()
    onView(isRoot()).perform(orientationLandscape())
    onView(withId(R.id.mark_topics_completed_mark_completed_text_view)).perform(click())
    assertThat(activityTestRule.activity.isFinishing).isTrue()
  }

  @Test
  fun testMarkTopicsCompletedActivity_allLessonsAreCompleted_allCheckboxIsChecked() {
    markAllLessonsCompleted()
    launch<MarkTopicsCompletedActivity>(
      createMarkTopicsCompletedActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.mark_topics_completed_all_check_box)).check(matches(isChecked()))
    }
  }

  @Test
  fun testMarkTopicsCompletedActivity_allLessonsAreCompleted_configChange_allCheckboxIsChecked() {
    markAllLessonsCompleted()
    launch<MarkTopicsCompletedActivity>(
      createMarkTopicsCompletedActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.mark_topics_completed_all_check_box)).check(matches(isChecked()))
    }
  }

  private fun createMarkTopicsCompletedActivityIntent(internalProfileId: Int): Intent {
    return MarkTopicsCompletedActivity.createMarkTopicsCompletedIntent(
      context, internalProfileId
    )
  }

  private fun verifyTopicNameOnTopicSummaryListItemAtPosition(
    itemPosition: Int,
    stringToMatch: String
  ) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.mark_topics_completed_recycler_view,
        position = itemPosition,
        targetViewId = R.id.mark_topics_completed_topic_check_box
      )
    ).check(matches(withText(stringToMatch)))
  }

  private fun verifyItemCheckedOnTopicSummaryListItem(itemPosition: Int) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.mark_topics_completed_recycler_view,
        position = itemPosition,
        targetViewId = R.id.mark_topics_completed_topic_check_box
      )
    ).check(matches(isChecked()))
  }

  private fun performItemCheckOnTopicSummaryListItem(itemPosition: Int) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.mark_topics_completed_recycler_view,
        position = itemPosition,
        targetViewId = R.id.mark_topics_completed_topic_check_box
      )
    ).perform(click())
  }

  private fun markFractionsTopicCompleted() {
    storyProgressTestHelper.markCompletedFractionsTopic(
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
    onView(withId(R.id.mark_topics_completed_recycler_view)).perform(
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
      ExplorationStorageModule::class, NetworkConnectionUtilDebugModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(markTopicsCompletedActivityTest: MarkTopicsCompletedActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerMarkTopicsCompletedActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(markTopicsCompletedActivityTest: MarkTopicsCompletedActivityTest) {
      component.inject(markTopicsCompletedActivityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
