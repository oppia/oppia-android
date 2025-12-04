package org.oppia.android.testing.math

import com.google.common.truth.DoubleSubject
import com.google.common.truth.FailureMetadata
import com.google.common.truth.IntegerSubject
import com.google.common.truth.StringSubject
import com.google.common.truth.Truth.assertAbout
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.google.common.truth.extensions.proto.LiteProtoSubject
import org.oppia.android.app.model.MathBinaryOperation
import org.oppia.android.app.model.MathExpression
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.BINARY_OPERATION
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.CONSTANT
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.FUNCTION_CALL
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.UNARY_OPERATION
import org.oppia.android.app.model.MathExpression.ExpressionTypeCase.VARIABLE
import org.oppia.android.app.model.MathFunctionCall
import org.oppia.android.app.model.MathUnaryOperation
import org.oppia.android.app.model.Real
import org.oppia.android.testing.math.MathExpressionSubject.Companion.assertThat
import org.oppia.android.testing.math.RealSubject.Companion.assertThat
import org.oppia.android.util.math.evaluateAsNumericExpression
import org.oppia.android.util.math.toRawLatex

/**
 * Truth subject for verifying properties of [MathExpression]s.
 *
 * This subject makes use of a custom Kotlin DSL to test the structure of an expression. This
 * structure allows for recursive verification of the structure since the structure itself is
 * recursive. Further, unchecked parts of the structure are not verified. See the following example
 * to get an idea of the DSL for verifying expressions (see specific methods the comparator for all
 * syntactical options):
 *
 * ```kotlin
 *  assertThat(expression).hasStructureThatMatches {
 *    addition {
 *      leftOperand {
 *        constant {
 *          withValueThat().isIntegerThat().isEqualTo(3)
 *        }
 *      }
 *      rightOperand {
 *        multiplication {
 *          leftOperand {
 *            constant {
 *              withValueThat().isIntegerThat().isEqualTo(4)
 *            }
 *          }
 *          rightOperand {
 *            negation {
 *              operand {
 *                constant {
 *                  withValueThat().isIntegerThat().isEqualTo(5)
 *                }
 *              }
 *            }
 *          }
 *        }
 *      }
 *    }
 *  }
 * ```
 *
 * The above verifies the following structure:
 * ```
 *    +
 *  /   \
 * 3     *
 *     /   \
 *    4    -
 *         |
 *         5
 * ```
 *
 * (which would correspond to the expression 3+4*-5).
 *
 * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying
 * [MathExpression] proto can be verified through inherited methods.
 *
 * Call [assertThat] to create the subject.
 */
