package org.oppia.android.util.math

/**
 * An error that can be encountered while trying to parse a raw number with units expression.
 *
 * All possible errors are subclasses to this sealed class. This class is modeled after
 * [MathParsingError] and represents errors specific to number-with-units parsing.
 */
sealed class NumberWithUnitsParsingError {
  /**
   * Indicates that an invalid symbol was encountered while parsing, e.g. '@'.
   *
   * This is an irrecoverable error.
   *
   * @property invalidSymbol the raw invalid symbol that was encountered during parsing
   */
  data class InvalidTokenError(val invalidSymbol: String) : NumberWithUnitsParsingError()

  /**
   * Indicates that the input expression is empty or contains only whitespace.
   *
   * This is an irrecoverable error.
   */
  object EmptyExpressionError : NumberWithUnitsParsingError()

  /**
   * Indicates that a number was expected but not found.
   *
   * This is an irrecoverable error. A number-with-units expression must begin with either
   * a currency prefix unit followed by a number, or a number directly.
   */
  object NumberExpectedError : NumberWithUnitsParsingError()

  /**
   * Indicates that at least one unit is expected but none were found after the number.
   *
   * This is an irrecoverable error. A valid number-with-units expression must have at least
   * one unit.
   */
  object UnitExpectedError : NumberWithUnitsParsingError()

  /**
   * Indicates that the user didn't finish a parenthetical group, e.g. '(m' or 'm)'.
   *
   * This is an irrecoverable error.
   */
  object UnbalancedParenthesesError : NumberWithUnitsParsingError()

  /**
   * Indicates that a number was expected after the division symbol '/' in a fraction.
   *
   * For example, '1/' is missing the denominator.
   *
   * This is an irrecoverable error.
   */
  object MissingDenominatorError : NumberWithUnitsParsingError()

  /**
   * Indicates that an exponent value was expected after the '^' operator but not found.
   *
   * For example, 'kg^' is missing the exponent.
   *
   * This is an irrecoverable error.
   */
  object MissingExponentError : NumberWithUnitsParsingError()

  /**
   * Indicates that a unit was expected after an SI prefix.
   *
   * For example, 'kilo' without a following unit like 'gram' or 'meter'.
   *
   * This is an irrecoverable error.
   *
   * @property prefix the SI prefix that was found without a following unit
   */
  data class UnitExpectedAfterSiPrefixError(
    val prefix: String
  ) : NumberWithUnitsParsingError()

  /**
   * Indicates that a currency prefix was used but no valid number followed it.
   *
   * For example, '$ kg' where a number is expected after '$'.
   *
   * This is an irrecoverable error.
   */
  object NumberExpectedAfterCurrencyPrefixError : NumberWithUnitsParsingError()

  /**
   * Indicates that a unit was expected after the division operator in a compound unit.
   *
   * For example, 'm /' where units are expected after the division.
   *
   * This is an irrecoverable error.
   */
  object UnitExpectedAfterDivisionError : NumberWithUnitsParsingError()

  /**
   * Indicates that both a currency prefix and a currency suffix were found in the same expression.
   *
   * For example, '$5 dollars' is ambiguous.
   *
   * This is an irrecoverable error.
   */
  object DuplicateCurrencyError : NumberWithUnitsParsingError()

  /**
   * Indicates that there are extra tokens remaining after the expression has been fully parsed.
   *
   * This is an irrecoverable error.
   */
  object TrailingTokensError : NumberWithUnitsParsingError()

  /**
   * Indicates a generic error that wasn't specifically recognized as any of the others.
   *
   * This is an irrecoverable error.
   */
  object GenericError : NumberWithUnitsParsingError()
}
