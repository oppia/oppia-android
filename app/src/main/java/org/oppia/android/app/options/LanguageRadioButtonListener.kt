package org.oppia.android.app.options

/**
 * Listener for when the language is selected from the [AppLanguageFragment] or
 * [AudioLanguageFragment].
 */
interface LanguageRadioButtonListener {
  fun onLanguageSelected(selectedLanguage: String)
}
