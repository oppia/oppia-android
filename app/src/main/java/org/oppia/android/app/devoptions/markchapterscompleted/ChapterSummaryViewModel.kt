package org.oppia.android.app.devoptions.markchapterscompleted

import androidx.lifecycle.ViewModel
import org.oppia.android.app.model.ChapterSummary
import org.oppia.android.app.viewmodel.ObservableViewModel

/** [ViewModel] for displaying a chapter summary. */
class ChapterSummaryViewModel(
  val chapterSummary: ChapterSummary,
  val index: Int,
  val isCompleted: Boolean
) : ObservableViewModel()
