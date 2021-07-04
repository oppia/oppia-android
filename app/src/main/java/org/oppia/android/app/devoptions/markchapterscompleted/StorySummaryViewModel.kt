package org.oppia.android.app.devoptions.markchapterscompleted

import androidx.lifecycle.ViewModel
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.model.StorySummary
import org.oppia.android.app.viewmodel.ObservableViewModel

/** [ViewModel] for displaying a story and list of chapters in a story as [ChapterSummaryViewModel] */
class StorySummaryViewModel(
  val storySummary: StorySummary
) : ObservableViewModel() {

  /**
   * List of chapter ids used in [MarkStoriesCompletedFragmentPresenter] to check if all topics are
   * selected or not.
   */
  val availableExplorationIdList = ArrayList<String>()

  /** List of [ChapterSummaryViewModel] used to populate the recyclerview to display chapters. */
  val chapterSummaryItemList: List<ChapterSummaryViewModel> by lazy {
    computeChapterSummaryItemList()
  }

  private fun computeChapterSummaryItemList(): List<ChapterSummaryViewModel> {
    return storySummary.chapterList.mapIndexed { index, chapterSummary ->
      val isCompleted = chapterSummary.chapterPlayState == ChapterPlayState.COMPLETED
      if (!isCompleted) {
        availableExplorationIdList.add(chapterSummary.explorationId)
      }
      ChapterSummaryViewModel(
        chapterSummary = chapterSummary,
        index = index,
        isCompleted = isCompleted
      )
    }
  }
}
