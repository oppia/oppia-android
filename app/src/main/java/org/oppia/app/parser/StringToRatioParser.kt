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
    val ratio = parseRatioOrThrow(text)
    return if (ratio.ratioComponentCount < numberOfTerms) {
      RatioParsingError.INVALID_SIZE
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
      text.contains("::") -> RatioParsingError.INVALID_COLONS
      !text.matches(invalidRatioRegex) -> RatioParsingError.INVALID_FORMAT
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
  fun parseRatioOrThrow(text: String): RatioExpression {
    return parseRatioOrNull(text)
      ?: throw IllegalArgumentException("Incorrectly formatted ratio: $text")
  }

  /** Enum to store the errors of [RatioInputInteractionView]. */
  enum class RatioParsingError(@StringRes private var error: Int?) {
    VALID(error = null),
    INVALID_CHARS(error = R.string.ratio_error_invalid_chars),
    INVALID_FORMAT(error = R.string.ratio_error_invalid_format),
    INVALID_COLONS(error = R.string.ratio_error_invalid_colons),
    INVALID_SIZE(error = R.string.ratio_error_invalid_size);

    /** Returns the string corresponding to this error's string resources, or null if there is none. */
    fun getErrorMessageFromStringRes(context: Context): String? {
      return error?.let(context::getString)
    }
  }
}

private fun String.removeWhitespace(): String {
  return this.replace(" ", "")
}

/**
 * Returns this Ratio in string format.
 * E.g. [1, 2, 3] will yield to 1 to 2 to 3
 */
fun RatioExpression.toAccessibleAnswerString(context: Context): String {
  return ratioComponentList.joinToString(
    context.getString(R.string.ratio_content_description_separator)
  )
}
