package org.oppia.app.topic.revision

import org.oppia.app.model.SkillSummary

/** Listener for when a skill is selected for review. */
interface RevisionSkillSelector {
  fun onTopicRevisionSummaryClicked(skillSummary: SkillSummary)
}
