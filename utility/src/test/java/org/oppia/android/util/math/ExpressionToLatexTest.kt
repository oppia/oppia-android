package org.oppia.android.util.math

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.MathEquation
import org.oppia.android.app.model.MathExpression
import org.oppia.android.testing.math.MathEquationSubject.Companion.assertThat
import org.oppia.android.testing.math.MathExpressionSubject.Companion.assertThat
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode
import org.oppia.android.util.math.MathExpressionParser.Companion.MathParsingResult
import org.robolectric.annotation.LooperMode

/** Tests for [MathExpressionParser]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class ExpressionToLatexTest {
  @Test
  fun testLatex() {
    // TODO: split up & move to separate test suites. Finish test cases.

    val exp1 = parseNumericExpressionWithAllErrors("1")
    assertThat(exp1).convertsToLatexStringThat().isEqualTo("1")

    val exp2 = parseNumericExpressionWithAllErrors("1+2")
    assertThat(exp2).convertsToLatexStringThat().isEqualTo("1 + 2")

    val exp3 = parseNumericExpressionWithAllErrors("1*2")
    assertThat(exp3).convertsToLatexStringThat().isEqualTo("1 \\times 2")

    val exp4 = parseNumericExpressionWithAllErrors("1/2")
    assertThat(exp4).convertsToLatexStringThat().isEqualTo("1 \\div 2")

    val exp5 = parseNumericExpressionWithAllErrors("1/2")
    assertThat(exp5).convertsWithFractionsToLatexStringThat().isEqualTo("\\frac{1}{2}")

    val exp10 = parseNumericExpressionWithAllErrors("√2")
    assertThat(exp10).convertsToLatexStringThat().isEqualTo("\\sqrt{2}")

    val exp11 = parseNumericExpressionWithAllErrors("√(1/2)")
    assertThat(exp11).convertsToLatexStringThat().isEqualTo("\\sqrt{(1 \\div 2)}")

    val exp6 = parseAlgebraicExpressionWithAllErrors("x+y")
    assertThat(exp6).convertsToLatexStringThat().isEqualTo("x + y")

    val exp7 = parseAlgebraicExpressionWithoutOptionalErrors("x^(1/y)")
    assertThat(exp7).convertsToLatexStringThat().isEqualTo("x ^ {(1 \\div y)}")

    val exp8 = parseAlgebraicExpressionWithoutOptionalErrors("x^(1/y)")
    assertThat(exp8).convertsWithFractionsToLatexStringThat().isEqualTo("x ^ {(\\frac{1}{y})}")

    val exp9 = parseAlgebraicExpressionWithoutOptionalErrors("x^y^z")
    assertThat(exp9).convertsWithFractionsToLatexStringThat().isEqualTo("x ^ {y ^ {z}}")

    val eq1 =
      parseAlgebraicEquationWithAllErrors(
        "7a^2+b^2+c^2=0", allowedVariables = listOf("a", "b", "c")
      )
    assertThat(eq1).convertsToLatexStringThat().isEqualTo("7a ^ {2} + b ^ {2} + c ^ {2} = 0")

    val eq2 = parseAlgebraicEquationWithAllErrors("sqrt(1+x)/x=1")
    assertThat(eq2).convertsToLatexStringThat().isEqualTo("\\sqrt{1 + x} \\div x = 1")

    val eq3 = parseAlgebraicEquationWithAllErrors("sqrt(1+x)/x=1")
    assertThat(eq3)
      .convertsWithFractionsToLatexStringThat()
      .isEqualTo("\\frac{\\sqrt{1 + x}}{x} = 1")
  }

  private companion object {
    // TODO: fix helper API.

    private fun parseNumericExpressionWithAllErrors(expression: String): MathExpression {
      val result =
        MathExpressionParser.parseNumericExpression(expression, ErrorCheckingMode.ALL_ERRORS)
      return (result as MathParsingResult.Success<MathExpression>).result
    }

    private fun parseAlgebraicExpressionWithoutOptionalErrors(
      expression: String,
      allowedVariables: List<String> = listOf("x", "y", "z")
    ): MathExpression {
      val result =
        parseAlgebraicExpressionInternal(
          expression, ErrorCheckingMode.REQUIRED_ONLY, allowedVariables
        )
      return (result as MathParsingResult.Success<MathExpression>).result
    }

    private fun parseAlgebraicExpressionWithAllErrors(
      expression: String,
      allowedVariables: List<String> = listOf("x", "y", "z")
    ): MathExpression {
      val result =
        parseAlgebraicExpressionInternal(
          expression, ErrorCheckingMode.ALL_ERRORS, allowedVariables
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

    private fun parseAlgebraicEquationWithAllErrors(
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
