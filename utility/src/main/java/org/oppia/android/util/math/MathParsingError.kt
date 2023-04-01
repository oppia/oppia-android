package org.oppia.android.util.math

import org.oppia.android.app.model.MathBinaryOperation
import org.oppia.android.app.model.MathExpression
import org.oppia.android.app.model.Real

/**
 * An error that can be encountered while trying to parse a raw math expression or equation.
 *
 * All possible errors are subclasses to this sealed class. Further, this class has a dedicated
 * Truth test subject that can be used for nicer testing.
 */
sealed class MathParsingError {
  /**
   * Indicates that the user put spaces between two subsequent errors (e.g. '2 2').
   *
   * This is an irrecoverable errors since implicit multiplication between numbers is expressly
   * prohibited.
   */
  object SpacesBetweenNumbersError : MathParsingError()

  /**
   * Indicates that the user didn't finish a parenthetical group, e.g. '(2' or '2)'.
   *
   * This is an irrecoverable error.
   */
  object UnbalancedParenthesesError : MathParsingError()

  /**
   * Indicates that the entire expression has redundant parentheses, e.g. '(2)'.
   *
   * This is an optional error.
   *
   * @property rawExpression the raw string expression that has the extra parentheses
   * @property expression the parsed sub-expression that has the extra parentheses
   */
  data class SingleRedundantParenthesesError(
    val rawExpression: String,
    val expression: MathExpression
  ) : MathParsingError()

  /**
   * Indicates that the entire expression or a sub-term has multiple redundant parentheses, e.g.
   * '((2))' or '((2)) + 1'.
   *
   * This is an optional error.
   *
   * @property rawExpression the raw string expression that has the extra parentheses
   * @property expression the parsed sub-expression that has the extra parentheses
   */
  data class MultipleRedundantParenthesesError(
    val rawExpression: String,
    val expression: MathExpression
  ) : MathParsingError()

  /**
   * Indicates that a sub-term of the expression has unnecessary parentheses, e.g. '(2) + 1'.
   *
   * This is an optional error.
   *
   * @property rawExpression the raw string expression that has the extra parentheses
   * @property expression the parsed sub-expression that has the extra parentheses
   */
  data class RedundantParenthesesForIndividualTermsError(
    val rawExpression: String,
    val expression: MathExpression
  ) : MathParsingError()

  /**
   * Indicates that an invalid symbol was encountered while parsing, e.g. '@'.
   *
   * This is an irrecoverable error.
   *
   * @property invalidSymbol the raw invalid symbol that was encountered during parsing
   */
  data class UnnecessarySymbolsError(val invalidSymbol: String) : MathParsingError()

  /**
   * Indicates that a number was encountered to the right of a variable, e.g. 'x2'.
   *
   * This is an irrecoverable error since the grammar specifically prohibits implicit multiplication
   * of numbers on the right side.
   *
   * @property number the number that was parsed on the right side of the variable
   * @property variable the variable to whose right is a number
   */
  data class NumberAfterVariableError(val number: Real, val variable: String) : MathParsingError()

  /**
   * Indicates that two binary operators were encountered with nothing between, e.g. '1 +* 2'.
   *
   * This is an irrecoverable error.
   *
   * @property operator1 the first (left) operator encountered
   * @property operator2 the second (right) operator encountered
   */
  data class SubsequentBinaryOperatorsError(
    val operator1: String,
    val operator2: String
  ) : MathParsingError()

  /**
   * Indicates that two unary operators were encountered with nothing between, e.g. '--2'.
   *
   * This is an irrecoverable error.
   */
  object SubsequentUnaryOperatorsError : MathParsingError()

  /**
   * Indicates that a binary operator was encountered without a left-hand operand, e.g. '*2'.
   *
   * This is a generally an irrecoverable error.
   *
   * Note that operators that are both unary and binary (e.g. '-') cannot trigger this error since
   * such cases will be correctly interpreted as a unary operation.
   *
   * Further, this error has one optional case for unary plus operators (i.e. with strict error
   * checking '+2' will result in this error, but is otherwise valid in non-strict mode).
   *
   * @property operator the operator to whose left is no operand
   * @property operatorSymbol the raw symbol used to represent [operator] (which can't be assumed as
   *     any particular value since multiple symbols can correspond to a single operator)
   */
  data class NoVariableOrNumberBeforeBinaryOperatorError(
    val operator: MathBinaryOperation.Operator,
    val operatorSymbol: String
  ) : MathParsingError()

