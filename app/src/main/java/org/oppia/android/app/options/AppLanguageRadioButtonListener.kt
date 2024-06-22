package org.oppia.android.app.options

import org.oppia.android.app.model.OppiaLanguage

/** Listener for when a language is selected for the [AppLanguageFragment]. */
interface AppLanguageRadioButtonListener {
  /** Called when the user selected a new app language to use as their default preference. */
  fun onLanguageSelected(appLanguage: OppiaLanguage)
}
