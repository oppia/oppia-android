package org.oppia.app.topic.review

import org.oppia.app.model.SkillSummary

/** Listener for when a skill is selected for review. */
interface ReviewSkillSelector {
  fun onTopicReviewSummaryClicked(skillSummary: SkillSummary)
}
