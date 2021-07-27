package org.oppia.android.app.devoptions

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
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
import org.oppia.android.app.devoptions.markstoriescompleted.MarkStoriesCompletedActivity
import org.oppia.android.app.devoptions.marktopicscompleted.MarkTopicsCompletedActivity
import org.oppia.android.app.devoptions.testing.DeveloperOptionsTestActivity
import org.oppia.android.app.devoptions.vieweventlogs.ViewEventLogsActivity
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
import org.oppia.android.testing.assertThrows
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

/** Tests for [DeveloperOptionsFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = DeveloperOptionsFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class DeveloperOptionsFragmentTest {

  private val internalProfileId = 0

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var context: Context

  @get:Rule
  val activityTestRule = ActivityTestRule(
    DeveloperOptionsTestActivity::class.java,
    /* initialTouchMode= */ true,
    /* launchActivity= */ false
  )

  @get:Rule
  val accessibilityTestRule = AccessibilityTestRule()

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
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
  fun testDeveloperOptionsFragment_modifyLessonProgressIsDisplayed() {
    launch<DeveloperOptionsTestActivity>(
      createDeveloperOptionsTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      verifyItemDisplayedOnDeveloperOptionsListItem(
        itemPosition = 0,
        targetView = R.id.modify_lesson_progress_text_view
      )
      verifyTextOnDeveloperOptionsListItemAtPosition(
        itemPosition = 0,
        targetViewId = R.id.mark_chapters_completed_text_view,
        stringIdToMatch = R.string.developer_options_mark_chapters_completed
      )
      verifyTextOnDeveloperOptionsListItemAtPosition(
        itemPosition = 0,
        targetViewId = R.id.mark_stories_completed_text_view,
        stringIdToMatch = R.string.developer_options_mark_stories_completed
      )
      verifyTextOnDeveloperOptionsListItemAtPosition(
        itemPosition = 0,
        targetViewId = R.id.mark_topics_completed_text_view,
        stringIdToMatch = R.string.developer_options_mark_topics_completed
      )
    }
  }

  @Test
  fun testDeveloperOptionsFragment_land_modifyLessonProgressIsDisplayed() {
    launch<DeveloperOptionsTestActivity>(
      createDeveloperOptionsTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 0)
      verifyItemDisplayedOnDeveloperOptionsListItem(
        itemPosition = 0,
        targetView = R.id.modify_lesson_progress_text_view
      )
      verifyTextOnDeveloperOptionsListItemAtPosition(
        itemPosition = 0,
        targetViewId = R.id.mark_chapters_completed_text_view,
        stringIdToMatch = R.string.developer_options_mark_chapters_completed
      )
      verifyTextOnDeveloperOptionsListItemAtPosition(
        itemPosition = 0,
        targetViewId = R.id.mark_stories_completed_text_view,
        stringIdToMatch = R.string.developer_options_mark_stories_completed
      )
      verifyTextOnDeveloperOptionsListItemAtPosition(
        itemPosition = 0,
        targetViewId = R.id.mark_topics_completed_text_view,
        stringIdToMatch = R.string.developer_options_mark_topics_completed
      )
    }
  }

  @Test
  fun testDeveloperOptionsFragment_viewLogsIsDisplayed() {
    launch<DeveloperOptionsTestActivity>(
      createDeveloperOptionsTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      verifyItemDisplayedOnDeveloperOptionsListItem(
        itemPosition = 1,
        targetView = R.id.view_logs_text_view
      )
      verifyTextOnDeveloperOptionsListItemAtPosition(
        itemPosition = 1,
        targetViewId = R.id.event_logs_text_view,
        stringIdToMatch = R.string.developer_options_event_logs
      )
    }
  }

  @Test
  fun testDeveloperOptionsFragment_land_viewLogsIsDisplayed() {
    launch<DeveloperOptionsTestActivity>(
      createDeveloperOptionsTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 1)
      verifyItemDisplayedOnDeveloperOptionsListItem(
        itemPosition = 1,
        targetView = R.id.view_logs_text_view
      )
      verifyTextOnDeveloperOptionsListItemAtPosition(
        itemPosition = 1,
        targetViewId = R.id.event_logs_text_view,
        stringIdToMatch = R.string.developer_options_event_logs
      )
    }
  }

  @Test
  fun testDeveloperOptionsFragment_overrideAppBehaviorsIsDisplayed() {
    launch<DeveloperOptionsTestActivity>(
      createDeveloperOptionsTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 2)
      verifyItemDisplayedOnDeveloperOptionsListItem(
        itemPosition = 2,
        targetView = R.id.override_app_behaviors_text_view
      )
      verifyTextOnDeveloperOptionsListItemAtPosition(
        itemPosition = 2,
        targetViewId = R.id.show_all_hints_solution_text_view,
        stringIdToMatch = R.string.developer_options_show_all_hints_solution
      )
      verifyTextOnDeveloperOptionsListItemAtPosition(
        itemPosition = 2,
        targetViewId = R.id.force_network_type_text_view,
        stringIdToMatch = R.string.developer_options_force_network_type
      )
      verifyTextOnDeveloperOptionsListItemAtPosition(
        itemPosition = 2,
        targetViewId = R.id.force_crash_text_view,
        stringIdToMatch = R.string.developer_options_force_crash
      )
    }
  }

  @Test
  fun testDeveloperOptionsFragment_land_overrideAppBehaviorsIsDisplayed() {
    launch<DeveloperOptionsTestActivity>(
      createDeveloperOptionsTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 2)
      verifyItemDisplayedOnDeveloperOptionsListItem(
        itemPosition = 2,
        targetView = R.id.override_app_behaviors_text_view
      )
      verifyTextOnDeveloperOptionsListItemAtPosition(
        itemPosition = 2,
        targetViewId = R.id.show_all_hints_solution_text_view,
        stringIdToMatch = R.string.developer_options_show_all_hints_solution
      )
      verifyTextOnDeveloperOptionsListItemAtPosition(
        itemPosition = 2,
        targetViewId = R.id.force_network_type_text_view,
        stringIdToMatch = R.string.developer_options_force_network_type
      )
      verifyTextOnDeveloperOptionsListItemAtPosition(
        itemPosition = 2,
        targetViewId = R.id.force_crash_text_view,
        stringIdToMatch = R.string.developer_options_force_crash
      )
    }
  }

  // TODO(#3397): When the logic to show all hints and solutions is implemented, write a test to
  //  check for click operation of the 'Show all hints/solution' switch and the configChange
  //  versions of all these tests including the below one.
  @Test
  fun testDeveloperOptionsFragment_hintsAndSolutionSwitchIsUncheck() {
    launch<DeveloperOptionsTestActivity>(
      createDeveloperOptionsTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 2)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.developer_options_list,
          position = 2,
          targetViewId = R.id.show_all_hints_solution_switch
        )
      ).check(matches(not(isChecked())))
    }
  }

  @Test
  fun testDeveloperOptionsFragment_clickForceCrash_throwsRuntimeException() {
    launch<DeveloperOptionsTestActivity>(
      createDeveloperOptionsTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      val exception = assertThrows(RuntimeException::class) {
        scrollToPosition(position = 2)
        onView(withId(R.id.force_crash_text_view)).perform(click())
      }
      assertThat(exception.cause).hasMessageThat().contains("Force crash occurred")
    }
  }

  @Test
  fun testDeveloperOptionsFragment_land_clickForceCrash_throwsRuntimeException() {
    launch<DeveloperOptionsTestActivity>(
      createDeveloperOptionsTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      val exception = assertThrows(RuntimeException::class) {
        scrollToPosition(position = 2)
        onView(withId(R.id.force_crash_text_view)).perform(click())
      }
      assertThat(exception.cause).hasMessageThat().contains("Force crash occurred")
    }
  }

  @Test
  fun testDeveloperOptionsFragment_clickEventLogs_opensViewEventLogsActivity() {
    launch<DeveloperOptionsTestActivity>(
      createDeveloperOptionsTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 1)
      onView(withId(R.id.event_logs_text_view)).perform(click())
      intended(hasComponent(ViewEventLogsActivity::class.java.name))
    }
  }

  @Test
  fun testDeveloperOptionsFragment_land_clickEventLogs_opensViewEventLogsActivity() {
    launch<DeveloperOptionsTestActivity>(
      createDeveloperOptionsTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 1)
      onView(withId(R.id.event_logs_text_view)).perform(click())
      intended(hasComponent(ViewEventLogsActivity::class.java.name))
    }
  }

  @Test
  fun testDeveloperOptions_clickMarkChaptersCompleted_opensMarkChaptersCompletedActivity() {
    launch<DeveloperOptionsTestActivity>(
      createDeveloperOptionsTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      onView(withId(R.id.mark_chapters_completed_text_view)).perform(click())
      intended(hasComponent(MarkChaptersCompletedActivity::class.java.name))
    }
  }

  @Test
  fun testDeveloperOptions_land_clickMarkChaptersCompleted_opensMarkChaptersCompletedActivity() {
    launch<DeveloperOptionsTestActivity>(
      createDeveloperOptionsTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 0)
      onView(withId(R.id.mark_chapters_completed_text_view)).perform(click())
      intended(hasComponent(MarkChaptersCompletedActivity::class.java.name))
    }
  }

  @Test
  fun testDeveloperOptions_clickMarkStoriesCompleted_opensMarkStoriesCompletedActivity() {
    launch<DeveloperOptionsTestActivity>(
      createDeveloperOptionsTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      onView(withId(R.id.mark_stories_completed_text_view)).perform(click())
      intended(hasComponent(MarkStoriesCompletedActivity::class.java.name))
    }
  }

  @Test
  fun testDeveloperOptions_land_clickMarkStoriesCompleted_opensMarkStoriesCompletedActivity() {
    launch<DeveloperOptionsTestActivity>(
      createDeveloperOptionsTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 0)
      onView(withId(R.id.mark_stories_completed_text_view)).perform(click())
      intended(hasComponent(MarkStoriesCompletedActivity::class.java.name))
    }
  }

  @Test
  fun testDeveloperOptions_clickMarkTopicsCompleted_opensMarkTopicsCompletedActivity() {
    launch<DeveloperOptionsTestActivity>(
      createDeveloperOptionsTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      onView(withId(R.id.mark_topics_completed_text_view)).perform(click())
      intended(hasComponent(MarkTopicsCompletedActivity::class.java.name))
    }
  }

  @Test
  fun testDeveloperOptions_land_clickMarkTopicsCompleted_opensMarkTopicsCompletedActivity() {
    launch<DeveloperOptionsTestActivity>(
      createDeveloperOptionsTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 0)
      onView(withId(R.id.mark_topics_completed_text_view)).perform(click())
      intended(hasComponent(MarkTopicsCompletedActivity::class.java.name))
    }
  }

  private fun createDeveloperOptionsTestActivityIntent(internalProfileId: Int): Intent {
    return DeveloperOptionsTestActivity.createDeveloperOptionsTestIntent(context, internalProfileId)
  }

  private fun verifyItemDisplayedOnDeveloperOptionsListItem(
    itemPosition: Int,
    targetView: Int
  ) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.developer_options_list,
        position = itemPosition,
        targetViewId = targetView
      )
    ).check(matches(isDisplayed()))
  }

  private fun verifyTextOnDeveloperOptionsListItemAtPosition(
    itemPosition: Int,
    targetViewId: Int,
    @StringRes stringIdToMatch: Int
  ) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.developer_options_list,
        position = itemPosition,
        targetViewId = targetViewId
      )
    ).check(matches(withText(context.getString(stringIdToMatch))))
  }

  private fun scrollToPosition(position: Int) {
    onView(withId(R.id.developer_options_list)).perform(
      scrollToPosition<ViewHolder>(
        position
      )
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

    fun inject(developerOptionsFragmentTest: DeveloperOptionsFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerDeveloperOptionsFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(developerOptionsFragmentTest: DeveloperOptionsFragmentTest) {
      component.inject(developerOptionsFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
