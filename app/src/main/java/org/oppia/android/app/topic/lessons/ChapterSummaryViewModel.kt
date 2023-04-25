package org.oppia.android.app.topic.lessons

import androidx.lifecycle.ViewModel
import org.oppia.android.R
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel

/** [ViewModel] for displaying a chapter summary. */
class ChapterSummaryViewModel(
  val chapterPlayState: ChapterPlayState,
  val explorationId: String,
  val chapterTitle: String,
  val previousChapterTitle: String,
  val storyId: String,
  private val index: Int,
  private val chapterSummarySelector: ChapterSummarySelector,
  private val resourceHandler: AppLanguageResourceHandler,
  val storyIndex: Int
) : ObservableViewModel() {

  fun onClick(explorationId: String) {
    chapterSummarySelector.selectChapterSummary(storyId, explorationId, chapterPlayState)
  }

  fun computeChapterPlayStateIconContentDescription(): String {
    return when (chapterPlayState) {
      ChapterPlayState.COMPLETED -> {
        resourceHandler.getStringInLocaleWithWrapping(
          R.string.chapter_completed, (index + 1).toString(), chapterTitle
        )
      }
      ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES -> {
        resourceHandler.getStringInLocaleWithWrapping(
          R.string.chapter_prerequisite_title_label, (index).toString(), previousChapterTitle
        )
      }
      else -> {
        resourceHandler.getStringInLocaleWithWrapping(
          R.string.chapter_in_progress, (index + 1).toString(), chapterTitle
        )
      }
    }
  }

  fun computePlayChapterIndexText(): String {
    return resourceHandler.getStringInLocaleWithWrapping(
      R.string.topic_play_chapter_index, (index + 1).toString()
    )
  }
}
