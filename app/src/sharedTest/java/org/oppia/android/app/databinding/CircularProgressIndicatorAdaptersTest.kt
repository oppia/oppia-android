package org.oppia.android.app.databinding

import android.app.Application
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec
import com.google.android.material.progressindicator.DeterminateDrawable
import com.google.common.truth.Truth.assertThat
import dagger.Component
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
import org.oppia.android.app.databinding.CircularProgressIndicatorAdapters.setAnimatedProgress
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.CircularProgressIndicatorAdaptersTestActivity
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
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
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestImageLoaderModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.TestPlatform
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
import org.oppia.android.util.math.isApproximatelyEqualTo
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.lang.reflect.Field
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for ``CircularProgressIndicator`` data-binding adapters. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = CircularProgressIndicatorAdaptersTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class CircularProgressIndicatorAdaptersTest {
  @get:Rule val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()
  @get:Rule val oppiaTestRule = OppiaTestRule()
  @get:Rule
  var activityRule =
    ActivityScenarioRule<CircularProgressIndicatorAdaptersTestActivity>(
      CircularProgressIndicatorAdaptersTestActivity.createIntent(
        ApplicationProvider.getApplicationContext()
      )
    )

  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    Intents.init()
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  @Test
  fun testSetAnimatedProgress_autoBinding_progressOfZero_doesNotChangeIndicatorProgress() {
    activityRule.scenario.onActivity { it.viewModel.currentAutoProgress.value = 0 }
    testCoroutineDispatchers.runCurrent()

    activityRule.scenario.onActivity {
      // Setting the progress to 0 should not change the view.
      val view = it.findAutoBoundIndicatorView()
      assertThat(view.progress).isEqualTo(it.viewModel.defaultInitialValue)
    }
  }

  @Test
  fun testSetAnimatedProgress_autoBinding_progressOfNonZero_updatesIndicatorProgress() {
    activityRule.scenario.onActivity { it.viewModel.currentAutoProgress.value = 30 }
    testCoroutineDispatchers.runCurrent()

    activityRule.scenario.onActivity {
      // Setting the progress to nonzero should change the view.
      val view = it.findAutoBoundIndicatorView()
      assertThat(view.progress).isNotEqualTo(it.viewModel.defaultInitialValue)
      assertThat(view.progress).isEqualTo(30)
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC) // Checking animation state could be flaky in Espresso.
  fun testSetAnimatedProgress_autoBinding_progressOfNonZero_animatesIndicatorProgress() {
    activityRule.scenario.onActivity {
      // Note that this needs to be done in the same 'onActivity' as checking the progress so as to
      // not fully run the animation through when checking if it's active.
      it.viewModel.currentAutoProgress.value = 30
      it.viewModel.notifyChange()
      it.binding.executePendingBindings()

      // Setting the progress to nonzero should animate the progress change.
      assertThat(it.findAutoBoundIndicatorView().isProgressSetToAnimate()).isTrue()
    }
  }

  @Test
  fun testSetAnimatedProgress_directCall_progressOfZero_doesNotChangeIndicatorProgress() {
    // Default the initial progress of the indicator view.
    activityRule.scenario.onActivity {
      it.findUnboundIndicatorView().progress = it.viewModel.defaultInitialValue
    }
    testCoroutineDispatchers.runCurrent()

    // The view needs to be changed directly since its progress isn't auto-bound to the adapter.
    activityRule.scenario.onActivity {
      setAnimatedProgress(it.findUnboundIndicatorView(), /* progress = */ 0)
    }
    testCoroutineDispatchers.runCurrent()

    activityRule.scenario.onActivity {
      // Setting the progress to 0 should not change the view.
      assertThat(it.findUnboundIndicatorView().progress).isEqualTo(it.viewModel.defaultInitialValue)
    }
  }

  @Test
  fun testSetAnimatedProgress_directCall_progressOfNonZero_updatesIndicatorProgress() {
    // Default the initial progress of the indicator view.
    activityRule.scenario.onActivity {
      it.findUnboundIndicatorView().progress = it.viewModel.defaultInitialValue
    }
    testCoroutineDispatchers.runCurrent()

    // The view needs to be changed directly since its progress isn't auto-bound to the adapter.
    activityRule.scenario.onActivity {
      setAnimatedProgress(it.findUnboundIndicatorView(), /* progress = */ 30)
    }
    testCoroutineDispatchers.runCurrent()

    activityRule.scenario.onActivity {
      // Setting the progress to nonzero should change the view.
      val view = it.findUnboundIndicatorView()
      assertThat(view.progress).isNotEqualTo(it.viewModel.defaultInitialValue)
      assertThat(view.progress).isEqualTo(30)
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC) // Checking animation state could be flaky in Espresso.
  fun testSetAnimatedProgress_directCall_progressOfNonZero_animatesIndicatorProgress() {
    // Default the initial progress of the indicator view.
    activityRule.scenario.onActivity {
      it.findUnboundIndicatorView().progress = it.viewModel.defaultInitialValue
    }
    testCoroutineDispatchers.runCurrent()

    activityRule.scenario.onActivity {
      // The view needs to be changed directly since its progress isn't auto-bound to the adapter.
      // Note that this needs to be done in the same 'onActivity' as checking the progress so as to
      // not fully run the animation through when checking if it's active.
      val indicatorView = it.findUnboundIndicatorView()
      setAnimatedProgress(indicatorView, /* progress = */ 30)

      // Setting the progress to nonzero should animate the progress change.
      assertThat(indicatorView.isProgressSetToAnimate()).isTrue()
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun AppCompatActivity.findAutoBoundIndicatorView() =
    findProgressIndicatorView(R.id.circular_progress_indicator_test_bound_view)

  private fun AppCompatActivity.findUnboundIndicatorView() =
    findProgressIndicatorView(R.id.circular_progress_indicator_test_unbound_view)

  private fun AppCompatActivity.findProgressIndicatorView(
    @IdRes viewId: Int
  ): CircularProgressIndicator = findViewById(viewId)

  /**
   * Returns whether, at the time of calling this method, this [CircularProgressIndicator] has been
   * set to animate its progress. Note that this method will skip the indicator's animation as part
   * of verifying this state.
   */
  private fun CircularProgressIndicator.isProgressSetToAnimate(): Boolean {
    // This is a hacky way to introspect implementation state of the indicator to verify that it's
    // animating since the API of the class provides no way to check this.
    val drawable = checkNotNull(progressDrawable) { "Missing progress drawable: $this." }
    val currentProgressFraction = drawable.getCurrentIndicatorFraction()
    drawable.jumpToCurrentState() // Simulate the animation being skipped.
    val updatedProgressFraction = drawable.getCurrentIndicatorFraction()
    // If the indicator fraction changes, that indicates that the indicator was set up to animate
    // (since it didn't jump straight to its latest progress).
    return !currentProgressFraction.isApproximatelyEqualTo(updatedProgressFraction)
  }

  private fun DeterminateDrawable<CircularProgressIndicatorSpec>.getCurrentIndicatorFraction() =
    javaClass.getAccessibleDeclaredField("indicatorFraction").getFloat(this)

  private fun <T> Class<T>.getAccessibleDeclaredField(name: String): Field {
    return checkNotNull(declaredFields.find { it.name == name }) {
      "Failed to find field with name '$name' in class: $this."
    }.also { it.isAccessible = true }
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
      GcsResourceModule::class, TestImageLoaderModule::class, ImageParsingModule::class,
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

    fun inject(test: CircularProgressIndicatorAdaptersTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerCircularProgressIndicatorAdaptersTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(test: CircularProgressIndicatorAdaptersTest) {
      component.inject(test)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
