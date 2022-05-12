package org.oppia.android.app.utility.math

import org.oppia.android.R
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
import org.oppia.android.app.model.OppiaLanguage.SWAHILI
import org.oppia.android.app.model.OppiaLanguage.UNRECOGNIZED
import org.oppia.android.app.model.Real.RealTypeCase.INTEGER
import org.oppia.android.app.model.Real.RealTypeCase.IRRATIONAL
import org.oppia.android.app.model.Real.RealTypeCase.RATIONAL
import org.oppia.android.app.model.Real.RealTypeCase.REALTYPE_NOT_SET
import org.oppia.android.app.translation.AppLanguageResourceHandler
import javax.inject.Inject
import org.oppia.android.app.model.MathBinaryOperation.Operator as BinaryOperator
import org.oppia.android.app.model.MathUnaryOperation.Operator as UnaryOperator

/**
 * Utility for computing an accessibility string for screenreaders to be able to read out parsed
 * [MathExpression]s and [MathEquation]s.
 *
 * See [convertToHumanReadableString] for the specific function.
 */
class MathExpressionAccessibilityUtil @Inject constructor(
  private val resourceHandler: AppLanguageResourceHandler
) {
  /**
   * Returns the human-readable string (for screenreaders) representation of the specified
   * [expression].
   *
   * Note that rational ``Real``s are specifically not supported and will result in a null value
   * being returned (for custom expression constructs should use a division operation and set
   * [divAsFraction] to true. Further, irrational reals may be rounded during formatting if they are
   * very large or have long decimals (for an easier time reading). Numbers will be formatted
   * according to the user's locale.
   *
   * @param expression the expression to convert
   * @param language the target language for which the expression should be generated
   * @param divAsFraction whether divisions should be read out as fractions rather than divisions
   * @return the human-readable string, or null if the expression is malformed or the target
   *     language is unsupported
   */
  fun convertToHumanReadableString(
    expression: MathExpression,
    language: OppiaLanguage,
    divAsFraction: Boolean
  ): String? {
    return when (language) {
      ENGLISH -> expression.toHumanReadableEnglishString(divAsFraction)
      ARABIC, HINDI, HINGLISH, PORTUGUESE, BRAZILIAN_PORTUGUESE, SWAHILI, LANGUAGE_UNSPECIFIED,
      UNRECOGNIZED -> null
    }
  }

  /**
   * Returns the human-readable string (for screenreaders) representation of the specified
   * [equation].
   *
   * This function behaves in the same way as the [MathExpression] version of
   * [convertToHumanReadableString]--see that method's documentation for more details.
   */
  fun convertToHumanReadableString(
    equation: MathEquation,
    language: OppiaLanguage,
    divAsFraction: Boolean
  ): String? {
    return when (language) {
      ENGLISH -> equation.toHumanReadableEnglishString(divAsFraction)
      ARABIC, HINDI, HINGLISH, PORTUGUESE, BRAZILIAN_PORTUGUESE, SWAHILI, LANGUAGE_UNSPECIFIED,
      UNRECOGNIZED -> null
    }
  }

  private fun MathEquation.toHumanReadableEnglishString(divAsFraction: Boolean): String? {
    val lhsStr = leftSide.toHumanReadableEnglishString(divAsFraction)
    val rhsStr = rightSide.toHumanReadableEnglishString(divAsFraction)
    return if (lhsStr != null && rhsStr != null) {
      resourceHandler.getStringInLocaleWithWrapping(
        R.string.math_accessibility_a_equals_b, lhsStr, rhsStr
      )
    } else null
  }

  private fun MathExpression.toHumanReadableEnglishString(divAsFraction: Boolean): String? {
    // Ref: https://docs.google.com/document/d/1SkzAD4k7SWLp5_3L5WNxsnR79ATlOk8pz4irfE2ls-4/view.

    // Note that extra bidi wrapping is occurring here since there's not an obvious way to wrap "at
    // the end" for non-equations.
    return when (expressionTypeCase) {
      CONSTANT -> when (constant.realTypeCase) {
        IRRATIONAL -> resourceHandler.formatDouble(constant.irrational)
        INTEGER -> resourceHandler.formatLong(constant.integer.toLong())
        // Note that rational types should not actually be encountered in raw expressions, so
        // there's no explicit support for reading them out.
        RATIONAL, REALTYPE_NOT_SET, null -> null
      }
      VARIABLE -> when (variable) {
        "z", "Z" -> {
          val zed =
            resourceHandler.getStringInLocale(R.string.math_accessibility_part_zed)
          if (variable == "Z") {
            resourceHandler.capitalizeForHumans(zed)
          } else zed
        }
        else -> variable
      }
      BINARY_OPERATION -> {
        val lhs = binaryOperation.leftOperand
        val rhs = binaryOperation.rightOperand
        val lhsStr = lhs.toHumanReadableEnglishString(divAsFraction)
        val rhsStr = rhs.toHumanReadableEnglishString(divAsFraction)
        if (lhsStr == null || rhsStr == null) return null
        when (binaryOperation.operator) {
          ADD -> {
            resourceHandler.getStringInLocaleWithWrapping(
              R.string.math_accessibility_a_plus_b, lhsStr, rhsStr
            )
          }
          SUBTRACT -> {
            resourceHandler.getStringInLocaleWithWrapping(
              R.string.math_accessibility_a_minus_b, lhsStr, rhsStr
            )
          }
          MULTIPLY -> {
            val strResId = if (binaryOperation.canBeReadAsImplicitMultiplication()) {
              R.string.math_accessibility_implicit_multiplication
            } else R.string.math_accessibility_a_times_b
            resourceHandler.getStringInLocaleWithWrapping(strResId, lhsStr, rhsStr)
          }
          DIVIDE -> when {
            divAsFraction -> when {
              binaryOperation.isOneHalf() -> {
                resourceHandler.getStringInLocaleWithWrapping(
                  R.string.math_accessibility_part_one_half
                )
              }
              binaryOperation.isSimpleFraction() -> {
                resourceHandler.getStringInLocaleWithWrapping(
                  R.string.math_accessibility_simple_fraction, lhsStr, rhsStr
                )
              }
              else -> {
                resourceHandler.getStringInLocaleWithWrapping(
                  R.string.math_accessibility_complex_fraction, lhsStr, rhsStr
                )
              }
            }
            else -> {
              resourceHandler.getStringInLocaleWithWrapping(
                R.string.math_accessibility_a_divides_b, lhsStr, rhsStr
              )
            }
          }
          EXPONENTIATE -> {
            resourceHandler.getStringInLocaleWithWrapping(
              R.string.math_accessibility_a_exp_b, lhsStr, rhsStr
            )
          }
          BinaryOperator.OPERATOR_UNSPECIFIED, BinaryOperator.UNRECOGNIZED, null -> null
        }
      }
      UNARY_OPERATION -> {
        val operandStr = unaryOperation.operand.toHumanReadableEnglishString(divAsFraction)
        when (unaryOperation.operator) {
          NEGATE -> operandStr?.let {
            resourceHandler.getStringInLocaleWithWrapping(
              R.string.math_accessibility_negative_a, it
            )
          }
          POSITIVE -> operandStr?.let {
            resourceHandler.getStringInLocaleWithWrapping(
              R.string.math_accessibility_positive_a, it
            )
          }
          UnaryOperator.OPERATOR_UNSPECIFIED, UnaryOperator.UNRECOGNIZED, null -> null
        }
      }
      FUNCTION_CALL -> {
        val argStr = functionCall.argument.toHumanReadableEnglishString(divAsFraction)
        when (functionCall.functionType) {
          SQUARE_ROOT -> argStr?.let {
            if (functionCall.argument.isSingleTerm()) {
              resourceHandler.getStringInLocaleWithWrapping(
                R.string.math_accessibility_simple_square_root, it
              )
            } else {
              resourceHandler.getStringInLocaleWithWrapping(
                R.string.math_accessibility_complex_square_root, it
              )
            }
          }
          FUNCTION_UNSPECIFIED, FunctionType.UNRECOGNIZED, null -> null
        }
      }
      GROUP -> group.toHumanReadableEnglishString(divAsFraction)?.let {
        if (!isSingleTerm()) {
          resourceHandler.getStringInLocaleWithWrapping(R.string.math_accessibility_group, it)
        } else it
      }
      EXPRESSIONTYPE_NOT_SET, null -> null
    }
  }

  private companion object {
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
