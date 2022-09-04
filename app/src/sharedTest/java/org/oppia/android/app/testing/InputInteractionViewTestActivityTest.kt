package org.oppia.android.app.testing

import android.app.Application
import android.content.res.Configuration
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.CoreMatchers.allOf
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
import org.oppia.android.app.topic.PracticeTabModule
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
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
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

  // TODO(#4135): Move fraction input tests to a dedicated test suite.

  @Test
  fun testFractionInput_withNoInput_hasCorrectPendingAnswerType() {
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.fractionInteractionViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.fraction.denominator).isEqualTo(0)
      assertThat(pendingAnswer.answer.fraction.numerator).isEqualTo(0)
      assertThat(pendingAnswer.answer.fraction.wholeNumber).isEqualTo(0)
    }
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and will not be used by user
  fun testFractionInput_withNegativeNumber_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(editTextInputAction.appendText("-9"))
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.fractionInteractionViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.objectTypeCase).isEqualTo(
        InteractionObject.ObjectTypeCase.FRACTION
      )
      assertThat(pendingAnswer.answer.fraction.isNegative).isEqualTo(true)
      assertThat(pendingAnswer.answer.fraction.wholeNumber).isEqualTo(9)
    }
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and will not be used by user
  fun testFractionInput_withWholeNumber_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(editTextInputAction.appendText("9"))
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.fractionInteractionViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.objectTypeCase).isEqualTo(
        InteractionObject.ObjectTypeCase.FRACTION
      )
      assertThat(pendingAnswer.answer.fraction.isNegative).isEqualTo(false)
      assertThat(pendingAnswer.answer.fraction.wholeNumber).isEqualTo(9)
    }
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and will not be used by user
  fun testFractionInput_withFraction_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "9/10"
        )
      )
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.fractionInteractionViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.objectTypeCase).isEqualTo(
        InteractionObject.ObjectTypeCase.FRACTION
      )
      assertThat(pendingAnswer.answer.fraction.isNegative).isEqualTo(false)
      assertThat(pendingAnswer.answer.fraction.numerator).isEqualTo(9)
      assertThat(pendingAnswer.answer.fraction.denominator).isEqualTo(10)
    }
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and will not be used by user
  fun testFractionInput_withNegativeFraction_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "-9/10"
        )
      )
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.fractionInteractionViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.objectTypeCase).isEqualTo(
        InteractionObject.ObjectTypeCase.FRACTION
      )
      assertThat(pendingAnswer.answer.fraction.isNegative).isEqualTo(true)
      assertThat(pendingAnswer.answer.fraction.numerator).isEqualTo(9)
      assertThat(pendingAnswer.answer.fraction.denominator).isEqualTo(10)
    }
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and will not be used by user
  fun testFractionInput_withMixedNumber_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "5 9/10"
        )
      )
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.fractionInteractionViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.objectTypeCase).isEqualTo(
        InteractionObject.ObjectTypeCase.FRACTION
      )
      assertThat(pendingAnswer.answer.fraction.isNegative).isEqualTo(false)
      assertThat(pendingAnswer.answer.fraction.wholeNumber).isEqualTo(5)
      assertThat(pendingAnswer.answer.fraction.numerator).isEqualTo(9)
      assertThat(pendingAnswer.answer.fraction.denominator).isEqualTo(10)
    }
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and will not be used by user
  fun testFractionInput_withNegativeMixedNumber_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "-55 59/9"
        )
      )
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.fractionInteractionViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.objectTypeCase).isEqualTo(
        InteractionObject.ObjectTypeCase.FRACTION
      )
      assertThat(pendingAnswer.answer.fraction.isNegative).isEqualTo(true)
      assertThat(pendingAnswer.answer.fraction.wholeNumber).isEqualTo(55)
      assertThat(pendingAnswer.answer.fraction.numerator).isEqualTo(59)
      assertThat(pendingAnswer.answer.fraction.denominator).isEqualTo(9)
    }
  }

  @Test
  @Ignore("Landscape not properly supported") // TODO(#56): Reenable once landscape is supported.
  fun testFractionInput_withFraction_configChange_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "9/5"
        )
      )
    activityScenario.onActivity { activity ->
      activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    }
    onView(withId(R.id.test_fraction_input_interaction_view)).check(matches(isDisplayed()))
      .check(matches(withText("9/5")))
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and will not be used by user
  fun testFractionInput_withNegativeSignOtherThanAt0_numberFormatErrorIsDisplayed() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "55-"
        )
      )
    onView(withId(R.id.fraction_input_error))
      .check(
        matches(
          withText(
            R.string.fraction_error_invalid_format
          )
        )
      )
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testFractionInput_withNegativeSignAt0MoreThan1_numberFormatErrorIsDisplayed() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "--55"
        )
      )
    onView(withId(R.id.fraction_input_error))
      .check(
        matches(
          withText(
            R.string.fraction_error_invalid_format
          )
        )
      )
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testFractionInput_withDividerMoreThanOnce_numberFormatErrorIsDisplayed() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "5/5/"
        )
      )
    onView(withId(R.id.fraction_input_error))
      .check(
        matches(
          withText(
            R.string.fraction_error_invalid_format
          )
        )
      )
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testFractionInput_withDividerAtStart_numberFormatErrorIsDisplayed() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "/5"
        )
      )
    onView(withId(R.id.fraction_input_error))
      .check(
        matches(
          withText(
            R.string.fraction_error_invalid_format
          )
        )
      )
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testFractionInput_withPartialMixedNumber_numberFormatErrorIsNotDisplayed() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "5 5/"
        )
      )
    onView(withId(R.id.fraction_input_error)).check(matches(withText("")))
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testFractionInput_withPartialMixedNumberSubmit_numberFormatErrorIsDisplayed() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "5 5/"
        )
      )
    closeSoftKeyboard()
    scrollToSubmitButton()
    onView(withId(R.id.submit_button)).check(matches(isDisplayed())).perform(click())
    onView(withId(R.id.fraction_input_error))
      .check(
        matches(
          withText(
            R.string.fraction_error_invalid_format
          )
        )
      )
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testFractionInput_withMixedNumber_submit_noErrorIsDisplayed() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "3 1/2"
        )
      )
    closeSoftKeyboard()
    scrollToSubmitButton()
    onView(withId(R.id.submit_button)).check(matches(isDisplayed())).perform(click())
    onView(withId(R.id.fraction_input_error)).check(matches(withText("")))
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testFractionInput_withDivideByZero_errorIsNotDisplayed() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "1/0"
        )
      )
    onView(withId(R.id.fraction_input_error)).check(matches(withText("")))
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testFractionInput_withDivideByZero_submit_divideByZeroErrorIsDisplayed() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_fraction_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "1/0"
        )
      )
    closeSoftKeyboard()
    scrollToSubmitButton()
    onView(withId(R.id.submit_button)).check(matches(isDisplayed())).perform(click())
    onView(withId(R.id.fraction_input_error))
      .check(
        matches(
          withText(
            R.string.fraction_error_divide_by_zero
          )
        )
      )
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testFractionInput_withInvalidCharacter_invalidCharacterErrorIsDisplayed() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java).use {
      onView(withId(R.id.test_fraction_input_interaction_view))
        .perform(
          editTextInputAction.appendText(
            "."
          )
        )
      onView(withId(R.id.fraction_input_error))
        .check(
          matches(
            withText(
              R.string.fraction_error_invalid_chars
            )
          )
        )
    }
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testFractionInput_withLong_submit_numberTooLongErrorIsDisplayed() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java).use {
      onView(withId(R.id.test_fraction_input_interaction_view))
        .perform(
          editTextInputAction.appendText(
            "12345678"
          )
        )
      closeSoftKeyboard()
      scrollToSubmitButton()
      onView(withId(R.id.submit_button)).check(matches(isDisplayed())).perform(click())
      onView(withId(R.id.fraction_input_error))
        .check(
          matches(
            withText(
              R.string.fraction_error_larger_than_seven_digits
            )
          )
        )
    }
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

  @Test
  fun testTextInput_withNoInput_hasCorrectPendingAnswerType() {
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.textInputViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.normalizedString).isEmpty()
    }
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testTextInput_withChar_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_text_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "abc"
        )
      )
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.textInputViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.objectTypeCase).isEqualTo(
        InteractionObject.ObjectTypeCase.NORMALIZED_STRING
      )
      assertThat(pendingAnswer.answer.normalizedString).isEqualTo("abc")
    }
  }

  @Test
  @Ignore("Landscape not properly supported") // TODO(#56): Reenable once landscape is supported.
  fun testTextInput_withChar_configChange_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_text_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "abc"
        )
      )
    activityScenario.onActivity { activity ->
      activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    }
    onView(withId(R.id.test_text_input_interaction_view)).check(matches(isDisplayed()))
      .check(matches(withText("abc")))
  }

  @Test
  fun testRatioInput_withNoInput_hasCorrectPendingAnswerType() {
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.ratioExpressionInputInteractionViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.ratioExpression.ratioComponentCount).isEqualTo(0)
    }
  }

  @Test
  fun testRatioInput_withRatioOfNumber_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_ratio_input_interaction_view))
      .perform(
        setTextToRatioInputInteractionView(
          "1:2:3"
        )
      )
    activityScenario.onActivity { activity ->
      val pendingAnswer = activity.ratioExpressionInputInteractionViewModel.getPendingAnswer()
      assertThat(pendingAnswer.answer).isInstanceOf(InteractionObject::class.java)
      assertThat(pendingAnswer.answer.objectTypeCase).isEqualTo(
        InteractionObject.ObjectTypeCase.RATIO_EXPRESSION
      )
      assertThat(pendingAnswer.answer.ratioExpression.ratioComponentList)
        .isEqualTo(listOf(1, 2, 3))
    }
  }

  @Test
  @Ignore("Landscape not properly supported") // TODO(#56): Reenable once landscape is supported.
  fun testRatioInput_withRatio_configChange_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(
      InputInteractionViewTestActivity::class.java
    )
    onView(withId(R.id.test_ratio_input_interaction_view))
      .perform(
        editTextInputAction.appendText(
          "1:2"
        )
      )
    activityScenario.onActivity { activity ->
      activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    }
    onView(withId(R.id.test_ratio_input_interaction_view)).check(matches(isDisplayed()))
      .check(matches(withText("1:2")))
  }

  @Test
  fun testRatioInput_withTwoColonsTogether_colonsTogetherFormatErrorIsDisplayed() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.test_ratio_input_interaction_view))
        .perform(
          setTextToRatioInputInteractionView(
            "1::2"
          )
        )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ratio_input_error))
        .check(
          matches(
            withText(
              R.string.ratio_error_invalid_colons
            )
          )
        )
    }
  }

  @Test
  fun testRatioInput_withNegativeRatioOfNumber_numberFormatErrorIsDisplayed() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.test_ratio_input_interaction_view))
        .perform(
          setTextToRatioInputInteractionView(
            "-1:2:3:4"
          )
        )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ratio_input_error))
        .check(
          matches(
            withText(
              R.string.ratio_error_invalid_chars
            )
          )
        )
    }
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testRatioInput_withZeroRatio_submit_numberWithZerosErrorIsDisplayed() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.test_ratio_input_interaction_view))
        .perform(
          setTextToRatioInputInteractionView(
            "1:0:4"
          )
        )
      testCoroutineDispatchers.runCurrent()
      scrollToSubmitButton()
      onView(withId(R.id.submit_button)).check(matches(isDisplayed())).perform(click())
      onView(withId(R.id.ratio_input_error))
        .check(
          matches(
            withText(
              R.string.ratio_error_includes_zero
            )
          )
        )
    }
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testRatioInput_withInvalidRatio_submit_numberFormatErrorIsDisplayed() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.test_ratio_input_interaction_view))
        .perform(
          setTextToRatioInputInteractionView(
            "1: 1 2 :4"
          )
        )
      closeSoftKeyboard()
      testCoroutineDispatchers.runCurrent()
      scrollToSubmitButton()
      onView(withId(R.id.submit_button)).check(matches(isDisplayed())).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ratio_input_error))
        .check(
          matches(
            withText(
              R.string.ratio_error_invalid_format
            )
          )
        )
    }
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testRatioInput_withRatioHaving4Terms_submit_invalidSizeErrorIsDisplayed() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.test_ratio_input_interaction_view))
        .perform(
          setTextToRatioInputInteractionView(
            "1:2:3:4"
          )
        )
      closeSoftKeyboard()
      testCoroutineDispatchers.runCurrent()
      scrollToSubmitButton()
      onView(withId(R.id.submit_button)).check(matches(isDisplayed())).perform(click())
      onView(withId(R.id.ratio_input_error))
        .check(
          matches(
            withText(
              R.string.ratio_error_invalid_size
            )
          )
        )
    }
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testRatioInput_withRatioHaving2Terms_submit_invalidSizeErrorIsDisplayed() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.test_ratio_input_interaction_view))
        .perform(
          setTextToRatioInputInteractionView(
            "1:2"
          )
        )
      closeSoftKeyboard()
      testCoroutineDispatchers.runCurrent()
      scrollToSubmitButton()
      onView(withId(R.id.submit_button)).check(matches(isDisplayed())).perform(click())
      onView(withId(R.id.ratio_input_error))
        .check(
          matches(
            withText(
              R.string.ratio_error_invalid_size
            )
          )
        )
    }
  }

  @Test
  @DisableAccessibilityChecks // Disabled, as InputInteractionViewTestActivity is a test file and
  // will not be used by user
  fun testRatioInput_withRatioHaving3Terms_submit_noErrorIsDisplayed() {
    ActivityScenario.launch(InputInteractionViewTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.test_ratio_input_interaction_view))
        .perform(
          setTextToRatioInputInteractionView(
            "1:2:3"
          )
        )
      closeSoftKeyboard()
      scrollToSubmitButton()
      onView(withId(R.id.submit_button)).check(matches(isDisplayed())).perform(click())
      onView(withId(R.id.ratio_input_error)).check(matches(withText("")))
    }
  }

  private fun scrollToSubmitButton() {
    onView(withId(R.id.submit_button)).perform(scrollTo())
    testCoroutineDispatchers.runCurrent()
  }

  private fun setTextToRatioInputInteractionView(
    newText: String?
  ): ViewAction? {
    return object : ViewAction {
      override fun getConstraints(): Matcher<View> {
        return allOf(isDisplayed(), isAssignableFrom(RatioInputInteractionView::class.java))
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
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class, PracticeTabModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, NetworkConfigProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, SplitScreenInteractionModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, MetricLogSchedulerModule::class, TestingBuildFlavorModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

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