  /**
   * Indicates that a binary operator was encountered without a right-hand operand, e.g. '2+'.
   *
   * This is an irrecoverable error.
   *
   * @property operator the operator to whose right is no operand
   * @property operatorSymbol the raw symbol used to represent [operator] (which can't be assumed as
   *     any particular value since multiple symbols can correspond to a single operator)
   */
  data class NoVariableOrNumberAfterBinaryOperatorError(
    val operator: MathBinaryOperation.Operator,
    val operatorSymbol: String
  ) : MathParsingError()

  /**
   * Indicates that an exponent has a variable in its power, e.g. '2^x'.
   *
   * This is an optional error to help enforce polynomial syntax.
   */
  object ExponentIsVariableExpressionError : MathParsingError()

  /**
   * Indicates that an exponent's power is too large, e.g. '3^1000'.
   *
   * This is an optional error to help avoid calculation overflow for certain answer classifiers.
   */
  object ExponentTooLargeError : MathParsingError()

  /**
   * Indicates that an exponent's power is another exponent, e.g. '2^3^4'.
   *
   * This is an optional error to help avoid calculation overflow or potential learner mistakes.
   */
  object NestedExponentsError : MathParsingError()

  /**
   * Indicates that no value was found after a square root, e.g. '2√'.
   *
   * This is an irrecoverable error.
   */
  object HangingSquareRootError : MathParsingError()

  /**
   * Indicates that a value is being divided by zero, e.g. '2/0'.
   *
   * This is an optional error that helps avoid automatic failure in classifiers. Note that the
   * absence of this error does not guarantee the expression has no divide-by-zeros (since it only
   * performs a non-evaluative cursory check).
   */
  object TermDividedByZeroError : MathParsingError()

  /**
   * Indicates that a variable was encountered in a numeric-only expression, e.g. '1+3-x'.
   *
   * This is an irrecoverable error.
   */
  object VariableInNumericExpressionError : MathParsingError()

  /**
   * Indicates that one or more non-whitelisted variables were encountered in an algebraic
   * expression.
   *
   * This is an optional error.
   *
   * @param variables the list of variables from the expression that aren't allowed
   */
  data class DisabledVariablesInUseError(val variables: List<String>) : MathParsingError()

  /**
   * Indicates that an algebraic equation is missing an equals sign, e.g. '4 x'.
   *
   * This is an irrecoverable error.
   */
  object EquationIsMissingEqualsError : MathParsingError()

  /**
   * Indicates that an algebraic equation has too many equals signs, e.g. '4 == x'.
   *
   * This is an irrecoverable error.
   */
  object EquationHasTooManyEqualsError : MathParsingError()

  /**
   * Indicates that an algebraic equation is missing either its left or right side, e.g. '4=' and
   * '=x'.
   *
   * This is an irrecoverable error.
   */
  object EquationMissingLhsOrRhsError : MathParsingError()

  /**
   * Indicates that a recognized disabled function was used, e.g. 'abs(x)'.
   *
   * This is an irrecoverable error since the proto structure for math expressions is strictly
   * limited to supported functions.
   *
   * @param functionName the name of the used prohibited function
   */
  data class InvalidFunctionInUseError(val functionName: String) : MathParsingError()

  /**
   * Indicates that a function name was started, but not completed, e.g.: 'sqr(2)'.
   *
   * This is an irrecoverable error.
   */
  object FunctionNameIncompleteError : MathParsingError()

  /**
   * Indicates a generic error that wasn't specifically recognized as any of the others.
   *
   * This is an irrecoverable error, though it may be triggered by trying to find optional errors.
   */
  object GenericError : MathParsingError()
}
