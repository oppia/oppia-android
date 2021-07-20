package org.oppia.android.app.databinding

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
import org.oppia.android.app.databinding.StateAssemblerPaddingBindingAdapters.setExplorationSplitViewPadding
import org.oppia.android.app.databinding.StateAssemblerPaddingBindingAdapters.setExplorationViewPadding
import org.oppia.android.app.databinding.StateAssemblerPaddingBindingAdapters.setQuestionSplitViewPadding
import org.oppia.android.app.databinding.StateAssemblerPaddingBindingAdapters.setQuestionViewPadding
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.StateAssemblerPaddingBindingAdaptersTestActivity
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

/** Tests for [StateAssemblerPaddingBindingAdapters]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = StateAssemblerPaddingBindingAdaptersTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class StateAssemblerPaddingBindingAdaptersTest {

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
      setExplorationPadding(textView)
      return@runWithActivity textView
    }
    assertThat(textView.paddingStart.toFloat()).isEqualTo(
      getExplorationPaddingStart()
    )
    assertThat(textView.paddingEnd.toFloat()).isEqualTo(
      getExplorationPaddingEnd()
    )
  }

  @Test
  fun testStateAssemblerPadding_explorationViewPadding_rtlIsEnabled_paddingsForRtlIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setExplorationPadding(textView)
      textView.layoutDirection = View.LAYOUT_DIRECTION_RTL
      return@runWithActivity textView
    }
    assertThat(textView.paddingEnd.toFloat()).isEqualTo(
      getExplorationPaddingStart()
    )
    assertThat(textView.paddingStart.toFloat()).isEqualTo(
      getExplorationPaddingEnd()
    )
  }

  @Test
  fun testStateAssemblerPadding_questionViewPadding_ltrIsEnabled_paddingsForLtrIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setQuestionPadding(textView)
      return@runWithActivity textView
    }
    assertThat(textView.paddingStart.toFloat()).isEqualTo(
      getExplorationPaddingStart()
    )
    assertThat(textView.paddingEnd.toFloat()).isEqualTo(
      getExplorationPaddingEnd()
    )
  }

  @Test
  fun testStateAssemblerPadding_questionViewPadding_rtlIsEnabled_paddingsForRtlIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setQuestionPadding(textView)
      textView.layoutDirection = View.LAYOUT_DIRECTION_RTL
      return@runWithActivity textView
    }
    assertThat(textView.paddingEnd.toFloat()).isEqualTo(
      getExplorationPaddingStart()
    )
    assertThat(textView.paddingStart.toFloat()).isEqualTo(
      getExplorationPaddingEnd()
    )
  }

  @Test
  fun testStateAssemblerPadding_questionSplitViewPadding_ltrIsEnabled_paddingsForLtrIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setQuestionSplitPadding(textView)
      return@runWithActivity textView
    }
    assertThat(textView.paddingStart.toFloat()).isEqualTo(
      getExplorationPaddingStart()
    )
    assertThat(textView.paddingEnd.toFloat()).isEqualTo(
      getExplorationPaddingEnd()
    )
  }

  @Test
  fun testStateAssemblerPadding_QuestionSplitViewPadding_rtlIsEnabled_paddingsForRtlIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setQuestionSplitPadding(textView)
      textView.layoutDirection = View.LAYOUT_DIRECTION_RTL
      return@runWithActivity textView
    }
    assertThat(textView.paddingEnd.toFloat()).isEqualTo(
      getExplorationPaddingStart()
    )
    assertThat(textView.paddingStart.toFloat()).isEqualTo(
      getExplorationPaddingEnd()
    )
  }

  @Config(qualifiers = "port")
  @Test
  fun testStateAssemblerPadding_ltrIsEnabled_port_paddingStartAndpaddingEndForLtrIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setExplorationSplitPadding(textView)
      return@runWithActivity textView
    }
    assertThat(textView.paddingStart.toFloat()).isEqualTo(
      getExplorationPaddingStart()
    )
    assertThat(textView.paddingEnd.toFloat()).isEqualTo(
      getExplorationPaddingEnd()
    )
  }

  @Config(qualifiers = "land")
  @Test
  fun testStateAssemblerPadding_ltrIsEnabled_landscape_paddingStartAndpaddingEndForLtrIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setExplorationSplitPadding(textView)
      return@runWithActivity textView
    }
    assertThat(textView.paddingStart.toFloat()).isEqualTo(
      getExplorationPaddingStart()
    )
    assertThat(textView.paddingEnd.toFloat()).isEqualTo(
      getExplorationPaddingEnd()
    )
  }

  @Config(qualifiers = "sw600dp-port")
  @Test
  fun testStateAssemblerPadding_ltrEnabled__port_tablet_paddingStartAndpaddingEndForLtrIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setExplorationSplitPadding(textView)
      return@runWithActivity textView
    }
    assertThat(textView.paddingStart.toFloat()).isEqualTo(
      getExplorationPaddingStart()
    )
    assertThat(textView.paddingEnd.toFloat()).isEqualTo(
      getExplorationPaddingEnd()
    )
  }

  @Config(qualifiers = "sw600dp-land")
  @Test
  fun testStateAssemblerPadding_ltrEnabled_landscapeTablet_paddingStartAndEndForLtrIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setExplorationSplitPadding(textView)
      return@runWithActivity textView
    }
    assertThat(textView.paddingStart.toFloat()).isEqualTo(
      getExplorationPaddingStart()
    )
    assertThat(textView.paddingEnd.toFloat()).isEqualTo(
      getExplorationPaddingEnd()
    )
  }

  @Config(qualifiers = "port")
  @Test
  fun testStateAssemblerPadding_rtlIsEnabled_port_paddingStartAndpaddingEndForRtlIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setExplorationSplitPadding(textView)
      textView.layoutDirection = View.LAYOUT_DIRECTION_RTL
      return@runWithActivity textView
    }
    assertThat(textView.paddingEnd.toFloat()).isEqualTo(
      getExplorationPaddingStart()
    )
    assertThat(textView.paddingStart.toFloat()).isEqualTo(
      getExplorationPaddingEnd()
    )
  }

  @Config(qualifiers = "land")
  @Test
  fun testStateAssemblerPadding_rtlIsEnabled_landscape_paddingStartAndpaddingEndForRtlIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setExplorationSplitPadding(textView)
      textView.layoutDirection = View.LAYOUT_DIRECTION_RTL
      return@runWithActivity textView
    }
    assertThat(textView.paddingEnd.toFloat()).isEqualTo(
      getExplorationPaddingStart()
    )
    assertThat(textView.paddingStart.toFloat()).isEqualTo(
      getExplorationPaddingEnd()
    )
  }

  @Config(qualifiers = "sw600dp-port")
  @Test
  fun testStateAssemblerPadding_rtlIsEnabled_port_tablet_paddingStartAndEndForRtlIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setExplorationSplitPadding(textView)
      textView.layoutDirection = View.LAYOUT_DIRECTION_RTL
      return@runWithActivity textView
    }
    assertThat(textView.paddingEnd.toFloat()).isEqualTo(
      getExplorationPaddingStart()
    )
    assertThat(textView.paddingStart.toFloat()).isEqualTo(
      getExplorationPaddingEnd()
    )
  }

  @Config(qualifiers = "sw600dp-land")
  @Test
  fun testStateAssemblerPadding_rtlEnabled_landscape_tablet_paddingStartAndEndForRtlIsCorrect() {
    val textView = activityRule.scenario.runWithActivity {
      val textView: TextView = it.findViewById(R.id.test_margin_text_view)
      setExplorationSplitPadding(textView)
      textView.layoutDirection = View.LAYOUT_DIRECTION_RTL
      return@runWithActivity textView
    }
    assertThat(textView.paddingEnd.toFloat()).isEqualTo(
      getExplorationPaddingStart()
    )
    assertThat(textView.paddingStart.toFloat()).isEqualTo(
      getExplorationPaddingEnd()
    )
  }
  
  private fun setExplorationPadding(textView: TextView) {
    setExplorationViewPadding(
      textView,
      /* isApplicable= */ true,
      getExplorationPaddingStart(),
      getExplorationPaddingTop(),
      getExplorationPaddingEnd(),getExplorationPaddingBottom()
    )
  }

  private fun getExplorationPaddingEnd(): Float {
    return context.resources.getDimension(R.dimen.content_item_exploration_view_padding_end)
  }

  private fun getExplorationPaddingStart(): Float {
    return context.resources.getDimension(R.dimen.content_item_exploration_view_padding_start)
  }
  
  private fun getExplorationPaddingTop(): Float {
    return context.resources.getDimension(R.dimen.content_item_exploration_view_padding_top)
  }
  
  private fun getExplorationPaddingBottom(): Float {
    return context.resources.getDimension(R.dimen.content_item_exploration_view_padding_bottom)
  }

  private fun setExplorationSplitPadding(textView: TextView) {
    setExplorationSplitViewPadding(
      textView,
      /* isApplicable= */ true,
      getExplorationPaddingStart(),
      getExplorationPaddingTop(),
      getExplorationPaddingEnd(),
      getExplorationPaddingBottom()
    )
  }

  private fun setQuestionPadding(textView: TextView) {
    setQuestionViewPadding(
      textView,
      /* isApplicable= */ true,
      getExplorationPaddingStart(),
      getExplorationPaddingTop(),
      getExplorationPaddingEnd(),
      getExplorationPaddingBottom()
    )
  }

  private fun setQuestionSplitPadding(textView: TextView) {
    setQuestionSplitViewPadding(
      textView,
      /* isApplicable= */ true,
      getExplorationPaddingStart(),
      getExplorationPaddingTop(),
      getExplorationPaddingEnd(),
      getExplorationPaddingBottom()
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

    fun inject(StateAssemblerPaddingBindingAdaptersTest: StateAssemblerPaddingBindingAdaptersTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerStateAssemblerPaddingBindingAdaptersTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(htmlParserTest: StateAssemblerPaddingBindingAdaptersTest) {
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
