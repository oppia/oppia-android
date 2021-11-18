package org.oppia.android.util.math

import org.oppia.android.app.model.MathBinaryOperation
import org.oppia.android.app.model.MathEquation
import org.oppia.android.app.model.MathExpression
import org.oppia.android.app.model.Real

sealed class MathParsingError {
  object SpacesBetweenNumbersError : MathParsingError()

  object UnbalancedParenthesesError : MathParsingError()

  data class SingleRedundantParenthesesError(
    val rawExpression: String, val expression: MathExpression
  ): MathParsingError()

  data class MultipleRedundantParenthesesError(
    val rawExpression: String, val expression: MathExpression
  ): MathParsingError()

  data class RedundantParenthesesForIndividualTermsError(
    val rawExpression: String, val expression: MathExpression
  ): MathParsingError()

  data class UnnecessarySymbolsError(val invalidSymbol: String): MathParsingError()

  data class NumberAfterVariableError(val number: Real, val variable: String): MathParsingError()

  data class SubsequentBinaryOperatorsError(
    val operator1: String,
    val operator2: String
  ): MathParsingError()

  object SubsequentUnaryOperatorsError : MathParsingError()

  data class NoVariableOrNumberBeforeBinaryOperatorError(
    val operator: MathBinaryOperation.Operator, val operatorSymbol: String
  ): MathParsingError()

  data class NoVariableOrNumberAfterBinaryOperatorError(
    val operator: MathBinaryOperation.Operator, val operatorSymbol: String
  ): MathParsingError()

  object ExponentIsVariableExpressionError : MathParsingError()

  object ExponentTooLargeError : MathParsingError()

  object NestedExponentsError : MathParsingError()

  object HangingSquareRootError : MathParsingError()

  object TermDividedByZeroError : MathParsingError()

  object VariableInNumericExpressionError : MathParsingError()

  data class DisabledVariablesInUseError(val variables: List<String>) : MathParsingError()

  object EquationHasWrongNumberOfEqualsError : MathParsingError()

  object EquationMissingLhsOrRhsError : MathParsingError()

  data class InvalidFunctionInUseError(val functionName: String) : MathParsingError()

  data class FunctionNameUsedAsVariables(val expectedFunctionName: String) : MathParsingError()

  object GenericError : MathParsingError()
}
