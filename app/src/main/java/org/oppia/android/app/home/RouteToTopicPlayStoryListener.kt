package org.oppia.android.app.home

/** Listener for when an activity should route to a story-item in TopicPlay tab. */
interface RouteToTopicPlayStoryListener {
  fun routeToTopicPlayStory(
    internalProfileId: Int,
    classroomId: String,
    topicId: String,
    storyId: String
  )
}
