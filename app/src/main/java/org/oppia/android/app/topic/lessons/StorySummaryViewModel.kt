package org.oppia.android.app.topic.lessons

import androidx.lifecycle.ViewModel
import org.oppia.android.app.model.StorySummary

/** [ViewModel] for displaying a story summary. */
class StorySummaryViewModel(
  val storySummary: StorySummary,
  private val storySummarySelector: StorySummarySelector,
  private val chapterSummarySelector: ChapterSummarySelector
) : TopicLessonsItemViewModel() {
  private val arrayList = ArrayList<ChapterSummaryViewModel>()

  val chapterSummaryItemList: List<ChapterSummaryViewModel> by lazy {
    getRecyclerViewItemList()
  }

  private fun getRecyclerViewItemList(): ArrayList<ChapterSummaryViewModel> {
    storySummary.chapterList.forEachIndexed { index, chapterSummary ->
      arrayList.add(
        ChapterSummaryViewModel(
          chapterPlayState = chapterSummary.chapterPlayState,
          explorationId = chapterSummary.explorationId,
          chapterName = chapterSummary.name,
          storyId = storySummary.storyId,
          index = index,
          chapterSummarySelector = chapterSummarySelector
        )
      )
    }
    return arrayList
  }

  fun clickOnStorySummaryTitle() {
    storySummarySelector.selectStorySummary(storySummary)
  }
}
