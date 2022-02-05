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
import org.oppia.android.app.model.Real
import org.oppia.android.app.model.MathBinaryOperation.Operator as BinaryOperator
import org.oppia.android.app.model.MathUnaryOperation.Operator as UnaryOperator

/**
 * Converter from [MathExpression] to [Polynomial].
 *
 * See the separate protos for specifics on structure, and [reduceToPolynomial] for the actual
 * conversion function.
 */
class ExpressionToPolynomialConverter private constructor() {
  companion object {
    /**
     * Returns a new [Polynomial] that represents this [MathExpression], or null if it's not a valid
     * polynomial.
     *
     * Polynomials are defined as a list of terms where each term has a coefficient and zero or more
     * variables. There are a number of specific constraints that this function guarantees for all
     * returned polynomials:
     * - Terms will never have duplicate variable expressions (e.g. there will never be a returned
     *   polynomial with multiple 'x' terms, but there can be an 'x' and 'x^2' term). This is
     *   because effort is taken to combine like terms.
     * - Terms are always sorted by lexicography of the variable names and variable powers which
     *   allows for comparison that operates independently of commutativity, associativity, and
     *   distributivity.
     * - There will only ever be at most one constant term in the polynomial.
     * - There will always be at least 1 term (even if it's the constant zero).
     * - The polynomial will be mathematically equivalent to the original expression.
     * - Coefficients will be kept to the highest possible precision (i.e. integers and fractions
     *   will be preferred over irrationals unless a rounding error occurs).
     * - Most polynomial operations will be computed, including unary negation, addition,
     *   subtraction, multiplication (both implicit and explicit), division, and powers.
     *
     * Note that this will return null if a polynomial cannot be computed, such as in the cases:
     * - The expression represents a division where the result has a remainder polynomial.
     * - The expression results in a variable with a negative power or a division by an expression.
     * - The expression results in a non-integer power (which includes a current limitation for
     *   expressions like 'sqrt(x)^2'; these cannot pass because internally the method cannot
     *   represent 'x^1/2').
     * - The expression results in a power variable (which can never represent a polynomial).
     * - The expression is invalid (e.g. a default proto instance).
     *
     * This function is only expected to be used in conjunction with algebraic expressions. It's
     * suggested to use evaluation when comparing for equivalence among numeric expressions as it
     * should yield the same result and be more performant.
     *
     * The tests for this method provide very thorough and broad examples of different cases that
     * this function supports. In particular, the equality tests are useful to see what sorts of
     * expressions can be considered the same per [Polynomial] representation.
     */
    fun MathExpression.reduceToPolynomial(): Polynomial? {
      return replaceSquareRoots()
        .reduceToPolynomialAux()
        ?.removeUnnecessaryVariables()
        ?.simplifyRationals()
        ?.sort()
    }

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
