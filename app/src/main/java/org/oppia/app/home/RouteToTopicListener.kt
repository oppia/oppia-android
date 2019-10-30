package org.oppia.app.home

/** Listener for when an activity should route to a topic. */
interface RouteToTopicListener {
  fun routeToTopic(topicId: String)
}
