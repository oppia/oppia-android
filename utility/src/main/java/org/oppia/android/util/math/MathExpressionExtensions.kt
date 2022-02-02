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
