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
import androidx.test.espresso.assertion.ViewAssertions.selectedDescendantsMatch
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
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
import org.oppia.android.app.devoptions.markchapterscompleted.MarkChaptersCompletedActivity
import org.oppia.android.app.drawer.DeveloperOptionsModule
import org.oppia.android.app.drawer.DeveloperOptionsStarterModule
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
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
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

/** Tests for [MarkChaptersCompletedActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = MarkChaptersCompletedActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class MarkChaptersCompletedActivityTest {

  private val internalProfileId = 0

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var context: Context

  @get:Rule
  val activityTestRule = ActivityTestRule(
    MarkChaptersCompletedActivity::class.java,
    /* initialTouchMode= */ true,
    /* launchActivity= */ false
  )

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
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
  fun testMarkChaptersCompletedActivity_hasCorrectActivityLabel() {
    activityTestRule.launchActivity(createMarkChaptersCompletedActivityIntent(internalProfileId))
    val title = activityTestRule.activity.title

    // Verify that the activity label is correct as a proxy to verify TalkBack will announce the
    // correct string when it's read out.
    assertThat(title).isEqualTo(context.getString(R.string.mark_chapters_completed_activity_title))
  }

  @Test
  fun testMarkChaptersCompletedActivity_storiesAreShown() {
    launch<MarkChaptersCompletedActivity>(
      createMarkChaptersCompletedActivityIntent(internalProfileId)
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
  fun testMarkChaptersCompletedActivity_configChange_storiesAreShown() {
    launch<MarkChaptersCompletedActivity>(
      createMarkChaptersCompletedActivityIntent(internalProfileId)
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
  fun testMarkChaptersCompletedActivity_chapterListIsVisible() {
    launch<MarkChaptersCompletedActivity>(
      createMarkChaptersCompletedActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      verifyItemDisplayedOnStorySummaryListItem(itemPosition = 0)
      scrollToPosition(position = 1)
      verifyItemDisplayedOnStorySummaryListItem(itemPosition = 1)
      scrollToPosition(position = 2)
      verifyItemDisplayedOnStorySummaryListItem(itemPosition = 2)
      scrollToPosition(position = 3)
      verifyItemDisplayedOnStorySummaryListItem(itemPosition = 3)
      scrollToPosition(position = 4)
      verifyItemDisplayedOnStorySummaryListItem(itemPosition = 4)
    }
  }

  @Test
  fun testMarkChaptersCompletedActivity_configChange_chapterListIsVisible() {
    launch<MarkChaptersCompletedActivity>(
      createMarkChaptersCompletedActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 0)
      verifyItemDisplayedOnStorySummaryListItem(itemPosition = 0)
      scrollToPosition(position = 1)
      verifyItemDisplayedOnStorySummaryListItem(itemPosition = 1)
      scrollToPosition(position = 2)
      verifyItemDisplayedOnStorySummaryListItem(itemPosition = 2)
      scrollToPosition(position = 3)
      verifyItemDisplayedOnStorySummaryListItem(itemPosition = 3)
      scrollToPosition(position = 4)
      verifyItemDisplayedOnStorySummaryListItem(itemPosition = 4)
    }
  }

  @Test
  fun testMarkChaptersCompletedActivity_selectAll_isChecked() {
    launch<MarkChaptersCompletedActivity>(
      createMarkChaptersCompletedActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.mark_chapters_completed_all_check_box))
        .perform(click()).check(matches(isChecked()))
    }
  }

  @Test
  fun testMarkChaptersCompletedActivity_selectAll_unselect_isNotChecked() {
    launch<MarkChaptersCompletedActivity>(
      createMarkChaptersCompletedActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.mark_chapters_completed_all_check_box))
        .perform(click()).perform(click()).check(matches(not(isChecked())))
    }
  }

  @Test
  fun testMarkChaptersCompletedActivity_selectChaptersOfFirstStory_chaptersAreChecked() {
    launch<MarkChaptersCompletedActivity>(
      createMarkChaptersCompletedActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.mark_chapters_completed_story_summary_recycler_view,
          position = 0,
          targetViewId = R.id.mark_chapters_completed_chapter_summary_recycler_view
        )
      ).perform(actionOnItemAtPosition<ViewHolder>(0, click()))
        .check(
          selectedDescendantsMatch(
            atPositionOnView(
              recyclerViewId = R.id.mark_chapters_completed_chapter_summary_recycler_view,
              position = 0,
              targetViewId = R.id.mark_chapters_completed_chapter_check_box
            ),
            isChecked()
          )
        )
        .perform(actionOnItemAtPosition<ViewHolder>(1, click()))
        .check(
          selectedDescendantsMatch(
            atPositionOnView(
              recyclerViewId = R.id.mark_chapters_completed_chapter_summary_recycler_view,
              position = 1,
              targetViewId = R.id.mark_chapters_completed_chapter_check_box
            ),
            isChecked()
          )
        )
    }
  }

  private fun createMarkChaptersCompletedActivityIntent(internalProfileId: Int): Intent {
    return MarkChaptersCompletedActivity.createMarkChaptersCompletedIntent(
      context, internalProfileId
    )
  }

  private fun verifyStoryNameOnStorySummaryListItemAtPosition(
    itemPosition: Int,
    stringToMatch: String
  ) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.mark_chapters_completed_story_summary_recycler_view,
        position = itemPosition,
        targetViewId = R.id.mark_chapters_completed_story_name_text_view
      )
    ).check(matches(withText(stringToMatch)))
  }

  private fun verifyItemDisplayedOnStorySummaryListItem(itemPosition: Int) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.mark_chapters_completed_story_summary_recycler_view,
        position = itemPosition,
        targetViewId = R.id.mark_chapters_completed_chapter_summary_recycler_view
      )
    ).check(matches(isDisplayed()))
  }

  private fun scrollToPosition(position: Int) {
    onView(withId(R.id.mark_chapters_completed_story_summary_recycler_view)).perform(
      scrollToPosition<ViewHolder>(
        position
      )
    )
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
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
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class, PracticeTabModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(markChaptersCompletedActivityTest: MarkChaptersCompletedActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerMarkChaptersCompletedActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(markChaptersCompletedActivityTest: MarkChaptersCompletedActivityTest) {
      component.inject(markChaptersCompletedActivityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
