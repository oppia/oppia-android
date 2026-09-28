package org.oppia.android.app.parser

import androidx.annotation.StringRes
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.view.models.R
import org.oppia.android.util.math.NumberWithUnitsParsingError

/** Enum to store the errors of [NumberWithUnitsInputInteractionView]. */
enum class NumberWithUnitsParsingUiError(@StringRes private var error: Int?) {
  /** Corresponds to a valid input with no errors. */
  VALID(error = null),

  /** Corresponds to [NumberWithUnitsParsingError.InvalidTokenError]. */
  INVALID_TOKEN(error = R.string.number_with_units_error_invalid_token),

  /** Corresponds to [NumberWithUnitsParsingError.InvalidUnitError]. */
  INVALID_UNIT(error = R.string.number_with_units_error_invalid_unit),

  /** Corresponds to [NumberWithUnitsParsingError.EmptyExpressionError]. */
  EMPTY_EXPRESSION(error = R.string.number_with_units_error_empty_input),

  /** Corresponds to [NumberWithUnitsParsingError.NumberExpectedError]. */
  NUMBER_EXPECTED(error = R.string.number_with_units_error_number_expected),

  /** Corresponds to [NumberWithUnitsParsingError.UnitExpectedError]. */
  UNIT_EXPECTED(error = R.string.number_with_units_error_unit_expected),

  /** Corresponds to [NumberWithUnitsParsingError.UnbalancedParenthesesError]. */
  UNBALANCED_PARENTHESES(error = R.string.number_with_units_error_unbalanced_parentheses),

  /** Corresponds to [NumberWithUnitsParsingError.MissingDenominatorError]. */
  MISSING_DENOMINATOR(error = R.string.number_with_units_error_missing_denominator),

  /** Corresponds to [NumberWithUnitsParsingError.MissingExponentError]. */
  MISSING_EXPONENT(error = R.string.number_with_units_error_missing_exponent),

  /** Corresponds to [NumberWithUnitsParsingError.UnitExpectedAfterSiPrefixError]. */
  UNIT_EXPECTED_AFTER_SI_PREFIX(
    error = R.string.number_with_units_error_unit_expected_after_si_prefix
  ),

  /** Corresponds to [NumberWithUnitsParsingError.NumberExpectedAfterCurrencyPrefixError]. */
  NUMBER_EXPECTED_AFTER_CURRENCY_PREFIX(
    error = R.string.number_with_units_error_number_expected_after_currency
  ),

  /** Corresponds to [NumberWithUnitsParsingError.UnitExpectedAfterDivisionError]. */
  UNIT_EXPECTED_AFTER_DIVISION(
    error = R.string.number_with_units_error_unit_expected_after_division
  ),

  /** Corresponds to [NumberWithUnitsParsingError.DuplicateCurrencyError]. */
  DUPLICATE_CURRENCY(error = R.string.number_with_units_error_duplicate_currency),

  /** Corresponds to [NumberWithUnitsParsingError.TrailingTokensError]. */
  TRAILING_TOKENS(error = R.string.number_with_units_error_trailing_tokens),

  /** Corresponds to [NumberWithUnitsParsingError.GenericError]. */
  GENERIC_ERROR(error = R.string.number_with_units_error_generic);

  fun getErrorMessageFromStringRes(resourceHandler: AppLanguageResourceHandler): String? =
    error?.let(resourceHandler::getStringInLocale)

  companion object {
    fun createFromParsingError(
      parsingError: NumberWithUnitsParsingError
    ): NumberWithUnitsParsingUiError {
      return when (parsingError) {
        is NumberWithUnitsParsingError.InvalidTokenError -> INVALID_TOKEN
        is NumberWithUnitsParsingError.InvalidUnitError -> INVALID_UNIT
        is NumberWithUnitsParsingError.EmptyExpressionError -> EMPTY_EXPRESSION
        is NumberWithUnitsParsingError.NumberExpectedError -> NUMBER_EXPECTED
        is NumberWithUnitsParsingError.UnitExpectedError -> UNIT_EXPECTED
        is NumberWithUnitsParsingError.UnbalancedParenthesesError -> UNBALANCED_PARENTHESES
        is NumberWithUnitsParsingError.MissingDenominatorError -> MISSING_DENOMINATOR
        is NumberWithUnitsParsingError.MissingExponentError -> MISSING_EXPONENT
        is NumberWithUnitsParsingError.UnitExpectedAfterSiPrefixError -> {
          UNIT_EXPECTED_AFTER_SI_PREFIX
        }
        is NumberWithUnitsParsingError.NumberExpectedAfterCurrencyPrefixError -> {
          NUMBER_EXPECTED_AFTER_CURRENCY_PREFIX
        }
        is NumberWithUnitsParsingError.UnitExpectedAfterDivisionError -> {
          UNIT_EXPECTED_AFTER_DIVISION
        }
        is NumberWithUnitsParsingError.DuplicateCurrencyError -> DUPLICATE_CURRENCY
        is NumberWithUnitsParsingError.TrailingTokensError -> TRAILING_TOKENS
        is NumberWithUnitsParsingError.GenericError -> GENERIC_ERROR
      }
    }
  }
}
