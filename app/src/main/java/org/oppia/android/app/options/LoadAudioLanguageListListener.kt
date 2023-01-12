package org.oppia.android.app.options

import org.oppia.android.app.model.AudioLanguage

/** Listener for when an activity should load a [AudioLanguageFragment]. */
interface LoadAudioLanguageListListener {
  /**
   * Called when the user wishes to change their default audio language (where [audioLanguage] is
   * the current default language), when the app is in tablet mode.
   */
  fun loadAudioLanguageFragment(audioLanguage: AudioLanguage)
}
