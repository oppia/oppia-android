package org.oppia.android.app.home

import org.oppia.android.app.model.RecentlyPlayedActivityTitle

/** Listener for when an activity should route to [RecentlyPlayedActivity]. */
interface RouteToRecentlyPlayedListener {
  fun routeToRecentlyPlayed(recentlyPlayedActivityTitle: RecentlyPlayedActivityTitle)
}
