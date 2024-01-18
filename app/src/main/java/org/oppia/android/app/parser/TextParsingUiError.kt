package org.oppia.android.app.parser

import androidx.annotation.StringRes
import org.oppia.android.R
import org.oppia.android.app.translation.AppLanguageResourceHandler

/** Enum to store the errors of [TextInputInteractionView]. */
enum class TextParsingUiError(@StringRes private var error: Int?) {
  /** Corresponds to [TextParsingError.VALID]. */
  VALID(error = null),

  /** Corresponds to [TextParsingError.EMPTY_INPUT]. */
  EMPTY_INPUT(error = R.string.text_error_empty_input);

  /**
   * Returns the string corresponding to this error's string resources, or null if there is none.
   */
  fun getErrorMessageFromStringRes(resourceHandler: AppLanguageResourceHandler): String? =
    error?.let(resourceHandler::getStringInLocale)

  companion object {
    /**
     * Returns the [TextParsingUiError] corresponding to the specified [TextParsingError].
     */
    fun createFromParsingError(parsingError: TextParsingError): TextParsingUiError {
      return when (parsingError) {

        TextParsingError.VALID -> VALID

        TextParsingError.EMPTY_INPUT -> EMPTY_INPUT
      }
    }
  }
}
