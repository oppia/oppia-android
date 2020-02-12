package org.oppia.app.topic.review

import org.oppia.app.model.Subtopic

/** Listener for when a skill is selected for review. */
interface ReviewSelector {
  fun onTopicReviewSummaryClicked(subtopic: Subtopic)
}
