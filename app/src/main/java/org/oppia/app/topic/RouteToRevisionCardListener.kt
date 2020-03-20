package org.oppia.app.topic

/** Listener for when an [TopicActivity] should route to a [ReviewCardFragment]. */
interface RouteToRevisionCardListener {
  fun routeToReviewCard(topicId: String, subtopicId: String)
}
