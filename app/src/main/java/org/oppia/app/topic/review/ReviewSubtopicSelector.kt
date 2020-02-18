package org.oppia.app.topic.review

import org.oppia.app.model.Subtopic

/** Listener for when a subtopic is selected for review. */
interface ReviewSubtopicSelector {
  fun onTopicReviewSummaryClicked(subtopic: Subtopic)
}
