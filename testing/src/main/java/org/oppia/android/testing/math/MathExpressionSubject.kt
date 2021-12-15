package org.oppia.android.testing.math

import com.google.common.truth.FailureMetadata
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
import org.oppia.android.testing.math.RealSubject.Companion.assertThat

// See: https://kotlinlang.org/docs/type-safe-builders.html.
class MathExpressionSubject(
  metadata: FailureMetadata,
  // TODO: restrict visibility.
  val actual: MathExpression
) : LiteProtoSubject(metadata, actual) {
  fun hasStructureThatMatches(init: ExpressionComparator.() -> Unit) {
    // TODO: maybe verify that all aspects are verified?
    ExpressionComparator.createFromExpression(actual).also(init)
  }

  // TODO: update DSL to not have return values (since it's unnecessary).
  @ExpressionComparatorMarker
  class ExpressionComparator private constructor(private val expression: MathExpression) {
    // TODO: convert to constant comparator?
    fun constant(init: ConstantComparator.() -> Unit): ConstantComparator =
      ConstantComparator.createFromExpression(expression).also(init)

    fun variable(init: VariableComparator.() -> Unit): VariableComparator =
      VariableComparator.createFromExpression(expression).also(init)

    fun addition(init: BinaryOperationComparator.() -> Unit): BinaryOperationComparator {
      return BinaryOperationComparator.createFromExpression(
        expression,
        expectedOperator = MathBinaryOperation.Operator.ADD
      ).also(init)
    }

    fun subtraction(init: BinaryOperationComparator.() -> Unit): BinaryOperationComparator {
      return BinaryOperationComparator.createFromExpression(
        expression,
        expectedOperator = MathBinaryOperation.Operator.SUBTRACT
      ).also(init)
    }

    fun multiplication(init: BinaryOperationComparator.() -> Unit): BinaryOperationComparator {
      return BinaryOperationComparator.createFromExpression(
        expression,
        expectedOperator = MathBinaryOperation.Operator.MULTIPLY
      ).also(init)
    }

    fun division(init: BinaryOperationComparator.() -> Unit): BinaryOperationComparator {
      return BinaryOperationComparator.createFromExpression(
        expression,
        expectedOperator = MathBinaryOperation.Operator.DIVIDE
      ).also(init)
    }

    fun exponentiation(init: BinaryOperationComparator.() -> Unit): BinaryOperationComparator {
      return BinaryOperationComparator.createFromExpression(
        expression,
        expectedOperator = MathBinaryOperation.Operator.EXPONENTIATE
      ).also(init)
    }

    fun negation(init: UnaryOperationComparator.() -> Unit): UnaryOperationComparator {
      return UnaryOperationComparator.createFromExpression(
        expression,
        expectedOperator = MathUnaryOperation.Operator.NEGATE
      ).also(init)
    }

    fun positive(init: UnaryOperationComparator.() -> Unit): UnaryOperationComparator {
      return UnaryOperationComparator.createFromExpression(
        expression,
        expectedOperator = MathUnaryOperation.Operator.POSITIVE
      ).also(init)
    }

    fun functionCallTo(
      type: MathFunctionCall.FunctionType,
      init: FunctionCallComparator.() -> Unit
    ): FunctionCallComparator {
      return FunctionCallComparator.createFromExpression(
        expression,
        expectedFunctionType = type
      ).also(init)
    }

    fun group(init: ExpressionComparator.() -> Unit): ExpressionComparator {
      return createFromExpression(expression.group).also(init)
    }

    internal companion object {
      fun createFromExpression(expression: MathExpression): ExpressionComparator =
        ExpressionComparator(expression)
    }
  }

  @ExpressionComparatorMarker
  class ConstantComparator private constructor(private val constant: Real) {
    fun withValueThat(): RealSubject = assertThat(constant)

    internal companion object {
      fun createFromExpression(expression: MathExpression): ConstantComparator {
        assertThat(expression.expressionTypeCase).isEqualTo(CONSTANT)
        return ConstantComparator(expression.constant)
      }
    }
  }

  @ExpressionComparatorMarker
  class VariableComparator private constructor(private val variableName: String) {
    fun withNameThat(): StringSubject = assertThat(variableName)

    internal companion object {
      fun createFromExpression(expression: MathExpression): VariableComparator {
        assertThat(expression.expressionTypeCase).isEqualTo(VARIABLE)
        return VariableComparator(expression.variable)
      }
    }
  }

  @ExpressionComparatorMarker
  class BinaryOperationComparator private constructor(
    private val operation: MathBinaryOperation
  ) {
    fun leftOperand(init: ExpressionComparator.() -> Unit): ExpressionComparator =
      ExpressionComparator.createFromExpression(operation.leftOperand).also(init)

    fun rightOperand(init: ExpressionComparator.() -> Unit): ExpressionComparator =
      ExpressionComparator.createFromExpression(operation.rightOperand).also(init)

    internal companion object {
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

  @ExpressionComparatorMarker
  class UnaryOperationComparator private constructor(
    private val operation: MathUnaryOperation
  ) {
    fun operand(init: ExpressionComparator.() -> Unit): ExpressionComparator =
      ExpressionComparator.createFromExpression(operation.operand).also(init)

    internal companion object {
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

  @ExpressionComparatorMarker
  class FunctionCallComparator private constructor(
    private val functionCall: MathFunctionCall
  ) {
    fun argument(init: ExpressionComparator.() -> Unit): ExpressionComparator =
      ExpressionComparator.createFromExpression(functionCall.argument).also(init)

    internal companion object {
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
    @DslMarker private annotation class ExpressionComparatorMarker

    fun assertThat(actual: MathExpression): MathExpressionSubject =
      assertAbout(::MathExpressionSubject).that(actual)
  }
}
