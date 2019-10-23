package org.oppia.app.topic

/** Listener for when an activity should route to a Question Player. */
interface RouteToStoryListener {
  fun routeToStory(storyId: String)
}
