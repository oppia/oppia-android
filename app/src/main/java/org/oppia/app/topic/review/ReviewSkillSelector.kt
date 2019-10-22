package org.oppia.app.topic.review

import org.oppia.app.model.SkillSummary

/** Listener for topic review-skill click. */
interface ReviewSkillSelector {
  fun onTopicReviewSummaryClicked(skillSummary: SkillSummary)
}
