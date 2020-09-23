package org.oppia.android.app.home

/** Listener for when an activity should route to a story-item in TopicPlay tab. */
interface RouteToTopicPlayStoryListener {
  fun routeToTopicPlayStory(internalProfileId: Int, topicId: String, storyId: String)
}
