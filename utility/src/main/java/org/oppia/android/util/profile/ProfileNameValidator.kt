package org.oppia.android.util.profile

import javax.inject.Inject
import javax.inject.Singleton

/** Utility to validate that profile names are correctly formatted. */
@Singleton
class ProfileNameValidator @Inject constructor() {
  private val allLanguageLettersRegex = "\\p{L}"
  private val allowedSymbolsRegex = ".'\\-"

  private val hindiRegex = "\\u0900-\\u097F"

  /**
   * Some languages may not be detected properly by [allLanguageLettersRegex].
   * In that case, the range of the Unicode character set will have to be added separately
   * for that language to be processed by Regex.
   *
   * To add a language,
   * create a variable containing Unicode range and add to the end of [lettersAndSymbolsRegexString]
   */
  private val lettersAndSymbolsRegexString =
    "$allLanguageLettersRegex$allowedSymbolsRegex$hindiRegex"

  private val letterAndSymbolsRegex by lazy {
    Regex("^.[$lettersAndSymbolsRegexString]+\$")
  }

  private val noRepeatedAllowedSymbolsRegex by lazy { Regex("[.'-]{2}") }

  /**
   * Validates a profile name to ensure that it isn't confusingly formatted
   * or contains invalid characters.
   *
   * @param name name of the profile
   * @return if the profile name is allowed or not
   */
  fun isNameValid(name: String): Boolean {
    return (onlyLettersAndAllowedSymbols(name) && noRepeatedUseOfAllowedSymbols(name))
  }

  private fun onlyLettersAndAllowedSymbols(name: String): Boolean {
    return name.matches(letterAndSymbolsRegex)
  }

  private fun noRepeatedUseOfAllowedSymbols(name: String): Boolean {
    return !name.contains(noRepeatedAllowedSymbolsRegex)
  }
}
