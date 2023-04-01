package org.oppia.android.app.options

import org.oppia.android.app.model.AudioLanguage

/** Listener for when an activity should route to a [AudioLanguageActivity]. */
interface RouteToAudioLanguageListListener {
  /**
   * Called when the user wishes to change their default audio language (where [audioLanguage] is
   * the current default language).
   */
  fun routeAudioLanguageList(audioLanguage: AudioLanguage)
}
