package org.oppia.android.app.administratorcontrols

import android.app.Activity

/** Listener when an activity should route to [ProfileEditActivity]. */
interface RouteToProfileEditListener {
  fun routeToProfileEditActivity(profileId: Int, profileName: String)
}