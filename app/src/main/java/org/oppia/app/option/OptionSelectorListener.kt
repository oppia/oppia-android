package org.oppia.app.option

/** Interface to update the selectedSkillList in [TopicTrainFragmentPresenter]. */
interface OptionSelectorListener {
  /** This skill will get added to selectedSkillList in [TopicTrainFragmentPresenter]. */
  fun skillSelected(skillId: String)

  /** This skill will get removed from selectedSkillList in [TopicTrainFragmentPresenter]. */
  fun skillUnselected(skillId: String)
}
