package org.oppia.app.parser

import android.content.Context
import androidx.annotation.StringRes
import org.oppia.app.R
import org.oppia.app.model.RatioExpression
import org.oppia.domain.util.normalizeWhitespace
import org.oppia.domain.util.removeWhitespace

/**
 * Utility for parsing [RatioExpression]s from strings and validating strings can be parsed
 * into a [RatioExpression].
 */
class StringToRatioParser {
  private val invalidCharsRegex =
    """^[\d\s/:]+$""".toRegex()
  private val invalidRatioRegex =
    """^(\s)?\d+(\s)?(:(\s)?\d+(\s)?)+(\s)?$""".toRegex()

  /**
   * Returns a [RatioParsingError] for the specified text input if it's an invalid ratio, or
   * [RatioParsingError.VALID] if no issues are found. Note that a valid ratio returned by this method is guaranteed
   * to be parsed correctly by [parseRatioOrNull].
   *
   * This method should only be used when a user tries submitting an answer. Real-time error detection should be done
   * using [getRealTimeAnswerError], instead.
   */
  fun getSubmitTimeError(text: String, numberOfTerms: Int): RatioParsingError {
    val normalized = text.normalizeWhitespace()
    // Check for invalid format before parsing otherwise it would throw an error instead.
    if (!normalized.matches(invalidRatioRegex)) {
      return RatioParsingError.INVALID_FORMAT
    }
    val ratio = parseRatioOrThrow(normalized)
    return when {
      ratio == null -> RatioParsingError.INVALID_FORMAT
      numberOfTerms != 0 && ratio.ratioComponentCount != numberOfTerms -> {
        RatioParsingError.INVALID_SIZE
      }
      ratio.ratioComponentList.contains(0) -> RatioParsingError.INCLUDES_ZERO
      else -> RatioParsingError.VALID
    }
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
      text.contains("::") -> RatioParsingError.INVALID_COLONS
      else -> RatioParsingError.VALID
    }
  }

  /** Returns a [RatioExpression] parse from the specified raw text string. */
  fun parseRatioOrNull(text: String): RatioExpression? {
    val normalizedText = text.removeWhitespace()
    val rawComponents = normalizedText.split(':')
    val components = rawComponents.mapNotNull { it.toIntOrNull() }
    return if (rawComponents.size == components.size) {
      RatioExpression.newBuilder().addAllRatioComponent(components).build()
    } else null // Something is incorrect in the original formatting.
  }

  /** Returns a [RatioExpression] parse from the specified raw text string. */
  fun parseRatioOrThrow(text: String): RatioExpression? {
    return parseRatioOrNull(text)
      ?: throw IllegalArgumentException("Incorrectly formatted ratio: $text")
  }

  /** Enum to store the errors of [RatioInputInteractionView]. */
  enum class RatioParsingError(@StringRes private var error: Int?) {
    VALID(error = null),
    INVALID_CHARS(error = R.string.ratio_error_invalid_chars),
    INVALID_FORMAT(error = R.string.ratio_error_invalid_format),
    INVALID_COLONS(error = R.string.ratio_error_invalid_colons),
    INVALID_SIZE(error = R.string.ratio_error_invalid_size),
    INCLUDES_ZERO(error = R.string.ratio_error_includes_zero);

    /** Returns the string corresponding to this error's string resources, or null if there is none. */
    fun getErrorMessageFromStringRes(context: Context): String? {
      return error?.let(context::getString)
    }
  }
}
