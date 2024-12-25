package org.oppia.android.app.databinding

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
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
import org.oppia.android.app.databinding.MarginBindingAdapters.setLayoutMarginBottom
import org.oppia.android.app.databinding.MarginBindingAdapters.setLayoutMarginEnd
import org.oppia.android.app.databinding.MarginBindingAdapters.setLayoutMarginStart
import org.oppia.android.app.databinding.MarginBindingAdapters.setLayoutMarginTop
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.MarginBindingAdaptersTestActivity
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
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

private const val TOLERANCE = 1e-5f

/** Tests for [MarginBindingAdapters]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = MarginBindingAdaptersTest.TestApplication::class, qualifiers = "port-xxhdpi")
class MarginBindingAdaptersTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @get:Rule
  var activityRule: ActivityScenarioRule<MarginBindingAdaptersTestActivity> =
    ActivityScenarioRule(
      Intent(
        ApplicationProvider.getApplicationContext(),
        MarginBindingAdaptersTestActivity::class.java
      )
    )

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    Intents.init()
    testCoroutineDispatchers.registerIdlingResource()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  @Config(qualifiers = "land")
  @Test
  fun testMarginBindableAdapters_landscape_topAndBottomIsCorrect() {
    testMarginBindableAdapters_topAndBottomIsCorrect()
  }

  @Config(qualifiers = "sw600dp-land")
  @Test
  fun testMarginBindableAdapters_landscapeWide_topAndBottomIsCorrect() {
    testMarginBindableAdapters_topAndBottomIsCorrect()
  }

  @Config(qualifiers = "port")
  @Test
  fun testMarginBindableAdapters_ltrIsEnabled_port_marginStartAndMarginEndForLtrIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setLayoutMarginStart(textView, /* marginStart= */ 24f)
      setLayoutMarginEnd(textView, /* marginEnd= */ 40f)
      return@runWithActivity textView
    }
    /*
     * Note that the margin starts/ends below match the ones set above because, when the adapters are
     * working correctly in RTL mode, the start/end should be exactly the start/end originally set.
     */
    assertThat(textView.marginStart.toFloat()).isWithin(TOLERANCE).of(24f)
    assertThat(textView.marginEnd.toFloat()).isWithin(TOLERANCE).of(40f)
  }

  @Config(qualifiers = "land")
  @Test
  fun testMarginBindableAdapters_ltrIsEnabled_landscape_marginStartAndMarginEndForLtrIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setLayoutMarginStart(textView, /* marginStart= */ 24f)
      setLayoutMarginEnd(textView, /* marginEnd= */ 40f)
      return@runWithActivity textView
    }
    /*
     * Note that the margin starts/ends below match the ones set above because, when the adapters are
     * working correctly in RTL mode, the start/end should be exactly the start/end originally set.
     */
    assertThat(textView.marginStart.toFloat()).isWithin(TOLERANCE).of(24f)
    assertThat(textView.marginEnd.toFloat()).isWithin(TOLERANCE).of(40f)
  }

  @Config(qualifiers = "sw600dp-port")
  @Test
  fun testMarginBindableAdapters_ltrEnabled_port_tablet_marginStartAndMarginEndForLtrIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setLayoutMarginStart(textView, /* marginStart= */ 24f)
      setLayoutMarginEnd(textView, /* marginEnd= */ 40f)
      return@runWithActivity textView
    }
    /*
     * Note that the margin starts/ends below match the ones set above because, when the adapters are
     * working correctly in RTL mode, the start/end should be exactly the start/end originally set.
     */
    assertThat(textView.marginStart.toFloat()).isWithin(TOLERANCE).of(24f)
    assertThat(textView.marginEnd.toFloat()).isWithin(TOLERANCE).of(40f)
  }

  @Config(qualifiers = "sw600dp-land")
  @Test
  fun testMarginBindableAdapters_ltrEnabled_landscape_tablet_marginStartAndEndForLtrIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setLayoutMarginStart(textView, /* marginStart= */ 24f)
      setLayoutMarginEnd(textView, /* marginEnd= */ 40f)
      return@runWithActivity textView
    }
    /*
     * Note that the margin starts/ends below match the ones set above because, when the adapters are
     * working correctly in RTL mode, the start/end should be exactly the start/end originally set.
     */
    assertThat(textView.marginStart.toFloat()).isWithin(TOLERANCE).of(24f)
    assertThat(textView.marginEnd.toFloat()).isWithin(TOLERANCE).of(40f)
  }

  @Config(qualifiers = "port")
  @Test
  fun testMarginBindableAdapters_rtlIsEnabled_port_marginStartAndMarginEndForRtlIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      ViewCompat.setLayoutDirection(textView, ViewCompat.LAYOUT_DIRECTION_RTL)
      setLayoutMarginStart(textView, /* marginStart= */ 24f)
      setLayoutMarginEnd(textView, /* marginEnd= */ 40f)
      return@runWithActivity textView
    }
    /*
     * Note that the margin starts/ends below match the ones set above because, when the adapters are
     * working correctly in RTL mode, the start/end should be exactly the start/end originally set.
     */
    assertThat(textView.marginStart.toFloat()).isWithin(TOLERANCE).of(24f)
    assertThat(textView.marginEnd.toFloat()).isWithin(TOLERANCE).of(40f)
  }

  @Config(qualifiers = "land")
  @Test
  fun testMarginBindableAdapters_rtlIsEnabled_landscape_marginStartAndMarginEndForRtlIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      ViewCompat.setLayoutDirection(textView, ViewCompat.LAYOUT_DIRECTION_RTL)
      setLayoutMarginStart(textView, /* marginStart= */ 24f)
      setLayoutMarginEnd(textView, /* marginEnd= */ 40f)
      return@runWithActivity textView
    }
    /*
     * Note that the margin starts/ends below match the ones set above because, when the adapters are
     * working correctly in RTL mode, the start/end should be exactly the start/end originally set.
     */
    assertThat(textView.marginStart.toFloat()).isWithin(TOLERANCE).of(24f)
    assertThat(textView.marginEnd.toFloat()).isWithin(TOLERANCE).of(40f)
  }

  @Config(qualifiers = "sw600dp-port")
  @Test
  fun testMarginBindableAdapters_rtlIsEnabled_port_tablet_marginStartAndMarginEndForRtlIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      ViewCompat.setLayoutDirection(textView, ViewCompat.LAYOUT_DIRECTION_RTL)
      setLayoutMarginStart(textView, /* marginStart= */ 24f)
      setLayoutMarginEnd(textView, /* marginEnd= */ 40f)
      return@runWithActivity textView
    }
    /*
     * Note that the margin starts/ends below match the ones set above because, when the adapters are
     * working correctly in RTL mode, the start/end should be exactly the start/end originally set.
     */
    assertThat(textView.marginStart.toFloat()).isWithin(TOLERANCE).of(24f)
    assertThat(textView.marginEnd.toFloat()).isWithin(TOLERANCE).of(40f)
  }

  @Config(qualifiers = "sw600dp-land")
  @Test
  fun testMarginBindableAdapters_rtlEnabled_landscape_tablet_marginStartAndEndForRtlIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      ViewCompat.setLayoutDirection(textView, ViewCompat.LAYOUT_DIRECTION_RTL)
      setLayoutMarginStart(textView, /* marginStart= */ 24f)
      setLayoutMarginEnd(textView, /* marginEnd= */ 40f)
      return@runWithActivity textView
    }
    /*
     * Note that the margin starts/ends below match the ones set above because, when the adapters are
     * working correctly in RTL mode, the start/end should be exactly the start/end originally set.
     */
    assertThat(textView.marginStart.toFloat()).isWithin(TOLERANCE).of(24f)
    assertThat(textView.marginEnd.toFloat()).isWithin(TOLERANCE).of(40f)
  }

  @Config(qualifiers = "port")
  @Test
  fun testMarginBindableAdapters_setLayoutParams_preservesMargins() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)

      // Set initial margins
      setLayoutMarginStart(textView, /* marginStart= */ 24f)
      setLayoutMarginEnd(textView, /* marginEnd= */ 40f)
      setLayoutMarginTop(textView, /* marginTop= */ 16f)
      setLayoutMarginBottom(textView, /* marginBottom= */ 32f)

      return@runWithActivity textView
    }

    assertThat(textView.marginStart.toFloat()).isWithin(TOLERANCE).of(24f)
    assertThat(textView.marginEnd.toFloat()).isWithin(TOLERANCE).of(40f)

    val layoutParams = textView.layoutParams as ViewGroup.MarginLayoutParams
    assertThat(layoutParams.topMargin.toFloat()).isWithin(TOLERANCE).of(16f)
    assertThat(layoutParams.bottomMargin.toFloat()).isWithin(TOLERANCE).of(32f)
  }

  @Config(qualifiers = "land")
  @Test
  fun testMarginBindableAdapters_landscapeMode_setLayoutParams_preservesMargins() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)

      // Set initial margins
      setLayoutMarginStart(textView, /* marginStart= */ 24f)
      setLayoutMarginEnd(textView, /* marginEnd= */ 40f)
      setLayoutMarginTop(textView, /* marginTop= */ 16f)
      setLayoutMarginBottom(textView, /* marginBottom= */ 32f)

      return@runWithActivity textView
    }

    assertThat(textView.marginStart.toFloat()).isWithin(TOLERANCE).of(24f)
    assertThat(textView.marginEnd.toFloat()).isWithin(TOLERANCE).of(40f)

    val layoutParams = textView.layoutParams as ViewGroup.MarginLayoutParams
    assertThat(layoutParams.topMargin.toFloat()).isWithin(TOLERANCE).of(16f)
    assertThat(layoutParams.bottomMargin.toFloat()).isWithin(TOLERANCE).of(32f)
  }

  @Config(qualifiers = "sw600dp-port")
  @Test
  fun testMarginBindableAdapters_tabletMode_setLayoutParams_preservesMargins() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)

      // Set initial margins
      setLayoutMarginStart(textView, /* marginStart= */ 24f)
      setLayoutMarginEnd(textView, /* marginEnd= */ 40f)
      setLayoutMarginTop(textView, /* marginTop= */ 16f)
      setLayoutMarginBottom(textView, /* marginBottom= */ 32f)

      return@runWithActivity textView
    }

    assertThat(textView.marginStart.toFloat()).isWithin(TOLERANCE).of(24f)
    assertThat(textView.marginEnd.toFloat()).isWithin(TOLERANCE).of(40f)

    val layoutParams = textView.layoutParams as ViewGroup.MarginLayoutParams
    assertThat(layoutParams.topMargin.toFloat()).isWithin(TOLERANCE).of(16f)
    assertThat(layoutParams.bottomMargin.toFloat()).isWithin(TOLERANCE).of(32f)
  }

  private fun testMarginBindableAdapters_topAndBottomIsCorrect() {
    activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setLayoutMarginBottom(textView, /* marginBottom= */ 24f)
      setLayoutMarginTop(textView, /* marginTop= */ 40f)

      testCoroutineDispatchers.runCurrent()

      val computedTopMargin = textView.y
      val computedBottomMargin = (textView.parent as View).height - textView.y - textView.height
      assertThat(computedTopMargin).isWithin(TOLERANCE).of(40f)
      assertThat(computedBottomMargin).isWithin(TOLERANCE).of(24f)
    }
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

    /** Inject [MarginBindingAdaptersTest] in TestApplicationComponent . */
    fun inject(marginBindableAdaptersTest: MarginBindingAdaptersTest)
  }

  /**
   * Class to override a dependency throughout the test application, instead of overriding the
   * dependencies in every test class, we can just do it once by extending the Application class.
   */
  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerMarginBindingAdaptersTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    /** Inject [MarginBindingAdaptersTest] in TestApplicationComponent . */
    fun inject(marginBindableAdaptersTest: MarginBindingAdaptersTest) {
      component.inject(marginBindableAdaptersTest)
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
