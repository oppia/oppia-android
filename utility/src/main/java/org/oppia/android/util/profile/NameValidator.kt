package org.oppia.android.util.profile

import javax.inject.Inject
import javax.inject.Singleton

/** Utility to validate profile names*/
@Singleton
class NameValidator @Inject constructor() {
  companion object {
    private const val MATCH_LETTERS_AND_ALLOWED_SYMBOLS =
      "^.[\\p{L}.'\\-]+\$"

    private const val REJECT_REPEATED_USE_OF_ALLOWED_SYMBOLS = "[\\.'-]{2}"

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
      return name.matches(Regex(MATCH_LETTERS_AND_ALLOWED_SYMBOLS))
    }

    private fun noRepeatedUseOfAllowedSymbols(name: String): Boolean {
      return !name.contains(Regex(REJECT_REPEATED_USE_OF_ALLOWED_SYMBOLS))
    }
  }
}
