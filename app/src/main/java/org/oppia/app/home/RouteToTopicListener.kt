package org.oppia.app.home

/** Listener for when an activity should route to a exploration. */
interface RouteToTopicListener {
  fun routeToTopic(explorationId: String)
}
