package org.oppia.android.app.home

import org.oppia.android.app.model.LegacyProfileId

/** Listener for when an activity should route to a story-item in TopicPlay tab. */
interface RouteToTopicPlayStoryListener {
  fun routeToTopicPlayStory(
    profileId: LegacyProfileId,
    classroomId: String,
    topicId: String,
    storyId: String
  )
}
