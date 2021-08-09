package org.oppia.android.app.home

/** Listener for when an activity should route to a topic preview. */
interface RouteToTopicPreviewListener {
  /** Helps to start TopicPreviewActivity. */
  fun routeToTopicPreview(internalProfileId: Int, topicId: String)
}
