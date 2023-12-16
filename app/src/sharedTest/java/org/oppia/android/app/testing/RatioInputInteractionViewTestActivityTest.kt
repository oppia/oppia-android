package org.oppia.android.app.testing

import android.app.Application
import android.content.res.Configuration
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import dagger.Component
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Before
import org.junit.Ignore
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
import org.oppia.android.app.customview.interaction.RatioInputInteractionView
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
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
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.DisableAccessibilityChecks
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.espresso.EditTextInputAction
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
import org.oppia.android.util.logging.EventLoggingConfigurationModule
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

/** Tests for [RatioInputInteractionViewTestActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = RatioInputInteractionViewTestActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class RatioInputInteractionViewTestActivityTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var editTextInputAction: EditTextInputAction

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
  fun testRatioInput_withNoInput_hasCorrectPendingAnswerType() {
    val activityScenario = ActivityScenario.launch(
      RatioInputInteractionViewTestActivity::class.java
    )
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.ratioExpressionInputInteractionViewModel.getPendingAnswer()
      Truth.assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      Truth.assertThat(pendingAnswer.answer.ratioExpression.ratioComponentCount).isEqualTo(0)
    }
  }

  @Test
  fun testRatioInput_withRatioOfNumber_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(
      RatioInputInteractionViewTestActivity::class.java
    )
    Espresso.onView(ViewMatchers.withId(R.id.test_ratio_input_interaction_view))
      .perform(
        setTextToRatioInputInteractionView(
          "1:2:3"
        )
      )
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.ratioExpressionInputInteractionViewModel.getPendingAnswer()
      Truth.assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      Truth.assertThat(pendingAnswer.answer.objectTypeCase).isEqualTo(
        InteractionObject.ObjectTypeCase.RATIO_EXPRESSION
      )
      Truth.assertThat(pendingAnswer.answer.ratioExpression.ratioComponentList)
        .isEqualTo(listOf(1, 2, 3))
    }
  }

  @Test
  @Ignore("Landscape not properly supported") // TODO(#56): Reenable once landscape is supported.
  fun testRatioInput_withRatio_configChange_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(
      RatioInputInteractionViewTestActivity::class.java
    )
    Espresso.onView(ViewMatchers.withId(R.id.test_ratio_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "1:2"
        )
      )
    activityScenario.onActivity { activity ->
      activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    }
    Espresso.onView(ViewMatchers.withId(R.id.test_ratio_input_interaction_view))
      .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
      .check(ViewAssertions.matches(ViewMatchers.withText("1:2")))
  }

  @Test
  fun testRatioInput_withTwoColonsTogether_colonsTogetherFormatErrorIsDisplayed() {
    ActivityScenario.launch(RatioInputInteractionViewTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      Espresso.onView(ViewMatchers.withId(R.id.test_ratio_input_interaction_view))
        .perform(
          setTextToRatioInputInteractionView(
            "1::2"
          )
        )
      testCoroutineDispatchers.runCurrent()
      Espresso.onView(ViewMatchers.withId(R.id.ratio_input_error))
        .check(
          ViewAssertions.matches(
            ViewMatchers.withText(
              R.string.ratio_error_invalid_colons
            )
          )
        )
    }
  }

  @Test
  fun testRatioInput_withNegativeRatioOfNumber_numberFormatErrorIsDisplayed() {
    ActivityScenario.launch(RatioInputInteractionViewTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      Espresso.onView(ViewMatchers.withId(R.id.test_ratio_input_interaction_view))
        .perform(
          setTextToRatioInputInteractionView(
            "-1:2:3:4"
          )
        )
      testCoroutineDispatchers.runCurrent()
      Espresso.onView(ViewMatchers.withId(R.id.ratio_input_error))
        .check(
          ViewAssertions.matches(
            ViewMatchers.withText(
              R.string.ratio_error_invalid_chars
            )
          )
        )
    }
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as RatioInputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testRatioInput_withBlankInput_submit_numberWithZerosErrorIsDisplayed() {
    ActivityScenario.launch(RatioInputInteractionViewTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToSubmitButton()
      Espresso.onView(ViewMatchers.withId(R.id.submit_button))
        .check(ViewAssertions.matches(ViewMatchers.isDisplayed())).perform(ViewActions.click())
      Espresso.onView(ViewMatchers.withId(R.id.ratio_input_error))
        .check(
          ViewAssertions.matches(
            ViewMatchers.withText(
              R.string.ratio_error_empty_input
            )
          )
        )
    }
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as RatioInputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testRatioInput_withZeroRatio_submit_numberWithZerosErrorIsDisplayed() {
    ActivityScenario.launch(RatioInputInteractionViewTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      Espresso.onView(ViewMatchers.withId(R.id.test_ratio_input_interaction_view))
        .perform(
          setTextToRatioInputInteractionView(
            "1:0:4"
          )
        )
      testCoroutineDispatchers.runCurrent()
      scrollToSubmitButton()
      Espresso.onView(ViewMatchers.withId(R.id.submit_button))
        .check(ViewAssertions.matches(ViewMatchers.isDisplayed())).perform(ViewActions.click())
      Espresso.onView(ViewMatchers.withId(R.id.ratio_input_error))
        .check(
          ViewAssertions.matches(
            ViewMatchers.withText(
              R.string.ratio_error_includes_zero
            )
          )
        )
    }
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as RatioInputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testRatioInput_withInvalidRatio_submit_numberFormatErrorIsDisplayed() {
    ActivityScenario.launch(RatioInputInteractionViewTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      Espresso.onView(ViewMatchers.withId(R.id.test_ratio_input_interaction_view))
        .perform(
          setTextToRatioInputInteractionView(
            "1: 1 2 :4"
          )
        )
      Espresso.closeSoftKeyboard()
      testCoroutineDispatchers.runCurrent()
      scrollToSubmitButton()
      Espresso.onView(ViewMatchers.withId(R.id.submit_button))
        .check(ViewAssertions.matches(ViewMatchers.isDisplayed())).perform(ViewActions.click())
      testCoroutineDispatchers.runCurrent()
      Espresso.onView(ViewMatchers.withId(R.id.ratio_input_error))
        .check(
          ViewAssertions.matches(
            ViewMatchers.withText(
              R.string.ratio_error_invalid_format
            )
          )
        )
    }
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as RatioInputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testRatioInput_withRatioHaving4Terms_submit_invalidSizeErrorIsDisplayed() {
    ActivityScenario.launch(RatioInputInteractionViewTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      Espresso.onView(ViewMatchers.withId(R.id.test_ratio_input_interaction_view))
        .perform(
          setTextToRatioInputInteractionView(
            "1:2:3:4"
          )
        )
      Espresso.closeSoftKeyboard()
      testCoroutineDispatchers.runCurrent()
      scrollToSubmitButton()
      Espresso.onView(ViewMatchers.withId(R.id.submit_button))
        .check(ViewAssertions.matches(ViewMatchers.isDisplayed())).perform(ViewActions.click())
      Espresso.onView(ViewMatchers.withId(R.id.ratio_input_error))
        .check(
          ViewAssertions.matches(
            ViewMatchers.withText(
              R.string.ratio_error_invalid_size
            )
          )
        )
    }
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as RatioInputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testRatioInput_withRatioHaving2Terms_submit_invalidSizeErrorIsDisplayed() {
    ActivityScenario.launch(RatioInputInteractionViewTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      Espresso.onView(ViewMatchers.withId(R.id.test_ratio_input_interaction_view))
        .perform(
          setTextToRatioInputInteractionView(
            "1:2"
          )
        )
      Espresso.closeSoftKeyboard()
      testCoroutineDispatchers.runCurrent()
      scrollToSubmitButton()
      Espresso.onView(ViewMatchers.withId(R.id.submit_button))
        .check(ViewAssertions.matches(ViewMatchers.isDisplayed())).perform(ViewActions.click())
      Espresso.onView(ViewMatchers.withId(R.id.ratio_input_error))
        .check(
          ViewAssertions.matches(
            ViewMatchers.withText(
              R.string.ratio_error_invalid_size
            )
          )
        )
    }
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as RatioInputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testRatioInput_withRatioHaving3Terms_submit_noErrorIsDisplayed() {
    ActivityScenario.launch(RatioInputInteractionViewTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      Espresso.onView(ViewMatchers.withId(R.id.test_ratio_input_interaction_view))
        .perform(
          setTextToRatioInputInteractionView(
            "1:2:3"
          )
        )
      Espresso.closeSoftKeyboard()
      scrollToSubmitButton()
      Espresso.onView(ViewMatchers.withId(R.id.submit_button))
        .check(ViewAssertions.matches(ViewMatchers.isDisplayed())).perform(ViewActions.click())
      Espresso.onView(ViewMatchers.withId(R.id.ratio_input_error))
        .check(ViewAssertions.matches(ViewMatchers.withText("")))
    }
  }

  private fun scrollToSubmitButton() {
    Espresso.onView(ViewMatchers.withId(R.id.submit_button)).perform(ViewActions.scrollTo())
    testCoroutineDispatchers.runCurrent()
  }

  private fun setTextToRatioInputInteractionView(
    newText: String?
  ): ViewAction? {
    return object : ViewAction {
      override fun getConstraints(): Matcher<View> {
        return CoreMatchers.allOf(
          ViewMatchers.isDisplayed(),
          ViewMatchers.isAssignableFrom(RatioInputInteractionView::class.java)
        )
      }

      override fun getDescription(): String {
        return "Update the text from the custom EditText"
      }

      override fun perform(uiController: UiController?, view: View) {
        (view as RatioInputInteractionView).setText(newText)
        uiController?.loopMainThreadUntilIdle()
      }
    }
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
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
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
      EventLoggingConfigurationModule::class, ActivityRouterModule::class,
      CpuPerformanceSnapshotterModule::class, ExplorationProgressModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(ratioInputInteractionViewTestActivityTest: RatioInputInteractionViewTestActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerRatioInputInteractionViewTestActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(inputInteractionViewTestActivityTest: RatioInputInteractionViewTestActivityTest) {
      component.inject(inputInteractionViewTestActivityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
