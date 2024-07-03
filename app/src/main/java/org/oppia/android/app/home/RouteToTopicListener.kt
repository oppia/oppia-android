package org.oppia.android.app.home

/** Listener for when an activity should route to a topic. */
interface RouteToTopicListener {
  fun routeToTopic(internalProfileId: Int, classroomId: String, topicId: String)
}
