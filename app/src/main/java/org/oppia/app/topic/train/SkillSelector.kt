package org.oppia.app.topic.train

import org.oppia.app.model.SkillSummary

/** Interface to update the selectedSkillList in [TopicTrainFragmentPresenter]. */
interface SkillSelector {
  /** This skill will get added to selectedSkillList in [TopicTrainFragmentPresenter]. */
  fun skillSelected(skill: SkillSummary)
  /** This skill will get removed from selectedSkillList in [TopicTrainFragmentPresenter]. */
  fun skillUnselected(skill: SkillSummary)
}
