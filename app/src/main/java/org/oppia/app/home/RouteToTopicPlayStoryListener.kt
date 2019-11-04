package org.oppia.app.home

/** Listener for when an activity should route to a story-item in TopicPlay tab. */
interface RouteToTopicPlayStoryListener {
  fun routeToTopicPlayStory(topicId: String, storyId: String)
}
