package org.oppia.android.app.options

import org.oppia.android.app.model.OppiaLanguage

/** Listener for when an activity should route to a [AppLanguageActivity]. */
interface RouteToAppLanguageListListener {
  fun routeAppLanguageList(oppiaLanguage: OppiaLanguage)
}
