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

class NumericExpressionEvaluator private constructor() {
  companion object {
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
