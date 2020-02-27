package org.oppia.app.topic.practice

/** Interface to update the selectedSkillList in [TopicPracticeFragmentPresenter]. */
interface SubtopicSkillsSelector {
  /** This skill will get added to selectedSkillList in [TopicPracticeFragmentPresenter]. */
  fun subtopicSkillsSelected(subtopicId: String, skillIdList: MutableList<String>)

  /** This skill will get removed from selectedSkillList in [TopicPracticeFragmentPresenter]. */
  fun subtopicSkillsUnselected(
    subtopicId: String,
    skillIdList: MutableList<String>
  )
}
