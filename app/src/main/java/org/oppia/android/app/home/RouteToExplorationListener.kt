package org.oppia.android.app.home

/** Listener for when an activity should route to a exploration. */
interface RouteToExplorationListener {
  fun routeToExploration(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    backflowScreen: Int?,
    isCheckpointingEnabled: Boolean
  )
}
