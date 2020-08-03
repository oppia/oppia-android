package org.oppia.app.options

import android.util.Log
import androidx.databinding.ObservableField

/** Audio language settings view model for the recycler view in [OptionsFragment]. */
class OptionsAudioLanguageViewModel(
  private val routeToAudioLanguageListListener: RouteToAudioLanguageListListener
) : OptionsItemViewModel() {
  val audioLanguage = ObservableField<String>("")

  fun setAudioLanguage(audioLanguageValue: String) {
    audioLanguage.set(audioLanguageValue)
  }

  fun onAudioLanguageClicked() {
    if (isMultipaneOptions.get()!!) {
      Log.d("Multipane", "OptionsAudioLanguageViewModel")
      // TODO add this fragment to "multipaneOptionsContainer"
    } else {
      routeToAudioLanguageListListener.routeAudioLanguageList(audioLanguage.get())
    }
  }
}
