package org.oppia.app.topic.review

import org.oppia.app.model.SkillSummary

/** Listener interface for when topic review-skill is clicked in the UI. */
interface ReviewSkillSelector {
  fun onTopicReviewSummaryClicked(skillSummary: SkillSummary)
}
