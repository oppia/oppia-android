package org.oppia.android.app.options

import org.oppia.android.app.model.OppiaLanguage

/** Listener for when an activity should load a [AppLanguageFragment]. */
interface LoadAppLanguageListListener {
  fun loadAppLanguageFragment(oppiaLanguage: OppiaLanguage)
}
