package org.oppia.app.topic

/** Listener for when an [TopicActivity] should route to a review. */
interface RouteToReviewListener {
  fun routeToReview(skillId: String)
}
