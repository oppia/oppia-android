package org.oppia.app.options

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
    routeToAudioLanguageListListener.routeAudioLanguageList(audioLanguage.get())
  }
}
