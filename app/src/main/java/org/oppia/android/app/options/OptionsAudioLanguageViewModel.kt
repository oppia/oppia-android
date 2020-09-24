package org.oppia.app.options

import androidx.databinding.ObservableField

/** Audio language settings view model for the recycler view in [OptionsFragment]. */
class OptionsAudioLanguageViewModel(
  private val routeToAudioLanguageListListener: RouteToAudioLanguageListListener,
  private val loadAudioLanguageListListener: LoadAudioLanguageListListener
) : OptionsItemViewModel() {
  val audioLanguage = ObservableField<String>("")

  fun setAudioLanguage(audioLanguageValue: String) {
    audioLanguage.set(audioLanguageValue)
  }

  fun onAudioLanguageClicked() {
    if (isMultipane.get()!!) {
      loadAudioLanguageListListener.loadAudioLanguageFragment(audioLanguage.get()!!)
    } else {
      routeToAudioLanguageListListener.routeAudioLanguageList(audioLanguage.get())
    }
  }
}
