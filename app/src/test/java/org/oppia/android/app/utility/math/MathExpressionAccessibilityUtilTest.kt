package org.oppia.android.app.utility.math

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.google.common.truth.StringSubject
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import dagger.BindsInstance
import dagger.Component
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.activity.ActivityIntentFactoriesModule
import org.oppia.android.app.activity.route.ActivityRouterModule
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.application.testing.TestingBuildFlavorModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.model.MathBinaryOperation
import org.oppia.android.app.model.MathEquation
import org.oppia.android.app.model.MathExpression
import org.oppia.android.app.model.MathFunctionCall
import org.oppia.android.app.model.MathUnaryOperation
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OppiaLanguage.ARABIC
import org.oppia.android.app.model.OppiaLanguage.BRAZILIAN_PORTUGUESE
import org.oppia.android.app.model.OppiaLanguage.ENGLISH
import org.oppia.android.app.model.OppiaLanguage.HINDI
import org.oppia.android.app.model.OppiaLanguage.HINGLISH
import org.oppia.android.app.model.OppiaLanguage.LANGUAGE_UNSPECIFIED
import org.oppia.android.app.model.OppiaLanguage.NIGERIAN_PIDGIN
import org.oppia.android.app.model.OppiaLanguage.PORTUGUESE
import org.oppia.android.app.model.OppiaLanguage.SWAHILI
import org.oppia.android.app.model.OppiaLanguage.UNRECOGNIZED
import org.oppia.android.app.model.Real
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
import org.oppia.android.domain.onboarding.testing.ExpirationMetaDataRetrieverTestModule
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
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Iteration
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Parameter
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.SelectRunnerPlatform
import org.oppia.android.testing.junit.ParameterizedRobolectricTestRunner
import org.oppia.android.testing.math.MathEquationSubject
import org.oppia.android.testing.math.MathEquationSubject.Companion.assertThat
import org.oppia.android.testing.math.MathExpressionSubject
import org.oppia.android.testing.math.MathExpressionSubject.Companion.assertThat
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.testing.LocaleTestModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.math.MathExpressionParser
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode.ALL_ERRORS
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode.REQUIRED_ONLY
import org.oppia.android.util.math.MathExpressionParser.Companion.MathParsingResult
import org.oppia.android.util.math.ONE_HALF
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Singleton

/**
 * Tests for [MathExpressionAccessibilityUtil].
 *
 * Note that this test suite does not make an effort to differentiate tests for numeric and
 * algebraic expressions since it's mainly testing [MathExpression] and [MathEquation] structures,
 * and relies on other test suites to verify that raw numeric expressions can be correctly converted
 * to [MathExpression]s.
 */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(OppiaParameterizedTestRunner::class)
