package org.oppia.app.player.state.audio

import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** [ViewModel] for user app usage history. */
@FragmentScope
class AudioViewModel @Inject constructor() : ViewModel() {

  val audioLanguageCode: String? by lazy {
    getCurrentLanguageCode()
  }

  private fun getCurrentLanguageCode(): String? {
    return getDefaultLanguageCode()
  }

  private fun getDefaultLanguageCode():String{
    // This code can come from save language preference or else its is en
    return "en"
  }
}
