package org.oppia.app.player.audio

/** Interface to receive selected language from [LanguageDialogFragment] */
interface LanguageInterface {
  fun onLanguageSelected(currentLanguageCode: String)
}
