package org.oppia.android.app.home

import org.oppia.android.app.player.exploration.ParentScreenForExplorationEnum

/** Listener for when an activity should route to a exploration. */
interface RouteToExplorationListener {
  fun routeToExploration(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    parentScreenForExplorationEnum: ParentScreenForExplorationEnum
  )
}
