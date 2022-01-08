package org.oppia.android.util.math

import org.oppia.android.app.model.ComparableOperationList
import org.oppia.android.app.model.MathEquation
import org.oppia.android.app.model.MathExpression
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.BINARY_OPERATION
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.CONSTANT
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.EXPRESSIONTYPE_NOT_SET
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.FUNCTION_CALL
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.GROUP
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.UNARY_OPERATION
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.VARIABLE
import org.oppia.android.app.model.Polynomial
import org.oppia.android.app.model.Real
import org.oppia.android.util.math.ExpressionToComparableOperationListConverter.Companion.toComparable
import org.oppia.android.util.math.ExpressionToLatexConverter.Companion.convertToLatex
import org.oppia.android.util.math.ExpressionToPolynomialConverter.Companion.reduceToPolynomial
import org.oppia.android.util.math.NumericExpressionEvaluator.Companion.evaluate

fun MathEquation.toRawLatex(divAsFraction: Boolean): String = convertToLatex(divAsFraction)

fun MathExpression.toRawLatex(divAsFraction: Boolean): String = convertToLatex(divAsFraction)

fun MathExpression.evaluateAsNumericExpression(): Real? = evaluate()

fun MathExpression.toComparableOperationList(): ComparableOperationList =
  stripGroups().toComparable()

fun MathExpression.toPolynomial(): Polynomial? = stripGroups().reduceToPolynomial()

/**
 * Returns whether this [MathExpression] approximately equals another, that is, that it fully
 * matches in its AST representation but all constants are compared using
 * [Real.approximatelyEquals]. Further, this does not check parser markers when considering
 * equivalence.
 */
fun MathExpression.approximatelyEquals(other: MathExpression): Boolean {
  if (expressionTypeCase != other.expressionTypeCase) return false
  return when (expressionTypeCase) {
    CONSTANT -> constant.approximatelyEquals(other.constant)
    VARIABLE -> variable == other.variable
    BINARY_OPERATION -> {
      binaryOperation.operator == other.binaryOperation.operator
        && binaryOperation.leftOperand.approximatelyEquals(other.binaryOperation.leftOperand)
        && binaryOperation.rightOperand.approximatelyEquals(other.binaryOperation.rightOperand)
    }
    UNARY_OPERATION -> {
      unaryOperation.operator == other.unaryOperation.operator
        && unaryOperation.operand.approximatelyEquals(other.unaryOperation.operand)
    }
    FUNCTION_CALL -> {
      functionCall.functionType == other.functionCall.functionType
        && functionCall.argument.approximatelyEquals(other.functionCall.argument)
    }
    GROUP -> group.approximatelyEquals(other.group)
    EXPRESSIONTYPE_NOT_SET, null -> true
  }
}

private fun MathExpression.stripGroups(): MathExpression {
  return when (expressionTypeCase) {
    BINARY_OPERATION -> toBuilder().apply {
      binaryOperation = binaryOperation.toBuilder().apply {
        leftOperand = binaryOperation.leftOperand.stripGroups()
        rightOperand = binaryOperation.rightOperand.stripGroups()
      }.build()
    }.build()
    UNARY_OPERATION -> toBuilder().apply {
      unaryOperation = unaryOperation.toBuilder().apply {
        operand = unaryOperation.operand.stripGroups()
      }.build()
    }.build()
    FUNCTION_CALL -> toBuilder().apply {
      functionCall = functionCall.toBuilder().apply {
        argument = functionCall.argument.stripGroups()
      }.build()
    }.build()
    GROUP -> group.stripGroups()
    CONSTANT, VARIABLE, EXPRESSIONTYPE_NOT_SET, null -> this
  }
}
