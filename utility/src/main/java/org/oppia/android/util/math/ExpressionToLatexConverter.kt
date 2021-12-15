package org.oppia.android.util.math

import org.oppia.android.app.model.MathBinaryOperation.Operator.ADD
import org.oppia.android.app.model.MathBinaryOperation.Operator.DIVIDE
import org.oppia.android.app.model.MathBinaryOperation.Operator.EXPONENTIATE
import org.oppia.android.app.model.MathBinaryOperation.Operator.MULTIPLY
import org.oppia.android.app.model.MathBinaryOperation.Operator.SUBTRACT
import org.oppia.android.app.model.MathEquation
import org.oppia.android.app.model.MathExpression
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.BINARY_OPERATION
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.CONSTANT
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.EXPRESSIONTYPE_NOT_SET
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.FUNCTION_CALL
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.GROUP
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.UNARY_OPERATION
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.VARIABLE
import org.oppia.android.app.model.MathFunctionCall.FunctionType
import org.oppia.android.app.model.MathFunctionCall.FunctionType.FUNCTION_UNSPECIFIED
import org.oppia.android.app.model.MathFunctionCall.FunctionType.SQUARE_ROOT
import org.oppia.android.app.model.MathUnaryOperation.Operator.NEGATE
import org.oppia.android.app.model.MathUnaryOperation.Operator.POSITIVE
import org.oppia.android.app.model.MathBinaryOperation.Operator as BinaryOperator
import org.oppia.android.app.model.MathUnaryOperation.Operator as UnaryOperator

class ExpressionToLatexConverter private constructor() {
  companion object {
    fun MathEquation.convertToLatex(divAsFraction: Boolean): String {
      val lhs = leftSide
      val rhs = rightSide
      return "${lhs.convertToLatex(divAsFraction)} = ${rhs.convertToLatex(divAsFraction)}"
    }

    fun MathExpression.convertToLatex(divAsFraction: Boolean): String {
      return when (expressionTypeCase) {
        CONSTANT -> constant.toPlainText()
        VARIABLE -> variable
        BINARY_OPERATION -> {
          val lhsLatex = binaryOperation.leftOperand.convertToLatex(divAsFraction)
          val rhsLatex = binaryOperation.rightOperand.convertToLatex(divAsFraction)
          when (binaryOperation.operator) {
            ADD -> "$lhsLatex + $rhsLatex"
            SUBTRACT -> "$lhsLatex - $rhsLatex"
            MULTIPLY -> if (binaryOperation.isImplicit) {
              "$lhsLatex$rhsLatex"
            } else "$lhsLatex \\times $rhsLatex"
            DIVIDE -> if (divAsFraction) {
              "\\frac{$lhsLatex}{$rhsLatex}"
            } else "$lhsLatex \\div $rhsLatex"
            EXPONENTIATE -> "$lhsLatex ^ {$rhsLatex}"
            BinaryOperator.OPERATOR_UNSPECIFIED, BinaryOperator.UNRECOGNIZED, null ->
              "$lhsLatex $rhsLatex"
          }
        }
        UNARY_OPERATION -> {
          val operandLatex = unaryOperation.operand.convertToLatex(divAsFraction)
          when (unaryOperation.operator) {
            NEGATE -> "-$operandLatex"
            POSITIVE -> "+$operandLatex"
            UnaryOperator.OPERATOR_UNSPECIFIED, UnaryOperator.UNRECOGNIZED, null -> operandLatex
          }
        }
        FUNCTION_CALL -> {
          val argumentLatex = functionCall.argument.convertToLatex(divAsFraction)
          when (functionCall.functionType) {
            SQUARE_ROOT -> "\\sqrt{$argumentLatex}"
            FUNCTION_UNSPECIFIED, FunctionType.UNRECOGNIZED, null -> argumentLatex
          }
        }
        GROUP -> "(${group.convertToLatex(divAsFraction)})"
        EXPRESSIONTYPE_NOT_SET, null -> ""
      }
    }
  }
}