class MathExpressionSubject private constructor(
  metadata: FailureMetadata,
  val actual: MathExpression
) : LiteProtoSubject(metadata, actual) {
  /**
   * Begins the structure syntax matcher.
   *
   * See [ExpressionComparator] for syntax.
   */
  fun hasStructureThatMatches(init: ExpressionComparator.() -> Unit) {
    ExpressionComparator.createFromExpression(actual).also(init)
  }

  /**
   * Assumes that this expression evaluates to a fraction (i.e. [Real.getRational]) and returns a
   * [FractionSubject] to verify the computed value.
   *
   * Note that this should only be used for numeric expressions as variable expressions cannot be
   * evaluated. For more context on expression evaluation, see [evaluateAsNumericExpression].
   */
  fun evaluatesToRationalThat(): FractionSubject =
    FractionSubject.assertThat(evaluateAsReal(expectedType = Real.RealTypeCase.RATIONAL).rational)

  /**
   * Assumes that this expression evaluates to an irrational (i.e. [Real.getIrrational]) and returns
   * a [DoubleSubject] to verify the computed value.
   *
   * Note that this should only be used for numeric expressions as variable expressions cannot be
   * evaluated. For more context on expression evaluation, see [evaluateAsNumericExpression].
   */
  fun evaluatesToIrrationalThat(): DoubleSubject =
    assertThat(evaluateAsReal(expectedType = Real.RealTypeCase.IRRATIONAL).irrational)

  /**
   * Assumes that this expression evaluates to an integer (i.e. [Real.getInteger]) and returns an
   * [IntegerSubject] to verify the computed value.
   *
   * Note that this should only be used for numeric expressions as variable expressions cannot be
   * evaluated. For more context on expression evaluation, see [evaluateAsNumericExpression].
   */
  fun evaluatesToIntegerThat(): IntegerSubject =
    assertThat(evaluateAsReal(expectedType = Real.RealTypeCase.INTEGER).integer)

  /**
   * Returns a [StringSubject] to verify the LaTeX conversion of the tested [MathExpression].
   *
   * For more details on LaTeX conversion, see [toRawLatex]. Note that this method, in contrast to
   * [convertsWithFractionsToLatexStringThat], retains division operations as-is.
   */
  fun convertsToLatexStringThat(): StringSubject =
    assertThat(convertToLatex(divAsFraction = false))

  /**
   * Returns a [StringSubject] to verify the LaTeX conversion of the tested [MathExpression].
   *
   * For more details on LaTeX conversion, see [toRawLatex]. Note that this method, in contrast to
   * [convertsToLatexStringThat], treats divisions as fractions.
   */
  fun convertsWithFractionsToLatexStringThat(): StringSubject =
    assertThat(convertToLatex(divAsFraction = true))

  private fun evaluateAsReal(expectedType: Real.RealTypeCase): Real {
    val real = actual.evaluateAsNumericExpression()
    assertWithMessage("Failed to evaluate numeric expression").that(real).isNotNull()
    assertWithMessage("Expected constant to evaluate to $expectedType")
      .that(real?.realTypeCase)
      .isEqualTo(expectedType)
    return checkNotNull(real) // Just to remove the nullable operator; the actual check is above.
  }

  private fun convertToLatex(divAsFraction: Boolean): String = actual.toRawLatex(divAsFraction)

  /**
   * DSL syntax provider for verifying the structure of a [MathExpression].
   *
   * Note that per the proto definition of [MathExpression], this comparator can only represent one
   * of the expression substructures (e.g. constant, variable, binary operations, and others). See
   * the member methods for the different substructures that can be verified.
   *
   * Example syntax for verifying a constant:
   *
   * ```kotlin
   * <prefix> {
   *   constant {
   *     ...
   *   }
   * }
   * ```
   *
   * <prefix> is either verifying the root (i.e. via [hasStructureThatMatches]) or is for verifying
   * a nested expression (such as through groups).
   */
  @ExpressionComparatorMarker
  class ExpressionComparator private constructor(private val expression: MathExpression) {
    /**
     * Begins structure matching for this expression as a constant per [MathExpression.getConstant].
     *
     * This method will fail if the expression corresponding to the subject is not a constant. See
     * [ConstantComparator] for example syntax.
     */
    fun constant(init: ConstantComparator.() -> Unit) {
      ConstantComparator.createFromExpression(expression).also(init)
    }

    /**
     * Begins structure matching for this expression as a variable per [MathExpression.getVariable].
     *
     * This method will fail if the expression corresponding to the subject is not a variable. See
     * [VariableComparator] for example syntax.
     */
    fun variable(init: VariableComparator.() -> Unit) {
      VariableComparator.createFromExpression(expression).also(init)
    }

    /**
     * Begins structure matching for this expression as an addition operation per
     * [MathExpression.getBinaryOperation].
     *
     * This method will fail if the expression corresponding to the subject is not an addition
     * operation. See [BinaryOperationComparator] for example syntax.
     */
    fun addition(init: BinaryOperationComparator.() -> Unit) {
      BinaryOperationComparator.createFromExpression(
        expression,
        expectedOperator = MathBinaryOperation.Operator.ADD
      ).also(init)
    }

    /**
     * Begins structure matching for this expression as a subtraction operation per
     * [MathExpression.getBinaryOperation].
     *
     * This method will fail if the expression corresponding to the subject is not a subtraction
     * operation. See [BinaryOperationComparator] for example syntax.
     */
    fun subtraction(init: BinaryOperationComparator.() -> Unit) {
      BinaryOperationComparator.createFromExpression(
        expression,
        expectedOperator = MathBinaryOperation.Operator.SUBTRACT
      ).also(init)
    }

    /**
     * Begins structure matching for this expression as a multiplication operation per
     * [MathExpression.getBinaryOperation].
     *
     * This method will fail if the expression corresponding to the subject is not a multiplication
     * operation. See [BinaryOperationComparator] for example syntax.
     *
     * This verifies that the multiplication operation is explicit by default, and this behavior can
     * be overwritten using [isImplicit].
     */
    fun multiplication(isImplicit: Boolean = false, init: BinaryOperationComparator.() -> Unit) {
      BinaryOperationComparator.createFromExpression(
        expression,
        expectedOperator = MathBinaryOperation.Operator.MULTIPLY
      ).also {
        assertWithMessage(
          "Expected multiplication to be ${if (isImplicit) "implicit" else "explicit" }"
        ).that(expression.binaryOperation.isImplicit).isEqualTo(isImplicit)
      }.also(init)
    }

    /**
     * Begins structure matching for this expression as a division operation per
     * [MathExpression.getBinaryOperation].
     *
     * This method will fail if the expression corresponding to the subject is not a division
     * operation. See [BinaryOperationComparator] for example syntax.
     */
    fun division(init: BinaryOperationComparator.() -> Unit) {
      BinaryOperationComparator.createFromExpression(
        expression,
        expectedOperator = MathBinaryOperation.Operator.DIVIDE
      ).also(init)
    }

    /**
     * Begins structure matching for this expression as an exponentiation operation per
     * [MathExpression.getBinaryOperation].
     *
     * This method will fail if the expression corresponding to the subject is not an exponentiation
     * operation. See [BinaryOperationComparator] for example syntax.
     */
    fun exponentiation(init: BinaryOperationComparator.() -> Unit) {
      BinaryOperationComparator.createFromExpression(
        expression,
        expectedOperator = MathBinaryOperation.Operator.EXPONENTIATE
      ).also(init)
    }

    /**
     * Begins structure matching for this expression as a negation operation per
     * [MathExpression.getUnaryOperation].
     *
     * This method will fail if the expression corresponding to the subject is not a negation
     * operation. See [UnaryOperationComparator] for example syntax.
     */
    fun negation(init: UnaryOperationComparator.() -> Unit) {
      UnaryOperationComparator.createFromExpression(
        expression,
        expectedOperator = MathUnaryOperation.Operator.NEGATE
      ).also(init)
    }

    /**
     * Begins structure matching for this expression as a positive operation per
     * [MathExpression.getUnaryOperation].
     *
     * This method will fail if the expression corresponding to the subject is not a positive
     * operation. See [UnaryOperationComparator] for example syntax.
     */
    fun positive(init: UnaryOperationComparator.() -> Unit) {
      UnaryOperationComparator.createFromExpression(
        expression,
        expectedOperator = MathUnaryOperation.Operator.POSITIVE
      ).also(init)
    }

    /**
     * Begins structure matching for this expression as a function call per
     * [MathExpression.getFunctionCall].
     *
     * This method will fail if the expression corresponding to the subject is not a function call.
     * See [FunctionCallComparator] for example syntax.
     */
    fun functionCallTo(
      type: MathFunctionCall.FunctionType,
      init: FunctionCallComparator.() -> Unit
    ) {
      FunctionCallComparator.createFromExpression(
        expression, expectedFunctionType = type
      ).also(init)
    }

    /**
     * Begins structure matching for this expression as a group per [MathExpression.getGroup].
     *
     * This method will fail if the expression corresponding to the subject is not a group. Example
     * syntax:
     *
     * ```kotlin
     * group {
     *   ... <expression verification> ...
     * }
     * ```
     *
     * Groups refer to other expressions, so [ExpressionComparator] is used to verify constituent
     * properties of the group.
     */
    fun group(init: ExpressionComparator.() -> Unit) {
      createFromExpression(expression.group).also(init)
    }

    internal companion object {
      /** Returns a new [ExpressionComparator] corresponding to the specified [MathExpression]. */
      fun createFromExpression(expression: MathExpression): ExpressionComparator =
        ExpressionComparator(expression)
    }
  }

  /**
   * DSL syntax provider for verifying constants.
   *
   * Example syntax:
   *
   * ```kotlin
   * constant {
   *   withValueThat()...
   * }
   * ```
   *
   * This comparator provides access to a [RealSubject] to verify the actual constant value.
   */
  @ExpressionComparatorMarker
  class ConstantComparator private constructor(private val constant: Real) {
    /**
     * Returns a [RealSubject] to verify the constant that's being represented by this comparator.
     */
    fun withValueThat(): RealSubject = assertThat(constant)

    internal companion object {
      /**
       * Returns a new [ConstantComparator] corresponding to the specified [MathExpression],
       * verifying that it is, indeed, a constant.
       */
      fun createFromExpression(expression: MathExpression): ConstantComparator {
        assertThat(expression.expressionTypeCase).isEqualTo(CONSTANT)
        return ConstantComparator(expression.constant)
      }
    }
  }

  /**
   * DSL syntax provider for verifying variables.
   *
   * Example syntax:
   *
   * ```kotlin
   * variable {
   *   withNameThat()...
   * }
   * ```
   *
   * This comparator provides access to a [StringSubject] to verify the actual variable value.
   */
  @ExpressionComparatorMarker
  class VariableComparator private constructor(private val variableName: String) {
    /**
     * Returns a [StringSubject] to verify the variable that's being represented by this comparator.
     */
    fun withNameThat(): StringSubject = assertThat(variableName)

    internal companion object {
      /**
       * Returns a new [VariableComparator] corresponding to the specified [MathExpression],
       * verifying that it is, indeed, a variable.
       */
      fun createFromExpression(expression: MathExpression): VariableComparator {
        assertThat(expression.expressionTypeCase).isEqualTo(VARIABLE)
        return VariableComparator(expression.variable)
      }
    }
  }

  /**
   * DSL syntax provider for verifying binary operations, like addition and multiplication.
   *
   * Example syntax:
   *
   * ```kotlin
   * division {
   *   leftOperand {
   *     ... <expression verification> ...
   *   }
   *
   *   rightOperand {
   *     ... <expression verification> ...
   *   }
   * }
   * ```
   *
   * Both the left and right operands represent other [MathExpression]s.
   */
  @ExpressionComparatorMarker
  class BinaryOperationComparator private constructor(
    private val operation: MathBinaryOperation
  ) {
    /**
     * Begins structure matching this operation's left operand per
     * [MathBinaryOperation.getLeftOperand] for the operation represented by this comparator.
     *
     * This method provides an [ExpressionComparator] to use to verify the constituent properties
     * of the operand.
     */
    fun leftOperand(init: ExpressionComparator.() -> Unit) {
      ExpressionComparator.createFromExpression(operation.leftOperand).also(init)
    }

    /**
     * Begins structure matching this operation's right operand per
     * [MathBinaryOperation.getRightOperand] for the operation represented by this comparator.
     *
     * This method provides an [ExpressionComparator] to use to verify the constituent properties
     * of the operand.
     */
    fun rightOperand(init: ExpressionComparator.() -> Unit) {
      ExpressionComparator.createFromExpression(operation.rightOperand).also(init)
    }

    internal companion object {
      /**
       * Returns a new [BinaryOperationComparator] corresponding to the specified [MathExpression],
       * verifying that it is, indeed, a binary operation with the specified operator.
       */
      fun createFromExpression(
        expression: MathExpression,
        expectedOperator: MathBinaryOperation.Operator
      ): BinaryOperationComparator {
        assertThat(expression.expressionTypeCase).isEqualTo(BINARY_OPERATION)
        assertWithMessage("Expected binary operation with operator: $expectedOperator")
          .that(expression.binaryOperation.operator)
          .isEqualTo(expectedOperator)
        return BinaryOperationComparator(expression.binaryOperation)
      }
    }
  }

  /**
   * DSL syntax provider for verifying unary operations, like negation.
   *
   * Example syntax:
   *
   * ```kotlin
   * negation {
   *   operand {
   *     ... <expression verification> ...
   *   }
   * }
   * ```
   *
   * The operation's operand represents another [MathExpression].
   */
  @ExpressionComparatorMarker
  class UnaryOperationComparator private constructor(
    private val operation: MathUnaryOperation
  ) {
    /**
     * Begins structure matching this operation's operand per [MathUnaryOperation.getOperand] for
     * the operation represented by this comparator.
     *
     * This method provides an [ExpressionComparator] to use to verify the constituent properties
     * of the operand.
     */
    fun operand(init: ExpressionComparator.() -> Unit) {
      ExpressionComparator.createFromExpression(operation.operand).also(init)
    }

    internal companion object {
      /**
       * Returns a new [UnaryOperationComparator] corresponding to the specified [MathExpression],
       * verifying that it is, indeed, a unary operation with the specified operator.
       */
      fun createFromExpression(
        expression: MathExpression,
        expectedOperator: MathUnaryOperation.Operator
      ): UnaryOperationComparator {
        assertThat(expression.expressionTypeCase).isEqualTo(UNARY_OPERATION)
        assertWithMessage("Expected unary operation with operator: $expectedOperator")
          .that(expression.unaryOperation.operator)
          .isEqualTo(expectedOperator)
        return UnaryOperationComparator(expression.unaryOperation)
      }
    }
  }

  /**
   * DSL syntax provider for verifying function calls, like square root.
   *
   * Example syntax:
   *
   * ```kotlin
   * functionCallTo(SQUARE_ROOT) {
   *   argument {
   *     ... <expression verification> ...
   *   }
   * }
   * ```
   *
   * The function call's argument represents another [MathExpression].
   */
  @ExpressionComparatorMarker
  class FunctionCallComparator private constructor(
    private val functionCall: MathFunctionCall
  ) {
    /**
     * Begins structure matching the function call's argument per [MathFunctionCall.getArgument] for
     * the operation represented by this comparator.
     *
     * This method provides an [ExpressionComparator] to use to verify the constituent properties
     * of the function call's argument.
     */
    fun argument(init: ExpressionComparator.() -> Unit) {
      ExpressionComparator.createFromExpression(functionCall.argument).also(init)
    }

    internal companion object {
      /**
       * Returns a new [FunctionCallComparator] corresponding to the specified [MathExpression],
       * verifying that it is, indeed, a function call with the function type.
       */
      fun createFromExpression(
        expression: MathExpression,
        expectedFunctionType: MathFunctionCall.FunctionType
      ): FunctionCallComparator {
        assertThat(expression.expressionTypeCase).isEqualTo(FUNCTION_CALL)
        assertWithMessage("Expected function call to: $expectedFunctionType")
          .that(expression.functionCall.functionType)
          .isEqualTo(expectedFunctionType)
        return FunctionCallComparator(expression.functionCall)
      }
    }
  }

  companion object {
    // See: https://kotlinlang.org/docs/type-safe-builders.html for how the DSL definition works.
    @DslMarker private annotation class ExpressionComparatorMarker

    /**
     * Returns a new [MathExpressionSubject] to verify aspects of the specified [MathExpression]
     * value.
     */
    fun assertThat(actual: MathExpression): MathExpressionSubject =
      assertAbout(::MathExpressionSubject).that(actual)
  }
}
