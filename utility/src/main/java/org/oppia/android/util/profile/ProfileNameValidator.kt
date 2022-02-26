package org.oppia.android.util.profile

import javax.inject.Inject
import javax.inject.Singleton

/** Utility to validate that profile names are correctly formatted. */
@Singleton
class ProfileNameValidator @Inject constructor() {
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
    name.forEach {
      if (!ProfileNameValidatorUtil().isNameValid(it)) {
        return false
      }
    }
    return true
  }

  private fun noRepeatedUseOfAllowedSymbols(name: String): Boolean {
    return !name.contains(noRepeatedAllowedSymbolsRegex)
  }
}
