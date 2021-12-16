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
import org.oppia.android.util.math.toPlainText
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import org.oppia.android.app.model.MathBinaryOperation.Operator as BinaryOperator
import org.oppia.android.app.model.MathUnaryOperation.Operator as UnaryOperator

class MathExpressionAccessibilityUtil @Inject constructor() {
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

  private companion object {
    // TODO: move these to the UI layer & have them utilize non-translatable strings.
    private val numberFormat by lazy { NumberFormat.getNumberInstance(Locale.US) }
    private val singularOrdinalNames = mapOf(
      1 to "oneth",
      2 to "half",
      3 to "third",
      4 to "fourth",
      5 to "fifth",
      6 to "sixth",
      7 to "seventh",
      8 to "eighth",
      9 to "ninth",
      10 to "tenth",
    )
    private val pluralOrdinalNames = mapOf(
      1 to "oneths",
      2 to "halves",
      3 to "thirds",
      4 to "fourths",
      5 to "fifths",
      6 to "sixths",
      7 to "sevenths",
      8 to "eighths",
      9 to "ninths",
      10 to "tenths",
    )

    private fun MathEquation.toHumanReadableEnglishString(divAsFraction: Boolean): String? {
      val lhsStr = leftSide.toHumanReadableEnglishString(divAsFraction)
      val rhsStr = rightSide.toHumanReadableEnglishString(divAsFraction)
      return if (lhsStr != null && rhsStr != null) "$lhsStr equals $rhsStr" else null
    }

    private fun MathExpression.toHumanReadableEnglishString(divAsFraction: Boolean): String? {
      // Reference:
      // https://docs.google.com/document/d/1P-dldXQ08O-02ZRG978paiWOSz0dsvcKpDgiV_rKH_Y/view.
      return when (expressionTypeCase) {
        CONSTANT -> if (constant.realTypeCase == INTEGER) {
          numberFormat.format(constant.integer.toLong())
        } else constant.toPlainText()
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
            DIVIDE -> {
              if (divAsFraction && lhs.isConstantInteger() && rhs.isConstantInteger()) {
                val numerator = lhs.constant.integer
                val denominator = rhs.constant.integer
                if (numerator in 0..10 && denominator in 1..10 && denominator >= numerator) {
                  val ordinalName =
                    if (numerator == 1) {
                      singularOrdinalNames.getValue(denominator)
                    } else pluralOrdinalNames.getValue(denominator)
                  "$numerator $ordinalName"
                } else "$lhsStr over $rhsStr"
              } else if (divAsFraction) {
                "the fraction with numerator $lhsStr and denominator $rhsStr"
              } else "$lhsStr divided by $rhsStr"
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

    private fun MathExpression.isConstantInteger(): Boolean =
      expressionTypeCase == CONSTANT && constant.realTypeCase == INTEGER

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
  }
}
