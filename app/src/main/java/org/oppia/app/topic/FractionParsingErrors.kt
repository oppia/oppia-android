package org.oppia.app.topic

import org.oppia.app.customview.interaction.FractionInputInteractionView

/** Enum to store the errors of [FractionInputInteractionView]. */
enum class FractionParsingErrors(errorCode: Int, error: String) {
  VALID(errorCode = 200, error = "Valid"),
  INVALID_CHARS(errorCode = 100, error = "Please only use numerical digits, spaces or forward slashes (/)"),
  INVALID_FORMAT(errorCode = 101, error = "Please enter a valid fraction (e.g., 5/3 or 1 2/3)"),
  DIVISION_BY_ZERO(errorCode = 102, error = "Please do not put 0 in the denominator");

  private var errorCode: Int
  private var error: String

  init {
    this.errorCode = errorCode
    this.error = error
  }

  fun getError(): String {
    return this.error
  }
}
