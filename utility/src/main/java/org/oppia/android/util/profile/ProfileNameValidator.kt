package org.oppia.android.util.profile

import dagger.Module
import javax.inject.Inject

/** Utility to validate that profile names are correctly formatted. */
@Module
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

  /** Validates if the character in the name is an alphabet or an allowed symbol or not. */
  private fun onlyLettersAndAllowedSymbols(name: String): Boolean {
    name.forEach {
      if (!(it.isAlphabetic() || isSymbolAllowed(it))) {
        return false
      }
    }
    return true
  }

  private fun noRepeatedUseOfAllowedSymbols(name: String): Boolean {
    return !name.contains(noRepeatedAllowedSymbolsRegex)
  }

  private fun isSymbolAllowed(symbol: Char): Boolean {
    return symbol == '.' || symbol == '-' || symbol == '\''
  }

  private fun Char.isAlphabetic(): Boolean {
    /**
     * The following categories are based on Kotlin's Char.isLetter() and Character.isAlphabetic().
     * It also adds spacing marks which can be safely ignored since they modify other Unicode
     * characters (which are then being verified as being letters). Note also that 'LETTER_NUMBER'
     * is included for roman numerals and other number-like letters since these can sometimes show
     * up in names.
     */
    return when (category) {
      CharCategory.UPPERCASE_LETTER, CharCategory.LOWERCASE_LETTER, CharCategory.TITLECASE_LETTER,
      CharCategory.MODIFIER_LETTER, CharCategory.OTHER_LETTER, CharCategory.LETTER_NUMBER,
      CharCategory.COMBINING_SPACING_MARK, CharCategory.NON_SPACING_MARK -> true
      else -> false
    }
  }
}
