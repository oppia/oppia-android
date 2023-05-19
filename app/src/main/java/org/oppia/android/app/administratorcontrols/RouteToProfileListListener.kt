package org.oppia.android.app.administratorcontrols

import org.oppia.android.app.settings.profile.ProfileListActivity

/** Listener for when an activity should route to a exploration. */
interface RouteToProfileListListener {
  /** Called when [ProfileListActivity] should be loaded. **/
  fun routeToProfileList()
}
