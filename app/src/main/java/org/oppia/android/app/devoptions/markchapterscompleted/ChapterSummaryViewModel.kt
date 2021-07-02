package org.oppia.android.app.devoptions.markchapterscompleted

import androidx.lifecycle.ViewModel
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.viewmodel.ObservableViewModel

/** [ViewModel] for displaying a chapter summary. */
class ChapterSummaryViewModel(
  val chapterName: String,
  val chapterPlayState: ChapterPlayState,
  val explorationId: String,
  val index: Int,
  val storyId: String
) : ObservableViewModel()
