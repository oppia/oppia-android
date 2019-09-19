package org.oppia.app.player.audio

import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** [ViewModel] for audio-player state. */
@FragmentScope
class AudioViewModel @Inject constructor(
) : ViewModel() {

  private var currentLanguageCode: String = "en"

  val audioLanguageCode: String? by lazy {
    getLanguageCode()
  }

  fun setAudioLanguageCode(languageCode: String) {
    currentLanguageCode = languageCode
  }

  private fun getLanguageCode(): String {
    return currentLanguageCode
  }
}
