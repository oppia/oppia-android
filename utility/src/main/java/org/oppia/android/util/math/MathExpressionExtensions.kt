package org.oppia.android.util.math

import org.oppia.android.app.model.ComparableOperation
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
import org.oppia.android.util.math.ExpressionToComparableOperationConverter.Companion.convertToComparableOperation
import org.oppia.android.util.math.ExpressionToLatexConverter.Companion.convertToLatex
import org.oppia.android.util.math.ExpressionToPolynomialConverter.Companion.reduceToPolynomial
import org.oppia.android.util.math.NumericExpressionEvaluator.Companion.evaluate

/**
 * Returns the LaTeX conversion of this [MathExpression], with the style configuration determined by
 * [divAsFraction].
 *
 * See [convertToLatex] for specifics.
 */
fun MathExpression.toRawLatex(divAsFraction: Boolean): String = convertToLatex(divAsFraction)

/**
 * Returns the LaTeX conversion of this [MathEquation], with the style configuration determined by
 * [divAsFraction].
 *
 * See [convertToLatex] for specifics.
 */
fun MathEquation.toRawLatex(divAsFraction: Boolean): String = convertToLatex(divAsFraction)

/**
 * Returns the [Real] evaluation of this [MathExpression].
 *
 * See [evaluate] for specifics.
 */
fun MathExpression.evaluateAsNumericExpression(): Real? = evaluate()

/**
 * Returns the [ComparableOperation] representation of this [MathExpression].
 *
 * See [convertToComparableOperation] for details.
 */
fun MathExpression.toComparableOperation(): ComparableOperation = convertToComparableOperation()

/**
 * Returns the [Polynomial] representation of this [MathExpression].
 *
 * See [reduceToPolynomial] for details.
 */
fun MathExpression.toPolynomial(): Polynomial? = reduceToPolynomial()

// TODO: Add tests & docs.
fun MathExpression.stripRedundantGroups(): MathExpression {
  return when (expressionTypeCase) {
    BINARY_OPERATION -> toBuilder().apply {
      binaryOperation = binaryOperation.toBuilder().apply {
        leftOperand = leftOperand.stripRedundantGroups()
        rightOperand = rightOperand.stripRedundantGroups()
      }.build()
    }.build()
    UNARY_OPERATION -> toBuilder().apply {
      unaryOperation = unaryOperation.toBuilder().apply {
        operand = operand.stripRedundantGroups()
      }.build()
    }.build()
    FUNCTION_CALL -> toBuilder().apply {
      functionCall = functionCall.toBuilder().apply {
        argument = argument.stripRedundantGroups()
      }.build()
    }.build()
    GROUP -> if (group.isSingleTerm()) group.stripRedundantGroups() else this
    CONSTANT, VARIABLE, EXPRESSIONTYPE_NOT_SET, null -> this
  }
}

/**
 * Returns whether this [MathExpression] approximately equals another, that is, that it fully
 * matches in its AST representation but all constants are compared using
 * [Real.isApproximatelyEqualTo]. Further, this does not check parser markers when considering
 * equality.
 */
fun MathExpression.isApproximatelyEqualTo(other: MathExpression): Boolean {
  if (expressionTypeCase != other.expressionTypeCase) return false
  return when (expressionTypeCase) {
    CONSTANT -> constant.isApproximatelyEqualTo(other.constant)
    VARIABLE -> variable == other.variable
    BINARY_OPERATION -> {
      binaryOperation.operator == other.binaryOperation.operator &&
        binaryOperation.leftOperand.isApproximatelyEqualTo(other.binaryOperation.leftOperand) &&
        binaryOperation.rightOperand.isApproximatelyEqualTo(other.binaryOperation.rightOperand)
    }
    UNARY_OPERATION -> {
      unaryOperation.operator == other.unaryOperation.operator &&
        unaryOperation.operand.isApproximatelyEqualTo(other.unaryOperation.operand)
    }
    FUNCTION_CALL -> {
      functionCall.functionType == other.functionCall.functionType &&
        functionCall.argument.isApproximatelyEqualTo(other.functionCall.argument)
    }
    GROUP -> group.isApproximatelyEqualTo(other.group)
    EXPRESSIONTYPE_NOT_SET, null -> true
  }
}

private fun MathExpression.isSingleTerm(): Boolean {
  return when (expressionTypeCase) {
    CONSTANT, VARIABLE, FUNCTION_CALL, GROUP -> true
    BINARY_OPERATION, UNARY_OPERATION, EXPRESSIONTYPE_NOT_SET, null -> false
  }
}
