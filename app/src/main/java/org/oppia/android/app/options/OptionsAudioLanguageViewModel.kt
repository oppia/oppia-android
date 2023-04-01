package org.oppia.android.app.options

import org.oppia.android.app.model.AudioLanguage

/** Audio language settings view model for the recycler view in [OptionsFragment]. */
class OptionsAudioLanguageViewModel(
  private val routeToAudioLanguageListListener: RouteToAudioLanguageListListener,
  private val loadAudioLanguageListListener: LoadAudioLanguageListListener,
  private val audioLanguage: AudioLanguage,
  val audioLanguageDisplayName: String
) : OptionsItemViewModel() {
  /**
   * Handles when the user wishes to change their default audio language and clicks on the button to
   * open that configuration screen/pane.
   */
  fun onAudioLanguageClicked() {
    if (isMultipane.get()!!) {
      loadAudioLanguageListListener.loadAudioLanguageFragment(audioLanguage)
    } else {
      routeToAudioLanguageListListener.routeAudioLanguageList(audioLanguage)
    }
  }
}
