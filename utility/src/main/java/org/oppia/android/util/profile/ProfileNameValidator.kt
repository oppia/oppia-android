package org.oppia.android.util.profile

import javax.inject.Inject

/** Utility to validate that profile names are correctly formatted. */
class ProfileNameValidator @Inject constructor() {
  private val space = ' '

  /**
   * Validates a profile name to ensure that it isn't confusingly formatted or contains invalid
   * characters (e.g. non-letter characters, per the Unicode standard for letter categories, plus
   * apostrophes and dashes)..
   *
   * @param name name of the profile
   * @return whether the profile name whether is a valid, acceptable name
   */
  fun isNameValid(name: String): Boolean {
    return containsOnlyLettersAndSpaces(name) && notOnlySpaces(name)
  }

  /** Validates if the character in the name is an alphabet or a space. */
  private fun containsOnlyLettersAndSpaces(name: String): Boolean {
    name.forEach {
      if (!(it.isAlphabetic() || it == space)) {
        return false
      }
    }
    return true
  }

  /** Ensures that name does not contains only spaces. */
  private fun notOnlySpaces(name: String): Boolean {
    name.forEach {
      if (it.isAlphabetic()) {
        return true
      }
    }
    return false
  }

  private fun Char.isAlphabetic(): Boolean {
     /*
      The following categories are based on Kotlin's Char.isLetter() and Character.isAlphabetic().
      It also adds spacing marks which can be safely ignored since they modify other Unicode
      characters (which are then being verified as being letters). Note also that 'LETTER_NUMBER'
      is included for roman numerals and other number-like letters since these can sometimes show
      up in names.
      */
    return when (category) {
      CharCategory.UPPERCASE_LETTER, CharCategory.LOWERCASE_LETTER, CharCategory.TITLECASE_LETTER,
      CharCategory.MODIFIER_LETTER, CharCategory.OTHER_LETTER, CharCategory.LETTER_NUMBER,
      CharCategory.COMBINING_SPACING_MARK, CharCategory.NON_SPACING_MARK -> true
      else -> false
    }
  }
}
