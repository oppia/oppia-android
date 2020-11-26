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
    storySummary.chapterList.forEachIndexed { index, element ->
      arrayList.add(
        ChapterSummaryViewModel(
          chapterSummary = element,
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
