package org.oppia.app.player.audio

/** Interface to receive selected language from [LanguageDialogFragment]. */
interface LanguageInterface {
  /** Play the audio corresponding to the language-selected. */
  fun onLanguageSelected(currentLanguageCode: String)

  /** Open the language selection dialog. */
  fun languageSelectionClicked()

  /**
   * Returns whether the user is actively seeking a new audio position, that is, dragging the
   * knob to a new position in the audio track.
   * */
  fun getUserIsSeeking(): Boolean

  /** Returns the position of the knob on the audio track. */
  fun getUserPosition(): Int
}
