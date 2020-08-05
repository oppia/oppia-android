package org.oppia.app.player.audio

/** Interface to receive selected language from [LanguageDialogFragment]. */
interface LanguageInterface {
  /** Play the audio corresponding to the language-selected. */
  fun onLanguageSelected(currentLanguageCode: String)

  /** Open the language selection dialog. */
  fun languageSelectionClicked()

  fun getUserIsSeeking(): Boolean

  fun getUserPosition(): Int
}
