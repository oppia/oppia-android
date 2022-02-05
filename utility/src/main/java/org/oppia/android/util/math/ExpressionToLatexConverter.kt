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

/**
 * Converter between math equations/expressions and renderable LaTeX strings.
 *
 * In order to use this converter, directly import [convertToLatex] and call it for any
 * [MathExpression]s or [MathEquation]s that should be converted to a renderable LaTeX
 * representation.
 */
class ExpressionToLatexConverter private constructor() {
  companion object {
    /**
     * Returns the LaTeX conversion of this [MathExpression].
     *
     * Note that this routine attempts to retain the exact structure of the original expression, but
     * not the actual original style. For example, parenthetical groups will be retained but spacing
     * between operators will be normalized regardless of the original raw expression.
     *
     * Note that the returned LaTeX is primarily intended to be render-ready, and may not be as
     * nicely human-readable. While some effort is taken to add spacing for better human
     * readability, there may be extra curly braces or LaTeX structures to generally ensure
     * correct rendering.
     *
     * Finally, the returned LaTeX should generally be portable/compatible with most LaTeX rendering
     * systems as it only relies on basic LaTeX language structures.
     *
     * @param divAsFraction determines whether divisions within the math structure should be
     *     rendered instead as fractions rather than division operations
     */
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
            // There's no operator, so try and "recover" by outputting the raw operands.
            BinaryOperator.OPERATOR_UNSPECIFIED, BinaryOperator.UNRECOGNIZED, null ->
              "$lhsLatex $rhsLatex"
          }
        }
        UNARY_OPERATION -> {
          val operandLatex = unaryOperation.operand.convertToLatex(divAsFraction)
          when (unaryOperation.operator) {
            NEGATE -> "-$operandLatex"
            POSITIVE -> "+$operandLatex"
            // There's no known operator, so just output the original operand.
            UnaryOperator.OPERATOR_UNSPECIFIED, UnaryOperator.UNRECOGNIZED, null -> operandLatex
          }
        }
        FUNCTION_CALL -> {
          val argumentLatex = functionCall.argument.convertToLatex(divAsFraction)
          when (functionCall.functionType) {
            SQUARE_ROOT -> "\\sqrt{$argumentLatex}"
            // There's no recognized function, so try to "recover" by outputting the raw argument.
            FUNCTION_UNSPECIFIED, FunctionType.UNRECOGNIZED, null -> argumentLatex
          }
        }
        GROUP -> "(${group.convertToLatex(divAsFraction)})"
        EXPRESSIONTYPE_NOT_SET, null -> "" // No corresponding LaTeX, so just go with empty string.
      }
    }

    /**
     * Returns the LaTeX conversion of this [MathEquation].
     *
     * See [convertToLatex] (for [MathExpression]s) for the specific behaviors and expectations of
     * this function.
     */
    fun MathEquation.convertToLatex(divAsFraction: Boolean): String {
      val lhs = leftSide
      val rhs = rightSide
      return "${lhs.convertToLatex(divAsFraction)} = ${rhs.convertToLatex(divAsFraction)}"
    }
  }
}
