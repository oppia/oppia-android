package org.oppia.android.app.preview

import org.oppia.android.app.viewmodel.ObservableViewModel

/** [ObservableViewModel] for the chapter recycler view of Topic Preview. */
class TopicPreviewChapterItemViewModel(
  val index: Int,
  val chapterName: String
) : ObservableViewModel()
