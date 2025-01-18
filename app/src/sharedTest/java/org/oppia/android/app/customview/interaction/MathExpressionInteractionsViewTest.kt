package org.oppia.android.app.customview.interaction

import android.app.Application
import android.graphics.Typeface
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.extensions.proto.LiteProtoTruth.assertThat
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
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.model.AnswerErrorCategory.REAL_TIME
import org.oppia.android.app.model.AnswerErrorCategory.SUBMIT_TIME
import org.oppia.android.app.model.CustomSchemaValue
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.MathExpressionInteractionsViewTestActivityParams
import org.oppia.android.app.model.MathExpressionInteractionsViewTestActivityParams.MathInteractionType
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.SchemaObject
import org.oppia.android.app.model.SchemaObjectList
import org.oppia.android.app.model.SubtitledUnicode
import org.oppia.android.app.model.Translation
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.MathExpressionInteractionsViewTestActivity
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
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.espresso.EditTextInputAction
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Iteration
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Parameter
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.SelectRunnerPlatform
import org.oppia.android.testing.junit.ParameterizedAutoAndroidTestRunner
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

/**
 * Tests for [MathExpressionInteractionsView] & its view model.
 *
 * This suite is principally responsible for ensuring that math expressions and equations can be
 * correctly inputted in a variety of circumstances, including accessibility use cases. Note that
 * this suite is not aiming to comprehensively test each of these scenarios but, rather, demonstrate
 * high-level integration (the underlying components used in the tested view & model are much more
 * thoroughly tested for correctness).
 */
