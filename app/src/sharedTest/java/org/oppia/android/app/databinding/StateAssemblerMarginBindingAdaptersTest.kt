package org.oppia.android.app.databinding

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.databinding.StateAssemblerMarginBindingAdapters.setExplorationSplitViewMargin
import org.oppia.android.app.databinding.StateAssemblerMarginBindingAdapters.setQuestionSplitViewMargin
import org.oppia.android.app.databinding.StateAssemblerMarginBindingAdapters.setQuestionViewMargin
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.StateAssemblerMarginBindingAdaptersTestActivity
import org.oppia.android.app.topic.PracticeTabModule
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
import org.oppia.android.testing.TestImageLoaderModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

private const val TOLERANCE = 1e-5f

/** Tests for [StateAssemblerMarginBindingAdapters]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = StateAssemblerMarginBindingAdaptersTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class StateAssemblerMarginBindingAdaptersTest {

  @Inject
  lateinit var context: Context

  @get:Rule
  var activityRule: ActivityScenarioRule<StateAssemblerMarginBindingAdaptersTestActivity> =
    ActivityScenarioRule(
      Intent(
        ApplicationProvider.getApplicationContext(),
        StateAssemblerMarginBindingAdaptersTestActivity::class.java
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
  fun testMarginBindingAdapters_explorationViewMargin_ltrIsEnabled_marginsForLtrIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setExplorationViewMargins(textView)
      return@runWithActivity textView
    }
    assertThat(textView.marginStart.toFloat()).isWithin(TOLERANCE).of(
      convertToPx(24f)
    )
    assertThat(textView.marginEnd.toFloat()).isWithin(TOLERANCE).of(
      convertToPx(40f)
    )
  }

  @Test
  fun testMarginBindingAdapters_explorationViewMargin_rtlIsEnabled_marginsForRtlIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setExplorationViewMargins(textView)
      textView.layoutDirection = View.LAYOUT_DIRECTION_RTL
      return@runWithActivity textView
    }
    assertThat(textView.marginEnd.toFloat()).isWithin(TOLERANCE).of(
      convertToPx(24f)
    )
    assertThat(textView.marginStart.toFloat()).isWithin(TOLERANCE).of(
      convertToPx(40f)
    )
  }

  @Test
  fun testMarginBindingAdapters_questionViewMargin_ltrIsEnabled_marginsForLtrIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setQuestionViewMargins(textView)
      return@runWithActivity textView
    }
    assertThat(textView.marginStart.toFloat()).isWithin(TOLERANCE).of(
      convertToPx(24f)
    )
    assertThat(textView.marginEnd.toFloat()).isWithin(TOLERANCE).of(
      convertToPx(40f)
    )
  }

  @Test
  fun testMarginBindingAdapters_questionViewMargin_rtlIsEnabled_marginsForRtlIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setQuestionViewMargins(textView)
      textView.layoutDirection = View.LAYOUT_DIRECTION_RTL
      return@runWithActivity textView
    }
    assertThat(textView.marginEnd.toFloat()).isWithin(TOLERANCE).of(
      convertToPx(24f)
    )
    assertThat(textView.marginStart.toFloat()).isWithin(TOLERANCE).of(
      convertToPx(40f)
    )
  }

  @Test
  fun testMarginBindingAdapters_QuestionSplitViewMargin_ltrIsEnabled_marginsForLtrIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setQuestionSplitViewMargins(textView)
      return@runWithActivity textView
    }
    assertThat(textView.marginStart.toFloat()).isWithin(TOLERANCE).of(
      convertToPx(24f)
    )
    assertThat(textView.marginEnd.toFloat()).isWithin(TOLERANCE).of(
      convertToPx(40f)
    )
  }

  @Test
  fun testMarginBindingAdapters_questionSplitViewMargin_rtlIsEnabled_marginsForRtlIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setQuestionSplitViewMargins(textView)
      textView.layoutDirection = View.LAYOUT_DIRECTION_RTL
      return@runWithActivity textView
    }
    assertThat(textView.marginEnd.toFloat()).isWithin(TOLERANCE).of(
      convertToPx(24f)
    )
    assertThat(textView.marginStart.toFloat()).isWithin(TOLERANCE).of(
      convertToPx(40f)
    )
  }

  @Config(qualifiers = "port")
  @Test
  fun testMarginBindingAdapters_ltrIsEnabled_port_marginStartAndMarginEndForLtrIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setExplorationViewMargins(textView)
      return@runWithActivity textView
    }
    assertThat(textView.marginStart.toFloat()).isWithin(TOLERANCE).of(
      convertToPx(24f)
    )
    assertThat(textView.marginEnd.toFloat()).isWithin(TOLERANCE).of(
      convertToPx(40f)
    )
  }

  @Config(qualifiers = "land")
  @Test
  fun testMarginBindingAdapters_ltrIsEnabled_landscape_marginStartAndMarginEndForLtrIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setExplorationViewMargins(textView)
      return@runWithActivity textView
    }
    assertThat(textView.marginStart.toFloat()).isWithin(TOLERANCE).of(
      convertToPx(24f)
    )
    assertThat(textView.marginEnd.toFloat()).isWithin(TOLERANCE).of(
      convertToPx(40f)
    )
  }

  @Config(qualifiers = "sw600dp-port")
  @Test
  fun testMarginBindingAdapters_ltrEnabled__port_tablet_marginStartAndMarginEndForLtrIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setExplorationViewMargins(textView)
      return@runWithActivity textView
    }
    assertThat(textView.marginStart.toFloat()).isWithin(TOLERANCE).of(
      convertToPx(24f)
    )
    assertThat(textView.marginEnd.toFloat()).isWithin(TOLERANCE).of(
      convertToPx(40f)
    )
  }

  @Config(qualifiers = "sw600dp-land")
  @Test
  fun testMarginBindingAdapters_ltrEnabled_landscape_tablet_marginStartAndEndForLtrIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setExplorationViewMargins(textView)
      return@runWithActivity textView
    }
    assertThat(textView.marginStart.toFloat()).isWithin(TOLERANCE).of(
      convertToPx(24f)
    )
    assertThat(textView.marginEnd.toFloat()).isWithin(TOLERANCE).of(
      convertToPx(40f)
    )
  }

  @Config(qualifiers = "port")
  @Test
  fun testMarginBindingAdapters_rtlIsEnabled_port_marginStartAndMarginEndForRtlIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setExplorationViewMargins(textView)
      textView.layoutDirection = View.LAYOUT_DIRECTION_RTL
      return@runWithActivity textView
    }
    assertThat(textView.marginEnd.toFloat()).isWithin(TOLERANCE).of(
      convertToPx(24f)
    )
    assertThat(textView.marginStart.toFloat()).isWithin(TOLERANCE).of(
      convertToPx(40f)
    )
  }

  @Config(qualifiers = "land")
  @Test
  fun testMarginBindingAdapters_rtlIsEnabled_landscape_marginStartAndMarginEndForRtlIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setExplorationViewMargins(textView)
      textView.layoutDirection = View.LAYOUT_DIRECTION_RTL
      return@runWithActivity textView
    }
    assertThat(textView.marginEnd.toFloat()).isWithin(TOLERANCE).of(
      convertToPx(24f)
    )
    assertThat(textView.marginStart.toFloat()).isWithin(TOLERANCE).of(
      convertToPx(40f)
    )
  }

  @Config(qualifiers = "sw600dp-port")
  @Test
  fun testMarginBindingAdapters_rtlIsEnabled_port_tablet_marginStartAndMarginEndForRtlIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setExplorationViewMargins(textView)
      textView.layoutDirection = View.LAYOUT_DIRECTION_RTL
      return@runWithActivity textView
    }
    assertThat(textView.marginEnd.toFloat()).isWithin(TOLERANCE).of(
      convertToPx(24f)
    )
    assertThat(textView.marginStart.toFloat()).isWithin(TOLERANCE).of(
      convertToPx(40f)
    )
  }

  @Config(qualifiers = "sw600dp-land")
  @Test
  fun testMarginBindingAdapters_rtlEnabled_landscape_tablet_marginStartAndEndForRtlIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setExplorationViewMargins(textView)
      textView.layoutDirection = View.LAYOUT_DIRECTION_RTL
      return@runWithActivity textView
    }
    assertThat(textView.marginEnd.toFloat()).isWithin(TOLERANCE).of(
      convertToPx(24f)
    )
    assertThat(textView.marginStart.toFloat()).isWithin(TOLERANCE).of(
      convertToPx(40f)
    )
  }

  private fun setQuestionViewMargins(textView: TextView) {
    setQuestionViewMargin(
      textView,
      /* isApplicable= */ true,
      convertToPx(24f),
      convertToPx(24f),
      convertToPx(40f),
      convertToPx(24f)
    )
  }

  private fun setQuestionSplitViewMargins(textView: TextView) {
    setQuestionSplitViewMargin(
      textView,
      /* isApplicable= */ true,
      convertToPx(24f),
      convertToPx(24f),
      convertToPx(40f),
      convertToPx(24f)
    )
  }

  private fun setExplorationViewMargins(textView: TextView) {
    setExplorationSplitViewMargin(
      textView,
      /* isApplicable= */ true,
      convertToPx(24f),
      convertToPx(24f),
      convertToPx(40f),
      convertToPx(24f)
    )
  }

  private fun convertToPx(dip: Float): Float {
    val r: Resources = context.resources
    return TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_PX,
      dip,
      r.displayMetrics
    )
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
      PlatformParameterModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, TestImageLoaderModule::class, ImageParsingModule::class,
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

    fun inject(StateAssemblerMarginBindingAdaptersTest: StateAssemblerMarginBindingAdaptersTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerStateAssemblerMarginBindingAdaptersTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(htmlParserTest: StateAssemblerMarginBindingAdaptersTest) {
      component.inject(htmlParserTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }

  private interface Consumer<T> {
    fun consume(value: T)
  }
}
