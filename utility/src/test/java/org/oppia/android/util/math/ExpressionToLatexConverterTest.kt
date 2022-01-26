package org.oppia.android.util.math

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.MathEquation
import org.oppia.android.app.model.MathExpression
import org.oppia.android.testing.math.MathEquationSubject.Companion.assertThat
import org.oppia.android.testing.math.MathExpressionSubject.Companion.assertThat
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode
import org.oppia.android.util.math.MathExpressionParser.Companion.MathParsingResult
import org.robolectric.annotation.LooperMode

/** Tests for [ExpressionToLatexConverter]. */
// FunctionName: test names are conventionally named with underscores.
// SameParameterValue: tests should have specific context included/excluded for readability.
@Suppress("FunctionName", "SameParameterValue")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class ExpressionToLatexConverterTest {
  @Test
  fun testConvert_numericExp_number_returnsConstantLatex() {
    val exp = parseNumericExpressionWithAllErrors("1")

    assertThat(exp).convertsToLatexStringThat().isEqualTo("1")
  }

  @Test
  fun testConvert_numericExp_unaryPlus_withoutOptionalErrors_returnLatexWithUnaryPlus() {
    val exp = parseNumericExpressionWithoutOptionalErrors("+1")

    assertThat(exp).convertsToLatexStringThat().isEqualTo("+1")
  }

  @Test
  fun testConvert_numericExp_unaryMinus_returnLatexWithUnaryMinus() {
    val exp = parseNumericExpressionWithAllErrors("-1")

    assertThat(exp).convertsToLatexStringThat().isEqualTo("-1")
  }

  @Test
  fun testConvert_numericExp_addition_returnsLatexWithAddition() {
    val exp = parseNumericExpressionWithAllErrors("1+2")

    assertThat(exp).convertsToLatexStringThat().isEqualTo("1 + 2")
  }

  @Test
  fun testConvert_numericExp_subtraction_returnsLatexWithSubtract() {
    val exp = parseNumericExpressionWithAllErrors("1-2")

    assertThat(exp).convertsToLatexStringThat().isEqualTo("1 - 2")
  }

  @Test
  fun testConvert_numericExp_multiplication_returnsLatexWithMultiplication() {
    val exp = parseNumericExpressionWithAllErrors("2*3")

    assertThat(exp).convertsToLatexStringThat().isEqualTo("2 \\times 3")
  }

  @Test
  fun testConvert_numericExp_division_returnsLatexWithDivision() {
    val exp = parseNumericExpressionWithAllErrors("2/3")

    assertThat(exp).convertsToLatexStringThat().isEqualTo("2 \\div 3")
  }

  @Test
  fun testConvert_numericExp_division_divAsFraction_returnsLatexWithFraction() {
    val exp = parseNumericExpressionWithAllErrors("2/3")

    assertThat(exp).convertsWithFractionsToLatexStringThat().isEqualTo("\\frac{2}{3}")
  }

  @Test
  fun testConvert_numericExp_multipleDivisions_divAsFraction_returnsLatexWithFractions() {
    val exp = parseNumericExpressionWithAllErrors("2/3/4")

    assertThat(exp).convertsWithFractionsToLatexStringThat().isEqualTo("\\frac{\\frac{2}{3}}{4}")
  }

  @Test
  fun testConvert_numericExp_exponent_returnsLatexWithExponent() {
    val exp = parseNumericExpressionWithAllErrors("2^3")

    assertThat(exp).convertsToLatexStringThat().isEqualTo("2 ^ {3}")
  }

  @Test
  fun testConvert_numericExp_inlineSquareRoot_returnsLatexWithSquareRoot() {
    val exp = parseNumericExpressionWithAllErrors("√2")

    assertThat(exp).convertsToLatexStringThat().isEqualTo("\\sqrt{2}")
  }

  @Test
  fun testConvert_numericExp_inlineSquareRoot_operationArg_returnsLatexWithSquareRoot() {
    val exp = parseNumericExpressionWithAllErrors("√(1+2)")

    assertThat(exp).convertsToLatexStringThat().isEqualTo("\\sqrt{(1 + 2)}")
  }

  @Test
  fun testConvert_numericExp_squareRoot_returnsLatexWithSquareRoot() {
    val exp = parseNumericExpressionWithAllErrors("sqrt(2)")

    assertThat(exp).convertsToLatexStringThat().isEqualTo("\\sqrt{2}")
  }

  @Test
  fun testConvert_numericExp_squareRoot_operationArg_returnsLatexWithSquareRoot() {
    val exp = parseNumericExpressionWithAllErrors("sqrt(1+2)")

    assertThat(exp).convertsToLatexStringThat().isEqualTo("\\sqrt{1 + 2}")
  }

  @Test
  fun testConvert_numericExp_parentheses_returnsLatexWithGroup() {
    val exp = parseNumericExpressionWithAllErrors("2/(3+4)")

    assertThat(exp).convertsToLatexStringThat().isEqualTo("2 \\div (3 + 4)")
  }

  @Test
  fun testConvert_numericExp_exponentToGroup_returnsCorrectlyWrappedLatex() {
    val exp = parseNumericExpressionWithAllErrors("2^(7-3)")

    assertThat(exp).convertsToLatexStringThat().isEqualTo("2 ^ {(7 - 3)}")
  }

  @Test
  fun testConvert_algebraicExp_variable_returnsVariableLatex() {
    val exp = parseAlgebraicExpressionWithAllErrors("x")

    assertThat(exp).convertsToLatexStringThat().isEqualTo("x")
  }

  @Test
  fun testConvert_algebraicExp_twoX_returnsLatexWithImplicitMultiplication() {
    val exp = parseAlgebraicExpressionWithAllErrors("2x")

    assertThat(exp).convertsToLatexStringThat().isEqualTo("2x")
  }

  @Test
  fun testConvert_algebraicEq_xEqualsOne_returnsLatexWithEquals() {
    val exp = parseAlgebraicEquationWithAllErrors("x=1")

    assertThat(exp).convertsToLatexStringThat().isEqualTo("x = 1")
  }

  @Test
  fun testConvert_algebraicEq_complexExpression_returnsCorrectLatex() {
    val exp = parseAlgebraicEquationWithAllErrors("(x+1)(x-2)=(x^3+2x^2-5x-6)/(x+3)")

    assertThat(exp)
      .convertsToLatexStringThat()
      .isEqualTo("(x + 1)(x - 2) = (x ^ {3} + 2x ^ {2} - 5x - 6) \\div (x + 3)")
  }

  @Test
  fun testConvert_algebraicEq_complexExpression_divAsFraction_returnsCorrectLatex() {
    val exp = parseAlgebraicEquationWithAllErrors("(x+1)(x-2)=(x^3+2x^2-5x-6)/(x+3)")

    assertThat(exp)
      .convertsWithFractionsToLatexStringThat()
      .isEqualTo("(x + 1)(x - 2) = \\frac{(x ^ {3} + 2x ^ {2} - 5x - 6)}{(x + 3)}")
  }

  private companion object {
    private fun parseNumericExpressionWithoutOptionalErrors(expression: String): MathExpression {
      return parseNumericExpressionInternal(expression, ErrorCheckingMode.REQUIRED_ONLY)
    }

    private fun parseNumericExpressionWithAllErrors(expression: String): MathExpression {
      return parseNumericExpressionInternal(expression, ErrorCheckingMode.ALL_ERRORS)
    }

    private fun parseAlgebraicExpressionWithAllErrors(
      expression: String,
      allowedVariables: List<String> = listOf("x", "y", "z")
    ): MathExpression {
      return MathExpressionParser.parseAlgebraicExpression(
        expression, allowedVariables,
        ErrorCheckingMode.ALL_ERRORS
      ).getExpectedSuccess()
    }
    
    private fun parseNumericExpressionInternal(
      expression: String, errorCheckingMode: ErrorCheckingMode
    ): MathExpression {
      return MathExpressionParser.parseNumericExpression(
        expression, errorCheckingMode
      ).getExpectedSuccess()
    }

    private fun parseAlgebraicEquationWithAllErrors(
      expression: String,
      allowedVariables: List<String> = listOf("x", "y", "z")
    ): MathEquation {
      return MathExpressionParser.parseAlgebraicEquation(
        expression, allowedVariables,
        ErrorCheckingMode.ALL_ERRORS
      ).getExpectedSuccess()
    }

    private inline fun <reified T> MathParsingResult<T>.getExpectedSuccess(): T {
      assertThat(this).isInstanceOf(MathParsingResult.Success::class.java)
      return (this as MathParsingResult.Success<T>).result
    }
  }
}
