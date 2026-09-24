package org.oppia.android.app.parser

import androidx.annotation.StringRes
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.view.models.R
import org.oppia.android.util.math.NumberWithUnitsParsingError

/** Enum to store the errors of [NumberWithUnitsInputInteractionView]. */
enum class NumberWithUnitsParsingUiError(@StringRes private var error: Int?) {
  VALID(error = null),
  INVALID_TOKEN(error = R.string.number_with_units_error_invalid_token),
  INVALID_UNIT(error = R.string.number_with_units_error_invalid_unit),
  EMPTY_EXPRESSION(error = R.string.number_with_units_error_empty_input),
  NUMBER_EXPECTED(error = R.string.number_with_units_error_number_expected),
  UNIT_EXPECTED(error = R.string.number_with_units_error_unit_expected),
  UNBALANCED_PARENTHESES(error = R.string.number_with_units_error_unbalanced_parentheses),
  MISSING_DENOMINATOR(error = R.string.number_with_units_error_missing_denominator),
  MISSING_EXPONENT(error = R.string.number_with_units_error_missing_exponent),
  UNIT_EXPECTED_AFTER_SI_PREFIX(error = R.string.number_with_units_error_unit_expected_after_si_prefix),
  NUMBER_EXPECTED_AFTER_CURRENCY_PREFIX(error = R.string.number_with_units_error_number_expected_after_currency),
  UNIT_EXPECTED_AFTER_DIVISION(error = R.string.number_with_units_error_unit_expected_after_division),
  DUPLICATE_CURRENCY(error = R.string.number_with_units_error_duplicate_currency),
  TRAILING_TOKENS(error = R.string.number_with_units_error_trailing_tokens),
  GENERIC_ERROR(error = R.string.number_with_units_error_generic);

  fun getErrorMessageFromStringRes(resourceHandler: AppLanguageResourceHandler): String? =
    error?.let(resourceHandler::getStringInLocale)

  companion object {
    fun createFromParsingError(parsingError: NumberWithUnitsParsingError): NumberWithUnitsParsingUiError {
      return when (parsingError) {
        is NumberWithUnitsParsingError.InvalidTokenError -> INVALID_TOKEN
        is NumberWithUnitsParsingError.InvalidUnitError -> INVALID_UNIT
        is NumberWithUnitsParsingError.EmptyExpressionError -> EMPTY_EXPRESSION
        is NumberWithUnitsParsingError.NumberExpectedError -> NUMBER_EXPECTED
        is NumberWithUnitsParsingError.UnitExpectedError -> UNIT_EXPECTED
        is NumberWithUnitsParsingError.UnbalancedParenthesesError -> UNBALANCED_PARENTHESES
        is NumberWithUnitsParsingError.MissingDenominatorError -> MISSING_DENOMINATOR
        is NumberWithUnitsParsingError.MissingExponentError -> MISSING_EXPONENT
        is NumberWithUnitsParsingError.UnitExpectedAfterSiPrefixError -> UNIT_EXPECTED_AFTER_SI_PREFIX
        is NumberWithUnitsParsingError.NumberExpectedAfterCurrencyPrefixError -> NUMBER_EXPECTED_AFTER_CURRENCY_PREFIX
        is NumberWithUnitsParsingError.UnitExpectedAfterDivisionError -> UNIT_EXPECTED_AFTER_DIVISION
        is NumberWithUnitsParsingError.DuplicateCurrencyError -> DUPLICATE_CURRENCY
        is NumberWithUnitsParsingError.TrailingTokensError -> TRAILING_TOKENS
        is NumberWithUnitsParsingError.GenericError -> GENERIC_ERROR
      }
    }
  }
}
