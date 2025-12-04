package org.oppia.android.app.home

import org.oppia.android.app.model.ProfileId

/** Listener for when an activity should route to a topic. */
interface RouteToTopicListener {
  fun routeToTopic(profileId: ProfileId, classroomId: String, topicId: String)
}