@SelectRunnerPlatform(ParameterizedRobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = MathExpressionAccessibilityUtilTest.TestApplication::class)
class MathExpressionAccessibilityUtilTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  var activityRule =
    ActivityScenarioRule<TestActivity>(
      TestActivity.createIntent(ApplicationProvider.getApplicationContext())
    )

  @Parameter lateinit var language: String
  @Parameter lateinit var expression: String
  @Parameter lateinit var equation: String
  @Parameter lateinit var a11yStr: String

  lateinit var util: MathExpressionAccessibilityUtil

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    activityRule.scenario.onActivity { util = it.mathExpressionAccessibilityUtil }
  }

  @Test
  fun testConvertToString_defaultExp_english_returnsNull() {
    val exp = MathExpression.getDefaultInstance()

    assertThat(exp).forHumanReadable(ENGLISH).doesNotConvertToString()
  }

  @Test
  fun testConvertToString_defaultEq_english_returnsNull() {
    val eq = MathEquation.getDefaultInstance()

    assertThat(eq).forHumanReadable(ENGLISH).doesNotConvertToString()
  }

  @Test
  @Iteration("LANGUAGE_UNSPECIFIED", "language=LANGUAGE_UNSPECIFIED")
  @Iteration("ARABIC", "language=ARABIC")
  @Iteration("HINDI", "language=HINDI")
  @Iteration("HINGLISH", "language=HINGLISH")
  @Iteration("PORTUGUESE", "language=PORTUGUESE")
  @Iteration("BRAZILIAN_PORTUGUESE", "language=BRAZILIAN_PORTUGUESE")
  @Iteration("SWAHILI", "language=SWAHILI")
  @Iteration("NIGERIAN_PIDGIN", "language=NIGERIAN_PIDGIN")
  @Iteration("UNRECOGNIZED", "language=UNRECOGNIZED")
  fun testConvertToString_constExp_unsupportedLanguage_returnsNull() {
    val exp = parseAlgebraicExpression("2")
    val language = OppiaLanguage.valueOf(language)

    assertThat(exp).forHumanReadable(language).doesNotConvertToString()
  }

  @Test
  @Iteration("LANGUAGE_UNSPECIFIED", "language=LANGUAGE_UNSPECIFIED")
  @Iteration("ARABIC", "language=ARABIC")
  @Iteration("HINDI", "language=HINDI")
  @Iteration("HINGLISH", "language=HINGLISH")
  @Iteration("PORTUGUESE", "language=PORTUGUESE")
  @Iteration("BRAZILIAN_PORTUGUESE", "language=BRAZILIAN_PORTUGUESE")
  @Iteration("SWAHILI", "language=SWAHILI")
  @Iteration("NIGERIAN_PIDGIN", "language=NIGERIAN_PIDGIN")
  @Iteration("UNRECOGNIZED", "language=UNRECOGNIZED")
  fun testConvertToString_constEq_unsupportedLanguage_returnsNull() {
    val eq = parseAlgebraicEquation("x=2")
    val language = OppiaLanguage.valueOf(language)

    assertThat(eq).forHumanReadable(language).doesNotConvertToString()
  }

  @Test
  fun testTestSuite_verifyLanguageCoverage_allLanguagesCovered() {
    // NOTE TO DEVELOPERS: This is a meta test to verify that the tests above are covering all
    // supported languages. If this test ever fails, please make sure to update both the list below
    // and other relevant tests in this suite.
    assertThat(OppiaLanguage.values())
      .asList()
      .containsExactly(
        LANGUAGE_UNSPECIFIED, ENGLISH, ARABIC, HINDI, HINGLISH, PORTUGUESE, BRAZILIAN_PORTUGUESE,
        SWAHILI, NIGERIAN_PIDGIN, UNRECOGNIZED
      )
  }

  @Test
  @Iteration("2", "expression=2", "a11yStr=2")
  @Iteration("123", "expression=123", "a11yStr=123")
  @Iteration("1234", "expression=1234", "a11yStr=1,234")
  @Iteration("12345", "expression=12345", "a11yStr=12,345")
  @Iteration("123456", "expression=123456", "a11yStr=123,456")
  @Iteration("1234567", "expression=1234567", "a11yStr=1,234,567")
  fun testConvertToString_eng_constIntExp_returnsIntegerConvertedString() {
    val exp = parseAlgebraicExpression(expression)

    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo(a11yStr)
  }

  @Test
  // Note that some rounding occurs when formatting doubles with decimals.
  @Iteration("2.0", "expression=2.0", "a11yStr=2")
  @Iteration("3.14", "expression=3.14", "a11yStr=3.14")
  @Iteration(
    "long_pi", "expression=3.14159265358979323846264338327950288419716939937510", "a11yStr=3.142"
  )
  @Iteration("1234.0", "expression=1234.0", "a11yStr=1,234")
  @Iteration("12345.0", "expression=12345.0", "a11yStr=12,345")
  @Iteration("123456.0", "expression=123456.0", "a11yStr=123,456")
  @Iteration("1234567.0", "expression=1234567.0", "a11yStr=1,234,567")
  @Iteration("1234567.987654321", "expression=1234567.987654321", "a11yStr=1,234,567.988")
  // Verify that scientific notation isn't used.
  @Iteration("small_number", "expression=0.000000000000000000001", "a11yStr=0")
  @Iteration(
    "large_number", "expression=123456789101112131415.0", "a11yStr=123,456,789,101,112,130,000"
  )
  fun testConvertToString_eng_constDoubleExp_returnsDoubleConvertedString() {
    val exp = parseAlgebraicExpression(expression)

    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo(a11yStr)
  }

  @Test
  @Iteration("x", "expression=x", "a11yStr=x")
  @Iteration("y", "expression=y", "a11yStr=y")
  @Iteration("z", "expression=z", "a11yStr=zed")
  @Iteration("X", "expression=X", "a11yStr=X")
  @Iteration("Y", "expression=Y", "a11yStr=Y")
  @Iteration("Z", "expression=Z", "a11yStr=Zed")
  @Iteration("a", "expression=a", "a11yStr=a")
  fun testConvertToString_eng_variableExp_returnsVariableNameWithZed() {
    val allowedVariables = listOf("a", "x", "y", "z", "X", "Y", "Z")
    val exp = parseAlgebraicExpression(expression, allowedVariables)

    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo(a11yStr)
  }

  @Test
  @Iteration("1+2", "expression=1+2", "a11yStr=1 plus 2")
  @Iteration("1+x", "expression=1+x", "a11yStr=1 plus x")
  @Iteration("z+1234", "expression=z+1234", "a11yStr=zed plus 1,234")
  @Iteration("z+3.14", "expression=z+3.14", "a11yStr=zed plus 3.14")
  @Iteration("x+z", "expression=x+z", "a11yStr=x plus zed")
  fun testConvertToString_eng_addition_returnsLeftPlusRightString() {
    val exp = parseAlgebraicExpression(expression)

    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo(a11yStr)
  }

  @Test
  @Iteration("1-2", "expression=1-2", "a11yStr=1 minus 2")
  @Iteration("1-x", "expression=1-x", "a11yStr=1 minus x")
  @Iteration("z-1234", "expression=z-1234", "a11yStr=zed minus 1,234")
  @Iteration("z-3.14", "expression=z-3.14", "a11yStr=zed minus 3.14")
  @Iteration("x-z", "expression=x-z", "a11yStr=x minus zed")
  fun testConvertToString_eng_subtraction_returnsLeftMinusRightString() {
    val exp = parseAlgebraicExpression(expression)

    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo(a11yStr)
  }

  @Test
  @Iteration("1*2", "expression=1*2", "a11yStr=1 times 2")
  @Iteration("1*x", "expression=1*x", "a11yStr=1 times x")
  @Iteration("z*1234", "expression=z*1234", "a11yStr=zed times 1,234")
  @Iteration("z*3.14", "expression=z*3.14", "a11yStr=zed times 3.14")
  @Iteration("x*z", "expression=x*z", "a11yStr=x times zed")
  fun testConvertToString_eng_multiplication_returnsLeftTimesRightString() {
    val exp = parseAlgebraicExpression(expression)

    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo(a11yStr)
  }

  @Test
  @Iteration("1/2", "expression=1/2", "a11yStr=1 divided by 2")
  @Iteration("1/x", "expression=1/x", "a11yStr=1 divided by x")
  @Iteration("z/1234", "expression=z/1234", "a11yStr=zed divided by 1,234")
  @Iteration("z/3.14", "expression=z/3.14", "a11yStr=zed divided by 3.14")
  @Iteration("x/z", "expression=x/z", "a11yStr=x divided by zed")
  fun testConvertToString_eng_division_returnsLeftDividedByRightString() {
    val exp = parseAlgebraicExpression(expression)

    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo(a11yStr)
  }

  @Test
  @Iteration("1^2", "expression=1^2", "a11yStr=1 raised to the power of 2")
  @Iteration("1^x", "expression=1^x", "a11yStr=1 raised to the power of x")
  @Iteration("z^1234", "expression=z^1234", "a11yStr=zed raised to the power of 1,234")
  @Iteration("z^3.14", "expression=z^3.14", "a11yStr=zed raised to the power of 3.14")
  @Iteration("x^z", "expression=x^z", "a11yStr=x raised to the power of zed")
  fun testConvertToString_eng_exponentiation_returnsLeftRaisedToThePowerOfRightString() {
    // Some expressions may include variable terms as exponents (which normally isn't allowed).
    val exp = parseAlgebraicExpression(expression, errorCheckingMode = REQUIRED_ONLY)

    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo(a11yStr)
  }

  @Test
  @Iteration("-2", "expression=-2", "a11yStr=negative 2")
  @Iteration("-x", "expression=-x", "a11yStr=negative x")
  @Iteration("-1234", "expression=-1234", "a11yStr=negative 1,234")
  @Iteration("-3.14", "expression=-3.14", "a11yStr=negative 3.14")
  @Iteration("-z", "expression=-z", "a11yStr=negative zed")
  fun testConvertToString_eng_negation_returnsNegativeOperandString() {
    val exp = parseAlgebraicExpression(expression)

    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo(a11yStr)
  }

  @Test
  @Iteration("+2", "expression=+2", "a11yStr=positive 2")
  @Iteration("+x", "expression=+x", "a11yStr=positive x")
  @Iteration("+1234", "expression=+1234", "a11yStr=positive 1,234")
  @Iteration("+3.14", "expression=+3.14", "a11yStr=positive 3.14")
  @Iteration("+z", "expression=+z", "a11yStr=positive zed")
  fun testConvertToString_eng_positiveUnary_returnsPositiveOperandString() {
    // Allow positive unary operations to verify this case.
    val exp = parseAlgebraicExpression(expression, errorCheckingMode = REQUIRED_ONLY)

    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo(a11yStr)
  }

  @Test
  @Iteration("√2", "expression=√2", "a11yStr=square root of 2")
  @Iteration("√x", "expression=√x", "a11yStr=square root of x")
  @Iteration("√z", "expression=√z", "a11yStr=square root of zed")
  @Iteration("√1234", "expression=√1234", "a11yStr=square root of 1,234")
  @Iteration("√3.14", "expression=√3.14", "a11yStr=square root of 3.14")
  @Iteration("√(2)", "expression=√(2)", "a11yStr=square root of 2")
  @Iteration("√(x)", "expression=√(x)", "a11yStr=square root of x")
  @Iteration("√(z)", "expression=√(z)", "a11yStr=square root of zed")
  @Iteration("√(1234)", "expression=√(1234)", "a11yStr=square root of 1,234")
  @Iteration("√(3.14)", "expression=√(3.14)", "a11yStr=square root of 3.14")
  fun testConvertToString_eng_inlineSqrt_returnsSquareRootOfArgumentString() {
    // Allow for single-term parentheses for testing (even though these cases would normally result
    // in errors).
    val exp = parseAlgebraicExpression(expression, errorCheckingMode = REQUIRED_ONLY)

    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo(a11yStr)
  }

  @Test
  @Iteration("sqrt(2)", "expression=sqrt(2)", "a11yStr=square root of 2")
  @Iteration("sqrt(x)", "expression=sqrt(x)", "a11yStr=square root of x")
  @Iteration("sqrt(z)", "expression=sqrt(z)", "a11yStr=square root of zed")
  @Iteration("sqrt(1234)", "expression=sqrt(1234)", "a11yStr=square root of 1,234")
  @Iteration("sqrt(3.14)", "expression=sqrt(3.14)", "a11yStr=square root of 3.14")
  fun testConvertToString_eng_sqrt_returnsSquareRootOfArgumentString() {
    val exp = parseAlgebraicExpression(expression)

    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo(a11yStr)
  }

  @Test
  @Iteration("(2)", "expression=(2)", "a11yStr=2")
  @Iteration("(x)", "expression=(x)", "a11yStr=x")
  @Iteration("(z)", "expression=(z)", "a11yStr=zed")
  @Iteration("(1234)", "expression=(1234)", "a11yStr=1,234")
  @Iteration("(3.14)", "expression=(3.14)", "a11yStr=3.14")
  @Iteration("((2))", "expression=((2))", "a11yStr=2")
  @Iteration("((x))", "expression=((x))", "a11yStr=x")
  @Iteration("((z))", "expression=((z))", "a11yStr=zed")
  @Iteration("((1234))", "expression=((1234))", "a11yStr=1,234")
  @Iteration("((3.14))", "expression=((3.14))", "a11yStr=3.14")
  @Iteration("(√2)", "expression=(√2)", "a11yStr=square root of 2")
  @Iteration("(√x)", "expression=(√x)", "a11yStr=square root of x")
  @Iteration("(sqrt(2))", "expression=(sqrt(2))", "a11yStr=square root of 2")
  @Iteration("(sqrt(x))", "expression=(sqrt(x))", "a11yStr=square root of x")
  fun testConvertToString_eng_group_singleTermOrNestedSingleTerm_returnsDirectString() {
    // Allow for single-term parentheses for testing (even though these cases would normally result
    // in errors).
    val exp = parseAlgebraicExpression(expression, errorCheckingMode = REQUIRED_ONLY)

    // Verify that groups are not included in the final string when they only encapsulate single
    // terms.
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo(a11yStr)
  }

  @Test
  @Iteration("(1+2)", "expression=(1+2)", "a11yStr=open parenthesis 1 plus 2 close parenthesis")
  @Iteration("(1+x)", "expression=(1+x)", "a11yStr=open parenthesis 1 plus x close parenthesis")
  @Iteration("(1+z)", "expression=(1+z)", "a11yStr=open parenthesis 1 plus zed close parenthesis")
  @Iteration(
    "(1+1234)", "expression=(1+1234)", "a11yStr=open parenthesis 1 plus 1,234 close parenthesis"
  )
  @Iteration(
    "(1+3.14)", "expression=(1+3.14)", "a11yStr=open parenthesis 1 plus 3.14 close parenthesis"
  )
  @Iteration("(1-2)", "expression=(1-2)", "a11yStr=open parenthesis 1 minus 2 close parenthesis")
  @Iteration("(x-2)", "expression=(x-2)", "a11yStr=open parenthesis x minus 2 close parenthesis")
  @Iteration("(1*2)", "expression=(1*2)", "a11yStr=open parenthesis 1 times 2 close parenthesis")
  @Iteration("(x*2)", "expression=(x*2)", "a11yStr=open parenthesis x times 2 close parenthesis")
  @Iteration(
    "(1/2)", "expression=(1/2)", "a11yStr=open parenthesis 1 divided by 2 close parenthesis"
  )
  @Iteration(
    "(x/2)", "expression=(x/2)", "a11yStr=open parenthesis x divided by 2 close parenthesis"
  )
  @Iteration(
    "(1^2)",
    "expression=(1^2)",
    "a11yStr=open parenthesis 1 raised to the power of 2 close parenthesis"
  )
  @Iteration(
    "(x^2)",
    "expression=(x^2)",
    "a11yStr=open parenthesis x raised to the power of 2 close parenthesis"
  )
  @Iteration("(-2)", "expression=(-2)", "a11yStr=open parenthesis negative 2 close parenthesis")
  @Iteration("(-x)", "expression=(-x)", "a11yStr=open parenthesis negative x close parenthesis")
  @Iteration("(+2)", "expression=(+2)", "a11yStr=open parenthesis positive 2 close parenthesis")
  @Iteration("(+x)", "expression=(+x)", "a11yStr=open parenthesis positive x close parenthesis")
  fun testConvertToString_eng_group_nestedOps_returnOpenParensOpCloseParensString() {
    // Allow for the outer expression to have redundant parentheses to test cases when groups are
    // announced (even though these exact cases would normally result in an error).
    val exp = parseAlgebraicExpression(expression, errorCheckingMode = REQUIRED_ONLY)

    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo(a11yStr)
  }

  @Test
  @Iteration("√-2", "expression=√-2", "a11yStr=start square root negative 2 end square root")
  @Iteration("√-x", "expression=√-x", "a11yStr=start square root negative x end square root")
  @Iteration("√+2", "expression=√+2", "a11yStr=start square root positive 2 end square root")
  @Iteration("√+x", "expression=√+x", "a11yStr=start square root positive x end square root")
  // Note that these cases compose with the group cases since √ only "attached" to the immediate
  // next terms rather than being able to encapsulate a whole operation (like sqrt()).
  @Iteration(
    "√(1+2)",
    "expression=√(1+2)",
    "a11yStr=start square root open parenthesis 1 plus 2 close parenthesis end square root"
  )
  @Iteration(
    "√(1+x)",
    "expression=√(1+x)",
    "a11yStr=start square root open parenthesis 1 plus x close parenthesis end square root"
  )
  @Iteration(
    "√(1-2)",
    "expression=√(1-2)",
    "a11yStr=start square root open parenthesis 1 minus 2 close parenthesis end square root"
  )
  @Iteration(
    "√(1-x)",
    "expression=√(1-x)",
    "a11yStr=start square root open parenthesis 1 minus x close parenthesis end square root"
  )
  @Iteration(
    "√(1*2)",
    "expression=√(1*2)",
    "a11yStr=start square root open parenthesis 1 times 2 close parenthesis end square root"
  )
  @Iteration(
    "√(1*x)",
    "expression=√(1*x)",
    "a11yStr=start square root open parenthesis 1 times x close parenthesis end square root"
  )
  @Iteration(
    "√(1/2)",
    "expression=√(1/2)",
    "a11yStr=start square root open parenthesis 1 divided by 2 close parenthesis end square root"
  )
  @Iteration(
    "√(1/x)",
    "expression=√(1/x)",
    "a11yStr=start square root open parenthesis 1 divided by x close parenthesis end square root"
  )
  @Iteration(
    "√(1^2)",
    "expression=√(1^2)",
    "a11yStr=start square root open parenthesis 1 raised to the power of 2 close parenthesis" +
      " end square root"
  )
  @Iteration(
    "√(1^x)",
    "expression=√(1^x)",
    "a11yStr=start square root open parenthesis 1 raised to the power of x close parenthesis" +
      " end square root"
  )
  @Iteration(
    "√(-2)",
    "expression=√(-2)",
    "a11yStr=start square root open parenthesis negative 2 close parenthesis end square root"
  )
  @Iteration(
    "√(-x)",
    "expression=√(-x)",
    "a11yStr=start square root open parenthesis negative x close parenthesis end square root"
  )
  @Iteration(
    "√(+2)",
    "expression=√(+2)",
    "a11yStr=start square root open parenthesis positive 2 close parenthesis end square root"
  )
  @Iteration(
    "√(+x)",
    "expression=√(+x)",
    "a11yStr=start square root open parenthesis positive x close parenthesis end square root"
  )
  fun testConvertToString_eng_inlineSqrt_nestedOp_returnsStartSquareRootConstructString() {
    // Allow for positive unary expressions.
    val exp = parseAlgebraicExpression(expression, errorCheckingMode = REQUIRED_ONLY)

    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo(a11yStr)
  }

  @Test
  @Iteration(
    "sqrt(1+2)", "expression=sqrt(1+2)", "a11yStr=start square root 1 plus 2 end square root"
  )
  @Iteration(
    "sqrt(1+x)", "expression=sqrt(1+x)", "a11yStr=start square root 1 plus x end square root"
  )
  @Iteration(
    "sqrt(1-2)", "expression=sqrt(1-2)", "a11yStr=start square root 1 minus 2 end square root"
  )
  @Iteration(
    "sqrt(1-x)", "expression=sqrt(1-x)", "a11yStr=start square root 1 minus x end square root"
  )
  @Iteration(
    "sqrt(1*2)", "expression=sqrt(1*2)", "a11yStr=start square root 1 times 2 end square root"
  )
  @Iteration(
    "sqrt(1*x)", "expression=sqrt(1*x)", "a11yStr=start square root 1 times x end square root"
  )
  @Iteration(
    "sqrt(1/2)",
    "expression=sqrt(1/2)",
    "a11yStr=start square root 1 divided by 2 end square root"
  )
  @Iteration(
    "sqrt(1/x)",
    "expression=sqrt(1/x)",
    "a11yStr=start square root 1 divided by x end square root"
  )
  @Iteration(
    "sqrt(1^2)",
    "expression=sqrt(1^2)",
    "a11yStr=start square root 1 raised to the power of 2 end square root"
  )
  @Iteration(
    "sqrt(1^x)",
    "expression=sqrt(1^x)",
    "a11yStr=start square root 1 raised to the power of x end square root"
  )
  @Iteration(
    "sqrt(-2)", "expression=sqrt(-2)", "a11yStr=start square root negative 2 end square root"
  )
  @Iteration(
    "sqrt(-x)", "expression=sqrt(-x)", "a11yStr=start square root negative x end square root"
  )
  @Iteration(
    "sqrt(+2)", "expression=sqrt(+2)", "a11yStr=start square root positive 2 end square root"
  )
  @Iteration(
    "sqrt(+x)", "expression=sqrt(+x)", "a11yStr=start square root positive x end square root"
  )
  fun testConvertToString_eng_sqrt_nestedOp_returnsStartSquareRootConstructString() {
    // Allow for positive unary expressions.
    val exp = parseAlgebraicExpression(expression, errorCheckingMode = REQUIRED_ONLY)

    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo(a11yStr)
  }

  @Test
  // Note that numeric exponentiations must be explicitly multiplied next to a constant. They
  // otherwise result in a grammatical error that cannot be resolved.
  @Iteration("2x", "expression=2x", "a11yStr=2 x")
  @Iteration("2z", "expression=2z", "a11yStr=2 zed")
  @Iteration("2x^3", "expression=2x^3", "a11yStr=2 x raised to the power of 3")
  @Iteration("2z^3", "expression=2z^3", "a11yStr=2 zed raised to the power of 3")
  @Iteration("1234x^3.14", "expression=1234x^3.14", "a11yStr=1,234 x raised to the power of 3.14")
  fun testConvertToString_eng_implicitMult_leftConst_rightVarOrExp_returnsLeftRightString() {
    val exp = parseAlgebraicExpression(expression)

    // Verify that the format <constant> <var> [^ <rhs>] results in an implicit multiplication with
    // no 'times' announced.
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo(a11yStr)
  }

  @Test
  @Iteration("xz", "expression=xz", "a11yStr=x times zed")
  @Iteration("2xy", "expression=2yx", "a11yStr=2 y times x")
  @Iteration("2√x", "expression=2√x", "a11yStr=2 times square root of x")
  @Iteration("2sqrt(x)", "expression=2sqrt(x)", "a11yStr=2 times square root of x")
  @Iteration("2(3)", "expression=2(3)", "a11yStr=2 times 3")
  @Iteration("2(x)", "expression=2(x)", "a11yStr=2 times x")
  @Iteration(
    "2(x^3)",
    "expression=2(x^3)",
    "a11yStr=2 times open parenthesis x raised to the power of 3 close parenthesis"
  )
  fun testConvertToString_eng_impMult_nonLeftConst_orRightIsNotVarOrExp_returnsLeftTimesRightStr() {
    // Allow for redundant single-term parentheses.
    val exp = parseAlgebraicExpression(expression, errorCheckingMode = REQUIRED_ONLY)

    // If anything breaks up the format tested in the previous test (even if it's a group), then the
    // multiplication is explicitly read out.
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo(a11yStr)
  }

  @Test
  fun testConvertToString_eng_divisionAsFractions_oneDivTwo_returnsOneHalfString() {
    val exp = parseAlgebraicExpression("1/2")

    // 1/2 is a special case.
    assertThat(exp)
      .forHumanReadable(ENGLISH)
      .convertsWithFractionsToStringThat().isEqualTo("one half")
  }

  @Test
  @Iteration("0/1", "expression=0/1", "a11yStr=0 over 1")
  @Iteration("1/1", "expression=1/1", "a11yStr=1 over 1")
  @Iteration("0/2", "expression=0/2", "a11yStr=0 over 2")
  @Iteration("2/2", "expression=2/2", "a11yStr=2 over 2")
  @Iteration("0/3", "expression=0/3", "a11yStr=0 over 3")
  @Iteration("1/3", "expression=1/3", "a11yStr=1 over 3")
  @Iteration("2/3", "expression=2/3", "a11yStr=2 over 3")
  @Iteration("3/3", "expression=3/3", "a11yStr=3 over 3")
  @Iteration("4/3", "expression=4/3", "a11yStr=4 over 3")
  @Iteration("5/3", "expression=5/3", "a11yStr=5 over 3")
  @Iteration("6/3", "expression=6/3", "a11yStr=6 over 3")
  @Iteration("5/9", "expression=5/9", "a11yStr=5 over 9")
  @Iteration("19/3", "expression=19/3", "a11yStr=19 over 3")
  @Iteration("2/17", "expression=2/17", "a11yStr=2 over 17")
  fun testConvertToString_eng_divisionAsFractions_smallIntegerFracs_returnsNumOverDenomString() {
    val exp = parseAlgebraicExpression(expression)

    assertThat(exp).forHumanReadable(ENGLISH).convertsWithFractionsToStringThat().isEqualTo(a11yStr)
  }

  @Test
  @Iteration("1/1234", "expression=1/1234", "a11yStr=1 over 1,234")
  @Iteration("1234/1", "expression=1234/1", "a11yStr=1,234 over 1")
  @Iteration("1234/987654", "expression=1234/987654", "a11yStr=1,234 over 987,654")
  fun testConvertToString_eng_divisionAsFractions_largeIntegerFracs_returnsNumOverDenomString() {
    val exp = parseAlgebraicExpression(expression)

    // Large numbers are read as part of the fraction.
    assertThat(exp).forHumanReadable(ENGLISH).convertsWithFractionsToStringThat().isEqualTo(a11yStr)
  }

  @Test
  @Iteration("1/x", "expression=1/x", "a11yStr=1 over x")
  @Iteration("1/z", "expression=1/z", "a11yStr=1 over zed")
  @Iteration("x/2", "expression=x/2", "a11yStr=x over 2")
  @Iteration("z/3", "expression=z/3", "a11yStr=zed over 3")
  @Iteration("x/z", "expression=x/z", "a11yStr=x over zed")
  fun testConvertToString_eng_divisionAsFractions_fracsWithVariables_returnsNumOverDenomString() {
    val exp = parseAlgebraicExpression(expression)

    // Variables are read as part of the fraction.
    assertThat(exp).forHumanReadable(ENGLISH).convertsWithFractionsToStringThat().isEqualTo(a11yStr)
  }

  @Test
  @Iteration(
    "x/√2",
    "expression=x/√2",
    "a11yStr=the fraction with numerator x and denominator square root of 2"
  )
  @Iteration(
    "x/-2",
    "expression=x/-2",
    "a11yStr=the fraction with numerator x and denominator negative 2"
  )
  @Iteration(
    "2/(1+2)",
    "expression=2/(1+2)",
    "a11yStr=the fraction with numerator 2 and denominator open parenthesis 1 plus 2 close" +
      " parenthesis"
  )
  // Nested fractions still cause the outer fraction to be read out the long way.
  @Iteration(
    "2/(1/2)",
    "expression=2/(1/2)",
    "a11yStr=the fraction with numerator 2 and denominator open parenthesis one half close" +
      " parenthesis"
  )
  @Iteration(
    "2/(1/3)",
    "expression=2/(1/3)",
    "a11yStr=the fraction with numerator 2 and denominator open parenthesis 1 over 3 close" +
      " parenthesis"
  )
  @Iteration(
    "x/sqrt(y/3)",
    "expression=x/sqrt(y/3)",
    "a11yStr=the fraction with numerator x and denominator start square root y over 3 end" +
      " square root"
  )
  @Iteration(
    "3.14/x", "expression=3.14/x", "a11yStr=the fraction with numerator 3.14 and denominator x"
  )
  @Iteration(
    "x/3.14", "expression=x/3.14", "a11yStr=the fraction with numerator x and denominator 3.14"
  )
  fun testConvertToString_eng_divisionAsFractions_fracWithComplexParts_returnsFracConstructStr() {
    val exp = parseAlgebraicExpression(expression)

    // Verify that complex fractions are read out with more specificity.
    assertThat(exp).forHumanReadable(ENGLISH).convertsWithFractionsToStringThat().isEqualTo(a11yStr)
  }

  @Test
  @Iteration("1=2", "expression=1=2", "a11yStr=1 equals 2")
  @Iteration("x=1", "expression=x=1", "a11yStr=x equals 1")
  @Iteration("z=1", "expression=z=1", "a11yStr=zed equals 1")
  @Iteration("2=x", "expression=2=x", "a11yStr=2 equals x")
  @Iteration("2=z", "expression=2=z", "a11yStr=2 equals zed")
  @Iteration("x=z", "expression=x=z", "a11yStr=x equals zed")
  fun testConvertToString_eng_simpleEquation_returnsLeftEqualsRightString() {
    val eq = parseAlgebraicEquation(expression)

    assertThat(eq).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo(a11yStr)
  }

  @Test
  @Iteration("xyz", "expression=xyz", "a11yStr=x times y times zed")
  @Iteration("1+x+x^2", "expression=1+x+x^2", "a11yStr=1 plus x plus x raised to the power of 2")
  @Iteration(
    "-3x^2+23x-14",
    "expression=-3x^2+23x-14",
    "a11yStr=negative 3 times x raised to the power of 2 plus 23 x minus 14"
  )
  @Iteration(
    "y^2+xy+x^2",
    "expression=y^2+xy+x^2",
    "a11yStr=y raised to the power of 2 plus x times y plus x raised to the power of 2"
  )
  fun testConvertToString_eng_polynomialExpressions_returnsCorrectlyBuiltString() {
    val exp = parseAlgebraicExpression(expression)

    // Polynomials should be read out correctly.
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo(a11yStr)
  }

  @Test
  @Iteration("z=xyz", "expression=z=xyz", "a11yStr=zed equals x times y times zed")
  @Iteration(
    "y=1+x+x^2",
    "expression=y=1+x+x^2",
    "a11yStr=y equals 1 plus x plus x raised to the power of 2"
  )
  @Iteration(
    "-3x^2+23x-14=7y^3",
    "expression=-3x^2+23x-14=7y^3",
    "a11yStr=negative 3 times x raised to the power of 2 plus 23 x minus 14 equals 7 y raised" +
      " to the power of 3"
  )
  @Iteration(
    "sqrt(z)=y^2+xy+x^2",
    "expression=sqrt(z)=y^2+xy+x^2",
    "a11yStr=square root of zed equals y raised to the power of 2 plus x times y plus x raised" +
      " to the power of 2"
  )
  fun testConvertToString_eng_polynomialEquations_returnsCorrectlyBuiltString() {
    val eq = parseAlgebraicEquation(expression)

    // Polynomial equations should be read out correctly.
    assertThat(eq).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo(a11yStr)
  }

  @Test
  @Iteration(
    "(x^2+2x+1)/(x+1)",
    "expression= (   x^2 +  2x + 1 ) /( x  + 1)",
    "a11yStr=open parenthesis x raised to the power of 2 plus 2 x plus 1 close parenthesis" +
      " divided by open parenthesis x plus 1 close parenthesis"
  )
  @Iteration(
    "(1/2)x",
    "expression=(1/2) x",
    "a11yStr=open parenthesis 1 divided by 2 close parenthesis times x"
  )
  @Iteration(
    "(-27x^3)^(1/3)",
    "expression=(\t-27x\n^3\r)^(1 /  3) ",
    "a11yStr=open parenthesis negative 27 times x raised to the power of 3 close parenthesis" +
      " raised to the power of open parenthesis 1 divided by 3 close parenthesis"
  )
  @Iteration(
    "(4x^2)^(-1/2)",
    "expression=( 4x  ^ 2)  ^ (  - 1  / 2  ) ",
    "a11yStr=open parenthesis 4 x raised to the power of 2 close parenthesis raised to the" +
      " power of open parenthesis negative 1 divided by 2 close parenthesis"
  )
  @Iteration(
    "sqrt(sqrt(sqrt(x)+1))",
    "expression=sqrt(   sqrt(  sqrt( x ) + 1  )   )",
    "a11yStr=square root of start square root square root of x plus 1 end square root"
  )
  @Iteration(
    "x-(1+(y-(2+z)))",
    "expression= x  -   (    1     +      (       y        -        (          2      +    z )))",
    "a11yStr=x minus open parenthesis 1 plus open parenthesis y minus open parenthesis 2 plus" +
      " zed close parenthesis close parenthesis close parenthesis"
  )
  @Iteration(
    "1/(2/(y+3/z))",
    "expression=1 /    ( 2 /  ( y + 3/z  ) )",
    "a11yStr=1 divided by open parenthesis 2 divided by open parenthesis y plus 3 divided by" +
      " zed close parenthesis close parenthesis"
  )
  @Iteration(
    "x/y/z/2", "expression= x/ y/ z/ 2", "a11yStr=x divided by y divided by zed divided by 2"
  )
  fun testConvertToString_eng_complexNestedExpression_returnsCorrectlyBuiltString() {
    val exp = parseAlgebraicExpression(expression)

    // The expression should correctly convert to a readable string, and all original whitespace
    // should be ignored in the final rendered string.
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo(a11yStr)
  }

  @Test
  @Iteration(
    "(x^2+2x+1)/(x+1)",
    "expression= (   x^2 +  2x + 1 ) /( x  + 1)",
    "a11yStr=the fraction with numerator open parenthesis x raised to the power of 2 plus 2 x" +
      " plus 1 close parenthesis and denominator open parenthesis x plus 1 close parenthesis"
  )
  @Iteration(
    "(1/2)x", "expression=(1/2) x", "a11yStr=open parenthesis one half close parenthesis times x"
  )
  @Iteration(
    "(-27x^3)^(1/3)",
    "expression=(\t-27x\n^3\r)^(1 /  3) ",
    "a11yStr=open parenthesis negative 27 times x raised to the power of 3 close parenthesis" +
      " raised to the power of open parenthesis 1 over 3 close parenthesis"
  )
  @Iteration(
    "(4x^2)^(-1/2)",
    "expression=( 4x  ^ 2)  ^ (  - 1  / 2  ) ",
    "a11yStr=open parenthesis 4 x raised to the power of 2 close parenthesis raised to the" +
      " power of open parenthesis the fraction with numerator negative 1 and denominator 2" +
      " close parenthesis"
  )
  @Iteration(
    "sqrt(sqrt(sqrt(x)+1))",
    "expression=sqrt(   sqrt(  sqrt( x ) + 1  )   )",
    "a11yStr=square root of start square root square root of x plus 1 end square root"
  )
  @Iteration(
    "x-(1+(y-(2+z)))",
    "expression= x  -   (    1     +      (       y        -        (          2      +    z )))",
    "a11yStr=x minus open parenthesis 1 plus open parenthesis y minus open parenthesis 2 plus" +
      " zed close parenthesis close parenthesis close parenthesis"
  )
  @Iteration(
    "1/(2/(y+3/z))",
    "expression=1 /    (  2 /  ( y + 3/z  ) )",
    "a11yStr=the fraction with numerator 1 and denominator open parenthesis the fraction with" +
      " numerator 2 and denominator open parenthesis y plus 3 over zed close parenthesis" +
      " close parenthesis"
  )
  @Iteration(
    "x/y/z/2",
    "expression= x/ y/ z/ 2",
    "a11yStr=the fraction with numerator the fraction with numerator x over y and denominator" +
      " zed and denominator 2"
  )
  fun testConvertToString_eng_complexNestedExpression_divAsFracs_returnsCorrectlyBuiltString() {
    val exp = parseAlgebraicExpression(expression)

    // The expression should correctly convert to a readable string, and all original whitespace
    // should be ignored in the final rendered string.
    assertThat(exp).forHumanReadable(ENGLISH).convertsWithFractionsToStringThat().isEqualTo(a11yStr)
  }

  @Test
  @Iteration(
    "y=(x^2+2x+1)/(x+1)",
    "expression=  y = (   x^2 +  2x + 1 ) /( x  + 1)",
    "a11yStr=y equals open parenthesis x raised to the power of 2 plus 2 x plus 1 close" +
      " parenthesis divided by open parenthesis x plus 1 close parenthesis"
  )
  @Iteration(
    "(1/2)x=sqrt(x)",
    "expression=(1/2) x               =sqrt                  (x)",
    "a11yStr=open parenthesis 1 divided by 2 close parenthesis times x equals square root of x"
  )
  @Iteration(
    "-3x=(-27x^3)^(1/3)",
    "expression=\n-\n3\nx\n=\n(\t-27x\n^3\r)^(1 /  3) ",
    "a11yStr=negative 3 times x equals open parenthesis negative 27 times x raised to the power" +
      " of 3 close parenthesis raised to the power of open parenthesis 1 divided by 3 close" +
      " parenthesis"
  )
  @Iteration(
    "(4x^2)^(-1/2)=1+x",
    "expression=( 4x  ^ 2)  ^ (  - 1  / 2  ) =1 +                    x            ",
    "a11yStr=open parenthesis 4 x raised to the power of 2 close parenthesis raised to the" +
      " power of open parenthesis negative 1 divided by 2 close parenthesis equals 1 plus x"
  )
  @Iteration(
    "sqrt(sqrt(sqrt(x)+1))=1/2",
    "expression=sqrt(   sqrt(  sqrt( x ) + 1  )   ) = 1        /        2",
    "a11yStr=square root of start square root square root of x plus 1 end square root equals 1" +
      " divided by 2"
  )
  @Iteration(
    "xy+x+y=x-(1+(y-(2+z)))",
    "expression=xy+x+y=x  -   (    1     +      (       y        -        (        2    +  z )))",
    "a11yStr=x times y plus x plus y equals x minus open parenthesis 1 plus open parenthesis y" +
      " minus open parenthesis 2 plus zed close parenthesis close parenthesis close parenthesis"
  )
  @Iteration(
    "x=1/(2/(y+3/z))",
    "expression=  x =   1 /    ( 2 /  ( y + 3/z  ) )",
    "a11yStr=x equals 1 divided by open parenthesis 2 divided by open parenthesis y plus 3" +
      " divided by zed close parenthesis close parenthesis"
  )
  @Iteration(
    "x/y/z/2=z",
    "expression= x/ y/ z/ 2=z",
    "a11yStr=x divided by y divided by zed divided by 2 equals zed"
  )
  fun testConvertToString_eng_complexNestedEquations_returnsCorrectlyBuiltString() {
    val eq = parseAlgebraicEquation(expression)

    // The equation should correctly convert to a readable string, and all original whitespace
    // should be ignored in the final rendered string.
    assertThat(eq).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo(a11yStr)
  }

  @Test
  @Iteration(
    "y=(x^2+2x+1)/(x+1)",
    "expression=  y = (   x^2 +  2x + 1 ) /( x  + 1)",
    "a11yStr=y equals the fraction with numerator open parenthesis x raised to the power of 2" +
      " plus 2 x plus 1 close parenthesis and denominator open parenthesis x plus 1 close" +
      " parenthesis"
  )
  @Iteration(
    "(1/2)x=sqrt(x)",
    "expression=(1/2) x               =sqrt                  (x)",
    "a11yStr=open parenthesis one half close parenthesis times x equals square root of x"
  )
  @Iteration(
    "-3x=(-27x^3)^(1/3)",
    "expression=\n-\n3\nx\n=\n(\t-27x\n^3\r)^(1 /  3) ",
    "a11yStr=negative 3 times x equals open parenthesis negative 27 times x raised to the power" +
      " of 3 close parenthesis raised to the power of open parenthesis 1 over 3 close parenthesis"
  )
  @Iteration(
    "(4x^2)^(-1/2)=1+x",
    "expression=( 4x  ^ 2)  ^ (  - 1  / 2  ) =1 +                    x            ",
    "a11yStr=open parenthesis 4 x raised to the power of 2 close parenthesis raised to the" +
      " power of open parenthesis the fraction with numerator negative 1 and denominator 2" +
      " close parenthesis equals 1 plus x"
  )
  @Iteration(
    "sqrt(sqrt(sqrt(x)+1))=1/2",
    "expression=sqrt(   sqrt(  sqrt( x ) + 1  )   ) = 1        /        2",
    "a11yStr=square root of start square root square root of x plus 1 end square root equals" +
      " one half"
  )
  @Iteration(
    "xy+x+y=x-(1+(y-(2+z)))",
    "expression=xy+x+y=x  -   (    1     +      (       y        -        (        2    +  z )))",
    "a11yStr=x times y plus x plus y equals x minus open parenthesis 1 plus open parenthesis y" +
      " minus open parenthesis 2 plus zed close parenthesis close parenthesis close parenthesis"
  )
  @Iteration(
    "x=1/(2/(y+3/z))",
    "expression=  x =   1 /    ( 2 /  ( y + 3/z  ) )",
    "a11yStr=x equals the fraction with numerator 1 and denominator open parenthesis the" +
      " fraction with numerator 2 and denominator open parenthesis y plus 3 over zed close" +
      " parenthesis close parenthesis"
  )
  @Iteration(
    "x/y/z/2=z",
    "expression= x/ y/ z/ 2=z",
    "a11yStr=the fraction with numerator the fraction with numerator x over y and denominator" +
      " zed and denominator 2 equals zed"
  )
  fun testConvertToString_eng_complexNestedEquations_divAsFracs_returnsCorrectlyBuiltString() {
    val eq = parseAlgebraicEquation(expression)

    // The equation should correctly convert to a readable string, and all original whitespace
    // should be ignored in the final rendered string.
    assertThat(eq).forHumanReadable(ENGLISH).convertsWithFractionsToStringThat().isEqualTo(a11yStr)
  }

  // This & the next test are implementing cases defined in the doc:
  // https://docs.google.com/document/d/1P-dldXQ08O-02ZRG978paiWOSz0dsvcKpDgiV_rKH_Y/edit#.
  @Test
  @Iteration(
    "(x + 6)/(x - 4)",
    "expression=(x + 6)/(x - 4)",
    "a11yStr=the fraction with numerator open parenthesis x plus 6 close parenthesis and" +
      " denominator open parenthesis x minus 4 close parenthesis"
  )
  @Iteration(
    "4*(x)^(2)+20x",
    "expression=4*(x)^(2)+20x",
    "a11yStr=4 times x raised to the power of 2 plus 20 x"
  )
  @Iteration("3+x-5", "expression=3+x-5", "a11yStr=3 plus x minus 5")
  @Iteration("Z+A-Z", "expression=Z+A-Z", "a11yStr=Zed plus A minus Zed")
  @Iteration("6C - 5A -1", "expression=6C - 5A -1", "a11yStr=6 C minus 5 A minus 1")
  @Iteration("5*Z-w", "expression=5*Z-w", "a11yStr=5 times Zed minus w")
  @Iteration("L*S-3S+L", "expression=L*S-3S+L", "a11yStr=L times S minus 3 S plus L")
  @Iteration(
    "2*(2+6+3+4)",
    "expression=2*(2+6+3+4)",
    "a11yStr=2 times open parenthesis 2 plus 6 plus 3 plus 4 close parenthesis"
  )
  @Iteration("sqrt(64)", "expression=sqrt(64)", "a11yStr=square root of 64")
  @Iteration(
    "√(a+b)",
    "expression=√(a+b)",
    "a11yStr=start square root open parenthesis a plus b close parenthesis end square root"
  )
  @Iteration(
    "3 * 10^-5", "expression=3 * 10^-5", "a11yStr=3 times 10 raised to the power of negative 5"
  )
  @Iteration(
    "((x+2y) + 5*(a - 2b) + z)",
    "expression=((x+2y) + 5*(a - 2b) + z)",
    "a11yStr=open parenthesis open parenthesis x plus 2 y close parenthesis plus 5 times open" +
      " parenthesis a minus 2 b close parenthesis plus zed close parenthesis"
  )
  fun testConvertToString_eng_assortedExpressionsFromPrd_returnsCorrectlyComputedString() {
    // Some of the expressions include cases that would normally result in errors.
    val exp = parseAlgebraicExpression(expression, errorCheckingMode = REQUIRED_ONLY)

    assertThat(exp).forHumanReadable(ENGLISH).convertsWithFractionsToStringThat().isEqualTo(a11yStr)
  }

  @Test
  @Iteration(
    "3x^2 + 4y = 62",
    "expression=3x^2 + 4y = 62",
    "a11yStr=3 x raised to the power of 2 plus 4 y equals 62"
  )
  fun testConvertToString_eng_assortedEquationsFromPrd_returnsCorrectlyComputedString() {
    val eq = parseAlgebraicEquation(expression)

    assertThat(eq).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo(a11yStr)
  }

  @Test
  fun testConvertToString_eng_rationalConstant_returnsNull() {
    val exp = MathExpression.newBuilder().apply {
      constant = ONE_HALF
    }.build()

    // The conversion should fail since the expression includes a rational real (which aren't yet
    // supported).
    assertThat(exp).forHumanReadable(ENGLISH).doesNotConvertToString()
  }

  @Test
  fun testConvertToString_eng_invalidConstant_returnsNull() {
    val exp = MathExpression.newBuilder().apply {
      constant = Real.getDefaultInstance()
    }.build()

    // The conversion should fail since the expression includes an invalid real constant.
    assertThat(exp).forHumanReadable(ENGLISH).doesNotConvertToString()
  }

  @Test
  fun testConvertToString_eng_invalidBinaryOp_returnsNull() {
    val exp = MathExpression.newBuilder().apply {
      binaryOperation = MathBinaryOperation.getDefaultInstance()
    }.build()

    // The conversion should fail since the expression includes an invalid binary operation.
    assertThat(exp).forHumanReadable(ENGLISH).doesNotConvertToString()
  }

  @Test
  fun testConvertToString_eng_invalidUnaryOp_returnsNull() {
    val exp = MathExpression.newBuilder().apply {
      unaryOperation = MathUnaryOperation.getDefaultInstance()
    }.build()

    // The conversion should fail since the expression includes an invalid unary operation.
    assertThat(exp).forHumanReadable(ENGLISH).doesNotConvertToString()
  }

  @Test
  fun testConvertToString_eng_invalidFunctionType_returnsNull() {
    val exp = MathExpression.newBuilder().apply {
      functionCall = MathFunctionCall.getDefaultInstance()
    }.build()

    // The conversion should fail since the expression includes an invalid function call.
    assertThat(exp).forHumanReadable(ENGLISH).doesNotConvertToString()
  }

  @Test
  fun testConvertToString_eng_nestedDefaultExp_returnsNull() {
    val exp = MathExpression.newBuilder().apply {
      group = MathExpression.getDefaultInstance()
    }.build()

    // The conversion should fail since the expression includes an invalid nested expression.
    assertThat(exp).forHumanReadable(ENGLISH).doesNotConvertToString()
  }

  @Test
  fun testConvertToString_eng_nestedInvalidBinaryOp_returnsNull() {
    val exp = MathExpression.newBuilder().apply {
      group = MathExpression.newBuilder().apply {
        binaryOperation = MathBinaryOperation.getDefaultInstance()
      }.build()
    }.build()

    // The conversion should fail since the expression includes an invalid nested expression.
    assertThat(exp).forHumanReadable(ENGLISH).doesNotConvertToString()
  }

  @Test
  fun testConvertToString_eng_nestedInvalidUnaryOp_returnsNull() {
    val exp = MathExpression.newBuilder().apply {
      group = MathExpression.newBuilder().apply {
        unaryOperation = MathUnaryOperation.getDefaultInstance()
      }.build()
    }.build()

    // The conversion should fail since the expression includes an invalid nested expression.
    assertThat(exp).forHumanReadable(ENGLISH).doesNotConvertToString()
  }

  @Test
  fun testConvertToString_eng_nestedInvalidFunctionType_returnsNull() {
    val exp = MathExpression.newBuilder().apply {
      group = MathExpression.newBuilder().apply {
        functionCall = MathFunctionCall.getDefaultInstance()
      }.build()
    }.build()

    // The conversion should fail since the expression includes an invalid nested expression.
    assertThat(exp).forHumanReadable(ENGLISH).doesNotConvertToString()
  }

  @Test
  fun testConvertToString_eq_withLeftInvalidExp_returnsNull() {
    val validExp = MathExpression.newBuilder().apply {
      constant = ONE_HALF
    }.build()
    val invalidExp = MathExpression.getDefaultInstance()
    val eq = MathEquation.newBuilder().apply {
      leftSide = invalidExp
      rightSide = validExp
    }.build()

    // Both sides of the equation must be valid.
    assertThat(eq).forHumanReadable(ENGLISH).doesNotConvertToString()
  }

  @Test
  fun testConvertToString_eq_withRightInvalidExp_returnsNull() {
    val validExp = MathExpression.newBuilder().apply {
      constant = ONE_HALF
    }.build()
    val invalidExp = MathExpression.getDefaultInstance()
    val eq = MathEquation.newBuilder().apply {
      leftSide = validExp
      rightSide = invalidExp
    }.build()

    // Both sides of the equation must be valid.
    assertThat(eq).forHumanReadable(ENGLISH).doesNotConvertToString()
  }

  private fun MathExpressionSubject.forHumanReadable(
    language: OppiaLanguage
  ): HumanReadableStringChecker {
    return HumanReadableStringChecker(language) { divAsFraction ->
      util.convertToHumanReadableString(actual, language, divAsFraction)
    }
  }

  private fun MathEquationSubject.forHumanReadable(
    language: OppiaLanguage
  ): HumanReadableStringChecker {
    return HumanReadableStringChecker(language) { divAsFraction ->
      util.convertToHumanReadableString(actual, language, divAsFraction)
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private class HumanReadableStringChecker(
    private val language: OppiaLanguage,
    private val maybeConvertToHumanReadableString: (Boolean) -> String?
  ) {
    fun convertsToStringThat(): StringSubject =
      assertThat(convertToHumanReadableString(language, /* divAsFraction= */ false))

    fun convertsWithFractionsToStringThat(): StringSubject =
      assertThat(convertToHumanReadableString(language, /* divAsFraction= */ true))

    fun doesNotConvertToString() {
      assertWithMessage("Expected to not convert to: $language")
        .that(maybeConvertToHumanReadableString(/* divAsFraction= */ false))
        .isNull()
    }

    private fun convertToHumanReadableString(
      language: OppiaLanguage,
      divAsFraction: Boolean
    ): String {
      val readableString = maybeConvertToHumanReadableString(divAsFraction)
      assertWithMessage("Expected to convert to: $language").that(readableString).isNotNull()
      return checkNotNull(readableString) // Verified in the above assertion check.
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class, TestDispatcherModule::class, ApplicationModule::class,
      PlatformParameterModule::class, LoggerModule::class, ContinueModule::class,
      FractionInputModule::class, ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      ExpirationMetaDataRetrieverTestModule::class,
      ViewBindingShimModule::class, RatioInputModule::class, NetworkConfigProdModule::class,
      ApplicationStartupListenerModule::class, HintsAndSolutionConfigModule::class,
      LogReportWorkerModule::class, WorkManagerConfigurationModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, HintsAndSolutionProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleTestModule::class, ActivityRecreatorTestModule::class,
      ActivityIntentFactoriesModule::class, PlatformParameterSingletonModule::class,
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
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: MathExpressionAccessibilityUtilTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerMathExpressionAccessibilityUtilTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: MathExpressionAccessibilityUtilTest) {
      component.inject(test)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }

  private companion object {
    private fun parseAlgebraicExpression(
      expression: String,
      allowedVariables: List<String> = listOf("x", "y", "z"),
      errorCheckingMode: ErrorCheckingMode = ALL_ERRORS
    ): MathExpression {
      return MathExpressionParser.parseAlgebraicExpression(
        expression, allowedVariables, errorCheckingMode
      ).getExpectedSuccess()
    }

    private fun parseAlgebraicEquation(
      expression: String,
    ): MathEquation {
      return MathExpressionParser.parseAlgebraicEquation(
        expression,
        allowedVariables = listOf("x", "y", "z"),
        errorCheckingMode = ALL_ERRORS
      ).getExpectedSuccess()
    }

    private inline fun <reified T> MathParsingResult<T>.getExpectedSuccess(): T {
      assertThat(this).isInstanceOf(MathParsingResult.Success::class.java)
      return (this as MathParsingResult.Success<T>).result
    }
  }
}
