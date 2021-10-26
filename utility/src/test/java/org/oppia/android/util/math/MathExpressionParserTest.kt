package org.oppia.android.util.math

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.MathBinaryOperation
import org.oppia.android.app.model.MathBinaryOperation.Operator.ADD
import org.oppia.android.app.model.MathBinaryOperation.Operator.DIVIDE
import org.oppia.android.app.model.MathBinaryOperation.Operator.EXPONENTIATE
import org.oppia.android.app.model.MathBinaryOperation.Operator.MULTIPLY
import org.oppia.android.app.model.MathBinaryOperation.Operator.SUBTRACT
import org.oppia.android.app.model.MathExpression
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.BINARY_OPERATION
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.CONSTANT
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.UNARY_OPERATION
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.VARIABLE
import org.oppia.android.app.model.MathUnaryOperation
import org.oppia.android.app.model.MathUnaryOperation.Operator.NEGATE
import org.oppia.android.app.model.Real
import org.oppia.android.app.model.Real.RealTypeCase.IRRATIONAL
import org.oppia.android.app.model.Real.RealTypeCase.RATIONAL
import org.oppia.android.testing.assertThrows
import org.oppia.android.util.math.MathExpressionParser.Companion.ParseResult
import org.oppia.android.util.math.MathExpressionParser.Companion.ParseResult.Failure
import org.oppia.android.util.math.MathExpressionParser.Companion.ParseResult.Success
import org.robolectric.annotation.LooperMode

