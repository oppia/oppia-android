package org.oppia.android.util.namevalidator

class NameValidator {
  companion object {
    private const val regContainsLettersAndNoWhiteSpace =
      "^.[\\w\\u00BF-\\u1FFF\\u2C00-\\uD7FF.'\\-]+\$"

    private const val regContainsNoSymbols = "^[^#*!@\$%^&()_+=\\\\|\\]\\[\":;?/><,`~{}]*\$"

    private const val regContainsRepeatedAllowedSymbols = "[\\.'-]{2}"

    fun nameAllowed(name: String): Boolean {
      return (
        notEmptyNoSpacesAndContainsLetters(name) &&
          noNumbers(name) &&
          noSymbols(name) &&
          noRepeatedUseOfAllowedSymbols(name)
        )
    }

    private fun notEmptyNoSpacesAndContainsLetters(name: String): Boolean {
      return name.matches(Regex(regContainsLettersAndNoWhiteSpace))
    }

    private fun noNumbers(name: String): Boolean {
      return name.none {
        it.isDigit()
      }
    }

    private fun noSymbols(name: String): Boolean {
      return name.matches(Regex(regContainsNoSymbols))
    }

    private fun noRepeatedUseOfAllowedSymbols(name: String): Boolean {
      return !name.contains(Regex(regContainsRepeatedAllowedSymbols))
    }
  }
}
