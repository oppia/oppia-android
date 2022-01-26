package org.oppia.android.util.math

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.MathExpression
import org.oppia.android.testing.math.RealSubject.Companion.assertThat
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode.ALL_ERRORS
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode.REQUIRED_ONLY
import org.oppia.android.util.math.MathExpressionParser.Companion.MathParsingResult
import org.oppia.android.util.math.MathExpressionParser.Companion.parseAlgebraicExpression
import org.oppia.android.util.math.NumericExpressionEvaluator.Companion.evaluate
import org.robolectric.annotation.LooperMode

/**
 * Tests for [NumericExpressionEvaluator].
 *
 * This test suite is primarily focused on verifying high-level behaviors of the evaluator. More
 * specific tests exist for the sub-implementation pieces of the evaluator in [RealExtensionsTest]
 * and [FractionExtensionsTest], and more complicated expression evaluation can be seen in
 * [NumericExpressionParserTest].
 */
// FunctionName: test names are conventionally named with underscores.
// SameParameterValue: tests should have specific context included/excluded for readability.
@Suppress("FunctionName", "SameParameterValue")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class NumericExpressionEvaluatorTest {
  @Test
  fun testEvaluate_defaultExpression_returnsNull() {
    val expression = MathExpression.getDefaultInstance()

    val result = expression.evaluate()

    // Default expressions have nothing to evaluate.
    assertThat(result).isNull()
  }

  @Test
  fun testEvaluate_constantExpression_returnsConstant() {
    val expression = parseNumericExpression("2")

    val result = expression.evaluate()

    assertThat(result).isIntegerThat().isEqualTo(2)
  }

  @Test
  fun testEvaluate_variableExpression_returnsNull() {
    val expression = parseAlgebraicExpression("2x")

    val result = expression.evaluate()

    // Cannot evaluate variables.
    assertThat(result).isNull()
  }

  @Test
  fun testEvaluate_onePlusTwo_returnsThree() {
    val expression = parseNumericExpression("1+2")

    val result = expression.evaluate()

    assertThat(result).isIntegerThat().isEqualTo(3)
  }

  @Test
  fun testEvaluate_oneMinusTwo_returnsNegativeOne() {
    val expression = parseNumericExpression("1-2")

    val result = expression.evaluate()

    assertThat(result).isIntegerThat().isEqualTo(-1)
  }

  @Test
  fun testEvaluate_twoTimesSeven_returnsFourteen() {
    val expression = parseNumericExpression("2*7")

    val result = expression.evaluate()

    assertThat(result).isIntegerThat().isEqualTo(14)
  }

  @Test
  fun testEvaluate_fourDividedByTwo_returnsTwo() {
    val expression = parseNumericExpression("4/2")

    val result = expression.evaluate()

    assertThat(result).isIntegerThat().isEqualTo(2)
  }

  @Test
  fun testEvaluate_oneDividedByTwo_returnsOneHalfFraction() {
    val expression = parseNumericExpression("1/2")

    val result = expression.evaluate()

    assertThat(result).isRationalThat().apply {
      hasNegativePropertyThat().isFalse()
      hasWholeNumberThat().isEqualTo(0)
      hasNumeratorThat().isEqualTo(1)
      hasDenominatorThat().isEqualTo(2)
    }
  }

  @Test
  fun testEvaluate_minusOne_returnsMinusOne() {
    val expression = parseNumericExpression("-2")

    val result = expression.evaluate()

    assertThat(result).isIntegerThat().isEqualTo(-2)
  }

  @Test
  fun testEvaluate_plusTwo_returnsTwo() {
    val expression = parseNumericExpression("+2", errorCheckingMode = REQUIRED_ONLY)

    val result = expression.evaluate()

    assertThat(result).isIntegerThat().isEqualTo(2)
  }

  @Test
  fun testEvaluate_minusGroupOneMinusTwo_returnsOne() {
    val expression = parseNumericExpression("-(1-2)")

    val result = expression.evaluate()

    assertThat(result).isIntegerThat().isEqualTo(1)
  }

  @Test
  fun testEvaluate_plusGroupOneMinusTwo_returnsMinusOne() {
    val expression = parseNumericExpression("+(1-2)", errorCheckingMode = REQUIRED_ONLY)

    val result = expression.evaluate()

    assertThat(result).isIntegerThat().isEqualTo(-1)
  }

  @Test
  fun testEvaluate_twoTimesNegativeSeven_returnsNegativeFourteen() {
    val expression = parseNumericExpression("2*-7")

    val result = expression.evaluate()

    assertThat(result).isIntegerThat().isEqualTo(-14)
  }

  @Test
  fun testEvaluate_oneDividedByGroupOfOnePlusTwo_returnsOneThirdFraction() {
    val expression = parseNumericExpression("1/(1+2)")

    val result = expression.evaluate()

    assertThat(result).isRationalThat().apply {
      hasNegativePropertyThat().isFalse()
      hasWholeNumberThat().isEqualTo(0)
      hasNumeratorThat().isEqualTo(1)
      hasDenominatorThat().isEqualTo(3)
    }
  }

  @Test
  fun testEvaluate_twoRaisedToThree_returnsEight() {
    val expression = parseNumericExpression("2^3")

    val result = expression.evaluate()

    assertThat(result).isIntegerThat().isEqualTo(8)
  }

  @Test
  fun testEvaluate_groupOneDividedByTwoRaisedToNegativeThree_returnsEightFraction() {
    val expression = parseNumericExpression("1/(2^-3)")

    val result = expression.evaluate()

    assertThat(result).isRationalThat().apply {
      hasNegativePropertyThat().isFalse()
      hasWholeNumberThat().isEqualTo(8)
      hasNumeratorThat().isEqualTo(0)
      hasDenominatorThat().isEqualTo(1)
    }
  }

  @Test
  fun testEvaluate_rootOfTwo_returnsSquareRootOfTwoDecimal() {
    val expression = parseNumericExpression("√2")

    val result = expression.evaluate()

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(1.414213562)
  }

  @Test
  fun testEvaluate_rootOfGroupTwoRaisedToTwo_returnsTwoInteger() {
    val expression = parseNumericExpression("√(2^2)")

    val result = expression.evaluate()

    assertThat(result).isIntegerThat().isEqualTo(2)
  }

  @Test
  fun testEvaluate_threeRaisedToOneDividedByTwo_returnsSquareRootOfThreeDecimal() {
    val expression = parseNumericExpression("3^(1/2)")

    val result = expression.evaluate()

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(1.732050808)
  }

  private companion object {
    private fun parseNumericExpression(
      expression: String, errorCheckingMode: ErrorCheckingMode = ALL_ERRORS
    ): MathExpression {
      return MathExpressionParser.parseNumericExpression(
        expression, errorCheckingMode
      ).retrieveExpectedSuccessfulResult()
    }

    private fun parseAlgebraicExpression(expression: String): MathExpression {
      return parseAlgebraicExpression(
        expression, allowedVariables = listOf("x", "y", "z"), ALL_ERRORS
      ).retrieveExpectedSuccessfulResult()
    }

    private fun <T> MathParsingResult<T>.retrieveExpectedSuccessfulResult(): T {
      assertThat(this).isInstanceOf(MathParsingResult.Success::class.java)
      return (this as MathParsingResult.Success<T>).result
    }
  }
}