// FunctionName: test names are conventionally named with underscores.
// SameParameterValue: tests should have specific context included/excluded for readability.
@Suppress("FunctionName", "SameParameterValue")
@RunWith(OppiaParameterizedTestRunner::class)
@SelectRunnerPlatform(ParameterizedAutoAndroidTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = MathExpressionInteractionsViewTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class MathExpressionInteractionsViewTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject lateinit var editTextInputAction: EditTextInputAction

  @Parameter lateinit var type: String
  @Parameter lateinit var lang: String
  @Parameter lateinit var text: String
  @Parameter lateinit var expHintText: String
  @Parameter lateinit var expLatex: String
  @Parameter lateinit var expA11y: String
  @Parameter lateinit var expErr: String

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
  fun testView_withNoInput_hasCorrectInitialPendingAnswer() {
    launchForNumericExpressions().use { scenario ->
      testCoroutineDispatchers.runCurrent()

      scenario.onActivity { activity ->
        val pendingAnswer = activity.mathExpressionViewModel.getPendingAnswer()

        assertThat(pendingAnswer.answer).isEqualToDefaultInstance()
      }
    }
  }

  @Test
  @Iteration(
    "numeric_exp",
    "type=NUMERIC_EXPRESSION",
    "expHintText=Type an expression here, using only numbers."
  )
  @Iteration("alg_exp", "type=ALGEBRAIC_EXPRESSION", "expHintText=Type an expression here.")
  @Iteration("math_equation", "type=MATH_EQUATION", "expHintText=Type an equation here.")
  fun testView_allInteractions_withoutPlaceholder_hasCorrectDefaultHintText() {
    launch(interactionType = MathInteractionType.valueOf(type)).use {
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.test_math_expression_input_interaction_view))
        .check(matches(withHint(expHintText)))
    }
  }

  @Test
  fun testView_numericExpression_withUnicodePlaceholder_hasCorrectCustomHintText() {
    val interaction = createInteractionWithPlaceholder("test placeholder")
    launchForNumericExpressions(interaction).use {
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.test_math_expression_input_interaction_view))
        .check(matches(withHint("test placeholder")))
    }
  }

  @Test
  @Iteration("alg_exp", "type=ALGEBRAIC_EXPRESSION", "expHintText=Type an expression here.")
  @Iteration("math_equation", "type=MATH_EQUATION", "expHintText=Type an equation here.")
  fun testView_algebraicInteractions_withUnicodePlaceholder_hasDefaultHintText() {
    val interaction = createInteractionWithPlaceholder("test placeholder")
    launch(interactionType = MathInteractionType.valueOf(type), interaction).use {
      testCoroutineDispatchers.runCurrent()

      // This interaction doesn't support overriding the default hint placeholder.
      onView(withId(R.id.test_math_expression_input_interaction_view))
        .check(matches(withHint(expHintText)))
    }
  }

  @Test
  fun testView_numericExpression_withNestedUnicodePlaceholder_hasCorrectCustomHintText() {
    val interaction = createInteractionWithNestedPlaceholder("test placeholder")
    launchForNumericExpressions(interaction).use {
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.test_math_expression_input_interaction_view))
        .check(matches(withHint("test placeholder")))
    }
  }

  @Test
  @Iteration("alg_exp", "type=ALGEBRAIC_EXPRESSION", "expHintText=Type an expression here.")
  @Iteration("math_equation", "type=MATH_EQUATION", "expHintText=Type an equation here.")
  fun testView_algebraicInteractions_withNestedUnicodePlaceholder_hasDefaultHintText() {
    val interaction = createInteractionWithNestedPlaceholder("test placeholder")
    launch(interactionType = MathInteractionType.valueOf(type), interaction).use {
      testCoroutineDispatchers.runCurrent()

      // This interaction doesn't support overriding the default hint placeholder.
      onView(withId(R.id.test_math_expression_input_interaction_view))
        .check(matches(withHint(expHintText)))
    }
  }

  @Test
  fun testView_gainFocus_resetsHint() {
    val interaction = createInteractionWithPlaceholder("test placeholder")
    launchForNumericExpressions(interaction).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      scenario.onActivity { activity -> activity.getInteractionView().requestFocus() }
      testCoroutineDispatchers.runCurrent()

      // There should be no hint since the text view has been focused.
      onView(withId(R.id.test_math_expression_input_interaction_view)).check(matches(withHint("")))
    }
  }

  @Test
  fun testNumericExpression_submitWithBlankInput_emptyInputErrorIsDisplayed() {
    val interaction = createInteractionWithPlaceholder("test placeholder")
    launchForNumericExpressions(interaction).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity -> activity.getInteractionView().requestFocus() }
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.submit_button)).perform(click())
      onView(withId(R.id.math_expression_input_error))
        .check(
          matches(
            withText(
              R.string.numeric_expression_error_empty_input
            )
          )
        )
    }
  }

  @Test
  fun testAlgebraicExpression_submitWithBlankInput_emptyInputErrorIsDisplayed() {
    val interaction = createInteractionWithPlaceholder("test placeholder")
    launchForAlgebraicExpressions(interaction).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity -> activity.getInteractionView().requestFocus() }
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.submit_button)).perform(click())
      onView(withId(R.id.math_expression_input_error))
        .check(
          matches(
            withText(
              R.string.algebraic_expression_error_empty_input
            )
          )
        )
    }
  }

  @Test
  fun testMathEquation_submitWithBlankInput_emptyInputErrorIsDisplayed() {
    val interaction = createInteractionWithPlaceholder("test placeholder")
    launchForMathEquations(interaction).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity -> activity.getInteractionView().requestFocus() }
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.submit_button)).perform(click())
      onView(withId(R.id.math_expression_input_error))
        .check(
          matches(
            withText(
              R.string.math_equation_error_empty_input
            )
          )
        )
    }
  }

  @Test
  fun testView_gainFocus_resetsTypeFace() {
    launchForNumericExpressions().use { scenario ->
      testCoroutineDispatchers.runCurrent()

      scenario.onActivity { activity -> activity.getInteractionView().requestFocus() }
      testCoroutineDispatchers.runCurrent()

      scenario.onActivity { activity ->
        val view = activity.getInteractionView()
        assertThat(view.typeface).isEqualTo(Typeface.DEFAULT)
        assertThat(view.typeface.style).isNotEqualTo(Typeface.ITALIC)
      }
    }
  }

  @Test
  fun testView_gainThenLoseFocus_setsHint() {
    val interaction = createInteractionWithPlaceholder("test placeholder")
    launchForNumericExpressions(interaction).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      // Request, then clear focus.
      scenario.onActivity { activity -> activity.getInteractionView().requestFocus() }
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity -> activity.getInteractionView().clearFocus() }
      testCoroutineDispatchers.runCurrent()

      // There should be no hint since the text view has been focused.
      // The placeholder should be restored.
      onView(withId(R.id.test_math_expression_input_interaction_view))
        .check(matches(withHint("test placeholder")))
    }
  }

  @Test
  fun testView_gainThenLoseFocus_emptyText_setsTypeFaceToItalic() {
    launchForNumericExpressions().use { scenario ->
      testCoroutineDispatchers.runCurrent()

      // Request, then clear focus.
      scenario.onActivity { activity -> activity.getInteractionView().requestFocus() }
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity -> activity.getInteractionView().clearFocus() }
      testCoroutineDispatchers.runCurrent()

      scenario.onActivity { activity ->
        val view = activity.getInteractionView()
        assertThat(view.typeface.style).isEqualTo(Typeface.ITALIC)
      }
    }
  }

  @Test
  fun testView_gainThenLoseFocus_nonEmptyText_doesNotSetTypeFaceToItalic() {
    launchForNumericExpressions().use { scenario ->
      testCoroutineDispatchers.runCurrent()

      // Request, then clear focus after inputting text.
      onView(withId(R.id.test_math_expression_input_interaction_view))
        .perform(editTextInputAction.appendText("12+7"))
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity -> activity.getInteractionView().requestFocus() }
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity -> activity.getInteractionView().clearFocus() }
      testCoroutineDispatchers.runCurrent()

      scenario.onActivity { activity ->
        val view = activity.getInteractionView()
        assertThat(view.typeface.style).isNotEqualTo(Typeface.ITALIC)
      }
    }
  }

  @Test
  fun testView_pendingAnswer_usesProvidedWrittenTranslationContext() {
    val writtenTranslationContext = WrittenTranslationContext.newBuilder().apply {
      putTranslations(
        "key",
        Translation.newBuilder().apply {
          html = "value"
        }.build()
      )
      language = OppiaLanguage.ENGLISH
    }.build()
    launchForNumericExpressions(translationContext = writtenTranslationContext).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      // Ensure there's a pending answer present.
      typeExpressionInput("12+7")

      scenario.onActivity { activity ->
        val pendingAnswer = activity.mathExpressionViewModel.getPendingAnswer()
        assertThat(pendingAnswer).isNotEqualToDefaultInstance()
        assertThat(pendingAnswer.writtenTranslationContext).isEqualTo(writtenTranslationContext)
      }
    }
  }

  @Test
  @Iteration("numeric_-1", "type=NUMERIC_EXPRESSION", "text=  -  1           ")
  @Iteration("numeric_1+2", "type=NUMERIC_EXPRESSION", "text=1 + 2")
  @Iteration("numeric_1-2", "type=NUMERIC_EXPRESSION", "text=1 − 2")
  @Iteration("numeric_1*2", "type=NUMERIC_EXPRESSION", "text=1 × 2")
  @Iteration("numeric_1/2", "type=NUMERIC_EXPRESSION", "text=1 ÷ 2")
  @Iteration("numeric_long_exp", "type=NUMERIC_EXPRESSION", "text=3*2-3+4^3*8/3*2+7")
  @Iteration("numeric_invalid_exp_extra_error", "type=NUMERIC_EXPRESSION", "text=3/0")
  @Iteration("numeric_invalid_exp_syntax_error", "type=NUMERIC_EXPRESSION", "text=3!")
  @Iteration("algebraic_-1", "type=ALGEBRAIC_EXPRESSION", "text=  -  x           ")
  @Iteration("algebraic_1+2", "type=ALGEBRAIC_EXPRESSION", "text=x + 2")
  @Iteration("algebraic_1-2", "type=ALGEBRAIC_EXPRESSION", "text=x − 2")
  @Iteration("algebraic_1*2", "type=ALGEBRAIC_EXPRESSION", "text=x × 2")
  @Iteration("algebraic_1/2", "type=ALGEBRAIC_EXPRESSION", "text=x c 2")
  @Iteration("algebraic_long_exp", "type=ALGEBRAIC_EXPRESSION", "text=12x^2y^2-(yz^2+yzx)-731z")
  @Iteration("algebraic_invalid_exp_extra_error", "type=ALGEBRAIC_EXPRESSION", "text=2^x")
  @Iteration("algebraic_invalid_exp_syntax_error", "type=ALGEBRAIC_EXPRESSION", "text=2**x")
  @Iteration("math_eq_-1", "type=MATH_EQUATION", "text=     y=  -  x           ")
  @Iteration("math_eq_1+2", "type=MATH_EQUATION", "text=y = x + 2")
  @Iteration("math_eq_1-2", "type=MATH_EQUATION", "text=y = x − 2")
  @Iteration("math_eq_1*2", "type=MATH_EQUATION", "text=y = x × 2")
  @Iteration("math_eq_1/2", "type=MATH_EQUATION", "text=y = x ÷ 2")
  @Iteration("math_eq_long_exp", "type=MATH_EQUATION", "text=(x^2-1)/(x+1)=y/2")
  @Iteration("math_eq_invalid_exp_extra_error", "type=MATH_EQUATION", "text=y=+x")
  @Iteration("math_eq_invalid_exp_syntax_error", "type=MATH_EQUATION", "text=y=x=z")
  fun testView_allInteractions_differentAnswers_producesAnswerWithOriginalMathExpression() {
    val interactionType = MathInteractionType.valueOf(type)
    val interaction = createInteraction()
    launch(interactionType, interaction).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      replaceExpressionInput(text)

      scenario.onActivity { activity ->
        // The exact original expression should be retained, including spacing (except surrounding
        // spacing).
        val pendingAnswer = activity.mathExpressionViewModel.getPendingAnswer()
        assertThat(pendingAnswer.answer.mathExpression).isEqualTo(text.trim())
      }
    }
  }

  @Test
  @Iteration("numeric_-1", "type=NUMERIC_EXPRESSION", "text=  -  1           ", "expLatex=-1")
  @Iteration("numeric_1+2", "type=NUMERIC_EXPRESSION", "text=1 + 2", "expLatex=1 + 2")
  @Iteration("numeric_1-2", "type=NUMERIC_EXPRESSION", "text=1 − 2", "expLatex=1 - 2")
  @Iteration("numeric_1*2", "type=NUMERIC_EXPRESSION", "text=1 × 2", "expLatex=1 \\times 2")
  @Iteration("numeric_1/2", "type=NUMERIC_EXPRESSION", "text=1 ÷ 2", "expLatex=1 \\div 2")
  @Iteration(
    "numeric_long_exp",
    "type=NUMERIC_EXPRESSION",
    "text=3*2-3+4^3*8/3*2+7",
    "expLatex=3 \\times 2 - 3 + 4 ^ {3} \\times 8 \\div 3 \\times 2 + 7"
  )
  @Iteration("algebraic_-x", "type=ALGEBRAIC_EXPRESSION", "text=  -  x           ", "expLatex=-x")
  @Iteration("algebraic_x+2", "type=ALGEBRAIC_EXPRESSION", "text=x + 2", "expLatex=x + 2")
  @Iteration("algebraic_x-2", "type=ALGEBRAIC_EXPRESSION", "text=x − 2", "expLatex=x - 2")
  @Iteration("algebraic_x*2", "type=ALGEBRAIC_EXPRESSION", "text=x × 2", "expLatex=x \\times 2")
  @Iteration("algebraic_x/2", "type=ALGEBRAIC_EXPRESSION", "text=x ÷ 2", "expLatex=x \\div 2")
  @Iteration(
    "algebraic_long_exp",
    "type=ALGEBRAIC_EXPRESSION",
    "text=12x^2y^2-(yz^2+yzx)-731z",
    "expLatex=12x ^ {2}y ^ {2} - (yz ^ {2} + yzx) - 731z"
  )
  @Iteration("math_eq_-x", "type=MATH_EQUATION", "text=     y=  -  x         ", "expLatex=y = -x")
  @Iteration("math_eq_x+2", "type=MATH_EQUATION", "text=y = x + 2", "expLatex=y = x + 2")
  @Iteration("math_eq_x-2", "type=MATH_EQUATION", "text=y = x − 2", "expLatex=y = x - 2")
  @Iteration("math_eq_x*2", "type=MATH_EQUATION", "text=y = x × 2", "expLatex=y = x \\times 2")
  @Iteration("math_eq_x/2", "type=MATH_EQUATION", "text=y = x ÷ 2", "expLatex=y = x \\div 2")
  @Iteration(
    "math_eq_long_exp",
    "type=MATH_EQUATION",
    "text=(x^2-1)/(x+1)=y/2",
    "expLatex=(x ^ {2} - 1) \\div (x + 1) = y \\div 2"
  )
  fun testView_allInteractions_differentAnswers_validAnswers_producesAnswerWithLatexHtml() {
    val interactionType = MathInteractionType.valueOf(type)
    val interaction = createInteraction()
    launch(interactionType, interaction).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      replaceExpressionInput(text)

      // Valid expressions/equations should produce corresponding LaTeX in block HTML format.
      scenario.onActivity { activity ->
        val pendingAnswer = activity.mathExpressionViewModel.getPendingAnswer()
        assertThat(pendingAnswer.htmlAnswer).contains("<oppia-noninteractive-math")
        assertThat(pendingAnswer.htmlAnswer).contains("render-type=\"block\"")
        // Note that backslashes are double-escaped in the HTML answer; adjust for that here.
        assertThat(pendingAnswer.htmlAnswer.replace("\\\\", "\\")).contains(expLatex)
      }
    }
  }

  @Test
  @Iteration("numeric_1/2", "type=NUMERIC_EXPRESSION", "text=1/2", "expLatex=1 \\div 2")
  @Iteration(
    "numeric_1/2/3", "type=NUMERIC_EXPRESSION", "text=1/2/3", "expLatex=1 \\div 2 \\div 3"
  )
  @Iteration(
    "numeric_1/(2+3)", "type=NUMERIC_EXPRESSION", "text=1/(2+3)", "expLatex=1 \\div (2 + 3)"
  )
  @Iteration("numeric_1/2+3", "type=NUMERIC_EXPRESSION", "text=1/2+3", "expLatex=1 \\div 2 + 3")
  @Iteration("algebraic_x/2", "type=ALGEBRAIC_EXPRESSION", "text=x/2", "expLatex=x \\div 2")
  @Iteration(
    "algebraic_x/y/z", "type=ALGEBRAIC_EXPRESSION", "text=x/y/z", "expLatex=x \\div y \\div z"
  )
  @Iteration(
    "algebraic_x/(2+y)", "type=ALGEBRAIC_EXPRESSION", "text=x/(2+y)", "expLatex=x \\div (2 + y)"
  )
  @Iteration(
    "algebraic_x/2+y", "type=ALGEBRAIC_EXPRESSION", "text=x/2+y", "expLatex=x \\div 2 + y"
  )
  @Iteration("math_eq_x/2", "type=MATH_EQUATION", "text=y=x/2", "expLatex=y = x \\div 2")
  @Iteration(
    "math_eq_x/y/z",
    "type=MATH_EQUATION",
    "text=y/2=x/y/z",
    "expLatex=y \\div 2 = x \\div y \\div z"
  )
  @Iteration(
    "math_eq_x/(2+y)", "type=MATH_EQUATION", "text=x/(2+y)=y", "expLatex=x \\div (2 + y) = y"
  )
  @Iteration("math_eq_x/2+y", "type=MATH_EQUATION", "text=x/2+y=y", "expLatex=x \\div 2 + y = y")
  fun testView_allInteractions_differentValidAnswers_divNotAsFractions_producesLatexWithDivs() {
    val interactionType = MathInteractionType.valueOf(type)
    val interaction = createInteraction(divAsFractions = false)
    launch(interactionType, interaction).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      typeExpressionInput(text)

      // Division-based expressions should produce divisions in LaTeX.
      scenario.onActivity { activity ->
        val pendingAnswer = activity.mathExpressionViewModel.getPendingAnswer()
        assertThat(pendingAnswer.htmlAnswer.replace("\\\\", "\\")).contains(expLatex)
      }
    }
  }

  @Test
  @Iteration("numeric_1/2", "type=NUMERIC_EXPRESSION", "text=1/2", "expLatex=\\frac{1}{2}")
  @Iteration(
    "numeric_1/2/3", "type=NUMERIC_EXPRESSION", "text=1/2/3", "expLatex=\\frac{\\frac{1}{2}}{3}"
  )
  @Iteration(
    "numeric_1/(2+3)", "type=NUMERIC_EXPRESSION", "text=1/(2+3)", "expLatex=\\frac{1}{(2 + 3)}"
  )
  @Iteration(
    "numeric_1/2+3", "type=NUMERIC_EXPRESSION", "text=1/2+3", "expLatex=\\frac{1}{2} + 3"
  )
  @Iteration("algebraic_x/2", "type=ALGEBRAIC_EXPRESSION", "text=x/2", "expLatex=\\frac{x}{2}")
  @Iteration(
    "algebraic_x/y/z",
    "type=ALGEBRAIC_EXPRESSION",
    "text=x/y/z",
    "expLatex=\\frac{\\frac{x}{y}}{z}"
  )
  @Iteration(
    "algebraic_x/(2+y)",
    "type=ALGEBRAIC_EXPRESSION",
    "text=x/(2+y)",
    "expLatex=\\frac{x}{(2 + y)}"
  )
  @Iteration(
    "algebraic_x/2+y", "type=ALGEBRAIC_EXPRESSION", "text=x/2+y", "expLatex=\\frac{x}{2} + y"
  )
  @Iteration("math_eq_x/2", "type=MATH_EQUATION", "text=y=x/2", "expLatex=y = \\frac{x}{2}")
  @Iteration(
    "math_eq_x/y/z",
    "type=MATH_EQUATION",
    "text=y/2=x/y/z",
    "expLatex=\\frac{y}{2} = \\frac{\\frac{x}{y}}{z}"
  )
  @Iteration(
    "math_eq_x/(2+y)", "type=MATH_EQUATION", "text=x/(2+y)=y", "expLatex=\\frac{x}{(2 + y)} = y"
  )
  @Iteration(
    "math_eq_x/2+y", "type=MATH_EQUATION", "text=x/2+y=y", "expLatex=\\frac{x}{2} + y = y"
  )
  fun testView_allInteractions_differentValidAnswers_divAsFractions_producesLatexWithFractions() {
    val interactionType = MathInteractionType.valueOf(type)
    val interaction = createInteraction(divAsFractions = true)
    launch(interactionType, interaction).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      typeExpressionInput(text)

      // Division-based expressions should produce fractions in LaTeX.
      scenario.onActivity { activity ->
        val pendingAnswer = activity.mathExpressionViewModel.getPendingAnswer()
        assertThat(pendingAnswer.htmlAnswer.replace("\\\\", "\\")).contains(expLatex)
      }
    }
  }

  @Test
  @Iteration("numeric_invalid_exp_extra_error", "type=NUMERIC_EXPRESSION", "text=3/0")
  @Iteration("numeric_invalid_exp_syntax_error", "type=NUMERIC_EXPRESSION", "text=3!")
  @Iteration("algebraic_invalid_exp_extra_error", "type=ALGEBRAIC_EXPRESSION", "text=2^x")
  @Iteration("algebraic_invalid_exp_syntax_error", "type=ALGEBRAIC_EXPRESSION", "text=2**x")
  @Iteration("math_eq_invalid_exp_extra_error", "type=MATH_EQUATION", "text=y=+x")
  @Iteration("math_eq_invalid_exp_syntax_error", "type=MATH_EQUATION", "text=y=x=z")
  fun testView_allInteractions_differentAnswers_failingAns_producesAnswerWithOrigExpAsPlainText() {
    val interactionType = MathInteractionType.valueOf(type)
    val interaction = createInteraction()
    launch(interactionType, interaction).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      typeExpressionInput(text)

      // An erroneous expression/equation should result in original expression being shown in
      // plain-text (since there may not be a valid LaTeX representation).
      scenario.onActivity { activity ->
        val pendingAnswer = activity.mathExpressionViewModel.getPendingAnswer()
        assertThat(pendingAnswer.htmlAnswer).isEmpty()
        assertThat(pendingAnswer.plainAnswer).isEqualTo(text.trim())
      }
    }
  }

  @Test
  @Iteration(
    "numeric_-1", "type=NUMERIC_EXPRESSION", "text=  -  1           ", "expA11y=negative 1"
  )
  @Iteration("numeric_1+2", "type=NUMERIC_EXPRESSION", "text=1 + 2", "expA11y=1 plus 2")
  @Iteration("numeric_1-2", "type=NUMERIC_EXPRESSION", "text=1 − 2", "expA11y=1 minus 2")
  @Iteration("numeric_1*2", "type=NUMERIC_EXPRESSION", "text=1 × 2", "expA11y=1 times 2")
  @Iteration("numeric_1/2", "type=NUMERIC_EXPRESSION", "text=1 ÷ 2", "expA11y=1 divided by 2")
  @Iteration(
    "numeric_long_exp",
    "type=NUMERIC_EXPRESSION",
    "text=3*2-3+4^3*8/3*2+7",
    "expA11y=3 times 2 minus 3 plus 4 raised to the power of 3 times 8 divided by 3 times" +
      " 2 plus 7"
  )
  @Iteration(
    "algebraic_-x", "type=ALGEBRAIC_EXPRESSION", "text=  -  x           ", "expA11y=negative x"
  )
  @Iteration("algebraic_x+2", "type=ALGEBRAIC_EXPRESSION", "text=x + 2", "expA11y=x plus 2")
  @Iteration("algebraic_x-2", "type=ALGEBRAIC_EXPRESSION", "text=x − 2", "expA11y=x minus 2")
  @Iteration("algebraic_x*2", "type=ALGEBRAIC_EXPRESSION", "text=x × 2", "expA11y=x times 2")
  @Iteration("algebraic_x/2", "type=ALGEBRAIC_EXPRESSION", "text=x ÷ 2", "expA11y=x divided by 2")
  @Iteration(
    "algebraic_long_exp",
    "type=ALGEBRAIC_EXPRESSION",
    "text=12x^2y^2-(yz^2+yzx)-731z",
    "expA11y=12 x raised to the power of 2 times y raised to the power of 2 minus open" +
      " parenthesis y times zed raised to the power of 2 plus y times zed times x close" +
      " parenthesis minus 731 zed"
  )
  @Iteration(
    "math_eq_-x", "type=MATH_EQUATION", "text=     y=  -  x       ", "expA11y=y equals negative x"
  )
  @Iteration("math_eq_x+2", "type=MATH_EQUATION", "text=y = x + 2", "expA11y=y equals x plus 2")
  @Iteration("math_eq_x-2", "type=MATH_EQUATION", "text=y = x − 2", "expA11y=y equals x minus 2")
  @Iteration("math_eq_x*2", "type=MATH_EQUATION", "text=y = x × 2", "expA11y=y equals x times 2")
  @Iteration(
    "math_eq_x/2", "type=MATH_EQUATION", "text=y = x ÷ 2", "expA11y=y equals x divided by 2"
  )
  @Iteration(
    "math_eq_long_exp",
    "type=MATH_EQUATION",
    "text=(x^2-1)/(x+1)=y/2",
    "expA11y=open parenthesis x raised to the power of 2 minus 1 close parenthesis divided by" +
      " open parenthesis x plus 1 close parenthesis equals y divided by 2"
  )
  fun testView_allInteractions_diffAnswers_english_producesAnswerWithReadableContentDescription() {
    val interactionType = MathInteractionType.valueOf(type)
    val interaction = createInteraction()
    val translationContext = createTranslationContext(language = OppiaLanguage.ENGLISH)
    launch(interactionType, interaction, translationContext).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      replaceExpressionInput(text)

      // Valid expressions/equations should produce corresponding readable a11y strings.
      scenario.onActivity { activity ->
        val pendingAnswer = activity.mathExpressionViewModel.getPendingAnswer()
        assertThat(pendingAnswer.contentDescription).isEqualTo(expA11y)
      }
    }
  }

  @Test
  @Iteration("numeric_1/2", "type=NUMERIC_EXPRESSION", "text=1/2", "expA11y=1 divided by 2")
  @Iteration(
    "numeric_1/2/3",
    "type=NUMERIC_EXPRESSION",
    "text=1/2/3",
    "expA11y=1 divided by 2 divided by 3"
  )
  @Iteration(
    "numeric_1/(2+3)",
    "type=NUMERIC_EXPRESSION",
    "text=1/(2+3)",
    "expA11y=1 divided by open parenthesis 2 plus 3 close parenthesis"
  )
  @Iteration(
    "numeric_1/2+3", "type=NUMERIC_EXPRESSION", "text=1/2+3", "expA11y=1 divided by 2 plus 3"
  )
  @Iteration(
    "numeric_1/3+3", "type=NUMERIC_EXPRESSION", "text=1/3+3", "expA11y=1 divided by 3 plus 3"
  )
  @Iteration("algebraic_x/2", "type=ALGEBRAIC_EXPRESSION", "text=x/2", "expA11y=x divided by 2")
  @Iteration(
    "algebraic_x/y/z",
    "type=ALGEBRAIC_EXPRESSION",
    "text=x/y/z",
    "expA11y=x divided by y divided by zed"
  )
  @Iteration(
    "algebraic_x/(2+y)",
    "type=ALGEBRAIC_EXPRESSION",
    "text=x/(2+y)",
    "expA11y=x divided by open parenthesis 2 plus y close parenthesis"
  )
  @Iteration(
    "algebraic_x/2+y", "type=ALGEBRAIC_EXPRESSION", "text=x/2+y", "expA11y=x divided by 2 plus y"
  )
  @Iteration("math_eq_x/2", "type=MATH_EQUATION", "text=y=x/2", "expA11y=y equals x divided by 2")
  @Iteration(
    "math_eq_x/y/z",
    "type=MATH_EQUATION",
    "text=y/2=x/y/z",
    "expA11y=y divided by 2 equals x divided by y divided by zed"
  )
  @Iteration(
    "math_eq_x/(2+y)",
    "type=MATH_EQUATION",
    "text=x/(2+y)=y",
    "expA11y=x divided by open parenthesis 2 plus y close parenthesis equals y"
  )
  @Iteration(
    "math_eq_x/2+y",
    "type=MATH_EQUATION",
    "text=x/2+y=y",
    "expA11y=x divided by 2 plus y equals y"
  )
  fun testView_allInteractions_diffAnswers_english_divNotAsFractions_producesReadableContentDesc() {
    val interactionType = MathInteractionType.valueOf(type)
    val interaction = createInteraction(divAsFractions = false)
    val translationContext = createTranslationContext(language = OppiaLanguage.ENGLISH)
    launch(interactionType, interaction, translationContext).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      typeExpressionInput(text)

      // Divisions should be correctly represented in the outputted content descriptions.
      scenario.onActivity { activity ->
        val pendingAnswer = activity.mathExpressionViewModel.getPendingAnswer()
        assertThat(pendingAnswer.contentDescription).isEqualTo(expA11y)
      }
    }
  }

  @Test
  @Iteration("numeric_1/2", "type=NUMERIC_EXPRESSION", "text=1/2", "expA11y=one half")
  @Iteration(
    "numeric_1/2/3",
    "type=NUMERIC_EXPRESSION",
    "text=1/2/3",
    "expA11y=the fraction with numerator one half and denominator 3"
  )
  @Iteration(
    "numeric_1/(2+3)",
    "type=NUMERIC_EXPRESSION",
    "text=1/(2+3)",
    "expA11y=the fraction with numerator 1 and denominator open parenthesis 2 plus 3 close" +
      " parenthesis"
  )
  @Iteration(
    "numeric_1/2+3", "type=NUMERIC_EXPRESSION", "text=1/2+3", "expA11y=one half plus 3"
  )
  @Iteration(
    "numeric_1/3+3", "type=NUMERIC_EXPRESSION", "text=1/3+3", "expA11y=1 over 3 plus 3"
  )
  @Iteration("algebraic_x/2", "type=ALGEBRAIC_EXPRESSION", "text=x/2", "expA11y=x over 2")
  @Iteration(
    "algebraic_x/y/z",
    "type=ALGEBRAIC_EXPRESSION",
    "text=x/y/z",
    "expA11y=the fraction with numerator x over y and denominator zed"
  )
  @Iteration(
    "algebraic_x/(2+y)",
    "type=ALGEBRAIC_EXPRESSION",
    "text=x/(2+y)",
    "expA11y=the fraction with numerator x and denominator open parenthesis 2 plus y close" +
      " parenthesis"
  )
  @Iteration(
    "algebraic_x/2+y", "type=ALGEBRAIC_EXPRESSION", "text=x/2+y", "expA11y=x over 2 plus y"
  )
  @Iteration("math_eq_x/2", "type=MATH_EQUATION", "text=y=x/2", "expA11y=y equals x over 2")
  @Iteration(
    "math_eq_x/y/z",
    "type=MATH_EQUATION",
    "text=y/2=x/y/z",
    "expA11y=y over 2 equals the fraction with numerator x over y and denominator zed"
  )
  @Iteration(
    "math_eq_x/(2+y)",
    "type=MATH_EQUATION",
    "text=x/(2+y)=y",
    "expA11y=the fraction with numerator x and denominator open parenthesis 2 plus y close" +
      " parenthesis equals y"
  )
  @Iteration(
    "math_eq_x/2+y", "type=MATH_EQUATION", "text=x/2+y=y", "expA11y=x over 2 plus y equals y"
  )
  fun testView_allInteractions_diffAnswers_english_divAsFractions_producesReadableContentDesc() {
    val interactionType = MathInteractionType.valueOf(type)
    val interaction = createInteraction(divAsFractions = true)
    val translationContext = createTranslationContext(language = OppiaLanguage.ENGLISH)
    launch(interactionType, interaction, translationContext).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      typeExpressionInput(text)

      // Valid expressions/equations should produce corresponding readable a11y strings.
      scenario.onActivity { activity ->
        val pendingAnswer = activity.mathExpressionViewModel.getPendingAnswer()
        assertThat(pendingAnswer.contentDescription).isEqualTo(expA11y)
      }
    }
  }

  @Test
  @Iteration("numeric_invalid_exp_extra_error", "type=NUMERIC_EXPRESSION", "text=3/0")
  @Iteration("numeric_invalid_exp_syntax_error", "type=NUMERIC_EXPRESSION", "text=3!")
  @Iteration("algebraic_invalid_exp_extra_error", "type=ALGEBRAIC_EXPRESSION", "text=2^x")
  @Iteration("algebraic_invalid_exp_syntax_error", "type=ALGEBRAIC_EXPRESSION", "text=2**x")
  @Iteration("math_eq_invalid_exp_extra_error", "type=MATH_EQUATION", "text=y=+x")
  @Iteration("math_eq_invalid_exp_syntax_error", "type=MATH_EQUATION", "text=y=x=z")
  fun testView_allInteractions_invalidAnswers_english_producesAnswerOriginalExpAsDescription() {
    val interactionType = MathInteractionType.valueOf(type)
    val interaction = createInteraction()
    val translationContext = createTranslationContext(language = OppiaLanguage.ENGLISH)
    launch(interactionType, interaction, translationContext).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      typeExpressionInput(text)

      // The content description will be the original expression since no valid human-readable
      // string could be produced.
      scenario.onActivity { activity ->
        val pendingAnswer = activity.mathExpressionViewModel.getPendingAnswer()
        assertThat(pendingAnswer.contentDescription).isEqualTo(text.trim())
      }
    }
  }

  @Test
  @Iteration("numexp_unsp", "type=NUMERIC_EXPRESSION", "lang=LANGUAGE_UNSPECIFIED", "text=3*2")
  @Iteration("numexp_ar", "type=NUMERIC_EXPRESSION", "lang=ARABIC", "text=3*2")
  @Iteration("numexp_hi", "type=NUMERIC_EXPRESSION", "lang=HINDI", "text=3*2")
  @Iteration("numexp_hi-EN", "type=NUMERIC_EXPRESSION", "lang=HINGLISH", "text=3*2")
  @Iteration("numexp_pt", "type=NUMERIC_EXPRESSION", "lang=PORTUGUESE", "text=3*2")
  @Iteration("numexp_pt-BR", "type=NUMERIC_EXPRESSION", "lang=BRAZILIAN_PORTUGUESE", "text=3*2")
  @Iteration("algexp_unsp", "type=ALGEBRAIC_EXPRESSION", "lang=LANGUAGE_UNSPECIFIED", "text=x*2")
  @Iteration("algexp_ar", "type=ALGEBRAIC_EXPRESSION", "lang=ARABIC", "text=x*2")
  @Iteration("algexp_hi", "type=ALGEBRAIC_EXPRESSION", "lang=HINDI", "text=x*2")
  @Iteration("algexp_hi-EN", "type=ALGEBRAIC_EXPRESSION", "lang=HINGLISH", "text=x*2")
  @Iteration("algexp_pt", "type=ALGEBRAIC_EXPRESSION", "lang=PORTUGUESE", "text=x*2")
  @Iteration("algexp_pt-BR", "type=ALGEBRAIC_EXPRESSION", "lang=BRAZILIAN_PORTUGUESE", "text=x*2")
  @Iteration("matheq_unsp", "type=MATH_EQUATION", "lang=LANGUAGE_UNSPECIFIED", "text=y=x")
  @Iteration("matheq_ar", "type=MATH_EQUATION", "lang=ARABIC", "text=y=x")
  @Iteration("matheq_hi", "type=MATH_EQUATION", "lang=HINDI", "text=y=x")
  @Iteration("matheq_hi-EN", "type=MATH_EQUATION", "lang=HINGLISH", "text=y=x")
  @Iteration("matheq_pt", "type=MATH_EQUATION", "lang=PORTUGUESE", "text=y=x")
  @Iteration("matheq_pt-BR", "type=MATH_EQUATION", "lang=BRAZILIAN_PORTUGUESE", "text=y=x")
  fun testView_allInteractions_unsupportedLang_producesAnswerOriginalExpAsDescription() {
    val interactionType = MathInteractionType.valueOf(type)
    val interaction = createInteraction()
    val language = OppiaLanguage.valueOf(lang)
    val translationContext = createTranslationContext(language)
    launch(interactionType, interaction, translationContext).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      typeExpressionInput(text)

      // Unsupported languages should produce content descriptions that are the original expression
      // (since no readable expression can be generated).
      scenario.onActivity { activity ->
        val pendingAnswer = activity.mathExpressionViewModel.getPendingAnswer()
        assertThat(pendingAnswer.contentDescription).isEqualTo(text)
      }
    }
  }

  // This test covers all errors which are not customized based on the specific input provided (i.e.
  // they're the same for all possible expressions/equations that can produce that error), and
  // aren't tied to specific interaction types.
  @Test
  @Iteration(
    "numexp_exp_too_large",
    "type=NUMERIC_EXPRESSION",
    "text=2^10",
    "expErr=Sorry, powers higher than 5 are not supported by the app. Please revise your answer."
  )
  @Iteration(
    "numexp_exp_incomplete_func_name",
    "type=NUMERIC_EXPRESSION",
    "text=sqr(2)",
    "expErr=Did you mean sqrt? If not, please separate the variables with multiplication symbols."
  )
  @Iteration(
    "numexp_exp_hanging_square_root",
    "type=NUMERIC_EXPRESSION",
    "text=√",
    "expErr=Missing input for square root."
  )
  @Iteration(
    "numexp_exp_nested_exponent",
    "type=NUMERIC_EXPRESSION",
    "text=2^3^4",
    "expErr=Sorry, repeated powers/exponents are not supported by the app. Please limit your" +
      " answer to one power."
  )
  @Iteration(
    "numexp_exp_spaces_in_number_input",
    "type=NUMERIC_EXPRESSION",
    "text=3.14 1",
    "expErr=Please remove the spaces between numbers in your answer."
  )
  @Iteration(
    "numexp_exp_consecutive_unary",
    "type=NUMERIC_EXPRESSION",
    "text=--2",
    "expErr=Please remove the extra symbols in your answer."
  )
  @Iteration(
    "numexp_exp_divide_by_zero",
    "type=NUMERIC_EXPRESSION",
    "text=2/0",
    "expErr=Dividing by zero is invalid. Please revise your answer."
  )
  @Iteration(
    "numexp_exp_hanging_parenthesis",
    "type=NUMERIC_EXPRESSION",
    "text=2+(3-1",
    "expErr=Please close or remove parentheses."
  )
  @Iteration(
    "numexp_exp_generic_error",
    "type=NUMERIC_EXPRESSION",
    "text=sqrt 2",
    "expErr=Sorry, we couldn't understand your answer. Please check it to make sure there" +
      " aren't any errors."
  )
  @Iteration(
    "algexp_exp_too_large",
    "type=ALGEBRAIC_EXPRESSION",
    "text=x^10",
    "expErr=Sorry, powers higher than 5 are not supported by the app. Please revise your answer."
  )
  @Iteration(
    "algexp_exp_incomplete_func_name",
    "type=ALGEBRAIC_EXPRESSION",
    "text=sqr(x)",
    "expErr=Did you mean sqrt? If not, please separate the variables with multiplication symbols."
  )
  @Iteration(
    "algexp_exp_hanging_square_root",
    "type=ALGEBRAIC_EXPRESSION",
    "text=√",
    "expErr=Missing input for square root."
  )
  @Iteration(
    "algexp_exp_nested_exponent",
    "type=ALGEBRAIC_EXPRESSION",
    "text=x^3^4",
    "expErr=Sorry, repeated powers/exponents are not supported by the app. Please limit your" +
      " answer to one power."
  )
  @Iteration(
    "algexp_exp_spaces_in_number_input",
    "type=ALGEBRAIC_EXPRESSION",
    "text=3.14 1",
    "expErr=Please remove the spaces between numbers in your answer."
  )
  @Iteration(
    "algexp_exp_consecutive_unary",
    "type=ALGEBRAIC_EXPRESSION",
    "text=--x",
    "expErr=Please remove the extra symbols in your answer."
  )
  @Iteration(
    "algexp_exp_divide_by_zero",
    "type=ALGEBRAIC_EXPRESSION",
    "text=x/0",
    "expErr=Dividing by zero is invalid. Please revise your answer."
  )
  @Iteration(
    "algexp_exp_hanging_parenthesis",
    "type=ALGEBRAIC_EXPRESSION",
    "text=x+(y-z",
    "expErr=Please close or remove parentheses."
  )
  @Iteration(
    "algexp_exp_generic_error",
    "type=ALGEBRAIC_EXPRESSION",
    "text=sqrt x",
    "expErr=Sorry, we couldn't understand your answer. Please check it to make sure there" +
      " aren't any errors."
  )
  @Iteration(
    "matheq_exp_too_large",
    "type=MATH_EQUATION",
    "text=y=x^10",
    "expErr=Sorry, powers higher than 5 are not supported by the app. Please revise your answer."
  )
  @Iteration(
    "matheq_exp_incomplete_func_name",
    "type=MATH_EQUATION",
    "text=y=sqr(x)",
    "expErr=Did you mean sqrt? If not, please separate the variables with multiplication symbols."
  )
  @Iteration(
    "matheq_exp_hanging_square_root",
    "type=MATH_EQUATION",
    "text=y=√",
    "expErr=Missing input for square root."
  )
  @Iteration(
    "matheq_exp_nested_exponent",
    "type=MATH_EQUATION",
    "text=y=x^3^4",
    "expErr=Sorry, repeated powers/exponents are not supported by the app. Please limit your" +
      " answer to one power."
  )
  @Iteration(
    "matheq_exp_spaces_in_number_input",
    "type=MATH_EQUATION",
    "text=y=3.14 1",
    "expErr=Please remove the spaces between numbers in your answer."
  )
  @Iteration(
    "matheq_exp_consecutive_unary",
    "type=MATH_EQUATION",
    "text=y=--x",
    "expErr=Please remove the extra symbols in your answer."
  )
  @Iteration(
    "matheq_exp_divide_by_zero",
    "type=MATH_EQUATION",
    "text=y=x/0",
    "expErr=Dividing by zero is invalid. Please revise your answer."
  )
  @Iteration(
    "matheq_exp_hanging_parenthesis",
    "type=MATH_EQUATION",
    "text=y=x+(y-z",
    "expErr=Please close or remove parentheses."
  )
  @Iteration(
    "matheq_exp_generic_error",
    "type=MATH_EQUATION",
    "text=y=sqrt x",
    "expErr=Sorry, we couldn't understand your answer. Please check it to make sure there" +
      " aren't any errors."
  )
  fun testView_allInteractions_invalidAnswer_casesWithNoCustomization_producesError() {
    val interactionType = MathInteractionType.valueOf(type)
    val interaction = createInteraction()
    launch(interactionType, interaction).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      replaceExpressionInput(text)

      // The expected error should occur for the given answer.
      scenario.onActivity { activity ->
        val answerError = activity.mathExpressionViewModel.checkPendingAnswerError(SUBMIT_TIME)
        assertThat(answerError).isEqualTo(expErr)
      }
    }
  }

  @Test
  fun testView_numericExpression_variableInAnswer_producesError() {
    launchForNumericExpressions().use { scenario ->
      testCoroutineDispatchers.runCurrent()

      typeExpressionInput("x + 2")

      // Variables aren't allowed in numeric expressions.
      scenario.onActivity { activity ->
        val answerError = activity.mathExpressionViewModel.checkPendingAnswerError(SUBMIT_TIME)
        assertThat(answerError).isEqualTo(
          "It looks like you have entered some variables. Please make sure that your answer" +
            " contains numbers only and remove any variables from your answer."
        )
      }
    }
  }

  @Test
  @Iteration("alg_exp", "type=ALGEBRAIC_EXPRESSION", "text=2^x")
  @Iteration("math_equation", "type=MATH_EQUATION", "text=y=2^x")
  fun testView_algebraicInteractions_variableInExponent_producesError() {
    val interactionType = MathInteractionType.valueOf(type)
    val interaction = createInteraction()
    launch(interactionType, interaction).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      typeExpressionInput(text)

      // Variables aren't allowed in exponents.
      scenario.onActivity { activity ->
        val answerError = activity.mathExpressionViewModel.checkPendingAnswerError(SUBMIT_TIME)
        assertThat(answerError).isEqualTo(
          "Sorry, variables in exponents are not supported by the app. Please revise your answer."
        )
      }
    }
  }

  @Test
  @Iteration("y", "text=y", "expErr=Your equation is missing an '=' sign.")
  @Iteration("2y", "text=2y", "expErr=Your equation is missing an '=' sign.")
  @Iteration(
    "y==x",
    "text=y==x",
    "expErr=Your equation contains too many '=' signs. It should have only one."
  )
  @Iteration("y=", "text=y=", "expErr=One of the sides of '=' in your equation is empty.")
  @Iteration("=x", "text==x", "expErr=One of the sides of '=' in your equation is empty.")
  @Iteration("=y=x", "text==y=x", "expErr=One of the sides of '=' in your equation is empty.")
  @Iteration(
    "y=x=",
    "text=y=x=",
    "expErr=Your equation contains too many '=' signs. It should have only one."
  )
  fun testView_mathEquationInput_incorrectEqualsUsage_producesError() {
    launchForMathEquations(interaction = createInteraction()).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      typeExpressionInput(text)

      scenario.onActivity { activity ->
        val answerError = activity.mathExpressionViewModel.checkPendingAnswerError(SUBMIT_TIME)
        assertThat(answerError).isEqualTo(expErr)
      }
    }
  }

  @Test
  @Iteration("num_exp", "type=NUMERIC_EXPRESSION", "text=abs(2)")
  @Iteration("alg_exp", "type=ALGEBRAIC_EXPRESSION", "text=abs(x)")
  @Iteration("math_equation", "type=MATH_EQUATION", "text=y=abs(x)")
  fun testView_allInteractions_invalidFunctionAnswer_producesError() {
    val interactionType = MathInteractionType.valueOf(type)
    val interaction = createInteraction()
    launch(interactionType, interaction).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      typeExpressionInput(text)

      // Functions like 'abs' are not supported.
      scenario.onActivity { activity ->
        val answerError = activity.mathExpressionViewModel.checkPendingAnswerError(SUBMIT_TIME)
        assertThat(answerError)
          .isEqualTo("Function 'abs' is not supported. Please revise your answer.")
      }
    }
  }

  @Test
  @Iteration(
    "num_exp",
    "type=NUMERIC_EXPRESSION",
    "text=1+((2* 3))",
    "expErr=Please remove extra parentheses around the '((2* 3))', for example: '(2* 3)'."
  )
  @Iteration(
    "alg_exp",
    "type=ALGEBRAIC_EXPRESSION",
    "text=x+((y* z))",
    "expErr=Please remove extra parentheses around the '((y* z))', for example: '(y* z)'."
  )
  @Iteration(
    "math_equation",
    "type=MATH_EQUATION",
    "text=y=x+((y* z))",
    "expErr=Please remove extra parentheses around the '((y* z))', for example: '(y* z)'."
  )
  fun testView_allInteractions_multipleRedundantParentheses_producesError() {
    val interactionType = MathInteractionType.valueOf(type)
    val interaction = createInteraction()
    launch(interactionType, interaction).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      typeExpressionInput(text)

      // Terms with extra parentheses should be simplified.
      scenario.onActivity { activity ->
        val answerError = activity.mathExpressionViewModel.checkPendingAnswerError(SUBMIT_TIME)
        assertThat(answerError).isEqualTo(expErr)
      }
    }
  }

  @Test
  @Iteration(
    "num_exp",
    "type=NUMERIC_EXPRESSION",
    "text=1+(2 )",
    "expErr=Please remove the extra parentheses around '(2)', for example: '2'."
  )
  @Iteration(
    "alg_exp",
    "type=ALGEBRAIC_EXPRESSION",
    "text=x+(y )",
    "expErr=Please remove the extra parentheses around '(y)', for example: 'y'."
  )
  @Iteration(
    "math_equation",
    "type=MATH_EQUATION",
    "text=y=x+(y )",
    "expErr=Please remove the extra parentheses around '(y)', for example: 'y'."
  )
  fun testView_allInteractions_individualRedundantParentheses_producesError() {
    val interactionType = MathInteractionType.valueOf(type)
    val interaction = createInteraction()
    launch(interactionType, interaction).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      typeExpressionInput(text)

      // Terms with extra parentheses should be simplified.
      scenario.onActivity { activity ->
        val answerError = activity.mathExpressionViewModel.checkPendingAnswerError(SUBMIT_TIME)
        assertThat(answerError).isEqualTo(expErr)
      }
    }
  }

  @Test
  @Iteration(
    "num_exp",
    "type=NUMERIC_EXPRESSION",
    "text=(1+2 )",
    "expErr=Please remove the parentheses around the whole answer: '(1+2 )'."
  )
  @Iteration(
    "alg_exp",
    "type=ALGEBRAIC_EXPRESSION",
    "text=(x+y )",
    "expErr=Please remove the parentheses around the whole answer: '(x+y )'."
  )
  @Iteration(
    "math_equation",
    "type=MATH_EQUATION",
    "text=y=(x+y )",
    "expErr=Please remove the parentheses around the whole answer: '(x+y )'."
  )
  fun testView_allInteractions_singleRedundantParentheses_producesError() {
    val interactionType = MathInteractionType.valueOf(type)
    val interaction = createInteraction()
    launch(interactionType, interaction).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      typeExpressionInput(text)

      // Terms with extra parentheses should be simplified.
      scenario.onActivity { activity ->
        val answerError = activity.mathExpressionViewModel.checkPendingAnswerError(SUBMIT_TIME)
        assertThat(answerError).isEqualTo(expErr)
      }
    }
  }

  @Test
  @Iteration(
    "numeric_expression_add",
    "type=NUMERIC_EXPRESSION",
    "text=1+",
    "expErr=There seems to be a number or a variable missing after the addition symbol '+'."
  )
  @Iteration(
    "numeric_expression_subtract",
    "type=NUMERIC_EXPRESSION",
    "text=1−",
    "expErr=There seems to be a number or a variable missing after the subtraction symbol '−'."
  )
  @Iteration(
    "numeric_expression_multiply",
    "type=NUMERIC_EXPRESSION",
    "text=1×",
    "expErr=There seems to be a number or a variable missing after the multiplication symbol '×'."
  )
  @Iteration(
    "numeric_expression_divide",
    "type=NUMERIC_EXPRESSION",
    "text=1÷",
    "expErr=There seems to be a number or a variable missing after the division symbol '÷'."
  )
  @Iteration(
    "numeric_expression_exponentiate",
    "type=NUMERIC_EXPRESSION",
    "text=1^",
    "expErr=There seems to be a number or a variable missing after the exponentiation symbol '^'."
  )
  @Iteration(
    "algebraic_expression_add",
    "type=ALGEBRAIC_EXPRESSION",
    "text=x+",
    "expErr=There seems to be a number or a variable missing after the addition symbol '+'."
  )
  @Iteration(
    "algebraic_expression_subtract",
    "type=ALGEBRAIC_EXPRESSION",
    "text=x−",
    "expErr=There seems to be a number or a variable missing after the subtraction symbol '−'."
  )
  @Iteration(
    "algebraic_expression_multiply",
    "type=ALGEBRAIC_EXPRESSION",
    "text=x×",
    "expErr=There seems to be a number or a variable missing after the multiplication symbol '×'."
  )
  @Iteration(
    "algebraic_expression_divide",
    "type=ALGEBRAIC_EXPRESSION",
    "text=x÷",
    "expErr=There seems to be a number or a variable missing after the division symbol '÷'."
  )
  @Iteration(
    "algebraic_expression_exponentiate",
    "type=ALGEBRAIC_EXPRESSION",
    "text=x^",
    "expErr=There seems to be a number or a variable missing after the exponentiation symbol '^'."
  )
  @Iteration(
    "math_equation_add",
    "type=MATH_EQUATION",
    "text=y=x+",
    "expErr=There seems to be a number or a variable missing after the addition symbol '+'."
  )
  @Iteration(
    "math_equation_subtract",
    "type=MATH_EQUATION",
    "text=y=x−",
    "expErr=There seems to be a number or a variable missing after the subtraction symbol '−'."
  )
  @Iteration(
    "math_equation_multiply",
    "type=MATH_EQUATION",
    "text=y=x×",
    "expErr=There seems to be a number or a variable missing after the multiplication symbol '×'."
  )
  @Iteration(
    "math_equation_divide",
    "type=MATH_EQUATION",
    "text=y=x÷",
    "expErr=There seems to be a number or a variable missing after the division symbol '÷'."
  )
  @Iteration(
    "math_equation_exponentiate",
    "type=MATH_EQUATION",
    "text=y=x^",
    "expErr=There seems to be a number or a variable missing after the exponentiation symbol '^'."
  )
  fun testView_allInteractions_noVarOrNumberAfterBinaryOpted_producesError() {
    val interactionType = MathInteractionType.valueOf(type)
    val interaction = createInteraction()
    launch(interactionType, interaction).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      replaceExpressionInput(text)

      // Operands should be on both sides of a binary operator.
      scenario.onActivity { activity ->
        val answerError = activity.mathExpressionViewModel.checkPendingAnswerError(SUBMIT_TIME)
        assertThat(answerError).isEqualTo(expErr)
      }
    }
  }

  // Note that subtraction is omitted here since expressions like '-2' would be treated as negative
  // 2 rather than a subtraction being assumed.
  @Test
  @Iteration(
    "numeric_expression_add",
    "type=NUMERIC_EXPRESSION",
    "text=+1",
    "expErr=Is there a number or a variable missing before or after the addition symbol '+'? If" +
      " not, please remove the extra '+'."
  )
  @Iteration(
    "numeric_expression_multiply",
    "type=NUMERIC_EXPRESSION",
    "text=×1",
    "expErr=Is there a number or a variable missing before or after the multiplication symbol" +
      " '×'? If not, please remove the extra '×'."
  )
  @Iteration(
    "numeric_expression_divide",
    "type=NUMERIC_EXPRESSION",
    "text=÷1",
    "expErr=Is there a number or a variable missing before or after the division symbol '÷'? If" +
      " not, please remove the extra '÷'."
  )
  @Iteration(
    "numeric_expression_exponentiate",
    "type=NUMERIC_EXPRESSION",
    "text=^1",
    "expErr=Is there a number or a variable missing before or after the exponentiation symbol" +
      " '^'? If not, please remove the extra '^'."
  )
  @Iteration(
    "algebraic_expression_add",
    "type=ALGEBRAIC_EXPRESSION",
    "text=+x",
    "expErr=Is there a number or a variable missing before or after the addition symbol '+'? If" +
      " not, please remove the extra '+'."
  )
  @Iteration(
    "algebraic_expression_multiply",
    "type=ALGEBRAIC_EXPRESSION",
    "text=×x",
    "expErr=Is there a number or a variable missing before or after the multiplication symbol" +
      " '×'? If not, please remove the extra '×'."
  )
  @Iteration(
    "algebraic_expression_divide",
    "type=ALGEBRAIC_EXPRESSION",
    "text=÷x",
    "expErr=Is there a number or a variable missing before or after the division symbol '÷'? If" +
      " not, please remove the extra '÷'."
  )
  @Iteration(
    "algebraic_expression_exponentiate",
    "type=ALGEBRAIC_EXPRESSION",
    "text=^x",
    "expErr=Is there a number or a variable missing before or after the exponentiation symbol" +
      " '^'? If not, please remove the extra '^'."
  )
  @Iteration(
    "math_equation_add",
    "type=MATH_EQUATION",
    "text=y=+x",
    "expErr=Is there a number or a variable missing before or after the addition symbol '+'? If" +
      " not, please remove the extra '+'."
  )
  @Iteration(
    "math_equation_multiply",
    "type=MATH_EQUATION",
    "text=y=×x",
    "expErr=Is there a number or a variable missing before or after the multiplication symbol" +
      " '×'? If not, please remove the extra '×'."
  )
  @Iteration(
    "math_equation_divide",
    "type=MATH_EQUATION",
    "text=y=÷x",
    "expErr=Is there a number or a variable missing before or after the division symbol '÷'? If" +
      " not, please remove the extra '÷'."
  )
  @Iteration(
    "math_equation_exponentiate",
    "type=MATH_EQUATION",
    "text=y=^x",
    "expErr=Is there a number or a variable missing before or after the exponentiation symbol" +
      " '^'? If not, please remove the extra '^'."
  )
  fun testView_allInteractions_noVarOrNumberBeforeBinaryOpted_producesError() {
    val interactionType = MathInteractionType.valueOf(type)
    val interaction = createInteraction()
    launch(interactionType, interaction).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      replaceExpressionInput(text)

      // Operands should be on both sides of a binary operator.
      scenario.onActivity { activity ->
        val answerError = activity.mathExpressionViewModel.checkPendingAnswerError(SUBMIT_TIME)
        assertThat(answerError).isEqualTo(expErr)
      }
    }
  }

  @Test
  @Iteration("algebraic_expression", "type=ALGEBRAIC_EXPRESSION", "text=x2")
  @Iteration("math_equation", "type=MATH_EQUATION", "text=y=x2")
  fun testView_algebraicInteractions_numberAfterVariable_producesError() {
    val interactionType = MathInteractionType.valueOf(type)
    val interaction = createInteraction()
    launch(interactionType, interaction).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      typeExpressionInput(text)

      // Cannot imply multiplication with the number on the right side.
      scenario.onActivity { activity ->
        val answerError = activity.mathExpressionViewModel.checkPendingAnswerError(SUBMIT_TIME)
        assertThat(answerError).isEqualTo("Please rearrange the order of x & 2. For example: 2x.")
      }
    }
  }

  @Test
  @Iteration("numeric_expression", "type=NUMERIC_EXPRESSION", "text=1×÷2")
  @Iteration("algebraic_expression", "type=ALGEBRAIC_EXPRESSION", "text=x×÷y")
  @Iteration("math_equation", "type=MATH_EQUATION", "text=z=x×÷y")
  fun testView_allInteractions_consecutiveBinaryOperators_producesError() {
    val interactionType = MathInteractionType.valueOf(type)
    val interaction = createInteraction()
    launch(interactionType, interaction).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      replaceExpressionInput(text)

      // Cannot have subsequent binary operators.
      scenario.onActivity { activity ->
        val answerError = activity.mathExpressionViewModel.checkPendingAnswerError(SUBMIT_TIME)
        assertThat(answerError).isEqualTo("× and ÷ should be separated by a number or a variable.")
      }
    }
  }

  @Test
  @Iteration("numeric_expression", "type=NUMERIC_EXPRESSION", "text=2!")
  @Iteration("algebraic_expression", "type=ALGEBRAIC_EXPRESSION", "text=x!")
  @Iteration("math_equation", "type=MATH_EQUATION", "text=z=x!")
  fun testView_allInteractions_invalidSymbol_producesError() {
    val interactionType = MathInteractionType.valueOf(type)
    val interaction = createInteraction()
    launch(interactionType, interaction).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      typeExpressionInput(text)

      // Invalid symbols should trigger a failure.
      scenario.onActivity { activity ->
        val answerError = activity.mathExpressionViewModel.checkPendingAnswerError(SUBMIT_TIME)
        assertThat(answerError)
          .isEqualTo("There is an invalid '!' in the answer. Please remove it.")
      }
    }
  }

  @Test
  @Iteration(
    "algebraic_expression",
    "type=ALGEBRAIC_EXPRESSION",
    "text=x+y",
    "expErr=Please use the variables specified in the question and not x, y."
  )
  @Iteration(
    "math_equation_single_var_lhs",
    "type=MATH_EQUATION",
    "text=z=x+y",
    "expErr=Please use the variables specified in the question and not z."
  )
  @Iteration(
    "math_equation_single_var_rhs",
    "type=MATH_EQUATION",
    "text=x+y=z",
    "expErr=Please use the variables specified in the question and not x, y."
  )
  fun testView_algebraicInteractions_missingVariables_producesError() {
    val interactionType = MathInteractionType.valueOf(type)
    launch(interactionType).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      typeExpressionInput(text)

      // Using not-allowed-listed variables should result in a failure.
      scenario.onActivity { activity ->
        val answerError = activity.mathExpressionViewModel.checkPendingAnswerError(SUBMIT_TIME)
        assertThat(answerError).isEqualTo(expErr)
      }
    }
  }

  @Test
  @Iteration("numeric_expression", "type=NUMERIC_EXPRESSION", "text=1+2")
  @Iteration("algebraic_expression", "type=ALGEBRAIC_EXPRESSION", "text=x+y")
  @Iteration("math_equation", "type=MATH_EQUATION", "text=z=x+y")
  fun testView_allInteractions_validExpression_doesNotProduceSubmitTimeErrorError() {
    val interactionType = MathInteractionType.valueOf(type)
    val interaction = createInteraction()
    launch(interactionType, interaction).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      typeExpressionInput(text)

      scenario.onActivity { activity ->
        val answerError = activity.mathExpressionViewModel.checkPendingAnswerError(SUBMIT_TIME)
        assertThat(answerError).isNull()
      }
    }
  }

  @Test
  @Iteration("numeric_expression_valid", "type=NUMERIC_EXPRESSION", "text=0/1")
  @Iteration("numeric_expression_invalid", "type=NUMERIC_EXPRESSION", "text=1/0")
  @Iteration("algebraic_expression_valid", "type=ALGEBRAIC_EXPRESSION", "text=x^2")
  @Iteration("algebraic_expression_invalid", "type=ALGEBRAIC_EXPRESSION", "text=2^x")
  @Iteration("math_equation_valid", "type=MATH_EQUATION", "text=z=x^2")
  @Iteration("math_equation_invalid", "type=MATH_EQUATION", "text=z=2^x")
  fun testView_allInteractions_validAndInvalidExpressions_doNotProduceRealTimeError() {
    val interactionType = MathInteractionType.valueOf(type)
    val interaction = createInteraction()
    launch(interactionType, interaction).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      typeExpressionInput(text)

      scenario.onActivity { activity ->
        val answerError = activity.mathExpressionViewModel.checkPendingAnswerError(REAL_TIME)
        assertThat(answerError).isNull()
      }
    }
  }

  @Test
  @Iteration("numeric_expression", "type=NUMERIC_EXPRESSION", "text=")
  @Iteration("algebraic_expression", "type=ALGEBRAIC_EXPRESSION", "text=")
  @Iteration("math_equation", "type=MATH_EQUATION", "text=")
  fun testView_allInteractions_blankInput_produceSubmitTimeError() {
    val interactionType = MathInteractionType.valueOf(type)
    val interaction = createInteraction()
    launch(interactionType, interaction).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      typeExpressionInput(text)

      // Using not-allowed-listed variables should result in a failure.
      scenario.onActivity { activity ->
        val answerError = activity.mathExpressionViewModel.checkPendingAnswerError(SUBMIT_TIME)
        assertThat(answerError).isNotEmpty()
      }
    }
  }

  private fun launchForNumericExpressions(
    interaction: Interaction = Interaction.getDefaultInstance(),
    translationContext: WrittenTranslationContext = WrittenTranslationContext.getDefaultInstance()
  ): ActivityScenario<MathExpressionInteractionsViewTestActivity> {
    return launch(MathInteractionType.NUMERIC_EXPRESSION, interaction, translationContext)
  }

  private fun launchForMathEquations(
    interaction: Interaction = Interaction.getDefaultInstance(),
    translationContext: WrittenTranslationContext = WrittenTranslationContext.getDefaultInstance()
  ): ActivityScenario<MathExpressionInteractionsViewTestActivity> {
    return launch(MathInteractionType.MATH_EQUATION, interaction, translationContext)
  }

  private fun launchForAlgebraicExpressions(
    interaction: Interaction = Interaction.getDefaultInstance(),
    translationContext: WrittenTranslationContext = WrittenTranslationContext.getDefaultInstance()
  ): ActivityScenario<MathExpressionInteractionsViewTestActivity> {
    return launch(MathInteractionType.ALGEBRAIC_EXPRESSION, interaction, translationContext)
  }

  private fun launch(
    interactionType: MathInteractionType,
    interaction: Interaction = Interaction.getDefaultInstance(),
    translationContext: WrittenTranslationContext = WrittenTranslationContext.getDefaultInstance()
  ): ActivityScenario<MathExpressionInteractionsViewTestActivity> {
    return ActivityScenario.launch(
      MathExpressionInteractionsViewTestActivity.createIntent(
        ApplicationProvider.getApplicationContext(),
        MathExpressionInteractionsViewTestActivityParams.newBuilder().apply {
          this.interaction = interaction
          writtenTranslationContext = translationContext
          mathInteractionType = interactionType
        }.build()
      )
    )
  }

  /**
   * Types the specified text into the math expression input interaction view under test.
   *
   * This helper should always be used for this operation rather than using [EditTextInputAction]
   * directly, or in the cases outlined by [replaceExpressionInput].
   */
  private fun typeExpressionInput(text: String) {
    onView(withId(R.id.test_math_expression_input_interaction_view))
      .perform(editTextInputAction.appendText(text))
    testCoroutineDispatchers.runCurrent()
  }

  /**
   * Replaces the text currently in the math expression input interaction view under test with the
   * specified text.
   *
   * [typeExpressionInput] should be used for testing certain raw expressions/equations in all cases
   * except those that require multiple spaces or Unicode characters (since Espresso handles both
   * cases incorrectly).
   */
  private fun replaceExpressionInput(text: String) {
    // Note that replaceText is used here since some answers can contain extra spaces (which will
    // trigger an automatic period to be entered) or Unicode (which Espresso doesn't supported).
    onView(withId(R.id.test_math_expression_input_interaction_view))
      .perform(editTextInputAction.replaceText(text))
    testCoroutineDispatchers.runCurrent()
  }

  private fun createInteractionWithPlaceholder(placeholder: String): Interaction {
    return Interaction.newBuilder().apply {
      putCustomizationArgs(
        "placeholder",
        SchemaObject.newBuilder().apply {
          subtitledUnicode = SubtitledUnicode.newBuilder().apply {
            unicodeStr = placeholder
          }.build()
        }.build()
      )
    }.build()
  }

  private fun createInteractionWithNestedPlaceholder(placeholder: String): Interaction {
    return Interaction.newBuilder().apply {
      putCustomizationArgs(
        "placeholder",
        SchemaObject.newBuilder().apply {
          customSchemaValue = CustomSchemaValue.newBuilder().apply {
            subtitledUnicode = SubtitledUnicode.newBuilder().apply {
              unicodeStr = placeholder
            }.build()
          }.build()
        }.build()
      )
    }.build()
  }

  private fun createInteraction(
    allowedVariables: List<String> = listOf("x", "y", "z"),
    divAsFractions: Boolean = false
  ): Interaction {
    return Interaction.newBuilder().apply {
      putCustomizationArgs(
        "customOskLetters",
        SchemaObject.newBuilder().apply {
          schemaObjectList = SchemaObjectList.newBuilder().apply {
            addAllSchemaObject(
              allowedVariables.map {
                SchemaObject.newBuilder().setNormalizedString(it).build()
              }
            )
          }.build()
        }.build()
      )
      putCustomizationArgs(
        "useFractionForDivision",
        SchemaObject.newBuilder().apply {
          boolValue = divAsFractions
        }.build()
      )
    }.build()
  }

  private fun createTranslationContext(language: OppiaLanguage): WrittenTranslationContext {
    return WrittenTranslationContext.newBuilder().apply {
      this.language = language
    }.build()
  }

  private fun MathExpressionInteractionsViewTestActivity.getInteractionView(): TextView =
    findViewById(R.id.test_math_expression_input_interaction_view)

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

    fun inject(mathExpressionInteractionsViewTest: MathExpressionInteractionsViewTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerMathExpressionInteractionsViewTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(mathExpressionInteractionsViewTest: MathExpressionInteractionsViewTest) {
      component.inject(mathExpressionInteractionsViewTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
