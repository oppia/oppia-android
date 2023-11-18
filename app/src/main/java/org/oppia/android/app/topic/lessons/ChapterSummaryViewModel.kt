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
  val previousChapterTitle: String?,
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
          R.string.topic_fragment_lessons_chapter_completed_text,
          (index + 1).toString(),
          chapterTitle
        )
      }
      ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES -> {
        if (previousChapterTitle != null) {
          resourceHandler.getStringInLocaleWithWrapping(
            R.string.topic_fragment_lessons_chapter_locked_prerequisite_label,
            (index + 1).toString(),
            chapterTitle,
            index.toString(),
            previousChapterTitle
          )
        } else {
          resourceHandler.getStringInLocaleWithWrapping(
            R.string.topic_fragment_lessons_without_chapter_prerequisite_label
          )
        }
      }
      else -> {
        resourceHandler.getStringInLocaleWithWrapping(
          R.string.topic_fragment_lessons_chapter_in_progress_text,
          (index + 1).toString(),
          chapterTitle
        )
      }
    }
  }

  fun computePlayChapterIndexText(): String {
    return resourceHandler.getStringInLocaleWithWrapping(
      R.string.topic_fragment_lessons_chapter_play_index, (index + 1).toString()
    )
  }
}
