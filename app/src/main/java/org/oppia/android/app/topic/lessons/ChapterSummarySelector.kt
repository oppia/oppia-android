package org.oppia.android.app.topic.lessons

import org.oppia.android.app.model.ChapterPlayState

/** Interface to transfer the selected chapter summary to [TopicLessonsFragmentPresenter]. */
interface ChapterSummarySelector {
  fun selectChapterSummary(
    storyId: String,
    explorationId: String,
    chapterPlayState: ChapterPlayState
  )
}
