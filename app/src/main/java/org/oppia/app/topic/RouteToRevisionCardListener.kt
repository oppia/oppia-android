package org.oppia.app.topic

/** Listener for when an [TopicActivity] should route to a [RevisionCardFragment]. */
interface RouteToRevisionCardListener {
  fun routeToRevisionCard(internalProfileId: Int, topicId: String, subtopicId: Int)
}
