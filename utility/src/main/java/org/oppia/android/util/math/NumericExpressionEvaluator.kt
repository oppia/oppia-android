package org.oppia.android.util.math

import org.oppia.android.app.model.MathBinaryOperation
import org.oppia.android.app.model.MathBinaryOperation.Operator.ADD
import org.oppia.android.app.model.MathBinaryOperation.Operator.DIVIDE
import org.oppia.android.app.model.MathBinaryOperation.Operator.EXPONENTIATE
import org.oppia.android.app.model.MathBinaryOperation.Operator.MULTIPLY
import org.oppia.android.app.model.MathBinaryOperation.Operator.SUBTRACT
import org.oppia.android.app.model.MathExpression
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.BINARY_OPERATION
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.CONSTANT
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.EXPRESSIONTYPE_NOT_SET
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.FUNCTION_CALL
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.GROUP
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.UNARY_OPERATION
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.VARIABLE
import org.oppia.android.app.model.MathFunctionCall
import org.oppia.android.app.model.MathFunctionCall.FunctionType
import org.oppia.android.app.model.MathFunctionCall.FunctionType.FUNCTION_UNSPECIFIED
import org.oppia.android.app.model.MathFunctionCall.FunctionType.SQUARE_ROOT
import org.oppia.android.app.model.MathUnaryOperation
import org.oppia.android.app.model.MathUnaryOperation.Operator.NEGATE
import org.oppia.android.app.model.MathUnaryOperation.Operator.POSITIVE
import org.oppia.android.app.model.Real
import org.oppia.android.app.model.MathBinaryOperation.Operator as BinaryOperator
import org.oppia.android.app.model.MathUnaryOperation.Operator as UnaryOperator

/**
 * Numeric evaluator for numeric [MathExpression]s.
 *
 * In order to use this evaluator, directly import [evaluate] and call it for any numeric
 * [MathExpression]s that should be evaluated.
 */
class NumericExpressionEvaluator private constructor() {
  companion object {
    /**
     * Evaluates a math expression.
     *
     * This function only works with numeric expressions since variable expressions have no means
     * for evaluation (so they'll always result in a ``null`` return value).
     *
     * The function generally attempts to retain the most precise representation of a value in the
     * following order (from highest priority to lowest):
     * 1. Integers
     * 2. Fractions (rational values)
     * 3. Doubles (irrational values)
     *
     * Doubles will only be used if there's no other choice as they do not have perfect precision
     * unlike the other two structures. Further, it's possible for doubles to be used in cases where
     * an integer could work, or fractions to represent whole integers (due to quirks in underlying
     * routines). That being said, within a certain precision threshold values returned by this
     * function should be deterministic across multiple calls (for the same [MathExpression]).
     *
     * There are a number of cases where this function will fail:
     * - When trying to evaluate a variable expression.
     * - When trying to evaluate an invalid [MathExpression] (i.e. one of the substructures within
     *   the expression is not actually initialized per the proto structures).
     * - When trying to perform an impossible math operation (such as divide by zero). Note that
     *   this will sometimes result in a [Real] being returned with a value like NaN or infinity,
     *   and other times may result in an exception being thrown.
     *
     * Note that there's no guard against overflowing values during computation, so care should be
     * taken by the caller that this is possible for certain expressions.
     *
     * For more specifics on the constituent operations that "power" this function, see:
     * - [Real.plus]
     * - [Real.minus]
     * - [Real.times]
     * - [Real.div]
     * - [Real.pow]
     * - [Real.unaryMinus]
     * - [sqrt]
     *
     * @return the [Real] representing the evaluated expression, or ``null`` if something went wrong
     */
    fun MathExpression.evaluate(): Real? {
      return when (expressionTypeCase) {
        CONSTANT -> constant
        VARIABLE -> null // Variables not supported in numeric expressions.
        BINARY_OPERATION -> binaryOperation.evaluate()
        UNARY_OPERATION -> unaryOperation.evaluate()
        FUNCTION_CALL -> functionCall.evaluate()
        GROUP -> group.evaluate()
        EXPRESSIONTYPE_NOT_SET, null -> null
      }
    }

    private fun MathBinaryOperation.evaluate(): Real? {
      return when (operator) {
        ADD -> rightOperand.evaluate()?.let { leftOperand.evaluate()?.plus(it) }
        SUBTRACT -> rightOperand.evaluate()?.let { leftOperand.evaluate()?.minus(it) }
        MULTIPLY -> rightOperand.evaluate()?.let { leftOperand.evaluate()?.times(it) }
        DIVIDE -> rightOperand.evaluate()?.let { leftOperand.evaluate()?.div(it) }
        EXPONENTIATE -> rightOperand.evaluate()?.let { leftOperand.evaluate()?.pow(it) }
        BinaryOperator.OPERATOR_UNSPECIFIED, BinaryOperator.UNRECOGNIZED, null -> null
      }
    }

    private fun MathUnaryOperation.evaluate(): Real? {
      return when (operator) {
        NEGATE -> operand.evaluate()?.let { -it }
        POSITIVE -> operand.evaluate() // '+2' is the same as just '2'.
        UnaryOperator.OPERATOR_UNSPECIFIED, UnaryOperator.UNRECOGNIZED, null -> null
      }
    }

    private fun MathFunctionCall.evaluate(): Real? {
      return when (functionType) {
        SQUARE_ROOT -> argument.evaluate()?.let { sqrt(it) }
        FUNCTION_UNSPECIFIED,
        FunctionType.UNRECOGNIZED,
        null -> null
      }
    }
  }
}
