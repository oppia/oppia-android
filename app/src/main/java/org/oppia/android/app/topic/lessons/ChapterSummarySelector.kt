package org.oppia.android.app.topic.lessons

import org.oppia.android.app.model.ChapterSummary

/** Interface to transfer the selected chapter summary to [TopicLessonsFragmentPresenter]. */
interface ChapterSummarySelector {
  fun selectChapterSummary(storyId: String, chapterSummary: ChapterSummary)
}
