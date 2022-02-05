package org.oppia.android.app.utility.math

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.StringSubject
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import dagger.BindsInstance
import dagger.Component
import javax.inject.Inject
import javax.inject.Singleton
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
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
import org.oppia.android.testing.math.MathEquationSubject
import org.oppia.android.testing.math.MathEquationSubject.Companion.assertThat
import org.oppia.android.testing.math.MathExpressionSubject
import org.oppia.android.testing.math.MathExpressionSubject.Companion.assertThat
import org.oppia.android.util.math.MathExpressionParser
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode
import org.oppia.android.util.math.MathExpressionParser.Companion.MathParsingResult
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/** Tests for [MathExpressionAccessibilityUtil]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = MathExpressionAccessibilityUtilTest.TestApplication::class)
class MathExpressionAccessibilityUtilTest {
  @Inject
  lateinit var util: MathExpressionAccessibilityUtil

  // TODO: finish tests

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun test1() {
    // TODO: do something with this test.
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("1")
    assertThat(exp).forHumanReadable(ARABIC).doesNotConvertToString()
  }

  @Test
  fun test2() {
    // TODO: do something with this test.
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("1")
    assertThat(exp).forHumanReadable(HINDI).doesNotConvertToString()
  }

  @Test
  fun test3() {
    // TODO: do something with this test.
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("1")
    assertThat(exp).forHumanReadable(HINGLISH).doesNotConvertToString()
  }

  @Test
  fun test4() {
    // TODO: do something with this test.
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("1")
    assertThat(exp).forHumanReadable(PORTUGUESE).doesNotConvertToString()
  }

  @Test
  fun test5() {
    // TODO: do something with this test.
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("1")
    assertThat(exp).forHumanReadable(BRAZILIAN_PORTUGUESE).doesNotConvertToString()
  }

  @Test
  fun test6() {
    // TODO: do something with this test.
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("1")
    assertThat(exp).forHumanReadable(LANGUAGE_UNSPECIFIED).doesNotConvertToString()
  }

  @Test
  fun test7() {
    // TODO: do something with this test.
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("1")
    assertThat(exp).forHumanReadable(UNRECOGNIZED).doesNotConvertToString()
  }

  @Test
  fun test8() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("x")
    assertThat(exp).forHumanReadable(ARABIC).doesNotConvertToString()
  }

  @Test
  fun test9() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("x")
    assertThat(exp).forHumanReadable(HINDI).doesNotConvertToString()
  }

  @Test
  fun test10() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("x")
    assertThat(exp).forHumanReadable(HINGLISH).doesNotConvertToString()
  }

  @Test
  fun test11() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("x")
    assertThat(exp).forHumanReadable(PORTUGUESE).doesNotConvertToString()
  }

  @Test
  fun test12() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("x")
    assertThat(exp).forHumanReadable(BRAZILIAN_PORTUGUESE).doesNotConvertToString()
  }

  @Test
  fun test13() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("x")
    assertThat(exp).forHumanReadable(LANGUAGE_UNSPECIFIED).doesNotConvertToString()
  }

  @Test
  fun test14() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("x")
    assertThat(exp).forHumanReadable(UNRECOGNIZED).doesNotConvertToString()
  }

  @Test
  fun test15() {
    // TODO: do something with this test.
    val eq = parseAlgebraicEquationSuccessfullyWithAllErrors("x=1")
    assertThat(eq).forHumanReadable(ARABIC).doesNotConvertToString()
  }

  @Test
  fun test16() {
    // TODO: do something with this test.
    val eq = parseAlgebraicEquationSuccessfullyWithAllErrors("x=1")
    assertThat(eq).forHumanReadable(HINDI).doesNotConvertToString()
  }

  @Test
  fun test17() {
    // TODO: do something with this test.
    val eq = parseAlgebraicEquationSuccessfullyWithAllErrors("x=1")
    assertThat(eq).forHumanReadable(HINGLISH).doesNotConvertToString()
  }

  @Test
  fun test18() {
    // TODO: do something with this test.
    val eq = parseAlgebraicEquationSuccessfullyWithAllErrors("x=1")
    assertThat(eq).forHumanReadable(PORTUGUESE).doesNotConvertToString()
  }

  @Test
  fun test19() {
    // TODO: do something with this test.
    val eq = parseAlgebraicEquationSuccessfullyWithAllErrors("x=1")
    assertThat(eq).forHumanReadable(BRAZILIAN_PORTUGUESE).doesNotConvertToString()
  }

  @Test
  fun test20() {
    // TODO: do something with this test.
    val eq = parseAlgebraicEquationSuccessfullyWithAllErrors("x=1")
    assertThat(eq).forHumanReadable(LANGUAGE_UNSPECIFIED).doesNotConvertToString()
  }

  @Test
  fun test21() {
    // TODO: do something with this test.
    val eq = parseAlgebraicEquationSuccessfullyWithAllErrors("x=1")
    assertThat(eq).forHumanReadable(UNRECOGNIZED).doesNotConvertToString()
  }

  @Test
  fun test22() {
    // TODO: do something with this test.
    // specific cases (from rules & other cases):
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("1")
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1")
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1")
  }

  @Test
  fun test23() {
    // TODO: do something with this test.
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("-1")
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("negative 1")
  }

  @Test
  fun test24() {
    // TODO: do something with this test.
    val exp = parseNumericExpressionSuccessfullyWithoutOptionalErrors("+1")
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("positive 1")
  }

  @Test
  fun test25() {
    // TODO: do something with this test.
    val exp = parseNumericExpressionSuccessfullyWithoutOptionalErrors("((1))")
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1")
  }

  @Test
  fun test26() {
    // TODO: do something with this test.
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("1+2")
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1 plus 2")
  }

  @Test
  fun test27() {
    // TODO: do something with this test.
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("1-2")
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1 minus 2")
  }

  @Test
  fun test28() {
    // TODO: do something with this test.
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("1*2")
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1 times 2")
  }

  @Test
  fun test29() {
    // TODO: do something with this test.
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("1/2")
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1 divided by 2")
  }

  @Test
  fun test30() {
    // TODO: do something with this test.
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("1+(1-2)")
    assertThat(exp)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("1 plus open parenthesis 1 minus 2 close parenthesis")
  }

  @Test
  fun test31() {
    // TODO: do something with this test.
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("2^3")
    assertThat(exp)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("2 raised to the power of 3")
  }

  @Test
  fun test32() {
    // TODO: do something with this test.
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("2^(1+2)")
    assertThat(exp)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("2 raised to the power of open parenthesis 1 plus 2 close parenthesis")
  }

  @Test
  fun test33() {
    // TODO: do something with this test.
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("100000*2")
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("100,000 times 2")
  }

  @Test
  fun test34() {
    // TODO: do something with this test.
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("sqrt(2)")
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("square root of 2")
  }

  @Test
  fun test35() {
    // TODO: do something with this test.
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("√2")
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("square root of 2")
  }

  @Test
  fun test36() {
    // TODO: do something with this test.
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("sqrt(1+2)")
    assertThat(exp)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("start square root 1 plus 2 end square root")
  }

  @Test
  fun test37() {
    // TODO: do something with this test.
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
        val exp =
          parseNumericExpressionSuccessfullyWithAllErrors("$numeratorToCheck/$denominatorToCheck")

        val ordinalName =
          if (numeratorToCheck == 1) {
            singularOrdinalNames.getValue(denominatorToCheck)
          } else pluralOrdinalNames.getValue(denominatorToCheck)
        assertThat(exp)
          .forHumanReadable(ENGLISH)
          .convertsWithFractionsToStringThat()
          .isEqualTo("$numeratorToCheck $ordinalName")
      }
    }
  }

  @Test
  fun test38() {
    // TODO: do something with this test.
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("-1/3")
    assertThat(exp)
      .forHumanReadable(ENGLISH)
      .convertsWithFractionsToStringThat()
      .isEqualTo("negative 1 third")
  }

  @Test
  fun test39() {
    // TODO: do something with this test.
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("-2/3")
    assertThat(exp)
      .forHumanReadable(ENGLISH)
      .convertsWithFractionsToStringThat()
      .isEqualTo("negative 2 thirds")
  }

  @Test
  fun test40() {
    // TODO: do something with this test.
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("10/11")
    assertThat(exp)
      .forHumanReadable(ENGLISH)
      .convertsWithFractionsToStringThat()
      .isEqualTo("10 over 11")
  }

  @Test
  fun test41() {
    // TODO: do something with this test.
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("121/7986")
    assertThat(exp)
      .forHumanReadable(ENGLISH)
      .convertsWithFractionsToStringThat()
      .isEqualTo("121 over 7,986")
  }

  @Test
  fun test42() {
    // TODO: do something with this test.
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("8/7")
    assertThat(exp)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("8 over 7")
  }

  @Test
  fun test43() {
    // TODO: do something with this test.
    val exp = parseNumericExpressionSuccessfullyWithAllErrors("-10/-30")
    assertThat(exp)
      .forHumanReadable(ENGLISH)
      .convertsWithFractionsToStringThat()
      .isEqualTo("negative the fraction with numerator 10 and denominator negative 30")
  }

  @Test
  fun test44() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("1")
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1")
  }

  @Test
  fun test45() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithoutOptionalErrors("((1))")
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1")
  }

  @Test
  fun test46() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("x")
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("x")
  }

  @Test
  fun test47() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithoutOptionalErrors("((x))")
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("x")
  }

  @Test
  fun test48() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("-x")
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("negative x")
  }

  @Test
  fun test49() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("+x")
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("positive x")
  }

  @Test
  fun test50() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("1+x")
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1 plus x")
  }

  @Test
  fun test51() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("1-x")
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1 minus x")
  }

  @Test
  fun test52() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("1*x")
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1 times x")
  }

  @Test
  fun test53() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("1/x")
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1 divided by x")
  }

  @Test
  fun test54() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("1/x")
    assertThat(exp)
      .forHumanReadable(ENGLISH)
      .convertsWithFractionsToStringThat()
      .isEqualTo("the fraction with numerator 1 and denominator x")
  }

  @Test
  fun test55() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("1+(1-x)")
    assertThat(exp)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("1 plus open parenthesis 1 minus x close parenthesis")
  }

  @Test
  fun test56() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("2x")
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("2 x")
  }

  @Test
  fun test57() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("xy")
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("x times y")
  }

  @Test
  fun test58() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("z")
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("zed")
  }

  @Test
  fun test59() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("2xz")
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("2 x times zed")
  }

  @Test
  fun test60() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("x^2")
    assertThat(exp)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("x raised to the power of 2")
  }

  @Test
  fun test61() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithoutOptionalErrors("x^(1+x)")
    assertThat(exp)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("x raised to the power of open parenthesis 1 plus x close parenthesis")
  }

  @Test
  fun test62() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("100000*2")
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("100,000 times 2")
  }

  @Test
  fun test63() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("sqrt(2)")
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("square root of 2")
  }

  @Test
  fun test64() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("sqrt(x)")
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("square root of x")
  }

  @Test
  fun test65() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("√2")
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("square root of 2")
  }

  @Test
  fun test66() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("√x")
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("square root of x")
  }

  @Test
  fun test67() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("sqrt(1+2)")
    assertThat(exp)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("start square root 1 plus 2 end square root")
  }

  @Test
  fun test68() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("sqrt(1+x)")
    assertThat(exp)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("start square root 1 plus x end square root")
  }

  @Test
  fun test69() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("√(1+x)")
    assertThat(exp)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("start square root open parenthesis 1 plus x close parenthesis end square root")
  }

  @Test
  fun test70() {
    // TODO: do something with this test.
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
        val exp =
          parseAlgebraicExpressionSuccessfullyWithAllErrors("$numeratorToCheck/$denominatorToCheck")

        val ordinalName =
          if (numeratorToCheck == 1) {
            singularOrdinalNames.getValue(denominatorToCheck)
          } else pluralOrdinalNames.getValue(denominatorToCheck)
        assertThat(exp)
          .forHumanReadable(ENGLISH)
          .convertsWithFractionsToStringThat()
          .isEqualTo("$numeratorToCheck $ordinalName")
      }
    }
  }

  @Test
  fun test71() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("1")
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("1")
  }

  @Test
  fun test72() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("x(5-y)")
    assertThat(exp)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("x times open parenthesis 5 minus y close parenthesis")
  }

  @Test
  fun test73() {
    // TODO: do something with this test.
    val eq = parseAlgebraicEquationSuccessfullyWithAllErrors("x=1/y")
    assertThat(eq)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("x equals 1 divided by y")
  }

  @Test
  fun test74() {
    // TODO: do something with this test.
    val eq = parseAlgebraicEquationSuccessfullyWithAllErrors("x=1/2")
    assertThat(eq)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("x equals 1 divided by 2")
  }

  @Test
  fun test75() {
    // TODO: do something with this test.
    val eq = parseAlgebraicEquationSuccessfullyWithAllErrors("x=1/y")
    assertThat(eq)
      .forHumanReadable(ENGLISH)
      .convertsWithFractionsToStringThat()
      .isEqualTo("x equals the fraction with numerator 1 and denominator y")
  }

  @Test
  fun test76() {
    // TODO: do something with this test.
    val eq = parseAlgebraicEquationSuccessfullyWithAllErrors("x=1/2")
    assertThat(eq)
      .forHumanReadable(ENGLISH)
      .convertsWithFractionsToStringThat()
      .isEqualTo("x equals 1 half")
  }

  @Test
  fun test77() {
    // TODO: do something with this test.
    // Tests from examples in the PRD
    val eq = parseAlgebraicEquationSuccessfullyWithAllErrors("3x^2+4y=62")
    assertThat(eq)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("3 x raised to the power of 2 plus 4 y equals 62")
  }

  @Test
  fun test78() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("(x+6)/(x-4)")
    assertThat(exp)
      .forHumanReadable(ENGLISH)
      .convertsWithFractionsToStringThat()
      .isEqualTo(
        "the fraction with numerator open parenthesis x plus 6 close parenthesis and denominator" +
          " open parenthesis x minus 4 close parenthesis"
      )
  }

  @Test
  fun test79() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithoutOptionalErrors("4*(x)^(2)+20x")
    assertThat(exp)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("4 times x raised to the power of 2 plus 20 x")
  }

  @Test
  fun test80() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("3+x-5")
    assertThat(exp).forHumanReadable(ENGLISH).convertsToStringThat().isEqualTo("3 plus x minus 5")
  }

  @Test
  fun test81() {
    // TODO: do something with this test.
    val exp =
      parseAlgebraicExpressionSuccessfullyWithAllErrors(
        "Z+A-Z", allowedVariables = listOf("A", "Z")
      )
    assertThat(exp).forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("Zed plus A minus Zed")
  }

  @Test
  fun test82() {
    // TODO: do something with this test.
    val exp =
      parseAlgebraicExpressionSuccessfullyWithAllErrors(
        "6C-5A-1", allowedVariables = listOf("A", "C")
      )
    assertThat(exp)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("6 C minus 5 A minus 1")
  }

  @Test
  fun test83() {
    // TODO: do something with this test.
    val exp =
      parseAlgebraicExpressionSuccessfullyWithAllErrors(
        "5*Z-w", allowedVariables = listOf("Z", "w")
      )
    assertThat(exp)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("5 times Zed minus w")
  }

  @Test
  fun test84() {
    // TODO: do something with this test.
    val exp =
      parseAlgebraicExpressionSuccessfullyWithAllErrors(
        "L*S-3S+L", allowedVariables = listOf("L", "S")
      )
    assertThat(exp)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("L times S minus 3 S plus L")
  }

  @Test
  fun test85() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("2*(2+6+3+4)")
    assertThat(exp)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("2 times open parenthesis 2 plus 6 plus 3 plus 4 close parenthesis")
  }

  @Test
  fun test86() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("sqrt(64)")
    assertThat(exp)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("square root of 64")
  }

  @Test
  fun test87() {
    // TODO: do something with this test.
    val exp =
      parseAlgebraicExpressionSuccessfullyWithAllErrors(
        "√(a+b)", allowedVariables = listOf("a", "b")
      )
    assertThat(exp)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("start square root open parenthesis a plus b close parenthesis end square root")
  }

  @Test
  fun test88() {
    // TODO: do something with this test.
    val exp = parseAlgebraicExpressionSuccessfullyWithAllErrors("3*10^-5")
    assertThat(exp)
      .forHumanReadable(ENGLISH)
      .convertsToStringThat()
      .isEqualTo("3 times 10 raised to the power of negative 5")
  }

  @Test
  fun test89() {
    // TODO: do something with this test.
    val exp =
      parseAlgebraicExpressionSuccessfullyWithoutOptionalErrors(
        "((x+2y)+5*(a-2b)+z)", allowedVariables = listOf("x", "y", "a", "b", "z")
      )
    assertThat(exp)
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
  @Singleton
  @Component(
    modules = [
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: MathExpressionAccessibilityUtilTest)
  }

  class TestApplication : Application() {
    private val component: TestApplicationComponent by lazy {
      DaggerMathExpressionAccessibilityUtilTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: MathExpressionAccessibilityUtilTest) {
      component.inject(test)
    }
  }

  private companion object {
    // TODO: finalize this API.

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
