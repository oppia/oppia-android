package org.oppia.app.topic

/** Listener for when an [TopicActivity] should route to a [RevisionCardFragment]. */
interface RouteToRevisionCardListener {
  fun routeToRevisionCard(topicId: String, subtopicId: String)
}
