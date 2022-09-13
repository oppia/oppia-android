package org.oppia.android.app.options

import org.oppia.android.app.model.AudioLanguage

/** Listener for when the a language is selected for the [AudioLanguageFragment]. */
interface AudioLanguageRadioButtonListener {
  /** Called when the user selected a new [AudioLanguage] to use as their default preference. */
  fun onLanguageSelected(audioLanguage: AudioLanguage)
}
