package org.oppia.app.topic.play

import org.oppia.app.model.ChapterSummary

/** Interface to transfer the selected chapter summary to [TopicPlayFragmentPresenter]. */
interface ChapterSummarySelector {
  fun selectChapterSummary(chapterSummary: ChapterSummary, storyId: String)
}
