package org.oppia.android.util.math

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.MathEquation
import org.oppia.android.app.model.MathExpression
import org.oppia.android.testing.math.RealSubject.Companion.assertThat
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode.ALL_ERRORS
import org.oppia.android.util.math.MathExpressionParser.Companion.MathParsingResult
import org.oppia.android.util.math.MathExpressionParser.Companion.parseAlgebraicExpression
import org.oppia.android.util.math.MathExpressionParser.Companion.parseNumericExpression
import org.robolectric.annotation.LooperMode

/**
 * Tests for [MathExpression] and [MathEquation] extensions.
 *
 * Note that this suite only verifies that the extensions work at a high-level. More specific
 * verifications for operations like LaTeX conversion and expression evaluation are part of more
 * targeted test suites such as [ExpressionToLatexConverterTest] and
 * [NumericExpressionEvaluatorTest]. For comparable operations, see
 * [ExpressionToComparableOperationConverterTest].
 */
// FunctionName: test names are conventionally named with underscores.
// SameParameterValue: tests should have specific context included/excluded for readability.
@Suppress("FunctionName", "SameParameterValue")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class MathExpressionExtensionsTest {
  @Test
  fun testToRawLatex_algebraicExpression_divNotAsFraction_returnsLatexStringWithDivision() {
    val expression = parseAlgebraicExpression("(x^2+7x-y)/2")

    val latex = expression.toRawLatex(divAsFraction = false)

    assertThat(latex).isEqualTo("(x ^ {2} + 7x - y) \\div 2")
  }

  @Test
  fun testToRawLatex_algebraicExpression_divAsFraction_returnsLatexStringWithFraction() {
    val expression = parseAlgebraicExpression("(x^2+7x-y)/2")

    val latex = expression.toRawLatex(divAsFraction = true)

    assertThat(latex).isEqualTo("\\frac{(x ^ {2} + 7x - y)}{2}")
  }

  @Test
  fun testToRawLatex_algebraicEquation_divNotAsFraction_returnsLatexStringWithDivisions() {
    val equation = parseAlgebraicEquation("y/2=(x^2+x-7)/(2x)")

    val latex = equation.toRawLatex(divAsFraction = false)

    assertThat(latex).isEqualTo("y \\div 2 = (x ^ {2} + x - 7) \\div (2x)")
  }

  @Test
  fun testToRawLatex_algebraicEquation_divAsFraction_returnsLatexStringWithFractions() {
    val equation = parseAlgebraicEquation("y/2=(x^2+x-7)/(2x)")

    val latex = equation.toRawLatex(divAsFraction = true)

    assertThat(latex).isEqualTo("\\frac{y}{2} = \\frac{(x ^ {2} + x - 7)}{(2x)}")
  }

  @Test
  fun testEvaluateAsNumericExpression_numericExpression_returnsCorrectValue() {
    val expression = parseNumericExpression("7*(3.14/0.76+8.4)^(3.8+1/(2+2/(7.4+1)))")

    val result = expression.evaluateAsNumericExpression()

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(322194.700361352)
  }

  @Test
  fun testToComparableOperation_twoAlgebraicExpressions_differentOrders_returnsEqualOperations() {
    val expression1 = parseAlgebraicExpression("x+2/x-(-7*8-9)+sqrt((x+2)+1)+3xy^2")
    val expression2 = parseAlgebraicExpression("sqrt(x+(1+2))+2/x+x-(-9+8*-7-3y^2x)")

    val operation1 = expression1.toComparableOperation()
    val operation2 = expression2.toComparableOperation()

    assertThat(operation1).isEqualTo(operation2)
  }

  @Test
  fun testToComparableOperation_twoAlgebraicExpressions_differentValue_returnsUnequalOperations() {
    val expression1 = parseAlgebraicExpression("x+2/x-(-7*8-9)+sqrt((x+2)+1)+3xy^2")
    val expression2 = parseAlgebraicExpression("sqrt(x+(1+3))+2/x+x-(-9+8*-7-3y^2x)")

    val operation1 = expression1.toComparableOperation()
    val operation2 = expression2.toComparableOperation()

    assertThat(operation1).isNotEqualTo(operation2)
  }

  private companion object {
    private fun parseNumericExpression(expression: String): MathExpression {
      return parseNumericExpression(expression, ALL_ERRORS).retrieveExpectedSuccessfulResult()
    }

    private fun parseAlgebraicExpression(expression: String): MathExpression {
      return parseAlgebraicExpression(
        expression, allowedVariables = listOf("x", "y", "z"), ALL_ERRORS
      ).retrieveExpectedSuccessfulResult()
    }

    private fun parseAlgebraicEquation(expression: String): MathEquation {
      return MathExpressionParser.parseAlgebraicEquation(
        expression, allowedVariables = listOf("x", "y", "z"), ALL_ERRORS
      ).retrieveExpectedSuccessfulResult()
    }

    private fun <T> MathParsingResult<T>.retrieveExpectedSuccessfulResult(): T {
      assertThat(this).isInstanceOf(MathParsingResult.Success::class.java)
      return (this as MathParsingResult.Success<T>).result
    }
  }
}
