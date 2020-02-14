package org.oppia.app.topic.practice

/** Interface to update the selectedSkillList in [TopicPracticeFragmentPresenter]. */
interface SkillSelector {
  /** This skill will get added to selectedSkillList in [TopicPracticeFragmentPresenter]. */
  fun skillSelected(skillId: String)

  /** This skill will get removed from selectedSkillList in [TopicPracticeFragmentPresenter]. */
  fun skillUnselected(skillId: String)
}
