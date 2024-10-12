package org.oppia.android.app.databinding

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
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
import org.oppia.android.app.databinding.StateAssemblerPaddingBindingAdapters.setExplorationSplitViewPadding
import org.oppia.android.app.databinding.StateAssemblerPaddingBindingAdapters.setExplorationViewPadding
import org.oppia.android.app.databinding.StateAssemblerPaddingBindingAdapters.setQuestionSplitViewPadding
import org.oppia.android.app.databinding.StateAssemblerPaddingBindingAdapters.setQuestionViewPadding
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.StateAssemblerPaddingBindingAdaptersTestActivity
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
import org.oppia.android.testing.TestImageLoaderModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.robolectric.RobolectricModule
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
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

private const val TOLERANCE = 1e-5f

/** Tests for [StateAssemblerPaddingBindingAdapters]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = StateAssemblerPaddingBindingAdaptersTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class StateAssemblerPaddingBindingAdaptersTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var context: Context

  @get:Rule
  var activityRule: ActivityScenarioRule<StateAssemblerPaddingBindingAdaptersTestActivity> =
    ActivityScenarioRule(
      Intent(
        ApplicationProvider.getApplicationContext(),
        StateAssemblerPaddingBindingAdaptersTestActivity::class.java
      )
    )

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
  fun testStateAssemblerPadding_explorationViewPadding_ltrIsEnabled_paddingsForLtrIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setExplorationViewPadding(
        textView,
        /* isApplicable= */ true,
        /* paddingStart= */ 12f,
        /* paddingTop= */ 16f,
        /* paddingEnd= */ 16f,
        /* paddingBottom= */ 12f
      )
      return@runWithActivity textView
    }
    /*
     * Note that the padding starts/ends below match the ones set above because, when the adapters are
     * working correctly in RTL mode, the start/end should be exactly the start/end originally set.
     */
    assertThat(textView.paddingStart.toFloat()).isWithin(TOLERANCE).of(12f)
    assertThat(textView.paddingEnd.toFloat()).isWithin(TOLERANCE).of(16f)
  }

  @Test
  fun testStateAssemblerPadding_explorationViewPadding_rtlIsEnabled_paddingsForRtlIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      ViewCompat.setLayoutDirection(textView, ViewCompat.LAYOUT_DIRECTION_RTL)
      setExplorationViewPadding(
        textView,
        /* isApplicable= */ true,
        /* paddingStart= */ 12f,
        /* paddingTop= */ 16f,
        /* paddingEnd= */ 16f,
        /* paddingBottom= */ 12f
      )
      return@runWithActivity textView
    }
    /*
     * Note that the padding starts/ends below match the ones set above because, when the adapters are
     * working correctly in RTL mode, the start/end should be exactly the start/end originally set.
     */
    assertThat(textView.paddingStart.toFloat()).isWithin(TOLERANCE).of(12f)
    assertThat(textView.paddingEnd.toFloat()).isWithin(TOLERANCE).of(16f)
  }

  @Test
  fun testStateAssemblerPadding_questionViewPadding_ltrIsEnabled_paddingsForLtrIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setQuestionViewPadding(
        textView,
        /* isApplicable= */ true,
        /* paddingStart= */ 12f,
        /* paddingTop= */ 16f,
        /* paddingEnd= */ 16f,
        /* paddingBottom= */ 12f
      )
      return@runWithActivity textView
    }
    /*
     * Note that the padding starts/ends below match the ones set above because, when the adapters are
     * working correctly in RTL mode, the start/end should be exactly the start/end originally set.
     */
    assertThat(textView.paddingStart.toFloat()).isWithin(TOLERANCE).of(12f)
    assertThat(textView.paddingEnd.toFloat()).isWithin(TOLERANCE).of(16f)
  }

  @Test
  fun testStateAssemblerPadding_questionViewPadding_rtlIsEnabled_paddingsForRtlIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      ViewCompat.setLayoutDirection(textView, ViewCompat.LAYOUT_DIRECTION_RTL)
      setQuestionViewPadding(
        textView,
        /* isApplicable= */ true,
        /* paddingStart= */ 12f,
        /* paddingTop= */ 16f,
        /* paddingEnd= */ 16f,
        /* paddingBottom= */ 12f
      )
      return@runWithActivity textView
    }
    /*
     * Note that the padding starts/ends below match the ones set above because, when the adapters are
     * working correctly in RTL mode, the start/end should be exactly the start/end originally set.
     */
    assertThat(textView.paddingStart.toFloat()).isWithin(TOLERANCE).of(12f)
    assertThat(textView.paddingEnd.toFloat()).isWithin(TOLERANCE).of(16f)
  }

  @Test
  fun testStateAssemblerPadding_questionSplitViewPadding_ltrIsEnabled_paddingsForLtrIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setQuestionSplitViewPadding(
        textView,
        /* isApplicable= */ true,
        /* paddingStart= */ 12f,
        /* paddingTop= */ 16f,
        /* paddingEnd= */ 16f,
        /* paddingBottom= */ 12f
      )
      return@runWithActivity textView
    }
    /*
     * Note that the padding starts/ends below match the ones set above because, when the adapters are
     * working correctly in RTL mode, the start/end should be exactly the start/end originally set.
     */
    assertThat(textView.paddingStart.toFloat()).isWithin(TOLERANCE).of(12f)
    assertThat(textView.paddingEnd.toFloat()).isWithin(TOLERANCE).of(16f)
  }

  @Test
  fun testStateAssemblerPadding_questionSplitViewPadding_rtlIsEnabled_paddingsForRtlIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      ViewCompat.setLayoutDirection(textView, ViewCompat.LAYOUT_DIRECTION_RTL)
      setQuestionSplitViewPadding(
        textView,
        /* isApplicable= */ true,
        /* paddingStart= */ 12f,
        /* paddingTop= */ 16f,
        /* paddingEnd= */ 16f,
        /* paddingBottom= */ 12f
      )
      return@runWithActivity textView
    }
    /*
     * Note that the padding starts/ends below match the ones set above because, when the adapters are
     * working correctly in RTL mode, the start/end should be exactly the start/end originally set.
     */
    assertThat(textView.paddingStart.toFloat()).isWithin(TOLERANCE).of(12f)
    assertThat(textView.paddingEnd.toFloat()).isWithin(TOLERANCE).of(16f)
  }

  @Config(qualifiers = "port")
  @Test
  fun testStateAssemblerPadding_ltrIsEnabled_port_paddingStartAndPaddingEndForLtrIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setExplorationSplitViewPadding(
        textView,
        /* isApplicable= */ true,
        /* paddingStart= */ 12f,
        /* paddingTop= */ 16f,
        /* paddingEnd= */ 16f,
        /* paddingBottom= */ 12f
      )
      return@runWithActivity textView
    }
    /*
     * Note that the padding starts/ends below match the ones set above because, when the adapters are
     * working correctly in RTL mode, the start/end should be exactly the start/end originally set.
     */
    assertThat(textView.paddingStart.toFloat()).isWithin(TOLERANCE).of(12f)
    assertThat(textView.paddingEnd.toFloat()).isWithin(TOLERANCE).of(16f)
  }

  @Config(qualifiers = "land")
  @Test
  fun testStateAssemblerPadding_ltrIsEnabled_landscape_paddingStartAndPaddingEndForLtrIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setExplorationSplitViewPadding(
        textView,
        /* isApplicable= */ true,
        /* paddingStart= */ 12f,
        /* paddingTop= */ 16f,
        /* paddingEnd= */ 16f,
        /* paddingBottom= */ 12f
      )
      return@runWithActivity textView
    }
    /*
     * Note that the padding starts/ends below match the ones set above because, when the adapters are
     * working correctly in RTL mode, the start/end should be exactly the start/end originally set.
     */
    assertThat(textView.paddingStart.toFloat()).isWithin(TOLERANCE).of(12f)
    assertThat(textView.paddingEnd.toFloat()).isWithin(TOLERANCE).of(16f)
  }

  @Config(qualifiers = "sw600dp-port")
  @Test
  fun testStateAssemblerPadding_ltrEnabled__port_tablet_paddingStartAndPaddingEndForLtrIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setExplorationSplitViewPadding(
        textView,
        /* isApplicable= */ true,
        /* paddingStart= */ 12f,
        /* paddingTop= */ 16f,
        /* paddingEnd= */ 16f,
        /* paddingBottom= */ 12f
      )
      return@runWithActivity textView
    }
    /*
     * Note that the padding starts/ends below match the ones set above because, when the adapters are
     * working correctly in RTL mode, the start/end should be exactly the start/end originally set.
     */
    assertThat(textView.paddingStart.toFloat()).isWithin(TOLERANCE).of(12f)
    assertThat(textView.paddingEnd.toFloat()).isWithin(TOLERANCE).of(16f)
  }

  @Config(qualifiers = "sw600dp-land")
  @Test
  fun testStateAssemblerPadding_ltrEnabled_landscape_tablet_paddingStartAndEndForLtrIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setExplorationSplitViewPadding(
        textView,
        /* isApplicable= */ true,
        /* paddingStart= */ 12f,
        /* paddingTop= */ 16f,
        /* paddingEnd= */ 16f,
        /* paddingBottom= */ 12f
      )
      return@runWithActivity textView
    }
    /*
     * Note that the padding starts/ends below match the ones set above because, when the adapters are
     * working correctly in RTL mode, the start/end should be exactly the start/end originally set.
     */
    assertThat(textView.paddingStart.toFloat()).isWithin(TOLERANCE).of(12f)
    assertThat(textView.paddingEnd.toFloat()).isWithin(TOLERANCE).of(16f)
  }

  @Config(qualifiers = "port")
  @Test
  fun testStateAssemblerPadding_rtlIsEnabled_port_paddingStartAndPaddingEndForRtlIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      ViewCompat.setLayoutDirection(textView, ViewCompat.LAYOUT_DIRECTION_RTL)
      setExplorationSplitViewPadding(
        textView,
        /* isApplicable= */ true,
        /* paddingStart= */ 12f,
        /* paddingTop= */ 16f,
        /* paddingEnd= */ 16f,
        /* paddingBottom= */ 12f
      )
      return@runWithActivity textView
    }
    /*
     * Note that the padding starts/ends below match the ones set above because, when the adapters are
     * working correctly in RTL mode, the start/end should be exactly the start/end originally set.
     */
    assertThat(textView.paddingStart.toFloat()).isWithin(TOLERANCE).of(12f)
    assertThat(textView.paddingEnd.toFloat()).isWithin(TOLERANCE).of(16f)
  }

  @Config(qualifiers = "land")
  @Test
  fun testStateAssemblerPadding_rtlIsEnabled_landscape_paddingStartAndpaddingEndForRtlIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      ViewCompat.setLayoutDirection(textView, ViewCompat.LAYOUT_DIRECTION_RTL)
      setExplorationSplitViewPadding(
        textView,
        /* isApplicable= */ true,
        /* paddingStart= */ 12f,
        /* paddingTop= */ 16f,
        /* paddingEnd= */ 16f,
        /* paddingBottom= */ 12f
      )
      return@runWithActivity textView
    }
    /*
     * Note that the padding starts/ends below match the ones set above because, when the adapters are
     * working correctly in RTL mode, the start/end should be exactly the start/end originally set.
     */
    assertThat(textView.paddingStart.toFloat()).isWithin(TOLERANCE).of(12f)
    assertThat(textView.paddingEnd.toFloat()).isWithin(TOLERANCE).of(16f)
  }

  @Config(qualifiers = "sw600dp-port")
  @Test
  fun testStateAssemblerPadding_rtlIsEnabled_port_tablet_paddingStartAndEndForRtlIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      ViewCompat.setLayoutDirection(textView, ViewCompat.LAYOUT_DIRECTION_RTL)
      setExplorationSplitViewPadding(
        textView,
        /* isApplicable= */ true,
        /* paddingStart= */ 12f,
        /* paddingTop= */ 16f,
        /* paddingEnd= */ 16f,
        /* paddingBottom= */ 12f
      )
      return@runWithActivity textView
    }
    /*
     * Note that the padding starts/ends below match the ones set above because, when the adapters are
     * working correctly in RTL mode, the start/end should be exactly the start/end originally set.
     */
    assertThat(textView.paddingStart.toFloat()).isWithin(TOLERANCE).of(12f)
    assertThat(textView.paddingEnd.toFloat()).isWithin(TOLERANCE).of(16f)
  }

  @Config(qualifiers = "sw600dp-land")
  @Test
  fun testStateAssemblerPadding_rtlEnabled_landscape_tablet_paddingStartAndEndForRtlIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      ViewCompat.setLayoutDirection(textView, ViewCompat.LAYOUT_DIRECTION_RTL)
      setExplorationSplitViewPadding(
        textView,
        /* isApplicable= */ true,
        /* paddingStart= */ 12f,
        /* paddingTop= */ 16f,
        /* paddingEnd= */ 16f,
        /* paddingBottom= */ 12f
      )
      return@runWithActivity textView
    }
    /*
     * Note that the padding starts/ends below match the ones set above because, when the adapters are
     * working correctly in RTL mode, the start/end should be exactly the start/end originally set.
     */
    assertThat(textView.paddingStart.toFloat()).isWithin(TOLERANCE).of(12f)
    assertThat(textView.paddingEnd.toFloat()).isWithin(TOLERANCE).of(16f)
  }

  private inline fun <reified V, A : Activity> ActivityScenario<A>.runWithActivity(
    crossinline action: (A) -> V
  ): V {
    // Use Mockito to ensure the routine is actually executed before returning the result.
    @Suppress("UNCHECKED_CAST") // The unsafe cast is necessary to make the routine generic.
    val fakeMock: Consumer<V> = mock(Consumer::class.java) as Consumer<V>
    val valueCaptor = ArgumentCaptor.forClass(V::class.java)
    onActivity { fakeMock.consume(action(it)) }
    verify(fakeMock).consume(valueCaptor.capture())
    return valueCaptor.value
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
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
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class, SyncStatusModule::class,
      MetricLogSchedulerModule::class, TestingBuildFlavorModule::class,
      ActivityRouterModule::class,
      CpuPerformanceSnapshotterModule::class, ExplorationProgressModule::class,
      TestAuthenticationModule::class
    ]
  )

  /** Create a TestApplicationComponent. */
  interface TestApplicationComponent : ApplicationComponent {
    /** Build the TestApplicationComponent. */
    @Component.Builder
    interface Builder : ApplicationComponent.Builder {
      override fun build(): TestApplicationComponent
    }

    /** Inject [StateAssemblerPaddingBindingAdaptersTest] in TestApplicationComponent . */
    fun inject(StateAssemblerPaddingBindingAdaptersTest: StateAssemblerPaddingBindingAdaptersTest)
  }

  /**
   * Class to override a dependency throughout the test application, instead of overriding the
   * dependencies in every test class, we can just do it once by extending the Application class.
   */
  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerStateAssemblerPaddingBindingAdaptersTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    /** Inject [StateAssemblerPaddingBindingAdaptersTest] in TestApplicationComponent . */
    fun inject(stateAssemblerPaddingBindingAdaptersTest: StateAssemblerPaddingBindingAdaptersTest) {
      component.inject(stateAssemblerPaddingBindingAdaptersTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }

  private interface Consumer<T> {
    /** Represents an operation that accepts a single input argument and returns no result. */
    fun consume(value: T)
  }
}
