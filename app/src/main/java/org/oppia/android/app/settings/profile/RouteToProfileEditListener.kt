package org.oppia.android.app.settings.profile

/** Listener when an activity should route to [ProfileEditActivity]. */
interface RouteToProfileEditListener {
  /** This method is called when an activity should route to [ProfileEditActivity]. */
  fun routeToProfileEditActivity(profileId: Int)
}
