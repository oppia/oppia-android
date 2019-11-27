package org.oppia.app.topic

import android.content.Context
import androidx.annotation.StringRes
import org.oppia.app.R
import org.oppia.app.customview.interaction.FractionInputInteractionView

/** Enum to store the errors of [FractionInputInteractionView]. */
enum class FractionParsingErrors(@StringRes error: Int) {
  VALID(error = R.string.valid),
  INVALID_CHARS(error = R.string.invalid_chars),
  INVALID_FORMAT(error = R.string.invalid_format),
  DIVISION_BY_ZERO(error = R.string.divide_by_zero);

  private var error: Int

  init {
    this.error = error
  }

  fun getErrorMessageFromStringRes(context: Context): String {
    return context.getString(this.error)
  }
}
