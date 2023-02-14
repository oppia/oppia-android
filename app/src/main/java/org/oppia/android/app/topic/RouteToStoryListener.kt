package org.oppia.android.app.topic

import org.oppia.android.app.model.ProfileId

/** Listener for when an [TopicActivity] should route to a [StoryActivity]. */
interface RouteToStoryListener {
  fun routeToStory(profileId: ProfileId, topicId: String, storyId: String)
}