/** Tests for [MathExpressionParser]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class MathExpressionParserTest {
//  @Test
//  fun testParse_emptyString_returnsFailure() {
//  }

  // test various incorrect formatting errors including:
  // - invalid variables
  // - incorrect parentheses
  // - consecutive binary operators
  // - multiple expressions at the root

  // TODO: add support for implied multiplication (e.g. 'xy' should imply x*y). Ditto for coefficients.
  // TOOD: test decimals, long variables, etc.

//    val result = MathExpressionParser.parseExpression("1")
  // val result = MathExpressionParser.parseExpression("133 + 3.14 * x / (11 - 15) ^ 2 ^ 3")
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
//    val result = MathExpressionParser.parseExpression("(x^2-3*x-10)*(x+2)") // works
//    val result = MathExpressionParser.parseExpression("(x^2-3*x-10)/(x+2)") // fails
//    val polynomial = (result as Success).mathExpression.toPolynomial()

//    println("@@@@@ Result: ${(result as Success).mathExpression}}")
//    println("@@@@@ Polynomial: $polynomial")
//    println("@@@@@ Polynomial str: ${polynomial?.toAnswerString()}")

//    assertThat(result).isInstanceOf(Success::class.java)
//    val expression = (result as Success).mathExpression
//    assertThat(expression.expressionTypeCase).isEqualTo(MathExpression.ExpressionTypeCase.CONSTANT)
//    assertThat(expression.constant.realTypeCase).isEqualTo(Real.RealTypeCase.RATIONAL)
//    assertThat(expression.constant.rational).isEqualTo(createWholeNumberFraction(1))

  @Test
  fun testParse_constantWholeNumber_parsesSuccessfully() {
    val result = MathExpressionParser.parseExpression("1", allowedVariables = listOf())

    assertThat(result).isInstanceOf(Success::class.java)
  }

  @Test
  fun testParse_constantWholeNumber_returnsExpressionWithFractionWholeNumber() {
    val result = MathExpressionParser.parseExpression("1", allowedVariables = listOf())

    val rootExpression = result.getExpectedSuccessfulExpression()
    assertThat(result).isInstanceOf(Success::class.java)
    assertThat(rootExpression.constant.realTypeCase).isEqualTo(RATIONAL)
    assertThat(rootExpression.constant.rational).isEqualTo(createWholeNumberFraction(1))
  }

  @Test
  fun testParse_constantDecimalNumber_returnsExpressionWithIrrationalNumber() {
    val result = MathExpressionParser.parseExpression("3.14", allowedVariables = listOf())

    val rootExpression = result.getExpectedSuccessfulExpression()
    assertThat(rootExpression.expressionTypeCase).isEqualTo(CONSTANT)
    assertThat(rootExpression.constant.realTypeCase).isEqualTo(IRRATIONAL)
    assertThat(rootExpression.constant.irrational).isWithin(1e-5).of(3.14)
  }

  @Test
  fun testParse_variable_returnsExpressionWithVariable() {
    val result = MathExpressionParser.parseExpression("x", allowedVariables = listOf("x"))

    val rootExpression = result.getExpectedSuccessfulExpression()
    assertThat(rootExpression.expressionTypeCase).isEqualTo(VARIABLE)
    assertThat(rootExpression.variable).isEqualTo("x")
  }

  @Test
  fun testParse_multipleShortVariables_returnsExpressionWithBothVariables() {
    val result = MathExpressionParser.parseExpression("x+y", allowedVariables = listOf("x", "y"))

    val rootExpression = result.getExpectedSuccessfulExpression()
    val binOp = rootExpression.getExpectedBinaryOperation()
    val leftVar = binOp.leftOperand.getExpectedVariable()
    val rightVar = binOp.rightOperand.getExpectedVariable()
    assertThat(leftVar).isEqualTo("x")
    assertThat(rightVar).isEqualTo("y")
  }

  @Test
  fun testParse_longVariable_returnsExpressionWithVariable() {
    val result =
      MathExpressionParser.parseExpression("1+lambda", allowedVariables = listOf("lambda"))

    val rootExpression = result.getExpectedSuccessfulExpression()
    val binOp = rootExpression.getExpectedBinaryOperation()
    val rightVar = binOp.rightOperand.getExpectedVariable()
    assertThat(rightVar).isEqualTo("lambda")
  }

  @Test
  fun testParse_mixedLongAndShortVariable_returnsExpressionWithBothVariables() {
    val result =
      MathExpressionParser.parseExpression("lambda+y", allowedVariables = listOf("y", "lambda"))

    val rootExpression = result.getExpectedSuccessfulExpression()
    val binOp = rootExpression.getExpectedBinaryOperation()
    val leftVar = binOp.leftOperand.getExpectedVariable()
    val rightVar = binOp.rightOperand.getExpectedVariable()
    assertThat(leftVar).isEqualTo("lambda")
    assertThat(rightVar).isEqualTo("y")
  }

  @Test
  fun testParse_negation_returnsUnaryExpressionNegatingVariable() {
    val result = MathExpressionParser.parseExpression("-x", allowedVariables = listOf("x"))

    val rootExpression = result.getExpectedSuccessfulExpression()
    val unaryOp = rootExpression.getExpectedUnaryOperation()
    val variableOperand = unaryOp.operand.getExpectedVariable()
    assertThat(unaryOp.operator).isEqualTo(NEGATE)
    assertThat(variableOperand).isEqualTo("x")
  }

  @Test
  fun testParse_negateExpression_returnsUnaryExpressionBeingNegated() {
    val result = MathExpressionParser.parseExpression("-(x+2)", allowedVariables = listOf("x"))

    val rootExpression = result.getExpectedSuccessfulExpression()
    val unaryOp = rootExpression.getExpectedUnaryOperationWithOperator(NEGATE)
    // The entire operation should be negated.
    assertThat(unaryOp.operand.expressionTypeCase).isEqualTo(BINARY_OPERATION)
  }

  @Test
  fun testParse_doubleNegate_cascadesInTree() {
    val result = MathExpressionParser.parseExpression("--x", allowedVariables = listOf("x"))

    // Expected tree (left-to-right): negate(negate(x))
    val rootExpression = result.getExpectedSuccessfulExpression()
    val outerOp = rootExpression.getExpectedUnaryOperationWithOperator(NEGATE)
    val innerOp = outerOp.operand.getExpectedUnaryOperationWithOperator(NEGATE)
    assertThat(innerOp.operand.expressionTypeCase).isEqualTo(VARIABLE)
  }

  @Test
  fun testParse_addVariableAndConstant_returnsBinaryExpression() {
    val result = MathExpressionParser.parseExpression("x+2", allowedVariables = listOf("x"))

    val rootExpression = result.getExpectedSuccessfulExpression()
    val binaryOp = rootExpression.getExpectedBinaryOperation()
    val leftVariable = binaryOp.leftOperand.getExpectedVariable()
    val rightConstant = binaryOp.rightOperand.getExpectedConstant()
    assertThat(binaryOp.operator).isEqualTo(ADD)
    assertThat(leftVariable).isEqualTo("x")
    assertThat(rightConstant.realTypeCase).isEqualTo(RATIONAL)
    assertThat(rightConstant.rational).isEqualTo(createWholeNumberFraction(2))
  }

  @Test
  fun testParse_addVariableAndIrrationalConstant_returnsBinaryExpression() {
    val result = MathExpressionParser.parseExpression("x+2.718", allowedVariables = listOf("x"))

    val rootExpression = result.getExpectedSuccessfulExpression()
    val binaryOp = rootExpression.getExpectedBinaryOperation()
    val leftVariable = binaryOp.leftOperand.getExpectedVariable()
    val rightConstant = binaryOp.rightOperand.getExpectedConstant()
    assertThat(binaryOp.operator).isEqualTo(ADD)
    assertThat(leftVariable).isEqualTo("x")
    assertThat(rightConstant.realTypeCase).isEqualTo(IRRATIONAL)
    assertThat(rightConstant.irrational).isWithin(1e-5).of(2.718)
  }

  @Test
  fun testParse_addConstantWithNegatedVariable_combinesUnaryAndBinaryOperations() {
    val result = MathExpressionParser.parseExpression("2+-x", allowedVariables = listOf("x"))

    // Expected tree (left-to-right): add(2, negate(x))
    val rootExpression = result.getExpectedSuccessfulExpression()
    val binaryOp = rootExpression.getExpectedBinaryOperationWithOperator(ADD)
    val leftConstant = binaryOp.leftOperand.getExpectedRationalConstant()
    val rightNegateOp = binaryOp.rightOperand.getExpectedUnaryOperationWithOperator(NEGATE)
    val negatedVariable = rightNegateOp.operand.getExpectedVariable()
    assertThat(leftConstant).isEqualTo(createWholeNumberFraction(2))
    assertThat(negatedVariable).isEqualTo("x")
  }

  @Test
  fun testParse_subtractVariableAndConstant_returnsBinaryExpression() {
    val result = MathExpressionParser.parseExpression("x-2", allowedVariables = listOf("x"))

    val rootExpression = result.getExpectedSuccessfulExpression()
    val binaryOp = rootExpression.getExpectedBinaryOperation()
    val leftVariable = binaryOp.leftOperand.getExpectedVariable()
    val rightConstant = binaryOp.rightOperand.getExpectedConstant()
    assertThat(binaryOp.operator).isEqualTo(SUBTRACT)
    assertThat(leftVariable).isEqualTo("x")
    assertThat(rightConstant.realTypeCase).isEqualTo(RATIONAL)
    assertThat(rightConstant.rational).isEqualTo(createWholeNumberFraction(2))
  }

  @Test
  fun testParse_multiplyVariableAndConstant_returnsBinaryExpression() {
    val result = MathExpressionParser.parseExpression("x × 2", allowedVariables = listOf("x"))

    val rootExpression = result.getExpectedSuccessfulExpression()
    val binaryOp = rootExpression.getExpectedBinaryOperation()
    val leftVariable = binaryOp.leftOperand.getExpectedVariable()
    val rightConstant = binaryOp.rightOperand.getExpectedConstant()
    assertThat(binaryOp.operator).isEqualTo(MULTIPLY)
    assertThat(leftVariable).isEqualTo("x")
    assertThat(rightConstant.realTypeCase).isEqualTo(RATIONAL)
    assertThat(rightConstant.rational).isEqualTo(createWholeNumberFraction(2))
  }

  @Test
  fun testParse_divideVariableAndConstant_returnsBinaryExpression() {
    val result = MathExpressionParser.parseExpression("x ÷ 2", allowedVariables = listOf("x"))

    val rootExpression = result.getExpectedSuccessfulExpression()
    val binaryOp = rootExpression.getExpectedBinaryOperation()
    val leftVariable = binaryOp.leftOperand.getExpectedVariable()
    val rightConstant = binaryOp.rightOperand.getExpectedConstant()
    assertThat(binaryOp.operator).isEqualTo(DIVIDE)
    assertThat(leftVariable).isEqualTo("x")
    assertThat(rightConstant.realTypeCase).isEqualTo(RATIONAL)
    assertThat(rightConstant.rational).isEqualTo(createWholeNumberFraction(2))
  }

  @Test
  fun testParse_variableRaisedByConstant_returnsBinaryExpression() {
    val result = MathExpressionParser.parseExpression("x^2", allowedVariables = listOf("x"))

    val rootExpression = result.getExpectedSuccessfulExpression()
    val binaryOp = rootExpression.getExpectedBinaryOperation()
    val leftVariable = binaryOp.leftOperand.getExpectedVariable()
    val rightConstant = binaryOp.rightOperand.getExpectedConstant()
    assertThat(binaryOp.operator).isEqualTo(EXPONENTIATE)
    assertThat(leftVariable).isEqualTo("x")
    assertThat(rightConstant.realTypeCase).isEqualTo(RATIONAL)
    assertThat(rightConstant.rational).isEqualTo(createWholeNumberFraction(2))
  }

  @Test
  fun testParse_binaryAddition_withRedundantParentheses_returnsSameBinaryExpression() {
    // Test that extra parentheses don't change the result.
    val result = MathExpressionParser.parseExpression("(x+2)", allowedVariables = listOf("x"))

    val rootExpression = result.getExpectedSuccessfulExpression()
    val binaryOp = rootExpression.getExpectedBinaryOperation()
    val leftVariable = binaryOp.leftOperand.getExpectedVariable()
    val rightConstant = binaryOp.rightOperand.getExpectedConstant()
    assertThat(binaryOp.operator).isEqualTo(ADD)
    assertThat(leftVariable).isEqualTo("x")
    assertThat(rightConstant.realTypeCase).isEqualTo(RATIONAL)
    assertThat(rightConstant.rational).isEqualTo(createWholeNumberFraction(2))
  }

  @Test
  fun testParse_twoAdditions_haveLeftToRightAssociativity() {
    val result = MathExpressionParser.parseExpression("1+3+2", allowedVariables = listOf())

    // Left-to-right associativity means the first encountered addition is done first, and the
    // second is done last (which means it's at the root). Expect the following tree:
    //    +
    //  +   2
    // 1 3
    val rootExpression = result.getExpectedSuccessfulExpression()
    val rootAddOp = rootExpression.getExpectedBinaryOperationWithOperator(ADD)
    val rootLeftAddOp = rootAddOp.leftOperand.getExpectedBinaryOperationWithOperator(ADD)
    val rootRightConstant = rootAddOp.rightOperand.getExpectedRationalConstant()
    val innerOpLeftConstant = rootLeftAddOp.leftOperand.getExpectedRationalConstant()
    val innerOpRightConstant = rootLeftAddOp.rightOperand.getExpectedRationalConstant()
    assertThat(rootRightConstant).isEqualTo(createWholeNumberFraction(2))
    assertThat(innerOpLeftConstant).isEqualTo(createWholeNumberFraction(1))
    assertThat(innerOpRightConstant).isEqualTo(createWholeNumberFraction(3))
  }

  @Test
  fun testParse_additionThenSubtraction_haveLeftToRightAssociativity() {
    val result = MathExpressionParser.parseExpression("1+3-2", allowedVariables = listOf())

    // Left-to-right associativity means the first encountered addition is done first, and the
    // second is done last (which means it's at the root). Expect the following tree:
    //    -
    //  +   2
    // 1 3
    val rootExpression = result.getExpectedSuccessfulExpression()
    val rootSubOp = rootExpression.getExpectedBinaryOperationWithOperator(SUBTRACT)
    val rootLeftAddOp = rootSubOp.leftOperand.getExpectedBinaryOperationWithOperator(ADD)
    val rootRightConstant = rootSubOp.rightOperand.getExpectedRationalConstant()
    val innerOpLeftConstant = rootLeftAddOp.leftOperand.getExpectedRationalConstant()
    val innerOpRightConstant = rootLeftAddOp.rightOperand.getExpectedRationalConstant()
    assertThat(rootRightConstant).isEqualTo(createWholeNumberFraction(2))
    assertThat(innerOpLeftConstant).isEqualTo(createWholeNumberFraction(1))
    assertThat(innerOpRightConstant).isEqualTo(createWholeNumberFraction(3))
  }

  @Test
  fun testParse_subtractionThenAddition_haveLeftToRightAssociativity() {
    val result = MathExpressionParser.parseExpression("1-3+2", allowedVariables = listOf())

    // Left-to-right associativity means the first encountered addition is done first, and the
    // second is done last (which means it's at the root). Expect the following tree:
    //    +
    //  -   2
    // 1 3
    val rootExpression = result.getExpectedSuccessfulExpression()
    val rootAddOp = rootExpression.getExpectedBinaryOperationWithOperator(ADD)
    val rootLeftSubOp = rootAddOp.leftOperand.getExpectedBinaryOperationWithOperator(SUBTRACT)
    val rootRightConstant = rootAddOp.rightOperand.getExpectedRationalConstant()
    val innerOpLeftConstant = rootLeftSubOp.leftOperand.getExpectedRationalConstant()
    val innerOpRightConstant = rootLeftSubOp.rightOperand.getExpectedRationalConstant()
    assertThat(rootRightConstant).isEqualTo(createWholeNumberFraction(2))
    assertThat(innerOpLeftConstant).isEqualTo(createWholeNumberFraction(1))
    assertThat(innerOpRightConstant).isEqualTo(createWholeNumberFraction(3))
  }

  @Test
  fun testParse_twoMultiplies_haveLeftToRightAssociativity() {
    val result = MathExpressionParser.parseExpression("1*3*2", allowedVariables = listOf())

    // Left-to-right associativity means the first encountered binary op is done first, and the
    // second is done last (which means it's at the root). Expect the following tree:
    //    *
    //  *   2
    // 1 3
    val rootExpression = result.getExpectedSuccessfulExpression()
    val rootMulOp = rootExpression.getExpectedBinaryOperationWithOperator(MULTIPLY)
    val rootLeftMulOp = rootMulOp.leftOperand.getExpectedBinaryOperationWithOperator(MULTIPLY)
    val rootRightConstant = rootMulOp.rightOperand.getExpectedRationalConstant()
    val innerOpLeftConstant = rootLeftMulOp.leftOperand.getExpectedRationalConstant()
    val innerOpRightConstant = rootLeftMulOp.rightOperand.getExpectedRationalConstant()
    assertThat(rootRightConstant).isEqualTo(createWholeNumberFraction(2))
    assertThat(innerOpLeftConstant).isEqualTo(createWholeNumberFraction(1))
    assertThat(innerOpRightConstant).isEqualTo(createWholeNumberFraction(3))
  }

  @Test
  fun testParse_multiplyThenDivide_haveLeftToRightAssociativity() {
    val result = MathExpressionParser.parseExpression("1*3/2", allowedVariables = listOf())

    // Left-to-right associativity means the first encountered binary op is done first, and the
    // second is done last (which means it's at the root). Expect the following tree:
    //    /
    //  *   2
    // 1 3
    val rootExpression = result.getExpectedSuccessfulExpression()
    val rootDivOp = rootExpression.getExpectedBinaryOperationWithOperator(DIVIDE)
    val rootLeftMulOp = rootDivOp.leftOperand.getExpectedBinaryOperationWithOperator(MULTIPLY)
    val rootRightConstant = rootDivOp.rightOperand.getExpectedRationalConstant()
    val innerOpLeftConstant = rootLeftMulOp.leftOperand.getExpectedRationalConstant()
    val innerOpRightConstant = rootLeftMulOp.rightOperand.getExpectedRationalConstant()
    assertThat(rootRightConstant).isEqualTo(createWholeNumberFraction(2))
    assertThat(innerOpLeftConstant).isEqualTo(createWholeNumberFraction(1))
    assertThat(innerOpRightConstant).isEqualTo(createWholeNumberFraction(3))
  }

  @Test
  fun testParse_divideThenMultiply_haveLeftToRightAssociativity() {
    val result = MathExpressionParser.parseExpression("1/3*2", allowedVariables = listOf())

    // Left-to-right associativity means the first encountered binary op is done first, and the
    // second is done last (which means it's at the root). Expect the following tree:
    //    *
    //  /   2
    // 1 3
    val rootExpression = result.getExpectedSuccessfulExpression()
    val rootMulOp = rootExpression.getExpectedBinaryOperationWithOperator(MULTIPLY)
    val rootLeftDivOp = rootMulOp.leftOperand.getExpectedBinaryOperationWithOperator(DIVIDE)
    val rootRightConstant = rootMulOp.rightOperand.getExpectedRationalConstant()
    val innerOpLeftConstant = rootLeftDivOp.leftOperand.getExpectedRationalConstant()
    val innerOpRightConstant = rootLeftDivOp.rightOperand.getExpectedRationalConstant()
    assertThat(rootRightConstant).isEqualTo(createWholeNumberFraction(2))
    assertThat(innerOpLeftConstant).isEqualTo(createWholeNumberFraction(1))
    assertThat(innerOpRightConstant).isEqualTo(createWholeNumberFraction(3))
  }

  @Test
  fun testParse_twoExponentiations_haveRightToLeftAssociativity() {
    val result = MathExpressionParser.parseExpression("1^3^2", allowedVariables = listOf())

    // Right-to-left associativity means the opposite of left-to-right: always perform operations in
    // the opposite order encountered. Expect the following tree:
    //    ^
    //  1   ^
    //     3 2
    val rootExpression = result.getExpectedSuccessfulExpression()
    val rootExpOp = rootExpression.getExpectedBinaryOperationWithOperator(EXPONENTIATE)
    val rootLeftConstant = rootExpOp.leftOperand.getExpectedRationalConstant()
    val rootRightExpOp = rootExpOp.rightOperand.getExpectedBinaryOperationWithOperator(EXPONENTIATE)
    val innerOpLeftConstant = rootRightExpOp.leftOperand.getExpectedRationalConstant()
    val innerOpRightConstant = rootRightExpOp.rightOperand.getExpectedRationalConstant()
    assertThat(rootLeftConstant).isEqualTo(createWholeNumberFraction(1))
    assertThat(innerOpLeftConstant).isEqualTo(createWholeNumberFraction(3))
    assertThat(innerOpRightConstant).isEqualTo(createWholeNumberFraction(2))
  }

  @Test
  fun testParse_addThenMultiply_multiplyHappensFirst() {
    val result = MathExpressionParser.parseExpression("1+3*2", allowedVariables = listOf())

    // Operator precedence means ensure multiplication happens first, but keep the general order
    // of operands. Expect the following tree:
    //    +
    //  1   *
    //     3 2
    val rootExpression = result.getExpectedSuccessfulExpression()
    val rootAddOp = rootExpression.getExpectedBinaryOperationWithOperator(ADD)
    val rootLeftConstant = rootAddOp.leftOperand.getExpectedRationalConstant()
    val rootRightMulOp = rootAddOp.rightOperand.getExpectedBinaryOperationWithOperator(MULTIPLY)
    val innerOpLeftConstant = rootRightMulOp.leftOperand.getExpectedRationalConstant()
    val innerOpRightConstant = rootRightMulOp.rightOperand.getExpectedRationalConstant()
    assertThat(rootLeftConstant).isEqualTo(createWholeNumberFraction(1))
    assertThat(innerOpLeftConstant).isEqualTo(createWholeNumberFraction(3))
    assertThat(innerOpRightConstant).isEqualTo(createWholeNumberFraction(2))
  }

  @Test
  fun testParse_multiplyThenAdd_multiplyHappensFirst() {
    val result = MathExpressionParser.parseExpression("1*3+2", allowedVariables = listOf())

    // Operator precedence follows expression order. Expect the following tree:
    //    +
    //  *   2
    // 1 3
    val rootExpression = result.getExpectedSuccessfulExpression()
    val rootAddOp = rootExpression.getExpectedBinaryOperationWithOperator(ADD)
    val rootLeftMulOp = rootAddOp.leftOperand.getExpectedBinaryOperationWithOperator(MULTIPLY)
    val rootRightConstant = rootAddOp.rightOperand.getExpectedRationalConstant()
    val innerOpLeftConstant = rootLeftMulOp.leftOperand.getExpectedRationalConstant()
    val innerOpRightConstant = rootLeftMulOp.rightOperand.getExpectedRationalConstant()
    assertThat(rootRightConstant).isEqualTo(createWholeNumberFraction(2))
    assertThat(innerOpLeftConstant).isEqualTo(createWholeNumberFraction(1))
    assertThat(innerOpRightConstant).isEqualTo(createWholeNumberFraction(3))
  }

  @Test
  fun testParse_multiplyThenExponentaite_exponentiationHappensFirst() {
    val result = MathExpressionParser.parseExpression("1*3^2", allowedVariables = listOf())

    // Operator precedence means ensure exponentiation happens first, but keep the general order
    // of operands. Expect the following tree:
    //    *
    //  1   ^
    //     3 2
    val rootExpression = result.getExpectedSuccessfulExpression()
    val rootMulOp = rootExpression.getExpectedBinaryOperationWithOperator(MULTIPLY)
    val rootLeftConstant = rootMulOp.leftOperand.getExpectedRationalConstant()
    val rootRightExpOp = rootMulOp.rightOperand.getExpectedBinaryOperationWithOperator(EXPONENTIATE)
    val innerOpLeftConstant = rootRightExpOp.leftOperand.getExpectedRationalConstant()
    val innerOpRightConstant = rootRightExpOp.rightOperand.getExpectedRationalConstant()
    assertThat(rootLeftConstant).isEqualTo(createWholeNumberFraction(1))
    assertThat(innerOpLeftConstant).isEqualTo(createWholeNumberFraction(3))
    assertThat(innerOpRightConstant).isEqualTo(createWholeNumberFraction(2))
  }

  @Test
  fun testParse_exponentiateThenMultiply_exponentiationHappensFirst() {
    val result = MathExpressionParser.parseExpression("1^3*2", allowedVariables = listOf())

    // Operator precedence follows expression order. Expect the following tree:
    //    *
    //  ^   2
    // 1 3
    val rootExpression = result.getExpectedSuccessfulExpression()
    val rootMulOp = rootExpression.getExpectedBinaryOperationWithOperator(MULTIPLY)
    val rootLeftExpOp = rootMulOp.leftOperand.getExpectedBinaryOperationWithOperator(EXPONENTIATE)
    val rootRightConstant = rootMulOp.rightOperand.getExpectedRationalConstant()
    val innerOpLeftConstant = rootLeftExpOp.leftOperand.getExpectedRationalConstant()
    val innerOpRightConstant = rootLeftExpOp.rightOperand.getExpectedRationalConstant()
    assertThat(rootRightConstant).isEqualTo(createWholeNumberFraction(2))
    assertThat(innerOpLeftConstant).isEqualTo(createWholeNumberFraction(1))
    assertThat(innerOpRightConstant).isEqualTo(createWholeNumberFraction(3))
  }

  @Test
  fun testParse_addThenMultiply_withParentheses_addHappensFirst() {
    val result = MathExpressionParser.parseExpression("(1+3)*2", allowedVariables = listOf())

    // Parentheses override operator precedence so that addition happens first. Expect the following
    // tree:
    //    *
    //  +   2
    // 1 3
    val rootExpression = result.getExpectedSuccessfulExpression()
    val rootMulOp = rootExpression.getExpectedBinaryOperationWithOperator(MULTIPLY)
    val rootLeftAddOp = rootMulOp.leftOperand.getExpectedBinaryOperationWithOperator(ADD)
    val rootRightConstant = rootMulOp.rightOperand.getExpectedRationalConstant()
    val innerOpLeftConstant = rootLeftAddOp.leftOperand.getExpectedRationalConstant()
    val innerOpRightConstant = rootLeftAddOp.rightOperand.getExpectedRationalConstant()
    assertThat(rootRightConstant).isEqualTo(createWholeNumberFraction(2))
    assertThat(innerOpLeftConstant).isEqualTo(createWholeNumberFraction(1))
    assertThat(innerOpRightConstant).isEqualTo(createWholeNumberFraction(3))
  }

  @Test
  fun testParse_withNestedParentheses_deeperParenthesesEvaluateFirst() {
    val result = MathExpressionParser.parseExpression("1^(2*(1-x))", allowedVariables = listOf("x"))

    // Nested parentheses happen before earlier parentheses. Expect the following tree:
    //   ^
    // 1    *
    //   2    -
    //      1   x
    val rootExpression = result.getExpectedSuccessfulExpression()
    val rootExpOp = rootExpression.getExpectedBinaryOperationWithOperator(EXPONENTIATE)
    val rootLeftConstant = rootExpOp.leftOperand.getExpectedRationalConstant()
    val rootRightMulOp = rootExpOp.rightOperand.getExpectedBinaryOperationWithOperator(MULTIPLY)
    val mulLeftConstant = rootRightMulOp.leftOperand.getExpectedRationalConstant()
    val mulRightSubOp = rootRightMulOp.rightOperand.getExpectedBinaryOperationWithOperator(SUBTRACT)
    val subLeftConstant = mulRightSubOp.leftOperand.getExpectedRationalConstant()
    val subRightVar = mulRightSubOp.rightOperand.getExpectedVariable()
    assertThat(rootLeftConstant).isEqualTo(createWholeNumberFraction(1))
    assertThat(mulLeftConstant).isEqualTo(createWholeNumberFraction(2))
    assertThat(subLeftConstant).isEqualTo(createWholeNumberFraction(1))
    assertThat(subRightVar).isEqualTo("x")
  }

  @Test
  fun testParse_combineMultiplicationSubtractionAndNegation_negationHasHighestPrecedence() {
    // See: https://wcipeg.com/wiki/Shunting_yard_algorithm#Unary_operators. Unary negation is
    // usually treated as higher precedence.
    val result = MathExpressionParser.parseExpression("10/-1*-2", allowedVariables = listOf())

    // Expected tree:
    //       *
    //    /      -
    // 10   -    2
    //      1
    val rootExpression = result.getExpectedSuccessfulExpression()
    val mulOp = rootExpression.getExpectedBinaryOperationWithOperator(MULTIPLY)
    val divOp = mulOp.leftOperand.getExpectedBinaryOperationWithOperator(DIVIDE)
    val rightMulNegateOp = mulOp.rightOperand.getExpectedUnaryOperationWithOperator(NEGATE)
    val rightMulNegatedConstant = rightMulNegateOp.operand.getExpectedRationalConstant()
    val leftDivConstant = divOp.leftOperand.getExpectedRationalConstant()
    val rightDivNegateOp = divOp.rightOperand.getExpectedUnaryOperationWithOperator(NEGATE)
    val rightDivNegatedConstant = rightDivNegateOp.operand.getExpectedRationalConstant()
    assertThat(rightMulNegatedConstant).isEqualTo(createWholeNumberFraction(2))
    assertThat(leftDivConstant).isEqualTo(createWholeNumberFraction(10))
    assertThat(rightDivNegatedConstant).isEqualTo(createWholeNumberFraction(1))
  }
  @Test
  fun test_negationWithExponentiation_exponentiationHasHigherPrecedence() {
    val result = MathExpressionParser.parseExpression("-2^3^4", allowedVariables = listOf())

    // Expected tree (note negation happens last since it's lower precedence):
    //       -
    //       ^
    //     2    ^
    //        3   4
    val rootExpression = result.getExpectedSuccessfulExpression()
    val negOp = rootExpression.getExpectedUnaryOperationWithOperator(NEGATE)
    val secondExpOp = negOp.operand.getExpectedBinaryOperationWithOperator(EXPONENTIATE)
    val secondExpLeftConstant = secondExpOp.leftOperand.getExpectedRationalConstant()
    val firstExpOp = secondExpOp.rightOperand.getExpectedBinaryOperationWithOperator(EXPONENTIATE)
    val firstExpLeftConstant = firstExpOp.leftOperand.getExpectedRationalConstant()
    val firstExpRightConstant = firstExpOp.rightOperand.getExpectedRationalConstant()
    assertThat(secondExpLeftConstant).isEqualTo(createWholeNumberFraction(2))
    assertThat(firstExpLeftConstant).isEqualTo(createWholeNumberFraction(3))
    assertThat(firstExpRightConstant).isEqualTo(createWholeNumberFraction(4))
  }

  @Test
  fun testParse_complexExpression_followsPemdasWithAssociativity() {
    val result = MathExpressionParser.parseExpression(
      "1+(13+12)/15+3*2-7/4^2^3*x+-(2*(y-3.14))",
      allowedVariables = listOf("x", "y")
    )

    // Look at past tests for associativity & precedence rules that are repeated here. Expect the
    // following tree:
    //                                                           +
    //                                      -                                 - (negation)
    //                     +                           *                      *
    //       +                    *              /        x                2      -
    //  1          /            3   2       7        ^                          y   3.14
    //          +    15                           4     ^
    //       13   12                                  2   3
    // Note that the enumeration below isn't done in evaluation order. The variables below are also
    // preferring a leftward-leaning breadth-first enumeration.
    val rootExpression = result.getExpectedSuccessfulExpression()
    // Parse the root level: addition.
    val lvl1Elem1Op = rootExpression.getExpectedBinaryOperationWithOperator(ADD)
    // Next level: subtraction & negation.
    val lvl2Elem1Op = lvl1Elem1Op.leftOperand.getExpectedBinaryOperationWithOperator(SUBTRACT)
    val lvl2Elem2Op = lvl1Elem1Op.rightOperand.getExpectedUnaryOperationWithOperator(NEGATE)
    // Next level: addition, multiplication, and multiplication.
    val lvl3Elem1Op = lvl2Elem1Op.leftOperand.getExpectedBinaryOperationWithOperator(ADD)
    val lvl3Elem2Op = lvl2Elem1Op.rightOperand.getExpectedBinaryOperationWithOperator(MULTIPLY)
    val lvl3Elem3Op = lvl2Elem2Op.operand.getExpectedBinaryOperationWithOperator(MULTIPLY)
    // Next level: addition, multiplication, division, variable x, constant 2, subtraction.
    val lvl4Elem1Op = lvl3Elem1Op.leftOperand.getExpectedBinaryOperationWithOperator(ADD)
    val lvl4Elem2Op = lvl3Elem1Op.rightOperand.getExpectedBinaryOperationWithOperator(MULTIPLY)
    val lvl4Elem3Op = lvl3Elem2Op.leftOperand.getExpectedBinaryOperationWithOperator(DIVIDE)
    val lvl4Elem4Var = lvl3Elem2Op.rightOperand.getExpectedVariable()
    val lvl4Elem5Const = lvl3Elem3Op.leftOperand.getExpectedRationalConstant()
    val lvl4Elem6Op = lvl3Elem3Op.rightOperand.getExpectedBinaryOperationWithOperator(SUBTRACT)
    // Next level: 1, division, 3, 2, 7, exponentiation, y, 3.14.
    val lvl5Elem1Const = lvl4Elem1Op.leftOperand.getExpectedRationalConstant()
    val lvl5Elem2Op = lvl4Elem1Op.rightOperand.getExpectedBinaryOperationWithOperator(DIVIDE)
    val lvl5Elem3Const = lvl4Elem2Op.leftOperand.getExpectedRationalConstant()
    val lvl5Elem4Const = lvl4Elem2Op.rightOperand.getExpectedRationalConstant()
    val lvl5Elem5Const = lvl4Elem3Op.leftOperand.getExpectedRationalConstant()
    val lvl5Elem6Op = lvl4Elem3Op.rightOperand.getExpectedBinaryOperationWithOperator(EXPONENTIATE)
    val lvl5Elem7Var = lvl4Elem6Op.leftOperand.getExpectedVariable()
    val lvl5Elem8Const = lvl4Elem6Op.rightOperand.getExpectedIrrationalConstant()
    // Next level: addition, 15, 4, exponentiation
    val lvl6Elem1Op = lvl5Elem2Op.leftOperand.getExpectedBinaryOperationWithOperator(ADD)
    val lvl6Elem2Const = lvl5Elem2Op.rightOperand.getExpectedRationalConstant()
    val lvl6Elem3Const = lvl5Elem6Op.leftOperand.getExpectedRationalConstant()
    val lvl6Elem4Op = lvl5Elem6Op.rightOperand.getExpectedBinaryOperationWithOperator(EXPONENTIATE)
    // Final level: 13, 12, 2, 3
    val lvl7Elem1Const = lvl6Elem1Op.leftOperand.getExpectedRationalConstant()
    val lvl7Elem2Const = lvl6Elem1Op.rightOperand.getExpectedRationalConstant()
    val lvl7Elem3Const = lvl6Elem4Op.leftOperand.getExpectedRationalConstant()
    val lvl7Elem4Const = lvl6Elem4Op.rightOperand.getExpectedRationalConstant()

    // Now, verify the constants and variables (cross reference with the level comments above & the
    // tree to verify).
    assertThat(lvl4Elem4Var).isEqualTo("x")
    assertThat(lvl4Elem5Const).isEqualTo(createWholeNumberFraction(2))
    assertThat(lvl5Elem1Const).isEqualTo(createWholeNumberFraction(1))
    assertThat(lvl5Elem3Const).isEqualTo(createWholeNumberFraction(3))
    assertThat(lvl5Elem4Const).isEqualTo(createWholeNumberFraction(2))
    assertThat(lvl5Elem5Const).isEqualTo(createWholeNumberFraction(7))
    assertThat(lvl5Elem7Var).isEqualTo("y")
    assertThat(lvl5Elem8Const).isWithin(1e-5).of(3.14)
    assertThat(lvl6Elem2Const).isEqualTo(createWholeNumberFraction(15))
    assertThat(lvl6Elem3Const).isEqualTo(createWholeNumberFraction(4))
    assertThat(lvl7Elem1Const).isEqualTo(createWholeNumberFraction(13))
    assertThat(lvl7Elem2Const).isEqualTo(createWholeNumberFraction(12))
    assertThat(lvl7Elem3Const).isEqualTo(createWholeNumberFraction(2))
    assertThat(lvl7Elem4Const).isEqualTo(createWholeNumberFraction(3))
  }

  @Test
  fun testParse_twoShortVariables_withoutOperator_impliesMultiplication() {
    val result = MathExpressionParser.parseExpression("xy", allowedVariables = listOf("x", "y"))

    // Having two variables right next to each other implies multiplication.
    val rootExpression = result.getExpectedSuccessfulExpression()
    val mulOp = rootExpression.getExpectedBinaryOperationWithOperator(MULTIPLY)
    val leftVar = mulOp.leftOperand.getExpectedVariable()
    val rightVar = mulOp.rightOperand.getExpectedVariable()
    assertThat(leftVar).isEqualTo("x")
    assertThat(rightVar).isEqualTo("y")
  }

  @Test
  fun testParse_constantWithShortVariable_withoutOperator_impliesMultiplication() {
    val result = MathExpressionParser.parseExpression("2x", allowedVariables = listOf("x"))

    // Having a constant and a variable right next to each other implies multiplication.
    val rootExpression = result.getExpectedSuccessfulExpression()
    val mulOp = rootExpression.getExpectedBinaryOperationWithOperator(MULTIPLY)
    val leftConstant = mulOp.leftOperand.getExpectedRationalConstant()
    val rightVar = mulOp.rightOperand.getExpectedVariable()
    assertThat(leftConstant).isEqualTo(createWholeNumberFraction(2))
    assertThat(rightVar).isEqualTo("x")
  }

  @Test
  fun testParse_constantWithLongVariable_withoutOperator_impliesMultiplication() {
    val result = MathExpressionParser.parseExpression(
      "2lambda", allowedVariables = listOf("lambda")
    )

    // Having a constant and a variable right next to each other implies multiplication.
    val rootExpression = result.getExpectedSuccessfulExpression()
    val mulOp = rootExpression.getExpectedBinaryOperationWithOperator(MULTIPLY)
    val leftConstant = mulOp.leftOperand.getExpectedRationalConstant()
    val rightVar = mulOp.rightOperand.getExpectedVariable()
    assertThat(leftConstant).isEqualTo(createWholeNumberFraction(2))
    assertThat(rightVar).isEqualTo("lambda")
  }

  @Test
  fun testParse_constantWithMultipleVariables_ambiguous_withoutOperator_impliesLongVarMult() {
    val result = MathExpressionParser.parseExpression(
      "2xyz", allowedVariables = listOf("x", "y", "z", "xyz")
    )

    // The implied multiplication here is always on long variable since it's otherwise ambiguous.
    val rootExpression = result.getExpectedSuccessfulExpression()
    val mulOp = rootExpression.getExpectedBinaryOperationWithOperator(MULTIPLY)
    val leftConstant = mulOp.leftOperand.getExpectedRationalConstant()
    val rightVar = mulOp.rightOperand.getExpectedVariable()
    assertThat(leftConstant).isEqualTo(createWholeNumberFraction(2))
    assertThat(rightVar).isEqualTo("xyz")
  }

  @Test
  fun testParse_shortAndLongVariable_withoutOperator_failsToParse() {
    val result = MathExpressionParser.parseExpression(
      "wxyz", allowedVariables = listOf("w", "xyz")
    )

    // There can't be implied multiplication here since 'wxyz' looks like 1 variable. Note that if
    // 'x', 'y', 'z' are separate allowed variables then the above *will* succeed in parsing an
    // expression equivalent to w*x*y*z.
    val failureReason = result.getExpectedFailedExpression()
    assertThat(failureReason).contains("Encountered invalid identifier: wxyz")
  }

  @Test
  fun testParse_variableNextToConstant_withoutOperator_impliesMultiplication() {
    val result = MathExpressionParser.parseExpression("x2", allowedVariables = listOf("x"))

    // Having a constant and a variable right next to each other implies multiplication.
    val rootExpression = result.getExpectedSuccessfulExpression()
    val mulOp = rootExpression.getExpectedBinaryOperationWithOperator(MULTIPLY)
    val leftVar = mulOp.leftOperand.getExpectedVariable()
    val rightConstant = mulOp.rightOperand.getExpectedRationalConstant()
    assertThat(leftVar).isEqualTo("x")
    assertThat(rightConstant).isEqualTo(createWholeNumberFraction(2))
  }

  @Test
  fun testParse_polynomialWithoutOperators_createsCorrectExpressionTree() {
    val result = MathExpressionParser.parseExpression("2x^-2", allowedVariables = listOf("x"))

    // Basic polynomial expressions should parse as expected. For the above expression, expect the
    // tree:
    //   *
    // 2   ^
    //   x   -
    //       2
    // Having a constant and a variable right next to each other implies multiplication.
    val rootExpression = result.getExpectedSuccessfulExpression()
    val mulOp = rootExpression.getExpectedBinaryOperationWithOperator(MULTIPLY)
    val leftMulConstant = mulOp.leftOperand.getExpectedRationalConstant()
    val rightExpOp = mulOp.rightOperand.getExpectedBinaryOperationWithOperator(EXPONENTIATE)
    val leftExpVar = rightExpOp.leftOperand.getExpectedVariable()
    val rightExpNegOp = rightExpOp.rightOperand.getExpectedUnaryOperationWithOperator(NEGATE)
    val negOpConst = rightExpNegOp.operand.getExpectedRationalConstant()
    assertThat(leftMulConstant).isEqualTo(createWholeNumberFraction(2))
    assertThat(leftExpVar).isEqualTo("x")
    assertThat(negOpConst).isEqualTo(createWholeNumberFraction(2))
  }

  @Test
  fun testParse_multipleShortVariables_withoutOperators_impliesMultipleMultiplications() {
    val result =
      MathExpressionParser.parseExpression("xyz", allowedVariables = listOf("x", "y", "z"))

    // Having consecutive variables also implies multiplication. In this case, the expression uses
    // left associativity, so expect the following tree:
    //       *
    //   *     z
    // x   y
    val rootExpression = result.getExpectedSuccessfulExpression()
    val secondMulOp = rootExpression.getExpectedBinaryOperationWithOperator(MULTIPLY)
    val firstMulOp = secondMulOp.leftOperand.getExpectedBinaryOperationWithOperator(MULTIPLY)
    val secondMulRightVar = secondMulOp.rightOperand.getExpectedVariable()
    val firstMulLeftVar = firstMulOp.leftOperand.getExpectedVariable()
    val firstMulRightVar = firstMulOp.rightOperand.getExpectedVariable()
    assertThat(secondMulRightVar).isEqualTo("z")
    assertThat(firstMulLeftVar).isEqualTo("x")
    assertThat(firstMulRightVar).isEqualTo("y")
  }

  @Test
  fun testParse_shortVariables_withAmbiguousLongVariable_noOperators_impliesSingleVariable() {
    val result = MathExpressionParser.parseExpression(
      "xyz", allowedVariables = listOf("x", "y", "z", "xyz")
    )

    // 'xyz' is ambiguous in this case, but a single variable should be preferred since it's an
    // exact match.
    val rootExpression = result.getExpectedSuccessfulExpression()
    val rootVar = rootExpression.getExpectedVariable()
    assertThat(rootVar).isEqualTo("xyz")
  }

  @Test
  fun testParse_shortVariables_withAmbiguousLongVariable_withOperator_hasMultipleVariables() {
    val result = MathExpressionParser.parseExpression(
      "x*yz", allowedVariables = listOf("x", "y", "z", "xyz")
    )

    // Unlike the above test, the single operator is sufficient to disambiguate the the x, y, z vs.
    // xyz variable dilemma, so expect the following tree:
    //       *
    //   *     z
    // x   y
    val rootExpression = result.getExpectedSuccessfulExpression()
    val secondMulOp = rootExpression.getExpectedBinaryOperationWithOperator(MULTIPLY)
    val firstMulOp = secondMulOp.leftOperand.getExpectedBinaryOperationWithOperator(MULTIPLY)
    val secondMulRightVar = secondMulOp.rightOperand.getExpectedVariable()
    val firstMulLeftVar = firstMulOp.leftOperand.getExpectedVariable()
    val firstMulRightVar = firstMulOp.rightOperand.getExpectedVariable()
    assertThat(secondMulRightVar).isEqualTo("z")
    assertThat(firstMulLeftVar).isEqualTo("x")
    assertThat(firstMulRightVar).isEqualTo("y")
  }

  @Test
  fun testParse_polynomialMultiplication_withoutOperator_impliesMultiplication() {
    val result = MathExpressionParser.parseExpression("(x+1)(x+2)", allowedVariables = listOf("x"))

    // Having two polynomials right next to each other implies multiplication. Expect the tree:
    //       *
    //   +       +
    // x   1   x   2
    val rootExpression = result.getExpectedSuccessfulExpression()
    val mulOp = rootExpression.getExpectedBinaryOperationWithOperator(MULTIPLY)
    val leftAddOp = mulOp.leftOperand.getExpectedBinaryOperationWithOperator(ADD)
    val rightAddOp = mulOp.rightOperand.getExpectedBinaryOperationWithOperator(ADD)
    val leftAddLeftVar = leftAddOp.leftOperand.getExpectedVariable()
    val leftAddRightVar = leftAddOp.rightOperand.getExpectedRationalConstant()
    val rightAddLeftVar = rightAddOp.leftOperand.getExpectedVariable()
    val rightAddRightVar = rightAddOp.rightOperand.getExpectedRationalConstant()
    assertThat(leftAddLeftVar).isEqualTo("x")
    assertThat(leftAddRightVar).isEqualTo(createWholeNumberFraction(1))
    assertThat(rightAddLeftVar).isEqualTo("x")
    assertThat(rightAddRightVar).isEqualTo(createWholeNumberFraction(2))
  }

  @Test
  fun testParse_constantAndPolynomialMultiplication_withoutOperator_impliesMultiplication() {
    val result = MathExpressionParser.parseExpression("2(x+1)", allowedVariables = listOf("x"))

    // Having a constant and a polynomial right next to each other implies multiplication. Expect:
    //       *
    //   2       +
    //         x   1
    val rootExpression = result.getExpectedSuccessfulExpression()
    val mulOp = rootExpression.getExpectedBinaryOperationWithOperator(MULTIPLY)
    val leftConstant = mulOp.leftOperand.getExpectedRationalConstant()
    val rightAddOp = mulOp.rightOperand.getExpectedBinaryOperationWithOperator(ADD)
    val rightAddLeftVar = rightAddOp.leftOperand.getExpectedVariable()
    val rightAddRightConstant = rightAddOp.rightOperand.getExpectedRationalConstant()
    assertThat(leftConstant).isEqualTo(createWholeNumberFraction(2))
    assertThat(rightAddLeftVar).isEqualTo("x")
    assertThat(rightAddRightConstant).isEqualTo(createWholeNumberFraction(1))
  }

  @Test
  fun testParse_polynomialAndConstantMultiplication_withoutOperator_impliesMultiplication() {
    val result = MathExpressionParser.parseExpression("(x+1)2", allowedVariables = listOf("x"))

    // Having a constant and a polynomial right next to each other implies multiplication. Expect:
    //       *
    //   +     2
    // x   1
    val rootExpression = result.getExpectedSuccessfulExpression()
    val mulOp = rootExpression.getExpectedBinaryOperationWithOperator(MULTIPLY)
    val leftAddOp = mulOp.leftOperand.getExpectedBinaryOperationWithOperator(ADD)
    val rightConstant = mulOp.rightOperand.getExpectedRationalConstant()
    val leftAddLeftVar = leftAddOp.leftOperand.getExpectedVariable()
    val leftAddRightConstant = leftAddOp.rightOperand.getExpectedRationalConstant()
    assertThat(rightConstant).isEqualTo(createWholeNumberFraction(2))
    assertThat(leftAddLeftVar).isEqualTo("x")
    assertThat(leftAddRightConstant).isEqualTo(createWholeNumberFraction(1))
  }

  @Test
  fun testParse_variableAndPolynomialMultiplication_withoutOperator_impliesMultiplication() {
    val result = MathExpressionParser.parseExpression("x(x+1)", allowedVariables = listOf("x"))

    // Having a constant and a polynomial right next to each other implies multiplication. Expect:
    //       *
    //   x       +
    //         x   1
    val rootExpression = result.getExpectedSuccessfulExpression()
    val mulOp = rootExpression.getExpectedBinaryOperationWithOperator(MULTIPLY)
    val leftVar = mulOp.leftOperand.getExpectedVariable()
    val rightAddOp = mulOp.rightOperand.getExpectedBinaryOperationWithOperator(ADD)
    val rightAddLeftVar = rightAddOp.leftOperand.getExpectedVariable()
    val rightAddRightConstant = rightAddOp.rightOperand.getExpectedRationalConstant()
    assertThat(leftVar).isEqualTo("x")
    assertThat(rightAddLeftVar).isEqualTo("x")
    assertThat(rightAddRightConstant).isEqualTo(createWholeNumberFraction(1))
  }

  @Test
  fun testParse_polynomialAndVariableMultiplication_withoutOperator_impliesMultiplication() {
    val result = MathExpressionParser.parseExpression("(x+1)x", allowedVariables = listOf("x"))

    // Having a constant and a polynomial right next to each other implies multiplication. Expect:
    //       *
    //   +     2
    // x   1
    val rootExpression = result.getExpectedSuccessfulExpression()
    val mulOp = rootExpression.getExpectedBinaryOperationWithOperator(MULTIPLY)
    val leftAddOp = mulOp.leftOperand.getExpectedBinaryOperationWithOperator(ADD)
    val rightVar = mulOp.rightOperand.getExpectedVariable()
    val leftAddLeftVar = leftAddOp.leftOperand.getExpectedVariable()
    val leftAddRightConstant = leftAddOp.rightOperand.getExpectedRationalConstant()
    assertThat(rightVar).isEqualTo("x")
    assertThat(leftAddLeftVar).isEqualTo("x")
    assertThat(leftAddRightConstant).isEqualTo(createWholeNumberFraction(1))
  }

  @Test
  fun testParse_emptyLiteral_failsToResolveTree() {
    val result = MathExpressionParser.parseExpression("", allowedVariables = listOf())

    val failureReason = result.getExpectedFailedExpression()
    assertThat(failureReason).contains("Failed to resolve expression tree")
  }

  @Test
  fun testParse_twoConsecutiveConstants_failsToResolveTree() {
    val result = MathExpressionParser.parseExpression("2 3", allowedVariables = listOf())

    val failureReason = result.getExpectedFailedExpression()
    assertThat(failureReason).contains("Failed to resolve expression tree")
  }

  @Test
  fun testParse_twoConsecutiveConstants_withParentheses_impliesMultiplication() {
    val result = MathExpressionParser.parseExpression("(2) (3)", allowedVariables = listOf())

    // Parentheses fully change the meaning since it now looks like polynomial multiplication.
    val rootExpression = result.getExpectedSuccessfulExpression()
    val mulOp = rootExpression.getExpectedBinaryOperationWithOperator(MULTIPLY)
    val leftConstant = mulOp.leftOperand.getExpectedRationalConstant()
    val rightConstant = mulOp.rightOperand.getExpectedRationalConstant()
    assertThat(leftConstant).isEqualTo(createWholeNumberFraction(2))
    assertThat(rightConstant).isEqualTo(createWholeNumberFraction(3))
  }

  @Test
  fun testParse_mismatchedOpenParenthesis_failsWithUnresolvedParenthesis() {
    val result = MathExpressionParser.parseExpression("(", allowedVariables = listOf())

    val failureReason = result.getExpectedFailedExpression()
    assertThat(failureReason).contains("unexpected open parenthesis")
  }

  @Test
  fun testParse_extraOpenParenthesis_failsWithUnresolvedParenthesis() {
    val result = MathExpressionParser.parseExpression("((2)", allowedVariables = listOf())

    val failureReason = result.getExpectedFailedExpression()
    assertThat(failureReason).contains("unexpected open parenthesis")
  }

  @Test
  fun testParse_mismatchedCloseParenthesis_failsWithUnresolvedParenthesis() {
    val result = MathExpressionParser.parseExpression(")", allowedVariables = listOf())

    val failureReason = result.getExpectedFailedExpression()
    assertThat(failureReason).contains("unexpected close parenthesis")
  }

  @Test
  fun testParse_extraCloseParenthesis_failsWithUnresolvedParenthesis() {
    val result = MathExpressionParser.parseExpression("(2))", allowedVariables = listOf())

    val failureReason = result.getExpectedFailedExpression()
    assertThat(failureReason).contains("unexpected close parenthesis")
  }

  @Test
  fun testParse_mismatchedCloseParenthesis_failsToResolveTree() {
    val result = MathExpressionParser.parseExpression("()", allowedVariables = listOf())

    val failureReason = result.getExpectedFailedExpression()
    assertThat(failureReason).contains("Failed to resolve expression tree")
  }

  @Test
  fun testParse_twoConsecutiveBinaryOperators_failsWithMissingBinaryOperand() {
    val result = MathExpressionParser.parseExpression("2**3", allowedVariables = listOf())

    val failureReason = result.getExpectedFailedExpression()
    assertThat(failureReason).contains("Encountered binary operator with missing operand(s)")
  }

  @Test
  fun testParse_binaryOperator_missingRightOperand_failsWithMissingBinaryOperand() {
    val result = MathExpressionParser.parseExpression("2*", allowedVariables = listOf())

    val failureReason = result.getExpectedFailedExpression()
    assertThat(failureReason).contains("Encountered binary operator with missing operand(s)")
  }

  @Test
  fun testParse_binaryOperator_missingLeftOperand_failsWithMissingBinaryOperand() {
    val result = MathExpressionParser.parseExpression("*2", allowedVariables = listOf())

    val failureReason = result.getExpectedFailedExpression()
    assertThat(failureReason).contains("Encountered binary operator with missing operand(s)")
  }

  @Test
  fun testParse_binaryOperator_missingBothOperands_failsWithMissingBinaryOperand() {
    val result = MathExpressionParser.parseExpression("*", allowedVariables = listOf())

    val failureReason = result.getExpectedFailedExpression()
    assertThat(failureReason).contains("Encountered binary operator with missing operand(s)")
  }

  @Test
  fun testParse_negation_missingOperand_failsWithMissingUnaryOperand() {
    val result = MathExpressionParser.parseExpression("-", allowedVariables = listOf())

    val failureReason = result.getExpectedFailedExpression()
    assertThat(failureReason).contains("Encountered unary operator without operand")
  }

  @Test
  fun testParse_unknownUnarySymbol_failsWithUnexpectedSymbol() {
    val result = MathExpressionParser.parseExpression("3!", allowedVariables = listOf())

    val failureReason = result.getExpectedFailedExpression()
    assertThat(failureReason).contains("unexpected symbol")
  }

  @Test
  fun testParse_unknownBinarySymbol_failsWithUnexpectedSymbol() {
    val result = MathExpressionParser.parseExpression("2%3", allowedVariables = listOf())

    val failureReason = result.getExpectedFailedExpression()
    assertThat(failureReason).contains("unexpected symbol")
  }

  @Test
  fun testParse_withInvalidAllowedVariable_throwsException() {
    val exception = assertThrows(IllegalArgumentException::class) {
      MathExpressionParser.parseExpression("", allowedVariables = listOf("invalid!"))
    }

    assertThat(exception).hasMessageThat().contains("contains non-letters")
  }

  @Test
  fun testParse_withEmptyAllowedVariable_throwsException() {
    val exception = assertThrows(IllegalArgumentException::class) {
      MathExpressionParser.parseExpression("", allowedVariables = listOf(""))
    }

    assertThat(exception).hasMessageThat().contains("empty identifier")
  }

  @Test
  fun testParse_expressionWithUndefinedVariable_failsWithUnexpectedIdentifier() {
    val result = MathExpressionParser.parseExpression("x", allowedVariables = listOf())

    val failureReason = result.getExpectedFailedExpression()
    assertThat(failureReason).contains("invalid identifier: x")
  }

  @Test
  fun testParse_expressionWithInvalidCharacter_failsWithUnexpectedSymbol() {
    val result = MathExpressionParser.parseExpression("∫", allowedVariables = listOf())

    val failureReason = result.getExpectedFailedExpression()
    assertThat(failureReason).contains("unexpected symbol")
  }

  private fun ParseResult.getExpectedSuccessfulExpression(): MathExpression {
    assertThat(this).isInstanceOf(Success::class.java)
    return (this as Success).mathExpression
  }

  private fun ParseResult.getExpectedFailedExpression(): String {
    assertThat(this).isInstanceOf(Failure::class.java)
    return (this as Failure).failureReason
  }

  private fun MathExpression.getExpectedConstant(): Real {
    return getExpectedType(MathExpression::getConstant, CONSTANT)
  }

  private fun MathExpression.getExpectedRationalConstant(): Fraction {
    val constant = getExpectedType(MathExpression::getConstant, CONSTANT)
    assertThat(constant.realTypeCase).isEqualTo(RATIONAL)
    return constant.rational
  }

  private fun MathExpression.getExpectedIrrationalConstant(): Double {
    val constant = getExpectedType(MathExpression::getConstant, CONSTANT)
    assertThat(constant.realTypeCase).isEqualTo(IRRATIONAL)
    return constant.irrational
  }

  private fun MathExpression.getExpectedVariable(): String {
    return getExpectedType(MathExpression::getVariable, VARIABLE)
  }

  private fun MathExpression.getExpectedUnaryOperation(): MathUnaryOperation {
    return getExpectedType(MathExpression::getUnaryOperation, UNARY_OPERATION)
  }

  private fun MathExpression.getExpectedUnaryOperationWithOperator(
    operator: MathUnaryOperation.Operator
  ): MathUnaryOperation {
    val expectedOp = getExpectedType(MathExpression::getUnaryOperation, UNARY_OPERATION)
    assertThat(expectedOp.operator).isEqualTo(operator)
    return expectedOp
  }

  private fun MathExpression.getExpectedBinaryOperation(): MathBinaryOperation {
    return getExpectedType(MathExpression::getBinaryOperation, BINARY_OPERATION)
  }

  private fun MathExpression.getExpectedBinaryOperationWithOperator(
    operator: MathBinaryOperation.Operator
  ): MathBinaryOperation {
    val expectedOp = getExpectedType(MathExpression::getBinaryOperation, BINARY_OPERATION)
    assertThat(expectedOp.operator).isEqualTo(operator)
    return expectedOp
  }

  private fun <T> MathExpression.getExpectedType(
    typeRetriever: MathExpression.() -> T,
    expectedType: ExpressionTypeCase
  ): T {
    assertThat(expressionTypeCase).isEqualTo(expectedType)
    return typeRetriever()
  }

  private fun createWholeNumberFraction(value: Int): Fraction {
    return Fraction.newBuilder().setWholeNumber(value).setDenominator(1).build()
  }
}
