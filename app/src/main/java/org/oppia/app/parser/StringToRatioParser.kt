package org.oppia.app.parser

import android.content.Context
import androidx.annotation.StringRes
import org.oppia.app.R
import org.oppia.app.model.RatioExpression

/** This class contains method that helps to parse string to ratio. */
class StringToRatioParser {
  private val invalidCharsRegex =
    """^[\d\s/\:\.]+$""".toRegex()
  private val invalidRatioRegex =
    """^(\s)*(\d+((\s)*:(\s)*\d+)+)(\s)*$""".toRegex()

  /**
   * Returns a [RatioParsingError] for the specified text input if it's an invalid ratio, or
   * [RatioParsingError.VALID] if no issues are found. Note that a valid ratio returned by this method is guaranteed
   * to be parsed correctly by [parseRatioFromString].
   *
   * This method should only be used when a user tries submitting an answer. Real-time error detection should be done
   * using [getRealTimeAnswerError], instead.
   */
  fun getSubmitTimeError(text: String, numberOfTerms: Int): RatioParsingError {
    val ratio = parseRatio(text)
    return if (ratio!!.serializedSize < numberOfTerms) {
      RatioParsingError.INVALID_FORMAT
    } else
      RatioParsingError.VALID
  }

  /**
   * Returns a [RatioParsingError] for obvious incorrect ratio formatting issues for the specified raw text, or
   * [RatioParsingError.VALID] if not such issues are found.
   *
   * Note that this method returning a valid result does not guarantee the text is a valid ratio--
   * [getSubmitTimeError] should be used for that, instead. This method is meant to be used as a quick sanity check for
   * general validity, not for definite correctness.
   */
  fun getRealTimeAnswerError(text: String): RatioParsingError {
    return when {
      !text.matches(invalidCharsRegex) -> RatioParsingError.INVALID_CHARS
      text.contains("::") -> RatioParsingError.INVALID_COLORS
      !text.matches(invalidRatioRegex) -> RatioParsingError.INVALID_FORMAT
      else -> RatioParsingError.VALID
    }
  }

  /** Returns a [RatioExpression] parse from the specified raw text string. */
  fun parseRatio(text: String): RatioExpression? {
    val components: List<Int> = text.split(':').map { it.toInt() }
    return RatioExpression.newBuilder()
      .addAllRatioComponent(components)
      .build()
  }

  /** Returns a [RatioExpression] parse from the specified raw text string. */
  fun parseRatioFromString(text: String): RatioExpression {
    return parseRatio(text)
      ?: throw IllegalArgumentException("Incorrectly formatted ratio: $text")
  }

  /** Enum to store the errors of [RatioInputInteractionView]. */
  enum class RatioParsingError(@StringRes private var error: Int?) {
    VALID(error = null),
    INVALID_CHARS(error = R.string.ratio_error_invalid_chars),
    INVALID_FORMAT(error = R.string.ratio_error_invalid_format),
    INVALID_COLORS(error = R.string.ratio_error_invalid_colons);

    /** Returns the string corresponding to this error's string resources, or null if there is none. */
    fun getErrorMessageFromStringRes(context: Context): String? {
      return error?.let(context::getString)
    }
  }
}
