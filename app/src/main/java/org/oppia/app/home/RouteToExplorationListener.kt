package org.oppia.app.home

/** Listener for when an activity should route to a exploration. */
interface RouteToExplorationListener {
  fun routeToExploration(explorationId: String, storyId: String, topicId: String)
}
