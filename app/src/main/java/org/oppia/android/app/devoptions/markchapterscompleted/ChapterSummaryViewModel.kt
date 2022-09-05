package org.oppia.android.app.devoptions.markchapterscompleted

import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.model.ChapterSummary
import org.oppia.android.app.model.EphemeralChapterSummary
import org.oppia.android.domain.translation.TranslationController

/** [MarkChaptersCompletedItemViewModel] for displaying a chapter summary. */
class ChapterSummaryViewModel(
  val chapterIndex: Int,
  ephemeralChapterSummary: EphemeralChapterSummary,
  val nextStoryIndex: Int,
  val storyId: String,
  val topicId: String,
  translationController: TranslationController
) : MarkChaptersCompletedItemViewModel() {
  val chapterSummary = ephemeralChapterSummary.chapterSummary

  val chapterTitle by lazy {
    translationController.extractString(
      chapterSummary.title, ephemeralChapterSummary.writtenTranslationContext
    )
  }

  /** Returns whether the chapter represented by the current view model is completed. */
  fun checkIfChapterIsCompleted(): Boolean =
    chapterSummary.chapterPlayState == ChapterPlayState.COMPLETED
}
