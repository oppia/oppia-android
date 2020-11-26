package org.oppia.android.util.math

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.MathExpression
import org.oppia.android.app.model.Real
import org.oppia.android.util.math.MathExpressionParser.Companion.ParseResult.Success
import org.robolectric.annotation.LooperMode

/** Tests for [MathExpressionParser]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class MathExpressionParserTest {
//  @Test
//  fun testParse_emptyString_returnsFailure() {
//  }

  // test different parenthesis errors
  // test unary operators
  // test nested parenthesis
  // test associativity (left & right)
  // test order of operations
  // test multiple variables
  // test reals vs. whole numbers
  // test nested operations for each operator

  // TODO: add support for implied multiplication (e.g. 'xy' should imply x*y). Ditto for coefficients.

  @Test
  fun testParse_constantNumber_returnsExpressionWithFractionWholeNumber() {
//    val result = MathExpressionParser.parseExpression("1")
    //val result = MathExpressionParser.parseExpression("133 + 3.14 * x / (11 - 15) ^ 2 ^ 3")
//    val result = MathExpressionParser.parseExpression("3 + 4 * 2 / (1 - 5) ^ 2 ^ 3")
    // Test: 10/-1*-2 to verify unary precedence.
    // Test unary at: beginning, after open paren, close paren (should be minus), and after operators
    // Test multiple (2 & 3) unaries after each of the above
    // Test invalid operator & operand cases, e.g. multiple operands or operators in wrong place

//    val result = MathExpressionParser.parseExpression("x^2*y^2 + 2") // works
//    val result = MathExpressionParser.parseExpression("(x-1)^3") // works
//    val result = MathExpressionParser.parseExpression("(x+1)/2") // fails
//    val result = MathExpressionParser.parseExpression("x^2-3*x-10") // works
//    val result = MathExpressionParser.parseExpression("x+2") // works
//    val result = MathExpressionParser.parseExpression("4*(x+2)") // works
    val result = MathExpressionParser.parseExpression("(x^2-3*x-10)*(x+2)") // works
//    val result = MathExpressionParser.parseExpression("(x^2-3*x-10)/(x+2)") // fails
    val polynomial = (result as Success).mathExpression.toPolynomial()

    println("@@@@@ Result: ${(result as Success).mathExpression}}")
    println("@@@@@ Polynomial: $polynomial")
    println("@@@@@ Polynomial str: ${polynomial?.toAnswerString()}")

    assertThat(result).isInstanceOf(Success::class.java)
    val expression = (result as Success).mathExpression
    assertThat(expression.expressionTypeCase).isEqualTo(MathExpression.ExpressionTypeCase.CONSTANT)
    assertThat(expression.constant.realTypeCase).isEqualTo(Real.RealTypeCase.RATIONAL)
    assertThat(expression.constant.rational).isEqualTo(createWholeNumberFraction(1))
  }

  private fun createWholeNumberFraction(value: Int): Fraction {
    return Fraction.newBuilder().setWholeNumber(value).build()
  }
}
