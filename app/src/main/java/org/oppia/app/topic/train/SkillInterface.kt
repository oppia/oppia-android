package org.oppia.app.topic.train

/** Interface to update the selectedSkillList in [TopicTrainFragmentPresenter]. */
interface SkillInterface {
  /** This skill will get added to selectedSkillList in [TopicTrainFragmentPresenter] */
  fun skillSelected(skill: String)
  /** This skill will get removed from selectedSkillList in [TopicTrainFragmentPresenter] */
  fun skillUnselected(skill: String)
}