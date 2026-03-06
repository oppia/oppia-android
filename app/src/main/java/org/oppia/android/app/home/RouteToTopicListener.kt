package org.oppia.android.app.home

import org.oppia.android.app.model.LegacyProfileId

/** Listener for when an activity should route to a topic. */
interface RouteToTopicListener {
  fun routeToTopic(profileId: LegacyProfileId, classroomId: String, topicId: String)
}
