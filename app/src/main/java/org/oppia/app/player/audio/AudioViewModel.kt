package org.oppia.app.player.audio

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** [ViewModel] for audio-player state. */
@FragmentScope
class AudioViewModel @Inject constructor(
) : ViewModel() {

  val currentLanguageCode = ObservableField<String>("en")

  fun setAudioLanguageCode(languageCode: String) {
    currentLanguageCode.set(languageCode)
  }
}
