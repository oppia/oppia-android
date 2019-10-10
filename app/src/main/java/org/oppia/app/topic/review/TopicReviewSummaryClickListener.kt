package org.oppia.app.topic.review

import org.oppia.app.model.SkillSummary

/** Listener interface for when topic review-skills are clicked in the UI. */
interface TopicReviewSummaryClickListener {
  fun onTopicReviewSummaryClicked(topicReview: SkillSummary)
}
