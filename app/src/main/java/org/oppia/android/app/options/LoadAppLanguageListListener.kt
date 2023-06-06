package org.oppia.android.app.options

import org.oppia.android.app.model.OppiaLanguage

/** Listener for when an activity should load a [AppLanguageFragment]. */
interface LoadAppLanguageListListener {
  /** Called when [AppLanguageFragment] should be opened. */
  fun loadAppLanguageFragment(oppiaLanguage: OppiaLanguage)
}
