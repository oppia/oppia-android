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
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.activity.route.ActivityRouterModule
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.application.testing.TestingBuildFlavorModule
import org.oppia.android.app.devoptions.forcenetworktype.ForceNetworkTypeActivity
import org.oppia.android.app.devoptions.markchapterscompleted.MarkChaptersCompletedActivity
import org.oppia.android.app.devoptions.markstoriescompleted.MarkStoriesCompletedActivity
import org.oppia.android.app.devoptions.marktopicscompleted.MarkTopicsCompletedActivity
import org.oppia.android.app.devoptions.mathexpressionparser.MathExpressionParserActivity
import org.oppia.android.app.devoptions.testing.DeveloperOptionsTestActivity
import org.oppia.android.app.devoptions.vieweventlogs.ViewEventLogsActivity
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
import org.oppia.android.domain.devoptions.ShowAllHintsAndSolutionController
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
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
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

/** Tests for [DeveloperOptionsFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = DeveloperOptionsFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class DeveloperOptionsFragmentTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  private val internalProfileId = 0

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var showAllHintsAndSolutionController: ShowAllHintsAndSolutionController

  @Inject
  lateinit var context: Context

  @get:Rule
  val activityTestRule = ActivityTestRule(
    DeveloperOptionsTestActivity::class.java,
    /* initialTouchMode= */ true,
    /* launchActivity= */ false
  )

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

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

  @Test
  fun testDeveloperOptionsFragment_hintsSwitchIsUnchecked() {
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
  fun testDeveloperOptionsFragment_clickShowAllHints_hintsSwitchIsChecked() {
    launch<DeveloperOptionsTestActivity>(
      createDeveloperOptionsTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 2)
      onView(withId(R.id.show_all_hints_solution_constraint_layout)).perform(click())
      onView(
        atPositionOnView(
          recyclerViewId = R.id.developer_options_list,
          position = 2,
          targetViewId = R.id.show_all_hints_solution_switch
        )
      ).check(matches(isChecked()))
    }
  }

  @Test
  fun testDeveloperOptionsFragment_clickShowAllHints_configChange_hintsSwitchIsChecked() {
    launch<DeveloperOptionsTestActivity>(
      createDeveloperOptionsTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 2)
      onView(withId(R.id.show_all_hints_solution_constraint_layout)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 2)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.developer_options_list,
          position = 2,
          targetViewId = R.id.show_all_hints_solution_switch
        )
      ).check(matches(isChecked()))
    }
  }

  @Test
  fun testDeveloperOptionsFragment_hintsSwitchIsDisabled_showAllHintsAndSolutionIsFalse() {
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
      assertThat(showAllHintsAndSolutionController.getShowAllHintsAndSolution()).isFalse()
    }
  }

  @Test
  fun testDeveloperOptionsFragment_hintsSwitchIsEnabled_showAllHintsAndSolutionIsTrue() {
    launch<DeveloperOptionsTestActivity>(
      createDeveloperOptionsTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 2)
      onView(withId(R.id.show_all_hints_solution_constraint_layout)).perform(click())
      scrollToPosition(position = 2)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.developer_options_list,
          position = 2,
          targetViewId = R.id.show_all_hints_solution_switch
        )
      ).check(matches(isChecked()))
      assertThat(showAllHintsAndSolutionController.getShowAllHintsAndSolution()).isTrue()
    }
  }

  @Test
  fun testDeveloperOptionsFragment_clickForceCrash_throwsRuntimeException() {
    launch<DeveloperOptionsTestActivity>(
      createDeveloperOptionsTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      val exception = assertThrows<RuntimeException>() {
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
      val exception = assertThrows<RuntimeException>() {
        scrollToPosition(position = 2)
        onView(withId(R.id.force_crash_text_view)).perform(click())
      }
      assertThat(exception.cause).hasMessageThat().contains("Force crash occurred")
    }
  }

  @Test
  fun testDeveloperOptionsFragment_clickForceNetworkType_opensForceNetworkTypeActivity() {
    launch<DeveloperOptionsTestActivity>(
      createDeveloperOptionsTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 2)
      onView(withId(R.id.force_network_type_text_view)).perform(click())
      intended(hasComponent(ForceNetworkTypeActivity::class.java.name))
    }
  }

  @Test
  fun testDeveloperOptionsFragment_land_clickForceNetworkType_opensForceNetworkTypeActivity() {
    launch<DeveloperOptionsTestActivity>(
      createDeveloperOptionsTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 2)
      onView(withId(R.id.force_network_type_text_view)).perform(click())
      intended(hasComponent(ForceNetworkTypeActivity::class.java.name))
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

  @Test
  fun testDeveloperOptionsFragment_clickMathExpressionsEquations_opensMathExpParserActivity() {
    launch<DeveloperOptionsTestActivity>(
      createDeveloperOptionsTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(position = 3)
      onView(withId(R.id.math_expressions_text_view)).perform(click())

      intended(hasComponent(MathExpressionParserActivity::class.java.name))
    }
  }

  @Test
  fun testDeveloperOptionsFragment_land_clickMathExpressionsEquations_opensMathExpParserActivity() {
    launch<DeveloperOptionsTestActivity>(
      createDeveloperOptionsTestActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()

      scrollToPosition(position = 3)
      onView(withId(R.id.math_expressions_text_view)).perform(click())

      intended(hasComponent(MathExpressionParserActivity::class.java.name))
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
