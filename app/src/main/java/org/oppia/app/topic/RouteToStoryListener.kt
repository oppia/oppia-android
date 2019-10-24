package org.oppia.app.topic

/** Listener for when an [TopicActivity] should route to a [StoryActivity]. */
interface RouteToStoryListener {
  fun routeToStory(storyId: String)
}
