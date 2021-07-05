package org.oppia.android.app.devoptions.markstoriescompleted

import androidx.lifecycle.ViewModel
import org.oppia.android.app.model.StorySummary
import org.oppia.android.app.viewmodel.ObservableViewModel

/** [ViewModel] for displaying a story summary. */
class StorySummaryViewModel(
  val storySummary: StorySummary,
  val isCompleted: Boolean,
  val topicId: String
) : ObservableViewModel()
