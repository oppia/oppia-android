package org.oppia.app.topic.practice

/** Interface to update the selectedSubtopicList in [TopicPracticeFragmentPresenter]. */
interface SubtopicSelector {
  /** This subtopic and skill will get added to selectedSubtopicList in [TopicPracticeFragmentPresenter]. */
  fun subtopicSelected(subtopicId: String, skillIdList: MutableList<String>)

  /** This subtopic and skill will get removed from selectedSubtopicList in [TopicPracticeFragmentPresenter]. */
  fun subtopicUnselected(subtopicId: String, skillIdList: MutableList<String>)
}
