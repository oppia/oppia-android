package org.oppia.android.app.testing

import android.app.Application
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
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
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.DisableAccessibilityChecks
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.espresso.EditTextInputAction
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

/** Tests for [InputInteractionViewTestActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = InputInteractionViewTestActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class InputInteractionViewTestActivityTest {
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
  fun testNumericInput_withNoInput_hasCorrectPendingAnswerType() {
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.numericInputViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.real).isWithin(1e-5).of(0.0)
    }
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testNumericInput_withRealNumber_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_number_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "9"
        )
      )
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.numericInputViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.objectTypeCase).isEqualTo(
        InteractionObject.ObjectTypeCase.REAL
      )
      assertThat(pendingAnswer.answer.real).isEqualTo(9.0)
    }
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testNumericInput_withRealNumberWithDecimal_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_number_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "9.5"
        )
      )
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.numericInputViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer.objectTypeCase).isEqualTo(
        InteractionObject.ObjectTypeCase.REAL
      )
      assertThat(pendingAnswer.answer.real).isEqualTo(9.5)
    }
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testNumericInput_withNegativeRealNumber_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_number_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "-9.5"
        )
      )
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.numericInputViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer.objectTypeCase).isEqualTo(
        InteractionObject.ObjectTypeCase.REAL
      )
      assertThat(pendingAnswer.answer.real).isEqualTo(-9.5)
      assertThat(pendingAnswer.answer.real).isLessThan(0.0)
    }
  }

  @Test
  @Ignore("Landscape not properly supported") // TODO(#56): Reenable once landscape is supported.
  fun testNumberInput_withText_configChange_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_number_input_interaction_view))
      .perform(editTextInputAction.appendText("9"))
    activityScenario.onActivity { activity ->
      activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    }
    onView(withId(R.id.test_number_input_interaction_view)).check(matches(isDisplayed()))
      .check(matches(withText("9")))
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testNumericInput_withInvalidCharacter_invalidCharacterErrorIsDisplayed() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java).use {
      onView(withId(R.id.test_number_input_interaction_view))
        .perform(
          editTextInputAction.appendText(
            "/"
          )
        )
      onView(withId(R.id.number_input_error))
        .check(
          matches(
            withText(
              R.string.number_error_invalid_format
            )
          )
        )
    }
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testNumericInput_withBlankInput_submit_emptyInputErrorIsDisplayed() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java).use {
      scrollToSubmitButton()
      onView(withId(R.id.submit_button)).check(matches(isDisplayed())).perform(click())
      onView(withId(R.id.number_input_error))
        .check(
          matches(
            withText(
              R.string.number_error_empty_input
            )
          )
        )
    }
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testNumericInput_withLongNumber_submit_numberTooLongErrorIsDisplayed() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java).use {
      onView(withId(R.id.test_number_input_interaction_view))
        .perform(
          editTextInputAction.appendText(
            "-12345678.6787687678"
          )
        )
      closeSoftKeyboard()
      scrollToSubmitButton()
      onView(withId(R.id.submit_button)).check(matches(isDisplayed())).perform(click())
      onView(withId(R.id.number_input_error))
        .check(
          matches(
            withText(
              R.string.number_error_larger_than_fifteen_characters
            )
          )
        )
    }
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testNumericInput_withLongInteger_submit_numberTooLongErrorIsDisplayed() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java).use {
      onView(withId(R.id.test_number_input_interaction_view))
        .perform(
          editTextInputAction.appendText(
            "1234567886787687678"
          )
        )
      closeSoftKeyboard()
      scrollToSubmitButton()
      onView(withId(R.id.submit_button)).check(matches(isDisplayed())).perform(click())
      onView(withId(R.id.number_input_error))
        .check(
          matches(
            withText(
              R.string.number_error_larger_than_fifteen_characters
            )
          )
        )
    }
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testNumericInput_withMinusSymbol_submit_numberFormatErrorIsDisplayed() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java).use {
      onView(withId(R.id.test_number_input_interaction_view))
        .perform(
          editTextInputAction.appendText(
            "-"
          )
        )
      closeSoftKeyboard()
      scrollToSubmitButton()
      onView(withId(R.id.submit_button)).check(matches(isDisplayed())).perform(click())
      onView(withId(R.id.number_input_error))
        .check(
          matches(
            withText(
              R.string.number_error_invalid_format
            )
          )
        )
    }
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testNumericInput_withNegativeSymbolNotAt0_numberFormatErrorIsDisplayed() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_number_input_interaction_view))
      .perform(editTextInputAction.appendText("55-"))
    onView(withId(R.id.number_input_error))
      .check(
        matches(
          withText(
            R.string.number_error_invalid_format
          )
        )
      )
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testNumericInput_withNegativeSignAt0MoreThan1_numberFormatErrorIsDisplayed() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_number_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "--55"
        )
      )
    onView(withId(R.id.number_input_error))
      .check(
        matches(
          withText(
            R.string.number_error_invalid_format
          )
        )
      )
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testNumericInput_withFloatingPointMoreThanOnce_numberFormatErrorIsDisplayed() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_number_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "5.5."
        )
      )
    onView(withId(R.id.number_input_error))
      .check(
        matches(
          withText(
            R.string.number_error_invalid_format
          )
        )
      )
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testNumericInput_withDecimalAtStart_numberStartingWithFloatingPointError() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_number_input_interaction_view))
      .perform(editTextInputAction.appendText(".5"))
    onView(withId(R.id.number_input_error))
      .check(
        matches(
          withText(
            R.string.number_error_starting_with_floating_point
          )
        )
      )
  }

  private fun scrollToSubmitButton() {
    onView(withId(R.id.submit_button)).perform(scrollTo())
    testCoroutineDispatchers.runCurrent()
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

    fun inject(inputInteractionViewTestActivityTest: InputInteractionViewTestActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerInputInteractionViewTestActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(inputInteractionViewTestActivityTest: InputInteractionViewTestActivityTest) {
      component.inject(inputInteractionViewTestActivityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
