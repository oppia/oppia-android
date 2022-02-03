package org.oppia.android.util.profile

import javax.inject.Inject
import javax.inject.Singleton

/** Utility to validate profile names */
@Singleton
class NameValidator @Inject constructor() {
  private val letterAndSymbolsRegex by lazy {
    Regex("^.[${NameValidatorUtil.lettersAndSymbolsRegexString}]+\$")
  }

  private val noRepeatedAllowedSymbolsRegex by lazy { Regex("[.'-]{2}") }

  /**
   * Validates names for a profile
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
