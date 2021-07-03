package org.oppia.android.app.devoptions.markchapterscompleted

import androidx.lifecycle.ViewModel
import org.oppia.android.app.model.StorySummary
import org.oppia.android.app.viewmodel.ObservableViewModel

/** [ViewModel] for displaying a story and list of chapters in a story as [ChapterSummaryViewModel] */
class StorySummaryViewModel(
  val storySummary: StorySummary
) : ObservableViewModel() {

  /** List of [ChapterSummaryViewModel] used to populate the recyclerview to display chapters. */
  val chapterSummaryItemList: List<ChapterSummaryViewModel> by lazy {
    computeChapterSummaryItemList()
  }

  private fun computeChapterSummaryItemList(): List<ChapterSummaryViewModel> {
    return storySummary.chapterList.mapIndexed { index, chapterSummary ->
      ChapterSummaryViewModel(
        chapterName = chapterSummary.name,
        chapterPlayState = chapterSummary.chapterPlayState,
        explorationId = chapterSummary.explorationId,
        index = index,
        storyId = storySummary.storyId
      )
    }
  }
}
