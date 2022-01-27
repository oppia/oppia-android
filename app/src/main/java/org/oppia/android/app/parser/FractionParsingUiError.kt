package org.oppia.android.app.parser

import androidx.annotation.StringRes
import org.oppia.android.R
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.math.FractionParser.FractionParsingError

/** Enum to store the errors of [FractionInputInteractionView]. */
enum class FractionParsingUiError(@StringRes private var error: Int?) {
  VALID(error = null),
  INVALID_CHARS(error = R.string.fraction_error_invalid_chars),
  INVALID_FORMAT(error = R.string.fraction_error_invalid_format),
  DIVISION_BY_ZERO(error = R.string.fraction_error_divide_by_zero),
  NUMBER_TOO_LONG(error = R.string.fraction_error_larger_than_seven_digits);

  /**
   * Returns the string corresponding to this error's string resources, or null if there is none.
   */
  fun getErrorMessageFromStringRes(resourceHandler: AppLanguageResourceHandler): String? =
    error?.let(resourceHandler::getStringInLocale)

  companion object {
    /**
     * Returns the [FractionParsingUiError] corresponding to the specified [FractionParsingError].
     */
    fun createFromParsingError(parsingError: FractionParsingError): FractionParsingUiError {
      return when (parsingError) {
        FractionParsingError.VALID -> VALID
        FractionParsingError.INVALID_CHARS -> INVALID_CHARS
        FractionParsingError.INVALID_FORMAT -> INVALID_FORMAT
        FractionParsingError.DIVISION_BY_ZERO -> DIVISION_BY_ZERO
        FractionParsingError.NUMBER_TOO_LONG -> NUMBER_TOO_LONG
      }
    }
  }
}
