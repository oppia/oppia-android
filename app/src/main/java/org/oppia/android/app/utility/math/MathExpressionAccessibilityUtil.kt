package org.oppia.android.app.utility.math

import org.oppia.android.app.model.MathBinaryOperation
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
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OppiaLanguage.ARABIC
import org.oppia.android.app.model.OppiaLanguage.BRAZILIAN_PORTUGUESE
import org.oppia.android.app.model.OppiaLanguage.ENGLISH
import org.oppia.android.app.model.OppiaLanguage.HINDI
import org.oppia.android.app.model.OppiaLanguage.HINGLISH
import org.oppia.android.app.model.OppiaLanguage.LANGUAGE_UNSPECIFIED
import org.oppia.android.app.model.OppiaLanguage.PORTUGUESE
import org.oppia.android.app.model.OppiaLanguage.UNRECOGNIZED
import org.oppia.android.app.model.Real.RealTypeCase.INTEGER
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import org.oppia.android.app.model.MathBinaryOperation.Operator as BinaryOperator
import org.oppia.android.app.model.MathUnaryOperation.Operator as UnaryOperator
import org.oppia.android.app.model.Real.RealTypeCase.IRRATIONAL
import org.oppia.android.app.model.Real.RealTypeCase.RATIONAL
import org.oppia.android.app.model.Real.RealTypeCase.REALTYPE_NOT_SET

class MathExpressionAccessibilityUtil @Inject constructor() {
  // TODO: document that rationals aren't supported, and that irrationals are rounded during formatting (and maybe that ints are also formatted?).

  fun convertToHumanReadableString(
    expression: MathExpression,
    language: OppiaLanguage,
    divAsFraction: Boolean
  ): String? {
    return when (language) {
      ENGLISH -> expression.toHumanReadableEnglishString(divAsFraction)
      ARABIC, HINDI, HINGLISH, PORTUGUESE, BRAZILIAN_PORTUGUESE, LANGUAGE_UNSPECIFIED,
      UNRECOGNIZED -> null
    }
  }

  fun convertToHumanReadableString(
    equation: MathEquation,
    language: OppiaLanguage,
    divAsFraction: Boolean
  ): String? {
    return when (language) {
      ENGLISH -> equation.toHumanReadableEnglishString(divAsFraction)
      ARABIC, HINDI, HINGLISH, PORTUGUESE, BRAZILIAN_PORTUGUESE, LANGUAGE_UNSPECIFIED,
      UNRECOGNIZED -> null
    }
  }

