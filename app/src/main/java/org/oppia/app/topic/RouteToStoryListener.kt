package org.oppia.app.topic

/** Listener for when an activity should route to a Story. */
interface RouteToStoryListener {
  fun routeToStory(storyId: String)
}
