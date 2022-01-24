package org.oppia.android.util.profile

class NameValidator {
  companion object {
    private const val MATCH_INTERNATIONAL_LETTERS_REJECT_SPACES =
      "^.[\\w\\u00BF-\\u1FFF\\u2C00-\\uD7FF.'\\-]+\$"

    private const val REJECT_SYMBOLS = "^[^#*!@\$%^&()_+=\\\\|\\]\\[\":;?/><,`~{}]*\$"

    private const val REJECT_REPEATED_USE_OF_ALLOWED_SYMBOLS = "[\\.'-]{2}"

    fun validate(name: String): Boolean {
      return (
        notEmptyNoSpacesAndContainsLetters(name) &&
          noNumbers(name) &&
          noSymbols(name) &&
          noRepeatedUseOfAllowedSymbols(name)
        )
    }

    private fun notEmptyNoSpacesAndContainsLetters(name: String): Boolean {
      return name.matches(Regex(MATCH_INTERNATIONAL_LETTERS_REJECT_SPACES))
    }

    private fun noNumbers(name: String): Boolean {
      return name.none { it.isDigit() }
    }

    private fun noSymbols(name: String): Boolean {
      return name.matches(Regex(REJECT_SYMBOLS))
    }

    private fun noRepeatedUseOfAllowedSymbols(name: String): Boolean {
      return !name.contains(Regex(REJECT_REPEATED_USE_OF_ALLOWED_SYMBOLS))
    }
  }
}
