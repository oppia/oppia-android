package org.oppia.android.app.devoptions.markchapterscompleted

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.CoreMatchers.containsString
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
import org.oppia.android.app.model.ScreenName
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.test.R
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
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
import org.oppia.android.domain.exploration.ExplorationProgressModule
import org.oppia.android.domain.exploration.ExplorationStorageProdModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverProdModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulerModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterProdModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.LogReportingTestModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.firebase.AuthenticationTestModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.platformparameter.PlatformParameterTestModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.DispatcherTestModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.extractCurrentAppScreenName
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusProdModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.logging.performancemetrics.testing.PerformanceMetricsAssessorTestModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.ImageLoaderProdModule
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
  @get:Rule val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()
  @get:Rule val oppiaTestRule = OppiaTestRule()

  @Inject lateinit var context: Context
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  private val internalProfileId = 0

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
  fun testActivity_createIntent_verifyScreenNameInIntent() {
    val screenName =
      createMarkChaptersCompletedActivityIntent(internalProfileId).extractCurrentAppScreenName()

    assertThat(screenName).isEqualTo(ScreenName.MARK_CHAPTERS_COMPLETED_ACTIVITY)
  }

  @Test
  fun testMarkChaptersCompletedActivity_hasCorrectActivityLabel() {
    launch<MarkChaptersCompletedActivity>(
      createMarkChaptersCompletedActivityIntent(internalProfileId)
    ).use { scenario ->
      scenario.onActivity { activity ->
        val title = activity.title

        // Verify that the activity label is correct as a proxy to verify TalkBack will announce the
        // correct string when it's read out.
        assertThat(title)
          .isEqualTo(context.getString(R.string.mark_chapters_completed_activity_title))
      }
    }
  }

  @Test
  fun testMarkChaptersCompletedActivity_markChaptersCompletedFragmentIsDisplayed() {
    launch<MarkChaptersCompletedActivity>(
      createMarkChaptersCompletedActivityIntent(internalProfileId)
    ).use {
      onView(withId(R.id.mark_chapters_completed_fragment_container)).check(
        matches(isDisplayed())
      )
    }
  }

  @Test
  fun testMarkChaptersCompletedActivity_configChange_markChaptersCompletedFragmentIsDisplayed() {
    launch<MarkChaptersCompletedActivity>(
      createMarkChaptersCompletedActivityIntent(internalProfileId)
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.mark_chapters_completed_fragment_container)).check(
        matches(isDisplayed())
      )
    }
  }

  @Test
  fun testActivity_showConfirmationTrueInIntent_selectChapters_markCompleted_confirmationShown() {
    launch<MarkChaptersCompletedActivity>(
      createMarkChaptersCompletedActivityIntent(internalProfileId, showConfirmationNotice = true)
    ).use {
      testCoroutineDispatchers.runCurrent()
      clickOnRecyclerViewItemAtPosition(itemPosition = 1)
      clickOnRecyclerViewItemAtPosition(itemPosition = 2)
      clickOnRecyclerViewItemAtPosition(itemPosition = 3)

      onView(withId(R.id.mark_chapters_completed_mark_completed_text_view)).perform(click())

      onView(withText(containsString("The following chapters will be marked as finished")))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testActivity_showConfirmationFalseInIntent_selectChapters_markCompleted_confNotShown() {
    launch<MarkChaptersCompletedActivity>(
      createMarkChaptersCompletedActivityIntent(internalProfileId, showConfirmationNotice = false)
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

  private fun createMarkChaptersCompletedActivityIntent(
    internalProfileId: Int,
    showConfirmationNotice: Boolean = false
  ): Intent {
    return MarkChaptersCompletedActivity.createMarkChaptersCompletedIntent(
      context, internalProfileId, showConfirmationNotice
    )
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

  private fun scrollToPosition(position: Int) {
    onView(withId(R.id.mark_chapters_completed_recycler_view)).perform(
      scrollToPosition<ViewHolder>(position)
    )
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      AccessibilityTestModule::class,
      ActivityRecreatorTestModule::class,
      ActivityRouterModule::class,
      AlgebraicExpressionInputModule::class,
      ApplicationLifecycleModule::class,
      ApplicationModule::class,
      ApplicationStartupListenerModule::class,
      AssetModule::class,
      AuthenticationTestModule::class,
      CachingTestModule::class,
      ContinueModule::class,
      CpuPerformanceSnapshotterModule::class,
      DeveloperOptionsModule::class,
      DeveloperOptionsStarterModule::class,
      DispatcherTestModule::class,
      DragDropSortInputModule::class,
      ExpirationMetaDataRetrieverProdModule::class,
      ExplorationProgressModule::class,
      ExplorationStorageProdModule::class,
      FakeOppiaClockModule::class,
      FirebaseLogUploaderModule::class,
      FractionInputModule::class,
      GcsResourceModule::class,
      HintsAndSolutionConfigModule::class,
      HintsAndSolutionProdModule::class,
      HtmlParserEntityTypeModule::class,
      ImageClickInputModule::class,
      ImageLoaderProdModule::class,
      ImageParsingModule::class,
      InteractionsModule::class,
      ItemSelectionInputModule::class,
      LocaleProdModule::class,
      LogReportWorkerModule::class,
      LogReportingTestModule::class,
      LogStorageModule::class,
      LoggerModule::class,
      LoggingIdentifierModule::class,
      MathEquationInputModule::class,
      MetricLogSchedulerModule::class,
      MultipleChoiceInputModule::class,
      NetworkConfigProdModule::class,
      NetworkConnectionDebugUtilModule::class,
      NetworkConnectionUtilDebugModule::class,
      NumberWithUnitsRuleModule::class,
      NumericExpressionInputModule::class,
      NumericInputRuleModule::class,
      PerformanceMetricsAssessorTestModule::class,
      PlatformParameterProdModule::class,
      PlatformParameterSingletonModule::class,
      PlatformParameterTestModule::class,
      QuestionModule::class,
      RatioInputModule::class,
      RetrofitModule::class,
      RetrofitServiceModule::class,
      RobolectricModule::class,
      SplitScreenInteractionModule::class,
      SyncStatusModule::class,
      SyncStatusProdModule::class,
      TestAuthenticationModule::class,
      TestDispatcherModule::class,
      TestLogReportingModule::class,
      TestingBuildFlavorModule::class,
      TextInputRuleModule::class,
      ViewBindingShimModule::class,
      WorkManagerConfigurationModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder {
      override fun build(): TestApplicationComponent
    }

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
