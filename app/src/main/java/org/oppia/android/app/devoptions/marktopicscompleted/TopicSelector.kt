package org.oppia.android.app.devoptions.marktopicscompleted

/** Interface to update the selectedTopicList in [MarkTopicsCompletedFragmentPresenter]. */
interface TopicSelector {
  /** This topic will get added to selectedTopicList in [MarkTopicsCompletedFragmentPresenter]. */
  fun topicSelected(topicId: String)

  /** This topic will get removed from selectedTopicList in [MarkTopicsCompletedFragmentPresenter]. */
  fun topicUnselected(topicId: String)
}
