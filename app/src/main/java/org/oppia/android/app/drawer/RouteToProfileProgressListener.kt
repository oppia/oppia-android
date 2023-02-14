package org.oppia.android.app.drawer

import org.oppia.android.app.model.ProfileId

/** Listener for when an activity should route to [ProfileProgressActivity]. */
interface RouteToProfileProgressListener {
  fun routeToProfileProgress(profileId: ProfileId)
}
