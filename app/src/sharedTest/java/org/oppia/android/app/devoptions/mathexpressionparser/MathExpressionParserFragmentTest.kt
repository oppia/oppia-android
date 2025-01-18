package org.oppia.android.app.devoptions.mathexpressionparser

import android.app.Application
import android.content.Context
import android.text.Spannable
import android.text.style.ImageSpan
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.Matchers.containsString
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
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
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.activity.TestActivity
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
import org.oppia.android.testing.TestImageLoaderModule
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
import org.oppia.android.util.parser.image.ImageParsingModule
import org.oppia.android.util.parser.image.TestGlideImageLoader
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [MathExpressionParserFragment] and its presenter and view model. */
// FunctionName: test names are conventionally named with underscores.
// SameParameterValue: tests should have specific context included/excluded for readability.
@Suppress("FunctionName", "SameParameterValue")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = MathExpressionParserFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class MathExpressionParserFragmentTest {
  private val initializeDefaultLocaleRule by lazy { InitializeDefaultLocaleRule() }

  private val activityScenarioRule by lazy {
    ActivityScenarioRule<TestActivity>(
      TestActivity.createIntent(ApplicationProvider.getApplicationContext())
    )
  }

  // Note that the locale rule must be initialized first since the scenario rule can depend on the
  // locale being initialized.
  @get:Rule
  val chain: TestRule =
    RuleChain.outerRule(initializeDefaultLocaleRule).around(activityScenarioRule)

  @Inject lateinit var context: Context
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var editTextInputAction: EditTextInputAction
  @Inject lateinit var testGlideImageLoader: TestGlideImageLoader

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
  }

  @Test
  fun testFragment_initialState_expressionEditBoxIsEmpty() {
    initializeMathExpressionParserFragment()

    onView(withId(R.id.math_expression_input_edit_text)).check(matches(withText("")))
  }

  @Test
  fun testFragment_initialState_hasUninitializedParseResult() {
    initializeMathExpressionParserFragment()

    scrollToParseResult()

    onView(withId(R.id.math_expression_parse_result_text_view))
      .check(matches(withText("Parse result: Uninitialized")))
  }

  /* Tests specific to numeric expressions. */

  @Test
  fun testFragment_selectNumExps_typeExp_emptyExp_clickParse_uninitedParseResult() {
    initializeMathExpressionParserFragment()

    selectNumericExpressionsParseType()
    selectMathExpressionResultType()
    clickParseButton()

    // Without an expression entered, nothing should be parsed.
    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view))
      .check(matches(withText("Parse result: Uninitialized")))
  }

  @Test
  fun testFragment_selectNumExps_typeOp_emptyExp_clickParse_uninitedParseResult() {
    initializeMathExpressionParserFragment()

    selectNumericExpressionsParseType()
    selectComparableOperationResultType()
    clickParseButton()

    // Without an expression entered, nothing should be parsed.
    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view))
      .check(matches(withText("Parse result: Uninitialized")))
  }

  @Test
  fun testFragment_selectNumExps_typePoly_emptyExp_clickParse_uninitedParseResult() {
    initializeMathExpressionParserFragment()

    selectNumericExpressionsParseType()
    selectPolynomialResultType()
    clickParseButton()

    // Without an expression entered, nothing should be parsed.
    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view))
      .check(matches(withText("Parse result: Uninitialized")))
  }

  @Test
  fun testFragment_selectNumExps_typeLatex_emptyExp_clickParse_uninitedParseResult() {
    initializeMathExpressionParserFragment()

    selectNumericExpressionsParseType()
    selectLatexResultType()
    clickParseButton()

    // Without an expression entered, nothing should be parsed.
    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view))
      .check(matches(withText("Parse result: Uninitialized")))
  }

  @Test
  fun testFragment_selectNumExps_typeA11y_emptyExp_clickParse_uninitedParseResult() {
    initializeMathExpressionParserFragment()

    selectNumericExpressionsParseType()
    selectHumanReadableStringResultType()
    clickParseButton()

    // Without an expression entered, nothing should be parsed.
    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view))
      .check(matches(withText("Parse result: Uninitialized")))
  }

  @Test
  fun testFragment_selectNumExps_typeExp_validExp_clickParse_initedParseResult() {
    initializeMathExpressionParserFragment()
    selectNumericExpressionsParseType()
    selectMathExpressionResultType()

    typeExpression("3 × 2 − 3 + 4 ^ 3.14 × 8 ÷ 3 × 2 + −7")
    clickParseButton()

    // Perform a cursory check to ensure that at least something sort of resembling the correct
    // proto output is displayed.
    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("MathExpression"))))
      check(matches(withText(containsString("operator: ADD"))))
      check(matches(withText(containsString("3"))))
    }
  }

  @Test
  fun testFragment_selectNumExps_typeOp_validExp_clickParse_initedParseResult() {
    initializeMathExpressionParserFragment()
    selectNumericExpressionsParseType()
    selectComparableOperationResultType()

    typeExpression("3 × 2 − 3 + 4 ^ 3.14 × 8 ÷ 3 × 2 + −7")
    clickParseButton()

    // Perform a cursory check to ensure that at least something sort of resembling the correct
    // proto output is displayed.
    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("ComparableOperation"))))
      check(matches(withText(containsString("accumulation_type: SUMMATION"))))
      check(matches(withText(containsString("3"))))
    }
  }

  @Test
  fun testFragment_selectNumExps_typePoly_validExp_clickParse_initedParseResult() {
    initializeMathExpressionParserFragment()
    selectNumericExpressionsParseType()
    selectPolynomialResultType()

    typeExpression("3 × 2 − 3 + 4 ^ 3 × 8 ÷ 3 × 2 + −7")
    clickParseButton()

    // Perform a cursory check to ensure that at least something sort of resembling the correct
    // proto output is displayed.
    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("Polynomial"))))
      check(matches(withText(containsString("coefficient"))))
      // The expression is fully evaluated as a constant polynomial.
      check(matches(withText(containsString("whole_number: 337"))))
    }
  }

  @Test
  fun testFragment_selectNumExps_typeLatex_validExp_clickParse_initedParseResult() {
    initializeMathExpressionParserFragment()
    selectNumericExpressionsParseType()
    selectLatexResultType()

    typeExpression("3 × 2 − 3 + 4 ^ 3 + 8 ÷ 3 × 2 + −7")
    clickParseButton()

    // Perform a cursory check to ensure that at least part of the LaTeX output is correct.
    activityScenarioRule.scenario.onActivity { activity ->
      val resultTextView =
        activity.findViewById<TextView>(R.id.math_expression_parse_result_text_view)
      val drawableSpans = resultTextView.findSpansOfType<ImageSpan>()
      val loadedModels = testGlideImageLoader.getLoadedMathDrawables()

      // Verify that an image drawable was loaded and with the correct LaTeX.
      assertThat(drawableSpans).hasSize(1)
      assertThat(loadedModels.first().rawLatex).contains("3 \\times 2")
      assertThat(loadedModels.first().rawLatex).contains("8 \\div 3")
      assertThat(loadedModels.first().useInlineRendering).isFalse()
    }
  }

  @Test
  fun testFragment_selectNumExps_typeLatex_validExp_divAsFrac_clickParse_initedParseResult() {
    initializeMathExpressionParserFragment()
    selectNumericExpressionsParseType()
    selectLatexResultType()
    toggleTreatDivisionsAsFractionsSwitch()

    typeExpression("3 × 2 − 3 + 4 ^ 3 + 8 ÷ 3 × 2 + −7")
    clickParseButton()

    // Perform a cursory check to ensure that at least part of the LaTeX output is correct for
    // cases when divisions are treated as fractions.
    activityScenarioRule.scenario.onActivity { activity ->
      val resultTextView =
        activity.findViewById<TextView>(R.id.math_expression_parse_result_text_view)
      val drawableSpans = resultTextView.findSpansOfType<ImageSpan>()
      val loadedModels = testGlideImageLoader.getLoadedMathDrawables()

      // Verify that an image drawable was loaded and with the correct LaTeX.
      assertThat(drawableSpans).hasSize(1)
      assertThat(loadedModels.first().rawLatex).contains("3 \\times 2")
      assertThat(loadedModels.first().rawLatex).contains("\\frac{8}{3}")
      assertThat(loadedModels.first().useInlineRendering).isFalse()
    }
  }

  @Test
  fun testFragment_selectNumExps_typeA11y_validExp_clickParse_initedParseResult() {
    initializeMathExpressionParserFragment()
    selectNumericExpressionsParseType()
    selectHumanReadableStringResultType()

    typeExpression("3 × 2 − 3 + 4 ^ 3 + 8 ÷ 3 × 2 + −7")
    clickParseButton()

    // Perform a cursory check to ensure that at least part of the a11y output is correct.
    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("3 times 2"))))
      check(matches(withText(containsString("4 raised to the power of 3"))))
      check(matches(withText(containsString("8 divided by 3"))))
      check(matches(withText(containsString("negative 7"))))
    }
  }

  @Test
  fun testFragment_selectNumExps_typeA11y_validExp_divAsFrac_clickParse_initedParseResult() {
    initializeMathExpressionParserFragment()
    selectNumericExpressionsParseType()
    selectHumanReadableStringResultType()
    toggleTreatDivisionsAsFractionsSwitch()

    typeExpression("3 × 2 − 3 + 4 ^ 3 + 8 ÷ 3 × 2 + −7")
    clickParseButton()

    // Perform a cursory check to ensure that at least part of the a11y output is correct for
    // cases when divisions are treated as fractions.
    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("3 times 2"))))
      check(matches(withText(containsString("4 raised to the power of 3"))))
      check(matches(withText(containsString("8 over 3"))))
      check(matches(withText(containsString("negative 7"))))
    }
  }

  @Test
  fun testFragment_selectNumExps_typeExp_invalidExp_clickParse_errorParseResult() {
    initializeMathExpressionParserFragment()
    selectNumericExpressionsParseType()
    selectMathExpressionResultType()

    typeExpression("3/0")
    clickParseButton()

    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("MathParsingError"))))
      check(matches(withText(containsString("TermDividedByZeroError"))))
    }
  }

  @Test
  fun testFragment_selectNumExps_typeOp_invalidExp_clickParse_errorParseResult() {
    initializeMathExpressionParserFragment()
    selectNumericExpressionsParseType()
    selectComparableOperationResultType()

    typeExpression("3/0")
    clickParseButton()

    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("MathParsingError"))))
      check(matches(withText(containsString("TermDividedByZeroError"))))
    }
  }

  @Test
  fun testFragment_selectNumExps_typePoly_invalidExp_clickParse_errorParseResult() {
    initializeMathExpressionParserFragment()
    selectNumericExpressionsParseType()
    selectPolynomialResultType()

    typeExpression("3/0")
    clickParseButton()

    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("MathParsingError"))))
      check(matches(withText(containsString("TermDividedByZeroError"))))
    }
  }

  @Test
  fun testFragment_selectNumExps_typeLatex_invalidExp_clickParse_errorParseResult() {
    initializeMathExpressionParserFragment()
    selectNumericExpressionsParseType()
    selectLatexResultType()

    typeExpression("3/0")
    clickParseButton()

    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("MathParsingError"))))
      check(matches(withText(containsString("TermDividedByZeroError"))))
    }
  }

  @Test
  fun testFragment_selectNumExps_typeA11y_invalidExp_clickParse_errorParseResult() {
    initializeMathExpressionParserFragment()
    selectNumericExpressionsParseType()
    selectHumanReadableStringResultType()

    typeExpression("3/0")
    clickParseButton()

    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("MathParsingError"))))
      check(matches(withText(containsString("TermDividedByZeroError"))))
    }
  }

  /* Tests specific to algebraic expressions. */

  @Test
  fun testFragment_selectAlgExps_typeExp_emptyExp_clickParse_uninitedParseResult() {
    initializeMathExpressionParserFragment()

    selectAlgebraicExpressionsParseType()
    selectMathExpressionResultType()
    clickParseButton()

    // Without an expression entered, nothing should be parsed.
    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view))
      .check(matches(withText("Parse result: Uninitialized")))
  }

  @Test
  fun testFragment_selectAlgExps_typeOp_emptyExp_clickParse_uninitedParseResult() {
    initializeMathExpressionParserFragment()

    selectAlgebraicExpressionsParseType()
    selectComparableOperationResultType()
    clickParseButton()

    // Without an expression entered, nothing should be parsed.
    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view))
      .check(matches(withText("Parse result: Uninitialized")))
  }

  @Test
  fun testFragment_selectAlgExps_typePoly_emptyExp_clickParse_uninitedParseResult() {
    initializeMathExpressionParserFragment()

    selectAlgebraicExpressionsParseType()
    selectPolynomialResultType()
    clickParseButton()

    // Without an expression entered, nothing should be parsed.
    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view))
      .check(matches(withText("Parse result: Uninitialized")))
  }

  @Test
  fun testFragment_selectAlgExps_typeLatex_emptyExp_clickParse_uninitedParseResult() {
    initializeMathExpressionParserFragment()

    selectAlgebraicExpressionsParseType()
    selectLatexResultType()
    clickParseButton()

    // Without an expression entered, nothing should be parsed.
    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view))
      .check(matches(withText("Parse result: Uninitialized")))
  }

  @Test
  fun testFragment_selectAlgExps_typeA11y_emptyExp_clickParse_uninitedParseResult() {
    initializeMathExpressionParserFragment()

    selectAlgebraicExpressionsParseType()
    selectHumanReadableStringResultType()
    clickParseButton()

    // Without an expression entered, nothing should be parsed.
    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view))
      .check(matches(withText("Parse result: Uninitialized")))
  }

  @Test
  fun testFragment_selectAlgExps_typeExp_validExp_clickParse_initedParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicExpressionsParseType()
    selectMathExpressionResultType()
    specifyAllowedVariables("x", "y", "z")

    typeExpression("12x^2y^2 − (y × z^2 + yzx) − 731z")
    clickParseButton()

    // Perform a cursory check to ensure that at least something sort of resembling the correct
    // proto output is displayed.
    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("MathExpression"))))
      check(matches(withText(containsString("operator: ADD"))))
      check(matches(withText(containsString("z"))))
    }
  }

  @Test
  fun testFragment_selectAlgExps_typeOp_validExp_clickParse_initedParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicExpressionsParseType()
    selectComparableOperationResultType()
    specifyAllowedVariables("x", "y", "z")

    typeExpression("12x^2y^2 − (y × z^2 + yzx) − 731z")
    clickParseButton()

    // Perform a cursory check to ensure that at least something sort of resembling the correct
    // proto output is displayed.
    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("ComparableOperation"))))
      check(matches(withText(containsString("accumulation_type: SUMMATION"))))
      check(matches(withText(containsString("exponentiation"))))
      check(matches(withText(containsString("x"))))
    }
  }

  @Test
  fun testFragment_selectAlgExps_typePoly_validExp_clickParse_initedParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicExpressionsParseType()
    selectPolynomialResultType()
    specifyAllowedVariables("x", "y", "z")

    typeExpression("12x^2y^2 − (y × z^2 + yzx) − 731z")
    clickParseButton()

    // Perform a cursory check to ensure that at least something sort of resembling the correct
    // proto output is displayed.
    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("Polynomial"))))
      check(matches(withText(containsString("name: \"x\""))))
      check(matches(withText(containsString("power: 2"))))
      check(matches(withText(containsString("integer: -731"))))
    }
  }

  @Test
  fun testFragment_selectAlgExps_typeLatex_validExp_clickParse_initedParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicExpressionsParseType()
    selectLatexResultType()
    specifyAllowedVariables("x", "y", "z")

    typeExpression("12x^2y^2 − (y × z^2 + yzx) − 731 / z")
    clickParseButton()

    // Perform a cursory check to ensure that at least part of the LaTeX output is correct.
    activityScenarioRule.scenario.onActivity { activity ->
      val resultTextView =
        activity.findViewById<TextView>(R.id.math_expression_parse_result_text_view)
      val drawableSpans = resultTextView.findSpansOfType<ImageSpan>()
      val loadedModels = testGlideImageLoader.getLoadedMathDrawables()

      // Verify that an image drawable was loaded and with the correct LaTeX.
      assertThat(drawableSpans).hasSize(1)
      assertThat(loadedModels.first().rawLatex).contains("12x ^ {2}")
      assertThat(loadedModels.first().rawLatex).contains("731 \\div z")
      assertThat(loadedModels.first().useInlineRendering).isFalse()
    }
  }

  @Test
  fun testFragment_selectAlgExps_typeLatex_validExp_divAsFrac_clickParse_initedParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicExpressionsParseType()
    selectLatexResultType()
    specifyAllowedVariables("x", "y", "z")
    toggleTreatDivisionsAsFractionsSwitch()

    typeExpression("12x^2y^2 − (y × z^2 + yzx) − 731 / z")
    clickParseButton()

    // Perform a cursory check to ensure that at least part of the LaTeX output is correct for
    // cases when divisions are treated as fractions.
    activityScenarioRule.scenario.onActivity { activity ->
      val resultTextView =
        activity.findViewById<TextView>(R.id.math_expression_parse_result_text_view)
      val drawableSpans = resultTextView.findSpansOfType<ImageSpan>()
      val loadedModels = testGlideImageLoader.getLoadedMathDrawables()

      // Verify that an image drawable was loaded and with the correct LaTeX.
      assertThat(drawableSpans).hasSize(1)
      assertThat(loadedModels.first().rawLatex).contains("12x ^ {2}")
      assertThat(loadedModels.first().rawLatex).contains("\\frac{731}{z}")
      assertThat(loadedModels.first().useInlineRendering).isFalse()
    }
  }

  @Test
  fun testFragment_selectAlgExps_typeA11y_validExp_clickParse_initedParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicExpressionsParseType()
    selectHumanReadableStringResultType()
    specifyAllowedVariables("x", "y", "z")

    typeExpression("12x^2y^2 − (y × z^2 + yzx) − 731 / z")
    clickParseButton()

    // Perform a cursory check to ensure that at least part of the a11y output is correct.
    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("12 x raised to the power of 2"))))
      check(matches(withText(containsString("731 divided by zed"))))
    }
  }

  @Test
  fun testFragment_selectAlgExps_typeA11y_validExp_divAsFrac_clickParse_initedParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicExpressionsParseType()
    selectHumanReadableStringResultType()
    specifyAllowedVariables("x", "y", "z")
    toggleTreatDivisionsAsFractionsSwitch()

    typeExpression("12x^2y^2 − (y × z^2 + yzx) − 731 / z")
    clickParseButton()

    // Perform a cursory check to ensure that at least part of the a11y output is correct for
    // cases when divisions are treated as fractions.
    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("12 x raised to the power of 2"))))
      check(matches(withText(containsString("731 over zed"))))
    }
  }

  @Test
  fun testFragment_selectAlgExps_typeExp_invalidExp_clickParse_errorParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicExpressionsParseType()
    selectMathExpressionResultType()

    typeExpression("x/0")
    clickParseButton()

    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("MathParsingError"))))
      check(matches(withText(containsString("TermDividedByZeroError"))))
    }
  }

  @Test
  fun testFragment_selectAlgExps_typeOp_invalidExp_clickParse_errorParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicExpressionsParseType()
    selectComparableOperationResultType()

    typeExpression("x/0")
    clickParseButton()

    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("MathParsingError"))))
      check(matches(withText(containsString("TermDividedByZeroError"))))
    }
  }

  @Test
  fun testFragment_selectAlgExps_typePoly_invalidExp_clickParse_errorParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicExpressionsParseType()
    selectPolynomialResultType()

    typeExpression("x/0")
    clickParseButton()

    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("MathParsingError"))))
      check(matches(withText(containsString("TermDividedByZeroError"))))
    }
  }

  @Test
  fun testFragment_selectAlgExps_typeLatex_invalidExp_clickParse_errorParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicExpressionsParseType()
    selectLatexResultType()

    typeExpression("x/0")
    clickParseButton()

    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("MathParsingError"))))
      check(matches(withText(containsString("TermDividedByZeroError"))))
    }
  }

  @Test
  fun testFragment_selectAlgExps_typeA11y_invalidExp_clickParse_errorParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicExpressionsParseType()
    selectHumanReadableStringResultType()

    typeExpression("x/0")
    clickParseButton()

    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("MathParsingError"))))
      check(matches(withText(containsString("TermDividedByZeroError"))))
    }
  }

  @Test
  fun testFragment_selectAlgExps_typeExp_validExp_missingVars_clickParse_errorParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicExpressionsParseType()
    selectMathExpressionResultType()
    specifyAllowedVariables("x", "y", "z")

    typeExpression("x/s")
    clickParseButton()

    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("DisabledVariablesInUseError"))))
      check(matches(withText(containsString("[s]"))))
    }
  }

  @Test
  fun testFragment_selectAlgExps_typeOp_validExp_missingVars_clickParse_errorParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicExpressionsParseType()
    selectComparableOperationResultType()
    specifyAllowedVariables("x", "y", "z")

    typeExpression("x/s")
    clickParseButton()

    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("DisabledVariablesInUseError"))))
      check(matches(withText(containsString("[s]"))))
    }
  }

  @Test
  fun testFragment_selectAlgExps_typePoly_validExp_missingVars_clickParse_errorParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicExpressionsParseType()
    selectPolynomialResultType()
    specifyAllowedVariables("x", "y", "z")

    typeExpression("x/s")
    clickParseButton()

    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("DisabledVariablesInUseError"))))
      check(matches(withText(containsString("[s]"))))
    }
  }

  @Test
  fun testFragment_selectAlgExps_typeLatex_validExp_missingVars_clickParse_errorParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicExpressionsParseType()
    selectLatexResultType()
    specifyAllowedVariables("x", "y", "z")

    typeExpression("x/s")
    clickParseButton()

    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("DisabledVariablesInUseError"))))
      check(matches(withText(containsString("[s]"))))
    }
  }

  @Test
  fun testFragment_selectAlgExps_typeA11y_validExp_missingVars_clickParse_errorParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicExpressionsParseType()
    selectHumanReadableStringResultType()
    specifyAllowedVariables("x", "y", "z")

    typeExpression("x/s")
    clickParseButton()

    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("DisabledVariablesInUseError"))))
      check(matches(withText(containsString("[s]"))))
    }
  }

  /* Tests specific to algebraic/math equations. */

  @Test
  fun testFragment_selectAlgEqs_typeExp_emptyExp_clickParse_uninitedParseResult() {
    initializeMathExpressionParserFragment()

    selectAlgebraicEquationsParseType()
    selectMathExpressionResultType()
    clickParseButton()

    // Without an expression entered, nothing should be parsed.
    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view))
      .check(matches(withText("Parse result: Uninitialized")))
  }

  @Test
  fun testFragment_selectAlgEqs_typeOp_emptyExp_clickParse_uninitedParseResult() {
    initializeMathExpressionParserFragment()

    selectAlgebraicEquationsParseType()
    selectComparableOperationResultType()
    clickParseButton()

    // Without an expression entered, nothing should be parsed.
    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view))
      .check(matches(withText("Parse result: Uninitialized")))
  }

  @Test
  fun testFragment_selectAlgEqs_typePoly_emptyExp_clickParse_uninitedParseResult() {
    initializeMathExpressionParserFragment()

    selectAlgebraicEquationsParseType()
    selectPolynomialResultType()
    clickParseButton()

    // Without an expression entered, nothing should be parsed.
    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view))
      .check(matches(withText("Parse result: Uninitialized")))
  }

  @Test
  fun testFragment_selectAlgEqs_typeLatex_emptyExp_clickParse_uninitedParseResult() {
    initializeMathExpressionParserFragment()

    selectAlgebraicEquationsParseType()
    selectLatexResultType()
    clickParseButton()

    // Without an expression entered, nothing should be parsed.
    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view))
      .check(matches(withText("Parse result: Uninitialized")))
  }

  @Test
  fun testFragment_selectAlgEqs_typeA11y_emptyExp_clickParse_uninitedParseResult() {
    initializeMathExpressionParserFragment()

    selectAlgebraicEquationsParseType()
    selectHumanReadableStringResultType()
    clickParseButton()

    // Without an expression entered, nothing should be parsed.
    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view))
      .check(matches(withText("Parse result: Uninitialized")))
  }

  @Test
  fun testFragment_selectAlgEqs_typeExp_validExp_clickParse_initedParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicEquationsParseType()
    selectMathExpressionResultType()
    specifyAllowedVariables("x", "y", "z")

    typeExpression("1/2y = 12x^2y^2 − (y × z^2 + yzx) − 731z")
    clickParseButton()

    // Perform a cursory check to ensure that at least something sort of resembling the correct
    // proto output is displayed.
    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("left_side"))))
      check(matches(withText(containsString("right_side"))))
      check(matches(withText(containsString("MathEquation"))))
      check(matches(withText(containsString("operator: ADD"))))
      check(matches(withText(containsString("z"))))
    }
  }

  @Test
  fun testFragment_selectAlgEqs_typeOp_validExp_clickParse_initedParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicEquationsParseType()
    selectComparableOperationResultType()
    specifyAllowedVariables("x", "y", "z")

    typeExpression("1/2y = 12x^2y^2 − (y × z^2 + yzx) − 731z")
    clickParseButton()

    // Perform a cursory check to ensure that at least something sort of resembling the correct
    // proto output is displayed.
    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("Left side"))))
      check(matches(withText(containsString("Right side"))))
      check(matches(withText(containsString("ComparableOperation"))))
      check(matches(withText(containsString("accumulation_type: SUMMATION"))))
      check(matches(withText(containsString("exponentiation"))))
      check(matches(withText(containsString("x"))))
    }
  }

  @Test
  fun testFragment_selectAlgEqs_typePoly_validExp_clickParse_initedParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicEquationsParseType()
    selectPolynomialResultType()
    specifyAllowedVariables("x", "y", "z")

    typeExpression("1/2y = 12x^2y^2 − (y × z^2 + yzx) − 731z")
    clickParseButton()

    // Perform a cursory check to ensure that at least something sort of resembling the correct
    // proto output is displayed.
    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("Left side"))))
      check(matches(withText(containsString("Right side"))))
      check(matches(withText(containsString("Polynomial"))))
      check(matches(withText(containsString("name: \"x\""))))
      check(matches(withText(containsString("power: 2"))))
      check(matches(withText(containsString("integer: -731"))))
    }
  }

  @Test
  fun testFragment_selectAlgEqs_typeLatex_validExp_clickParse_initedParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicEquationsParseType()
    selectLatexResultType()
    specifyAllowedVariables("x", "y", "z")

    typeExpression("1/2y = 12x^2y^2 − (y × z^2 + yzx) − 731 / z")
    clickParseButton()

    // Perform a cursory check to ensure that at least part of the LaTeX output is correct.
    activityScenarioRule.scenario.onActivity { activity ->
      val resultTextView =
        activity.findViewById<TextView>(R.id.math_expression_parse_result_text_view)
      val drawableSpans = resultTextView.findSpansOfType<ImageSpan>()
      val loadedModels = testGlideImageLoader.getLoadedMathDrawables()

      // Verify that an image drawable was loaded and with the correct LaTeX.
      assertThat(drawableSpans).hasSize(1)
      assertThat(loadedModels.first().rawLatex).contains("1 \\div 2y =")
      assertThat(loadedModels.first().rawLatex).contains("12x ^ {2}")
      assertThat(loadedModels.first().rawLatex).contains("731 \\div z")
      assertThat(loadedModels.first().useInlineRendering).isFalse()
    }
  }

  @Test
  fun testFragment_selectAlgEqs_typeLatex_validExp_divAsFrac_clickParse_initedParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicEquationsParseType()
    selectLatexResultType()
    specifyAllowedVariables("x", "y", "z")
    toggleTreatDivisionsAsFractionsSwitch()

    typeExpression("1/2y = 12x^2y^2 − (y × z^2 + yzx) − 731 / z")
    clickParseButton()

    // Perform a cursory check to ensure that at least part of the LaTeX output is correct for
    // cases when divisions are treated as fractions.
    activityScenarioRule.scenario.onActivity { activity ->
      val resultTextView =
        activity.findViewById<TextView>(R.id.math_expression_parse_result_text_view)
      val drawableSpans = resultTextView.findSpansOfType<ImageSpan>()
      val loadedModels = testGlideImageLoader.getLoadedMathDrawables()

      // Verify that an image drawable was loaded and with the correct LaTeX.
      assertThat(drawableSpans).hasSize(1)
      assertThat(loadedModels.first().rawLatex).contains("\\frac{1}{2}y =")
      assertThat(loadedModels.first().rawLatex).contains("12x ^ {2}")
      assertThat(loadedModels.first().rawLatex).contains("\\frac{731}{z}")
      assertThat(loadedModels.first().useInlineRendering).isFalse()
    }
  }

  @Test
  fun testFragment_selectAlgEqs_typeA11y_validExp_clickParse_initedParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicEquationsParseType()
    selectHumanReadableStringResultType()
    specifyAllowedVariables("x", "y", "z")

    typeExpression("1/2y = 12x^2y^2 − (y × z^2 + yzx) − 731 / z")
    clickParseButton()

    // Perform a cursory check to ensure that at least part of the a11y output is correct.
    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("1 divided by 2 times y equals"))))
      check(matches(withText(containsString("12 x raised to the power of 2"))))
      check(matches(withText(containsString("731 divided by zed"))))
    }
  }

  @Test
  fun testFragment_selectAlgEqs_typeA11y_validExp_divAsFrac_clickParse_initedParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicEquationsParseType()
    selectHumanReadableStringResultType()
    specifyAllowedVariables("x", "y", "z")
    toggleTreatDivisionsAsFractionsSwitch()

    typeExpression("1/2y = 12x^2y^2 − (y × z^2 + yzx) − 731 / z")
    clickParseButton()

    // Perform a cursory check to ensure that at least part of the a11y output is correct for
    // cases when divisions are treated as fractions.
    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("one half times y equals"))))
      check(matches(withText(containsString("12 x raised to the power of 2"))))
      check(matches(withText(containsString("731 over zed"))))
    }
  }

  @Test
  fun testFragment_selectAlgEqs_typeExp_invalidExp_clickParse_errorParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicEquationsParseType()
    selectMathExpressionResultType()

    typeExpression("y=x/0")
    clickParseButton()

    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("MathParsingError"))))
      check(matches(withText(containsString("TermDividedByZeroError"))))
    }
  }

  @Test
  fun testFragment_selectAlgEqs_typeOp_invalidExp_clickParse_errorParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicEquationsParseType()
    selectComparableOperationResultType()

    typeExpression("y=x/0")
    clickParseButton()

    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("MathParsingError"))))
      check(matches(withText(containsString("TermDividedByZeroError"))))
    }
  }

  @Test
  fun testFragment_selectAlgEqs_typePoly_invalidExp_clickParse_errorParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicEquationsParseType()
    selectPolynomialResultType()

    typeExpression("y=x/0")
    clickParseButton()

    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("MathParsingError"))))
      check(matches(withText(containsString("TermDividedByZeroError"))))
    }
  }

  @Test
  fun testFragment_selectAlgEqs_typeLatex_invalidExp_clickParse_errorParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicEquationsParseType()
    selectLatexResultType()

    typeExpression("y=x/0")
    clickParseButton()

    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("MathParsingError"))))
      check(matches(withText(containsString("TermDividedByZeroError"))))
    }
  }

  @Test
  fun testFragment_selectAlgEqs_typeA11y_invalidExp_clickParse_errorParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicEquationsParseType()
    selectHumanReadableStringResultType()

    typeExpression("y=x/0")
    clickParseButton()

    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("MathParsingError"))))
      check(matches(withText(containsString("TermDividedByZeroError"))))
    }
  }

  @Test
  fun testFragment_selectAlgEqs_typeExp_validExp_missingVars_clickParse_errorParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicEquationsParseType()
    selectMathExpressionResultType()
    specifyAllowedVariables("x", "y", "z")

    typeExpression("y=x/s")
    clickParseButton()

    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("DisabledVariablesInUseError"))))
      check(matches(withText(containsString("[s]"))))
    }
  }

  @Test
  fun testFragment_selectAlgEqs_typeOp_validExp_missingVars_clickParse_errorParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicEquationsParseType()
    selectComparableOperationResultType()
    specifyAllowedVariables("x", "y", "z")

    typeExpression("y=x/s")
    clickParseButton()

    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("DisabledVariablesInUseError"))))
      check(matches(withText(containsString("[s]"))))
    }
  }

  @Test
  fun testFragment_selectAlgEqs_typePoly_validExp_missingVars_clickParse_errorParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicEquationsParseType()
    selectPolynomialResultType()
    specifyAllowedVariables("x", "y", "z")

    typeExpression("y=x/s")
    clickParseButton()

    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("DisabledVariablesInUseError"))))
      check(matches(withText(containsString("[s]"))))
    }
  }

  @Test
  fun testFragment_selectAlgEqs_typeLatex_validExp_missingVars_clickParse_errorParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicEquationsParseType()
    selectLatexResultType()
    specifyAllowedVariables("x", "y", "z")

    typeExpression("y=x/s")
    clickParseButton()

    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("DisabledVariablesInUseError"))))
      check(matches(withText(containsString("[s]"))))
    }
  }

  @Test
  fun testFragment_selectAlgEqs_typeA11y_validExp_missingVars_clickParse_errorParseResult() {
    initializeMathExpressionParserFragment()
    selectAlgebraicEquationsParseType()
    selectHumanReadableStringResultType()
    specifyAllowedVariables("x", "y", "z")

    typeExpression("y=x/s")
    clickParseButton()

    scrollToParseResult()
    onView(withId(R.id.math_expression_parse_result_text_view)).apply {
      check(matches(withText(containsString("DisabledVariablesInUseError"))))
      check(matches(withText(containsString("[s]"))))
    }
  }

  private fun typeExpression(text: String) =
    typeIntoView(R.id.math_expression_input_edit_text, text)

  private fun clickParseButton() = clickOnView(R.id.parse_math_expression_button)

  private fun selectNumericExpressionsParseType() {
    // First, select other bullet items before finally selecting numeric expressions (to ensure that
    // the default bullet item doesn't change the result of the test; this should result in the
    // parse type actually changing).
    clickOnAlgebraicExpressionsRadioButton()
    clickOnAlgebraicEquationsRadioButton()
    clickOnNumericExpressionsRadioButton()
  }

  private fun selectAlgebraicExpressionsParseType() {
    // See the comment in selectNumericExpressionsParseType for details on this approach.
    clickOnNumericExpressionsRadioButton()
    clickOnAlgebraicEquationsRadioButton()
    clickOnAlgebraicExpressionsRadioButton()
  }

  private fun selectAlgebraicEquationsParseType() {
    // See the comment in selectNumericExpressionsParseType for details on this approach.
    clickOnNumericExpressionsRadioButton()
    clickOnAlgebraicExpressionsRadioButton()
    clickOnAlgebraicEquationsRadioButton()
  }

  private fun selectMathExpressionResultType() {
    // See the comment in selectNumericExpressionsParseType for details on this approach.
    clickOnComparableOperationRadioButton()
    clickOnPolynomialRadioButton()
    clickOnLatexRadioButton()
    clickOnHumanReadableStringRadioButton()
    clickOnMathExpressionRadioButton()
  }

  private fun selectComparableOperationResultType() {
    // See the comment in selectNumericExpressionsParseType for details on this approach.
    clickOnMathExpressionRadioButton()
    clickOnPolynomialRadioButton()
    clickOnLatexRadioButton()
    clickOnHumanReadableStringRadioButton()
    clickOnComparableOperationRadioButton()
  }

  private fun selectPolynomialResultType() {
    // See the comment in selectNumericExpressionsParseType for details on this approach.
    clickOnMathExpressionRadioButton()
    clickOnComparableOperationRadioButton()
    clickOnLatexRadioButton()
    clickOnHumanReadableStringRadioButton()
    clickOnPolynomialRadioButton()
  }

  private fun selectLatexResultType() {
    // See the comment in selectNumericExpressionsParseType for details on this approach.
    clickOnMathExpressionRadioButton()
    clickOnComparableOperationRadioButton()
    clickOnPolynomialRadioButton()
    clickOnHumanReadableStringRadioButton()
    clickOnLatexRadioButton()
  }

  private fun selectHumanReadableStringResultType() {
    // See the comment in selectNumericExpressionsParseType for details on this approach.
    clickOnMathExpressionRadioButton()
    clickOnComparableOperationRadioButton()
    clickOnPolynomialRadioButton()
    clickOnLatexRadioButton()
    clickOnHumanReadableStringRadioButton()
  }

  private fun specifyAllowedVariables(vararg allowedVariables: String) {
    typeIntoView(R.id.allowed_variables_edit_text, allowedVariables.joinToString(separator = ","))
  }

  private fun toggleTreatDivisionsAsFractionsSwitch() =
    clickOnView(R.id.math_expression_treat_divisions_as_fractions_switch)

  private fun scrollToParseResult() = scrollToView(R.id.math_expression_parse_result_text_view)

  private fun clickOnNumericExpressionsRadioButton() =
    clickOnView(R.id.math_expression_parse_type_numeric_expression_radio_button)

  private fun clickOnAlgebraicExpressionsRadioButton() =
    clickOnView(R.id.math_expression_parse_type_algebraic_expression_radio_button)

  private fun clickOnAlgebraicEquationsRadioButton() =
    clickOnView(R.id.math_expression_parse_type_algebraic_equation_radio_button)

  private fun clickOnMathExpressionRadioButton() =
    clickOnView(R.id.math_expression_result_type_math_expression_radio_button)

  private fun clickOnComparableOperationRadioButton() =
    clickOnView(R.id.math_expression_result_type_comparable_operation_radio_button)

  private fun clickOnPolynomialRadioButton() =
    clickOnView(R.id.math_expression_result_type_polynomial_radio_button)

  private fun clickOnLatexRadioButton() =
    clickOnView(R.id.math_expression_result_type_latex_radio_button)

  private fun clickOnHumanReadableStringRadioButton() =
    clickOnView(R.id.math_expression_result_type_human_readable_string_radio_button)

  private fun scrollToView(@IdRes viewId: Int) {
    onView(withId(viewId)).perform(scrollTo())
    testCoroutineDispatchers.runCurrent()
  }

  private fun typeIntoView(@IdRes viewId: Int, text: String) {
    // First, ensure the view is visible before trying to input text.
    scrollToView(viewId)
    onView(withId(viewId)).perform(editTextInputAction.replaceText(text))
    testCoroutineDispatchers.runCurrent()
  }

  private fun clickOnView(@IdRes viewId: Int) {
    // First, ensure the view is visible before trying to click on it.
    scrollToView(viewId)
    onView(withId(viewId)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun initializeMathExpressionParserFragment() {
    activityScenarioRule.scenario.onActivity { it.addMathExpressionParserFragment() }
    testCoroutineDispatchers.runCurrent()
  }

  private fun TestActivity.addMathExpressionParserFragment() {
    supportFragmentManager.beginTransaction().apply {
      add(R.id.test_fragment_placeholder, MathExpressionParserFragment.createNewInstance())
    }.commitNow()
  }

  private inline fun <reified T> TextView.findSpansOfType(): List<T> {
    return (text as? Spannable)?.getSpans(0, text.length, T::class.java)?.toList() ?: listOf()
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

    fun inject(mathExpressionParserFragmentTest: MathExpressionParserFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerMathExpressionParserFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(mathExpressionParserFragmentTest: MathExpressionParserFragmentTest) {
      component.inject(mathExpressionParserFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
