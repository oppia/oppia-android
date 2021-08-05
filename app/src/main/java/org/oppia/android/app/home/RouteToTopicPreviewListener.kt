package org.oppia.android.app.home

/** Listener for when an activity should route to a topic preview. */
interface RouteToTopicPreviewListener {
  fun routeToTopicPreview(internalProfileId: Int, topicId: String)
}
