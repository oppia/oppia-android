package org.oppia.app.topic

/** Listener for when an [TopicActivity] should route to a [StoryActivity]. */
interface RouteToStoryListener {
  fun routeToStory(internalProfileId: Int, topicId: String, storyId: String)
}
