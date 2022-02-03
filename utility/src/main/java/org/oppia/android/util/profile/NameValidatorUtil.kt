package org.oppia.android.util.profile

/** Utility class for [NameValidator]. */
object NameValidatorUtil {
  /**
   * Some languages may not be detected properly by [allLanguageLettersRegex].
   * In that case, the range of the Unicode character set will have to be added separately
   * for that language to be processed by Regex.
   *
   * To add a language,
   * create a variable containing Unicode range and add to the end of [lettersAndSymbolsRegexString]
   */

  private const val allLanguageLettersRegex = "\\p{L}"
  private const val allowedSymbolsRegex = ".'\\-"

  private const val hindiRegex = "\\u0900-\\u097F"

  const val lettersAndSymbolsRegexString = "$allLanguageLettersRegex$hindiRegex$allowedSymbolsRegex"
}
