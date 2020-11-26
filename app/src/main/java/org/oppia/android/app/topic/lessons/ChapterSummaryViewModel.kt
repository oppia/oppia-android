package org.oppia.android.app.topic.lessons

import androidx.lifecycle.ViewModel
import org.oppia.android.app.model.ChapterSummary
import org.oppia.android.app.viewmodel.ObservableViewModel

/** [ViewModel] for displaying a chapter summary. */
class ChapterSummaryViewModel(
  val chapterSummary: ChapterSummary,
  val storyId: String,
  val index: Int,
  private val chapterSummarySelector: ChapterSummarySelector
) : ObservableViewModel() {

  fun onClick(chapterSummary: ChapterSummary) {
    chapterSummarySelector.selectChapterSummary(storyId, chapterSummary)
  }
}
