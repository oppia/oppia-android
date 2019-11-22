package org.oppia.app.topic

import org.oppia.app.customview.interaction.FractionInputInteractionView

/** Enum to store the errors of [FractionInputInteractionView] and get tab by errorCode. */
enum class FractionParsingErrors(private var errorCode: Int) {
  VALID(errorCode = 200){
    override fun toString(): String {
      return "Valid"
    }
  },INVALID_CHARS(errorCode = 100){
    override fun toString(): String {
      return "Please only use numerical digits, spaces or forward slashes (/)"
    }
  },
  INVALID_FORMAT(errorCode = 101){
    override fun toString(): String {
      return "Please enter a valid fraction (e.g., 5/3 or 1 2/3)"
    }
  },
  DIVISION_BY_ZERO(errorCode = 102){
    override fun toString(): String {
      return "Please do not put 0 in the denominator"
    }
  };

  companion object {
    fun getErrorForErrorCode(errorCode: Int): FractionParsingErrors {
      val ordinal = checkNotNull(values().map(FractionParsingErrors::errorCode)[errorCode]) {
        "No tab corresponding to errorCode: $errorCode"
      }
      return values()[ordinal]
    }
  }
}
