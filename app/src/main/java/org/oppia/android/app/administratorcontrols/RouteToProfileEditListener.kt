package org.oppia.android.app.administratorcontrols

/** Listener when an activity should route to [ProfileEditActivity]. */
interface RouteToProfileEditListener {
  fun routeToProfileEditActivity(profileId: Int)
}