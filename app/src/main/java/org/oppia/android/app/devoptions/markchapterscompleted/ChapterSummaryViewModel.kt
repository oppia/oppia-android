package org.oppia.android.app.devoptions.markchapterscompleted

import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.model.ChapterSummary

/** [MarkChaptersCompletedItemViewModel] for displaying a chapter summary. */
class ChapterSummaryViewModel(
  val chapterIndex: Int,
  val chapterSummary: ChapterSummary,
  val nextStoryIndex: Int,
  val storyId: String,
  val topicId: String
) : MarkChaptersCompletedItemViewModel() {
  /** Returns whether the chapter represented by the current view model is completed. */
  fun checkIfChapterIsCompleted(): Boolean =
    chapterSummary.chapterPlayState == ChapterPlayState.COMPLETED
}
