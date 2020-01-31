package org.oppia.app.topic.lessons

import org.oppia.app.model.ChapterSummary

/** Interface to transfer the selected chapter summary to [TopicLessonsFragmentPresenter]. */
interface ChapterSummarySelector {
  fun selectChapterSummary(chapterSummary: ChapterSummary)
}
