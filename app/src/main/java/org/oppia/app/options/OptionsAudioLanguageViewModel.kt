package org.oppia.app.options

import androidx.databinding.ObservableField

/** Audio language settings view model for the recycler view in [OptionsFragment]. */
class OptionsAudioLanguageViewModel(
  private val routeToAudioLanguageListListener: RouteToAudioLanguageListListener,
  private val loadDefaultAudioFragmentListener: LoadDefaultAudioFragmentListener
) : OptionsItemViewModel() {
  val audioLanguage = ObservableField<String>("")

  fun setAudioLanguage(audioLanguageValue: String) {
    audioLanguage.set(audioLanguageValue)
  }

  fun loadAudioLanguageFragment() {
    loadDefaultAudioFragmentListener.loadDefaultAudioFragment(audioLanguage.get()!!)
  }

  fun onAudioLanguageClicked() {
    if (isMultipaneOptions.get()!!) {
      loadAudioLanguageFragment()
    } else {
      routeToAudioLanguageListListener.routeAudioLanguageList(audioLanguage.get())
    }
  }
}
