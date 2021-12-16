package org.oppia.android.app.utility.math

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.StringSubject
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.model.MathEquation
import org.oppia.android.app.model.MathExpression
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OppiaLanguage.ARABIC
import org.oppia.android.app.model.OppiaLanguage.BRAZILIAN_PORTUGUESE
import org.oppia.android.app.model.OppiaLanguage.ENGLISH
import org.oppia.android.app.model.OppiaLanguage.HINDI
import org.oppia.android.app.model.OppiaLanguage.HINGLISH
import org.oppia.android.app.model.OppiaLanguage.LANGUAGE_UNSPECIFIED
import org.oppia.android.app.model.OppiaLanguage.PORTUGUESE
import org.oppia.android.app.model.OppiaLanguage.UNRECOGNIZED
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
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.TestLogReportingModule
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
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.math.MathExpressionParser
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode
import org.oppia.android.util.math.MathExpressionParser.Companion.MathParsingResult
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [MathExpressionAccessibilityUtil]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = MathExpressionAccessibilityUtilTest.TestApplication::class)
class MathExpressionAccessibilityUtilTest {
  @Inject lateinit var util: MathExpressionAccessibilityUtil

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testHumanReadableString() {
    // TODO: split up & move to separate test suites. Finish test cases (if anymore are needed).

    val exp1 = parseNumericExpressionSuccessfullyWithAllErrors("1")
    assertThat(exp1).forHumanReadable(ARABIC).doesNotConvertToString()

    assertThat(exp1).forHumanReadable(HINDI).doesNotConvertToString()

    assertThat(exp1).forHumanReadable(HINGLISH).doesNotConvertToString()

    assertThat(exp1).forHumanReadable(PORTUGUESE).doesNotConvertToString()

    assertThat(exp1).forHumanReadable(BRAZILIAN_PORTUGUESE).doesNotConvertToString()

    assertThat(exp1).forHumanReadable(LANGUAGE_UNSPECIFIED).doesNotConvertToString()

    assertThat(exp1).forHumanReadable(UNRECOGNIZED).doesNotConvertToString()

    val exp2 = parseAlgebraicExpressionSuccessfullyWithAllErrors("x")
    assertThat(exp2).forHumanReadable(ARABIC).doesNotConvertToString()

    assertThat(exp2).forHumanReadable(HINDI).doesNotConvertToString()

    assertThat(exp2).forHumanReadable(HINGLISH).doesNotConvertToString()

    assertThat(exp2).forHumanReadable(PORTUGUESE).doesNotConvertToString()

    assertThat(exp2).forHumanReadable(BRAZILIAN_PORTUGUESE).doesNotConvertToString()

    assertThat(exp2).forHumanReadable(LANGUAGE_UNSPECIFIED).doesNotConvertToString()

    assertThat(exp2).forHumanReadable(UNRECOGNIZED).doesNotConvertToString()

    val eq1 = parseAlgebraicEquationSuccessfullyWithAllErrors("x=1")
    assertThat(eq1).forHumanReadable(ARABIC).doesNotConvertToString()

    assertThat(eq1).forHumanReadable(HINDI).doesNotConvertToString()

    assertThat(eq1).forHumanReadable(HINGLISH).doesNotConvertToString()

    assertThat(eq1).forHumanReadable(PORTUGUESE).doesNotConvertToString()

    assertThat(eq1).forHumanReadable(BRAZILIAN_PORTUGUESE).doesNotConvertToString()

    assertThat(eq1).forHumanReadable(LANGUAGE_UNSPECIFIED).doesNotConvertToString()

    assertThat(eq1).forHumanReadable(UNRECOGNIZED).doesNotConvertToString()

    // specific cases (from rules & other cases):
    val exp3 = parseNumericExpressionSuccessfullyWithAllErrors("1")
    assertThat(exp3).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1")
    assertThat(exp3).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1")

    val exp49 = parseNumericExpressionSuccessfullyWithAllErrors("-1")
    assertThat(exp49).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("negative 1")

    val exp50 = parseNumericExpressionSuccessfullyWithAllErrors("+1")
    assertThat(exp50).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("positive 1")

    val exp4 = parseNumericExpressionSuccessfullyWithoutOptionalErrors("((1))")
    assertThat(exp4).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1")

    val exp5 = parseNumericExpressionSuccessfullyWithAllErrors("1+2")
    assertThat(exp5).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1 plus 2")

    val exp6 = parseNumericExpressionSuccessfullyWithAllErrors("1-2")
    assertThat(exp6).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1 minus 2")

    val exp7 = parseNumericExpressionSuccessfullyWithAllErrors("1*2")
    assertThat(exp7).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1 times 2")

    val exp8 = parseNumericExpressionSuccessfullyWithAllErrors("1/2")
    assertThat(exp8).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1 divided by 2")

    val exp9 = parseNumericExpressionSuccessfullyWithAllErrors("1+(1-2)")
    assertThat(exp9)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("1 plus open parenthesis 1 minus 2 close parenthesis")

    val exp10 = parseNumericExpressionSuccessfullyWithAllErrors("2^3")
    assertThat(exp10)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("2 raised to the power of 3")

    val exp11 = parseNumericExpressionSuccessfullyWithAllErrors("2^(1+2)")
    assertThat(exp11)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("2 raised to the power of open parenthesis 1 plus 2 close parenthesis")

    val exp12 = parseNumericExpressionSuccessfullyWithAllErrors("100000*2")
    assertThat(exp12).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("100,000 times 2")

    val exp13 = parseNumericExpressionSuccessfullyWithAllErrors("sqrt(2)")
    assertThat(exp13).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("square root of 2")

    val exp14 = parseNumericExpressionSuccessfullyWithAllErrors("√2")
    assertThat(exp14).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("square root of 2")

    val exp15 = parseNumericExpressionSuccessfullyWithAllErrors("sqrt(1+2)")
    assertThat(exp15)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("start square root 1 plus 2 end square root")

    val singularOrdinalNames = mapOf(
      1 to "oneth",
      2 to "half",
      3 to "third",
      4 to "fourth",
      5 to "fifth",
      6 to "sixth",
      7 to "seventh",
      8 to "eighth",
      9 to "ninth",
      10 to "tenth",
    )
    val pluralOrdinalNames = mapOf(
      1 to "oneths",
      2 to "halves",
      3 to "thirds",
      4 to "fourths",
      5 to "fifths",
      6 to "sixths",
      7 to "sevenths",
      8 to "eighths",
      9 to "ninths",
      10 to "tenths",
    )
    for (denominatorToCheck in 1..10) {
      for (numeratorToCheck in 0..denominatorToCheck) {
        val exp16 =
          parseNumericExpressionSuccessfullyWithAllErrors("$numeratorToCheck/$denominatorToCheck")

        val ordinalName =
          if (numeratorToCheck == 1) {
            singularOrdinalNames.getValue(denominatorToCheck)
          } else pluralOrdinalNames.getValue(denominatorToCheck)
        assertThat(exp16)
          .forHumanReadable(ENGLISH)
          .convertsWithFractionsToStringThat()
          .isEqualTo("$numeratorToCheck $ordinalName")
      }
    }

    val exp17 = parseNumericExpressionSuccessfullyWithAllErrors("-1/3")
    assertThat(exp17)
      .forHumanReadable(ENGLISH)
      .convertsWithFractionsToStringThat()
      .isEqualTo("negative 1 third")

    val exp18 = parseNumericExpressionSuccessfullyWithAllErrors("-2/3")
    assertThat(exp18)
      .forHumanReadable(ENGLISH)
      .convertsWithFractionsToStringThat()
      .isEqualTo("negative 2 thirds")

    val exp19 = parseNumericExpressionSuccessfullyWithAllErrors("10/11")
    assertThat(exp19)
      .forHumanReadable(ENGLISH)
      .convertsWithFractionsToStringThat()
      .isEqualTo("10 over 11")

    val exp20 = parseNumericExpressionSuccessfullyWithAllErrors("121/7986")
    assertThat(exp20)
      .forHumanReadable(ENGLISH)
      .convertsWithFractionsToStringThat()
      .isEqualTo("121 over 7,986")

    val exp21 = parseNumericExpressionSuccessfullyWithAllErrors("8/7")
    assertThat(exp21)
      .forHumanReadable(ENGLISH)
      .convertsWithFractionsToStringThat()
      .isEqualTo("8 over 7")

    val exp22 = parseNumericExpressionSuccessfullyWithAllErrors("-10/-30")
    assertThat(exp22)
      .forHumanReadable(ENGLISH)
      .convertsWithFractionsToStringThat()
      .isEqualTo("negative the fraction with numerator 10 and denominator negative 30")

    val exp23 = parseAlgebraicExpressionSuccessfullyWithAllErrors("1")
    assertThat(exp23).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1")

    val exp24 = parseAlgebraicExpressionSuccessfullyWithoutOptionalErrors("((1))")
    assertThat(exp24).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1")

    val exp25 = parseAlgebraicExpressionSuccessfullyWithAllErrors("x")
    assertThat(exp25).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("x")

    val exp26 = parseAlgebraicExpressionSuccessfullyWithoutOptionalErrors("((x))")
    assertThat(exp26).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("x")

    val exp51 = parseAlgebraicExpressionSuccessfullyWithAllErrors("-x")
    assertThat(exp51).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("negative x")

    val exp52 = parseAlgebraicExpressionSuccessfullyWithAllErrors("+x")
    assertThat(exp52).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("positive x")

    val exp27 = parseAlgebraicExpressionSuccessfullyWithAllErrors("1+x")
    assertThat(exp27).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1 plus x")

    val exp28 = parseAlgebraicExpressionSuccessfullyWithAllErrors("1-x")
    assertThat(exp28).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1 minus x")

    val exp29 = parseAlgebraicExpressionSuccessfullyWithAllErrors("1*x")
    assertThat(exp29).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1 times x")

    val exp30 = parseAlgebraicExpressionSuccessfullyWithAllErrors("1/x")
    assertThat(exp30).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1 divided by x")

    val exp31 = parseAlgebraicExpressionSuccessfullyWithAllErrors("1/x")
    assertThat(exp31)
      .forHumanReadable(ENGLISH)
      .convertsWithFractionsToStringThat()
      .isEqualTo("the fraction with numerator 1 and denominator x")

    val exp32 = parseAlgebraicExpressionSuccessfullyWithAllErrors("1+(1-x)")
    assertThat(exp32)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("1 plus open parenthesis 1 minus x close parenthesis")

    val exp33 = parseAlgebraicExpressionSuccessfullyWithAllErrors("2x")
    assertThat(exp33).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("2 x")

    val exp34 = parseAlgebraicExpressionSuccessfullyWithAllErrors("xy")
    assertThat(exp34).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("x times y")

    val exp35 = parseAlgebraicExpressionSuccessfullyWithAllErrors("z")
    assertThat(exp35).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("zed")

    val exp36 = parseAlgebraicExpressionSuccessfullyWithAllErrors("2xz")
    assertThat(exp36).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("2 x times zed")

    val exp37 = parseAlgebraicExpressionSuccessfullyWithAllErrors("x^2")
    assertThat(exp37)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("x raised to the power of 2")

    val exp38 = parseAlgebraicExpressionSuccessfullyWithoutOptionalErrors("x^(1+x)")
    assertThat(exp38)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("x raised to the power of open parenthesis 1 plus x close parenthesis")

    val exp39 = parseAlgebraicExpressionSuccessfullyWithAllErrors("100000*2")
    assertThat(exp39).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("100,000 times 2")

    val exp40 = parseAlgebraicExpressionSuccessfullyWithAllErrors("sqrt(2)")
    assertThat(exp40).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("square root of 2")

    val exp41 = parseAlgebraicExpressionSuccessfullyWithAllErrors("sqrt(x)")
    assertThat(exp41).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("square root of x")

    val exp42 = parseAlgebraicExpressionSuccessfullyWithAllErrors("√2")
    assertThat(exp42).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("square root of 2")

    val exp43 = parseAlgebraicExpressionSuccessfullyWithAllErrors("√x")
    assertThat(exp43).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("square root of x")

    val exp44 = parseAlgebraicExpressionSuccessfullyWithAllErrors("sqrt(1+2)")
    assertThat(exp44)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("start square root 1 plus 2 end square root")

    val exp45 = parseAlgebraicExpressionSuccessfullyWithAllErrors("sqrt(1+x)")
    assertThat(exp45)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("start square root 1 plus x end square root")

    val exp46 = parseAlgebraicExpressionSuccessfullyWithAllErrors("√(1+x)")
    assertThat(exp46)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("start square root open parenthesis 1 plus x close parenthesis end square root")

    for (denominatorToCheck in 1..10) {
      for (numeratorToCheck in 0..denominatorToCheck) {
        val exp16 =
          parseAlgebraicExpressionSuccessfullyWithAllErrors("$numeratorToCheck/$denominatorToCheck")

        val ordinalName =
          if (numeratorToCheck == 1) {
            singularOrdinalNames.getValue(denominatorToCheck)
          } else pluralOrdinalNames.getValue(denominatorToCheck)
        assertThat(exp16)
          .forHumanReadable(ENGLISH)
          .convertsWithFractionsToStringThat()
          .isEqualTo("$numeratorToCheck $ordinalName")
      }
    }

    val exp47 = parseAlgebraicExpressionSuccessfullyWithAllErrors("1")
    assertThat(exp47).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1")

    val exp48 = parseAlgebraicExpressionSuccessfullyWithAllErrors("x(5-y)")
    assertThat(exp48)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("x times open parenthesis 5 minus y close parenthesis")

    val eq2 = parseAlgebraicEquationSuccessfullyWithAllErrors("x=1/y")
    assertThat(eq2)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("x equals 1 divided by y")

    val eq3 = parseAlgebraicEquationSuccessfullyWithAllErrors("x=1/2")
    assertThat(eq3)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("x equals 1 divided by 2")

    val eq4 = parseAlgebraicEquationSuccessfullyWithAllErrors("x=1/y")
    assertThat(eq4)
      .forHumanReadable(ENGLISH)
      .convertsWithFractionsToStringThat()
      .isEqualTo("x equals the fraction with numerator 1 and denominator y")

    val eq5 = parseAlgebraicEquationSuccessfullyWithAllErrors("x=1/2")
    assertThat(eq5)
      .forHumanReadable(ENGLISH)
      .convertsWithFractionsToStringThat()
      .isEqualTo("x equals 1 half")

    // Tests from examples in the PRD
    val eq6 = parseAlgebraicEquationSuccessfullyWithAllErrors("3x^2+4y=62")
    assertThat(eq6)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("3 x raised to the power of 2 plus 4 y equals 62")

    val exp53 = parseAlgebraicExpressionSuccessfullyWithAllErrors("(x+6)/(x-4)")
    assertThat(exp53)
      .forHumanReadable(ENGLISH)
      .convertsWithFractionsToStringThat()
      .isEqualTo(
        "the fraction with numerator open parenthesis x plus 6 close parenthesis and denominator" +
          " open parenthesis x minus 4 close parenthesis"
      )

    val exp54 = parseAlgebraicExpressionSuccessfullyWithoutOptionalErrors("4*(x)^(2)+20x")
    assertThat(exp54)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("4 times x raised to the power of 2 plus 20 x")

    val exp55 = parseAlgebraicExpressionSuccessfullyWithAllErrors("3+x-5")
    assertThat(exp55).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("3 plus x minus 5")

    val exp56 =
      parseAlgebraicExpressionSuccessfullyWithAllErrors(
        "Z+A-Z", allowedVariables = listOf("A", "Z")
      )
    assertThat(exp56).forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("Zed plus A minus Zed")

    val exp57 =
      parseAlgebraicExpressionSuccessfullyWithAllErrors(
        "6C-5A-1", allowedVariables = listOf("A", "C")
      )
    assertThat(exp57)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("6 C minus 5 A minus 1")

    val exp58 =
      parseAlgebraicExpressionSuccessfullyWithAllErrors(
        "5*Z-w", allowedVariables = listOf("Z", "w")
      )
    assertThat(exp58)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("5 times Zed minus w")

    val exp59 =
      parseAlgebraicExpressionSuccessfullyWithAllErrors(
        "L*S-3S+L", allowedVariables = listOf("L", "S")
      )
    assertThat(exp59)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("L times S minus 3 S plus L")

    val exp60 = parseAlgebraicExpressionSuccessfullyWithAllErrors("2*(2+6+3+4)")
    assertThat(exp60)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("2 times open parenthesis 2 plus 6 plus 3 plus 4 close parenthesis")

    val exp61 = parseAlgebraicExpressionSuccessfullyWithAllErrors("sqrt(64)")
    assertThat(exp61)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("square root of 64")

    val exp62 =
      parseAlgebraicExpressionSuccessfullyWithAllErrors(
        "√(a+b)", allowedVariables = listOf("a", "b")
      )
    assertThat(exp62)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("start square root open parenthesis a plus b close parenthesis end square root")

    val exp63 = parseAlgebraicExpressionSuccessfullyWithAllErrors("3*10^-5")
    assertThat(exp63)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("3 times 10 raised to the power of negative 5")

    val exp64 =
      parseAlgebraicExpressionSuccessfullyWithoutOptionalErrors(
        "((x+2y)+5*(a-2b)+z)", allowedVariables = listOf("x", "y", "a", "b", "z")
      )
    assertThat(exp64)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo(
        "open parenthesis open parenthesis x plus 2 y close parenthesis plus 5 times open" +
          " parenthesis a minus 2 b close parenthesis plus zed close parenthesis"
      )
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
  @Module
  class TestModule {
    // TODO(#59): Either isolate these to their own shared test module, or use the real logging
    // module in tests to avoid needing to specify these settings for tests.
    @EnableConsoleLog
    @Provides
    fun provideEnableConsoleLog(): Boolean = true

    @EnableFileLog
    @Provides
    fun provideEnableFileLog(): Boolean = false

    @GlobalLogLevel
    @Provides
    fun provideGlobalLogLevel(): LogLevel = LogLevel.VERBOSE
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      TestLogReportingModule::class, TestDispatcherModule::class, ApplicationModule::class,
      ApplicationStartupListenerModule::class, WorkManagerConfigurationModule::class,
      ImageParsingModule::class, AccessibilityTestModule::class, PracticeTabModule::class,
      GcsResourceModule::class, NetworkConnectionUtilDebugModule::class, LogStorageModule::class,
      NetworkModule::class, PlatformParameterModule::class, HintsAndSolutionProdModule::class,
      CachingTestModule::class, InteractionsModule::class, ExplorationStorageModule::class,
      QuestionModule::class, NetworkConfigProdModule::class, ContinueModule::class,
      FractionInputModule::class, ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, RatioInputModule::class,
      HintsAndSolutionConfigModule::class, ExpirationMetaDataRetrieverModule::class,
      GlideImageLoaderModule::class, PrimeTopicAssetsControllerModule::class,
      HtmlParserEntityTypeModule::class, NetworkConnectionDebugUtilModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class, AssetModule::class,
      LocaleProdModule::class, ActivityRecreatorTestModule::class,
      PlatformParameterSingletonModule::class, NumericExpressionInputModule::class,
      AlgebraicExpressionInputModule::class, MathEquationInputModule::class
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
    private fun parseNumericExpressionSuccessfullyWithAllErrors(
      expression: String
    ): MathExpression {
      val result = parseNumericExpressionInternal(expression, ErrorCheckingMode.ALL_ERRORS)
      return (result as MathParsingResult.Success<MathExpression>).result
    }

    private fun parseNumericExpressionSuccessfullyWithoutOptionalErrors(
      expression: String
    ): MathExpression {
      val result =
        parseNumericExpressionInternal(
          expression, ErrorCheckingMode.REQUIRED_ONLY
        )
      return (result as MathParsingResult.Success<MathExpression>).result
    }

    private fun parseNumericExpressionInternal(
      expression: String,
      errorCheckingMode: ErrorCheckingMode
    ): MathParsingResult<MathExpression> {
      return MathExpressionParser.parseNumericExpression(expression, errorCheckingMode)
    }

    private fun parseAlgebraicExpressionSuccessfullyWithAllErrors(
      expression: String,
      allowedVariables: List<String> = listOf("x", "y", "z")
    ): MathExpression {
      val result =
        parseAlgebraicExpressionInternal(expression, ErrorCheckingMode.ALL_ERRORS, allowedVariables)
      return (result as MathParsingResult.Success<MathExpression>).result
    }

    private fun parseAlgebraicExpressionSuccessfullyWithoutOptionalErrors(
      expression: String,
      allowedVariables: List<String> = listOf("x", "y", "z")
    ): MathExpression {
      val result =
        parseAlgebraicExpressionInternal(
          expression, ErrorCheckingMode.REQUIRED_ONLY, allowedVariables
        )
      return (result as MathParsingResult.Success<MathExpression>).result
    }

    private fun parseAlgebraicExpressionInternal(
      expression: String,
      errorCheckingMode: ErrorCheckingMode,
      allowedVariables: List<String> = listOf("x", "y", "z")
    ): MathParsingResult<MathExpression> {
      return MathExpressionParser.parseAlgebraicExpression(
        expression, allowedVariables, errorCheckingMode
      )
    }

    private fun parseAlgebraicEquationSuccessfullyWithAllErrors(
      expression: String,
      allowedVariables: List<String> = listOf("x", "y", "z")
    ): MathEquation {
      val result =
        MathExpressionParser.parseAlgebraicEquation(
          expression, allowedVariables,
          ErrorCheckingMode.ALL_ERRORS
        )
      return (result as MathParsingResult.Success<MathEquation>).result
    }
  }
}
