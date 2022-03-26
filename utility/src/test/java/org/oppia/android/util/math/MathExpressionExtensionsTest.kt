package org.oppia.android.util.math

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.MathEquation
import org.oppia.android.app.model.MathExpression
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Iteration
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Parameter
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.RunParameterized
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.SelectRunnerPlatform
import org.oppia.android.testing.junit.ParameterizedJunitTestRunner
import org.oppia.android.testing.math.PolynomialSubject.Companion.assertThat
import org.oppia.android.testing.math.RealSubject.Companion.assertThat
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode.ALL_ERRORS
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode.REQUIRED_ONLY
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
 * [ExpressionToComparableOperationConverterTest]. For polynomials, see
 * [ExpressionToPolynomialConverterTest].
 */
// FunctionName: test names are conventionally named with underscores.
// SameParameterValue: tests should have specific context included/excluded for readability.
@Suppress("FunctionName", "SameParameterValue")
@RunWith(OppiaParameterizedTestRunner::class)
@SelectRunnerPlatform(ParameterizedJunitTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
class MathExpressionExtensionsTest {
  @Parameter lateinit var exp1: String
  @Parameter lateinit var exp2: String

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

  @Test
  fun testToPolynomial_algebraicExpression_returnsCorrectPolynomial() {
    val expression = parseAlgebraicExpression("(x^3-y^3)/(x-y)")

    val polynomial = expression.toPolynomial()

    assertThat(polynomial).hasTermCountThat().isEqualTo(3)
    assertThat(polynomial).term(0).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(1)
      hasVariableCountThat().isEqualTo(1)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(2)
      }
    }
    assertThat(polynomial).term(1).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(1)
      hasVariableCountThat().isEqualTo(2)
      variable(0).apply {
        hasNameThat().isEqualTo("x")
        hasPowerThat().isEqualTo(1)
      }
      variable(1).apply {
        hasNameThat().isEqualTo("y")
        hasPowerThat().isEqualTo(1)
      }
    }
    assertThat(polynomial).term(2).apply {
      hasCoefficientThat().isIntegerThat().isEqualTo(1)
      hasVariableCountThat().isEqualTo(1)
      variable(0).apply {
        hasNameThat().isEqualTo("y")
        hasPowerThat().isEqualTo(2)
      }
    }
  }

  /* Equality checks. Note that these are symmetrical to reduce the number of needed test cases. */

  @Test
  fun testIsApproximatelyEqualTo_oneIsDefault_otherIsConstInt2_returnsFalse() {
    val first = MathExpression.getDefaultInstance()
    val second = parseNumericExpression("2")

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_oneIsConstInt2_otherIsDefault_returnsFalse() {
    val first = parseNumericExpression("2")
    val second = MathExpression.getDefaultInstance()

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  @RunParameterized(
    Iteration("2==2", "exp1=2", "exp2=2"),
    Iteration("2==2.000000000000001", "exp1=2", "exp2=2.000000000000001"),
    Iteration("x+1==x+1", "exp1=x+1", "exp2=x+1"),
    Iteration("x-1==x-1", "exp1=x-1", "exp2=x-1"),
    Iteration("x*2==x*2", "exp1=x*2", "exp2=x*2"),
    Iteration("x/2==x/2", "exp1=x/2", "exp2=x/2"),
    Iteration("x^2==x^2", "exp1=x^2", "exp2=x^2"),
    Iteration("-x==-x", "exp1=-x", "exp2=-x"),
    Iteration("sqrt(x)==sqrt(x)", "exp1=sqrt(x)", "exp2=sqrt(x)")
  )
  fun testIsApproximatelyEqualTo_bothAreSingleTermsOrOperations_andSame_returnsTrue() {
    val first = parseAlgebraicExpression(exp1)
    val second = parseAlgebraicExpression(exp2)

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  @RunParameterized(
    Iteration("2!=3", "exp1=2", "exp2=3"),
    Iteration("2!=3/2", "exp1=2", "exp2=3/2"),
    Iteration("2!=3.14", "exp1=2", "exp2=3.14"),
    Iteration("x!=y", "exp1=x", "exp2=y"),
    Iteration("x!=2", "exp1=x", "exp2=2"),
    // The number of terms must match.
    Iteration("1+x!=1", "exp1=1+x", "exp2=1"),
    Iteration("1+x!=x", "exp1=1+x", "exp2=x"),
    Iteration("1+1+x!=2+x", "exp1=1+1+x", "exp2=2+x"),
    // Term order must match.
    Iteration("1+x!=2+x", "exp1=1+x", "exp2=2+x"),
    Iteration("1+x!=x+1", "exp1=1+x", "exp2=x+1"),
    Iteration("1-x!=2-x", "exp1=1-x", "exp2=2-x"),
    Iteration("1-x!=x-1", "exp1=1-x", "exp2=x-1"),
    Iteration("2*x!=3*x", "exp1=2*x", "exp2=3*x"),
    Iteration("2*x!=x*2", "exp1=2*x", "exp2=x*2"),
    Iteration("x/2!=x/3", "exp1=x/2", "exp2=x/3"),
    Iteration("x/2!=2/x", "exp1=x/2", "exp2=2/x"),
    Iteration("x^2!=x^3", "exp1=x^2", "exp2=x^3"),
    Iteration("x^2!=2^x", "exp1=x^2", "exp2=2^x"),
    Iteration("x!=-2", "exp1=x", "exp2=-2"),
    Iteration("x!=-x", "exp1=x", "exp2=-x"),
    Iteration("sqrt(x)!=sqrt(2)", "exp1=sqrt(x)", "exp2=sqrt(2)"),
    // These checks are numerically equivalent but fail due to the expression structure not
    // matching.
    Iteration("2==2/1", "exp1=2", "exp2=2/1"),
    Iteration("1/3==0.33333333", "exp1=1/3", "exp2=0.33333333"),
    Iteration("1.5==3/2", "exp1=1.5", "exp2=3/2")
  )
  fun testIsApproximatelyEqualTo_bothAreSingleTermsOrOperations_butDifferent_returnsFalse() {
    // Some expressions may attempt normally disallowed expressions (such as '2^x').
    val first = parseAlgebraicExpression(exp1, errorCheckingMode = REQUIRED_ONLY)
    val second = parseAlgebraicExpression(exp2, errorCheckingMode = REQUIRED_ONLY)

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_complexExpressionsWithNesting_allTermsMatch_returnsTrue() {
    val expression = "x+2/x-(-7*8-9)+sqrt((x+2)+1)+3xy^2"
    val first = parseAlgebraicExpression(expression)
    val second = parseAlgebraicExpression(expression)

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_complexExpressionsWithNesting_oneDifferent_returnsFalse() {
    // One difference in operations, but otherwise the same values. This equality check demonstrates
    // that the check is inherently recursive & properly checks for nested expression equality.
    val first = parseAlgebraicExpression("x+2/x-(-7*8-9)+sqrt((x+2)+1)+3xy^2")
    val second = parseAlgebraicExpression("x+2/x-(-7*8-9)+sqrt((x+1+1)+1)+3xy^2")

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_complexExpressionsWithNesting_comparedWithDefault_returnsFalse() {
    val first = parseAlgebraicExpression("x+2/x-(-7*8-9)+sqrt((x+2)+1)+3xy^2")
    val second = MathExpression.getDefaultInstance()

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  private companion object {
    private fun parseNumericExpression(expression: String): MathExpression {
      return parseNumericExpression(expression, ALL_ERRORS).retrieveExpectedSuccessfulResult()
    }

    private fun parseAlgebraicExpression(
      expression: String,
      errorCheckingMode: ErrorCheckingMode = ALL_ERRORS
    ): MathExpression {
      return parseAlgebraicExpression(
        expression, allowedVariables = listOf("x", "y", "z"), errorCheckingMode
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
