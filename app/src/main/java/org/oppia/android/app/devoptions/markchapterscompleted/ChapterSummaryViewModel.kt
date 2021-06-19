package org.oppia.android.app.devoptions.markchapterscompleted

import androidx.lifecycle.ViewModel
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.viewmodel.ObservableViewModel

/** [ViewModel] for displaying a chapter summary. */
class ChapterSummaryViewModel(
  val chapterPlayState: ChapterPlayState,
  val explorationId: String,
  val chapterName: String,
  val storyId: String,
  val index: Int,
) : ObservableViewModel() {

}