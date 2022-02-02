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
import org.oppia.android.app.model.MathFunctionCall.FunctionType
import org.oppia.android.app.model.MathFunctionCall.FunctionType.FUNCTION_UNSPECIFIED
import org.oppia.android.app.model.MathFunctionCall.FunctionType.SQUARE_ROOT
import org.oppia.android.app.model.MathUnaryOperation
import org.oppia.android.app.model.MathUnaryOperation.Operator.NEGATE
import org.oppia.android.app.model.MathUnaryOperation.Operator.POSITIVE
import org.oppia.android.app.model.Polynomial
import org.oppia.android.app.model.MathBinaryOperation.Operator as BinaryOperator
import org.oppia.android.app.model.MathUnaryOperation.Operator as UnaryOperator
import org.oppia.android.app.model.Real

class ExpressionToPolynomialConverter private constructor() {
  companion object {
    // TODO: document that this generally only relate to algebraic expressions.
    fun MathExpression.reduceToPolynomial(): Polynomial? =
      replaceSquareRoots()
        .reduceToPolynomialAux()
        ?.removeUnnecessaryVariables()
        ?.simplifyRationals()
        ?.sort()

    private fun MathExpression.replaceSquareRoots(): MathExpression {
      return when (expressionTypeCase) {
        BINARY_OPERATION -> toBuilder().apply {
          binaryOperation = binaryOperation.toBuilder().apply {
            leftOperand = binaryOperation.leftOperand.replaceSquareRoots()
            rightOperand = binaryOperation.rightOperand.replaceSquareRoots()
          }.build()
        }.build()
        UNARY_OPERATION -> toBuilder().apply {
          unaryOperation = unaryOperation.toBuilder().apply {
            operand = unaryOperation.operand.replaceSquareRoots()
          }.build()
        }.build()
        FUNCTION_CALL -> when (functionCall.functionType) {
          SQUARE_ROOT -> toBuilder().apply {
            // Replace the square root function call with the equivalent exponentiation. That is,
            // sqrt(x)=x^(1/2).
            binaryOperation = MathBinaryOperation.newBuilder().apply {
              operator = EXPONENTIATE
              leftOperand = functionCall.argument.replaceSquareRoots()
              rightOperand = MathExpression.newBuilder().apply {
                constant = ONE_HALF
              }.build()
            }.build()
          }.build()
          FUNCTION_UNSPECIFIED, FunctionType.UNRECOGNIZED, null -> this
        }
        // This also eliminates groups from the expression.
        GROUP -> group.replaceSquareRoots()
        CONSTANT, VARIABLE, EXPRESSIONTYPE_NOT_SET, null -> this
      }
    }

    private fun MathExpression.reduceToPolynomialAux(): Polynomial? {
      return when (expressionTypeCase) {
        CONSTANT -> createConstantPolynomial(constant)
        VARIABLE -> createSingleVariablePolynomial(variable)
        BINARY_OPERATION -> binaryOperation.reduceToPolynomial()
        UNARY_OPERATION -> unaryOperation.reduceToPolynomial()
        // Both functions & groups should be removed ahead of polynomial reduction.
        FUNCTION_CALL, GROUP, EXPRESSIONTYPE_NOT_SET, null -> null
      }
    }

    private fun MathBinaryOperation.reduceToPolynomial(): Polynomial? {
      val leftPolynomial = leftOperand.reduceToPolynomialAux() ?: return null
      val rightPolynomial = rightOperand.reduceToPolynomialAux() ?: return null
      return when (operator) {
        ADD -> leftPolynomial + rightPolynomial
        SUBTRACT -> leftPolynomial - rightPolynomial
        MULTIPLY -> leftPolynomial * rightPolynomial
        DIVIDE -> leftPolynomial / rightPolynomial
        EXPONENTIATE -> leftPolynomial pow rightPolynomial
        BinaryOperator.OPERATOR_UNSPECIFIED, BinaryOperator.UNRECOGNIZED, null -> null
      }
    }

    private fun MathUnaryOperation.reduceToPolynomial(): Polynomial? {
      return when (operator) {
        NEGATE -> -(operand.reduceToPolynomialAux() ?: return null)
        POSITIVE -> operand.reduceToPolynomialAux() // Positive unary changes nothing.
        UnaryOperator.OPERATOR_UNSPECIFIED, UnaryOperator.UNRECOGNIZED, null -> null
      }
    }

    private fun createSingleVariablePolynomial(variableName: String): Polynomial {
      return createSingleTermPolynomial(
        Polynomial.Term.newBuilder().apply {
          coefficient = ONE
          addVariable(
            Polynomial.Term.Variable.newBuilder().apply {
              name = variableName
              power = 1
            }.build()
          )
        }.build()
      )
    }

    private fun createConstantPolynomial(constant: Real): Polynomial =
      createSingleTermPolynomial(Polynomial.Term.newBuilder().setCoefficient(constant).build())

    private fun createSingleTermPolynomial(term: Polynomial.Term): Polynomial =
      Polynomial.newBuilder().apply { addTerm(term) }.build()
  }
}