  private companion object {
    // TODO: move these to the UI layer & have them utilize non-translatable strings.
    private val numberFormat by lazy { NumberFormat.getNumberInstance(Locale.US) }

    private fun MathEquation.toHumanReadableEnglishString(divAsFraction: Boolean): String? {
      val lhsStr = leftSide.toHumanReadableEnglishString(divAsFraction)
      val rhsStr = rightSide.toHumanReadableEnglishString(divAsFraction)
      return if (lhsStr != null && rhsStr != null) "$lhsStr equals $rhsStr" else null
    }

    private fun MathExpression.toHumanReadableEnglishString(divAsFraction: Boolean): String? {
      // Reference:
      // https://docs.google.com/document/d/1P-dldXQ08O-02ZRG978paiWOSz0dsvcKpDgiV_rKH_Y/view.
      return when (expressionTypeCase) {
        CONSTANT -> when (constant.realTypeCase) {
          IRRATIONAL -> numberFormat.format(constant.irrational)
          INTEGER -> numberFormat.format(constant.integer.toLong())
          // Note that rational types should not actually be encountered in raw expressions, so
          // there's no explicit support for reading them out.
          RATIONAL, REALTYPE_NOT_SET, null -> null
        }
        VARIABLE -> when (variable) {
          "z" -> "zed"
          "Z" -> "Zed"
          else -> variable
        }
        BINARY_OPERATION -> {
          val lhs = binaryOperation.leftOperand
          val rhs = binaryOperation.rightOperand
          val lhsStr = lhs.toHumanReadableEnglishString(divAsFraction)
          val rhsStr = rhs.toHumanReadableEnglishString(divAsFraction)
          if (lhsStr == null || rhsStr == null) return null
          when (binaryOperation.operator) {
            ADD -> "$lhsStr plus $rhsStr"
            SUBTRACT -> "$lhsStr minus $rhsStr"
            MULTIPLY -> {
              if (binaryOperation.canBeReadAsImplicitMultiplication()) {
                "$lhsStr $rhsStr"
              } else "$lhsStr times $rhsStr"
            }
            DIVIDE -> when {
              divAsFraction -> when {
                binaryOperation.isOneHalf() -> "one half"
                binaryOperation.isSimpleFraction() -> "$lhsStr over $rhsStr"
                else -> "the fraction with numerator $lhsStr and denominator $rhsStr"
              }
              else -> "$lhsStr divided by $rhsStr"
            }
            EXPONENTIATE -> "$lhsStr raised to the power of $rhsStr"
            BinaryOperator.OPERATOR_UNSPECIFIED, BinaryOperator.UNRECOGNIZED, null -> null
          }
        }
        UNARY_OPERATION -> {
          val operandStr = unaryOperation.operand.toHumanReadableEnglishString(divAsFraction)
          when (unaryOperation.operator) {
            NEGATE -> operandStr?.let { "negative $it" }
            POSITIVE -> operandStr?.let { "positive $it" }
            UnaryOperator.OPERATOR_UNSPECIFIED, UnaryOperator.UNRECOGNIZED, null -> null
          }
        }
        FUNCTION_CALL -> {
          val argStr = functionCall.argument.toHumanReadableEnglishString(divAsFraction)
          when (functionCall.functionType) {
            SQUARE_ROOT -> argStr?.let {
              if (functionCall.argument.isSingleTerm()) {
                "square root of $it"
              } else "start square root $it end square root"
            }
            FUNCTION_UNSPECIFIED, FunctionType.UNRECOGNIZED, null -> null
          }
        }
        GROUP -> group.toHumanReadableEnglishString(divAsFraction)?.let {
          if (isSingleTerm()) it else "open parenthesis $it close parenthesis"
        }
        EXPRESSIONTYPE_NOT_SET, null -> null
      }
    }

    private fun MathBinaryOperation.canBeReadAsImplicitMultiplication(): Boolean {
      // Note that exponentiation is specialized since it's higher precedence than multiplication
      // which means the graph won't look like "constant * variable" for polynomial terms like 2x^4
      // (which are cases the system should read using implicit multiplication, e.g. "two x raised
      // to the power of 4").
      if (!isImplicit || !leftOperand.isConstant()) return false
      return rightOperand.isVariable() || rightOperand.isExponentiation()
    }

    private fun MathBinaryOperation.isSimpleFraction(): Boolean {
      // 'Simple' fractions are those with single term numerators and denominators (which are
      // subsequently easier to read out), and whose constant numerator/denonominators are integers.
      return leftOperand.isSimpleFractionTerm() && rightOperand.isSimpleFractionTerm()
    }

    private fun MathBinaryOperation.isOneHalf(): Boolean {
      // If the either operand isn't an integer it will default to 0 per proto3 rules.
      return leftOperand.constant.integer == 1 && rightOperand.constant.integer == 2
    }

    private fun MathExpression.isConstant(): Boolean = expressionTypeCase == CONSTANT

    private fun MathExpression.isVariable(): Boolean = expressionTypeCase == VARIABLE

    private fun MathExpression.isExponentiation(): Boolean =
      expressionTypeCase == BINARY_OPERATION && binaryOperation.operator == EXPONENTIATE

    private fun MathExpression.isSingleTerm(): Boolean = when (expressionTypeCase) {
      CONSTANT, VARIABLE, FUNCTION_CALL -> true
      BINARY_OPERATION, UNARY_OPERATION -> false
      GROUP -> group.isSingleTerm()
      EXPRESSIONTYPE_NOT_SET, null -> false
    }

    private fun MathExpression.isSimpleFractionTerm(): Boolean = when (expressionTypeCase) {
      CONSTANT -> constant.realTypeCase == INTEGER
      VARIABLE -> true
      BINARY_OPERATION, UNARY_OPERATION, FUNCTION_CALL, GROUP, EXPRESSIONTYPE_NOT_SET, null -> false
    }
  }
}
