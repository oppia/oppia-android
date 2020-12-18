package org.oppia.android.app.topic.lessons

/** Interface to transfer the selected chapter summary to [TopicLessonsFragmentPresenter]. */
interface ChapterSummarySelector {
  fun selectChapterSummary(storyId: String, explorationId: String)
}
