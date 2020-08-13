package org.oppia.app.player.audio

/** Interface to receive selected language from [LanguageDialogFragment]. */
interface LanguageInterface {
  /** Play the audio corresponding to the language-selected. */
  fun onLanguageSelected(currentLanguageCode: String)

  /** Open the language selection dialog. */
  fun languageSelectionClicked()

  /** Getter for userIsSeeking in AudioFragment */
  fun getUserIsSeeking(): Boolean

  /** Getter for userPosition in AudioFragment */
  fun getUserPosition(): Int
}
