package org.oppia.app.home

/** Listener for when an activity should route to a topic. */
interface RouteToTopicPlayStoryListener {
  fun routeToTopicPlayStory(topicId: String, storyId: String)
}
