package org.oppia.android.app.home

import org.oppia.android.app.model.ProfileId

/** Listener for when an activity should route to a story-item in TopicPlay tab. */
interface RouteToTopicPlayStoryListener {
  fun routeToTopicPlayStory(
    profileId: ProfileId,
    classroomId: String,
    topicId: String,
    storyId: String
  )
}
