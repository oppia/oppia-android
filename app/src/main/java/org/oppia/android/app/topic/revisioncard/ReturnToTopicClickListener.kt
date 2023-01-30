package org.oppia.android.app.topic.revisioncard

/** Listener for when user wishes to navigate back from a revision card to the topic screen. */
interface ReturnToTopicClickListener {
  /** Indicates that the user wishes to return to the topic screen from a revision card. */
  fun onReturnToTopicRequested()
}
